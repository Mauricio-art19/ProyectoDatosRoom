package com.materiagris.proyectodatosroom

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.materiagris.proyectodatosroom.ui.theme.ProyectoDatosRoomTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/******************** MAIN ACTIVITY ********************/

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProyectoDatosRoomTheme {
                val viewModel = ViewModelProvider(
                    this,
                    ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )[WikiViewModel::class.java]

                MainScreen(viewModel)
            }
        }
    }
}

/******************** ESTRUCTURA DE NAVEGACION ********************/

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Inicio : Screen("inicio", "Inicio", Icons.Default.Home)
    object Juegos : Screen("juegos", "Juegos", Icons.Default.Gamepad)
    object Consolas : Screen("consolas", "Consolas", Icons.Default.VideogameAsset)
    object Agregar : Screen("agregar", "Agregar", Icons.Default.AddCircle)

    companion object {
        const val EDITAR_JUEGO = "editar_juego/{id}"
        const val EDITAR_CONSOLA = "editar_consola/{id}"

        fun editarJuego(id: Int) = "editar_juego/$id"
        fun editarConsola(id: Int) = "editar_consola/$id"
    }
}

@Composable
fun MainScreen(vm: WikiViewModel) {
    val nav = rememberNavController()
    val current = nav.currentBackStackEntryAsState().value?.destination?.route

    val screens = listOf(Screen.Inicio, Screen.Juegos, Screen.Consolas, Screen.Agregar)

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { s ->
                    NavigationBarItem(
                        icon = { Icon(s.icon, contentDescription = s.label) },
                        label = { Text(s.label) },
                        selected = current == s.route,
                        onClick = {
                            nav.navigate(s.route) {
                                popUpTo(nav.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Screen.Inicio.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Inicio.route) { InicioScreen(vm, nav) }
            composable(Screen.Juegos.route) { JuegosScreen(vm, nav) }
            composable(Screen.Consolas.route) { ConsolasScreen(vm, nav) }
            composable(Screen.Agregar.route) { AgregarScreen(vm, nav) }

            composable(
                route = Screen.EDITAR_JUEGO,
                arguments = listOf(navArgument("id") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id")
                val juego = id?.let { vm.juegos.find { it.id == id } }
                if (juego != null) {
                    EditarJuegoScreen(vm, nav, juego)
                }
            }

            composable(
                route = Screen.EDITAR_CONSOLA,
                arguments = listOf(navArgument("id") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id")
                val consola = id?.let { vm.consolas.find { it.id == id } }
                if (consola != null) {
                    EditarConsolaScreen(vm, nav, consola)
                }
            }
        }
    }
}

/******************** PANTALLAS PRINCIPALES ********************/

@Composable
fun InicioScreen(vm: WikiViewModel, navController: NavHostController) {
    // DISEÑO MEJORADO: Estilo Dashboard
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "WikiGames",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Tu enciclopedia personal",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Tarjeta de estadísticas
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Colección",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${vm.juegos.size + vm.consolas.size}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("artículos registrados")
            }
        }
    }
}

@Composable
fun JuegosScreen(vm: WikiViewModel, navController: NavHostController) {
    Column(Modifier.padding(16.dp)) {
        Text("Videojuegos", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vm.juegos) { j ->
                JuegoCard(
                    juego = j,
                    onEdit = { navController.navigate(Screen.editarJuego(j.id)) },
                    onDelete = { vm.deleteJuego(j) }
                )
            }
        }
    }
}

@Composable
fun ConsolasScreen(vm: WikiViewModel, navController: NavHostController) {
    Column(Modifier.padding(16.dp)) {
        Text("Consolas", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vm.consolas) { c ->
                ConsolaCard(
                    consola = c,
                    onEdit = { navController.navigate(Screen.editarConsola(c.id)) },
                    onDelete = { vm.deleteConsola(c) }
                )
            }
        }
    }
}

/******************** COMPONENTES UI (TARJETAS) ********************/

