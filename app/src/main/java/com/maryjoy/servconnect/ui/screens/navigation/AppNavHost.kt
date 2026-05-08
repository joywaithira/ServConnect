package com.maryjoy.servconnect.ui.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maryjoy.servconnect.ui.screens.auth.VolunteerRegisterScreen
import com.maryjoy.servconnect.ui.screens.organization.IssueCertificateScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import android.widget.Toast
import android.util.Log
import com.maryjoy.servconnect.ui.screens.organization.ManageBookingsScreen
import com.maryjoy.servconnect.ui.screens.organization.MyOpportunitiesScreen
import com.maryjoy.servconnect.ui.screens.organization.NotificationsScreen as OrgNotificationsScreen
import com.maryjoy.servconnect.ui.screens.organization.OrgOpportunityDetailScreen
import com.maryjoy.servconnect.ui.screens.organization.OrgMessagesScreen
import com.maryjoy.servconnect.ui.screens.organization.OrgProfileScreen
import com.maryjoy.servconnect.ui.screens.organization.OrgDashboardScreen
import com.maryjoy.servconnect.ui.screens.organization.PostOpportunityScreen
import com.maryjoy.servconnect.ui.screens.organization.QRCodeScreen
import com.maryjoy.servconnect.ui.screens.organization.VolunteerProfileViewScreen
import com.maryjoy.servconnect.ui.screens.pendingverifications.PendingVerificationScreen
import com.maryjoy.servconnect.ui.screens.shared.auth.LoginScreen
import com.maryjoy.servconnect.ui.screens.shared.auth.OrgRegisterScreen
import com.maryjoy.servconnect.ui.screens.shared.auth.RegisterSelectionScreen
import com.maryjoy.servconnect.ui.screens.shared.splash.SplashScreen
import com.maryjoy.servconnect.ui.screens.users.ActivityHistoryScreen
import com.maryjoy.servconnect.ui.screens.users.ChatDetailScreen
import com.maryjoy.servconnect.ui.screens.users.MessagesScreen
import com.maryjoy.servconnect.ui.screens.users.MyProfileScreen
import com.maryjoy.servconnect.ui.screens.users.OrganizationProfileViewScreen
import com.maryjoy.servconnect.ui.screens.users.ServicePortfolioScreen
import com.maryjoy.servconnect.ui.screens.users.SettingsScreen
import com.maryjoy.servconnect.ui.screens.volunteer.MyBookingsScreen
import com.maryjoy.servconnect.ui.screens.volunteer.QrCheckInScreen
import com.maryjoy.servconnect.ui.screens.users.ExploreScreen
import com.maryjoy.servconnect.ui.screens.users.OpportunityDetailScreen
import com.maryjoy.servconnect.ui.screens.users.PickInterestScreen
import com.maryjoy.servconnect.ui.screens.users.VolunteerHomeScreen
import com.maryjoy.servconnect.ui.screens.users.NotificationsScreen as VolunteerNotificationsScreen


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {

        // --- Auth & Onboarding ---

        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToNext = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.RegisterSelection.route)
                },
                onLoginSuccess = { isOrg ->
                    if (isOrg) {
                        // In real app, check if verified. Here assuming verified for flow
                        navController.navigate(Screen.OrgDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.PickInterest.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = Screen.RegisterSelection.route) {
            RegisterSelectionScreen(
                onNavigateBack = { navController.popBackStack() },
                onVolunteerSelected = { navController.navigate(Screen.VolunteerRegister.route) },
                onOrgSelected = { navController.navigate(Screen.OrgRegister.route) }
            )
        }

        composable(route = Screen.OrgRegister.route) {
            OrgRegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.OrgRegister.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.VolunteerRegister.route) {
            VolunteerRegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.VolunteerRegister.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.PendingVerification.route) {
            PendingVerificationScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.PendingVerification.route) { inclusive = true }
                    }
                },
                onContactSupport = { /* Handle support */ }
            )
        }

        composable(route = Screen.PickInterest.route) {
            PickInterestScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PickInterest.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Organization Path ---

        composable(route = Screen.OrgDashboard.route) {
            OrgDashboardScreen(
                isVerified = true,
                onNavigateToPostOpportunity = { navController.navigate(Screen.PostOpportunity.route) },
                onNavigateToManageBookings = { navController.navigate(Screen.ManageBookings.createRoute("all")) },
                onNavigateToProfile = { navController.navigate(Screen.OrgProfile.route) },
                onNavigateToMessages = { navController.navigate(Screen.Messages.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToMyOpportunities = { navController.navigate(Screen.MyOpportunities.route) }
            )
        }

        composable(route = Screen.PostOpportunity.route) {
            PostOpportunityScreen(
                onNavigateBack = { navController.popBackStack() },
                onPostSuccess = { navController.navigate(Screen.OrgDashboard.route) }
            )
        }

        composable(route = Screen.MyOpportunities.route) {
            MyOpportunitiesScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpportunityClick = { id -> 
                    navController.navigate(Screen.OrgOpportunityDetail.createRoute(id)) 
                }
            )
        }

        composable(
            route = Screen.OrgOpportunityDetail.route,
            arguments = listOf(navArgument("opportunityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("opportunityId") ?: ""
            OrgOpportunityDetailScreen(
                opportunityId = id,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { /* Handle edit */ },
                onManageBookingsClick = { 
                    navController.navigate(Screen.ManageBookings.createRoute(id)) 
                },
                onViewQrCodeClick = {
                    navController.navigate(Screen.QrCode.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.ManageBookings.route,
            arguments = listOf(navArgument("opportunityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("opportunityId") ?: ""
            ManageBookingsScreen(
                opportunityId = id,
                onNavigateBack = { navController.popBackStack() },
                onVolunteerClick = { volunteerId -> 
                    navController.navigate(Screen.VolunteerProfileView.createRoute(volunteerId)) 
                }
            )
        }

        composable(
            route = Screen.QrCode.route,
            arguments = listOf(navArgument("opportunityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("opportunityId") ?: ""
            QRCodeScreen(
                onNavigateBack = { navController.popBackStack() },
                opportunityId = id
            )
        }

        composable(
            route = Screen.VolunteerProfileView.route,
            arguments = listOf(navArgument("volunteerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("volunteerId") ?: ""
            VolunteerProfileViewScreen(
                volunteerId = id,
                onNavigateBack = { navController.popBackStack() },
                onIssueCertificateClick = { volunteerName, activityTitle -> 
                    navController.navigate(Screen.IssueCertificate.createRoute(id, activityTitle)) 
                },
                onMessageClick = { volunteerName ->
                    navController.navigate(Screen.ChatDetail.createRoute(id, volunteerName))
                }
            )
        }

        composable(
            route = Screen.IssueCertificate.route,
            arguments = listOf(
                navArgument("volunteerId") { type = NavType.StringType },
                navArgument("activityTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val volunteerId = backStackEntry.arguments?.getString("volunteerId") ?: ""
            val activityTitle = backStackEntry.arguments?.getString("activityTitle") ?: ""
            val context = androidx.compose.ui.platform.LocalContext.current
            IssueCertificateScreen(
                volunteerName = "Volunteer", // In real app, fetch from ID
                activityTitle = activityTitle,
                onNavigateBack = { navController.popBackStack() },
                onIssueSuccess = { navController.popBackStack() },
                onIssueCertificate = { hours: Int, remarks: String ->
                    val auth = FirebaseAuth.getInstance()
                    val database = FirebaseDatabase.getInstance()
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val organizationId = currentUser.uid
                        val certificateId = database.getReference("certificates").push().key

                        if (certificateId != null) {
                            val certificateData = hashMapOf(
                                "certificateId" to certificateId,
                                "volunteerName" to "Volunteer", // Replace with actual name if available
                                "activityTitle" to activityTitle,
                                "hours" to hours,
                                "remarks" to remarks,
                                "organizationId" to organizationId,
                                "issueDate" to ServerValue.TIMESTAMP
                            )

                            database.getReference("certificates").child(certificateId).setValue(certificateData)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Certificate issued successfully!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Failed to issue certificate: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                        Log.e("IssueCertificateScreen", "Failed to issue certificate", task.exception)
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Failed to generate certificate ID.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "You need to be logged in as an organization to issue a certificate.", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        composable(route = Screen.OrgProfile.route) {
            OrgProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        // --- Volunteer Path ---

        composable(route = Screen.Home.route) {
            VolunteerHomeScreen(
                onNavigateToDetails = { id ->
                    navController.navigate(Screen.OpportunityDetail.createRoute(id))
                },
                onNavigateToQrCheckIn = {
                    navController.navigate(Screen.QrCheckIn.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.MyProfile.route)
                },
                onNavigateToExplore = {
                    navController.navigate(Screen.Explore.route)
                }
            )
        }

        composable(route = Screen.Explore.route) {
            ExploreScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpportunityClick = { id ->
                    navController.navigate(Screen.OpportunityDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.OpportunityDetail.route,
            arguments = listOf(navArgument("opportunityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("opportunityId") ?: ""
            OpportunityDetailScreen(
                opportunityId = id,
                onNavigateBack = { navController.popBackStack() },
                onBookingSuccess = {
                    navController.navigate(Screen.MyBookings.route)
                },
                onViewOrgProfile = { orgId ->
                    navController.navigate(Screen.OrgProfileView.createRoute(orgId))
                },
                onMessageClick = { orgId, orgName ->
                    navController.navigate(Screen.ChatDetail.createRoute(orgId, orgName))
                }
            )
        }

        composable(route = Screen.MyBookings.route) {
            MyBookingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQrCheckIn = {
                    navController.navigate(Screen.QrCheckIn.route)
                }
            )
        }

        composable(route = Screen.QrCheckIn.route) {
            QrCheckInScreen(
                onNavigateBack = { navController.popBackStack() },
                onCheckInSuccess = {
                    // Navigate to history or home
                    navController.navigate(Screen.ActivityHistory.route)
                }
            )
        }

        composable(route = Screen.MyProfile.route) {
            MyProfileScreen(
                onNavigateToPortfolio = {
                    navController.navigate(Screen.ServicePortfolio.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.ActivityHistory.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.ServicePortfolio.route) {
            ServicePortfolioScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = {
                    navController.navigate(Screen.ActivityHistory.route)
                }
            )
        }

        composable(route = Screen.ActivityHistory.route) {
            ActivityHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onOrgClick = { orgId ->
                    navController.navigate(Screen.OrgProfileView.createRoute(orgId))
                }
            )
        }

        composable(
            route = Screen.OrgProfileView.route,
            arguments = listOf(navArgument("orgId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("orgId") ?: ""
            OrganizationProfileViewScreen(
                orgId = id,
                onNavigateBack = { navController.popBackStack() },
                onOpportunityClick = { oppId ->
                    navController.navigate(Screen.OpportunityDetail.createRoute(oppId))
                },
                onMessageClick = { orgName ->
                    navController.navigate(Screen.ChatDetail.createRoute(id, orgName))
                }
            )
        }

        // --- Shared ---

        composable(route = Screen.Messages.route) {
            MessagesScreen(
                onNavigateBack = { navController.popBackStack() },
                onConversationClick = { orgId, orgName ->
                    navController.navigate(Screen.ChatDetail.createRoute(orgId, orgName))
                }
            )
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("orgId") { type = NavType.StringType },
                navArgument("orgName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orgName = backStackEntry.arguments?.getString("orgName") ?: "Chat"
            ChatDetailScreen(
                organizationName = orgName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Notifications.route) {
            // This could be a shared screen or different based on role
            // Using a simple check or separate screens if needed
            VolunteerNotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
