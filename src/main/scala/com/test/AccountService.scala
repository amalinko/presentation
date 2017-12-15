package com.test

import java.time.{Clock, LocalDateTime}
import java.util.UUID

import scala.concurrent.Future

object AccountService {
  val MaxBalance = 1000
}

class AccountService(repository: AccountRepository,
                     confirmationService: ConfirmationService,
                     emailService: EmailService,
                     clock: Clock) {

  def create(name: String, email: String): Future[UUID] = {
    val id = UUID.randomUUID()
    val createdAt = LocalDateTime.now(clock)
    val account = Account(id, name, email, role = User, createdAt = createdAt)
    for {
      _ <- repository.save(account)
      _ <- confirmationService.startConfirmation(id, email)
    } yield id
  }

  def encourage(id: UUID, points: Int): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      updatedAccount = forActiveAndExistentAccount(maybeAccount) { a =>
        addToBalance(a, points)
      }
      _ <- repository.save(updatedAccount)
      _ <- sendMailIfAccountHasMaxBalance(updatedAccount)
    } yield ()
  }

  def fine(id: UUID, points: Int): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      updatedAccount = forActiveAndExistentAccount(maybeAccount) { a =>
        removeFromBalance(a, points)
      }
      _ <- repository.save(updatedAccount)
      _ <- sendMailIfClosed(updatedAccount)
    } yield ()
  }

  def close(id: UUID, reason: String): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      updatedAccount = forActiveAndExistentAccount(maybeAccount) { a =>
        if (a.closedAt.isDefined) {
          throw new Exception
        } else {
          a.copy(
            closedAt = Some(LocalDateTime.now(clock)),
            closingReason = Some(reason)
          )
        }
      }
      _ <- repository.save(updatedAccount)
      _ <- sendMailIfClosed(updatedAccount)
    } yield ()
  }

  private def forActiveAndExistentAccount(maybeAccount: Option[Account])(
      f: Account => Account): Account = maybeAccount match {
    case Some(a) if a.confirmedAt.isDefined => f(a)
    case _                                  => throw Exception
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
            closedAt = Some(LocalDateTime.now(clock)),
            balance = newBalance
          )
        }
      } else {
        account.copy(balance = newBalance)
      }
    }
  }

  private def sendMailIfClosed(account: Account): Future[Unit] = {
    if (account.closedAt.nonEmpty) {
      emailService.sendEmail(
        account.email,
        body = "Yours account has been closed for inappropriate behavior")
    } else {
      Future.successful(())
    }
  }

  private def sendMailIfAccountHasMaxBalance(account: Account): Future[Unit] = {
    if (account.balance >= AccountService.MaxBalance) {
      emailService.sendEmail(account.email,
                             body = "You are great user. Thank you!")
    } else {
      Future.successful(())
    }
  }

}
