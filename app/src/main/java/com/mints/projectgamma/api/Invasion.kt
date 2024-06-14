package com.mints.projectgamma.api

import com.mints.projectgamma.mapping.DataMappings

data class Invasion(
    val name: String,
    val lat: Double,
    val lng: Double,
    val invasion_start: Long,
    val invasion_end: Long,
    val character: Int,
    val type: Int
) {
    val characterName: String
        get() = DataMappings.characterNamesMap[character] ?: "Unknown Character"

    val typeDescription: String
        get() = DataMappings.typeDescriptionsMap[type] ?: "Unknown Type"
}