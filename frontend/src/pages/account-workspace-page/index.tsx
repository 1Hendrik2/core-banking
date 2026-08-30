import { useEffect } from "react"
import { Link, useParams } from "react-router-dom"
import { Skeleton } from "@/components/ui/skeleton"
import { getErrorMessage, isNotFoundError } from "@/lib/utils"
import { isUuid, rememberAccount } from "@/lib/utils"
import { useGetAccountQuery } from "@/store/api/account-api"
import { useGetTransactionsQuery } from "@/store/api/transaction-api"
import { AccountWorkspaceHeader } from "./components/account-workspace-header"
import { AccountSummary } from "./components/account-summary"
import { TransactionsTable } from "@/components/shared/transaction-table"
import { CreateTransactionForm } from "@/components/forms/create-transaction-form/create-transaction-form"
import { Button } from "@/components/ui/button"

export function AccountWorkspacePage() {
    const { accountId = "" } = useParams()
    const valid = isUuid(accountId)

    const accountQuery = useGetAccountQuery(accountId, { skip: !valid })
    const transactionsQuery = useGetTransactionsQuery(accountId, { skip: !valid })

    useEffect(() => {
        if (!accountQuery.data) return
        rememberAccount({
            accountId: accountQuery.data.accountId,
            customerId: accountQuery.data.customerId,
        })
    }, [accountQuery.data])

    if (!valid) {
        return (
            <EmptyState
                title="Invalid account ID"
                description="The URL does not contain a valid UUID."
            />
        )
    }

    if (accountQuery.isError && isNotFoundError(accountQuery.error)) {
        return (
            <EmptyState
                title="Account not found"
                description="No account exists for this UUID."
            />
        )
    }

    if (accountQuery.isLoading) {
        return (
            <div className="mx-auto flex w-full max-w-6xl flex-col gap-4">
                <Skeleton className="h-16 w-full" />
                <Skeleton className="h-40 w-full" />
                <Skeleton className="h-56 w-full" />
            </div>
        )
    }

    if (accountQuery.isError || !accountQuery.data) {
        return (
            <EmptyState
                title="Unable to load account"
                description={getErrorMessage(accountQuery.error, "The account request failed.")}
            />
        )
    }

    const account = accountQuery.data

    return (
        <div className="mx-auto flex w-full max-w-6xl flex-col gap-6">
            <AccountWorkspaceHeader
                account={account}
                isRefreshing={accountQuery.isFetching || transactionsQuery.isFetching}
                onRefresh={() => {
                    void accountQuery.refetch()
                    void transactionsQuery.refetch()
                }}
            />

            <AccountSummary account={account} />
            <CreateTransactionForm accountId={account.accountId} account={account} />

            {transactionsQuery.isError && !isNotFoundError(transactionsQuery.error) ? (
                <p className="text-sm text-destructive">
                    {getErrorMessage(transactionsQuery.error, "Unable to load transactions.")}
                </p>
            ) : null}

            <TransactionsTable
                transactions={transactionsQuery.data}
                isLoading={transactionsQuery.isLoading}
            />
        </div>
    )
}

function EmptyState({
    title,
    description,
}: {
    title: string
    description: string
}) {
    return (
        <div className="mx-auto flex max-w-lg flex-col items-start gap-3 rounded-xl border border-border bg-card p-6">
            <h1 className="text-xl font-medium text-foreground">{title}</h1>
            <p className="text-sm text-muted-foreground">{description}</p>
            <Button variant="outline">
                <Link to="/accounts">Back to Accounts</Link>
            </Button>
        </div>
    )
}
