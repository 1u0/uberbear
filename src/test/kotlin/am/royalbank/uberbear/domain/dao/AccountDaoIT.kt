package am.royalbank.uberbear.domain.dao

import am.royalbank.uberbear.domain.entities.TransactionType
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

    @Test fun `test account create transaction and balance updates`() {
        db.makeConnection { connection ->
            val accountDao = AccountDao(connection)

            val accountId = UUID.randomUUID()

            assertThat(accountDao.createAccount(accountId)).isTrue()

            assertThat(accountDao.getAccountBalance(accountId, "EUR"))
                .isEqualByComparingTo(BigDecimal.ZERO)

            assertThat(accountDao.createTransaction(
                accountId,
                UUID.randomUUID(),
                "EUR",
                BigDecimal("12.34"),
                TransactionType.Debit,
                "Gift voucher from friends"
            )).isTrue()

            assertThat(accountDao.getAccountBalance(accountId, "EUR"))
                .isEqualByComparingTo(BigDecimal("12.34"))

            assertThat(accountDao.createTransaction(
                accountId,
                UUID.randomUUID(),
                "EUR",
                BigDecimal("1.23"),
                TransactionType.Credit,
                "Post service"
            )).isTrue()

            assertThat(accountDao.getAccountBalance(accountId, "EUR"))
                .isEqualByComparingTo(BigDecimal("11.11"))
        }
    }
}
