package com.seglo

import java.util
import java.util.Map.Entry

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.ByteString
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.Future

object SlowConsumer extends App {
  val configFile = args.headOption.getOrElse("application.conf")
  val conf = ConfigFactory.load(configFile)

  val kafkaHost = conf.getString("kafkaHost")
  val topic = conf.getString("topic")
  val consumerGroup = conf.getString("consumerGroup")
  val clientId = conf.getString("clientId")
  val consumeMessagePerSecond = conf.getInt("consumeMessagePerSecond")
  val commitGroupSize = conf.getInt("commitGroupSize")

  implicit val system: ActorSystem = ActorSystem("SlowConsumer")
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(kafkaHost)
    .withGroupId(consumerGroup)
    .withClientId(clientId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


  var messageCount = 0

  type KCommittableMessage = ConsumerMessage.CommittableMessage[Array[Byte], Array[Byte]]


  /**
    * Use akka streams back-pressure to throttle the message per second of the stream.
    * http://doc.akka.io/docs/akka/2.4/scala/stream/stream-quickstart.html#Time-Based_Processing
    */
  val messagesPerSecondThrottle: Flow[KCommittableMessage, KCommittableMessage, NotUsed] =
    Flow[KCommittableMessage].throttle(
      // number of elements (messages) to allow per unit of time
      elements = consumeMessagePerSecond,
      // per unit time
      per = 1.second,
      maximumBurst = consumeMessagePerSecond,
      // use Shaping instead of Enforcing so that stream will continue even when Source exceeds limits
      mode = ThrottleMode.Shaping
    )

  // load each partition offset from last event sourced message for kafka "exactly-once" semantics
  val subscription = Subscriptions.topics(topic)
  val consume =
    Consumer.committableSource(consumerSettings, subscription)
      .via(messagesPerSecondThrottle)
      .map { (msg: KCommittableMessage) =>
        println(s"value: ${msg.record.value()}")
        messageCount += 1
        msg
      }
      .groupedWithin(commitGroupSize, 10.seconds)
      .map { group =>
        group.foldLeft(CommittableOffsetBatch.empty: CommittableOffsetBatch) { (batch, elem) => batch.updated(elem.committableOffset) }
      }
      .map { batch =>
        val set: util.Set[Entry[ConsumerMessage.GroupTopicPartition, Long]] = batch.getOffsets().entrySet()
        val offsets = set.toArray.map { entry =>
          val e = entry.asInstanceOf[Entry[ConsumerMessage.GroupTopicPartition, Long]]
          val p = e.getKey.partition
          val o = e.getValue
          s"Partition: $p, Offset: $o"
        }
        //println(s"Consumed $messageCount records.  ${offsets.sorted.mkString(", ")}")
        batch
      }
      .map(_.commitScaladsl())
      .runWith(Sink.ignore)

  consume.foreach(_ => println("consume complete"))(scala.concurrent.ExecutionContext.Implicits.global)
}
