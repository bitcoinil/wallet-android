/**
* Auth API
* Auth API<br> <a href='/changelog'>Changelog</a>
*
* The version of the OpenAPI document: v0.0.50
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package com.mycelium.bequant.remote.client.models



import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 * @param address1 Required address string
 * @param address2 Optional address string
 * @param addressConfirmed 
 * @param addressCount 
 * @param birthday YYYY-MM-DD format
 * @param city User's city
 * @param country ISO 3166 country format
 * @param email User's email
 * @param firstName User's first name
 * @param hasDocumentAddress 
 * @param hasDocumentIdentity 
 * @param hasDocumentSelfie 
 * @param identityConfirmed 
 * @param identityCount 
 * @param identityType 
 * @param lastName User's last name
 * @param lastUndeletableIdentity 
 * @param lastUndeletableSelfie 
 * @param lastUndeletableUtilityBill 
 * @param mobileConfirmed 
 * @param mobilePhone 
 * @param mobilePhoneCountryCode 
 * @param nationality User's country of origin
 * @param poaVisible 
 * @param selfieConfirmed 
 * @param selfieCount 
 * @param state 
 * @param taxId 
 * @param zip User's ZIP code
 */
data class KycUser (
    @JsonProperty("address_1")
    val address1: kotlin.String,
    @JsonProperty("birthday")
    val birthday: kotlin.String,
    @JsonProperty("city")
    val city: kotlin.String,
    @JsonProperty("country")
    val country: kotlin.String,
    @JsonProperty("email")
    val email: kotlin.String,
    @JsonProperty("first_name")
    val firstName: kotlin.String,
    @JsonProperty("last_name")
    val lastName: kotlin.String,
    @JsonProperty("nationality")
    val nationality: kotlin.String,
    @JsonProperty("zip")
    val zip: kotlin.String,
    @JsonProperty("address_2")
    val address2: kotlin.String? = null,
    @JsonProperty("address_confirmed")
    val addressConfirmed: kotlin.Boolean? = null,
    @JsonProperty("address_count")
    val addressCount: kotlin.Int? = null,
    @JsonProperty("has_document_address")
    val hasDocumentAddress: kotlin.Boolean? = null,
    @JsonProperty("has_document_identity")
    val hasDocumentIdentity: kotlin.Boolean? = null,
    @JsonProperty("has_document_selfie")
    val hasDocumentSelfie: kotlin.Boolean? = null,
    @JsonProperty("identity_confirmed")
    val identityConfirmed: kotlin.Boolean? = null,
    @JsonProperty("identity_count")
    val identityCount: kotlin.Int? = null,
    @JsonProperty("identity_type")
    val identityType: kotlin.String? = null,
    @JsonProperty("last_undeletable_identity")
    val lastUndeletableIdentity: kotlin.Int? = null,
    @JsonProperty("last_undeletable_selfie")
    val lastUndeletableSelfie: kotlin.Int? = null,
    @JsonProperty("last_undeletable_utility_bill")
    val lastUndeletableUtilityBill: kotlin.Int? = null,
    @JsonProperty("mobile_confirmed")
    val mobileConfirmed: kotlin.Boolean? = null,
    @JsonProperty("mobile_phone")
    val mobilePhone: kotlin.String? = null,
    @JsonProperty("mobile_phone_country_code")
    val mobilePhoneCountryCode: kotlin.String? = null,
    @JsonProperty("poa_visible")
    val poaVisible: kotlin.Boolean? = null,
    @JsonProperty("selfie_confirmed")
    val selfieConfirmed: kotlin.Boolean? = null,
    @JsonProperty("selfie_count")
    val selfieCount: kotlin.Int? = null,
    @JsonProperty("state")
    val state: kotlin.String? = null,
    @JsonProperty("tax_id")
    val taxId: kotlin.String? = null
)

