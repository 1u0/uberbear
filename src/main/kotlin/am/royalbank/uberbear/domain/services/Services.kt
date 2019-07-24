package am.royalbank.uberbear.domain.services

import am.royalbank.uberbear.domain.services.accounts.AccountService
import am.royalbank.uberbear.domain.services.transfers.TransferService
import am.royalbank.uberbear.frameworks.sql.Db

data class Services(
    val accounts: AccountService,
    val transfers: TransferService
) {
    companion object {
        fun create(db: Db): Services {
            return Services(
                AccountService(db),
                TransferService(db)
            )
        }
    }
}
