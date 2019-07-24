package am.royalbank.uberbear.domain.services.transfers

import am.royalbank.uberbear.domain.entities.MoneyAmount
import am.royalbank.uberbear.domain.services.accounts.AccountService
import am.royalbank.uberbear.frameworks.sql.Db
import am.royalbank.uberbear.frameworks.testcontainers.KPostgreSQLContainer
import java.math.BigDecimal
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
                MoneyAmount("EUR", BigDecimal("12.34")),
                "test transfer"
            )
        }
    }

    @Test fun `transfer to non existing account`() {
        val sourceAccountId = accountService.createAccount("EUR")
        val targetAccountId = UUID.randomUUID()

        check(accountService.getAccountWithBalances(sourceAccountId)!!.balances[0].value.signum() == 0) { "initial balance is 0" }

        assertThrows(SQLException::class.java) {
            service.makeTransfer(
                sourceAccountId,
                targetAccountId,
                MoneyAmount("EUR", BigDecimal("12.34")),
                "test transfer"
            )
        }

        check(accountService.getAccountWithBalances(sourceAccountId)!!.balances[0].value.signum() == 0) { "balance is unchanged" }
    }

    @Test fun `transfer from one account to another`() {
        val sourceAccountId = accountService.createAccount("EUR")
        val targetAccountId = accountService.createAccount("EUR")

        val account1 = accountService.getAccountWithBalances(sourceAccountId)
        check(account1!!.balances[0].value.signum() == 0) { "initial balance is 0" }
        val account2 = accountService.getAccountWithBalances(targetAccountId)
        check(account2!!.balances[0].value.signum() == 0) { "initial balance is 0" }

        service.makeTransfer(
            sourceAccountId,
            targetAccountId,
            MoneyAmount("EUR", BigDecimal("12.34")),
            "test transfer"
        )

        check(accountService.getAccountWithBalances(sourceAccountId)!!.balances[0].value.signum() < 0) { "balance is unchanged" }
        check(accountService.getAccountWithBalances(targetAccountId)!!.balances[0].value.signum() > 0) { "balance is unchanged" }
    }
}
