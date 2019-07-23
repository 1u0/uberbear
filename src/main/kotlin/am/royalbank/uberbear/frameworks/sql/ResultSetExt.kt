package am.royalbank.uberbear.frameworks.sql

import java.sql.ResultSet
import java.util.UUID

inline fun ResultSet.getUUID(columnLabel: String): UUID =
    this.getObject(columnLabel, UUID::class.java)
