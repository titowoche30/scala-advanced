package lectures.part3concurrency

import java.util.concurrent.Executors

object Intro extends App{
    //Creation,Manipulation and Comunication of JVM Threads

    /*
    Pra instanciar uma thread tem que mandar uma instância
    da Trair runnable, que só tem 1 método, que não está implementado
    chamado run
     */

    val aThread = new Thread(new Runnable {
        override def run(): Unit = println("Correndo em paralelo")
    })

    //Essa linha abaixo cria uma JVM Thread que roda numa OS Thread( operatin system)
    //Ou seja, roda em uma JVM Thread diferente da desse código aqui
//    aThread.start()
//    aThread.join()      //blocks until aThread finishes running

    val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello")))
    val threadBye = new Thread(() => (1 to 5).foreach(_ => println("falooou")))
    // Cada vez que executar, essas de baixo vão produzir diferentes resultados
    // Pq different runs with a multithread environment produce different results
//    threadHello.start()
//    threadBye.start()

    //Executors
    //Threads are expensive to start and kill, the solution is to reuse them
//    val pool = Executors.newFixedThreadPool(10)
//    //abaixo o runnable vai ser executado por 1 das 10 threads gerenciadas pela Thread pool acima
//    pool.execute(() => println("something in the waaay, hmmmm"))
//    pool.execute(() => {
//        Thread.sleep(1000)
//        println("Dormiu por 1 segundo")
//    })
//    pool.execute(() => {
//        Thread.sleep(1000)
//        println("Quase hein")
//        Thread.sleep(1000)
//        println("Cordoooou :) ")
//    })

    // Essas de cima são executadas sequencialmente, mas como foi tudo na mesma
    // pool, as duas primeiras vão ser quase ao mesmo tempo, pq deleguei pra 2 das 10 threads disponíveis
    // As 3 threads vão ser executadas ao mesmo tempo(paralelo)

    //No more actions can be submited depois que as paradas foram executadas
//    pool.shutdown()
    //Shut logo, nem espera execução, interrompe os sleep
    //pool.shutdownNow()

    //vai retornar true mesmo enquanto executa
    //pq o shutdown é pra não receber mais ações
//    println(pool.isShutdown)

    def runInParallel:Unit = {
        var x = 0

        val thread1 = new Thread(() => {
            x = 1
        })

        val thread2 = new Thread(() => {
            x = 2
        })

        thread1.start()
        thread2.start()
        println(x)
    }

 //   for (_ <- 1 to 100) runInParallel
    // Race condition
    // Dá valores diferentes pq duas threads tão mexendo no
    // mesmo endereço de memória ao mesmo tempo

    class BankAccount(var amount: Int) {
        override def toString: String = "" + amount
    }

    def buy(account:BankAccount, thing:String, price:Int) = {
        account.amount -= price
    //    println("I've bought " + thing)
     //   println("my account is now " + account)
    }

//    for (_ <- 1 to 1000){
//        val account = new BankAccount(50000)
//        val thread1 = new Thread(() => buy(account,"shoes",3000))
//        val thread2 = new Thread(() => buy(account,"iphone12",4000))
//
//        thread1.start()
//        thread2.start()
//        Thread.sleep(10)
//        if (account.amount != 43000) println("aha:" + account.amount)
//        //println()
//    }

    //Race condition problem
    //Pq isso acontece?
    //A thread 2 pode acessar amount antes de a thread 1 ter completado a operação
    /*
     thread1 (shoes): 50000
     - account = 50000 - 3000 = 47000
     thread2 (iphone): 50000
     - account = 50000 - 4000 = 46000 overwrites the memory of account.amount
     */


    //Como resolver
    //Option #1: Use synchronized
    //Entering a synchronized expression on an object locks the object

    def buySafe(account: BankAccount,thing:String,price: Int) = {
        //No two threads can evaluate the expression inside synchronized at the same time
        account.synchronized {
            account.amount-=price
           // println("I've bought " + thing)
           // println("my account is now " + account)
        }

    }

        //Option #2: use @volatile
        //On val or a var means that all the reads and writes
        // to it are synchronized

        //mete um     class BankAccount(@volatile var amount: Int)

        //a .synchronized é mais poderosa pq deixa vc colocar múltiplas
        // expressões num bloco e dá mais controle sob o que vc quer isolar entre threads
        //

        //Exercícios
    /*
    * 1) Construct 50 "inception" threads
      *     Thread1 -> thread2 -> thread3 -> ...
    *     println("hello from thread #3")
    *   in REVERSE ORDER
    */

    def inceptionThreads(maxThreads: Int, i: Int = 1): Thread = {
        new Thread(() => {
            if (i < maxThreads) {
                val newThread = inceptionThreads(maxThreads,i+1)
                newThread.start()
                newThread.join()
            }
            println(s"Hello from thread $i")
        })
    }


    inceptionThreads(50).start()


    // 2)
    var x = 0
    val threads = (1 to 100).map(_ => new Thread(() => x+=1))
    threads.foreach(_.start())
    threads.foreach(_.join())
    println(x)
        /*
        1 - qual o maior valor possível para x? 100
        2 - qual o menor valor possível para x? 1
            Pq as threads rodam juntas em paralelo, pode acontecer de 2 ou mais acessarem x na mesma hora e incrementarem na mesma hora
            ex:
                thread1: x = 0
                thread2: x = 0
                ...
                thread100: x = 0

                pode acontecer de todas as threads incrementarem ao mesmo tempo e x terminar com 1

         */


    //3) Sleep falacy
    var message = ""
    val awesomeThread = new Thread(() => {
        Thread.sleep(1000)
        message = "Scala is awesome"
    })

    message = "Scala sucks"
    awesomeThread.start()
    Thread.sleep(2000)
    awesomeThread.join() // sol. do problema - wait for the awesome thread to join
    println(message)
/*
      what's the value of message? almost always "Scala is awesome"
      is it guaranteed? NO!
      why? why not?

      Colocar uma Thread pra dormir por um tempo x e outra thread por 2x
      NÃO significa que elas vão ser executadas em ordem
      explicação na aula 2 da parte 3, 25:00

    Como ajeitar?
    Synchronized não funciona pq ele só é útil pra mudanças concorrentes, ou seja, num caso onde duas
    threads tentam modificar a mensagem ao mesmo tempo,já o problema aqui é sequencial

    A solução é meter um join
*/


}
