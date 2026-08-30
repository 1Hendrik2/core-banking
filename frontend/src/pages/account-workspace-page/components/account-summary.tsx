import { useState, type ReactNode } from "react"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardAction,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import type { AccountResponse } from "@/types/dto/account/account-response"
import { formatMoneyValue } from "@/lib/utils"
import { RecordDetailsDialog } from "@/components/modal/record-details-dialog"

export function AccountSummary({ account }: { account: AccountResponse }) {
    const [open, setOpen] = useState(false)

    return (
        <Card>
            <CardHeader className="border-b">
                <CardTitle>Account summary</CardTitle>
                <CardDescription>
                    Fields returned by GET /accounts/{"{accountId}"}
                </CardDescription>
                <CardAction>
                    <Button type="button" variant="outline" size="sm" onClick={() => setOpen(true)}>
                        See all fields
                    </Button>
                </CardAction>
            </CardHeader>
            <CardContent className="pt-4">
                <dl className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    <SummaryItem label="Account ID">
                        <code className="block truncate font-mono text-xs" title={account.accountId}>
                            {account.accountId}
                        </code>
                    </SummaryItem>
                    <SummaryItem label="Customer ID">
                        <span>{account.customerId || "—"}</span>
                    </SummaryItem>
                    {(account.balances ?? []).map((balance, index) => (
                        <SummaryItem
                            key={`${balance.money.currencyCode}-${index}`}
                            label={`${balance.money.currencyCode} balance`}
                        >
                            <span className="tabular-nums">
                                {formatMoneyValue(balance.money)}
                            </span>
                        </SummaryItem>
                    ))}
                </dl>
            </CardContent>
            <RecordDetailsDialog
                open={open}
                onOpenChange={setOpen}
                title="Account details"
                record={account}
            />
        </Card>
    )
}

function SummaryItem({
    label,
    children,
}: {
    label: string
    children: ReactNode
}) {
    return (
        <div className="min-w-0">
            <dt className="text-xs font-medium tracking-wide text-muted-foreground uppercase">
                {label}
            </dt>
            <dd className="mt-1 text-sm text-foreground">{children}</dd>
        </div>
    )
}
