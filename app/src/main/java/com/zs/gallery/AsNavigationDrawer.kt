package com.zs.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.zs.gallery.common.Route
import com.zs.gallery.files.RouteFiles
import com.zs.gallery.folders.RouteFolders
import com.zs.gallery.settings.RouteSettings
import com.zs.gallery.settings.Settings
import kotlinx.coroutines.launch

private const val PROFILE_PREFS = "as_gallery_profile"
private const val PROFILE_NAME = "display_name"
private const val PROFILE_AVATAR = "avatar_uri"

/**
 * Drawer مشترک AS Gallery.
 *
 * جهت بیرونی عمداً RTL است تا Drawer از سمت راست باز شود؛ محتوای واقعی برنامه و خود Sheet
 * دوباره به جهت زبان سیستم برگردانده می‌شوند تا انگلیسی/فارسی هر دو درست نمایش داده شوند.
 */
@Composable
fun AsNavigationDrawer(
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val originalDirection = LocalLayoutDirection.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // stringResource در Composition خوانده می‌شود تا تغییر زبان/Configuration فوراً اعمال شود.
    val shareLabel = stringResource(R.string.share_app_label)
    val prefs = remember(context) {
        context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
    }

    var displayName by rememberSaveable {
        mutableStateOf(prefs.getString(PROFILE_NAME, null).orEmpty())
    }
    var avatarUri by rememberSaveable {
        mutableStateOf(prefs.getString(PROFILE_AVATAR, null).orEmpty())
    }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        avatarUri = uri.toString()
        prefs.edit().putString(PROFILE_AVATAR, avatarUri).apply()
    }

    fun closeThen(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    fun navigate(route: Route) = closeThen {
        navController.navigate(route()) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides originalDirection) {
                    ModalDrawerSheet(
                        modifier = Modifier.widthIn(max = 340.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = stringResource(R.string.developed_by_as_team),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            AsyncImage(
                                model = avatarUri.ifBlank { R.drawable.ic_launcher_foreground },
                                contentDescription = stringResource(R.string.change_profile_photo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .clickable { avatarPicker.launch(arrayOf("image/*")) }
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = {
                                    displayName = it.take(40)
                                    prefs.edit().putString(PROFILE_NAME, displayName).apply()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text(stringResource(R.string.profile_name)) },
                                placeholder = { Text(stringResource(R.string.profile_default_name)) }
                            )
                        }

                        HorizontalDivider()

                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.home)) },
                            selected = false,
                            icon = { Icon(Icons.Default.Home, null) },
                            onClick = { navigate(RouteFiles) }
                        )
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.photos)) },
                            selected = false,
                            icon = { Icon(Icons.Default.PhotoLibrary, null) },
                            onClick = { navigate(RouteFiles) }
                        )
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.folders)) },
                            selected = false,
                            icon = { Icon(Icons.Default.FolderCopy, null) },
                            onClick = { navigate(RouteFolders) }
                        )
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.settings)) },
                            selected = false,
                            icon = { Icon(Icons.Default.Settings, null) },
                            onClick = { navigate(RouteSettings) }
                        )
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.share_app_label)) },
                            selected = false,
                            icon = { Icon(Icons.Default.Share, null) },
                            onClick = {
                                closeThen {
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Settings.ShareAppIntent),
                                            shareLabel
                                        )
                                    )
                                }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.about_gallery)) },
                            selected = false,
                            icon = { Icon(Icons.Default.Info, null) },
                            onClick = { navigate(RouteSettings) }
                        )
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.contact_us)) },
                            selected = false,
                            icon = { Icon(Icons.AutoMirrored.Filled.ContactSupport, null) },
                            onClick = {
                                closeThen { context.startActivity(Intent(Settings.FeedbackIntent)) }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.exit)) },
                            selected = false,
                            icon = { Icon(Icons.Default.Close, null) },
                            onClick = { closeThen { activity?.finishAffinity() } }
                        )
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides originalDirection) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.menu)
                        )
                    }
                }
            }
        }
    }
}
