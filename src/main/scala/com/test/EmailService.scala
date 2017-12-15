package com.test

import scala.concurrent.Future

class EmailService {
  def sendEmail(email: String, body: String): Future[Unit] = ???
}
