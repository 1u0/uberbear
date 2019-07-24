package am.royalbank.uberbear

import am.royalbank.uberbear.api.rest.ErrorHandler
import am.royalbank.uberbear.api.rest.accounts.AccountResource
import am.royalbank.uberbear.api.rest.accounts.AccountResource.Params.AccountId
import am.royalbank.uberbear.api.rest.transfers.TransferResource
import am.royalbank.uberbear.domain.services.Services
import am.royalbank.uberbear.frameworks.jackson.ObjectMappers
import am.royalbank.uberbear.frameworks.sql.Db
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJackson

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        val jdbcUrl = "jdbc:hsqldb:mem:test;sql.syntax_pgs=true"
        val db = Db.init(jdbcUrl, "", "")
        val services = Services.create(db)

        JavalinJackson.configure(ObjectMappers.createApiObjectMapper())
        Javalin.create()
            .setupApi(services)
            .start(8081)
    }

    private fun Javalin.setupApi(services: Services): Javalin {
        exception(Exception::class.java, ErrorHandler)
        before { it.header("Server", "nginx") }

        run {
            val resource = AccountResource(services.accounts)
            post("/accounts", resource::create)
            get("/accounts/:$AccountId/balances", resource::getAccountBalances)
        }
        run {
            val resource = TransferResource(services.transfers)
            post("/accounts/:$AccountId/transfers", resource::makeTransfer)
        }
        return this
    }
}
