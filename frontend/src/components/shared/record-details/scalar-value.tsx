import { formatScalar, isUuid } from "@/lib/utils"

export function ScalarValue({ value }: { value: unknown }) {
    if (typeof value === "string" && isUuid(value)) {
        return (
            <code className="block truncate font-mono text-xs" title={value}>
                {value}
            </code>
        )
    }

    return <span>{formatScalar(value)}</span>
}