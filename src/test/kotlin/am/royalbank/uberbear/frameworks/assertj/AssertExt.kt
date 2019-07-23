package am.royalbank.uberbear.frameworks.assertj

import org.assertj.core.api.AbstractAssert

// A helper method to assert that the actual object is not null and hint Kotlin compiler.
fun <Self : AbstractAssert<Self, Actual?>, Actual> AbstractAssert<Self, Actual?>.notNull(): AbstractAssert<Self, Actual> =
    this.isNotNull as AbstractAssert<Self, Actual>
