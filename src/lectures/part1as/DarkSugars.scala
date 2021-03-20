package lectures.part1as

import scala.util.Try

object DarkSugars extends App{
    // 1. Methods com 1 parâmetro

    def singleArg(arg:Int) = s"$arg patinhos"

    val description = singleArg{
        //código aqui, expression aqui
        42
    }
    println(description)

    //Dá pra fazer isso nos apply dos objects tbm
    val aTryInstace = Try {                 //É usado na prática pq parece o try do Java
        throw new RuntimeException
    }
    println(aTryInstace)

    val mapda = List(1,2,3) map { x =>
        x * 2
    }
    mapda.foreach(print)

    //2. Single abstract method
    //Instances of traits with single methods can be reduced to lambdas

    trait Action{
        def act(x: Int): Int
    }

    val anInstance:Action = new Action {
        override def act(x: Int): Int = x + 324
    }

    val anInstance1:Action = (x: Int) => x + 324        //Compilador reconhece que é uma Function1 e faz um single abstract type conversion

    //ex: Runnables
    //Runnables are instances of traits(java interfaces) that can be passed on to Threads
    val aThread = new Thread(new Runnable {
        override def run(): Unit = println("Hello Scala")
    })

    //Funciona pq Runnable só tem 1 método tbm

    val aSweeterThread = new Thread(() => println("Hello Scala"))
    //ou
    val laga:Runnable = () => println("Hello Scala")
    val aSweeterThread2 = new Thread(laga)

    //Isso tbm funciona pra classes que têm métodos implementados mas SÓ 1 NÃO implementado

    abstract class AbsType{
        def implemented():Int = 23
        def f(a:Int):Unit
    }

    //Implementação da f
    val anAbsInstance:AbsType = (x:Int) => println("xuga")

    //3. métodos :: e #::

    val prependList = 2 :: List(3,4)
    //NÃO É 2.::(List(3,4))
    //SIM É List(3,4).::(2)

    //scala spec: The associativity of a method is determined by the operator's last character
    // ou seje, o last char decide a associatividade
    // ou duplo seje, o segundo : determina que é right associative
    // right associative = começa da direita, tem um parêntese na direita
    1 :: 2 :: 3 :: List(4,5)
    List(4,5).::(3).::(2).::(1)    // mesma coisa

    class MyStream[T] {
        def -->:(value:T):MyStream[T] = this
    }


    val mystream = 1 -->: 2 -->: 3-->: new MyStream[Int]

    //4. Multi-word method naming

    class TeenGirl(name: String){
        def `and then said`(gossip:String) = println(s"$name said $gossip")
    }

    val lilly = new TeenGirl("Lilly")
    lilly `and then said` "Scala is top"

    //5. Infix types
    class Composite[A,B]

    //val composite: Int Composite String = ???

    class -->[A,B]
    val vaipra: Int --> Double = ???

    //6. update(), special like apply

    val anArray = Array(1,2,3)
    anArray(2) = 32             //Compilador escreve como anArray.update(2,32)

    //7. setters for mutable containers
    class Mutable{
        private var internalMember: Int = 0     //encapsulation
        def member = internalMember             //getter
        def member_=(value:Int):Unit =          //setter
            internalMember = value
    }

    //SÓ acontece se definir um getter e um setter chamados member e member_=

    val mutableContainer = new Mutable
    mutableContainer.member = 42                    //reescrito como mutableContainer.member_=(42)



}
