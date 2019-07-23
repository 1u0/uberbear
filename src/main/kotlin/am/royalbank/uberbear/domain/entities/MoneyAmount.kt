package am.royalbank.uberbear.domain.entities

import java.math.BigDecimal

data class MoneyAmount(
    val currency: String,
    val value: BigDecimal
)
