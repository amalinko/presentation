package com.test

import java.time.LocalDateTime
import java.util.UUID

sealed trait Role
case object Admin extends Role
case object Moderator extends Role
case object User extends Role

sealed trait AccountState{
  def createdAt: LocalDateTime
}
case class NotConfirmed(createdAt: LocalDateTime) extends AccountState
case class Active(createdAt: LocalDateTime) extends AccountState
case class Closed(createdAt: LocalDateTime, closedAt: LocalDateTime, closingReason: String) extends AccountState

case class Account(id: UUID, name: String, email: Email, role: Role, balance: Int, accountState: AccountState)
