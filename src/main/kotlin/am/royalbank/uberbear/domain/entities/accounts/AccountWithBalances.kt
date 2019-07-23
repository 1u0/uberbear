package am.royalbank.uberbear.domain.entities.accounts

import am.royalbank.uberbear.domain.entities.MoneyAmount
import java.util.UUID

data class AccountWithBalances(
    val accountId: UUID,
    val balances: List<MoneyAmount>
)
