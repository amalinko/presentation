package com.test

import scala.concurrent.Future

class EmailService {
  def sendEmail(email: Email, body: String): Future[Unit] = ???
}
