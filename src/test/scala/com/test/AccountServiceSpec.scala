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

  "encourage" should "add points to user balance" in new Fixture {
    val account: Account = ???
    val expectedAccount = account.copy(balance = 100)
    givenThereIsAccount(account)
    givenAccountCanBeSaved(expectedAccount)

    accountService.encourage(account.id, 100).futureValue
  }

  it should "not add negative points" in new Fixture {
    val account: Account = ???
    val expectedAccount = account.copy(balance = 100)
    givenThereIsAccount(account)
    givenAccountCanBeSaved(expectedAccount)

    intercept[Exception] {
      accountService.encourage(account.id, -1).futureValue
    }
  }

  it should "limit balance with MaxBalance value" in new Fixture {
    val account: Account = ???
    val expectedAccount = account.copy(balance = AccountService.MaxBalance)
    givenThereIsAccount(account)
    givenAccountCanBeSaved(expectedAccount)
    givenEventCanBePosted()

    accountService
      .encourage(account.id, AccountService.MaxBalance + 1)
      .futureValue
  }

  it should "dispatch event if balance reached max value" in new Fixture {
    val account: Account = ???
    val expectedAccount = account.copy(balance = AccountService.MaxBalance)
    givenThereIsAccount(account)
    givenAccountCanBeSaved(expectedAccount)
    givenEventCanBePosted()

    accountService
      .encourage(account.id, AccountService.MaxBalance + 1)
      .futureValue

    verifyEventPosted(AccountHasMaxBalance(account.id))
  }

  it should "throw error if account is not confirmed" in new Fixture {
    val notConfirmedAccount: Account = ???
    givenThereIsAccount(notConfirmedAccount)

    intercept[Exception] {
      accountService.encourage(notConfirmedAccount.id, -1).futureValue
    }
    verifyAccountNotSaved()
  }

  "fine" should "subtract point from account's balance" in new Fixture {}

  it should "fail on negative points" in new Fixture {}

  it should "close account if after subtraction balance is negative" in new Fixture {}

  it should "send email if account is closed" in new Fixture {}

  it should "not close account if account has role Moderator or Admin" in new Fixture {}

  it should "fail if account already closed" in new Fixture {}

  class Fixture {
    val accountService: AccountService = ???

    def givenThereIsAccount(account: Account) = ???
    def givenAccountCanBeSaved(expectedAccount: Account) = ???
    def givenEventCanBePosted() = ???
    def verifyEventPosted(event: Event) = ???
    def verifyAccountNotSaved() = ???
  }

}
