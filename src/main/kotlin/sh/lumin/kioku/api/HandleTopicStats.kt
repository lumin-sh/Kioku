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

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.firstOrNull
import sh.lumin.kioku.db.LogEntry
import sh.lumin.kioku.db.StatsResult
import sh.lumin.kioku.utils.APIUtils
import sh.lumin.kioku.utils.Utils

suspend fun handleTopicStats(call: ApplicationCall, apiKeys: Map<String, APIUtils.ApiKey>, collectionCache: MutableMap<String, MongoCollection<LogEntry>>) {
    val apiKey = call.request.headers["X-API-Key"] ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "No API key provided!"))
    val topic = call.parameters["topic"]!!

    if (!APIUtils.validateApiKey(apiKey, topic, apiKeys)) {
        return call.respond(HttpStatusCode.Forbidden)
    }

    val collection = Utils.getCollectionForTopic(topic, collectionCache)

    val aggregationResult = collection.aggregate<StatsResult>(
        listOf(
            Aggregates.group(null, Accumulators.sum("count", 1)),
            Aggregates.project(
                Projections.fields(
                    Projections.excludeId(),
                    Projections.computed("totalCount", "\$count")
                )
            )
        )
    ).firstOrNull() ?: StatsResult(0)

    call.respond(aggregationResult)
}