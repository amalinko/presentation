package com.test

import java.time.Clock
import java.util.UUID

import scala.concurrent.Future

object AccountService {
  val MaxBalance = 1000
}

class AccountService(idFactory: () => UUID,
                     repository: AccountRepository,
                     eventDispatcher: EventDispatcher,
                     clock: Clock) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def create(name: String, email: Email): Future[UUID] = {
    val id = idFactory()
    val account = Account.create(id, name, email, role = User, clock = clock)
    for {
      _ <- repository.save(account)
      _ <- eventDispatcher.dispatch(AccountCreated(id))
    } yield id
  }

  def encourage(id: UUID, points: Int): Future[Unit] = updateExistingAccount(id) { account =>
    account.encourage(points)
  }

  def fine(id: UUID, points: Int): Future[Unit] = updateExistingAccount(id) { account =>
    account.fine(points, clock)
  }

  def close(id: UUID, reason: String): Future[Unit] = updateExistingAccount(id) { account =>
    account.close(reason, clock)
  }

  private def updateExistingAccount(id: UUID)(f: Account => (Account, Option[Event])): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      account = maybeAccount.getOrElse(throw new Exception)
      (updated, maybeEvent) = f(account)
      _ <- repository.save(updated)
      _ <- maybeEvent match {
        case Some(event) => eventDispatcher.dispatch(event)
        case None => Future.unit
      }
    } yield ()
  }

}
