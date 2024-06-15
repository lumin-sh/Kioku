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

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import sh.lumin.kioku.db.LogEntry

class LogEntryCodec : Codec<LogEntry> {
    override fun encode(writer: BsonWriter, value: LogEntry, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        writer.writeName("timestamp")
        writer.writeDateTime(value.timestamp)
        writer.writeName("topic")
        writer.writeString(value.topic)
        writer.writeName("data")

        when (value.data) {
            is String -> writer.writeString(value.data)
            is Number -> writer.writeDouble(value.data.toDouble())
            is Boolean -> writer.writeBoolean(value.data)
            else -> writer.writeString(value.data.toString()) // Fallback to string representation
        }

        writer.writeEndDocument()
    }

    override fun getEncoderClass(): Class<LogEntry> = LogEntry::class.java
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): LogEntry {
        reader.readStartDocument()

        var timestamp: Long? = null
        var topic: String? = null
        var data: Any? = null

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val fieldName = reader.readName() // Read the field name

             when (fieldName) {
                "timestamp" -> timestamp = reader.readDateTime()
                "topic" -> topic = reader.readString()
                "data" -> {
                    data = when (reader.currentBsonType) {
                        BsonType.STRING -> reader.readString()
                        BsonType.INT32 -> reader.readInt32()
                        BsonType.INT64 -> reader.readInt64()
                        BsonType.DOUBLE -> reader.readDouble()
                        BsonType.BOOLEAN -> reader.readBoolean()
                        else -> {
                            reader.skipValue()
                            null
                        }
                    }
                }
                else -> {
                    reader.skipValue()
                    null
                }
            }
        }
        reader.readEndDocument()
        return LogEntry(timestamp ?: 0L, topic ?: "", data ?: "Unknown")
    }
}