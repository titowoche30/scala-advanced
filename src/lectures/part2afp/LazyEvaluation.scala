package lectures.part2afp

object LazyEvaluation extends App{
    //Lazy values are evaluated once, but only when they're used for the first time
    //Lazy values são evaluated só uma vez quando eles são chamados pela primeira vez
    //Ou seja, lazy atrasa a evaluation de values
    //É como uma chamada by name
    lazy val x = throw new RuntimeException("hello,downton")
   // print(x) //crasha pq agora vai evaluatedar

    lazy val y = {
        println("Iaew")                 //Ele vai evaluetedar esse print só na primeira chamada
        42                              //Esse ele vai mostrar duas vezes mesmo
    }

    println(y)
    println("--")
    println(y)

    //Exemplos
    //1 - side effects
    def sideEffecter: Boolean = {
        println("uhu")
        true
    }

    def simpleCondition: Boolean = false

    lazy val lazyCondition = sideEffecter
    println(if (simpleCondition && lazyCondition) "yes" else "no")
    //O side effect de lazy que é o método não é printado
    // pq ele nem foi evalueted, pq a primeira condição é falsa, aí já retorna falso

    println(if (simpleCondition || lazyCondition) "yes" else "no")
    //Agora sim dá o side effect

    // 2 - In conjuction with call by name
    def byNameMethod(n: => Int): Int = n + n + n +1
    def retrieveMagicValue = {
        Thread.sleep(1000)
        println("...")
        42
    }

//    println(byNameMethod(retrieveMagicValue))
    // vai de fato chamar a função retrieve 3 vezes

    //Podemos substituir isso por lazy val, que vai evaluetadar só 1 vez

    def byNameMethod1(n: => Int): Int = {
        lazy val t = n          //Only evaluated once
        t + t + t + 1
    }

    // Vai aparecer os 3 pontos só 1 vez, pq só vai ser evaluated once
    // o 42 vai ser retornado 3 vezes mesmo
//    println(byNameMethod1(retrieveMagicValue))

    // O nome da técnica é CALL BY NEED
    // Combinar a chamda de um parâmetro por name com uma lazy val recebendo esse parâmetro
    // serve pra quando vc quer evaluatedar um parâmetro  só quando vc precisa
    // mas usar o mesmo valor pelo resto do código

    // 3 - Filtering with lazy vals
    def lessThen30(i: Int):Boolean = {
        println(s"$i eh menor que 30?")
        i < 30
    }

    def greaterThen20(i: Int):Boolean = {
        println(s"$i eh maior que 20?")
        i > 20
    }

    val numbers = List(1,25,40,5,23)
    val lt30 = numbers.filter(lessThen30)   //1,25,5,23
    val gt20 = lt30.filter(greaterThen20) // 25,23
    println(gt20)
    //Printa os side effects tudin como esperado

    val lt30Lazy = numbers.withFilter(lessThen30)   //withFilter usa lazy vals
    val gt20Lazy = lt30Lazy.withFilter(greaterThen20)
    println(gt20Lazy)
    // Nada de side effects, os métodos pra filtrar nem foram chamados
    // o gt20Lazy é um  FilterMonadic[Int, List[Int]]
    gt20Lazy.foreach(println)
    //agora sim

    // for-comprehensions usam withFilter com os guards(os ifs)
    val forada = for {
        a <- (0 to 16).toList if a%2==0
    }yield a + 1

    println(forada)
    //WithFilter transforma num FilterMonadic, o map transforma de volta em List[Int]
    val listada = (0 to 16).toList.withFilter(_%2==0).map(_ + 1 )
    println(listada)








}
