package lectures.part4implicits

import scala.annotation.tailrec

object OrganizingImplicits extends App{
    //println(List(1,4,5,3,2).sorted)
    //Tem um implicit mandado aqui no sorted, ele fica no
    //scala.Predef, que é importado automaticamente

    implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)      //Tem precdência em relação ao implicit do Predef
    println(List(1,4,5,3,2).sorted)

    /*
        Implicits:
            - val/var
            - object
            - accessor methods = defs with no parentheses
     */

    // Exercise
    // Sort Person in alphabetical order
    case class Person(name: String, age:Int)

    val persons = List(
        Person("Tito",22),
        Person("Teto",20),
        Person("Noah",0),
        Person("Julie",5),
        Person("Silvana",60)
    )



    //compareTo = The result is a negative integer if this String object lexicographically precedes the argument string.
//    implicit val alphaOrder: Ordering[Person] = Ordering.fromLessThan((p1,p2) => p1.name.compareTo(p2.name) < 0)
 //   println(persons.sorted)

    /*
        Implicit Scope(by order of higher priority)
            - normal scope = local scope
            - imported scope
            - companion objects of all types involved in the method signature
                ** Ordem que o compilador vai procurar por implicit Ordering no sorted abaixo **
                - List
                - Ordering
                - all the types involved = A or any supertype
     */

    // Ex do sorted: def sorted[B >: A](implicit ord : scala.math.Ordering[B]): List[B] = { ... }

    object Xaga{
        implicit val alpha2Order: Ordering[Person] = Ordering.fromLessThan((p1,p2) => p1.name.compareTo(p2.name) < 0)
    }

    //implicit val x = Xaga.alpha2Order
    //ou
    //import Xaga._
    //println(persons.sorted)

    // Exercises
    // totalPrice
    // by unit count
    // by unit price

    case class Purchase(nUnits: Int, unitPrice: Double) {
        def getTotal = nUnits * unitPrice
    }

    //se fosse object Purchase, esse sort seria o padrão das instâncias da classe
    object totalPrice {
        implicit val totalOrder: Ordering[Purchase] = Ordering.fromLessThan((p1, p2) => p1.getTotal < p2.getTotal)
    }

    object unitCount {
        implicit val countOrder: Ordering[Purchase] = Ordering.fromLessThan((p1,p2) => p1.nUnits < p2.nUnits)
    }

    object unitPrice {
        implicit val priceOrder: Ordering[Purchase] = Ordering.fromLessThan((p1,p2) => p1.unitPrice < p2.unitPrice)
    }

    val compras = List(
        Purchase(3,8),
        Purchase(10,2),
        Purchase(5,30),
        Purchase(18,20),
        Purchase(100,4)
    )

    println(compras.sorted(totalPrice.totalOrder))
    //import unitCount._
    import unitPrice._
    println(compras.sorted)

}
