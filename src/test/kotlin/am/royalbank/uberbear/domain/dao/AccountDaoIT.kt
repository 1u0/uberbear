package am.royalbank.uberbear.domain.dao

import am.royalbank.uberbear.domain.entities.EUR
import am.royalbank.uberbear.domain.entities.TransactionType
import am.royalbank.uberbear.domain.matchers.AccountMatchers.accountWithBalance
import am.royalbank.uberbear.frameworks.sql.Db
import am.royalbank.uberbear.frameworks.testcontainers.KPostgreSQLContainer
import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.present
import java.math.BigDecimal
import java.util.UUID
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class AccountDaoIT {

    companion object {
        @Container
        private val dbContainer = KPostgreSQLContainer()
    }

    private val db = Db.init(dbContainer.jdbcUrl, dbContainer.username, dbContainer.password)

    @Test fun `get account balance returns null for not existing account`() {
        db.makeConnection { connection ->
            val accountDao = AccountDao(connection)
            val accountId = UUID.randomUUID()

            assertThat(accountDao.getAccountWithBalances(accountId), absent())
        }
    }

    @Test fun `get account balance returns null for existing account without open statements and open transactions`() {
        db.makeConnection { connection ->
            val accountDao = AccountDao(connection)
            val accountId = UUID.randomUUID()
            check(accountDao.createAccount(accountId)) { "create account" }

            assertThat(accountDao.getAccountWithBalances(accountId), absent())
        }
    }

    @Test fun `get account balance with statement and transactions`() {
        db.makeConnection { connection ->
            val accountDao = AccountDao(connection)
            val accountId = UUID.randomUUID()
            check(accountDao.createAccount(accountId)) { "create account" }
            check(
                accountDao.createStatement(accountId, UUID.randomUUID(), "EUR", BigDecimal.ZERO)
            ) { "create EUR account statement" }

            assertThat(accountDao.getAccountWithBalances(accountId),
                present(accountWithBalance(accountId, 0.EUR)))

            check(
                accountDao.createTransaction(
                    accountId,
                    UUID.randomUUID(),
                    TransactionType.Debit,
                    12_34.EUR,
                    "Gift voucher from family")
            ) { "make a EUR debit transaction" }

            assertThat(accountDao.getAccountWithBalances(accountId),
                present(accountWithBalance(accountId, 12_34.EUR)))

            check(
                accountDao.createTransaction(
                    accountId,
                    UUID.randomUUID(),
                    TransactionType.Credit,
                    1_23.EUR,
                    "Post service")
            ) { "make a EUR credit transaction" }

            assertThat(accountDao.getAccountWithBalances(accountId),
                present(accountWithBalance(accountId, 11_11.EUR)))
        }
    }
}
