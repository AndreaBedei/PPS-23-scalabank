package scalabank.database.bank

import scalabank.database.{Database, DatabaseOperations}
import scalabank.currency.MoneyADT.toMoney
import scalabank.bank.BankAccountType

import java.sql.{Connection, ResultSet}

/**
 * Class representing the bank account type table in the database.
 *
 * @param connection The database connection to use.
 * @param database The database reference.
 */
class BankAccountTypeTable(override val connection: Connection, override val database: Database) extends DatabaseOperations[BankAccountType, String]:

  private val tableCreated =
    if !tableExists("bankAccountType", connection) then
      val query = "CREATE TABLE IF NOT EXISTS bankAccountType (" +
        "nameType VARCHAR(30) PRIMARY KEY, " +
        "feePerOperation VARCHAR(30), " +
        "interestSavingJar DOUBLE)"
      connection.createStatement().execute(query)
      true
    else false

  override def initialize(): Unit =
    if tableCreated then
      populateDB()

  def insert(bankAccountType: BankAccountType): Unit =
    val query = "INSERT INTO bankAccountType (nameType, feePerOperation, interestSavingJar) VALUES (?, ?, ?)"
    val stmt = connection.prepareStatement(query)
    stmt.setString(1, bankAccountType.nameType)
    stmt.setString(2, bankAccountType.feePerOperation.toString())
    stmt.setDouble(3, bankAccountType.interestSavingJar)
    stmt.executeUpdate

  private def createBankAccountType(resultSet: ResultSet): BankAccountType =
    BankAccountType(
      resultSet.getString("nameType"),
      resultSet.getBigDecimal("feePerOperation").toMoney,
      resultSet.getDouble("interestSavingJar")
    )

  def findById(nameType: String): Option[BankAccountType] =
    val stmt = connection.prepareStatement("SELECT * FROM bankAccountType WHERE nameType = ?")
    stmt.setString(1, nameType)
    val result = stmt.executeQuery
    if result.next then Some(createBankAccountType(result)) else None

  def findAll(): Seq[BankAccountType] =
    val stmt = connection.createStatement
    val resultSet = stmt.executeQuery("SELECT * FROM bankAccountType")
    new Iterator[BankAccountType]:
      def hasNext: Boolean = resultSet.next
      def next(): BankAccountType = createBankAccountType(resultSet)
    .toSeq

  def update(bankAccountType: BankAccountType): Unit =
    val query = "UPDATE bankAccountType SET feePerOperation = ?, interestSavingJar = ? WHERE nameType = ?"
    val stmt = connection.prepareStatement(query)
    stmt.setString(1, bankAccountType.feePerOperation.toString())
    stmt.setDouble(2, bankAccountType.interestSavingJar)
    stmt.setString(3, bankAccountType.nameType)
    stmt.executeUpdate

  def delete(nameType: String): Unit =
    val query = "DELETE FROM bankAccountType WHERE nameType = ?"
    val stmt = connection.prepareStatement(query)
    stmt.setString(1, nameType)
    stmt.executeUpdate

  private def populateDB(): Unit =
    List(
      BankAccountType("default", 1.00.toMoney, 0.04),
      BankAccountType("young", 0.50.toMoney, 0.03),
      BankAccountType("old", 0.75.toMoney, 0.05)
    ).foreach(insert)