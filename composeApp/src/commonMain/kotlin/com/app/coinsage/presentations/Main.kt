package com.app.coinsage.presentations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coinsage.composeapp.generated.resources.Res
import coinsage.composeapp.generated.resources.camera
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

class Main {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text(text = "Profile") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    selected = false,
                    onClick = {  }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Info") },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = false,
                    onClick = {  }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = false,
                    onClick = {  }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2D1B6B), Color(0xFFBBB3E5))
                    )
                )
            //.blur(16.dp) // Applying blur effect
            ,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(buildAnnotatedString {
                            withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append("Coin")
                            }
                            append(" Sage")
                        }, color = Color.White, textAlign = TextAlign.Center)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            BadgedBox(
                                badge = {
                                    //Badge()
                                }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2D1B6B))
                )
            },
//            bottomBar = {
//                BottomAppBar(containerColor = Color(0xFF2D1B6B)) {
//                    Text(
//                        "Bayar Koin Digital",
//                        color = MaterialTheme.colorScheme.onPrimary,
//                        style = MaterialTheme.typography.labelMedium,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//            },
            floatingActionButton = { FloatingActionButton(onClick = {},
                content = {Icon(
                    painter = painterResource(Res.drawable.camera),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )}) },
            floatingActionButtonPosition = FabPosition.Center,
            containerColor = Color(0xFF2D1B6B)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Column(modifier = Modifier.width(150.dp), verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                            Text("Upload", modifier = Modifier.padding(8.dp))
                        }
                        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                            Text("Chat", modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}