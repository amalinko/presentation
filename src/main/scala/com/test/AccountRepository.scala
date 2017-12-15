package com.test

import java.util.UUID

import scala.concurrent.Future

class AccountRepository {
  def save(account: Account): Future[Unit] = ???
  def find(id: UUID): Future[Option[Account]] = ???
}
