package am.royalbank.uberbear.domain.entities

enum class TransactionType {
    Debit,
    Credit;

    val dbName: String
        get() = name.toLowerCase()
}
