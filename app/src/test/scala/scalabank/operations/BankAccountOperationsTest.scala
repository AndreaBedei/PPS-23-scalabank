package scalabank.operations

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import scalabank.bankAccount.{BankAccount, Deposit, MoneyTransfer, StateBankAccount, Withdraw}
import scalabank.bank.BankAccountType
import scalabank.currency.{Currency, FeeManager, MoneyADT}
import scalabank.currency.MoneyADT.toMoney
import scalabank.entities.Customer

@RunWith(classOf[JUnitRunner])
class BankAccountOperationsTest extends AnyFlatSpec with Matchers:
  val customer: Customer = Customer("CUS12345L67T890M", "John", "Doe", 1980)
  val bankAccountType: BankAccountType = BankAccountType("Checking", 0.01.toMoney, 0.toMoney, 0.01.toMoney, 0.5)
  val currency: Currency = Currency("EUR", "€")
  val differentCurrency: Currency = Currency("USD", "$")
  val initialBalance: MoneyADT.Money = 1000.toMoney

  "BankAccount" should "allow deposits and update the balance correctly" in:
    val account = BankAccount(1, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    account.deposit(500.toMoney)
    account.balance shouldEqual 1500.toMoney
    account.movements.size shouldBe 1
    account.movements.head shouldBe a[Deposit]
    account.movements.head.value shouldEqual 500.toMoney

  it should "allow withdrawals and update the balance correctly with fee applied" in:
    val account = BankAccount(2, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    val amount = 100.toMoney
    account.withdraw(amount)
    val expectedBalance = 1000.toMoney - FeeManager.calculateAmountWithFee(amount, bankAccountType.feeWithdraw)
    account.balance shouldEqual expectedBalance
    account.movements.size shouldBe 1
    account.movements.head shouldBe a[Withdraw]
    account.movements.head.value shouldEqual 100.toMoney

  it should "return false when trying to withdraw more than the balance including the fee" in:
    val account = BankAccount(3, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    val largeWithdrawAmount = 2000.toMoney
    val result = account.withdraw(largeWithdrawAmount)
    result shouldEqual false
    account.balance shouldEqual initialBalance
    account.movements shouldBe empty

  import bankAccountType.*
  it should "record multiple movements correctly" in:
    val account = BankAccount(4, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    account.deposit(200.toMoney)
    val amount = 100.toMoney
    account.withdraw(amount)
    val amount2 = 50.toMoney
    account.withdraw(amount2)
    val expectedBalance = 1000.toMoney + 200.toMoney - FeeManager.calculateAmountWithFee(amount, feeWithdraw) - FeeManager.calculateAmountWithFee(amount2, feeWithdraw)
    account.balance shouldEqual expectedBalance
    account.movements.size shouldBe 3
    account.movements.head shouldBe a[Deposit]
    account.movements.head.value shouldEqual 200.toMoney
    account.movements(1) shouldBe a[Withdraw]
    account.movements(1).value shouldEqual 100.toMoney
    account.movements(2) shouldBe a[Withdraw]
    account.movements(2).value shouldEqual 50.toMoney

  "A MoneyTransfer" should "update the balances of both accounts correctly" in:
    val sender = BankAccount(5, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    val receiver = BankAccount(6, Customer("CUS67890A12B345C", "Jane", "Smith", 1990), 500.toMoney, currency, StateBankAccount.Active, bankAccountType)
    val amount = 200.toMoney
    val result = sender.makeMoneyTransfer(receiver, amount)
    result shouldBe true
    sender.balance shouldEqual (initialBalance - FeeManager.calculateAmountWithFee(amount, feeMoneyTransfert))
    receiver.balance shouldEqual (500.toMoney + amount)
    sender.movements.size shouldBe 1
    sender.movements.head shouldBe a[MoneyTransfer]
    sender.movements.head.value shouldEqual amount
    receiver.movements.size shouldBe 1

  "A MoneyTransfer between bank accounts with different currencies" should "update the balances of both accounts correctly" in:
    val sender = BankAccount(7, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    val receiver = BankAccount(8, Customer("CUS67890A12B345C", "Jane", "Smith", 1990), 500.toMoney, differentCurrency, StateBankAccount.Active, bankAccountType)
    val amount = 200.toMoney
    val result = sender.makeMoneyTransfer(receiver, amount)
    result shouldBe true
    sender.balance shouldEqual (initialBalance - FeeManager.calculateAmountWithFee(amount, bankAccountType.feeMoneyTransfert))
    receiver.balance shouldBe (500.toMoney + amount)
    sender.movements.size shouldBe 1
    sender.movements.head shouldBe a[MoneyTransfer]
    sender.movements.head.value shouldEqual amount
    receiver.movements.size shouldBe 1

  "A MoneyTransfer" should "return false when trying to transfer more than the sender's balance including the fee" in:
    val sender = BankAccount(9, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    val receiver = BankAccount(10, Customer("CUS67890A12B345C", "Jane", "Smith", 1990), 500.toMoney, currency, StateBankAccount.Active, bankAccountType)
    val largeTransferAmount = 2000.toMoney
    val result = sender.makeMoneyTransfer(receiver, largeTransferAmount)
    result shouldBe false
    sender.balance shouldEqual initialBalance
    receiver.balance shouldEqual 500.toMoney
    sender.movements shouldBe empty
    receiver.movements shouldBe empty

  "BankAccount" should "filter only Withdraw movements correctly" in :
    val account = BankAccount(5, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    account.deposit(500.toMoney)
    account.withdraw(100.toMoney)
    account.withdraw(50.toMoney)
    val withdraws = account.filterMovements[Withdraw]
    withdraws.forall(_.isInstanceOf[Withdraw]) shouldBe true
    withdraws.map(_.value) should contain theSameElementsAs Seq(100.toMoney, 50.toMoney)

  it should "filter only Deposit movements correctly" in :
    val account = BankAccount(6, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    account.deposit(500.toMoney)
    account.deposit(300.toMoney)
    account.withdraw(100.toMoney)
    val deposits = account.filterMovements[Deposit]
    deposits.size shouldBe 2
    deposits.forall(_.isInstanceOf[Deposit]) shouldBe true
    deposits.map(_.value) should contain theSameElementsAs Seq(500.toMoney, 300.toMoney)

  it should "return an empty sequence when no movements of the specified type exist" in :
    val account = BankAccount(7, customer, initialBalance, currency, StateBankAccount.Active, bankAccountType)
    account.deposit(500.toMoney)
    val withdraws = account.filterMovements[Withdraw]
    withdraws shouldBe empty
