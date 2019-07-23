package am.royalbank.uberbear.frameworks.sql

import java.math.BigDecimal
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID
import org.intellij.lang.annotations.Language

fun <R> Connection.sqlQuery(
    @Language("sql") sql: String,
    vararg params: Any?,
    block: (ResultSet) -> R
): R =
    prepareStatement(sql.trimIndent())
        .use {
            it.setParams(params)
                .executeQuery()
                .let(block)
        }

fun Connection.sqlUpdate(
    @Language("sql") sql: String,
    vararg params: Any?
): Int =
    prepareStatement(sql.trimIndent())
        .use { statement ->
            statement
                .setParams(params)
                .executeUpdate()
        }

private fun PreparedStatement.setParams(params: Array<out Any?>): PreparedStatement {
    params.forEachIndexed { index, param ->
        when (param) {
            null -> setString(index + 1, param)
            is String -> setString(index + 1, param)
            is BigDecimal -> setBigDecimal(index + 1, param)
            is UUID -> setObject(index + 1, param)
            else -> throw IllegalStateException("Unhandled sql query param type (${param.javaClass})")
        }
    }
    return this
}
