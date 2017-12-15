package com.test

import java.util.UUID

import scala.concurrent.Future

class ConfirmationRepository {
  def find(accountId: UUID): Future[String] = ???
}
