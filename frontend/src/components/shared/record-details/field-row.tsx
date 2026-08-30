import type { ReactNode } from "react"

export function FieldRow({
    label,
    children,
}: {
    label: string
    children: ReactNode
}) {
    return (
        <div className="grid gap-1 border-b border-border py-2.5 last:border-b-0 sm:grid-cols-[minmax(8rem,12rem)_1fr] sm:gap-4">
            <dt className="text-xs font-medium tracking-wide text-muted-foreground uppercase">
                {label}
            </dt>
            <dd className="min-w-0 text-sm text-foreground">{children}</dd>
        </div>
    )
}