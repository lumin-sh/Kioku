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
import io.ktor.server.response.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import sh.lumin.apiKeys
import sh.lumin.kioku.db.LogEntry
import sh.lumin.kioku.db.MongoDB
import sh.lumin.kioku.db.StatsResult
import sh.lumin.kioku.utils.APIUtils
import sh.lumin.kioku.utils.Utils

suspend fun handleAllStats(call: ApplicationCall, collectionCache: MutableMap<String, MongoCollection<LogEntry>>) {
    val key = call.request.headers["X-API-Key"] ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "No API key provided!"))
    //
    val allStats = MongoDB.database.listCollectionNames().filter { topic ->
        APIUtils.validateApiKey(key, topic, apiKeys) // Filter out topics where API key is invalid or doesn't have permission for
    }.map { topic ->
        val collection = Utils.getCollectionForTopic(topic, collectionCache)
        val totalCount = collection.countDocuments()
        topic to StatsResult(totalCount)
    }.toList().toMap()
    call.respond(allStats)
}