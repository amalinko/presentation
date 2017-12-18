package com.test

import java.time.{Clock, LocalDateTime}
import java.util.UUID

import scala.concurrent.Future

class ConfirmationService(repository: AccountRepository, confirmationRepository: ConfirmationRepository, clock: Clock) {
  import scala.concurrent.ExecutionContext.Implicits.global

  def startConfirmation(id: UUID, email: Email): Future[Unit] = ???
  def confirm(id: UUID, code: String): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      account = maybeAccount match {
        case Some(a) => a
        case None => throw new Exception
      }
      existentCode <- confirmationRepository.find(id)
      confirmedAccount = doConfirm(account, code, existentCode)
      _ <- repository.save(confirmedAccount)
    } yield ()
  }

  private def doConfirm(account: Account, code: String, existentCode: String): Account = {
    account.accountState match {
      case NotConfirmed(createdAt) =>
        account.copy(accountState = Active(createdAt))
      case _ => throw new Exception
    }
  }
}
