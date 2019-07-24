package am.royalbank.uberbear.api.rest.transfers.data

import java.math.BigDecimal

data class MakeTransferRequest(
    val requestId: String,
    val sourceAccountId: String,
    val targetAccountId: String,
    val amount: BigDecimal,
    val currency: String,
    val description: String?
)
