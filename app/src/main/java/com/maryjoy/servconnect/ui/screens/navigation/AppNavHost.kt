package com.maryjoy.servconnect.ui.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.screens.auth.VolunteerRegisterScreen
import com.maryjoy.servconnect.ui.screens.home.HomeScreen
import com.maryjoy.servconnect.ui.screens.organization.IssueCertificateScreen
import com.maryjoy.servconnect.ui.screens.organization.ManageBookingsScreen
import com.maryjoy.servconnect.ui.screens.organization.MyOpportunitiesScreen
import com.maryjoy.servconnect.ui.screens.organization.NotificationsScreen
import com.maryjoy.servconnect.ui.screens.organization.OpportunityDetailScreen
import com.maryjoy.servconnect.ui.screens.organization.OrgDashboardScreen
import com.maryjoy.servconnect.ui.screens.organization.PostOpportunityScreen
import com.maryjoy.servconnect.ui.screens.organization.QRCodeScreen
import com.maryjoy.servconnect.ui.screens.organization.VolunteerProfileViewScreen
import com.maryjoy.servconnect.ui.screens.pendingverifications.PendingVerificationScreen
import com.maryjoy.servconnect.ui.screens.shared.auth.LoginScreen
import com.maryjoy.servconnect.ui.screens.shared.auth.OrgRegisterScreen
import com.maryjoy.servconnect.ui.screens.shared.splash.SplashScreen


@Composable
fun AppNavHost(modifier: Modifier = Modifier, navController:NavHostController = rememberNavController(), startDestination:String = ROUT_SPLASH) {
    NavHost(navController = navController, modifier=modifier, startDestination = startDestination){
        composable(ROUT_SPLASH){
            SplashScreen(navController)
        }
        composable(ROUT_HOME){
            HomeScreen(navController)
        }
        composable(ROUT_REGISTER_ORGANIZATION) {
            OrgRegisterScreen(navController = navController)
        }
        composable(ROUT_REGISTER_VOLUNTEER) {
            VolunteerRegisterScreen(navController = navController)
        }
        composable(ROUT_LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(ROUT_PENDING_VERIFICATION) {
            PendingVerificationScreen(navController = navController)
        }
        composable(ROUT_ORG_HOME) {
            OrgDashboardScreen(navController = navController)
        }
        composable(ROUT_POST_OPPORTUNITY) {
            PostOpportunityScreen(navController = navController)
        }
        composable(ROUT_MY_OPPORTUNITIES) {
            MyOpportunitiesScreen(navController = navController)
        }
        composable("$ROUT_OPPORTUNITY_DETAIL/{opportunityId}") { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId") ?: ""
            OpportunityDetailScreen(
                navController  = navController,
                opportunityId  = opportunityId
            )
        }
        // In your NavHost — supports both with and without opportunityId:
        composable(ROUT_MANAGE_BOOKINGS) {
            ManageBookingsScreen(navController = navController)
        }
        composable("$ROUT_MANAGE_BOOKINGS/{opportunityId}") { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId")
            ManageBookingsScreen(
                navController  = navController,
                opportunityId  = opportunityId
            )
        }

        composable("$ROUT_QR_CODE/{opportunityId}") { backStackEntry ->
            val opportunityId = backStackEntry.arguments
                ?.getString("opportunityId") ?: ""
            QRCodeScreen(
                navController = navController,
                opportunityId = opportunityId
            )
        }

        composable("$ROUT_VOLUNTEER_PROFILE/{volunteerId}") { backStackEntry ->
            val volunteerId = backStackEntry.arguments
                ?.getString("volunteerId") ?: ""
            VolunteerProfileViewScreen(
                navController = navController,
                volunteerId   = volunteerId
            )
        }

        composable("$ROUT_ISSUE_CERTIFICATE/{opportunityId}") { backStackEntry ->
            val opportunityId = backStackEntry.arguments
                ?.getString("opportunityId") ?: ""
            IssueCertificateScreen(
                navController = navController,
                opportunityId = opportunityId
            )
        }


        composable(ROUT_ORG_NOTIFICATIONS) {
            NotificationsScreen(navController = navController)
        }









    }
}