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

package sh.lumin.kioku.utils

import sh.lumin.properties
import java.io.File

object APIUtils {
    data class ApiKey(val key: String, val allowedTopics: Set<String>)

    fun loadApiKeys(filePath: String): Map<String, ApiKey> {
        val apiKeys = File(filePath)
        //
        if(!apiKeys.exists()) {
            apiKeys.createNewFile()
        }
        //
        return apiKeys.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .associate { line ->
                val parts = line.split(":")
                val key = parts[0]
                val topics = parts[1].split(",").toSet()
                key to ApiKey(key, topics)
            }
    }

    fun validateApiKey(apiKey: String, topic: String, apiKeys: Map<String, ApiKey>): Boolean {
        if(properties.getProperty("use_api_keys", "true").toBoolean()) return true
        val apiKeyData = apiKeys[apiKey]
        return apiKeyData != null && apiKeyData.allowedTopics.contains(topic)
    }
}