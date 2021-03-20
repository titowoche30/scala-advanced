package lectures.part3concurrency

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Random, Success}
//important for futures
import scala.concurrent.ExecutionContext.Implicits.global
// O compilador procura esse global pra colocar na segunda lista de parâmetros de Future
// Esse global é uma threadPool no qual vai rodar o future

object FuturesPromisses extends App{
    //Futures are a functional way of computing something in parallel

    def calculateMeaningOfLife: Int = {
        println("Entrou hein")
        Thread.sleep(2000)
        42
    }

    // Future is a computation that will hold a value which is computed by somebody(some thread) at some point in time
    // Ou seja, o valor dentro, vai ser evaluated por alguma thread em alguma hora sem meu controle
    val aFuture = Future {                      //Cria uma instância de Future pelo apply do companion object da trait future
        calculateMeaningOfLife                  //Expression that I want to delegate to another thread
    } //(global) o compilador passa

    println("Waiting on future")

    // Use the value of the future when it actually completes
    // Recebe um Function1[scala.util.Try[T], U] como argumento pq o future pode soltar uma exception

//    aFuture.onComplete(t => t match {
//        case Success(meaningOfLife) => println(s"the meaning of life is $meaningOfLife")
//        case Failure(e) => println(s"Deu ruim com $e")
//    })

    //Pq é partial function
    aFuture.onComplete {
        case Success(meaningOfLife) => println(s"the meaning of life is $meaningOfLife")
        case Failure(e) => println(s"Deu ruim com $e")
    } // SOME Threads executes this
    //On complete retorna unit, ou seja, é usado pra side effects

    Thread.sleep(3000)

    //mini social network

    case class Profile(id: String, name:String) {
        def poke(anotherProfile:Profile) = {
            println(s"${this.name} poked ${anotherProfile.name} ")
        }
    }

    object SocialNetwork {
        // Supondo que tenha um banco de dados de profile como um map

        val names = Map (
            "fb.id.1-zuck" -> "Mark",
            "fb.id.2-bill" -> "Bill",
            "fb.id.0-dummy" -> "Dummy"
        )

        val friends = Map (
            "fb.id.1-zuck" -> "fb.id.2-bill"
        )

        val random = new Random()

        // API
        def fetchProfile(id: String): Future[Profile] = {       //A future which will hold a profile at some point
            Future {
                // fetching from the DB
                Thread.sleep(random.nextInt(300))
                Profile(id,this.names(id))                           //Retorna o profile com esse id
            }
        }

        def fetchBestFriend(profile: Profile): Future[Profile] = Future {
            Thread.sleep(random.nextInt(400))
            val bfId = friends(profile.id)
            Profile(bfId,names(bfId))
        }



    }
//    //mark to poke bill
//    val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
//    mark.onComplete {
//        case Success(markProfile) => {
//            val bill = SocialNetwork.fetchBestFriend(markProfile)
//            bill.onComplete {
//                case Success(billProfile) => markProfile.poke(billProfile)
//                case Failure(e) => e.printStackTrace()
//            }
//        }
//        case Failure(ex) => ex.printStackTrace()
//    }


    //functional composition of futures
    // The recomended approach to do assyncronous computation with Future
    //map, flatMap e filter

    //mark é um Future[Profile]
    //nameOnTheWall uma Future[String]
    val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
    val nameOnTheWall = mark.map(profile => profile.name)
    //marksBestFriend é Future[Profile]
    val marksBestFriend = mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
    val zucksBestFriendRestricted = marksBestFriend.filter(profile => profile.name.startsWith("Z"))

    // for-comprehensions
    for {
        mark <- SocialNetwork.fetchProfile("fb.id.1-zuck")          //dado mark obtido depois de ter completado o RHS
        bill <- SocialNetwork.fetchBestFriend(mark)                     //dado bill obtido depois de ter completado o RHS
    } mark.poke(bill)

    Thread.sleep(1000)

    //fallbacks

    //Quero recuperar a Future com um dummy profile no caso de existir uma exception dentro da future retornada pelo fetchProfile
    val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unkwon id").recover {
        case e: Throwable => Profile("fb.id.0-dummy","Forever Alone")
    }

    val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unkwon id").recoverWith {
        case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")                //Passando um id existente
    }

    // Se a parte antes do fallbackTo não throzar uma exception, fallbackResult recebe ela
    // se não, vai receber o que ta no fallbackTo
    // se o argumento do fallbackTo tbm falhar, vai receber a exception da parte antes do fallbackTo
    val falbackResult = SocialNetwork.fetchProfile("unkwon id").fallbackTo(SocialNetwork.fetchProfile("fb.id.0-dummy"))

    // online banking app
    // como dar block num future
    case class User(name: String)
    case class Transaction(sender:String,receiver:String,amount:Double,status:String)

    object BankingApp{
        val name = "Rock the JVM bankin"

        def fetchUser(name:String): Future[User] = Future {
            //simulate fetching from the DB
            Thread.sleep(500)
            User(name)
        }

        def createTransaction(user: User, merchantName:String, amount:Double): Future[Transaction] = Future {
            // Simulate some processes
            Thread.sleep(1000)
            Transaction(user.name,merchantName,amount,"SUCCESS")
        }

        def purchase(username:String, item:String,merchantName:String,cost: Double): String = {
            // fetch the user from the DB
            // create a transaction
            // wait for the transaction to finish
            val transactionStatusFuture = for {
                user <- fetchUser(username)
                transaction <- createTransaction(user,merchantName,cost)
            }   yield transaction.status

            //Option e um duration object
            Await.result(transactionStatusFuture,2.seconds)
        }

    }

    // Esse objeto vai estar block(ou seja, não vai ser mexido por outra Thread) até que os futures estejam completed
    // E vai printar o status da transaction
    // Não precisei colocar os sleep por conta do Await
    println(BankingApp.purchase("Tito","Xiaomi","mercado livre",1800))

    // PROMISES
    // Manual manipulation of Futures
    // Wraper of future
    // Notice that we can only read or manipulate the results from future by calling onComplete or using function composition
    // Sometimes we need to specifically set or complete a future at a point of our choosing, which is the need that introduces
    // the concept of promises

    val promise = Promise[Int]()        //Controller over a future
    val future = promise.future

    //small producer consumer
    //thread 1 - consumer
    //"reading"
    // Assumindo que o future vai ser preenchido com um valor alguma hora, pode ser qualquer coisa, onComplete não é obrigatório
    future.onComplete {
        case Success(r) => println(s"[consumer] i've received $r")
    }

    //thread 2 - producer
    //"writing"
    val producer = new Thread( () => {
        println("[producer] crunching numbers...")
        Thread.sleep(500)
        //cumprindo a promessa
        promise.success(42)                     //Manipulates the internal future to complete with a sucessful value of 42 which is then
        println("[producer] done")              // handled is onComplete by some consumer Thread
                                                //Só executa o onComplete depois que acaba esse bloco aqui
    })
    // The promisse pattern: First thread knows how to handle the future and Second thread inserts values or a failure into the future by calling
    // .sucess(value) .failure(exception) ou .complete(Try{...}), onde todos esses 3 vão trigerar o .onComplete do future(consumer)

    producer.start()
    Thread.sleep(1000)







}
