package lectures.part3concurrency

import java.util.concurrent.atomic.AtomicReference

import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.immutable.ParVector
import scala.concurrent.forkjoin.ForkJoinPool

object PrallelUtils extends App{
    //1 - Parallel Collections
    //A parallel colection means that operations on them are handled by multiple threads at the same time

    val parList = List(1,2,3,4).par
    val aParVector = ParVector[Int](1,2,3)
    val otherParVector = Vector(1,2,3).par

    /* Podemos criar (não só essas) de
        Seq
        Vector
        Arrays
        Map - HashMap, TrieMap
        Set - HashSets,TrieSets
     */

    def measure[T](operation: => T): Long = {
        val time = System.currentTimeMillis()
        operation
        System.currentTimeMillis() - time
    }

    //big serial list vs big parallel list
    val list = (1 to 10000).toList
    val serialTime = measure {
        list.map(_ + 1)
    }

    println("serial time = " + serialTime)
    val parallelTime = measure {
        list.par.map(_ + 1)
    }

    println("parallel time = " + parallelTime)          // metade do tempo de a lista tiver 10 milhões,
                                                        // tempo maior se a lista tiver 10000
                                                        // Startar e Parar Threads é mais caro que o tempo que uma CPU

                                                        // demora pra processar uma instancia simples duma colecction
     /*
    Isso acontece pq o .par funciona no modelo MapReduce
     O que acontece na parallel colection:
        - Split the elements into chunks which will be processed independently by a single thread each
        - operation is done on each chunk (map)
        - recombine the results (reduce)
    */

    //Cuidado com fold e reduce pq os operators que tu passa pra função podem não ser associativos
    println(List(1,2,3).reduce(_ - _))
    println(List(1,2,3).par.reduce(_ - _))              //Não associativo

    println(List(1,2,3).reduce(_ + _))
    println(List(1,2,3).par.reduce(_ + _))              //Associativo

    // synchronization
    var sum = 0
    List(1,2,3).par.foreach(sum += _)                   // O resultado certo não é garantido, pq tem threads rodando
                                                        // e os side effects podem ser executados em ordens diferentes
                                                        // pq mais de 1 thread pode acessar o sum ao mesmo tempo
                                                        // Race condition
    println(sum)

    // configuring a parallel colection
    // tasksupport é um membro das parallel colections
    aParVector.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(2))    //2 threads

    //2 - atomic operations and references
    // atomic operation = cannot be divided, it either runs fully or not at all
    // in multithread context, an atomic operation cannot be intercepted by another thread

    // atomic types are thread safe
    val atomic = new AtomicReference[Int](2)
    val currentValue = atomic.get()         // thread-safe read
    atomic.set(34)                          // thread-safe write
    atomic.getAndSet(55)           // thread-safe combo, retorna o antigo e seta um novo

    atomic.compareAndSet(39,90)    // se o valor for 39, então seta pra 90, senão, num faz nada
    atomic.updateAndGet(_ + 1)                    // thread-safe function run, roda uma função e pegar o valor final
    atomic.getAndUpdate(_ + 1)                      // primeiro pega dps roda

    atomic.accumulateAndGet(12,_ - _)               //thread safe accumulation, atual - argumento
    println(atomic.getAndAccumulate(12, _ - _))


}
