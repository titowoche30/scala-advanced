package lectures.part3concurrency

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future, Promise, promise}
import scala.util.{Failure, Random, Success, Try}

object FuturesPromissesExercicios extends App{
    //Exercícios
    //1) Fulfill a future IMMEDIATELY with a value

    //Jeito clássico com promise
    /*
    val promise1 = Promise[Int]()
    val future = promise1.future
    future.onComplete {
        case Success(num) => println(s"Chegou $num ")
    }
    val thread = new Thread( () => {
        promise1.success(34)
    })
    thread.start()
    Thread.sleep(500)               //Tem que ter esse sleep pra dar tempo da 'thread' executar
    */

    //Outro jeito com promisse
//    val promise2 = Promise[Int]()
//    val future_value = promise2.success(38)
//    println(future_value)
//
//    //Sem promisse mas notcompleted
//    val valueFuture = Future[Int](34)
//    println(valueFuture)

    //2) inSequence(fa,fb) = função que roda futureB depois que futureA tiver sido rodado

    def inSequence[A,B](fa: Future[A], fb: Future[B]):Future[B] = {
        //'_' é o valor já completado do fa
        fa.flatMap(_ => fb)
    }

    //3) first(fa,fb) => new future with the value of the future who finishes firts
    def first[A](fa: Future[A], fb: Future[A]): Future[A] = {
        val promise = Promise[A]()        //É um controlador de Future[A], posso usar ele dentro do onComplete das Futures

        //Se eu tentar dar sucess ou failure numa promisse que já deu antes, vai dar uma exceção
        def tryComplete(promise: Promise[A], result: Try[A]) = result match {
            case Success(r) => try {
                promise.success(r)
            } catch {
                case _ => //faz nada
            }
            case Failure(t) => try {
                promise.failure(t)
            } catch {
                case _ => //faz nada
            }
        }

        fa.onComplete(value => tryComplete(promise,value))              //value vai ter um Sucess ou Failure, que são filhos do Try
        fb.onComplete(value => tryComplete(promise,value))

        /* OU
        //Essa função do promise retorna um boolean indicando se a promise foi cumprida pelo _ ou não
        fa.onComplete(promise.tryComplete(_))
        fb.onComplete(promise.tryComplete(_))
         */

        promise.future                                          //Vai ter o value ou a exception do que terminar primeiro
    }


    //4) last(fa,fb) => new future with the value of the future who finishes last
    def last[A](fa: Future[A], fb: Future[A]): Future[A] = {
        // 1 promise which both futures will try to complete,ou seja, when both futures complete they will try to fulfill this first promise
        // O primeiro vai conseguir fulfilar, mas o segundo não
        // 2 promise which the last future will complete

        val bothPromise = Promise[A]
        val lastPromise = Promise[A]

        fa.onComplete(value => {
            if (!bothPromise.tryComplete(value))        //Se já tiver dado complete, vai retornar falso, que vai entrar no if
                lastPromise.complete(value)
        })

        fb.onComplete(value => {
            if (!bothPromise.tryComplete(value))
                lastPromise.complete(value)
        })

        /* OU
        val checkAndComplete = (result: Try[A]) =>
            if (!bothPromise.tryComplete(value))
                lastPromise.complete(value)

        fa.onComplete(checkAndComplete)
        fb.onComplete(checkAndComplete)

         */


        lastPromise.future
    }

    //Por mais que isso não garanta a ordem dos resultados, aqui vai dar bom
    val fast = Future {
        Thread.sleep(100)
        11
    }

    val slow = Future {
        Thread.sleep(200)
        22
    }

    first(fast,slow).foreach(println)
    last(fast,slow).foreach(println)

    Thread.sleep(1000)

    //5) retryUntil[T](action: () => Future[T], condition: T => Boolean)
    //Executar a ação enquanto a condição não for satisfeita, sendo satisfeita retorna o Future
    def retryUntil[A](action : () => Future[A], condition: A => Boolean): Future[A] = {
        action()
          .filter(condition)
          .recoverWith {                                    //Se o filter não match the condition, vai jogar uma excessão que via pro recoverwith
              case _ => retryUntil(action,condition)
          }
    }

    val random = new Random()
    val action = () => Future {
        Thread.sleep(100)
        val rand = random.nextInt(100)
        println("Gerou "+ rand)
        rand
    }
    val condition = (i:Int) => i < 30

    retryUntil(action,condition).foreach(result => println("Parou no " + result))
    Thread.sleep(10000)                  //O tempo q eu colocar aqui e o tempo que o retryUntil vai ficar rodando









}
