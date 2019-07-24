package am.royalbank.uberbear.domain.entities

import java.math.BigDecimal

val Int.EUR: MoneyAmount
    get() = MoneyAmount("EUR", BigDecimal.valueOf(this.toLong(), 2))
