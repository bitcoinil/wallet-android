package com.mycelium.wallet.activity.main.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.mycelium.wallet.MbwManager
import com.mycelium.wallet.R
import com.mycelium.wallet.Utils
import com.mycelium.wallet.activity.util.toStringWithUnit
import com.mycelium.wapi.api.lib.CurrencyCode
import com.mycelium.wapi.wallet.coins.AssetInfo
import com.mycelium.wapi.wallet.coins.COINS
import com.mycelium.wapi.wallet.coins.CryptoCurrency
import com.mycelium.wapi.wallet.coins.Value
import com.mycelium.wapi.wallet.fio.FioGroup
import com.mycelium.wapi.wallet.fio.FioRequestStatus
import fiofoundation.io.fiosdk.models.fionetworkprovider.FIORequestContent
import fiofoundation.io.fiosdk.models.fionetworkprovider.SentFIORequestContent
import org.w3c.dom.Text
import java.math.BigDecimal
import java.math.BigInteger
import java.text.SimpleDateFormat


class FioRequestArrayAdapter(var activity: Activity,
                             private val groups: List<FioGroup>,
                             val mbwManager: MbwManager) : BaseExpandableListAdapter() {

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return groups[groupPosition].children[childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return 0
    }

    val inDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val outDate = SimpleDateFormat("dd/MM/yyyy")

    override fun getChildView(groupPosition: Int, childPosition: Int,
                              isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        val fioRequestContent = getChild(groupPosition, childPosition) as FIORequestContent
        val group = getGroup(groupPosition)

        val fioRequestView = convertView
                ?: activity.layoutInflater.inflate(R.layout.fio_request_row, null)!!

        val content = fioRequestContent.deserializedContent

        val isIncoming = when (group.status) {
            FioGroup.Type.SENT -> true
            FioGroup.Type.PENDING -> false
        }

        val color = if (isIncoming) R.color.green else R.color.red
        val direction = fioRequestView.findViewById<TextView>(R.id.tvDirection)
        direction?.text = if (isIncoming) "From:" else "To:"
        val address = fioRequestView.findViewById<TextView>(R.id.tvAddress)
        address?.text = fioRequestContent.payeeFioAddress

        var hasStatus = false;
        val tvStatus = fioRequestView.findViewById<TextView>(R.id.tvStatus)
        if (getGroup(groupPosition).status == FioGroup.Type.SENT) {
            val status = FioRequestStatus.getStatus((fioRequestContent as SentFIORequestContent).status)
            if (status != FioRequestStatus.NONE) {
                val color = if (status == FioRequestStatus.RECEIVED) R.color.green else R.color.fio_request_pending
                tvStatus.text = status.status.capitalize()
                tvStatus.setTextColor(ContextCompat.getColor(activity, color))
                hasStatus = true
            }
        } else {
            tvStatus.text = ""
        }

        val tvDate = fioRequestView.findViewById<TextView>(R.id.tvDate)

        val date = inDate.parse(fioRequestContent.timeStamp)
        tvDate.text = """${outDate.format(date)}${if (hasStatus) ", " else ""}"""

        val ivStatus = fioRequestView.findViewById<ImageView>(R.id.ivStatus)


        val memo = fioRequestView.findViewById<TextView>(R.id.tvTransactionLabel)
        memo?.text = content?.memo
        val amount = fioRequestView.findViewById<TextView>(R.id.tvAmount)
        val requestedCurrency = COINS.values.firstOrNull {
            it.symbol.equals(content?.chainCode ?: "", true)
        }
                ?: return fioRequestView
        val amountValue = Value.valueOf(requestedCurrency, strToBigInteger(requestedCurrency, content!!.amount))
        amount?.text = amountValue.toStringWithUnit()
        amount?.setTextColor(ContextCompat.getColor(activity, color))
        val convert = convert(amountValue, Utils.getTypeByName(CurrencyCode.USD.shortString)!!)
        val tvFiatAmount = fioRequestView.findViewById<TextView>(R.id.tvFiatAmount)
        tvFiatAmount?.text = convert?.toStringWithUnit()

        return fioRequestView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return groups[groupPosition].children.size
    }

    override fun getGroup(groupPosition: Int): FioGroup {
        return groups[groupPosition]
    }

    override fun getGroupCount(): Int {
        return groups.size
    }

    override fun onGroupCollapsed(groupPosition: Int) {
        super.onGroupCollapsed(groupPosition)
    }

    override fun onGroupExpanded(groupPosition: Int) {
        super.onGroupExpanded(groupPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return 0
    }

    @SuppressLint("SetTextI18n")
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean,
                              convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val group = getGroup(groupPosition) as FioGroup

        if (convertView == null) {
            val inflater = activity.layoutInflater
            convertView = inflater.inflate(R.layout.fio_request_listrow_group, null)

            convertView?.setOnClickListener {
                val expandableListView = convertView?.getParent() as ExpandableListView
                if (!expandableListView.isGroupExpanded(groupPosition)) {
                    expandableListView.expandGroup(groupPosition)
                } else {
                    expandableListView.collapseGroup(groupPosition)
                }
            }
        }
        val arrow = convertView?.findViewById<CheckBox>(R.id.cbArrow)
        val checkedTextView = convertView?.findViewById(R.id.textView1) as TextView
        checkedTextView.text = """${group.status} (${group.children.size})"""
        arrow?.isChecked = isExpanded
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }


    private fun convert(value: Value, assetInfo: AssetInfo): Value? {
        return mbwManager.exchangeRateManager.get(value, assetInfo)
    }

    private fun strToBigInteger(coinType: CryptoCurrency, amountStr: String): BigInteger =
            BigDecimal(amountStr).movePointRight(coinType.unitExponent).toBigIntegerExact()
}