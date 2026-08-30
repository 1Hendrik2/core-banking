import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import type { AccountResponse } from "@/types/dto/account/account-response"
import { RecordDetailsDialog } from "@/components/modal/record-details-dialog"

export function CreateAccountOrphanCard({
    account,
    onReset,
}: {
    account: AccountResponse
    onReset: () => void
}) {
    const [detailsOpen, setDetailsOpen] = useState(false)

    return (
        <Card className="h-full">
            <CardHeader>
                <CardTitle>Account created</CardTitle>
                <CardDescription>
                    The API did not return an accountId, so the workspace cannot be opened
                    automatically.
                </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-3">
                <Button type="button" onClick={() => setDetailsOpen(true)}>
                    See all fields
                </Button>
                <Button type="button" variant="outline" onClick={onReset}>
                    Create another
                </Button>
                <RecordDetailsDialog
                    open={detailsOpen}
                    onOpenChange={setDetailsOpen}
                    title="Account details"
                    record={account}
                />
            </CardContent>
        </Card>
    )
}