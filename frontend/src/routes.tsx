import { createBrowserRouter } from "react-router";
import { Dashboard } from "./pages/user/Dashboard";
import { Statistics } from "./pages/user/Statistics";
import { FeedWater } from "./pages/user/FeedWater";
import { Ventilation } from "./pages/user/Ventilation";
import { SettingsPage } from "./pages/user/Settings";
import { HospitalPage } from "./pages/user/Hospital";
import { Layout } from "./layouts/Sidebar";
import { AdminDashboard } from "./pages/admin/AdminDashboard";
import { AuthPage } from "./pages/auth/AuthPage";
import { Landing } from "./pages/Landing";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Landing,
  },
  {
    path: "/auth",
    Component: AuthPage,
  },
  {
    path: "/admin",
    Component: AdminDashboard,
  },
  {
    path: "/user",
    Component: Layout,
    children: [
      { index: true, Component: Dashboard },
      { path: "statistics", Component: Statistics },
      { path: "feed-water", Component: FeedWater },
      { path: "ventilation", Component: Ventilation },
      { path: "settings", Component: SettingsPage },
      { path: "hospital", Component: HospitalPage },
    ],
  },
]);
