package com.example

import com.example.Constant.sampleContent

object Constant {

  var sampleContent = "SAMPLE"


  val resp = (0 to 250000).map(_ => {
    sampleContent + "\n" + sampleContent
  })

  println(resp.length)
  val fileContent = resp.mkString

}
