package com.mycelium.wapi.wallet.fio.coins

import com.mycelium.wapi.wallet.Address
import com.mycelium.wapi.wallet.coins.families.EOSBasedCryptoCurrency

object FIOTest : EOSBasedCryptoCurrency() {
    override fun parseAddress(addressString: String): Address {
        TODO()
    }
    const val url ="http://testnet.fioprotocol.io/v1/"
    init {
        id = "fio.test"
        name = "FIO Test"
        symbol = "tFIO"
        unitExponent = 8
    }
}