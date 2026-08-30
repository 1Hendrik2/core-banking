import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import {
    getRecentAccounts,
    shortenUuid,
    type RecentAccount,
} from "@/lib/utils"

export function RecentAccounts() {
    const [items, setItems] = useState<RecentAccount[]>(() => getRecentAccounts())

    useEffect(() => {
        function refresh() {
            setItems(getRecentAccounts())
        }
        window.addEventListener("storage", refresh)
        window.addEventListener("focus", refresh)
        return () => {
            window.removeEventListener("storage", refresh)
            window.removeEventListener("focus", refresh)
        }
    }, [])

    if (items.length === 0) {
        return (
            <section>
                <h2 className="mb-2 text-sm font-medium text-foreground">
                    Recent accounts
                </h2>
                <p className="text-sm text-muted-foreground">
                    Opened and created accounts are stored in this browser only. There is
                    no list-all-accounts API.
                </p>
            </section>
        )
    }

    return (
        <section>
            <h2 className="mb-3 text-sm font-medium text-foreground">
                Recent accounts
            </h2>
            <ul className="divide-y divide-border rounded-lg border border-border bg-card">
                {items.map((item) => (
                    <li key={item.accountId}>
                        <Link
                            to={`/accounts/${item.accountId}`}
                            className="flex flex-col gap-0.5 px-3 py-2.5 transition-colors hover:bg-muted/60 sm:flex-row sm:items-center sm:justify-between"
                        >
                            <span className="font-mono text-sm text-foreground">
                                {shortenUuid(item.accountId)}
                            </span>
                            <span className="text-sm text-muted-foreground">
                                {item.customerId || "No customer label"}
                            </span>
                        </Link>
                    </li>
                ))}
            </ul>
        </section>
    )
}
