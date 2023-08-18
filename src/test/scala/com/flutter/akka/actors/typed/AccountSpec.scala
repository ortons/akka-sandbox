package com.flutter.akka.actors.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.flutter.akka.actors.classic.Account.{AccountCredited, Deposit, props}
import com.typesafe.config.ConfigFactory
import org.scalatest.{GivenWhenThen, Matchers}
import org.scalatest.flatspec.AnyFlatSpecLike
import com.flutter.akka.actors.typed.Account.{AccountBalance, AccountCredited, Deposit, GetBalance, StopCommand}

import scala.concurrent.duration.DurationInt



class AccountSpec extends ScalaTestWithActorTestKit(ConfigFactory.defaultApplication()) with AnyFlatSpecLike with Matchers with GivenWhenThen {
  val accountNo = "37288374"
  it should "handle Account Commands" in {
    val probe = createTestProbe(AccountEvent)
    val actor = spawn(Account.behavior("acc-1"))
    actor ! Deposit(accountNo,30.0)
    val res1 = probe.expectMessageType[AccountCredited](3.seconds)
    res1.amount.shouldBe(30.0)

    actor ! StopCommand(accountNo)
  }

  it should "handle Account Re-hydration" in {
    val probe = createTestProbe(AccountEvent)
    val actor = spawn(Account.behavior(accountNo))
    actor ! GetBalance(accountNo, probe.ref)
    val res1 = probe.expectMessageType[AccountCredited](3.seconds)
    res1.totalBalance.shouldBe(30.0)

  }
}
