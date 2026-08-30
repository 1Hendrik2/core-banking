import {
    columnFilteringFeature,
    createColumnHelper,
    createFilteredRowModel,
    createPaginatedRowModel,
    filterFn_includesString,
    globalFilteringFeature,
    rowPaginationFeature,
    tableFeatures,
    useTable,
} from "@tanstack/react-table"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"
import type { TransactionResponse } from "@/types/dto/transaction/transaction-response"
import { formatMoney, shortenUuid } from "@/lib/utils"
import { RecordDetailsDialog } from "../modal/record-details-dialog"

const EMPTY: TransactionResponse[] = []

const features = tableFeatures({
    columnFilteringFeature,
    globalFilteringFeature,
    rowPaginationFeature,
    filteredRowModel: createFilteredRowModel(),
    paginatedRowModel: createPaginatedRowModel(),
    filterFns: { includesString: filterFn_includesString },
})

const helper = createColumnHelper<typeof features, TransactionResponse>()

const columns = helper.columns([
    helper.accessor("transactionId", {
        header: "ID",
        cell: (info) => (
            <code className="font-mono text-xs" title={info.getValue()}>
                {shortenUuid(info.getValue())}
            </code>
        ),
    }),
    helper.accessor("direction", {
        id: "type",
        header: "Type",
        cell: (info) => info.getValue(),
    }),
    helper.accessor((row) => row.money.amount, {
        id: "amount",
        header: "Amount",
        cell: (info) => (
            <span className="tabular-nums">
                {formatMoney(info.getValue(), info.row.original.money.currencyCode)}
            </span>
        ),
    }),
    helper.accessor((row) => row.money.currencyCode, {
        id: "currency",
        header: "Currency",
    }),
    helper.accessor("description", {
        header: "Description",
        cell: (info) => (
            <span className="block max-w-56 truncate" title={info.getValue()}>
                {info.getValue()}
            </span>
        ),
    }),
    helper.accessor("balanceAfter", {
        header: "Balance after",
        cell: (info) => (
            <span className="tabular-nums">
                {formatMoney(info.getValue(), info.row.original.money.currencyCode)}
            </span>
        ),
    }),
    helper.display({
        id: "actions",
        header: "",
        enableGlobalFilter: false,
        cell: ({ row, table }) => (
            <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={(event) => {
                    event.stopPropagation()
                    const meta = table.options.meta as TableMeta | undefined
                    meta?.onSeeMore(row.original)
                }}
            >
                See more
            </Button>
        ),
    }),
])

interface TableMeta {
    onSeeMore: (tx: TransactionResponse) => void
}

export function TransactionsTable({
    transactions,
    isLoading,
}: {
    transactions: TransactionResponse[] | undefined
    isLoading: boolean
}) {
    const [selected, setSelected] = useState<TransactionResponse | null>(null)
    const data = transactions ?? EMPTY

    const table = useTable(
        {
            features,
            columns,
            data,
            globalFilterFn: "includesString",
            initialState: {
                pagination: { pageIndex: 0, pageSize: 10 },
            },
            meta: {
                onSeeMore: (tx: TransactionResponse) => setSelected(tx),
            } satisfies TableMeta,
        },
        (state) => ({
            pagination: state.pagination,
            globalFilter: state.globalFilter,
        }),
    )

    const pageCount = table.getPageCount()
    const pageIndex = table.state.pagination.pageIndex
    const rowCount = table.getRowCount()
    const filter = (table.state.globalFilter as string | undefined) ?? ""
    const rows = table.getRowModel().rows

    return (
        <Card>
            <CardHeader className="border-b">
                <CardTitle>Transactions</CardTitle>
                <CardDescription>
                    This account only. Search filters visible columns in the browser.
                </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 pt-4">
                <Input
                    value={filter}
                    onChange={(event) => table.setGlobalFilter(event.target.value)}
                    placeholder="Search transactions"
                    aria-label="Search transactions"
                />

                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map((group) => (
                            <TableRow key={group.id}>
                                {group.headers.map((header) => (
                                    <TableHead key={header.id}>
                                        {header.isPlaceholder ? null : (
                                            <table.FlexRender header={header} />
                                        )}
                                    </TableHead>
                                ))}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    Loading transactions…
                                </TableCell>
                            </TableRow>
                        ) : rows.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    No transactions for this account.
                                </TableCell>
                            </TableRow>
                        ) : (
                            rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    className="cursor-pointer"
                                    onClick={() => setSelected(row.original)}
                                >
                                    {row.getAllCells().map((cell) => (
                                        <TableCell key={cell.id}>
                                            <table.FlexRender cell={cell} />
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>

                <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                    <p className="text-xs text-muted-foreground">
                        {rowCount} transaction{rowCount === 1 ? "" : "s"}
                        {pageCount > 0
                            ? ` · page ${pageIndex + 1} of ${pageCount}`
                            : ""}
                    </p>
                    <div className="flex gap-2">
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => table.previousPage()}
                            disabled={!table.getCanPreviousPage()}
                        >
                            Previous
                        </Button>
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => table.nextPage()}
                            disabled={!table.getCanNextPage()}
                        >
                            Next
                        </Button>
                    </div>
                </div>
            </CardContent>

            <RecordDetailsDialog
                open={selected !== null}
                onOpenChange={(open) => {
                    if (!open) setSelected(null)
                }}
                title="Transaction details"
                record={selected}
            />
        </Card>
    )
}
