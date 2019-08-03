package am.royalbank.uberbear.domain.services.transfers

import am.royalbank.uberbear.domain.entities.EUR
import am.royalbank.uberbear.frameworks.sql.Db
import io.mockk.mockk
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TransferServiceTest {
    private val db = mockk<Db>()
    private val service = TransferService(db)

    @Test fun `transferring negative amount is not allowed`() {
        val sourceAccountId = UUID.randomUUID()
        val targetAccountId = UUID.randomUUID()

        val random = ThreadLocalRandom.current()
        val amount = random.nextInt(-500_00, 0).EUR

        assertThrows<IllegalArgumentException> {
            service.makeTransfer(sourceAccountId, targetAccountId, amount, "Counter transfer")
        }
    }

    @Test fun `transferring zero amount is not allowed`() {
        val sourceAccountId = UUID.randomUUID()
        val targetAccountId = UUID.randomUUID()

        assertThrows<IllegalArgumentException> {
            service.makeTransfer(sourceAccountId, targetAccountId, 0.EUR, "Send messages for free!")
        }
    }

    @Test fun `transferring to self is not allowed`() {
        val sourceAccountId = UUID.randomUUID()

        assertThrows<IllegalArgumentException> {
            service.makeTransfer(sourceAccountId, sourceAccountId, 12_34.EUR, "No one writes to the Colonel")
        }
    }
}
