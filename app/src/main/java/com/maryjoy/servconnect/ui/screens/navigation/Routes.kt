package com.maryjoy.servconnect.ui.screens.navigation

sealed class Screen(val route: String) {
    // Auth & Onboarding
    object Splash : Screen("splash")
    object Login : Screen("login")
    object RegisterSelection : Screen("register_selection")
    object VolunteerRegister : Screen("volunteer_register")
    object OrgRegister : Screen("org_register")
    object PendingVerification : Screen("pending_verification")
    object PickInterest : Screen("pick_interest")

    // Volunteer Path
    object Home : Screen("home")
    object Explore : Screen("explore")
    object MyBookings : Screen("my_bookings")
    object QrCheckIn : Screen("qr_check_in")
    object MyProfile : Screen("my_profile")
    object ServicePortfolio : Screen("service_portfolio")
    object ActivityHistory : Screen("activity_history")
    object Settings : Screen("settings")
    object OpportunityDetail : Screen("opportunity_detail/{opportunityId}") {
        fun createRoute(opportunityId: String) = "opportunity_detail/$opportunityId"
    }

    // Organization Path
    object OrgDashboard : Screen("org_dashboard")
    object PostOpportunity : Screen("post_opportunity")
    object MyOpportunities : Screen("my_opportunities")
    object OrgOpportunityDetail : Screen("org_opportunity_detail/{opportunityId}") {
        fun createRoute(opportunityId: String) = "org_opportunity_detail/$opportunityId"
    }
    object ManageBookings : Screen("manage_bookings/{opportunityId}") {
        fun createRoute(opportunityId: String) = "manage_bookings/$opportunityId"
    }
    object QrCode : Screen("qr_code/{opportunityId}") {
        fun createRoute(opportunityId: String) = "qr_code/$opportunityId"
    }
    object VolunteerProfileView : Screen("volunteer_profile_view/{volunteerId}") {
        fun createRoute(volunteerId: String) = "volunteer_profile_view/$volunteerId"
    }
    object IssueCertificate : Screen("issue_certificate/{volunteerId}/{activityTitle}") {
        fun createRoute(volunteerId: String, activityTitle: String) = "issue_certificate/$volunteerId/$activityTitle"
    }
    object OrgProfile : Screen("org_profile")

    // Shared
    object Messages : Screen("messages")
    object ChatDetail : Screen("chat_detail/{orgId}/{orgName}") {
        fun createRoute(orgId: String, orgName: String) = "chat_detail/$orgId/$orgName"
    }
    object Notifications : Screen("notifications")
    object OrgProfileView : Screen("org_profile_view/{orgId}") {
        fun createRoute(orgId: String) = "org_profile_view/$orgId"
    }
}
