package lectures.part4implicits

object TypeClasses extends App{
    // Aquela trait Ordering  de ImplicitsIntro é uma type class, e a gente definiu nossas instancias de uma
    // type class

    // Type classes são uma Trait que recebe um type e descreve que operações podem ser aplicadas a esse tipo
    // Quem herda de uma type class é chamado de type class instance(normalmente são singleton objects)
    // ,mesmo que ele mesmo seja uma classe

    trait HTMLWritable {
        def toHtml: String
    }

    case class User(name:String,age:Int,email:String) extends HTMLWritable{
        override def toHtml: String = s"<div> $name ($age yo) <a href=$email/> </div>"
    }

    val judo = User("John",32,"john@laga.com").toHtml

    /*
    Em relação ao de cima
     1 - Only works for the types WE write
     2 - One implementation out of quite a number
    */

    // option 2 - pattern matching
    object HTMLSerializerPM {
        def serializeToHtml(value: Any) = value match {
            case User(n,a,e) =>
            case _ =>
        }
    }

    /*
    Em relação ao de cima
     1 - lost type safety
     2 - need to modify the code everytime some new data structure is aded
     3 - still one implementation
    */

    // option 3 - better design

    //Type Class
    trait HTMLSerializer[T]{
        def serialize(value: T): String
    }

//    object UserSerializer extends HTMLSerializer[User]{
//        override def serialize(user: User): String = s"<div> ${user.name} (${user.age} yo) <a href=${user.email}/> </div>"
//    }

    val john = User("John",32,"john@laga.com")
//    println(UserSerializer.serialize(john))

    /*
    Em relação ao de cima
     1 - We can define sealizers for other types
     2 - We can define multiple seralizers
     3 - still one implementation
    */

    //2
    object PartialUserSerializer extends HTMLSerializer[User]{
        override def serialize(user: User): String = s"<div> ${user.name}  </div>"
    }

    // TYPE CLASS Template
    // All the implementors of this type class template need to suply an implementation for this action
//    trait MyTypeClassTemplate[T]{
//        def action(value: T): String
//    }

    // Exercícios
    // Type Class
//    trait Equal[T] {
//        def apply(value1:T, value2:T): Boolean
//    }
//    // Type Class instance
//    object NameEqual extends Equal[User]{
//        override def apply(value1: User, value2: User): Boolean = value1.name == value2.name
//    }
//    // Type Class instance
//    object EmailEqual extends Equal[User]{
//        override def apply(value1: User, value2: User): Boolean = value1.email == value2.email
//    }
//    // Type Class instance
//    object NameEmailEqual extends Equal[User]{
//        override def apply(value1: User, value2: User): Boolean =
//            value1.name == value2.name && value1.email == value2.email
//    }
//
//    val tito = User("Tito",22,"titowoche30@gmail.com")
//    val claudemir = User("Claudemir",22,"titowoche30@gmail.com")
//    println(NameEqual(tito,claudemir))
//    println(EmailEqual(tito,claudemir))
//    println(NameEmailEqual(tito,claudemir))

    // Implicits com type classes

    object HTMLSerializer {
        def serialize[T](value:T)(implicit serializer: HTMLSerializer[T]): String =
            serializer.serialize(value)

        def apply[T](implicit serializer: HTMLSerializer[T]) = serializer
    }

//    implicit object IntSerializer extends HTMLSerializer[Int]{
//        override def serialize(value: Int): String = s"<div style: color = blue> $value </div>"
//    }
//
//    implicit object UserSerializer extends HTMLSerializer[User]{
//        override def serialize(user: User): String = s"<div> ${user.name} (${user.age} yo) <a href=${user.email}/> </div>"
//    }

//    println(HTMLSerializer.serialize(42))
//    println(HTMLSerializer.serialize(claudemir))
//    //Access to the entire type class interface with this type
//    println(HTMLSerializer[User].serialize(tito))

    trait MyTypeClassTemplate[T]{
        def action(value: T): String
    }

    object MyTypeClassTemplate{
        def apply[T](implicit instance: MyTypeClassTemplate[T]) = instance
    }

    //Exercise: Meter implicit na Trait de equal
    trait Equal[T] {
        def apply(value1:T, value2:T): Boolean
    }

    object Equal{
        def apply[T](value1: T, value2: T)(implicit equalizador: Equal[T]) =
            equalizador.apply(value1,value2)
    }

    // Type Class instance
//    implicit object NameEqual extends Equal[User]{
//        override def apply(value1: User, value2: User): Boolean = value1.name == value2.name
//    }
//    // Type Class instance
//    implicit object EmailEqual extends Equal[User]{
//        override def apply(value1: User, value2: User): Boolean = value1.email == value2.email
//    }
    // Type Class instance
    implicit object NameEmailEqual extends Equal[User]{
        override def apply(value1: User, value2: User): Boolean =
            value1.name == value2.name && value1.email == value2.email
    }


    val tito = User("Tito",22,"titowoche30@gmail.com")
    val claudemir = User("Claudemir",22,"titowoche30@gmail.com")
    println(Equal[User](tito,claudemir))
    //De cima é AD-HOC polymorphism
//    println(EmailEqual(tito,claudemir))
//    println(NameEmailEqual(tito,claudemir))


}
