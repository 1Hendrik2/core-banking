import { Copy, RefreshCw } from "lucide-react"
import { toast } from "sonner"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { formatMoneyValue } from "@/lib/utils"
import type { AccountResponse } from "@/types/dto/account/account-response"

export function AccountWorkspaceHeader({
    account,
    isRefreshing,
    onRefresh,
}: {
    account: AccountResponse
    isRefreshing: boolean
    onRefresh: () => void
}) {
    const label = account.customerId || account.accountId

    async function copyId() {
        try {
            await navigator.clipboard.writeText(account.accountId)
            toast.success("Account ID copied")
        } catch {
            toast.error("Unable to copy account ID")
        }
    }

    return (
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div className="min-w-0">
                <h1 className="truncate text-2xl font-medium tracking-tight text-foreground">
                    {label}
                </h1>
                <p className="mt-1 truncate font-mono text-xs text-muted-foreground">
                    {account.accountId}
                </p>
                <div className="mt-3 flex flex-wrap gap-2">
                    {(account.balances ?? []).map((balance, index) => (
                        <Badge key={`${balance.money.currencyCode}-${index}`} variant="secondary">
                            {formatMoneyValue(balance.money)}
                        </Badge>
                    ))}
                </div>
            </div>

            <div className="flex shrink-0 gap-2">
                <Button type="button" variant="outline" size="sm" onClick={() => void copyId()}>
                    <Copy />
                    Copy ID
                </Button>
                <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={isRefreshing}
                    onClick={onRefresh}
                >
                    <RefreshCw />
                    Refresh
                </Button>
            </div>
        </div>
    )
}