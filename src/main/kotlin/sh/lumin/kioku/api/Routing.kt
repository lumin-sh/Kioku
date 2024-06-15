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
import io.ktor.server.application.*
import io.ktor.server.routing.*
import sh.lumin.apiKeys
import sh.lumin.kioku.db.LogEntry


fun Application.configureRouting() {
    val collectionCache = mutableMapOf<String, MongoCollection<LogEntry>>()

    routing {
        post("/telemetry") { handleLogEntry(call, apiKeys, collectionCache) }
        get("/telemetry/{topic}") { handleLogRetrieval(call, apiKeys, collectionCache) }
        get("/telemetry/stats/{topic}") { handleTopicStats(call, apiKeys, collectionCache) }
        get("/telemetry/stats") { handleAllStats(call, collectionCache) }
    }
}
