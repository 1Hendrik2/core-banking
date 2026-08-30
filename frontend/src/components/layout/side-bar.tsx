import { BookOpen, Landmark, WalletCards } from "lucide-react"
import { NavLink } from "react-router-dom"
import { ThemeToggle } from "@/components/layout/theme-toggle"
import { Separator } from "@/components/ui/separator"
import { cn } from "@/lib/utils"

const NAV_ITEMS = [
    { to: "/accounts", label: "Accounts", icon: WalletCards },
    { to: "/docs", label: "Documentation", icon: BookOpen },
] as const

export function Sidebar({ onNavigate }: { onNavigate?: () => void }) {
    return (
        <div className="flex h-full flex-col bg-sidebar text-sidebar-foreground">
            <div className="flex items-center gap-2.5 px-4 py-4">
                <div className="flex size-8 items-center justify-center rounded-md bg-sidebar-accent text-sidebar-accent-foreground">
                    <Landmark className="size-4" />
                </div>
                <div className="min-w-0 leading-tight">
                    <p className="truncate text-sm font-medium">Core Banking</p>
                    <p className="truncate text-xs text-sidebar-foreground/70">
                        Admin console
                    </p>
                </div>
            </div>
            <Separator className="bg-sidebar-border" />
            <nav className="flex flex-1 flex-col gap-1 p-3">
                {NAV_ITEMS.map((item) => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        onClick={onNavigate}
                        className={({ isActive }) =>
                            cn(
                                "flex items-center gap-2 rounded-md px-2.5 py-2 text-sm transition-colors",
                                isActive
                                    ? "bg-sidebar-accent text-sidebar-accent-foreground"
                                    : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                            )
                        }
                    >
                        <item.icon className="size-4" />
                        {item.label}
                    </NavLink>
                ))}
            </nav>
            <div className="p-3">
                <ThemeToggle />
            </div>
        </div>
    )
}