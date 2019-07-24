package am.royalbank.uberbear.domain.matchers

import am.royalbank.uberbear.domain.entities.MoneyAmount
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher

object MoneyMatchers {

    fun amountEquals(expected: MoneyAmount): Matcher<MoneyAmount> {
        return object : Matcher.Primitive<MoneyAmount>() {
            override fun invoke(actual: MoneyAmount): MatchResult = when {
                expected.currency != actual.currency ->
                    MatchResult.Mismatch("currency mismatch: expected=${expected.currency}, actual=${actual.currency}")
                expected.value.compareTo(actual.value) != 0 ->
                    MatchResult.Mismatch("value mismatch: expected=${expected.value}, actual=${actual.value}")
                else -> MatchResult.Match
            }

            override val description: String
                get() = "match $expected"
        }
    }
}
