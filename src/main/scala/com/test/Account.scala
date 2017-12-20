package com.test

import java.time.{Clock, LocalDateTime}
import java.util.UUID

sealed trait Role
case object Admin extends Role
case object Moderator extends Role
case object User extends Role

sealed trait AccountState {
  def createdAt: LocalDateTime
}
case class NotConfirmed(createdAt: LocalDateTime) extends AccountState
case class Active(createdAt: LocalDateTime) extends AccountState
case class Closed(createdAt: LocalDateTime, closedAt: LocalDateTime, closingReason: String) extends AccountState

sealed trait AccountEvent extends Event
case class AccountCreated(id: UUID) extends AccountEvent
case class AccountClosed(id: UUID) extends AccountEvent
case class AccountHasMaxBalance(id: UUID) extends AccountEvent

object Account {
  val MaxBalance = 100

  def create(id: UUID, name: String, email: Email, role: Role, clock: Clock): Account =
    new Account(id, name, email, role, balance = 0, accountState = NotConfirmed(LocalDateTime.now(clock)))

}

case class Account(id: UUID, name: String, email: Email, role: Role, balance: Int, accountState: AccountState) {

  def encourage(points: Int): (Account, Option[AccountEvent]) = {
    if (points < 0) {
      throw new Exception
    } else {
      val newBalance = Math.min(balance + points, AccountService.MaxBalance)
      val updated = copy(balance = newBalance)
      if (updated.balance == Account.MaxBalance) (updated, Some(AccountHasMaxBalance(id))) else (updated, None)
    }
  }

  def fine(points: Int, clock: Clock): (Account, Option[AccountEvent]) = {
    val updated = if (points < 0) {
      throw new Exception
    } else {
      val newBalance = balance - points
      if (newBalance < 0) {
        if (role == Admin || role == Moderator) {
          copy(balance = 0)
        } else {
          copy(
            accountState = Closed(accountState.createdAt, LocalDateTime.now(clock), "Bad user"),
            balance = newBalance
          )
        }
      } else {
        copy(balance = newBalance)
      }
    }
    updated.accountState match {
      case _: Closed => (updated, Some(AccountClosed(id)))
      case _ => (updated, None)
    }
  }

  def close(reason: String, clock: Clock): (Account, Option[AccountEvent]) = accountState match {
    case _: Closed =>
      throw new Exception
    case other =>
      val closed = copy(accountState = Closed(other.createdAt, LocalDateTime.now(clock), reason))
      (closed, Some(AccountClosed(id)))
  }

}
