package am.royalbank.uberbear.api.rest

import am.royalbank.uberbear.frameworks.http.HttpStatusCodes
import io.javalin.http.Context
import io.javalin.http.ExceptionHandler
import org.slf4j.LoggerFactory

object ErrorHandler : ExceptionHandler<Exception> {
    private val LOG = LoggerFactory.getLogger(ErrorHandler::class.java)

    override fun handle(exception: Exception, ctx: Context) {
        val (status, message) = when (exception) {
            is IllegalArgumentException -> HttpStatusCodes.BAD_REQUEST to (exception.message ?: "unknown")
            else -> {
                LOG.warn("Unhandled request exception", exception)
                HttpStatusCodes.INTERNAL_SERVER_ERROR to "Internal Server Error"
            }
        }
        ctx.status(status)
            .json(ErrorResponse(message))
    }

    data class ErrorResponse(
        val message: String
    )
}
