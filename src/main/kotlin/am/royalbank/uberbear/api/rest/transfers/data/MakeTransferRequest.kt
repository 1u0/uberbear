package am.royalbank.uberbear.api.rest.transfers.data

import java.math.BigDecimal
import java.util.UUID

data class MakeTransferRequest(
    val targetAccountId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val description: String?
)
