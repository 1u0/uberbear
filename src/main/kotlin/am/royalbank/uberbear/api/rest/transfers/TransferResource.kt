package am.royalbank.uberbear.api.rest.transfers

import am.royalbank.uberbear.api.rest.accounts.AccountResource
import am.royalbank.uberbear.api.rest.transfers.data.MakeTransferRequest
import am.royalbank.uberbear.api.rest.transfers.data.MakeTransferResponse
import am.royalbank.uberbear.domain.entities.MoneyAmount
import am.royalbank.uberbear.domain.services.transfers.TransferService
import am.royalbank.uberbear.frameworks.http.HttpStatusCodes
import io.javalin.http.Context
import java.util.UUID

class TransferResource(
    private val service: TransferService
) {

    fun makeTransfer(ctx: Context) {
        val accountId = UUID.fromString(ctx.pathParam(AccountResource.AccountId))
        val request = ctx.body<MakeTransferRequest>()

        service.makeTransfer(
            accountId,
            request.targetAccountId,
            MoneyAmount(request.currency, request.amount),
            request.description
        )

        ctx.status(HttpStatusCodes.ACCEPTED)
            .json(
                MakeTransferResponse(
                    status = "OK"
                )
            )
    }
}
