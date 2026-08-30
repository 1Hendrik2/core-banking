import { ChevronDown } from "lucide-react"
import { useMemo, useState } from "react"
import {
    Collapsible,
    CollapsibleContent,
    CollapsibleTrigger,
} from "@/components/ui/collapsible"
import { Button } from "@/components/ui/button"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import { ScrollArea } from "@/components/ui/scroll-area"
import { formatMoney, isMoney } from "@/lib/utils"
import { FieldRow } from "../shared/record-details/field-row"
import { ScalarValue } from "../shared/record-details/scalar-value"

interface RecordDetailsDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    title: string
    record: unknown
}


function RecordTree({ value, path }: { value: unknown; path: string }) {
    if (value === null || value === undefined || typeof value !== "object") {
        return (
            <FieldRow label={path || "value"}>
                <ScalarValue value={value} />
            </FieldRow>
        )
    }

    if (isMoney(value)) {
        return (
            <FieldRow label={path || "money"}>
                <span className="font-medium tabular-nums">
                    {formatMoney(value.amount, value.currencyCode)}
                </span>
            </FieldRow>
        )
    }

    if (Array.isArray(value)) {
        if (value.length === 0) {
            return (
                <FieldRow label={path || "items"}>
                    <span className="text-muted-foreground">empty list</span>
                </FieldRow>
            )
        }
        return (
            <div className="space-y-2">
                {value.map((item, index) => (
                    <div
                        key={`${path}.${index}`}
                        className="rounded-md border border-border bg-muted/30 px-3"
                    >
                        <RecordTree
                            value={item}
                            path={path ? `${path}[${index}]` : `[${index}]`}
                        />
                    </div>
                ))}
            </div>
        )
    }

    const entries = Object.entries(value as Record<string, unknown>)
    if (entries.length === 0) {
        return (
            <FieldRow label={path || "object"}>
                <span className="text-muted-foreground">empty object</span>
            </FieldRow>
        )
    }

    return (
        <dl>
            {entries.map(([key, nested]) => {
                const nextPath = path ? `${path}.${key}` : key
                const nestedIsObject =
                    nested !== null && typeof nested === "object" && !isMoney(nested)
                if (nestedIsObject) {
                    return (
                        <div key={nextPath} className="border-b border-border py-2.5 last:border-b-0">
                            <p className="mb-1.5 text-xs font-medium tracking-wide text-muted-foreground uppercase">
                                {key}
                            </p>
                            <div className="pl-0 sm:pl-2">
                                <RecordTree value={nested} path={key} />
                            </div>
                        </div>
                    )
                }
                return (
                    <FieldRow key={nextPath} label={key}>
                        {isMoney(nested) ? (
                            <span className="font-medium tabular-nums">
                                {formatMoney(nested.amount, nested.currencyCode)}
                            </span>
                        ) : (
                            <ScalarValue value={nested} />
                        )}
                    </FieldRow>
                )
            })}
        </dl>
    )
}

export function RecordDetailsDialog({
    open,
    onOpenChange,
    title,
    record,
}: RecordDetailsDialogProps) {
    const [jsonOpen, setJsonOpen] = useState(false)
    const json = useMemo(() => JSON.stringify(record, null, 2), [record])

    return (
        <Dialog
            open={open}
            onOpenChange={(next) => {
                if (!next) setJsonOpen(false)
                onOpenChange(next)
            }}
        >
            <DialogContent className="flex max-h-[85vh] max-w-[calc(100%-2rem)] flex-col gap-0 overflow-hidden sm:max-w-2xl">
                <DialogHeader className="pr-8">
                    <DialogTitle>{title}</DialogTitle>
                    <DialogDescription>
                        Every field on this record, including nested objects.
                    </DialogDescription>
                </DialogHeader>
                <ScrollArea className="min-h-0 flex-1 pr-3">
                    <div className="py-3">
                        <RecordTree value={record} path="" />
                    </div>
                    <Collapsible open={jsonOpen} onOpenChange={setJsonOpen}>
                        <CollapsibleTrigger
                            render={
                                <Button variant="ghost" size="sm" className="mb-2 gap-1" />
                            }
                        >
                            Raw JSON
                            <ChevronDown
                                className={`size-4 transition-transform ${jsonOpen ? "rotate-180" : ""}`}
                            />
                        </CollapsibleTrigger>
                        <CollapsibleContent>
                            <pre className="mb-3 overflow-x-auto rounded-md border border-border bg-muted p-3 font-mono text-xs text-foreground">
                                {json}
                            </pre>
                        </CollapsibleContent>
                    </Collapsible>
                </ScrollArea>
                <DialogFooter showCloseButton />
            </DialogContent>
        </Dialog>
    )
}
