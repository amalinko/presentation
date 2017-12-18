package com.test

object Email {
  def apply(email: String): Email = {
    // validation
    new Email(email)
  }
}

case class Email(email: String)
