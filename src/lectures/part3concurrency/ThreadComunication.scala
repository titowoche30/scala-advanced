package lectures.part3concurrency

import scala.collection.mutable
import scala.util.Random

object ThreadComunication extends App{
    /*
    Multithreaded problem
    The producer-consumer problem

    producer -> [x] -> consumer
    producer: set a value inside the container
    consumer: extract the value from the container
    Producer and consumer run in parallel at the same time, so they don't know when each other has finished working.
    Somehow make the consumer wait to the producer to finish it's job.
    Garantee the threads to run in a garanteed certain order
     */

    //LEVEL 1 - Produz e consome só 1 elemento
    class SimpleContainer {
        private var value: Int = 0

        def isEmpty: Boolean = value == 0

        // Como se fosse o producing method
        def set(newValue:Int) = {
            value = newValue
        }

        // Pega o valor e seta ele pra default
        // Como se fosse o Consuming method
        def get = {
            val result = value
            value = 0
            result
        }
    }

    def naiveProdCons(): Unit = {
        val container = new SimpleContainer
        val consumer = new Thread(() => {
            println("[consumer] waiting...")                //wait as long as the container is empty
            while(container.isEmpty) {
                println("[consumer] actively waiting")
            }
            println("[consumer] I have consumed " + container.get)
        })

        val producer = new Thread( () => {
            println("[producer] computing...")
            Thread.sleep(500)
            val value = 34
            println("[producer] I have produced the value " + value)
            container.set(value)
        })

        consumer.start()
        producer.start()
    }

    //naiveProdCons()

    // synchronized lock the object's monitor
    // monitor is a data structure internaly used by the JVM to keep track of each object is locked by each thread
    // cada objeto ta lá no monitor
    // quando sai do synchronized o lock é liberado e qualquer outra thread está livre pra acessar

    //wait() - chamar wait() num objeto do monitor suspends the thread indefinitely e só continua quando outra thread do mesmo objeto
    // chamar notify(). Chamar notify() manda o sinal pra uma sleeping thread que ela pode continuar(vc n tem controle sob qual)
    // notifyAll awake all the threads
    //wait() e notify() só funcionam em synchronized expressions

    def smartProdCons(): Unit = {
        val container = new SimpleContainer

        val consumer = new Thread( () => {
            println("[consumer waiting]")
            container.synchronized {
                container.wait()
            }
            println("[consumer] I have consumed " + container.get)
        })

        val producer = new Thread( () => {
            println("[producer] hard at work...")
            Thread.sleep(2000)
            val value = 42

            container.synchronized {
                println("[producer] I'm producing " + value)
                container.set(value)
                container.notify()
            }
        })

        consumer.start()
        producer.start()
    }

    //smartProdCons()

    /*
        Buffer é uma região de memória física utilizada para armazenar temporariamente os dados enquanto eles estão sendo
        movidos de um lugar para outro.
        Os buffers normalmente são usados quando há uma diferença entre a taxa a qual os dados são recebidos e a taxa a
        qual eles podem ser processados, ou no caso em que estas taxas são variáveis.

        producer -> [? ? ? ] -> consumer
        Producer produz muitos valores, consumer consome qualquer valor novo
        Buffer é o [? ? ?]
        Caso o Buffer encha, o producer deve parar até que o consumer tire to;do mundo do buffer e vice-versa
     */

    //LEVEL 2 - Produz e consome múltiplos elementos
    def prodConsLargeBuffer(): Unit = {
        val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
        val capacity = 3            //producer cannot produce more then capicity values without blocking e consumer ...

        val consumer = new Thread( () => {
            val random = new Random()

            while(true) {
                buffer.synchronized {
                    if (buffer.isEmpty){
                        println("[consumer] buffer empty, to esperano")
                        buffer.wait()
                    }
                    //Fora do if it must be at least one value in the buffer ou o buffer foi woken(pq foi setado e tem valor)
                    val x = buffer.dequeue()
                    println(s"[consumer] consumed $x")

                    //hey producer, there's empty space available
                    buffer.notify()
                }
                Thread.sleep(random.nextInt(500))
            }
        })

        val producer = new Thread( () => {
            val random = new Random()
            var i = 0

            while(true) {
                buffer.synchronized {
                    if (buffer.size == capacity) {
                        println("[producer] buffer is full, waiting...")
                        buffer.wait()               //esperar pelo consumer notify
                    }
                    println("[producer] producing " + i)
                    buffer.enqueue(i)                           //Coloca o elemento i

                    //Hey consumer, chegou mercadoria
                    buffer.notify()
                    i+=1
                }
                Thread.sleep(random.nextInt(500))
            }
        })

        consumer.start()
        producer.start()
    }

