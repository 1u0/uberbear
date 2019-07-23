package am.royalbank.uberbear.domain.dao

import am.royalbank.uberbear.domain.entities.MoneyAmount
import am.royalbank.uberbear.domain.entities.TransactionType
import am.royalbank.uberbear.frameworks.assertj.notNull
import am.royalbank.uberbear.frameworks.sql.Db
import am.royalbank.uberbear.frameworks.testcontainers.KPostgreSQLContainer
import java.math.BigDecimal
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
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

            assertThat(accountDao.getAccountWithBalances(accountId)).isNull()
        }
    }

    @Test fun `get account balance returns null for existing account without open statements and open transactions`() {
        db.makeConnection { connection ->
            val accountDao = AccountDao(connection)
            val accountId = UUID.randomUUID()
            check(accountDao.createAccount(accountId)) { "create account" }

            assertThat(accountDao.getAccountWithBalances(accountId)).isNull()
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

            assertThat(accountDao.getAccountWithBalances(accountId)).notNull()
                .satisfies { accountBalances ->
                    assertThat(accountBalances.accountId).isEqualTo(accountId)
                    assertThat(accountBalances.balances)
                        .hasSize(1)
                        .satisfies {
                            hasAmount(it, MoneyAmount("EUR", BigDecimal.ZERO))
                        }
                }

            check(
                accountDao.createTransaction(
                    accountId,
                    UUID.randomUUID(),
                    TransactionType.Debit,
                    MoneyAmount("EUR", BigDecimal("12.34")),
                    "Gift voucher from family")
            ) { "make a EUR debit transaction" }

            assertThat(accountDao.getAccountWithBalances(accountId)).notNull()
                .satisfies { accountBalances ->
                    assertThat(accountBalances.accountId).isEqualTo(accountId)
                    assertThat(accountBalances.balances)
                        .hasSize(1)
                        .satisfies {
                            hasAmount(it, MoneyAmount("EUR", BigDecimal("12.34")))
                        }
                }

            check(
                accountDao.createTransaction(
                    accountId,
                    UUID.randomUUID(),
                    TransactionType.Credit,
                    MoneyAmount("EUR", BigDecimal("1.23")),
                    "Post service")
            ) { "make a EUR credit transaction" }

            assertThat(accountDao.getAccountWithBalances(accountId)).notNull()
                .satisfies { accountBalances ->
                    assertThat(accountBalances.accountId).isEqualTo(accountId)
                    assertThat(accountBalances.balances)
                        .hasSize(1)
                        .satisfies {
                            hasAmount(it, MoneyAmount("EUR", BigDecimal("11.11")))
                        }
                }
        }
    }

    private fun hasAmount(balances: List<MoneyAmount>, expectedBalance: MoneyAmount) {
        assertThat(balances.find { it.currency == expectedBalance.currency })
            .notNull()
            .satisfies {
                assertThat(it.value).isEqualByComparingTo(expectedBalance.value)
            }
    }
}
