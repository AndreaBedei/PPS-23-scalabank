package scalabank.database.interest

import scalabank.database.{Database, DatabaseOperations}
import scalabank.loan.InterestRate

import java.sql.{Connection, ResultSet}

/**
 * Represent the operations for the interest rate table.
 */
trait InterestTable extends DatabaseOperations[(String, InterestRate), String]

/**
 * Object for creating instances of InterestTable.
 */
object InterestTable:

  /**
   * Creates a new instance of InterestTable.
   *
   * @param connection The database connection to use.
   * @param database The database reference.
   * @return A new instance of InterestTable.
   */
  def apply(connection: Connection, database: Database): InterestTable = InterestTableImpl(connection, database)

  private class InterestTableImpl(override val connection: Connection, override val database: Database) extends InterestTable:

    private val tableCreated =
      if !tableExists("interestRate", connection) then
        val query = "CREATE TABLE IF NOT EXISTS interestRate (id VARCHAR(30) PRIMARY KEY, rate DOUBLE)"
        connection.createStatement().execute(query)
        true
      else false

    override def initialize(): Unit =
      if tableCreated then
        populateDB()

    override def insert(rowToInsert: (String, InterestRate)): Unit =
      val query = "INSERT INTO interestRate (id, rate) VALUES (?, ?)"
      val stmt = connection.prepareStatement(query)
      stmt.setString(1, rowToInsert._1)
      stmt.setDouble(2, rowToInsert._2.interestValue)
      stmt.executeUpdate

    private def createInterestRate(resultSet: ResultSet): (String, InterestRate) =
      (resultSet.getString("id"), InterestRate(resultSet.getDouble("rate")))

    override def findById(id: String): Option[(String, InterestRate)] =
      val stmt = connection.prepareStatement("SELECT * FROM interestRate WHERE id = ?")
      stmt.setString(1, id)
      val result = stmt.executeQuery
      if result.next then Some(createInterestRate(result)) else None

    override def findAll(): Seq[(String, InterestRate)] =
      val stmt = connection.createStatement
      val resultSet = stmt.executeQuery("SELECT * FROM interestRate")
      new Iterator[(String, InterestRate)]:
        def hasNext: Boolean = resultSet.next
        def next(): (String, InterestRate) = createInterestRate(resultSet)
      .toSeq

    override def update(rowToUpdate: (String, InterestRate)): Unit =
      val query = "UPDATE interestRate SET rate = ? WHERE id = ?"
      val stmt = connection.prepareStatement(query)
      stmt.setDouble(1, rowToUpdate._2.interestValue)
      stmt.setString(2, rowToUpdate._1)
      stmt.executeUpdate

    override def delete(id: String): Unit =
      val query = "DELETE FROM interestRate WHERE id = ?"
      val stmt = connection.prepareStatement(query)
      stmt.setString(1, id)
      stmt.executeUpdate

    private def populateDB(): Unit =
      List(
        ("default", InterestRate(0.04)),
        ("young", InterestRate(0.03)),
        ("old", InterestRate(0.05))
      ).foreach(insert)