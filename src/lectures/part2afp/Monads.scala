package lectures.part2afp

//Our own Try monad
trait Attempt[+A]{
    def flatMap[B](f: A => Attempt[B]): Attempt[B]
}

object Attempt{
    def apply[A](a: => A): Attempt[A] = {
        try{
            Sucess(a)
        } catch {
            case e:Throwable => Failure(e)
        }
    }
}

case class Sucess[+A](value: A) extends Attempt[A] {
    override def flatMap[B](f: A => Attempt[B]): Attempt[B] = {
        try{
            f(value)
        } catch {
            case e: Throwable => Failure(e)
        }
    }
}

case class Failure(e: Throwable) extends Attempt[Nothing] {
    override def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] = this
}

object Monads extends App{
    //Monads são um design pattern
    //Provides a standard interface for composing and sequencing operations on some contained values
    //Option é uma monad
    // Monads capture policies for funciton application and ocmposition, in map and flatMap
    // for-comprehensions provide a sequential syntax, on top of map and flatMap

    /*
    left-identity

    unit.flatMap(f) = f(x)
    Attempt(x).flatMap(f) = f(x) //Sucess case
    Sucess(x).flatMap(f) = f(x) //proved

    right-identity

    Attempt.flatMap(unit) = Attempt
    Sucess(x).flatMap(x => Attempt(x)) = Attempt(x) = Sucess(x)
    Failure(e).flatMap(...) = Failure(e)

    associativity

    Attempt.flatMap(f).flatMap(g) = Attempt.flatMap(x => f(x).flatMap(g))
    Failure(e).flatMap(f).flatMap(g) = Failure(e)
    Failure(e).flatMap(x => f(x).flatMap(g)) = Failure(e)

    Sucess(v).flatMap(f).flatMap(g) = f(v).flatMap(g) OR Failure(e)
    Sucess(v).flatMap(x => f(x).flatMap(g)) = f(v).flatMap(g)) OR Failure(e)


     */

    val attempt = Attempt{
        throw new RuntimeException("Dagabadi")
    }
    println(attempt)

    //Exercícios
    //1 - Lazy Monad

    class Lazy[+A](value: => A){
        //call by need
        private lazy val internalValue = value
        def use: A = internalValue
        //a f by name
        def flatMap[B](f: (=> A) => Lazy[B]): Lazy[B] = f(internalValue)
    }

    object Lazy{
        def apply[A](value: => A): Lazy[A] = new Lazy(value)  //Monad Unit
    }

    val lazyInstance = Lazy {
        println("tururu tãruru")
        42
    }

    //Se não der esse print abaixo ele não printa nada, pq os valores lazy não foram chamados
    //println(lazyInstance.use)

    val flatMapped = lazyInstance.flatMap(x => Lazy {
        10 * x
    })

    val flatMapped2 = lazyInstance.flatMap(x => Lazy {
        10 * x
    })

    flatMapped.use
    flatMapped2.use

    /*
    left-identity

    monad.flatMap(f) = f(v)
    Lazy(v).flatMap(f) = f(v)

    right-identity
    monad.flatMap(unit) = monad
    Lazy(v).flatMap(x => Lazy(x)) = Lazy(v)

    associativity
    monad.flatMap(f).flatMap(g) = monad.flatMap(x => f(x).flatMap(g))
    Lazy(v).flatMap(f).flatMap(g) = f(v).flatMap(g)
    Lazy(v).flatMap(x => f(x).flatMap(g)) = f(v).flatMap(g)
     */

    //2 - Map and flatten in terms of flatMap
    //Dada uma monad já implementada, gerar outra com
    // map e flatten

    /*
    Monad[T] { // List
      def flatMap[B](f: T => Monad[B]): Monad[B] = ... (implemented)
      def map[B](f: T => B): Monad[B] = flatMap(x => unit(f(x))) // Monad[B]
      def flatten(m: Monad[Monad[T]]): Monad[T] = m.flatMap((x: Monad[T]) => x)

      List(1,2,3).map(_ * 2) = List(1,2,3).flatMap(x => List(x * 2))
      List(List(1, 2), List(3, 4)).flatten = List(List(1, 2), List(3, 4)).flatMap(x => x) = List(1,2,3,4)
    }
   */





}
