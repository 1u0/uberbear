package am.royalbank.uberbear.domain.services.transfers

import am.royalbank.uberbear.domain.entities.EUR
import am.royalbank.uberbear.domain.entities.MoneyAmount
import am.royalbank.uberbear.domain.matchers.AccountMatchers
import am.royalbank.uberbear.domain.matchers.MoneyMatchers
import am.royalbank.uberbear.domain.services.accounts.AccountService
import am.royalbank.uberbear.frameworks.sql.Db
import am.royalbank.uberbear.utils.Threads
import com.natpryce.hamkrest.allOf
import com.natpryce.hamkrest.anyElement
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
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

        val threads = Threads.runSimultaneously(count = 64) {
            runWorker(accountIds, iterations = 1_000)
        }
        Threads.awaitAll(threads)

        assertThat(getTotalBalance(accountIds), allOf(
            hasSize(equalTo(1)),
            anyElement(MoneyMatchers.amountEquals(0.EUR))))
    }

    private fun createAccounts(count: Int): List<UUID> =
        (1..count)
            .map { accountService.createAccount("EUR") }
            .toList()

    private fun runWorker(accountIds: List<UUID>, iterations: Int) {
        val threadId = Thread.currentThread().id
        val accountsCount = accountIds.size
        val random = ThreadLocalRandom.current()
        (1..iterations).forEach {
            val sourceAccountId = accountIds[random.nextInt(accountsCount)]
            val targetAccountId = accountIds[random.nextInt(accountsCount)]
            val amount = random.nextInt(1, 500_00).EUR // zero amount is not allowed to transfer
            transferService.makeTransfer(sourceAccountId, targetAccountId, amount, "$threadId:$it")
        }
    }

    private fun getTotalBalance(accountIds: List<UUID>): List<MoneyAmount> =
        accountIds.asSequence()
            .map { accountService.getAccountWithBalances(it) }
            .onEach(::println) // for debugging
            .flatMap { it!!.balances.asSequence() }
            .groupBy { it.currency }
            .map { MoneyAmount(it.key, it.value.map(MoneyAmount::value).reduce(BigDecimal::add)) }
}
