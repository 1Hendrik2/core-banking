import { Menu } from "lucide-react"
import { useState } from "react"
import { useLocation, useParams } from "react-router-dom"
import { Sidebar } from "@/components/layout/side-bar"
import { Button } from "@/components/ui/button"
import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
} from "@/components/ui/sheet"
import { isUuid, shortenUuid } from "@/lib/utils"

export function Header() {
    const location = useLocation()
    const params = useParams()
    const [open, setOpen] = useState(false)

    const accountId = params.accountId
    const title =
        location.pathname === "/accounts"
            ? "Accounts"
            : accountId && isUuid(accountId)
                ? shortenUuid(accountId)
                : "Account"
    return (
        <header className="sticky top-0 z-20 flex h-14 items-center gap-3 border-b border-border bg-background/90 px-4 backdrop-blur-sm md:px-6">
            <Sheet open={open} onOpenChange={setOpen}>
                <Button
                    variant="ghost"
                    size="icon"
                    className="md:hidden"
                    onClick={() => setOpen(true)}
                    aria-label="Open navigation"
                >
                    <Menu />
                </Button>
                <SheetContent side="left" className="w-64 p-0" showCloseButton>
                    <SheetHeader className="sr-only">
                        <SheetTitle>Navigation</SheetTitle>
                    </SheetHeader>
                    <Sidebar onNavigate={() => setOpen(false)} />
                </SheetContent>
            </Sheet>
            <div className="min-w-0">
                <p className="truncate text-sm font-medium text-foreground">{title}</p>
                <p className="truncate text-xs text-muted-foreground">
                    Internal operations
                </p>
            </div>
        </header>
    )
}
