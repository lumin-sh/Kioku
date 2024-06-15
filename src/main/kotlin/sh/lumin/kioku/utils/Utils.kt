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

import com.mongodb.kotlin.client.coroutine.MongoCollection
import sh.lumin.kioku.db.LogEntry
import sh.lumin.kioku.db.MongoDB
import java.io.*
import java.util.Properties

object Utils {
    fun getCollectionForTopic(topic: String, collectionCache: MutableMap<String, MongoCollection<LogEntry>>): MongoCollection<LogEntry> {
        return collectionCache.getOrPut(topic) {
            MongoDB.database.getCollection(topic, LogEntry::class.java)
        }
    }

    fun loadConfig(): Properties {
        val configFileName = "config.properties"
        val config = Properties()

        // 1. Try loading from current directory
        val externalConfigFile = File(configFileName)
        if (externalConfigFile.exists()) {
            try {
                FileInputStream(externalConfigFile).use { fis ->
                    config.load(fis)
                    return config
                }
            } catch (_: IOException) {}
        }

        // 2. Load from JAR resource if not found externally
        Utils::class.java.classLoader.getResourceAsStream(configFileName)?.use { ips ->
            config.load(ips)

            // 3. Save to current directory
            try {
                FileOutputStream(externalConfigFile).use { fos ->
                    config.store(fos, "")
                }
            } catch (_: IOException) {}
        }

        return config
    }
}