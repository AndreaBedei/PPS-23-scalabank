package scalabank.database.person

import scalabank.database.{AbstractCache, Database, DatabaseOperations, PopulateEntityTable}
import scalabank.entities.Person

import java.sql.{Connection, ResultSet}
import scala.collection.mutable.Map as MutableMap

/**
 * Represent the operations for the person table.
 */
trait PersonTable extends AbstractCache[Person, String] with DatabaseOperations[Person, String]

/**
 * Object for creating instances of PersonTable.
 */
object PersonTable:

  /**
   * Creates a new instance of PersonTable.
   *
   * @param connection The database connection to use.
   * @param database The database reference.
   * @return A new instance of PersonTable.
   */
  def apply(connection: Connection, database: Database): PersonTable = PersonTableImpl(connection, database)

  private class PersonTableImpl(override val connection: Connection, override val database: Database) extends PersonTable:

    private val fetchedPeople = cache
  
    private val tableCreated =
      if !tableExists("person", connection) then
        val query = "CREATE TABLE IF NOT EXISTS person (cf VARCHAR(16) PRIMARY KEY, name VARCHAR(255), surname VARCHAR(255), birthYear INT)"
        connection.createStatement().execute(query)
        true
      else false
  
    override def initialize(): Unit =
      if tableCreated then 
        populateDB(1)
  
  
    override def insert(entity: Person): Unit =
      val query = "INSERT INTO person (cf, name, surname, birthYear) VALUES (?, ?, ?, ?)"
      val stmt = connection.prepareStatement(query)
      stmt.setString(1, entity.cf)
      stmt.setString(2, entity.name)
      stmt.setString(3, entity.surname)
      stmt.setInt(4, entity.birthYear)
      stmt.executeUpdate
  
    private def createPerson(resultSet: ResultSet) =
      val cf = resultSet.getString("cf")
      fetchedPeople.get(cf) match
        case Some(p) => p
        case None =>
          val person = Person(cf, resultSet.getString("name"), resultSet.getString("surname"), resultSet.getInt("birthYear"))
          fetchedPeople.put(cf, person)
          person
  
    override def findById(cf: String): Option[Person] =
      val stmt = connection.prepareStatement("SELECT * FROM person WHERE cf = ?")
      stmt.setString(1, cf)
      val result = stmt.executeQuery
      for
        _ <- Option(result) if result.next
      yield createPerson(result)
  
    override def findAll(): Seq[Person] =
      val stmt = connection.createStatement
      val resultSet = stmt.executeQuery("SELECT * FROM person")
      new Iterator[Person]:
        def hasNext: Boolean = resultSet.next
        def next(): Person = createPerson(resultSet)
      .toSeq
  
    override def update(entity: Person): Unit =
      val query = "UPDATE person SET name = ?, surname = ?, birthYear = ? WHERE cf = ?"
      val stmt = connection.prepareStatement(query)
      stmt.setString(1, entity.name)
      stmt.setString(2, entity.surname)
      stmt.setInt(3, entity.birthYear)
      stmt.setString(4, entity.cf)
      stmt.executeUpdate
      fetchedPeople.remove(entity.cf)
  
    override def delete(cf: String): Unit =
      val query = "DELETE FROM person WHERE cf = ?"
      val stmt = connection.prepareStatement(query)
      stmt.setString(1, cf)
      stmt.executeUpdate
      fetchedPeople.remove(cf)
  
    private def populateDB(numberOfEntries: Int): Unit =
        PopulateEntityTable.createInstancesDB(numberOfEntries, (cf, name, surname, birthYear) => Person(cf, name, surname, birthYear)).foreach(_ => insert(_))



