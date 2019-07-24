package am.royalbank.uberbear.domain.dao

import am.royalbank.uberbear.domain.entities.MoneyAmount
import am.royalbank.uberbear.domain.entities.TransactionType
import am.royalbank.uberbear.domain.entities.accounts.AccountWithBalances
import am.royalbank.uberbear.frameworks.sql.getUUID
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

    fun createStatement(accountId: UUID, statementId: UUID, currency: String, openingBalance: BigDecimal): Boolean =
        connection.sqlUpdate("""
            insert into AccountOpenStatement(
                accountOpenStatementId,
                accountId,
                currency,
                openingBalance)
            values (?, ?, ?::Currency, ?)
            """,
            statementId,
            accountId,
            currency,
            openingBalance
        ) == 1

    fun createTransaction(
        accountId: UUID,
        transactionId: UUID,
        transactionType: TransactionType,
        amount: MoneyAmount,
        description: String?
    ): Boolean =
        // TODO: restrict transaction creation to currencies that explicitly enabled at account level
        connection.sqlUpdate("""
            insert into AccountTransaction(
                accountId,
                accountTransactionId,
                transactionType,
                currency,
                amount,
                description)
            values (?, ?, ?::AccountTransactionType, ?::Currency, ?, ?)
            """,
            accountId,
            transactionId,
            transactionType.dbName,
            amount.currency,
            amount.value,
            description
        ) == 1

    fun getAccountWithBalances(accountId: UUID): AccountWithBalances? =
        connection.sqlQuery("""
            with
                Param(accountId) as (
                    values(?)
                ),
                -- NOTE: we allow to have AccountTransaction independent of having an AccountOpenStatement
                CurrentTransactionTotal as (
                    select
                        accountId,
                        currency,
                        sum(case
                            when transactionType = 'debit' then amount
                            else -amount
                            end) as total
                    from AccountTransaction
                    -- filter out transactions that are closed
                    where not exists (
                        select 1 from AccountOpenStatement as stm
                        where stm.accountId = accountId and stm.currency = currency and openedAt > createdAt)
                    group by accountId, currency
                ),
                Result as (
                    select accountId, currency, coalesce(openingBalance, 0.0) + coalesce(total, 0.0) as balance
                    from AccountOpenStatement
                    full join CurrentTransactionTotal using (accountId, currency)
                )
            
            select * from Param inner join Result using (accountId)
            """,
            accountId
        ) { resultSet ->
            val balances = ArrayList<MoneyAmount>()
            while (resultSet.next()) {
                check(accountId == resultSet.getUUID("accountId")) { "accountId mismatch" }
                balances.add(MoneyAmount(resultSet.getString("currency"), resultSet.getBigDecimal("balance")))
            }

            // TODO: for existing account, without any statements and transactions return an empty balances instead
            if (balances.isEmpty()) {
                null
            } else {
                AccountWithBalances(accountId, balances)
            }
        }
}
