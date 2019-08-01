package com.example.geots

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_transform.*
import kotlinx.android.synthetic.main.activity_transform_row.view.*


open class TypedTransformActivity(private val csType: CSType) : AppCompatActivity() {
    companion object {
        private val re = Regex("geo:(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?)(.*)")
    }

    var lat: Double? = null
    var lng: Double? = null
    var rst: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transform)
        if (intent == null) {
            // silly
            Toast.makeText(this, "Empty intent", Toast.LENGTH_SHORT).show()
            this.finish()
            return
        }
        val matchResult = re.find(intent.data.toString())
        if (matchResult == null || matchResult.groupValues.size != 4) {
            // kinda silly but weird
            Toast.makeText(this, "Empty intent", Toast.LENGTH_SHORT).show()
            this.finish()
            return
        }
        lat = matchResult.groupValues[1].toDouble()
        lng = matchResult.groupValues[2].toDouble()
        rst = matchResult.groupValues[3]
        // silly check
        if (rst.isNullOrBlank()) rst = ""

        var wgs84 = DoubleArray(2)
        var gcj02 = DoubleArray(2)
        var bd09 = DoubleArray(2)

        when (csType) {
            CSType.WGS84 -> wgs84 = arrayOf(lat!!, lng!!).toDoubleArray()
            CSType.GCJ02 -> gcj02 = arrayOf(lat!!, lng!!).toDoubleArray()
            CSType.BD09 -> bd09 = arrayOf(lat!!, lng!!).toDoubleArray()
        }

        when (csType) {
            CSType.WGS84 -> {
                gcj02 = Transformers.wgs84toGcj02(lat!!, lng!!)
                bd09 = Transformers.wgs84toBd09(lat!!, lng!!)
            }
            CSType.GCJ02 -> {
                wgs84 = Transformers.gcj02toWgs84(lat!!, lng!!)
                bd09 = Transformers.gcj02toBd09(lat!!, lng!!)
            }
            CSType.BD09 -> {
                gcj02 = Transformers.bd09toGcj02(lat!!, lng!!)
                wgs84 = Transformers.bd09toWgs84(lat!!, lng!!)
            }
        }

        // just like adding new elements to DOM
        // use RecyclerView is better
        // but I LIKE IT
        val addWGS84 = { addTableRow("WGS-84", wgs84, csType == CSType.WGS84, { newIntent(wgs84) }, { copyToClipboard(wgs84) }) }
        val addGCJ02 = { addTableRow("GCJ-02", gcj02, csType == CSType.GCJ02, { newIntent(gcj02) }, { copyToClipboard(gcj02) }) }
        val addBD09 = { addTableRow("BD-09", bd09, csType == CSType.BD09, { newIntent(bd09) }, { copyToClipboard(bd09) }) }

        when (csType) {
            CSType.WGS84 -> {
                addWGS84()
                addGCJ02()
                addBD09()
            }
            CSType.GCJ02 -> {
                addGCJ02()
                addWGS84()
                addBD09()
            }
            CSType.BD09 -> {
                addBD09()
                addWGS84()
                addGCJ02()
            }
        }
    }

    private fun newIntent(doubleArray: DoubleArray) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("geo:${doubleArray.joinToString(",")}$rst")
        startActivity(intent)
    }

    private fun copyToClipboard(doubleArray: DoubleArray) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip =
                ClipData.newPlainText("GeoTransformer", doubleArray.joinToString(","))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("DefaultLocale")
    fun Double.toFixed6(): String {
        return java.lang.String.format("%.6f", this)
    }

    @SuppressLint("SetTextI18n")
    private fun addTableRow(name: String, value: DoubleArray, isSource: Boolean = false, onClick: (() -> Unit), onLongPress: (() -> Unit)) {
        val tableRow = layoutInflater.inflate(R.layout.activity_transform_row, table, false)
        tableRow.name.text = "${if (!isSource) ">> " else "== "}$name"
        tableRow.value.text = value.joinToString(",") { it.toFixed6() }
        tableRow.setOnClickListener { onClick() }
        tableRow.setOnLongClickListener { onLongPress(); true }
        table.addView(tableRow)
    }
}