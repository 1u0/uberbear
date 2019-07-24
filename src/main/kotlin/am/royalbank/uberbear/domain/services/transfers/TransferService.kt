package am.royalbank.uberbear.domain.services.transfers

import am.royalbank.uberbear.domain.dao.AccountDao
import am.royalbank.uberbear.domain.entities.MoneyAmount
import am.royalbank.uberbear.domain.entities.TransactionType
import am.royalbank.uberbear.frameworks.sql.Db
import java.util.UUID

class TransferService(
    private val db: Db
) {
    fun makeTransfer(sourceAccountId: UUID, targetAccountId: UUID, amount: MoneyAmount, description: String?) {
        // For simplicity, transfers are supported only for the same database.
        // TODO: make proper distributed transfer, assuming that accounts may be on different services.

        require(amount.value.signum() > 0) { "transferred amount must be positive" }
        db.connect(autoCommit = false) { connection ->
            val dao = AccountDao(connection)

            if (!dao.createTransaction(
                sourceAccountId,
                UUID.randomUUID(),
                TransactionType.Credit,
                amount,
                description)) {
                throw IllegalStateException("couldn't withdraw from source account")
            }
            if (!dao.createTransaction(
                    targetAccountId,
                    UUID.randomUUID(),
                    TransactionType.Debit,
                    amount,
                    description)) {
                throw IllegalStateException("couldn't deposit to target account")
            }
        }
    }
}
