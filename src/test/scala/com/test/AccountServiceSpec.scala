package com.test

import java.util.UUID

import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures

class AccountServiceSpec extends FlatSpec with ScalaFutures {

  "create" should "save created account and start confirmation" in new Fixture {
    val id = UUID.randomUUID()
    val expectedAccount: Account = ???
    givenAccountCanBeSaved(expectedAccount)
    givenEventCanBePosted()

    accountService.create("some name", Email("example@email.com")).futureValue

    verifyEventPosted(AccountCreated(id))
  }

  "encourage" should "should add points to account balance and post event if account balance reached max value" in new Fixture {
    val account: Account = ???
    val expectedAccount = account.copy(balance = Account.MaxBalance)
    givenThereIsAccount(account)
    givenAccountCanBeSaved(expectedAccount)
    givenEventCanBePosted()

    accountService
      .encourage(account.id, Account.MaxBalance)
      .futureValue

    verifyEventPosted(AccountHasMaxBalance(account.id))
  }

  "fine" should "subtract point from account's balance" in new Fixture {}

  "close" should "close account" in new Fixture {}

  class Fixture {
    val accountService: AccountService = ???

    def givenThereIsAccount(account: Account) = ???
    def givenAccountCanBeSaved(expectedAccount: Account) = ???
    def givenEventCanBePosted() = ???
    def verifyEventPosted(event: Event) = ???
    def verifyAccountNotSaved() = ???
  }

}
