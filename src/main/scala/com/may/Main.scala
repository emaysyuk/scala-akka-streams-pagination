package com.may

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

object Main {
  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit = {
    val behavior = Behaviors.setup[Behavior[_]] { context =>
      implicit val sys: ActorSystem[_] = context.system
      implicit val ex: ExecutionContext = sys.executionContext

      val client = new CatsHttpClientImpl()
      client.getAllBreads.map { seq =>
        seq.foreach(cat => logger.info(cat.toString))
        sys.terminate()
      }

      Behaviors.same
    }

    val rootSystem = ActorSystem(guardianBehavior = behavior, name = "CatsSystem")
    rootSystem.whenTerminated.onComplete(_ => logger.info("ActorSystem terminated"))(rootSystem.executionContext)
    Await.ready(rootSystem.whenTerminated, 2.minutes) // in case sbt run
  }
}