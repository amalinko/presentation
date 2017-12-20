package com.test

import org.scalatest.{FlatSpec, Matchers}

class AccountSpec extends FlatSpec with Matchers {

  "encourage" should "add points to user balance" in {
    val account: Account = ???
    val expectedAccount = account.copy(balance = 100)

    val (updated, _) = account.encourage(100)

    updated shouldEqual expectedAccount
  }

  it should "not add negative points" in {
    val account: Account = ???

    intercept[Exception] {
      account.encourage(-1)
    }
  }

  it should "limit balance with MaxBalance value" in {
    val account: Account = ???
    val expectedAccount = account.copy(balance = Account.MaxBalance)

    val actual = account.encourage(Account.MaxBalance + 1)

    actual shouldEqual expectedAccount
  }

  it should "return event if balance reached max value" in {
    val account: Account = ???

    val (_, Some(event)) = account.encourage(Account.MaxBalance)

    event shouldEqual AccountHasMaxBalance(account.id)
  }

  it should "throw error if account is not confirmed" in {
    val notConfirmedAccount: Account = ???
    intercept[Exception] {
      notConfirmedAccount.encourage(100)
    }
  }

  "fine" should "subtract point from account's balance" in {}

  it should "fail on negative points" in {}

  it should "close account if after subtraction balance is negative" in {}

  it should "send email if account is closed" in {}

  it should "not close account if account has role Moderator or Admin" in {}

  it should "fail if account already closed" in {}

}
