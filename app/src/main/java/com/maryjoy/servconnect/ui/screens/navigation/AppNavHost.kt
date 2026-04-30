package com.maryjoy.servconnect.ui.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.screens.shared.splash.SplashScreen


@Composable
fun AppNavHost(modifier: Modifier = Modifier, navController:NavHostController = rememberNavController(), startDestination:String = ROUT_SPLASH) {
    NavHost(navController = navController, modifier=modifier, startDestination = startDestination){
        composable(ROUT_SPLASH){
            SplashScreen(navController)
        }
        composable(ROUT_LOGIN){
            // LoginScreen(navController)
        }
    }
}