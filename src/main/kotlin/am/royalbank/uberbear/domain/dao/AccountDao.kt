package am.royalbank.uberbear.domain.dao

import am.royalbank.uberbear.domain.entities.TransactionType
import am.royalbank.uberbear.frameworks.sql.sqlQuery
import am.royalbank.uberbear.frameworks.sql.sqlUpdate
import java.math.BigDecimal
import java.sql.Connection
import java.util.UUID

class AccountDao(
    private val connection: Connection
) {
    fun createAccount(accountId: UUID): Boolean =
        connection.sqlUpdate("""
            insert into Account(accountId)
            values (?)
            """,
            accountId
        ) == 1

    fun createTransaction(
        accountId: UUID,
        transactionId: UUID,
        currency: String,
        amount: BigDecimal,
        transactionType: TransactionType,
        description: String?
    ): Boolean =
        connection.sqlUpdate("""
            insert into AccountTransaction(
                accountId,
                accountTransactionId,
                currency,
                amount,
                transactionType,
                description)
            values (?, ?, ?::Currency, ?, ?::AccountTransactionType, ?)
            """,
            accountId,
            transactionId,
            currency,
            amount,
            transactionType.dbName,
            description
        ) == 1

    fun getAccountBalance(accountId: UUID, currency: String): BigDecimal? =
        connection.sqlQuery("""
            with Params(accountId, currency) as (values (?, ?::Currency)),
                Statement(balance, openedAt) as (
                    select openingBalance, openedAt
                    from AccountOpenStatement
                    natural inner join Params
                    order by openedAt desc
                    limit 1)
            select (
                (select coalesce((select balance from Statement), 0.0)) +
                (select coalesce(sum(case when transactionType = 'debit' then amount else -amount end), 0.0)
                from AccountTransaction
                natural inner join Params
                where not exists (select 1 from Statement where openedAt > createdAt))
            ) as total
            """,
            accountId,
            currency
        ) { resultSet ->
            if (resultSet.next()) {
                resultSet.getBigDecimal("total")
            } else {
                null
            }
        }
}
