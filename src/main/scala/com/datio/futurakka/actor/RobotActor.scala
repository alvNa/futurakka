package com.datio.futurakka.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.datio.futurakka.services.{SuicideService, SupplyService, WorkService}
import com.datio.futurakka.utils.DumbFunctions

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

object RobotActor {
  def props(): Props = Props(classOf[RobotActor])
}

class RobotActor extends Actor with ActorLogging {
  def receive: Receive = {
    case x: String =>
      log.info(s"Robot receiving message $x")
    case suicide: SuicideRequest => commitSuicide(suicide.sucType)
    case drinkReq: DrinkRequest => drink(drinkReq.drink)
    case _: FastRequest => doFast()
    case _: CombinedRequest => chainedActions()
      //linkActions()
      //combinedActions()
    case _ =>
      log.info("Default message")
  }

  private def commitSuicide(suicideType: SuicideType.SuicideType): Future[String] = {
    val fut: Future[String] = suicideType match {
      case SuicideType.PhoneCall => SuicideService.phoneCall()
      case SuicideType.Stab => SuicideService.stab()
    }

    fut pipeTo sender()
  }

  private def drink(drink: String) = {
    SupplyService.drink(drink) pipeTo sender()
  }

  /**
    * Actions performed in parallel using for comprehension
    **/
  private def combinedActions(): Future[String] = {

    val combinedFuture = for {
      res1 <- WorkService.bend("metal")
      res2 <- SupplyService.drink("alcohol")
      res3 <- SuicideService.stab()
    } yield (s"\n work: ${res1}\n drink: ${res2}\n suicide: ${res3}")

    combinedFuture pipeTo sender()
  }

  /**
    * Chaining futures with promises
    *
    * @return
    */
  private def chainedActions(): Future[String] = {

    val f1 = WorkService.bend("metal")
    val f2 = SupplyService.drink("alcohol")
    val f3 = SuicideService.stab()

    val drinkPromise = Promise[String]()
    val suicidePromise = Promise[String]()

    f1 onSuccess {
      case x => println(s"future1 resolved : ${x}")
        drinkPromise.completeWith(f2)
    }
    f2 onSuccess {
      case x => println(s"future2 resolved : ${x}")
        suicidePromise.completeWith(f3)
    }

    suicidePromise.future pipeTo sender()
  }

  private def chainedActions2(): Future[String] = {

    val f1 = WorkService.bend("metal")
    val f2 = SupplyService.drink("alcohol")
    val f3 = SuicideService.stab()

    f1 andThen {
      case x => f2
    }

    val drinkPromise = Promise[String]()
    val suicidePromise = Promise[String]()

    f1 onSuccess {
      case x => println(s"future1 resolved : ${x}")
        drinkPromise.completeWith(f2)
    }
    f2 onSuccess {
      case x => println(s"future2 resolved : ${x}")
        suicidePromise.completeWith(f3)
    }

    suicidePromise.future pipeTo sender()
  }

  /**
    * Chaining actions to a sequence of futures
    *
    * @return
    */
  private def linkActions(): Future[String] = {

    val f1 = WorkService.bend("metal")
    val f2 = SupplyService.drink("alcohol")
    val f3 = SuicideService.stab()


    val originalSender = sender()
    //onComplete When this future is completed, either through an exception, or a value, apply the provided function.
    //If the future has already been completed, this will either be applied immediately or be scheduled asynchronously.

    //Chaining futures nesting them (ugly way)
    f1 onSuccess {
      case _ => f2 onSuccess {
        case _ => f3 pipeTo originalSender
      }
    }

    // AndThen Applies the side-effecting function to the result of this future, and returns a new future with the result of this future.
    //This method allows one to enforce that the callbacks are executed in a specified order.
    f1 andThen {
      case x => {
        println(Console.BLUE + DumbFunctions.toJSON(x.getOrElse("")))
        f2 andThen {
          case y => {
            println(Console.GREEN + DumbFunctions.toJSON(y.getOrElse("")))
            f3 andThen {
              case z => println(Console.MAGENTA + s"${DumbFunctions.getTime()} ${z.get}" + Console.BLACK)
            }
          }
        }
      }
    }
  }

  /**
    * Chaining actions to a sequence of futures
    *
    * @return
    */
  private def doFast(): Future[String] = {
    val f1 = Promise[String].future // not resolved future 1
    val f3 = SupplyService.drink("alcohol") //the only resolved future
    val f2 = Promise[String].future // not resolved future 2

    Future.firstCompletedOf(Seq(f1, f2, f3)) pipeTo sender()
  }

}


object SuicideType extends Enumeration {
  type SuicideType = Value
  val Stab, PhoneCall = Value
}

import com.datio.futurakka.actor.SuicideType._

case class SuicideRequest(sucType: SuicideType)

case class DrinkRequest(drink: String)

case class CombinedRequest()

case class FastRequest(x: String)