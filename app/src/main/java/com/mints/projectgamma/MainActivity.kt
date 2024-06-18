package com.mints.projectgamma

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mints.projectgamma.api.ApiClient
import com.mints.projectgamma.api.ApiService
import com.mints.projectgamma.api.Invasion
import com.mints.projectgamma.mapping.DataMappings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


private const val MAX_VISITED_INVASIONS = 750

class MainActivity : ComponentActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var allInvasions: List<Invasion>
    private lateinit var filteredInvasions: MutableList<Invasion>
    private lateinit var selectedItems: MutableList<String>
    private var items = arrayOf(
        "Cliff","Arlo","Sierra","Giovanni","Dragon Female", "Dark Female",
        "Bug Male","Fairy Female", "Fighting Female", "Fire Female", "Flying Female",
        "Ghost","Grass Male","Ground Male","Ice Female","Normal Male","Poison Female",
        "Psychic Male","Rock Male","Steel Male","Water Female","Water Male",
        "Electric","Typeless Female","Typeless Male", "Kecleon","Showcase")
    private var defaultFilter = arrayOf("Dragon Female", "Dark Female",
        "Bug Male","Fairy Female", "Fighting Female", "Fire Female", "Flying Female",
        "Ghost","Grass Male","Ground Male","Ice Female","Normal Male","Poison Female",
        "Psychic Male","Rock Male","Steel Male","Water Female","Water Male",
        "Electric","Typeless Female","Typeless Male")

    private lateinit var selectedDataSources: MutableList<String>
    private var dataSources = arrayOf("NYC","London","Vancouver","Singapore","Sydney")
    private var defaultDataSources = arrayOf("NYC")
    private lateinit var selectedBooleanArray: BooleanArray
    private lateinit var selectedBooleanArrayData: BooleanArray
    private lateinit var selectedItemsTextView: TextView
    private lateinit var visitedInvasions: MutableList<Invasion>
    private var cliffCheck = false
    private var arloCheck = false
    private var sierraCheck = false
    private var giovanniCheck = false
    private var dragonFemaleCheck = false
    private var darkFemaleCheck = false
    private var bugMaleCheck = false
    private var fairyFemaleCheck = false
    private var fightingFemaleCheck = false
    private var fireFemaleCheck = false
    private var flyingFemaleCheck = false
    private var ghostCheck = false
    private var grassMaleCheck = false
    private var groundMaleCheck = false
    private var iceFemaleCheck = false
    private var normalMaleCheck = false
    private var poisonFemaleCheck = false
    private var psychicMaleCheck = false
    private var rockMaleCheck = false
    private var metalMaleCheck = false
    private var waterFemaleCheck = false
    private var waterMaleCheck = false
    private var electricCheck = false
    private var typelessFemaleCheck = false
    private var typelessMaleCheck = false
    private var kecleonCheck = false
    private var showcaseCheck = false
    private var allSelected = true
    private var nycChecked = false
    private var singaporeChecked = false
    private var londonChecked = false
    private var vancouverChecked = false
    private var sydneyChecked = false



    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val showMultiSelectDialogButton: Button = findViewById(R.id.showMultiSelectDialogButton)
        val showMultiSelectDialogButtonData: Button = findViewById(R.id.showMultiSelectDialogButtonData)
        val textBanner: TextView = findViewById(R.id.text_banner)
        val apiCallButton: Button = findViewById(R.id.button_make_api_call)
        val selectAllButton : Button = findViewById(R.id.select_deselect)

        textBanner.movementMethod = LinkMovementMethod.getInstance()
            selectedItemsTextView  = findViewById(R.id.selectedItemsTextView)
        resultTextView = findViewById(R.id.text_view_result)

        visitedInvasions = mutableListOf()
        selectedItems = mutableListOf()
        selectedDataSources = mutableListOf()

        loadFilterArray()
        loadDataSources()
        loadVisitedInvasions()

        if(selectedItems.isEmpty()) {
            selectedItems = defaultFilter.toMutableList()
            selectedBooleanArray = BooleanArray(items.size) { index ->
                when (items[index]) {
                    "Cliff", "Arlo", "Sierra", "Giovanni" -> false
                    else -> true
                }
            }
        } else {
            Log.d("TAG", "T:$selectedItems")

            selectedBooleanArray = BooleanArray(items.size) { index ->
                selectedItems.contains(items[index])
            }
        }

        if(selectedDataSources.isEmpty()) {
            selectedDataSources = defaultDataSources.toMutableList()
            selectedBooleanArrayData = BooleanArray(dataSources.size) { index ->
                when (dataSources[index]) {
                    "London", "Vancouver", "Singapore", "Sydney" -> false
                    else -> true
                }
            }
        } else {
            Log.d("TAG", "T:$selectedDataSources")

            selectedBooleanArrayData = BooleanArray(dataSources.size) { index ->
                selectedDataSources.contains(dataSources[index])
            }
        }


        selectedItemsTextView.text =
            getString(R.string.selected_items, selectedItems.joinToString(", "))
        createFilter()
        makeApiCalls()

        showMultiSelectDialogButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Items")
            val tempSelectedItems = ArrayList(selectedItems)
            val tempSelectedBooleanArray = selectedBooleanArray.clone()
            builder.setMultiChoiceItems(items, tempSelectedBooleanArray) { _, which, isChecked ->
                if (isChecked) {
                    tempSelectedItems.add(items[which])
                } else {
                    tempSelectedItems.remove(items[which])
                }
                tempSelectedBooleanArray[which] = isChecked
            }

            builder.setPositiveButton("OK") { _, _ ->
                selectedItems.clear()
                selectedItems.addAll(tempSelectedItems)
                System.arraycopy(tempSelectedBooleanArray, 0, selectedBooleanArray, 0, tempSelectedBooleanArray.size)

                if (selectedItems.containsAll(items.toList())) {
                    selectedItemsTextView.text = getString(R.string.all_items_selected)
                    createFilter()
                    saveFilterArray()
                } else if (selectedItems.isEmpty()) {
                    selectedItemsTextView.text = getString(R.string.no_items_selected)
                    createFilter()
                    saveFilterArray()
                } else {
                    selectedItemsTextView.text =
                        getString(R.string.selected_items, selectedItems.joinToString(", "))
                    createFilter()
                    saveFilterArray()
                }


            }

            builder.setNegativeButton("Cancel", null)
            builder.show()
        }


        showMultiSelectDialogButtonData.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Data Sources")
            val tempSelectedDataSources = ArrayList(selectedDataSources)
            val tempSelectedBooleanArrayData = selectedBooleanArrayData.clone()

            builder.setMultiChoiceItems(dataSources, tempSelectedBooleanArrayData) { _, which, isChecked ->
                if (isChecked) {
                    tempSelectedDataSources.add(dataSources[which])
                } else {
                    tempSelectedDataSources.remove(dataSources[which])
                }
                tempSelectedBooleanArrayData[which] = isChecked
            }

            builder.setPositiveButton("OK") { _, _ ->
                selectedDataSources.clear()
                selectedDataSources.addAll(tempSelectedDataSources)
                System.arraycopy(tempSelectedBooleanArrayData, 0, selectedBooleanArrayData, 0, tempSelectedBooleanArrayData.size)

                createDataSourceFilter()
            }

            builder.setNegativeButton("Cancel", null)
            builder.show()
        }


        apiCallButton.setOnClickListener {
            createDataSourceFilter()
            makeApiCalls()
            Log.d("TAG","Selected:$selectedDataSources")

        }


        selectAllButton.setOnClickListener {
            selectAll()
        }
        allInvasions = emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectAll() {
        if (!allSelected) {
            selectedBooleanArray.fill(true)
            selectedItems.clear()
            selectedItems.addAll(items)
            selectedItemsTextView.text = getString(R.string.all_items_selected)
        } else {
            selectedBooleanArray.fill(false)
            selectedItems.clear()
            selectedItemsTextView.text = getString(R.string.no_items_selected)
        }
        allSelected = !allSelected
        createFilter()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun createFilter() {
        resetFilter()

        selectedItems.forEach { item ->
            when (item) {
                "Cliff" -> cliffCheck = true
                "Arlo" -> arloCheck = true
                "Sierra" -> sierraCheck = true
                "Giovanni" -> giovanniCheck = true
                "Dragon Female" -> dragonFemaleCheck = true
                "Dark Female" -> darkFemaleCheck = true
                "Bug Male" -> bugMaleCheck = true
                "Fairy Female" -> fairyFemaleCheck = true
                "Fighting Female" -> fightingFemaleCheck = true
                "Fire Female" -> fireFemaleCheck = true
                "Flying Female" -> flyingFemaleCheck = true
                "Ghost" -> ghostCheck = true
                "Grass Male" -> grassMaleCheck = true
                "Ground Male" -> groundMaleCheck = true
                "Ice Female" -> iceFemaleCheck = true
                "Normal Male" -> normalMaleCheck = true
                "Poison Female" -> poisonFemaleCheck = true
                "Psychic Male" -> psychicMaleCheck = true
                "Rock Male" -> rockMaleCheck = true
                "Metal Male" -> metalMaleCheck = true
                "Water Female" -> waterFemaleCheck = true
                "Water Male" -> waterMaleCheck = true
                "Electric" -> electricCheck = true
                "Typeless Female" -> typelessFemaleCheck = true
                "Typeless Male" -> typelessMaleCheck = true
                "Kecleon" -> kecleonCheck = true
                "Showcase" -> showcaseCheck = true
            }
        }
        saveFilterArray()

    }
    private fun resetFilter() {
        cliffCheck = false
        arloCheck = false
        sierraCheck = false
        giovanniCheck = false
        dragonFemaleCheck = false
        darkFemaleCheck = false
        bugMaleCheck = false
        fairyFemaleCheck = false
        fightingFemaleCheck = false
        fireFemaleCheck = false
        flyingFemaleCheck = false
        ghostCheck = false
        grassMaleCheck = false
        groundMaleCheck = false
        iceFemaleCheck = false
        normalMaleCheck = false
        poisonFemaleCheck = false
        psychicMaleCheck = false
        rockMaleCheck = false
        metalMaleCheck = false
        waterFemaleCheck = false
        waterMaleCheck = false
        electricCheck = false
        typelessFemaleCheck = false
        typelessMaleCheck = false
        kecleonCheck = false
        showcaseCheck = false
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDataSourceFilter() {
        resetDataSources()
        selectedDataSources.forEach { item ->
            when (item) {
                "NYC" -> nycChecked = true
                "London" -> londonChecked = true
                "Vancouver" -> vancouverChecked = true
                "Singapore" -> singaporeChecked = true
                "Sydney" -> sydneyChecked = true
            }
        }
        saveDataSources()
    }


    private fun resetDataSources() {
         nycChecked = false
         singaporeChecked = false
         londonChecked = false
        vancouverChecked = false
         sydneyChecked = false
    }

    private fun saveFilterArray() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        if(selectedItems.isEmpty()) {
            selectedBooleanArray = BooleanArray(items.size)

        }

        val json = gson.toJson(selectedItems)
        editor.putString("filterArray", json)
        editor.apply()
    }


    private fun saveDataSources() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        if(selectedDataSources.isEmpty()) {
            selectedBooleanArrayData = BooleanArray(dataSources.size)

        }

        val json = gson.toJson(selectedDataSources)
        editor.putString("filterDataArray", json)
        editor.apply()
    }

    private fun loadFilterArray() {
        val   sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("filterArray", null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        selectedItems = gson.fromJson(json, type) ?: mutableListOf()


        if(selectedItems.isNotEmpty()) {
            Log.d("MainActivity", "TEST TEST: $selectedItems")

            selectedBooleanArray = BooleanArray(items.size) { index ->
                selectedItems.contains(items[index])

            }
        } else {
            Log.d("MainActivity", "TEST 2: $selectedItems")

            selectedItems = defaultFilter.toMutableList()
        }

    }
    private fun loadDataSources() {
        val   sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("filterDataArray", null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        selectedDataSources = gson.fromJson(json, type) ?: mutableListOf()
        if(selectedDataSources.isNotEmpty()) {

            selectedBooleanArrayData = BooleanArray(dataSources.size) { index ->
                selectedDataSources.contains(dataSources[index])

            }
        } else {

            selectedDataSources = defaultDataSources.toMutableList()
        }

    }



    private fun saveVisitedInvasions() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val limitedList = visitedInvasions.takeLast(MAX_VISITED_INVASIONS)
        val json = gson.toJson(limitedList)
        editor.putString("visitedInvasions", json)
        editor.apply()
    }

    private fun loadVisitedInvasions() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("visitedInvasions", null)
        val type = object : TypeToken<MutableList<Invasion>>() {}.type
        val allVisitedInvasions = gson.fromJson<MutableList<Invasion>>(json, type) ?: mutableListOf()
        visitedInvasions = allVisitedInvasions.takeLast(MAX_VISITED_INVASIONS).toMutableList()

        if(visitedInvasions.isNotEmpty()) {

            Log.d("TAG","Visited invasions:$visitedInvasions")
        } else {
            Log.d("TAG","Visited invasions is empty.")
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeApiCalls() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = coroutineScope {
                    val deferreds = mutableListOf<Deferred<List<Invasion>>>()

                    if (selectedDataSources.contains("NYC")) {
                        deferreds.add(async { makeApiCall(ApiClient.retrofitNYC) })
                    }
                    if (selectedDataSources.contains("London")) {
                        deferreds.add(async { makeApiCall(ApiClient.retrofitLondon) })
                    }
                    if (selectedDataSources.contains("Singapore")) {
                        deferreds.add(async { makeApiCall(ApiClient.retrofitSingapore) })
                    }
                    if (selectedDataSources.contains("Vancouver")) {
                        deferreds.add(async { makeApiCall(ApiClient.retrofitVancouver) })
                    }
                    if (selectedDataSources.contains("Sydney")) {
                        deferreds.add(async { makeApiCall(ApiClient.retrofitSydney) })
                    }

                    deferreds.awaitAll().flatten()
                }
                withContext(Dispatchers.Main) {
                    handleSuccess(results)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleFailure(e)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun makeApiCall(apiClient: Retrofit): List<Invasion> {
        val apiService = apiClient.create(ApiService::class.java)

        return try {
            val response = apiService.getInvasions()
            response.invasions
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                handleFailure(e)
            }
            emptyList()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatInvasionEndTime(invasionEnd: Long): String {
        try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss zzz")
                .withZone(ZoneId.systemDefault())

            val formattedTime = formatter.format(Instant.ofEpochSecond(invasionEnd))

            return formattedTime
        } catch (e: Exception) {
            Log.e("TimeDebug", "Error formatting invasion end time: ${e.message}")
            return "Error"
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSuccess(invasions: List<Invasion>) {
        filteredInvasions = mutableListOf()
        var counter = 0
        val maxAtOnce = 200
        val sortedInvasions = sortInvasionsByEndTime(invasions)

        for (invasion in sortedInvasions) {
            if (counter >= maxAtOnce) {
                break
            }
            val characterName = DataMappings.characterNamesMap[invasion.character] ?: continue
            if(selectedItems.contains("Kecleon")) {
                kecleonCheck = true
            }
            if(selectedItems.contains("Showcase")) {
                showcaseCheck = true
            }
            val isSelectedCharacter = when (characterName) {
                "Cliff" -> cliffCheck
                "Arlo" -> arloCheck
                "Sierra" -> sierraCheck
                "Giovanni" -> giovanniCheck
                "Dragon Female" -> dragonFemaleCheck
                "Dark Female" -> darkFemaleCheck
                "Bug Male" -> bugMaleCheck
                "Fairy Female" -> fairyFemaleCheck
                "Fighting Female" -> fightingFemaleCheck
                "Fire Female" -> fireFemaleCheck
                "Flying Female" -> flyingFemaleCheck
                "Ghost" -> ghostCheck
                "Grass Male" -> grassMaleCheck
                "Ground Male" -> groundMaleCheck
                "Ice Female" -> iceFemaleCheck
                "Normal Male" -> normalMaleCheck
                "Poison Female" -> poisonFemaleCheck
                "Psychic Male" -> psychicMaleCheck
                "Rock Male" -> rockMaleCheck
                "Metal Male" -> metalMaleCheck
                "Water Female" -> waterFemaleCheck
                "Water Male" -> waterMaleCheck
                "Electric" -> electricCheck
                "Typeless Female" -> typelessFemaleCheck
                "Typeless Male" -> typelessMaleCheck
                else -> false
            }
            if (isSelectedCharacter) {
                Log.d("TAG:", "Adding 1 :${invasion.character}")

                filteredInvasions.add(invasion)
                counter += 1
            }

            if(kecleonCheck && invasion.type == 8) {
                Log.d("TAG:", " Adding 2 ${invasion.character}")
                invasion.character = 1

                filteredInvasions.add(invasion)
                counter +=1
                kecleonCheck = false
            }

            if(showcaseCheck && invasion.type == 9) {
                Log.d("TAG:", " Adding 3 ${invasion.character}")
                invasion.character = 0

                filteredInvasions.add(invasion)
                counter +=1
                showcaseCheck = false
            }


        }

        updateResultTextView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateResultTextView() {
        val stringBuilder = SpannableStringBuilder()
        for ((index, invasion) in filteredInvasions.withIndex()) {
            postData(invasion, stringBuilder, index)
        }
        resultTextView.text = stringBuilder
        resultTextView.movementMethod = LinkMovementMethod.getInstance()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun postData(invasion: Invasion, stringBuilder: SpannableStringBuilder, @Suppress("UNUSED_PARAMETER") index: Int) {
        if (!visitedInvasions.contains(invasion)) {
            val source: String = when {
                invasion.lat.toString().startsWith("40") -> "New York"
                invasion.lat.toString().startsWith("51") -> "London"
                invasion.lat.toString().startsWith("-33") || invasion.lat.toString().startsWith("-34") -> "Sydney"
                invasion.lat.toString().startsWith("1") -> "Singapore"
                invasion.lat.toString().startsWith("49") -> "Vancouver"
                else -> "Unknown"
            }
            val link = "https://ipogo.app/?coords=${invasion.lat},${invasion.lng}"
            val endTime = formatInvasionEndTime(invasion.invasion_end)
            val teleportText = "Teleport"
            val copyText = "Copy"
            val deleteText = "Delete"
            val invasionText = "Name: ${invasion.name}\n" +
                    "Location: $teleportText | $copyText | $deleteText\n" +
                    "Character: ${invasion.characterName}\n" +
                    "Type: ${invasion.typeDescription}\n" +
                     "Source: $source\n" +
                    "Ending at: $endTime\n\n"
            val start = stringBuilder.length
            stringBuilder.append(invasionText)
            val spanStartTeleport = start + "Name: ${invasion.name}\nLocation: ".length
            val spanEndTeleport = spanStartTeleport + teleportText.length
            val spanStartCopy = spanEndTeleport + " | ".length
            val spanEndCopy = spanStartCopy + copyText.length
            val spanStartDelete = spanEndCopy + " | ".length
            val spanEndDelete = spanStartDelete + deleteText.length

            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    widget.context.startActivity(intent)

                    filteredInvasions.remove(invasion)
                    visitedInvasions.add(invasion)
                    saveVisitedInvasions()
                    updateResultTextView()
                }
            }, spanStartTeleport, spanEndTeleport, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val clipboard = widget.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Coordinates", "${invasion.lat},${invasion.lng}")
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(widget.context, "Coordinates copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            }, spanStartCopy, spanEndCopy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    filteredInvasions.remove(invasion)
                    visitedInvasions.add(invasion)
                    saveVisitedInvasions()
                    updateResultTextView()
                }
            }, spanStartDelete, spanEndDelete, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun handleFailure(exception: Exception) {
        resultTextView.text = getString(R.string.api_call_failed, exception.message)
    }
    private fun sortInvasionsByEndTime(invasions: List<Invasion>): List<Invasion> {
        return invasions.sortedByDescending { it.invasion_end }
    }


}
