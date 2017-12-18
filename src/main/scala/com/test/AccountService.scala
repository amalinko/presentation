package com.test

import java.time.{Clock, LocalDateTime}
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
    val createdAt = LocalDateTime.now(clock)
    val account = Account(id, name, email, role = User, balance = 0, accountState = NotConfirmed(createdAt))
    for {
      _ <- repository.save(account)
      _ <- eventDispatcher.dispatch(AccountCreated(id))
    } yield id
  }

  def encourage(id: UUID, points: Int): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      updatedAccount = forActiveAndExistentAccount(maybeAccount) { a =>
        addToBalance(a, points)
      }
      _ <- repository.save(updatedAccount)
      _ <- postEventIfAccountHasMaxBalance(updatedAccount)
    } yield ()
  }

  def fine(id: UUID, points: Int): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      updatedAccount = forActiveAndExistentAccount(maybeAccount) { a =>
        removeFromBalance(a, points)
      }
      _ <- repository.save(updatedAccount)
      _ <- postEventIfClosed(updatedAccount)
    } yield ()
  }

  def close(id: UUID, reason: String): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      updatedAccount = forActiveAndExistentAccount(maybeAccount) { a =>
        a.accountState match {
          case _: Closed => throw new Exception
          case other => a.copy(accountState = Closed(other.createdAt, LocalDateTime.now(clock), reason))
        }
      }
      _ <- repository.save(updatedAccount)
      _ <- postEventIfClosed(updatedAccount)
    } yield ()
  }

  private def forActiveAndExistentAccount(maybeAccount: Option[Account])(f: Account => Account): Account =
    maybeAccount match {
      case Some(a) =>
        a.accountState match {
          case _: Active => f(a)
          case _ => throw new Exception
        }
      case _ => throw new Exception
    }

  private def addToBalance(account: Account, points: Int): Account = {
    if (points < 0) {
      throw new Exception
    } else {
      val newBalance =
        Math.min(account.balance + points, AccountService.MaxBalance)
      account.copy(balance = newBalance)
    }
  }

  private def removeFromBalance(account: Account, points: Int): Account = {
    if (points < 0) {
      throw new Exception
    } else {
      val newBalance = account.balance - points
      if (newBalance < 0) {
        if (account.role == Admin || account.role == Moderator) {
          account.copy(balance = 0)
        } else {
          account.copy(
            accountState = Closed(account.accountState.createdAt, LocalDateTime.now(clock), "Bad user"),
            balance = newBalance
          )
        }
      } else {
        account.copy(balance = newBalance)
      }
    }
  }

  private def postEventIfClosed(account: Account): Future[Unit] = {
    account.accountState match {
      case _: Closed =>
        eventDispatcher.dispatch(AccountClosed(account.id))
      case _ =>
        Future.successful(())
    }
  }

  private def postEventIfAccountHasMaxBalance(account: Account): Future[Unit] = {
    if (account.balance >= AccountService.MaxBalance) {
      eventDispatcher.dispatch(AccountHasMaxBalance(account.id))
    } else {
      Future.successful(())
    }
  }

}
