package com.datio.futurakka.services

import scala.concurrent.Future


object SupplyService extends SupplyService {

}

trait SupplyService {

  def drink(drinkType:String): Future[String] = {

    if (drinkType=="water") {
      Future.successful {
        "Healthy for humans"
      }
    }
    else if (drinkType=="alcohol"){
      Future.successful {
        "Healthy for robots"
      }
    }
    else Future.failed(new UnsupportedOperationException())
  }

  def food(): Future[String] = {
    Future.successful {
      "Yami yami"
    }
  }

  def cigars(money:Int) : Future[String] = {
    if (money<30) Future.failed[String](new ArithmeticException("No money no supply"))
    else if (money>=30 && money <100) Future.successful("Normal cigar")
    else if (money <300) Future.successful("Zuban cigar")
    else Future.failed(new UnsupportedOperationException())
  }
}