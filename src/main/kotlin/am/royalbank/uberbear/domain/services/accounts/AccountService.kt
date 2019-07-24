package am.royalbank.uberbear.domain.services.accounts

import am.royalbank.uberbear.domain.dao.AccountDao
import am.royalbank.uberbear.domain.entities.accounts.AccountWithBalances
import am.royalbank.uberbear.frameworks.sql.Db
import java.math.BigDecimal
import java.util.UUID

class AccountService(
    private val db: Db
) {
    fun createAccount(currency: String): UUID =
        db.connect(autoCommit = false) { connection ->
            val dao = AccountDao(connection)
            val accountId = UUID.randomUUID()
            if (!dao.createAccount(accountId)) {
                throw IllegalStateException("cannot create an account (id=$accountId)")
            }
            if (!dao.createStatement(accountId, UUID.randomUUID(), currency, BigDecimal.ZERO)) {
                throw IllegalArgumentException("cannot create an account statement (id=$accountId)")
            }
            accountId
        }

    fun getAccountWithBalances(accountId: UUID): AccountWithBalances? =
        db.connect { connection ->
            val dao = AccountDao(connection)
            dao.getAccountWithBalances(accountId)
        }
}
