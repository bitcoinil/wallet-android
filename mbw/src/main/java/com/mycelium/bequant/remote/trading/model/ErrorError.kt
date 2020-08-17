/**
 * API
 * Create API keys in your profile and use public API key as username and secret as password to authorize.
 *
 * The version of the OpenAPI document: 2.19.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.mycelium.bequant.remote.trading.model


import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param code
 * @param message
 * @param description
 */

data class ErrorError(
        @JsonProperty("code")
        val code: String,
        @JsonProperty("message")
        val message: String?,
        @JsonProperty("description")
        val description: String? = null
)