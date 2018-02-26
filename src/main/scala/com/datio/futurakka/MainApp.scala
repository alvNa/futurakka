package com.datio.futurakka

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.datio.futurakka.actor._
import akka.pattern.ask
import com.datio.futurakka.services.{SuicideService, SupplyService, WorkService}

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success}


/**
  * App entrypoint
  */
object MainApp extends App {
  implicit val system = ActorSystem("futurama")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(10 seconds)
  val log = Logging(system, getClass)

  log.info(s">>> ${getClass.getName()} Welcome to the wolrd of tomorrow!!")
/*
  val humanActor = system.actorOf(HumanActor.props, "Professor_Farnsworth")
  */
  val robotActor = system.actorOf(RobotActor.props, "Bender")
  val robotActor2 = system.actorOf(RobotActor.props, "Calculon")

  //Actor's first call with a message
  /*humanActor ? "Good news everyone!"*/
  //robotActor ? "Bite my shiny metal ass"
  //val req = SuicideRequest(SuicideType.PhoneCall)
  val req = CombinedRequest()
  val req2 = FastRequest("")

  val f1 = (robotActor ? req).mapTo[String]
  val f2 = (robotActor2 ? req2).mapTo[String]

  val p1 = Promise[String]()

  log.info(s"<<< Robot1 request $req")
  log.info(s"<<< Robot2 request $req2")

  f1 onComplete {
    case Success(resp) => {
      log.info(s">>> ---------------------------------")
      log.info(s">>> Robot1 receiving message: $resp")
      log.info(s">>> ---------------------------------")
      p1.completeWith(f2)
    }
    case Failure(t: Throwable) => log.error(s"Shit, something went wrong: $t")
  }

  p1.future onComplete {
    case Success(resp) => {
      log.info(s">>> ---------------------------------")
      log.info(s">>> Robot2 receiving message: $resp")
      log.info(s">>> ---------------------------------")
    }
    case Failure(t: Throwable) => log.error(s"Shit, something went wrong: $t")
  }

/* Nested way
  f1 onComplete {
    case Success(resp:String) => {
      log.info(s">>> ---------------------------------")
      log.info(s">>> Robot1 receiving message: $resp")
      log.info(s">>> ---------------------------------")

      log.info(s"<<< robot request $req2")
      f2 onComplete {
        case Success(resp) => {
          log.info(s">>> ---------------------------------")
          log.info(s">>> Robot2 receiving message: $resp")
          log.info(s">>> ---------------------------------")
        }
        case Failure(t: Throwable) => println(s"Shit, something went wrong: $t")
      }

    }
    case Failure(t: Throwable) => println(s"Shit, something went wrong: $t")
  }*/

  system.terminate

}
