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

package sh.lumin

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import sh.lumin.kioku.api.configureRouting
import sh.lumin.kioku.db.MongoDB
import sh.lumin.kioku.utils.Utils

val properties = Utils.loadConfig()

fun main() {
    embeddedServer(
        Netty,
        port = properties.getProperty("ktor_port", "8080").toInt(),
        host = properties.getProperty("ktor_host", "0.0.0.0"),
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    //
    MongoDB(properties.getProperty("mongo_uri", "mongodb://localhost:27017/"))
    //
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }
    //
    install(ContentNegotiation) {
        json()
        gson()
    }
    //
    configureRouting()
}
