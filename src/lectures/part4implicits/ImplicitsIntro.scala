package lectures.part4implicits

object ImplicitsIntro extends App {
    val pair = "Daniel" -> "555"
    val intPair = 1 -> 2            // -> é um implicit

    case class Person(name:String){
        def greet = s"Hi, my name is $name"
    }

    implicit def fromStringToPerson(str:String): Person = Person(str)

    println("Peter".greet)

    /*
     Compilador olha pra todos os implicits classes,objects,values,methods
     que podem ajudar na transformação, ou seja, procura por qualquer coisa
     que possa transformar a string em algo que tenha um método greet
     Ele vai fazer println(fromStringToPerson("Peter").greet)
     Se tiver mais um implicit que pode fazer a transformação o compilador
     não escolhe nenhum e não compila o código
    */

    //Implicit parameters
    def increment(x: Int)(implicit amount:Int) = x + amount

    implicit val defaultAmount = 10
    println(increment(2))                  //A implicit val vai ser passada pelo compilador como a segunda lista de parâmetros
    def incrementor(x:Int)(implicit amount2:Double) = {
        x  * amount2
    }

    implicit val multiplier= 3.14
    val fun = incrementor(3)
    println(fun)
}