@Composable
fun JuegoCard(juego: Juego, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            juego.imagenUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = juego.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }
            Text("${juego.nombre} (${juego.anio})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("Género: ${juego.genero} • ${juego.descripcion}", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun ConsolaCard(consola: Consola, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            consola.imagenUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = consola.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }
            Text("${consola.nombre} (${consola.anioLanzamiento})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("${consola.fabricante} • ${consola.descripcion}", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

/******************** PANTALLA AGREGAR (DISEÑO MEJORADO) ********************/

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AgregarScreen(vm: WikiViewModel, navController: NavHostController) {
    val ctx = LocalContext.current

    // Estados
    var tipo by remember { mutableStateOf("Juego") }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var espec1 by remember { mutableStateOf("") }
    var espec2 by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    // Permisos e Imagen
    val permiso = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    val permState = rememberPermissionState(permiso)
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imagenUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Nuevo Registro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 1. SELECTOR TIPO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Juego", "Consola").forEach { op ->
                val selected = tipo == op
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { tipo = op }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = op,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // 2. CAMPOS DE TEXTO
        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Label, null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") },
            leadingIcon = { Icon(Icons.Default.Description, null) },
            modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = anio, onValueChange = { anio = it },
            label = { Text(if (tipo == "Juego") "Año Lanzamiento" else "Año Salida") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = espec1, onValueChange = { espec1 = it },
                label = { Text(if (tipo == "Juego") "Género" else "Fabricante") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = espec2, onValueChange = { espec2 = it },
                label = { Text(if (tipo == "Juego") "Desarrollador" else "Generación") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // 3. IMAGEN
        Text("Portada / Imagen", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable {
                    if (permState.status.isGranted) imagePicker.launch("image/*")
                    else permState.launchPermissionRequest()
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (imagenUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Toca para subir imagen", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    AsyncImage(
                        model = imagenUri, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // 4. GUARDAR
        Button(
            onClick = {
                if (nombre.isBlank() || descripcion.isBlank() || imagenUri == null) {
                    Toast.makeText(ctx, "Faltan datos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (tipo == "Juego") {
                    vm.addJuego(Juego(nombre = nombre, descripcion = descripcion, anio = anio, genero = espec1, desarrollador = espec2, imagenUri = imagenUri.toString()))
                } else {
                    vm.addConsola(Consola(nombre = nombre, descripcion = descripcion, anioLanzamiento = anio, fabricante = espec1, generacion = espec2, imagenUri = imagenUri.toString()))
                }
                nombre = ""; descripcion = ""; anio = ""; espec1 = ""; espec2 = ""; imagenUri = null
                Toast.makeText(ctx, "Guardado correctamente", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Guardar Registro", fontSize = 18.sp)
        }
        Spacer(Modifier.height(50.dp))
    }
}

/******************** PANTALLAS DE EDICIÓN (DISEÑO MEJORADO) ********************/

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditarJuegoScreen(vm: WikiViewModel, navController: NavHostController, juego: Juego) {
    val ctx = LocalContext.current
    var nombre by remember { mutableStateOf(juego.nombre) }
    var descripcion by remember { mutableStateOf(juego.descripcion) }
    var anio by remember { mutableStateOf(juego.anio) }
    var genero by remember { mutableStateOf(juego.genero) }
    var desarrollador by remember { mutableStateOf(juego.desarrollador) }
    var imagenUri by remember { mutableStateOf<Uri?>(juego.imagenUri?.let { Uri.parse(it) }) }

    val permiso = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    val permState = rememberPermissionState(permiso)
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imagenUri = uri }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Editar Juego", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripcion") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = anio, onValueChange = { anio = it }, label = { Text("Año") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = genero, onValueChange = { genero = it }, label = { Text("Genero") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = desarrollador, onValueChange = { desarrollador = it }, label = { Text("Desarrollador") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }
        Spacer(Modifier.height(16.dp))

        // Imagen
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp).clickable { if (permState.status.isGranted) imagePicker.launch("image/*") else permState.launchPermissionRequest() },
            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (imagenUri == null) Text("Elegir imagen") else AsyncImage(model = imagenUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Cancelar") }
            Button(
                onClick = {
                    if (nombre.isBlank() || descripcion.isBlank() || imagenUri == null) {
                        Toast.makeText(ctx, "Faltan datos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    vm.updateJuego(juego.copy(nombre = nombre, descripcion = descripcion, anio = anio, genero = genero, desarrollador = desarrollador, imagenUri = imagenUri.toString()))
                    Toast.makeText(ctx, "Actualizado", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)
            ) { Text("Guardar") }
        }
        Spacer(Modifier.height(50.dp))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditarConsolaScreen(vm: WikiViewModel, navController: NavHostController, consola: Consola) {
    val ctx = LocalContext.current
    var nombre by remember { mutableStateOf(consola.nombre) }
    var descripcion by remember { mutableStateOf(consola.descripcion) }
    var anioLanzamiento by remember { mutableStateOf(consola.anioLanzamiento) }
    var fabricante by remember { mutableStateOf(consola.fabricante) }
    var generacion by remember { mutableStateOf(consola.generacion) }
    var imagenUri by remember { mutableStateOf<Uri?>(consola.imagenUri?.let { Uri.parse(it) }) }

    val permiso = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    val permState = rememberPermissionState(permiso)
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imagenUri = uri }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Editar Consola", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripcion") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = anioLanzamiento, onValueChange = { anioLanzamiento = it }, label = { Text("Año") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = fabricante, onValueChange = { fabricante = it }, label = { Text("Fabricante") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = generacion, onValueChange = { generacion = it }, label = { Text("Generación") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }
        Spacer(Modifier.height(16.dp))

        // Imagen
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp).clickable { if (permState.status.isGranted) imagePicker.launch("image/*") else permState.launchPermissionRequest() },
            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (imagenUri == null) Text("Elegir imagen") else AsyncImage(model = imagenUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Cancelar") }
            Button(
                onClick = {
                    if (nombre.isBlank() || descripcion.isBlank() || imagenUri == null) {
                        Toast.makeText(ctx, "Faltan datos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    vm.updateConsola(consola.copy(nombre = nombre, descripcion = descripcion, anioLanzamiento = anioLanzamiento, fabricante = fabricante, generacion = generacion, imagenUri = imagenUri.toString()))
                    Toast.makeText(ctx, "Actualizado", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)
            ) { Text("Guardar") }
        }
        Spacer(Modifier.height(50.dp))
    }
}