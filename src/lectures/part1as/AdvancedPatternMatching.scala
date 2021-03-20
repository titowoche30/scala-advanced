package lectures.part1as

import com.sun.org.apache.xpath.internal.functions.FuncFalse
import sun.invoke.empty.Empty

object AdvancedPatternMatching extends App{
    val numbers = List(1)
    val description = numbers match {
        case head :: Nil => println(s"The only element is $head")     // ::[A] é uma subclasse de List(é o node)
                                                                      // com head e tail e o Nil é o vazio.
                                                                      // Então isso aqui diz que o objeto é
                                                                      // ::(head,Nil). Ou seja, é o único elemento da lista
        case _ => 
    }
    
    //Posso fazer matching em
    //Constants, wildcard, case classes, tuples
    
    //Como fazer minha própria classe matchable
    //supondo que não posso fazer dela uma case class
    
    class Person(val name: String, val age: Int)
    
    object Person{
        // arg com tipo do troço que vai ficar antes do match
        // e return uma tupla com types que quero decompose,
        // ou seja, os parâmetros do case tururu(par1,par2)
        def unapply(arg: Person): Option[(String, Int)] = Some((arg.name,arg.age))

        def unapply(age: Int): Option[String] = {
            Some(if (age <21) "Minor" else "Major")
        }
    }

    val bob = new Person("bob",20)
    val greeting = bob match {
        case Person(n,a) => s"$n e $a years old"            //Só o nome do object que é importante aqui
    }
    println(greeting)

    val legalStatus = bob.age match {
        case Person(status) => s"Você eh $status"
        case _ =>
    }

    // Ele vê que é um int match, vai até o Object Person, checa se tem um unnaply que recebe int
    // e o retorno do Option é o arg do case

    println(legalStatus)


    //Exercício

    object even{
        def unapply(arg: Int): Boolean = {
            arg % 2 == 0
        }
    }

    object singleDigit{
        def unapply(arg: Int): Boolean =
            arg > -10 && arg < 10
    }

    val n = -3
    val mater = n match {
        case even() => "Even hein"
        case singleDigit() => "Single digit hein"
        case _ => "nadica"
    }
    println(mater)

    // Infix patterns
    // ex: head :: Nil

    case class Or[A,B](a: A,b: B)

    val eiter = Or(1,"palavras, apenas, palavras")
    val humanDescription = eiter match {
        //case Or(num,str) => s"num $num e str $str"
        case num Or str => s"num ($num) e str ($str)"
    }
    println(humanDescription)

    // decomposing sequences
    // vararg pattern = _* que fiz que têm n parâmetros
    val varArg = numbers match {
        case List(1,_*) => "Starting with 1"
    }
    println(varArg)

    //Como fazer _* pra minha classe
    abstract class MyList[+A] {
        def head: A = ???
        def tail: MyList[A] = ???
    }

    case object Empty extends MyList
    case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

    object MyList{                      //o nome na vdd pode ser qualquer, normalmente é companion por prática msm

        // Esse unapply tranforma um MyList[A] em um Option[Seq[A]]
        // preservando os mesmos elementos na mesma ordem
        def unapplySeq[A](list: MyList[A]): Option[Seq[A]] =
            if (list == Empty) Some(Seq.empty)
            else unapplySeq(list.tail).map(list.head +: _)
    }

    val myList:MyList[Int] = Cons(1,Cons(2,Cons(3,Cons(4,Empty))))
    val decomposed = myList match {
        case MyList(1,2,_*) => s"Começa com 1,2"
        case _ => s"banido"
    }
    println(decomposed)

    // Custom return types for unapply
    // O return do unapply não precisa ser necessariamente Option
    // Mas precisa ser uma ED com os métodos isEmpty: Boolean, get: something

    abstract class Wrapper[T]{
        def isEmpty: Boolean
        def get: T
    }

    object PersonWrapper{
        def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
            override def isEmpty: Boolean = false
            override def get: String = person.name
            }
        }

    println(bob match {
        case PersonWrapper(name) => s"nome é $name"
        case _ => "banido"
    })

    }
