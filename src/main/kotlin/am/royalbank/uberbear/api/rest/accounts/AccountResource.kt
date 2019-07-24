package am.royalbank.uberbear.api.rest.accounts

import am.royalbank.uberbear.api.rest.accounts.data.CreateAccountRequest
import am.royalbank.uberbear.api.rest.accounts.data.CreateAccountResponse
import am.royalbank.uberbear.domain.services.accounts.AccountService
import am.royalbank.uberbear.frameworks.http.HttpStatusCodes
import io.javalin.http.Context
import java.util.UUID

class AccountResource(private val service: AccountService) {

    companion object Params {
        const val AccountId = "accountId"
    }

    fun create(ctx: Context) {
        val request = ctx.body<CreateAccountRequest>()

        val accountId = service.createAccount(request.currency)

        ctx.status(HttpStatusCodes.ACCEPTED)
            .json(CreateAccountResponse(accountId))
    }

    fun getAccountBalances(ctx: Context) {
        val accountId = UUID.fromString(ctx.pathParam(AccountId))

        val account = service.getAccountWithBalances(accountId)

        if (account == null) {
            ctx.status(HttpStatusCodes.NOT_FOUND)
        } else {
            ctx.status(HttpStatusCodes.OK)
                .json(account)
        }
    }
}
