package exercises

import scala.annotation.tailrec

trait MySet[A] extends (A => Boolean){
    def apply(elem: A): Boolean = contains(elem)
    def contains(elem: A):Boolean
    def +(elem:A): MySet[A]
    def ++(anotherSet:MySet[A]):MySet[A]

    def map[B](f:A => B): MySet[B]
    def flatMap[B](f: A => MySet[B]): MySet[B]
    def filter(predicate: A => Boolean): MySet[A]
    def foreach(f: A=> Unit): Unit

    def -(elem: A): MySet[A]                        //remove
    def &(anotherSet: MySet[A]): MySet[A]           //interseção
    def -- (anotherSet: MySet[A]): MySet[A]         //diferença com outro set
    def unary_! : MySet[A]

    def head(): A
    def tail(): MySet[A]
}

class EmptySet[A] extends MySet[A] {
    override def contains(elem: A): Boolean = false

    override def +(elem: A): MySet[A] = new Cons(elem,this)

    override def ++(anotherSet: MySet[A]): MySet[A] = anotherSet

    override def map[B](f: A => B): MySet[B] = new EmptySet[B]

    override def flatMap[B](f: A => MySet[B]): MySet[B] = new EmptySet[B]

    override def filter(predicate: A => Boolean): MySet[A] = this

    override def foreach(f: A => Unit): Unit = ()

    override def -(elem: A): MySet[A] = this

    override def &(anotherSet: MySet[A]): MySet[A] = this

    override def --(anotherSet: MySet[A]): MySet[A] = this



    override def head(): A = throw new NoSuchElementException

    override def tail(): MySet[A] = throw new NoSuchElementException

    override def unary_! : MySet[A] = new PropertyBasedSet[A](_ => true)
}

//property-based set. Conjunto baseado no predicado. Todos os elementos de tipo A que satisfazem a property
//{x in A | property(x)}
//{x pertence D | f(x)}

//Oposto do Empty
//É um infinite set
class PropertyBasedSet[A](property: A => Boolean) extends MySet[A]{
    override def contains(elem: A): Boolean = (property(elem))

    // aparentemente o elemento novo não precisa satisfazer a property
    // {x in A | property(x)} + element = {x in A | property(x) || x == element}
    override def +(elem: A): MySet[A] = {
        new PropertyBasedSet[A](x => property(x) || x == elem)
    }

    // {x in A | property(x)} + set = {x in A | property(x) || set.contains(element)}
    override def ++(anotherSet: MySet[A]): MySet[A] = {
        new PropertyBasedSet[A](x => property(x) || anotherSet(x))
    }

    override def map[B](f: A => B): MySet[B] = politelyFail

    override def flatMap[B](f: A => MySet[B]): MySet[B] = politelyFail

    override def filter(predicate: A => Boolean): MySet[A] = {
        new PropertyBasedSet[A](x => property(x) && predicate(x))
    }

    override def foreach(f: A => Unit): Unit = politelyFail

    override def -(elem: A): MySet[A] = filter(x => x != elem)

    override def &(anotherSet: MySet[A]): MySet[A] = filter(anotherSet)

    override def --(anotherSet: MySet[A]): MySet[A] = filter(!anotherSet)

    override def unary_! : MySet[A] = new PropertyBasedSet[A](x => !property(x))

    def politelyFail = throw new IllegalArgumentException("Rabbit Hole!")

    override def head(): A = ???

    override def tail(): MySet[A] = ???

}

class Cons[A](h:A, t: MySet[A]) extends MySet[A] {
    override def contains(elem: A): Boolean =
        this.h == elem || this.tail.contains(elem)

    override def +(elem: A): MySet[A] = {
        if (this.contains(elem)) this
        else new Cons[A](elem,this)
    }

