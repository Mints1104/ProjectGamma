package com.mints.projectgamma


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val MAX_VISITED_INVASIONS = 2000

class MainActivity : ComponentActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var allInvasions: List<Invasion>
    private lateinit var filteredInvasions: MutableList<Invasion>
    private lateinit var selectedItems: MutableList<String>
    private var items = arrayOf(
        "Cliff","Arlo","Sierra","Giovanni", "Kecleon","Showcase", "Dragon Female", "Dark Female",
        "Bug Male","Fairy Female", "Fighting Female", "Fire Female", "Flying Female",
        "Ghost","Grass Male","Ground Male","Ice Female","Normal Male","Poison Female",
        "Psychic Male","Rock Male","Steel Male","Water Female","Water Male",
        "Electric","Typeless Female","Typeless Male")
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
    private lateinit var loadedGrunts: TextView
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
    private var placeAccepted = false
    private var storedFavourites: MutableList<FavouriteLocation> = mutableListOf()
    private var initialiseFavoritesList = false

    private lateinit var homeCoordinates:String
    private var sessionGruntsBeat = 0
    private lateinit var sessionGrunts: TextView
    private lateinit var favoritesText: TextView
   private lateinit  var settingsTextView: EditText
   private var initialiseImport = false
private var locationDeleted = false
    private lateinit var gruntsPerMinuteTextView: TextView
    private var isTracking = false
    private lateinit var gpmJob: Job



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
        val selectFavorites : Button = findViewById(R.id.favoritesButton)
        textBanner.movementMethod = LinkMovementMethod.getInstance()
        selectedItemsTextView  = findViewById(R.id.selectedItemsTextView)

        resultTextView = findViewById(R.id.text_view_result)
        visitedInvasions = mutableListOf()
        selectedItems = mutableListOf()
        selectedDataSources = mutableListOf()
        gruntsPerMinuteTextView = findViewById(R.id.session_grunts)
        startTrackingGPM()



        selectFavorites.setOnClickListener { view->



            showFullScreenDialog(view)
        }

        loadFilterArray()
        loadDataSources()
        loadVisitedInvasions()
        loadHomeCoordinates()

        loadedGrunts = findViewById(R.id.saved_grunts)

        sessionGrunts = findViewById(R.id.session_grunts)
   //     sessionGrunts.text = getString(R.string.number_of_grunts_beat, sessionGruntsBeat)

        loadedGrunts.text = getString(R.string.saved_grunts_info, visitedInvasions.size, MAX_VISITED_INVASIONS)

        Log.d("TAG","Loading home coords: $homeCoordinates")
        printJournal()

        val homeCoordinatesEditText: EditText = findViewById(R.id.userHomeCoordinates)

        homeCoordinatesEditText.setText(homeCoordinates)
        val coordinatesRegex = """^-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?$""".toRegex()

        // Handle the "Done" action on the keyboard
        homeCoordinatesEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val input = homeCoordinatesEditText.text.toString().trim()
                if (coordinatesRegex.matches(input)) {
                    homeCoordinates = input
                    saveHomeCoordinates()
                    Toast.makeText(this, "Coordinates saved: $homeCoordinates", Toast.LENGTH_SHORT).show()

                    // Hide the keyboard and clear focus
                    hideKeyboard()
                    homeCoordinatesEditText.clearFocus()
                    true  // Return true to indicate that the action was handled
                } else {
                    if(homeCoordinates.isEmpty() || input.isEmpty()) {
                        homeCoordinatesEditText.setText("")


                        Toast.makeText(this, "Home coordinates cleared.", Toast.LENGTH_SHORT).show()
                        homeCoordinates = ""
                        saveHomeCoordinates()


                    } else {
                        homeCoordinatesEditText.setText(homeCoordinates)
                        Toast.makeText(this, "Invalid coordinates format. Please enter valid coordinates.", Toast.LENGTH_SHORT).show()

                    }

                    hideKeyboard()
                    homeCoordinatesEditText.clearFocus()

                    false  // Return false to indicate that the action was not handled
                }
            } else {
                false  // Return false to let the system handle other actions
            }
        }

        // Clear focus when clicking outside the EditText
        homeCoordinatesEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard()
            }
        }






        Log.d("TAG","Stored visited invasions:${visitedInvasions.size}")

        if(selectedItems.isEmpty()) {
            selectedItems = defaultFilter.toMutableList()
            selectedBooleanArray = BooleanArray(items.size) { index ->
                when (items[index]) {
                    "Cliff", "Arlo", "Sierra", "Giovanni" -> false
                    else -> true
                }
            }
        } else {
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
            builder.setTitle("Select Invasions")
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
                tempSelectedBooleanArray.copyInto(selectedBooleanArray)
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
                tempSelectedBooleanArrayData.copyInto(selectedBooleanArrayData)
                createDataSourceFilter()
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        apiCallButton.setOnClickListener {
            createDataSourceFilter()
            makeApiCalls()
        }

        selectAllButton.setOnClickListener {
            selectAll()
        }
        allInvasions = emptyList()
    }


    private fun showFullScreenDialog(view: View) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_fullscreen)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        val closeButton: Button = dialog.findViewById(R.id.dialog_close_button)
        val addButton: Button = dialog.findViewById(R.id.dialog_add_button)
        val importExportButton: Button = dialog.findViewById(R.id.editTextImportExport)

        favoritesText = dialog.findViewById(R.id.favoritesTextView)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        loadLocations()
        updateUIWithFavourites()
        Log.d("Load Location","$storedFavourites")



        importExportButton.setOnClickListener {

            showImportExportDialog()
        }
        addButton.setOnClickListener {
            showAddFavoriteDialog()
        }

        dialog.show()
    }



    private fun showAddFavoriteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_favorite, null)
        val editTextLocationName: EditText = dialogView.findViewById(R.id.editTextLocationName)
        val editTextCoordinates: EditText = dialogView.findViewById(R.id.editTextCoordinates)
        val buttonCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val buttonAdd: Button = dialogView.findViewById(R.id.buttonAdd)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonAdd.setOnClickListener {
            var locationName = editTextLocationName.text.toString()
            var coordinates = editTextCoordinates.text.toString()

            addFavourites(locationName, coordinates)

            if (placeAccepted) {
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Invalid or duplicate location", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showImportExportDialog() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.import_export_fullscreen)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT)

        val buttonClose: Button = dialog.findViewById(R.id.dialog_close_import)
        val buttonConfirmImport: Button = dialog.findViewById(R.id.dialog_confirm_import)
       settingsTextView = dialog.findViewById(R.id.editTextImportExport)

populateFavImport()
        buttonClose.setOnClickListener {

            dialog.dismiss()

        }

        buttonConfirmImport.setOnClickListener {

            if(!initialiseImport) {
                populateFavImport()

            } else {
                populateFavFromImport()
            }
            populateFavFromImport()
            Toast.makeText(this, "Successfully imported favorites!", Toast.LENGTH_SHORT).show()



        }


        dialog.show()


    }

    private fun populateFavImport() {


        if(storedFavourites.isNotEmpty()) {

            for (location in storedFavourites) {

                Log.d("ImportTest", location.name)

                settingsTextView.append("{" + location.name + ":" + location.coordinates + "}")
                saveImport()
                initialiseImport = true
            }
        }

    }


    private fun populateFavFromImport() {

        Log.d("Test", settingsTextView.toString())




    }

    private fun showEditFavoriteDialog(location: FavouriteLocation) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_favorite, null)
        val editTextLocationName: EditText = dialogView.findViewById(R.id.editTextLocationName)
        val editTextCoordinates: EditText = dialogView.findViewById(R.id.editTextCoordinates)
        val buttonCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val buttonAdd: Button = dialogView.findViewById(R.id.buttonAdd)
        val dialogTitle:TextView = dialogView.findViewById(R.id.dialog_title)
        dialogTitle.text= getString(R.string.edit_favorite_location)
        buttonAdd.text = getString(R.string.confirm)
        editTextLocationName.setText(location.name)

        editTextCoordinates.setText(location.coordinates)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonAdd.setOnClickListener {


            var locationName = editTextLocationName.text.toString()
            var coordinates = editTextCoordinates.text.toString()

            editFavourites(location, locationName, coordinates)

            if (placeAccepted) {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteFavoriteDialog(location: FavouriteLocation) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_favorite, null)
        val buttonCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val buttonDelete: Button = dialogView.findViewById(R.id.buttonConfirm)


        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonDelete.setOnClickListener {

            Toast.makeText(this, "Deleted location: ${location.name}", Toast.LENGTH_SHORT).show()



            deleteFavourite(location)


            if (locationDeleted) {
                dialog.dismiss()
            }
            locationDeleted = false
        }

        dialog.show()
    }

    private fun deleteFavourite(location:FavouriteLocation) {
        // Remove location from list and update UI
        storedFavourites.remove(location)
           saveLocations() // Save updated list
        updateUIWithFavourites() // Refresh UI
        locationDeleted = true


    }

    private fun editFavourites(oldLocation: FavouriteLocation, locationName: String, coordinates: String) {
        val coordinatesRegex = """^-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?$""".toRegex()

        // Validate the format of the coordinates if provided
        if (!coordinatesRegex.matches(coordinates) && coordinates.isNotEmpty()) {
            Log.d("TAG", "Incorrect coordinate format")
            Toast.makeText(this, "Incorrect coordinate format", Toast.LENGTH_SHORT).show()
            return
        }

        // Use the old location's values if the new ones are empty
        var newName = locationName.ifEmpty { oldLocation.name }
        var newCoordinates = coordinates.ifEmpty { oldLocation.coordinates }

        // Check for duplicates only if newName or newCoordinates are not empty
        if (newName.isNotEmpty() && storedFavourites.any { it.name == newName && it !== oldLocation }) {
            Toast.makeText(this, "Duplicate or invalid location name found", Toast.LENGTH_SHORT).show()
            return
        }

        if (newCoordinates.isNotEmpty() && storedFavourites.any { it.coordinates == newCoordinates && it !== oldLocation }) {
            Toast.makeText(this, "Duplicate coordinates found", Toast.LENGTH_SHORT).show()
            return
        }

        placeAccepted = true

        // Update the old location with new values
        oldLocation.name = newName
        oldLocation.coordinates = newCoordinates
        Toast.makeText(this, "Edit successful", Toast.LENGTH_SHORT).show()

        // Save the updated list and refresh the UI
        saveLocations()
        updateUIWithFavourites()
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

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = this.currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
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

    private fun saveHomeCoordinates() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("homeCoordinates", homeCoordinates)
        editor.apply()
    }


    private fun loadHomeCoordinates() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        homeCoordinates = sharedPreferences.getString("homeCoordinates", "") ?: ""
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
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("filterArray", null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        selectedItems = gson.fromJson(json, type) ?: mutableListOf()

        if(selectedItems.isNotEmpty()) {
            selectedBooleanArray = BooleanArray(items.size) { index ->
                selectedItems.contains(items[index])
            }
        } else {
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
                filteredInvasions.add(invasion)
                counter += 1
            }

            if(kecleonCheck && invasion.type == 8) {
                invasion.character = 1
                filteredInvasions.add(invasion)
                counter +=1
                kecleonCheck = false
            }

            if(showcaseCheck && invasion.type == 9) {
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
        for (invasion in filteredInvasions) {
            postData(invasion, stringBuilder)
        }
        resultTextView.text = stringBuilder
        resultTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun calculateGruntsPerMinute(startTime: Long, gruntsBeat: Int): Double {
        val elapsedTimeMinutes = (System.currentTimeMillis() - startTime) / 60000.0
        return if (elapsedTimeMinutes > 0) gruntsBeat / elapsedTimeMinutes else 0.0
    }





    @RequiresApi(Build.VERSION_CODES.O)
    private fun postData(invasion: Invasion, stringBuilder: SpannableStringBuilder) {
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
                    handleInteraction(invasion)

                    if(invasion.characterName== "Kecleon" || invasion.characterName == "Showcase") {
                       Log.d("TAG",invasion.characterName)

                    } else {
                        sessionGruntsBeat++

                        Log.d("TAG", "Success $sessionGruntsBeat")
                        Log.d("TAG", invasion.characterName)

                        sessionGrunts.text = getString(R.string.number_of_grunts_beat, sessionGruntsBeat)


                    }

                    loadedGrunts.text = getString(R.string.saved_grunts_info, visitedInvasions.size, MAX_VISITED_INVASIONS)
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
                    handleInteraction(invasion)

                    if(invasion.characterName== "Kecleon" || invasion.characterName == "Showcase") {
                        Log.d("TAG",invasion.characterName)

                    } else {
                        sessionGruntsBeat++

                        Log.d("TAG","Success $sessionGruntsBeat")
                        Log.d("TAG", invasion.characterName)

                        sessionGrunts.text = getString(R.string.number_of_grunts_beat, sessionGruntsBeat)


                    }

                    loadedGrunts.text = getString(R.string.saved_grunts_info, visitedInvasions.size, MAX_VISITED_INVASIONS)

                    updateResultTextView()

                }
            }, spanStartDelete, spanEndDelete, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }










    private var interactionCount = 0
    private var startTime: Long = 0L


    /*

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postData(invasion: Invasion, stringBuilder: SpannableStringBuilder) {
        if (!visitedInvasions.contains(invasion)) {
            val source: String = when {
                invasion.lat.toString().startsWith("40") -> "New York"
                invasion.lat.toString().startsWith("51") -> "London"
                invasion.lat.toString().startsWith("-33") || invasion.lat.toString().startsWith("-34") -> "Sydney"
                invasion.lat.toString().startsWith("1") -> "Singapore"
                invasion.lat.toString().startsWith("49") -> "Vancouver"
                else -> "Unknown"
            }
            val ipogoLink = "https://ipogo.app/?coords=${invasion.lat},${invasion.lng}"
            val endTime = formatInvasionEndTime(invasion.invasion_end)
            val teleportText = "Teleport"
            val joystickTeleportText = "Joystick TP"
            val copyText = "Copy"
            val deleteText = "Delete"
            val invasionText = "Name: ${invasion.name}\n" +
                    "Location: $teleportText | $joystickTeleportText | $copyText | $deleteText\n" +
                    "Character: ${invasion.characterName}\n" +
                    "Type: ${invasion.typeDescription}\n" +
                    "Source: $source\n" +
                    "Ending at: $endTime\n\n"
            val start = stringBuilder.length
            stringBuilder.append(invasionText)
            val spanStartTeleport = start + "Name: ${invasion.name}\nLocation: ".length
            val spanEndTeleport = spanStartTeleport + teleportText.length
            val spanStartJoystickTeleport = spanEndTeleport + " | ".length
            val spanEndJoystickTeleport = spanStartJoystickTeleport + joystickTeleportText.length
            val spanStartCopy = spanEndJoystickTeleport + " | ".length
            val spanEndCopy = spanStartCopy + copyText.length
            val spanStartDelete = spanEndCopy + " | ".length
            val spanEndDelete = spanStartDelete + deleteText.length

            // iPogo Teleport span
            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ipogoLink))
                    widget.context.startActivity(intent)

                    handleInteraction(invasion)
                }
            }, spanStartTeleport, spanEndTeleport, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // GPS Joystick Teleport span
            stringBuilder.setSpan(object : ClickableSpan() {
                @SuppressLint("StringFormatInvalid")
                override fun onClick(widget: View) {
                    val intent = Intent().apply {
                        action = "theappninjas.gpsjoystick.TELEPORT"
                        putExtra("lat", invasion.lat.toFloat())
                        putExtra("lng", invasion.lng.toFloat())
                    }
                    intent.component = ComponentName("com.theappninjas.fakegpsjoystick",
                        "com.theappninjas.fakegpsjoystick.service.OverlayService")

                    try {
                        val componentName = widget.context.startService(intent)
                        if (componentName != null) {
                            intent.component = ComponentName("com.thekkgqtaoxz.ymaaammipjyfatw",
                                "com.thekkgqtaoxz.ymaaammipjyfatw.service.OverlayService")
                            handleInteraction(invasion)
                        } else {
                            throw IllegalStateException("Service not found")
                        }
                    } catch (e: Exception) {
                        Toast.makeText(widget.context, "Error: Joystick not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }, spanStartJoystickTeleport, spanEndJoystickTeleport, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Copy coordinates span
            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val clipboard = widget.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Coordinates", "${invasion.lat},${invasion.lng}")
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(widget.context, "${invasion.lat}, ${invasion.lng} copied ", Toast.LENGTH_SHORT).show()
                }
            }, spanStartCopy, spanEndCopy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Delete invasion span
            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    handleInteraction(invasion)
                    Toast.makeText(widget.context, "Deleted invasion: ${invasion.name}", Toast.LENGTH_SHORT).show()
                }
            }, spanStartDelete, spanEndDelete, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

     */



    private fun startTrackingGPM() {
        if (!isTracking) {
            isTracking = true
            startTime = System.currentTimeMillis()

            // Start a coroutine to update GPM every second
            gpmJob = CoroutineScope(Dispatchers.Main).launch {
                while (isTracking) {
                    updateGPMDisplay()
                    delay(1000L)  // Update every second
                }
            }
        }
    }

    private fun stopTrackingGPM() {
        isTracking = false
        if (::gpmJob.isInitialized) {
            gpmJob.cancel()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleInteraction(invasion: Invasion) {
        interactionCount++

        // Start tracking after the first interaction
        if (interactionCount == 1) {
            startTrackingGPM()
        }

        filteredInvasions.remove(invasion)
        visitedInvasions.add(invasion)
        saveVisitedInvasions()
        updateResultTextView()
        loadedGrunts.text = getString(R.string.saved_grunts_info, visitedInvasions.size, MAX_VISITED_INVASIONS)
    }


    private fun updateGPMDisplay() {
        val currentTime = System.currentTimeMillis()
        val timeElapsedSeconds = (currentTime - startTime) / 1000.0 // seconds
        val timeElapsedMinutes = timeElapsedSeconds / 60.0 // minutes

        val gpmThreshold = 0.5 // Minimum time in minutes before calculating GPM (30 seconds)
        val windowSizeMinutes = 2.0 // Sliding window size in minutes
        val effectiveTimeElapsed = minOf(timeElapsedMinutes, windowSizeMinutes)

        // Adjust GPM calculation to consider the entire time elapsed, not just the window
        val gpm = if (timeElapsedMinutes >= gpmThreshold && interactionCount > 0) {
            interactionCount / timeElapsedMinutes // grunts per minute across the entire elapsed time
        } else {
            0.0 // Show 0.0 if the threshold isn't met or no interactions
        }

        val gpmMessage = "You are battling %.2f grunts per minute (stay below 1)".format(gpm)

            gruntsPerMinuteTextView.text = gpmMessage

    }



    override fun onDestroy() {
        super.onDestroy()
        stopTrackingGPM() // Stop the coroutine when the activity is destroyed
    }


                        data class FavouriteLocation(var name: String, var coordinates: String)


    private fun saveLocations() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        val json = gson.toJson(storedFavourites)
        editor.putString("favourites", json)
        val isSuccess = editor.commit() // Use commit() to ensure saving is synchronous
        if (isSuccess) {
            Log.d("Save Location", "Locations saved successfully")
        } else {
            Log.e("Save Location", "Failed to save locations")
        }
    }



    private fun loadLocations() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE) // Consistent name
        val gson = Gson()
        val json = sharedPreferences.getString("favourites", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<FavouriteLocation>>() {}.type
            storedFavourites = gson.fromJson(json, type)
            Log.d("Load Location", "Locations loaded successfully: $storedFavourites")
        } else {
            Log.d("Load Location", "No saved locations found")
            storedFavourites = mutableListOf() // Initialize as empty list if no data
        }
    }

    private fun saveImport() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        val json = gson.toJson(storedFavourites)
        editor.putString("import", json)
        val isSuccess = editor.commit() // Use commit() to ensure saving is synchronous
        if (isSuccess) {
            Log.d("Save Location", "Import/Export saved successfully")
        } else {
            Log.e("Save Location", "Failed to save import/export")
        }
    }


    private fun loadImport() {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE) // Consistent name
        val gson = Gson()
        val json = sharedPreferences.getString("import", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<FavouriteLocation>>() {}.type
            storedFavourites = gson.fromJson(json, type)
            Log.d("Import Favorites", "Import loaded successfully: $storedFavourites")
        } else {
            Log.d("Import Favorites", "No import found")
            storedFavourites = mutableListOf()
        }
    }

    private fun updateUIWithFavourites() {
        val currentText = SpannableStringBuilder()
        val iterator = storedFavourites.iterator()

        while (iterator.hasNext()) {
            val location = iterator.next()
            val link = "https://ipogo.app/?coords=${location.coordinates}"
            val locationText = "Location: ${location.name}\nCoords: ${location.coordinates} \nActions: Teleport | Delete | Edit \n\n"
            val spannableString = SpannableString(locationText)

            // Create clickable spans
            val teleportSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)
                }
            }

            val deleteSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    showDeleteFavoriteDialog(location)
                    updateUIWithFavourites() // Refresh UI
                }
            }

            val copySpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Copy coordinates to clipboard
                    val clipboard = widget.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Coordinates:", location.coordinates)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(widget.context, "Coordinates copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            }

            val editSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {

                    showEditFavoriteDialog(location)

                }
            }

            // Set spans
            val startTeleport = locationText.indexOf("Teleport")
            val endTeleport = startTeleport + "Teleport".length
            spannableString.setSpan(teleportSpan, startTeleport, endTeleport, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val startDelete = locationText.indexOf("Delete")
            val endDelete = startDelete + "Delete".length

            spannableString.setSpan(deleteSpan, startDelete, endDelete, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val startEdit = locationText.indexOf("Edit")
            val endEdit = startEdit + "Edit".length
            spannableString.setSpan(editSpan, startEdit, endEdit, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val startCoordinates = locationText.indexOf(location.coordinates)
            val endCoordinates = startCoordinates + location.coordinates.length
            spannableString.setSpan(copySpan, startCoordinates, endCoordinates, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            currentText.append(spannableString)
        }

        Log.d("TAG", "Setting text: $currentText")

        runOnUiThread {
            favoritesText.text = currentText
            favoritesText.movementMethod = LinkMovementMethod.getInstance()
        }

        Log.d("TAG", "Updated UI with favourites")
    }






    private fun addFavourites(locationName: String, coordinates: String) {
        val coordinatesRegex = """^-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?$""".toRegex()
        if (!coordinatesRegex.matches(coordinates)) {
            Log.d("TAG", "Incorrect coordinate format")
            Toast.makeText(this, "Incorrect coordinate format", Toast.LENGTH_SHORT).show()
            return
        }

        loadLocations()

        if (storedFavourites.any { it.name == locationName }) {
            Toast.makeText(this, "Duplicate or invalid location name found", Toast.LENGTH_SHORT).show()
            return
        }

        if (storedFavourites.any { it.coordinates == coordinates }) {
            Toast.makeText(this, "Duplicate coordinates found", Toast.LENGTH_SHORT).show()
            return
        }

        placeAccepted = true
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()

        val newFavourite = FavouriteLocation(locationName, coordinates)
        storedFavourites.add(newFavourite)
        saveLocations()
        updateUIWithFavourites()
    }

    private fun printJournal() {
        if (visitedInvasions.isNotEmpty()) {
            Log.d("TAG","Journal:")
            for (invasion in visitedInvasions) {
                Log.d("TAG", "Pokestop Name: ${invasion.name} " +
                        "Type: ${invasion.characterName} " +
                        "Coordinates: ${invasion.lat}, ${invasion.lng}")
            }
        } else {
            Log.d("TAG", "No stored invasions!!")
        }
    }



    private fun handleFailure(exception: Exception) {
        resultTextView.text = getString(R.string.api_call_failed, exception.message)
    }
    private fun sortInvasionsByEndTime(invasions: List<Invasion>): List<Invasion> {
        return invasions.sortedByDescending { it.invasion_end }
    }


    private fun exportFavourites() {




    }



}