    //prodConsLargeBuffer()


    //LEVEL 3 - Multiple producers and multiple consumers in a limited capacity buffer

      /*
        producer1 -> [? ? ? ? ] -> consumer1
        producer2 ----^        ^---- consumer2
       */

    class Consumer(id:Int, buffer: mutable.Queue[Int]) extends Thread{
        override def run(): Unit = {
            val random = new Random()

            while(true) {
                buffer.synchronized {
                    while (buffer.isEmpty){
                        println(s"[consumer $id] buffer empty, to esperano")
                        buffer.wait()
                    }
                    val x = buffer.dequeue()
                    println(s"[consumer $id] consumed $x")

                    //hey producer, there's empty space available
                    buffer.notify()
                }
                Thread.sleep(random.nextInt(500))
            }
        }
    }

    class Producer(id:Int,buffer:mutable.Queue[Int],capacity:Int) extends Thread{
        override def run(): Unit = {
            val random = new Random()
            var i = 0

            while(true) {
                buffer.synchronized {
                    while (buffer.size == capacity) {
                        println(s"[producer $id] buffer is full, waiting...")
                        buffer.wait()               //esperar pelo consumer notify
                    }
                    println(s"[producer $id] producing " + i)
                    buffer.enqueue(i)                           //Coloca o elemento i

                    //Hey consumer, chegou mercadoria
                    buffer.notify()
                    i+=1
                }
                Thread.sleep(random.nextInt(500))
            }
        }
    }

    def multiProdCons(nConsumers:Int, nProducers: Int): Unit = {
        val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
        val capacity = 3            //producer cannot produce more then capicity values without blocking e consumer ...

        (1 to nConsumers).foreach(i => new Consumer(i,buffer).start())
        (1 to nProducers).foreach(i => new Producer(i,buffer,capacity).start())
    }


    //multiProdCons(3,3)

    /*
    Exercícios
    1) Think of an example where notifyAll acts in a different way than notify? Because in our example, they end up doing the same thing
    2) Create a deadlock. Deadlock: Situation where one or multiple threads block each other and they cannot continue
    3) Create a livelock. Livelock: Situation where one or multiple threads yield executions to each other in such a way that nobody can continue
     */

    //1)
    def testNotifyAll(): Unit = {
        val bell = new Object

        (1 to 10).foreach(i => new Thread( () => {
            bell.synchronized {
                println(s"[thread $i] waiting")
                bell.wait()
                println(s"[thread $i] woke up")
            }
        }).start())

        new Thread( () => {
            Thread.sleep(2000)
            println("[announcer] acorda cornarada")
            bell.synchronized {
                bell.notifyAll()                               //Com notify, só 1 thread vai acordar e as outras vão ficar travadas
                                                            //com notifyAll to;do mundo acorda e o programa continua
            }
        }).start()
    }

//    testNotifyAll()

    //2) Deadlock

    case class Friend(name: String) {
        def bow(other:Friend):Unit = {
            this.synchronized {
                println(s"$this: I'm bowing to my friend $other")
                other.rise(this)
                println(s"$this: my friend $other has risen ")
            }
        }

        def rise(other: Friend): Unit = {
            this.synchronized {
                println(s"$this: I'm rising to my friend $other")
            }
        }

        var side = "right"
        def switchSide(): Unit = {
            if (side == "right") side = "left"
            else side = "right"
        }

        def pass(other: Friend):Unit = {
            while (this.side == other.side) {
                println(s"$this, meu xapa $other, pode passar")
                switchSide()
                Thread.sleep(1000)
            }
        }


    }

    val tito = Friend("Tito")
    val woche = Friend("Woche")

//    new Thread( () => tito.bow(woche)).start()              //Dá ruim pq um trava o outro
//    new Thread( () => woche.bow(tito)).start()

    //3) Livelock
    new Thread( () => tito.pass(woche)).start()
    new Thread( () => woche.pass(tito)).start()

}
