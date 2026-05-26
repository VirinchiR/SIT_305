package com.example.lostfoundapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sealed class representing screens in our application.
 * Using a state-based approach makes navigation simple, visual,
 * and 100% free of external library version mismatch errors.
 */
sealed class Screen {
    object Home : Screen()
    object CreateAdvert : Screen()
    object ListAdverts : Screen()
    data class Detail(val itemId: Int) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6200EE),
                    secondary = Color(0xFF03DAC6),
                    tertiary = Color(0xFF3700B3),
                    background = Color(0xFFF6F8FB),
                    surface = Color(0xFFFFFFFF)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Helper function to decode an image from a URI string into a Bitmap.
 * Using standard Android APIs (ImageDecoder on Pie+, BitmapFactory on older),
 * this handles local content URIs safely with zero external dependencies.
 */
fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
    if (uriString.isEmpty()) return null
    return try {
        val uri = Uri.parse(uriString)
        // Persist content permission if possible to prevent security exceptions on reload
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            // Ignore security permission failures for standard file picker intents
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * A beautiful, reusable Composable that displays a locally selected image
 * from a URI string, or shows a custom placeholder if no image exists.
 */
@Composable
fun LocalImageLoader(uriString: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(uriString) {
        loadBitmapFromUri(context, uriString)
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Advert Image Preview",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📸 No Image Selected",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Root Composable managing state-based navigation and database instance.
 */
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val dbHelper = remember { LostFoundDatabaseHelper(context) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    when (val screen = currentScreen) {
        is Screen.Home -> HomeScreen(
            onNavigateToCreate = { currentScreen = Screen.CreateAdvert },
            onNavigateToList = { currentScreen = Screen.ListAdverts }
        )
        is Screen.CreateAdvert -> CreateAdvertScreen(
            dbHelper = dbHelper,
            onNavigateBack = { currentScreen = Screen.Home },
            onSuccess = { currentScreen = Screen.ListAdverts }
        )
        is Screen.ListAdverts -> ListAdvertsScreen(
            dbHelper = dbHelper,
            onNavigateToDetail = { id -> currentScreen = Screen.Detail(id) },
            onNavigateBack = { currentScreen = Screen.Home }
        )
        is Screen.Detail -> DetailScreen(
            itemId = screen.itemId,
            dbHelper = dbHelper,
            onNavigateBack = { currentScreen = Screen.ListAdverts },
            onRemoved = { currentScreen = Screen.ListAdverts }
        )
    }
}

// ==========================================
// 1. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Hero Card / Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF6200EE), Color(0xFF3700B3))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔍",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lost & Found App",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Reconnecting people with their items",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // Action Buttons
        Button(
            onClick = onNavigateToCreate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Icon")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Advert", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToList,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
        ) {
            Text("Show All Lost & Found Items", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 2. CREATE ADVERT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdvertScreen(
    dbHelper: LostFoundDatabaseHelper,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current

    // Form Field States
    var postType by remember { mutableStateOf("LOST") } // Default to LOST
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") } // Default Category
    var imageUri by remember { mutableStateOf("") }

    val categories = listOf("Electronics", "Pets", "Wallets", "Keys", "Documents", "Other")
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Launcher for selecting an image from storage
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Advert", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Post Type Selection
                Text(
                    text = "Post Type",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Lost Button Chip
                    Button(
                        onClick = { postType = "LOST" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (postType == "LOST") Color(0xFFEF5350) else Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🔴 LOST",
                            color = if (postType == "LOST") Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Found Button Chip
                    Button(
                        onClick = { postType = "FOUND" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (postType == "FOUND") Color(0xFF66BB6A) else Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🟢 FOUND",
                            color = if (postType == "FOUND") Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Input Fields
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    placeholder = { Text("e.g. 0412345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Black leather wallet with student ID") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date of occurrence") },
                    placeholder = { Text("e.g. 23 May 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Building C Library") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Category Dropdown Selection
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Image Selection Block
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Advert Image (Required)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    LocalImageLoader(
                        uriString = imageUri,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Prompt
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        ) {
                            Text(
                                text = "Tap frame to select image 📸",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Validation Checks
                        if (name.isBlank() || phone.isBlank() || description.isBlank() ||
                            date.isBlank() || location.isBlank() || category.isBlank()
                        ) {
                            Toast.makeText(context, "⚠️ Please fill in all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (imageUri.isBlank()) {
                            Toast.makeText(context, "📸 Please upload an image for the post", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Generate Auto Timestamp
                        val timestampFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        val currentTimestamp = timestampFormatter.format(Date())

                        // Save details into SQLite
                        val newAdvert = LostFoundItem(
                            postType = postType,
                            name = name,
                            phone = phone,
                            description = description,
                            date = date,
                            location = location,
                            category = category,
                            imageUri = imageUri,
                            timestamp = currentTimestamp
                        )

                        val insertResult = dbHelper.insertAdvert(newAdvert)
                        if (insertResult > -1) {
                            Toast.makeText(context, "🎉 Advert saved successfully!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "❌ Error saving to database", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Save Advert", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==========================================
// 3. LIST SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListAdvertsScreen(
    dbHelper: LostFoundDatabaseHelper,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    var allItems by remember { mutableStateOf(listOf<LostFoundItem>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Load/Refresh data from DB when this screen is active
    LaunchedEffect(Unit) {
        allItems = dbHelper.getAllAdverts()
    }

    val categories = listOf("All", "Electronics", "Pets", "Wallets", "Keys", "Documents", "Other")

    // Filter Items dynamically in UI based on Search and Selected Category
    val filteredItems = allItems.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = item.description.contains(searchQuery, ignoreCase = true) ||
                item.location.contains(searchQuery, ignoreCase = true) ||
                item.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lost & Found Adverts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // Live Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search location or description") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection Row
            Text(text = "Categories", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid list showing posts
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No adverts found 🔍",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredItems) { item ->
                        AdvertCard(
                            item = item,
                            onClick = { onNavigateToDetail(item.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Beautifully designed item card representing a single Lost/Found post.
 */
@Composable
fun AdvertCard(
    item: LostFoundItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image Loader
            LocalImageLoader(
                uriString = item.imageUri,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Post Type Badge Tag
                val isLost = item.postType.equals("LOST", ignoreCase = true)
                Surface(
                    color = if (isLost) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isLost) "🔴 LOST" else "🟢 FOUND",
                        color = if (isLost) Color(0xFFC62828) else Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Item Name
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                // Category & Location Meta Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📁 ${item.category}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "📍 ${item.location}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Post Timestamp
                Text(
                    text = "Posted: ${item.timestamp}",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

// ==========================================
// 4. DETAIL SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: Int,
    dbHelper: LostFoundDatabaseHelper,
    onNavigateBack: () -> Unit,
    onRemoved: () -> Unit
) {
    val context = LocalContext.current
    val item = remember(itemId) { dbHelper.getAdvertById(itemId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advert Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Advert not found 😢", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Image Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        LocalImageLoader(
                            uriString = item.imageUri,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Core Advert Information
                item {
                    val isLost = item.postType.equals("LOST", ignoreCase = true)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge Tag
                        Surface(
                            color = if (isLost) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isLost) "🔴 LOST" else "🟢 FOUND",
                                color = if (isLost) Color(0xFFC62828) else Color(0xFF2E7D32),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Category Card Tag
                        Surface(
                            color = Color(0xFFF3E5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "📁 ${item.category}",
                                color = Color(0xFF7B1FA2),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Title / Item Name
                item {
                    Text(
                        text = item.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Formatted Details Panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailRow(label = "📞 Contact", value = item.phone)
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            DetailRow(label = "📅 Date Reported", value = item.date)
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            DetailRow(label = "📍 Location", value = item.location)
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            DetailRow(label = "⏰ Saved Time", value = item.timestamp)
                        }
                    }
                }

                // Description Box
                item {
                    Text(
                        text = "Item Description",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFAFAFA),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Text(
                            text = item.description,
                            modifier = Modifier.padding(16.dp),
                            color = Color.DarkGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Remove Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val deleted = dbHelper.deleteAdvert(item.id)
                            if (deleted) {
                                Toast.makeText(context, "✅ Advert removed successfully", Toast.LENGTH_SHORT).show()
                                onRemoved()
                            } else {
                                Toast.makeText(context, "❌ Error removing advert", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove Advert", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Reusable detail table row composable.
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
    }
}