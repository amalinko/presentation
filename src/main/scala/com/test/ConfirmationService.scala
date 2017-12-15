package com.test

import java.time.{Clock, LocalDateTime}
import java.util.UUID

import scala.concurrent.Future

class ConfirmationService(repository: AccountRepository,
                          confirmationRepository: ConfirmationRepository,
                          clock: Clock) {
  def startConfirmation(id: UUID, email: String): Future[Unit] = ???
  def confirm(id: UUID, code: String): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      account = maybeAccount match {
        case Some(a) => a
        case None    => throw new Exception
      }
      existentCode <- confirmationRepository.find(id)
      confirmedAccount = doConfirm(account, code, existentCode)
      _ <- repository.save(confirmedAccount)
    } yield ()
  }

  private def doConfirm(account: Account,
                        code: String,
                        existentCode: String): Account = {
    if (account.confirmedAt.isEmpty && account.closedAt.isEmpty && code == existentCode)
      account.copy(confirmedAt = Some(LocalDateTime.now(clock)))
    else throw new Exception
  }
}
