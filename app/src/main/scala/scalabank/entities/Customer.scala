package scalabank.entities

import scalabank.entities.Person

trait Customer extends Person:
  def fidelity: Fidelity

trait YoungCustomer extends Customer

trait BaseCustomer extends Customer

object Customer:
  def apply(name: String, surname: String, birthYear: Int): Customer = Person(name, surname, birthYear) match
    case person if person.age < 35 => YoungCustomerImpl(name, surname, birthYear)
    case _ => BaseCustomerImpl(name, surname, birthYear)

case class YoungCustomerImpl(_name: String,
                             _surname: String,
                             _birthYear: Int) extends YoungCustomer:
  override def fidelity: Fidelity = Fidelity(0)
  private val person = Person(_name, _surname, _birthYear)
  export person.*

case class BaseCustomerImpl(_name: String,
                             _surname: String,
                             _birthYear: Int) extends BaseCustomer:
  override def fidelity: Fidelity = Fidelity(0)
  private val person = Person(_name, _surname, _birthYear)
  export person.*