package com.test

import java.time.LocalDateTime
import java.util.UUID

sealed trait Role
case object Admin extends Role
case object Moderator extends Role
case object User extends Role

case class Account(id: UUID,
                   name: String,
                   email: String,
                   role: Role,
                   createdAt: LocalDateTime,
                   balance: Int = 0,
                   isConfirmed: Boolean = false,
                   closedAt: Option[LocalDateTime] = None,
                   closingReason: Option[String] = None)
