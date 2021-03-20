package exercises

import scala.annotation.tailrec
//Singly linked STREAM of elements.
//A head é sempre evaluated e sempre available
//A tail é available só por demanda

abstract class MyStream[+A]{
    def isEmpty: Boolean
    def head: A
    def tail: MyStream[A]

    //right associative
    def #::[B >: A](element: B): MyStream[B]
    def ++[B >: A](anotherStream: => MyStream[B]): MyStream[B]

    def foreach(f: A => Unit):Unit
    def map[B](f: A => B): MyStream[B]
    def flatMap[B](f: A => MyStream[B]): MyStream[B]
    def filter(predicate: A => Boolean): MyStream[A]

    //takes the first n elements out of this stream
    def take(n: Int): MyStream[A]
    final def takeAsList(n: Int): List[A] = take(n).toList()

    /*
    [1 2 3].toList
    [2 3].toList([1])
    [3].toList([1 2])
    [].toList([1 2 3)
    [1 2 3]
     */
    @tailrec
    final def toList[B >: A](acc: List[B] = Nil): List[B] =
        if (isEmpty) acc.reverse
        else tail.toList(head :: acc)

}

object EmptyStream extends MyStream[Nothing]{
    override def isEmpty: Boolean = true

    override def head: Nothing = throw new NoSuchElementException

    override def tail: MyStream[Nothing] = throw new NoSuchElementException

    override def #::[B >: Nothing](element: B): MyStream[B] = new ConsS(element,this)

    override def ++[B >: Nothing](anotherStream: => MyStream[B]): MyStream[B] = anotherStream

    override def foreach(f: Nothing => Unit): Unit = ()

    override def map[B](f: Nothing => B): MyStream[B] = this

    override def flatMap[B](f: Nothing => MyStream[B]): MyStream[B] = this

    override def filter(predicate: Nothing => Boolean): MyStream[Nothing] = this

    override def take(n: Int): MyStream[Nothing] = this

}

//Tail by name
 class ConsS[+A](hd:A, tl : => MyStream[A] ) extends MyStream[A]{
    override def isEmpty: Boolean = false

    override val head: A = this.hd
    override lazy val tail: MyStream[A] = this.tl       //call by need


    override def #::[B >: A](element: B): MyStream[B] = new ConsS(element,this)
    // Preserva lazy evaluation, pq a parte depois da vírgula(tail) só vai ser chamada quando necessário, ou seja,
    // quando alguém chamar
    override def ++[B >: A](anotherStream: => MyStream[B]): MyStream[B] = new ConsS(this.head,tail ++ anotherStream)

    override def foreach(f: A => Unit): Unit = {
        f(this.head)
        this.tail.foreach(f)
    }

    //Preserva lazy evaluation, pq a parte depois da vírgula(tail) só vai ser chamada quando necessário
    override def map[B](f: A => B): MyStream[B] =
        new ConsS(f(this.head),this.tail.map(f))

    //concatenate de application of f on every element of the stream
    override def flatMap[B](f: A => MyStream[B]): MyStream[B] =
        f(this.head) ++ this.tail.flatMap(f)

    override def filter(predicate: A => Boolean): MyStream[A] = {
        //println("entrou no filtro hein")
        if (predicate(this.head)) new ConsS(this.head,this.tail.filter(predicate))
        else this.tail.filter(predicate)   //preserves lazy evaluation
    }

    //Como o segundo de ConsS é by name e tail é lazy, a expressão preserva lazy evaluation
    override def take(n: Int): MyStream[A] = {
        //println(s"entrou no take com $n hein")
        if (n <= 0) EmptyStream
        else if (n == 1) new ConsS(this.head,EmptyStream)
        else new ConsS(this.head,this.tail.take(n-1))
    }

}

//É infinito mesmo, mas não dá ruim pq é lazy evaluated, ou seja, só vai evaluatedar quando for chamado
object MyStream{
    def from[A](start:A)(generator: A => A): MyStream[A] =
        new ConsS(start,MyStream.from(generator(start))(generator))
}


object StreamsPlayground extends App{
//    val naturals = MyStream.from(1)(_ + 1)
//    println(naturals.head)
//    println(naturals.tail.head)
//    println(naturals.tail.tail.head)
////    naturals.foreach(println)    //vai stackoverflowzar
//
//    val startFrom0 = 0 #:: naturals    //naturals.#::(0)
//    println(startFrom0.head)
//    println(startFrom0.tail.head)
//
//    //Dá stackoverflow, mexi em all buraco, mas n resolvi
//    //println(startFrom0.take(10000).foreach(println))
//    println(startFrom0.map(_ *2).take(100).toList())
//    println(startFrom0.flatMap(x => new ConsS(x,new ConsS(x+1,EmptyStream))).takeAsList(10))
//    println(startFrom0.filter(_ < 100).takeAsList(12))
    //Vira finite só quando chama take ou takeAsList


    // Exercises on streams
    // 1 - stream of Fibonacci numbers
    // 2 - stream of prime numbers with Eratosthenes' sieve
    /*
      [ 2 3 4 ... ]
      filter out all numbers divisible by 2
      [ 2 3 5 7 9 11 ...]
      filter  out all numbers divisible by 3
      [ 2 3 5 7 11 13 17 ... ]
      filter out all numbers divisible by 5
        ...
     */

    def fibonacci(first: BigInt, second:BigInt): MyStream[BigInt] = {
        //println("entrou no fibo hein")
        new ConsS(first,fibonacci(second,first+second))             //Lembrando que a tail é by name, ou seja, NÃO é evaluated logo
    }

    println(fibonacci(1,1).takeAsList(50))

    /*
    [2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]
    filtra por % 2 != 0
    [2,3,5,7,9,11,13,15]
    filtra por % 3 != 0
    [2,3,5,7,11,13]
    filtra por % 5 != 0
    [2,3,5,7,11,13]
    filtra por % 7 != 0
    [2,3,5,7,11,13]
    filtra por % 11 != 0
    [2,3,5,7,11,13]
    filtra por % 13 != 0
    [2,3,5,7,11,13]

     */


    def eratosthenes(numbers:MyStream[Int]):MyStream[Int] = {
        //println("entrou no erato hein")
        if (numbers.isEmpty) numbers
        else new ConsS(numbers.head,eratosthenes(numbers.tail.filter(_ % numbers.head != 0)))
    }

    val eratosTeste = MyStream.from(2)(_ + 1)
 //   println(eratosthenes(eratosTeste).takeAsList(6))        //6 primeiros primos duma stream

    val eratosTeste2 = MyStream.from(2)(_ + 1).take(100)
    eratosthenes(eratosTeste2).foreach(x => print(x+","))       //Os primos duma stream

}
