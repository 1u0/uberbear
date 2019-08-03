package am.royalbank.uberbear.frameworks.hamkrest

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.describe

internal fun match(comparisonResult: Boolean, describeMismatch: () -> String): MatchResult =
    if (comparisonResult) {
        MatchResult.Match
    } else {
        MatchResult.Mismatch(describeMismatch())
    }

fun <T : Comparable<T>> equalByComparingTo(expected: T?): Matcher<T?> =
    object : Matcher<T?> {
        override fun invoke(actual: T?): MatchResult = when {
            expected != null && actual != null -> match(expected.compareTo(actual) == 0) { "was: ${describe(actual)}" }
            else -> match(expected == null && actual == null) { "was: ${describe(actual)}" }
        }

        override val description: String get() = "is equal to ${describe(expected)}"

        override val negatedDescription: String get() = "is not equal to ${describe(expected)}"
    }
