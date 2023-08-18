package com.flutter.akka.actors.classic

import akka.actor.{ActorLogging, PoisonPill, Props}
import akka.persistence.PersistentActor
import com.flutter.akka.actors.classic.Account.{AccountBalance, AccountCredited, AccountEvent, AccountState, Deposit, GetBalance}


object Account {
  sealed trait AccountCommand

  case class AccountBalance(accountNo: String, timestamp: Long, totalBalance: Double)

  case class Deposit(accountNo: String, amount: Double) extends AccountCommand

  sealed trait AccountEvent

  case class AccountCredited(accountNo: String, timestamp: Long, amount: Double) extends AccountEvent

  case class AccountState(accountNo: String, balance: Double = 0.0, transactions: List[AccountEvent] = List.empty) {

    def apply: AccountEvent => AccountState = {
      case credit: AccountCredited => copy(balance = balance + credit.amount, transactions = credit :: transactions)
    }
  }

  def props(accountNo: String): Props = {
    Props(new Account(accountNo))
  }

  case class GetBalance(accountNo: String) extends AccountCommand


}

class Account(accountNo: String) extends PersistentActor with ActorLogging {

  private def applyEvent: AccountEvent => Unit = {
    ev => state = state.apply(ev)
  }

  private var state = AccountState(accountNo)


  override def receiveRecover: Receive = {
    case ev: AccountEvent => applyEvent(ev)
  }

  override def receiveCommand: Receive = {
    case Deposit(accountNo, amount) =>
      var ev = AccountCredited(accountNo = accountNo, timestamp = System.currentTimeMillis(), amount = amount)
      persist(ev)(applyEvent)
      sender() ! ev

    case GetBalance(_) => {
      sender() ! AccountBalance(accountNo, System.currentTimeMillis(), state.balance)
    }

    case PoisonPill =>
      log.info("I'm dying")
  }

  override def persistenceId: String = s"classic-acc-$accountNo"
}
