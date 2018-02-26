package com.datio.futurakka.utils

/**
  * Created by AlvaroNav on 18/2/18.
  */
object DumbFunctions {

  def toJSON (input:String): String = {
    s"""{"data" : "${input}"}"""
  }

  def getTime(): Long ={
    System.nanoTime() / 3600
  }
}
