import { Outlet } from "react-router-dom"
import { Header } from "@/components/layout/header"
import { Sidebar } from "@/components/layout/side-bar"

export function Layout() {
    return (
        <div className="flex min-h-svh bg-background">
            <aside className="sticky top-0 hidden h-svh w-60 shrink-0 border-r border-sidebar-border md:block">
                <Sidebar />
            </aside>
            <div className="flex min-w-0 flex-1 flex-col">
                <Header />
                <main className="flex-1 px-4 py-5 md:px-6 md:py-6">
                    <Outlet />
                </main>
            </div>
        </div>
    )
}
