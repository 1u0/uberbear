package am.royalbank.uberbear.domain.services.transfers

import am.royalbank.uberbear.domain.entities.EUR
import am.royalbank.uberbear.domain.matchers.AccountMatchers
import am.royalbank.uberbear.domain.services.accounts.AccountService
import am.royalbank.uberbear.frameworks.sql.Db
import am.royalbank.uberbear.frameworks.testcontainers.KPostgreSQLContainer
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.present
import java.sql.SQLException
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class TransferServiceIT {
    companion object {
        @Container
        private val postgres = KPostgreSQLContainer()
    }

    private val db = Db.init(postgres.jdbcUrl, postgres.username, postgres.password)
    private val accountService = AccountService(db)
    private val service = TransferService(db)

    @Test fun `transfer from non existing account`() {
        val sourceAccountId = UUID.randomUUID()
        val targetAccountId = UUID.randomUUID()

        assertThrows(SQLException::class.java) {
            service.makeTransfer(
                sourceAccountId,
                targetAccountId,
                12_34.EUR,
                "test transfer"
            )
        }
    }

    @Test fun `transfer to non existing account`() {
        val sourceAccountId = accountService.createAccount("EUR")
        val targetAccountId = UUID.randomUUID()

        assertThat(accountService.getAccountWithBalances(sourceAccountId),
            present(AccountMatchers.accountWithBalance(sourceAccountId, 0.EUR)))

        assertThrows(SQLException::class.java) {
            service.makeTransfer(
                sourceAccountId,
                targetAccountId,
                12_34.EUR,
                "test transfer"
            )
        }

        assertThat(accountService.getAccountWithBalances(sourceAccountId),
            present(AccountMatchers.accountWithBalance(sourceAccountId, 0.EUR)))
    }

    @Test fun `transfer from one account to another`() {
        val sourceAccountId = accountService.createAccount("EUR")
        val targetAccountId = accountService.createAccount("EUR")

        assertThat(accountService.getAccountWithBalances(sourceAccountId),
            present(AccountMatchers.accountWithBalance(sourceAccountId, 0.EUR)))

        assertThat(accountService.getAccountWithBalances(targetAccountId),
            present(AccountMatchers.accountWithBalance(targetAccountId, 0.EUR)))

        service.makeTransfer(
            sourceAccountId,
            targetAccountId,
            12_34.EUR,
            "test transfer"
        )

        assertThat(accountService.getAccountWithBalances(sourceAccountId),
            present(AccountMatchers.accountWithBalance(sourceAccountId, (-12_34).EUR)))

        assertThat(accountService.getAccountWithBalances(targetAccountId),
            present(AccountMatchers.accountWithBalance(targetAccountId, 12_34.EUR)))
    }
}
