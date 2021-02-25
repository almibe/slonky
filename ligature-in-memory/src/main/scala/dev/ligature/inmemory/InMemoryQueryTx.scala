/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.inmemory

import dev.ligature._
import monix.eval.Task
import monix.reactive.Observable

/** Represents a QueryTx within the context of a Ligature instance and a single Dataset */
class InMemoryQueryTx(private val store: DatasetStore) extends QueryTx {
  /** Returns all PersistedStatements in this Dataset. */
  def allStatements(): Observable[Either[LigatureError, PersistedStatement]] = {
    Observable.fromIterable(store.statements.map(Right(_)))
  }

  /** Returns all PersistedStatements that match the given criteria.
   * If a parameter is None then it matches all, so passing all Nones is the same as calling allStatements. */
  def matchStatements(
                       entity: Option[Entity],
                       attribute: Option[Attribute],
                       value: Option[Value],
                     ): Observable[Either[LigatureError, PersistedStatement]] = {
    var res = Observable.fromIterable(store.statements)
    if (entity.isDefined) {
      res = res.filter(_.statement.entity == entity.get)
    }
    if (attribute.isDefined) {
      res = res.filter(_.statement.attribute == attribute.get)
    }
    if (value.isDefined) {
      res = res.filter(_.statement.value == value.get)
    }
    res.map(Right(_))
  }

  /** Retuns all PersistedStatements that match the given criteria.
   * If a parameter is None then it matches all. */
  def matchStatementsRange(
                            entity: Option[Entity],
                            attribute: Option[Attribute],
                            range: dev.ligature.Range,
                          ): Observable[Either[LigatureError, PersistedStatement]] = {
    var res = Observable.fromIterable(store.statements)
    if (entity.isDefined) {
      res = res.filter(_.statement.entity == entity.get)
    }
    if (attribute.isDefined) {
      res = res.filter(_.statement.attribute == attribute.get)
    }
    res = res.filter { ps =>
      val testValue = ps.statement.value
      (testValue, range) match {
        case (StringLiteral(v), StringLiteralRange(start, end))     => v >= start && v < end
        case (FloatLiteral(v), FloatLiteralRange(start, end))       => v >= start && v < end
        case (IntergerLiteral(v), IntergerLiteralRange(start, end)) => v >= start && v < end
        case _                                                      => false
      }
    }
    res.map(Right(_))
  }

  /** Returns the PersistedStatement for the given context. */
  def statementForContext(context: Entity): Task[Either[LigatureError, Option[PersistedStatement]]] = Task {
    Right(store.statements.find(_.context == context))
  }
}
