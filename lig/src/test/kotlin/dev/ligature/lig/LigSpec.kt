/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.lig

import arrow.core.Either
import arrow.core.getOrElse
import dev.ligature.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import dev.ligature.rakkoon.*

class LigSpec : FunSpec() {
    init {
        val ligParser = LigParser()
        val ligWriter = LigWriter()

        val testEntity = Entity.from("test").getOrElse { TODO() }
        val testAttribute = Attribute.from("test").getOrElse { TODO() }

        fun entity(id: String): Entity = Entity.from(id).getOrElse { TODO() }
        fun attribute(name: String): Attribute = Attribute.from(name).getOrElse { TODO() }

        test("write entities") {
            val res = ligWriter.writeEntity(testEntity)
            res shouldBe "<test>"
        }

        test("parse entities") {
            val test = "<test>"
            val entity = ligParser.parseEntity(Rakkoon(test), null)
            entity shouldBe Either.Right(testEntity)
        }

        test("complex entity parsing") {
            val entity = "<http$://&&this@2]34.[42;342?#--__>"
            val entityRes = ligParser.parseEntity(Rakkoon(entity))
            entityRes shouldBe Either.Right(entity("http\$://&&this@2]34.[42;342?#--__"))
        }

        test("write attributes") {
            val res = ligWriter.writeAttribute(testAttribute)
            res shouldBe "@<test>"
        }

        test("parse attributes") {
            val test = "@<test>"
            val attribute = ligParser.parseAttribute(Rakkoon(test), null)
            attribute shouldBe Either.Right(testAttribute)
        }

        test("complex attribute parsing") {
            val attribute = "@<http$://&&this@2]34.[42;342?#--__>"
            val attributeRes = ligParser.parseAttribute(Rakkoon(attribute))
            attributeRes shouldBe Either.Right(attribute("http\$://&&this@2]34.[42;342?#--__"))
        }

        test("write FloatLiteral") {
            val test = FloatLiteral(3.0)
            val res = ligWriter.writeValue(test)
            res shouldBe "3.0"
        }

        test("parse FloatLiteral") {
            val test = "3.5"
            val res = ligParser.parseFloatLiteral(Rakkoon(test))
            res shouldBe Either.Right(FloatLiteral(3.5))
        }

        test("write IntegerLiteral") {
            val test = IntegerLiteral(3535)
            val res = ligWriter.writeValue(test)
            res shouldBe "3535"
        }

        test("parse IntegerLiteral") {
            val test = "3452345"
            val res = ligParser.parseIntegerLiteral(Rakkoon(test))
            res shouldBe Either.Right(IntegerLiteral(3452345))
        }

        test("write StringLiteral") {
            val test = StringLiteral("3535 55Hello")
            val res = ligWriter.writeValue(test)
            res shouldBe "\"3535 55Hello\""
        }

        test("parse StringLiteral") {
            val test = "\"3452345\\nHello\""
            val res = ligParser.parseStringLiteral(Rakkoon(test))
            res shouldBe Either.Right(StringLiteral("3452345\\nHello"))
        }

        test("basic Statement with all Entities") {
            val statement = Statement(entity("e1"), attribute("a1"), entity("e2"), entity("context"))
            val lines = ligWriter.write(listOf(statement).iterator())
            val resStatements = ligParser.parse(lines)
            listOf(statement) shouldBe resStatements.asSequence().toList()
        }

        test("list of Statements with Literal Values") {
            val statements = listOf(
                Statement(entity("e1"), attribute("a1"), entity("e2"), entity("context")),
                Statement(entity("e2"), attribute("a2"), StringLiteral("string literal"), entity("context2")),
                Statement(entity("e2"), attribute("a3"), IntegerLiteral(Long.MAX_VALUE), entity("context3")),
                Statement(entity("e3"), attribute("a4"), FloatLiteral(7.5), entity("context4"))
            )
            val lines = ligWriter.write(statements.iterator())
            val resStatements = ligParser.parse(lines)
            statements shouldBe resStatements.asSequence().toList()
        }

//        test("parsing with wildcards") {
//            val textInput = "<e1> @<a2> 777 <e5>\n" +
//                    "_ @<a3> _ <e6>\n" +
//                    "_ _ \"Hello\" _\n" +
//                    "<e7> _ _ <e5>\n"
//            val expectedStatements = listOf(
//                Statement(Entity("e1"), Attribute("a2"), IntegerLiteral(777), Entity("e5")),
//                Statement(Entity("e1"), Attribute("a3"), IntegerLiteral(777), Entity("e6")),
//                Statement(Entity("e1"), Attribute("a3"), StringLiteral("Hello"), Entity("e6")),
//                Statement(Entity("e7"), Attribute("a3"), StringLiteral("Hello"), Entity("e5"))
//            )
//            val resStatements = ligParser.parse(textInput)
//            expectedStatements shouldBe resStatements.asSequence().toList()
//        }
    }
}
