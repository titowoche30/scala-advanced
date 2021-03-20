package lectures.part2afp

object PartialFunctions extends App{
    val aFunction = (x:Int) => x+1          //Function1[Int,Int]  == Int => Int

    //Partial Function de Int => Int pq aceita só uma parte do domínio Int
    //{1,2,60} => Int
    //Dizemos que ela é partially defined no domínio {1,2,60}
    val nicerTotalFunction = (x:Int) => x match {
        case 1 => 10
        case 2 => 20
        case 60 => 600
    }
    println(nicerTotalFunction(60))

    //Mesma coisa do de cima, mas o de cima não é uma partial function, é uma total mesmo
    val aPartialFunction: PartialFunction[Int,Int] = {
        case 1 => 10
        case 2 => 20
        case 60 => 600
    }       //O code block é chamado de partial function value

    println(aPartialFunction(2))

    // Utilities
    println(aPartialFunction.isDefinedAt(67))       //true ou false

    //lift
    val lifted = aPartialFunction.lift              // trasnforma em Int => Option[Int]
    println(lifted(1))                    // Some(10)
    println(lifted(344))                  // None

    //orElse takes another partial function as argument
    val chained = aPartialFunction.orElse[Int,Int] {
        case 45 => 450
    }
    println(chained(45))

    val pfCha: PartialFunction[Int,Int] = {
        case 23 => 230
    }

    println((aPartialFunction orElse pfCha)(23))

    // PF extend normal functions (herdam de)

    //Essa aqui é uma total function
    val aFunction1: Int => Int = {
        case 1 => 99
    }

    //Higher Order Functions aceitam PartialFunctions tbm

    val aMappedList = List(1,2,3) map {
        case 1 => 16
        case 2 => 32
        case 3 => 48
     //   case 5 => 34      //Vai dar match error
    }   //Meti uma PF no map

    println(aMappedList)

    /*
    * NOTES
        * PF só têm um parâmetro. Pq ela usa pattern matching, e esse recurso é só de 1 por 1
     */

    //EXERCÍCIOS

    //1 - Construir uma PF instanciando a própria trait
    val myPF:PartialFunction[Int,Int] = new PartialFunction[Int,Int] {
        override def isDefinedAt(x: Int): Boolean = x match {
            case 3 | 4 => true
            case _ => false
        }
        override def apply(v1: Int): Int = v1 match {
            case 3 => 21
            case 4 => 28
            case _ => 0
            }


    }

    println(myPF(3))
    println(myPF(4))
    println(myPF(23))
    println(myPF.isDefinedAt(23))


    //2 - Construir um chatbotzin

    val roboEd:PartialFunction[String,String] = {
        case n:String if n.contains("triste") => "Sei comé :("
        case "oi" => "Hello, I'm Robo Ed"
        case "Tudo bem com vc?" => "I'm great and you?"
        case "I'm fine" => "That's really nice"
        case n:String if n.contains("language") => "Scala  is top"
        case _ => "I have no answer to that =/"
    }

    //scala.io.Source.stdin.getLines().foreach(line => println("> " + roboEd(line)))
    scala.io.Source.stdin.getLines().map(roboEd).foreach(println)



}
