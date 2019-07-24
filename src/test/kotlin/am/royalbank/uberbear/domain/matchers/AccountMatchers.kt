package am.royalbank.uberbear.domain.matchers

import am.royalbank.uberbear.domain.entities.MoneyAmount
import am.royalbank.uberbear.domain.entities.accounts.AccountWithBalances
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.allOf
import com.natpryce.hamkrest.anyElement
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.hasSize
import java.util.UUID

object AccountMatchers {

    fun accountWithBalance(accountId: UUID, balance: MoneyAmount): Matcher<AccountWithBalances> =
        allOf(
            has(AccountWithBalances::accountId, equalTo(accountId)),
            has(AccountWithBalances::balances, allOf(
                hasSize(equalTo(1)),
                anyElement(MoneyMatchers.amountEquals(balance)))))
}
