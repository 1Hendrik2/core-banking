import { Badge } from "@/components/ui/badge"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"

const endpoints = [
    {
        method: "POST",
        path: "/accounts",
        title: "Create account",
        description: "Opens a multi-currency account for a customer.",
        request: `{
  "customerId": "cust-001",
  "country": "EE",
  "currencies": ["EUR", "USD"]
}`,
        response: `{
  "accountId": "5e04cdee-04d6-450d-892c-0f8dbfdb10ab",
  "customerId": "cust-001",
  "balances": [
    { "money": { "amount": 0.00, "currencyCode": "EUR" } },
    { "money": { "amount": 0.00, "currencyCode": "USD" } }
  ]
}`,
        rules: [
            "customerId is required",
            "country is an ISO 3166-1 alpha-2 code (EE, DE, …)",
            "currencies must be a non-empty list of Currency enum values: EUR, USD, GBP, SEK",
        ],
    },
    {
        method: "GET",
        path: "/accounts/{accountId}",
        title: "Get account",
        description: "Loads one account and its balances.",
        request: "Path param accountId: UUID",
        response: `{
  "accountId": "5e04cdee-04d6-450d-892c-0f8dbfdb10ab",
  "customerId": "cust-001",
  "balances": [
    { "money": { "amount": 40.24, "currencyCode": "EUR" } },
    { "money": { "amount": 0.00, "currencyCode": "USD" } }
  ]
}`,
        rules: ["accountId must be a UUID of an existing account", "404 if not found"],
    },
    {
        method: "POST",
        path: "/accounts/{accountId}/transactions",
        title: "Create transaction",
        description: "Credits or debits one currency on the account.",
        request: `{
  "money": { "amount": 10.50, "currencyCode": "EUR" },
  "direction": "IN",
  "description": "Cash deposit"
}`,
        response: `{
  "accountId": "5e04cdee-04d6-450d-892c-0f8dbfdb10ab",
  "transactionId": "…",
  "money": { "amount": 10.50, "currencyCode": "EUR" },
  "direction": "IN",
  "description": "Cash deposit",
  "balanceAfter": 50.74
}`,
        rules: [
            "Path param accountId: UUID",
            "money.amount and money.currencyCode are required",
            "direction is IN (credit) or OUT (debit)",
            "description is required",
            "currency must already exist on the account",
        ],
    },
    {
        method: "GET",
        path: "/accounts/{accountId}/transactions",
        title: "List transactions",
        description: "Returns every transaction posted to the account.",
        request: "Path param accountId: UUID",
        response: `[
  {
    "accountId": "…",
    "transactionId": "…",
    "money": { "amount": 10.50, "currencyCode": "EUR" },
    "direction": "IN",
    "description": "Cash deposit",
    "balanceAfter": 50.74
  }
]`,
        rules: ["accountId must be a UUID of an existing account"],
    },
] as const

function MethodBadge({ method }: { method: string }) {
    return (
        <Badge variant={method === "GET" ? "secondary" : "default"}>{method}</Badge>
    )
}

export function DocsPage() {
    return (
        <div className="mx-auto flex w-full max-w-4xl flex-col gap-6">
            <div>
                <h1 className="text-2xl font-medium tracking-tight">Documentation</h1>
                <p className="mt-1 text-sm text-muted-foreground">
                    Core Banking HTTP API used by this console.
                </p>
            </div>

            {endpoints.map((endpoint) => (
                <Card key={`${endpoint.method}-${endpoint.path}`}>
                    <CardHeader>
                        <div className="flex flex-wrap items-center gap-2">
                            <MethodBadge method={endpoint.method} />
                            <code className="font-mono text-sm">{endpoint.path}</code>
                        </div>
                        <CardTitle className="text-lg">{endpoint.title}</CardTitle>
                        <CardDescription>{endpoint.description}</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <section>
                            <h2 className="mb-2 text-xs font-medium tracking-wide text-muted-foreground uppercase">
                                Send
                            </h2>
                            <pre className="overflow-x-auto rounded-md border border-border bg-muted p-3 font-mono text-xs">
                                {endpoint.request}
                            </pre>
                        </section>
                        <section>
                            <h2 className="mb-2 text-xs font-medium tracking-wide text-muted-foreground uppercase">
                                Receive
                            </h2>
                            <pre className="overflow-x-auto rounded-md border border-border bg-muted p-3 font-mono text-xs">
                                {endpoint.response}
                            </pre>
                        </section>
                        <ul className="list-disc space-y-1 pl-5 text-sm text-muted-foreground">
                            {endpoint.rules.map((rule) => (
                                <li key={rule}>{rule}</li>
                            ))}
                        </ul>
                    </CardContent>
                </Card>
            ))}
        </div>
    )
}