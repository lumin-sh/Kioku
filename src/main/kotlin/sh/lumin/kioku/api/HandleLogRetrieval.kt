/*
 * Copyright (C) 2024 LuminSh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sh.lumin.kioku.api

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Sorts.descending
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.toList
import sh.lumin.kioku.db.LogEntry
import sh.lumin.kioku.utils.APIUtils
import sh.lumin.kioku.utils.Utils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

suspend fun handleLogRetrieval(call: ApplicationCall, apiKeys: Map<String, APIUtils.ApiKey>, collectionCache: MutableMap<String, MongoCollection<LogEntry>>) {
    val apiKey = call.request.headers["X-API-Key"] ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "No API key provided!"))
    val topic = call.parameters["topic"]!!

    if (!APIUtils.validateApiKey(apiKey, topic, apiKeys)) {
        return call.respond(HttpStatusCode.Forbidden)
    }

    val collection = Utils.getCollectionForTopic(topic, collectionCache)
    val startDateStr = call.request.queryParameters["startDate"]
    val endDateStr = call.request.queryParameters["endDate"]
    val filter = if (startDateStr != null && endDateStr != null) {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val startDate = LocalDateTime.parse(startDateStr, formatter)
        val endDate = LocalDateTime.parse(endDateStr, formatter)
        and(gte("timestamp", startDate), lte("timestamp", endDate))
    } else {
        empty()
    }
    val sort = descending("timestamp")
    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10
    val skip = (page - 1) * pageSize
    val logs = collection.find(filter).sort(sort).skip(skip).limit(pageSize).toList()
    call.respond(logs)
}
