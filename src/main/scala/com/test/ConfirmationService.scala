package com.test

import java.util.UUID

import scala.concurrent.Future

class ConfirmationService(repository: AccountRepository, confirmationRepository: ConfirmationRepository) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def startConfirmation(id: UUID, email: Email): Future[Unit] = ???

  def confirm(id: UUID, code: String): Future[Unit] = {
    for {
      maybeAccount <- repository.find(id)
      existentCode <- confirmationRepository.find(id)
      confirmed = maybeAccount match {
        case Some(a) if code == existentCode => a.confirm()
        case _ => throw new Exception
      }
      _ <- repository.save(confirmed)
    } yield ()
  }

}
