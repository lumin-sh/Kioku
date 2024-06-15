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

import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import sh.lumin.kioku.db.LogEntry
import sh.lumin.kioku.utils.APIUtils
import sh.lumin.kioku.utils.Utils

suspend fun handleLogEntry(call: ApplicationCall, apiKeys: Map<String, APIUtils.ApiKey>, collectionCache: MutableMap<String, MongoCollection<LogEntry>>) {
    val apiKey = call.request.headers["X-API-Key"] ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "No API key provided!"))
    val logEntry = call.receive<LogEntry>()

    if (!APIUtils.validateApiKey(apiKey, logEntry.topic, apiKeys)) {
        return call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You do not have permission to view this topic!"))
    }

    val collection = Utils.getCollectionForTopic(logEntry.topic, collectionCache)
    collection.insertOne(logEntry)
    call.respond(HttpStatusCode.Created)
}