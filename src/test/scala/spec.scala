import com.robert42.ft.{MongoConfig, Todo, Todos}
import org.junit.Test
import com.codahale.simplespec.Spec
import com.foursquare.rogue.Rogue._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.Calendar

class AppSpec extends Spec {
  MongoConfig.init

  val TEXT        = "Something."
  val UPDATE_TEXT = "Something else."

  class `A MongoDB document` {
    @Test def `can be CRUDed from JSON.` = {
      // Creation.
      val json = ("text" -> TEXT) ~ ("order" -> 1L) ~ ("done" -> false)
      Todos.fromJson(compact(render(json)), create = true)

      // Querying.
      val query = Todo where (_.text eqs TEXT) get
      val todo  = query.get
      assertTodo(todo)

      // Updating.
      val jsonForUpdate = ("_id" -> todo.id.toString) ~ ("done" -> true) ~
        ("createdAt" -> todo.createdAt.value.getTimeInMillis) ~
        ("text" -> UPDATE_TEXT)
      val updatedTodo = Todos.fromJson(
        compact(render(jsonForUpdate)), update = true
      )
      assertTodo(updatedTodo, updated = true)

      // Deletion.
      Todos remove todo.id.toString
    }

    @Test def `can be CRUDed from XML.` = {
      // Creation.
      val xml = <todo>
                  <text type="string">Something.</text>
                  <order type="int">1</order>
                  <done type="boolean">false</done>
                </todo>
                  .toString
      Todos.fromXml(xml, create = true)

      // Querying.
      val query = Todo where (_.text eqs TEXT) get
      val todo  = query.get
      assertTodo(todo)

      // Updating.
      def makeXmlForUpdate(id: String, text: String) =
        <todo id={id}>
          <text type="string">{text}</text>
          <done type="boolean">true</done>
        </todo>
          .toString
      val xmlForUpdate = makeXmlForUpdate(todo.id.toString, UPDATE_TEXT)
      val updatedTodo  = Todos.fromXml(xmlForUpdate, update = true)
      assertTodo(updatedTodo, updated = true)

      // Deletion.
      Todos remove todo.id.toString
    }

    def assertTodo(todo: Todo, updated: Boolean = false) = {
      if (!updated) {
        todo.text.value       must equalTo(TEXT)
        todo.order.value      must equalTo(1L)
        todo.done.value       must equalTo(false)
        todo.modifiedAt.value must be(None)
      } else {
        todo.text.value       must equalTo(UPDATE_TEXT)
        todo.order.value      must equalTo(1L)
        todo.done.value       must equalTo(true)
        todo.modifiedAt.value must not be(None)
      }
      todo.createdAt.value    must not equalTo(null)
    }
  }
}
