package com.flutter.akka.actors.classic

import akka.actor.PoisonPill
import akka.testkit.TestProbe
import com.flutter.akka.actors.classic.Account.{AccountBalance, AccountCredited, Deposit, GetBalance}
import scala.concurrent.duration.DurationInt


class AccountSpec extends BaseActorSpec("AccountSpec") {
  val accNo:String = "acc-1"
  it should "handle account commands" in {


    val actor = system.actorOf(Account.props(accountNo = accNo))
    val probe = TestProbe()
    actor.tell(Deposit(accNo, 10), probe.ref)
    var res1 = probe.expectMsgType[AccountCredited](3.seconds)
    res1.amount.shouldBe(10)

    actor.tell(GetBalance(accNo), probe.ref)
    var res2 = probe.expectMsgType[AccountBalance](3.seconds)
    res2.totalBalance.shouldBe(10)

    probe.watch(actor)

    actor ! PoisonPill
    probe.expectTerminated(actor)

  }

  it should "recovery works" in {

    val probe = TestProbe()
    val actor = system.actorOf(Account.props(accNo))
    actor.tell(GetBalance(accNo), probe.ref)
    val res3 = probe.expectMsgType[AccountBalance](3.seconds)
    res3.totalBalance.shouldBe(10)

  }
}
