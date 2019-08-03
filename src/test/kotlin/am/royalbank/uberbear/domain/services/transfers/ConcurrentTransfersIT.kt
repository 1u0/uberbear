package am.royalbank.uberbear.domain.services.transfers

import am.royalbank.uberbear.domain.entities.EUR
import am.royalbank.uberbear.domain.entities.MoneyAmount
import am.royalbank.uberbear.domain.matchers.AccountMatchers
import am.royalbank.uberbear.domain.services.accounts.AccountService
import am.royalbank.uberbear.frameworks.hamkrest.equalByComparingTo
import am.royalbank.uberbear.frameworks.sql.Db
import am.royalbank.uberbear.utils.Threads
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.present
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import org.junit.jupiter.api.Test

class ConcurrentTransfersIT {
    companion object {
        private const val jdbcUrl = "jdbc:hsqldb:mem:test;sql.syntax_pgs=true"
    }

    private val db = Db.init(jdbcUrl, "", "")
    private val accountService = AccountService(db)
    private val transferService = TransferService(db)

    @Test fun `multiple concurrent transfers are correct`() {
        val accountIds = createAccounts(count = 3)
        accountIds.forEach { accountId ->
            assertThat(accountService.getAccountWithBalances(accountId),
                present(AccountMatchers.accountWithBalance(accountId, 0.EUR)))
        }

        val allUpdates = Threads.runInParallel(parallelism = 64) {
            makeRandomTransfers(accountIds, iterations = 1_000)
        }

        val updates = allUpdates.reduce { totalUpdates, updates ->
            totalUpdates.zip(updates).map { it.first + it.second }
        }

        assertThat(updates.reduce { sum, value -> sum + value }, equalByComparingTo(BigDecimal.ZERO))

        accountIds.forEachIndexed { i, accountId ->
            assertThat(accountService.getAccountWithBalances(accountId),
                present(AccountMatchers.accountWithBalance(accountId, MoneyAmount("EUR", updates[i]))))
        }
    }

    private fun createAccounts(count: Int): List<UUID> =
        (1..count)
            .map { accountService.createAccount("EUR") }
            .toList()

    private fun makeRandomTransfers(accountIds: List<UUID>, iterations: Int): List<BigDecimal> {
        val threadId = Thread.currentThread().id
        val accountsCount = accountIds.size
        val random = ThreadLocalRandom.current()
        val updates = (1..accountsCount).map { BigDecimal.ZERO }.toMutableList()

        (1..iterations).forEach {
            val source = random.nextInt(accountsCount)
            var target = random.nextInt(accountsCount - 1)
            if (target >= source) {
                target++
            }
            val amount = random.nextInt(1, 500_00).EUR // zero amount is not allowed to transfer
            transferService.makeTransfer(accountIds[source], accountIds[target], amount, "$threadId:$it")
            updates[source] -= amount.value
            updates[target] += amount.value
        }

        return updates
    }
}