    /*
    [1,2,3] ++ [4,5] =
    [2,3] ++ [4,5] + 1 =
    [3] ++ [4,5] + 1 + 2 =
    [] ++ [4,5] + 1 + 2 + 3 =
    [4,5] + 1 +2 +3 = [4,5,1,2,3]
     */
    override def ++(anotherSet: MySet[A]): MySet[A] = {
        this.tail ++ anotherSet + this.h
        //new Cons[A](this.h,this.tail ++ anotherSet)
    }

    override def map[B](f: A => B): MySet[B] = {
        this.tail.map(f) + f(this.h)
        //new Cons[B](f(this.head),this.tail.map(f))
    }

    override def flatMap[B](f: A => MySet[B]): MySet[B] = {
         this.tail.flatMap(f) ++ f(this.h)
    }

    override def filter(predicate: A => Boolean): MySet[A] = {
        val filteredTail = this.tail.filter(predicate)
        if (predicate(this.h)) filteredTail + this.h
        else filteredTail
    }

    override def foreach(f: A => Unit): Unit = {
        f(this.h)
        this.tail.foreach(f)
    }

    override def head(): A = this.h
    override def tail(): MySet[A] = this.t


    /*
        [1,2,3,4,5] - 3 =
        [2,3,4,5] - 3 + 1 =
        [3,4,5] - 3 + 2 + 1 =
        [4,5] + 2 + 1 = [4,5,2,1]
     */

    override def -(elem: A): MySet[A] = {
        if (this.head == elem) this.tail
        else this.tail - elem + this.head
    }

    override def &(anotherSet: MySet[A]): MySet[A] =
        filter(anotherSet)                 //Pq o apply do set já retorna um booleano do contains
        //filter(x => anotherSet.contains(x))

    override def --(anotherSet: MySet[A]): MySet[A] =
        filter(!anotherSet)                            // Dado o operador ! já criado
    //   filter(x => !anotherSet.contains(x))           // Tiro os que não contém


    override def unary_! : MySet[A] = new PropertyBasedSet[A](x => !this.contains(x))




    //   override def toString(): String = {
//        s"( this.head" + this.tail.toString() + ")"
//    }
}

object MySet{
    /*
    val s = MySet(1,2,3) = buildSet(seq(1,2,3),[])
    = buildSet(seq(2,3),[] + 1)
    = buildSet(seq(3), [1] + 2)
    = buildSet(seq(), [1 2] + 3)
    = [1 2 3]
     */

    def apply[A](values: A*):MySet[A] = {             //Posso passar múltiplos parâmetros do tipo A
        @tailrec
        def buildSet(valSeq: Seq[A], acc:MySet[A]):MySet[A] = {
            if (valSeq.isEmpty) acc
            else buildSet(valSeq.tail,acc + valSeq.head)
        }

        buildSet(values.toSeq, new EmptySet[A])
    }
}

object MySetPlayground extends App{

    /* V1 TESTS
    val s = MySet(1,2,3,4)
//    s.foreach(println)
//    s + 5 foreach println
//    s + 5 ++ MySet(-3,-13,42) + 5 foreach println
//    s + 5 ++ MySet(-3,-13,42) + 5 map (_*10) foreach println

    s + 5 ++ MySet(-3,-13,42) + 5 flatMap (x => MySet(x, math.log(x))) filter (_ > 1) foreach println
    */

 /* V2 TESTS



  */
//    val s = MySet(1,2,3,4)
//    val negativeS = !s    // todos os naturais diferentes de 4,3,2,1
//    println(negativeS(2))    //false
//    println(negativeS(5))    //true
//
//    val negativeEven = negativeS.filter(_ % 2 == 0)        //Todos os pares >4
//    println(negativeEven(5))
//    val negativeEven5 = negativeEven + 5
//    println(negativeEven5(5))

    val void = MySet(0)
    val all = !void
    val even = all.filter(_ % 2 == 0)
    println(even(17))
    val div6 = even.filter(_ % 3 == 0)
    println(div6(6*13))

    val forada = for{
        i <- 1 to 18
        }yield i + "-" + div6(i)

    println(forada)
}