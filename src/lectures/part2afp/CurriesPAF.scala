package lectures.part2afp

object CurriesPAF extends App{
    //curried functions
    val superAdder: Int => (Int => Int) = x => (y => x + y)

    val add3 = superAdder(3)
    println(add3(5))
    println(superAdder(5)(3))

    def curriedAdder(x:Int)(y:Int): Int = x + y     //curried method

    val add4: Int => Int = curriedAdder(4)          //Só funciona se tiver a type anotation, pq ele ta convertendo um método pra uma função int => int
    //To transformando em partial function pq o pegando um sub-conjunto do domínio
    println(add4(3))
    println(curriedAdder(5)(3))


    // lifting é esse processo de transformar um método em uma função
    // termo técnico: ETA-EXPANSION. Wraping function in extra layer while preserving identical funcionality

    //functions !+ methods
    def inc(x:Int):Int = x + 1
    List(1,2,3).map(inc)      //Quando faço isso de usar método como função, o compilador faz um ETA-expansion pra transformar um método numa função
                              // e usa o value da função no map
    //Faz isso List(1,2,3).map(x => inc(x))

    //Posso forçar o ETA

    //Partial function applications
    val add5 = curriedAdder(5) _  //Diz pro compilador fazer ETA e converter pra function

    val simpleAddFunction = (x:Int,y:Int) => x + y
    def simpleAddMethod(x:Int, y:Int):Int= x + y
    def curriedAddMethod (x:Int)(y:Int):Int = x + y
    def simpleMultiplyMethod(x:Int, y:Int):Int= x * y
    def curriedMultiplyMethod (x:Int)(y:Int):Int = x * y

    val add7_0 = (x:Int) => simpleAddFunction(7,x)      // o output pode ser qualquer uma das 3
    println(add7_0(3))

    val add7_1 = simpleAddFunction.curried(7)
    println(add7_1(3))

    val add7_2 = curriedAddMethod(7) _
    //val add7_2 = curriedAddMethod(7)(_)           //mesma coisa
    println(add7_2(3))

    //A notação (_) tbm funciona pra non curried methods

    val add7_3 = simpleAddMethod(7,_:Int)          //transf em currie, ou seja, transformou o método em function value
    println(add7_3(3))

    val add7_4 = simpleAddFunction(7,_:Int)        //Serve pra functions tbm
    println(add7_4(3))

    val add10mult2 = curriedAddMethod(10) _ compose curriedMultiplyMethod(2)   //Da direita pra esquerda, Primeiro multiplica por 2, dps soma 10
    println("aaa" + add10mult2(10))
    val add2mult10 = curriedMultiplyMethod(10) _ andThen curriedAddMethod(2)   //Da esquerda pra direita, Primeiro multiplica por 10, dps adiciona 2
    println("bbbb" + add2mult10(10))

    //The underscore forces the compiler to expand a method to a function value
    //Transorma numa função que tem o método dentro
    def concatenator(a:String,b:String,c:String):String = a + b + c

    val insertName = concatenator("Olá, eu sou o ", _:String, ", como você está?")     //x:String => concatenator("Olá, eu sou o ", x, ", como você está?")
    println(insertName("Tito"))

    val fillTheBlanks = concatenator("Hello", _:String,_:String)                     // (x:String,y:String) => concatenator("Hello",x,y)
    println(fillTheBlanks(" meu"," xapa"))

    //EXERCÍCIOS

    def formater(format: String)(number: Double) = {
        format.format(number)
    }

    val numbers = List(Math.PI,Math.E)
    val formats = List("%8.6f","%4.2f","%14.12f")

    val format1 = formater(formats(0))(_)      //lift
    val format2 = formater(formats(1))(_)
    val format3 = formater(formats(2))(_)

    val forada = for {
        form <- List(format1,format2,format3)
        num <- numbers
    }yield form(num)

    println(forada)
    val mapada = numbers.flatMap(n => List(format1,format2,format3).map(f => f(n)))
    println(mapada)

    println(numbers.map(formater(formats(2))))         //compilador já faz o ETA, não precisa do (_)

    //0-lambda é uma lambda assim: f: () => Int
    def byName(n: => Int) = n + 1     //n passado by name
    def byFunction(f: () => Int) = f() + 1

    def method: Int = 42
    def parenMethod(): Int = 42


    byName(23)   //ok
    byName(method)  //ok
    byName(parenMethod()) //ok
//    byName(() => 42)   //not ok]
    byName((() => 42)())   //ok
//    byName(parenMethod _) //not ok

//    byFunction(43)   //not ok
//    byFunction(method)  //not ok
    byFunction(parenMethod)   //ok
    // Métodos sem parâmetros com e sem parênteses SÃO diferentes, os sem não podem ser passados pra HOFs, pq o compilador não faz ETA neles

    byFunction(parenMethod _)  //ETA na mao

    byFunction(() => 3434)   //ok

}
