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


import com.squareup.moshi.Json

/**
 *
 * @param currency currency code
 * @param available available balance
 * @param reserved reserved balance
 */

data class SubAccountBalanceMain(
        /* currency code */
        @Json(name = "currency")
        val currency: kotlin.String? = null,
        /* available balance */
        @Json(name = "available")
        val available: kotlin.String? = null,
        /* reserved balance */
        @Json(name = "reserved")
        val reserved: kotlin.String? = null
)

