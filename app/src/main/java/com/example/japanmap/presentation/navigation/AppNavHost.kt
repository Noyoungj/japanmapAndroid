package com.example.japanmap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.japanmap.di.AppContainer
import com.example.japanmap.presentation.scene.detail.PrefectureDetailScreen
import com.example.japanmap.presentation.scene.editor.TripEditorScreen
import com.example.japanmap.presentation.scene.gallery.PhotoGalleryScreen
import com.example.japanmap.presentation.scene.main.MainScreen

/**
 * 앱 네비게이션 그래프. iOS Coordinator 계층 대응.
 * main → detail/{id} → editor/{id} , detail → gallery/{tripId}/{startIndex}
 */
object Routes {
    const val MAIN = "main"
    const val DETAIL = "detail/{prefectureId}"
    const val EDITOR = "editor/{prefectureId}"
    const val GALLERY = "gallery/{tripId}/{startIndex}"

    fun detail(id: Int) = "detail/$id"
    fun editor(id: Int) = "editor/$id"
    fun gallery(tripId: String, startIndex: Int) = "gallery/$tripId/$startIndex"
}

@Composable
fun AppNavHost(container: AppContainer) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.MAIN) {

        composable(Routes.MAIN) {
            MainScreen(
                container = container,
                onPrefectureSelected = { id -> nav.navigate(Routes.detail(id)) },
            )
        }

        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("prefectureId") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("prefectureId") ?: return@composable
            PrefectureDetailScreen(
                container = container,
                prefectureID = id,
                onBack = { nav.popBackStack() },
                onAddTrip = { nav.navigate(Routes.editor(id)) },
                onOpenGallery = { tripId, startIndex -> nav.navigate(Routes.gallery(tripId, startIndex)) },
            )
        }

        composable(
            Routes.EDITOR,
            arguments = listOf(navArgument("prefectureId") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("prefectureId") ?: return@composable
            TripEditorScreen(
                container = container,
                prefectureID = id,
                onBack = { nav.popBackStack() },
                onSaved = { nav.popBackStack() },
            )
        }

        composable(
            Routes.GALLERY,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("startIndex") { type = NavType.IntType },
            ),
        ) { entry ->
            PhotoGalleryScreen(
                container = container,
                tripId = entry.arguments?.getString("tripId") ?: "",
                startIndex = entry.arguments?.getInt("startIndex") ?: 0,
                onClose = { nav.popBackStack() },
            )
        }
    }
}
