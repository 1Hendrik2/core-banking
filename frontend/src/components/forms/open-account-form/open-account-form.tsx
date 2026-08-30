import { useState } from "react"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import { useNavigate } from "react-router-dom"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import {
    Field,
    FieldDescription,
    FieldError,
    FieldGroup,
    FieldLabel,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import {
    apiErrorMessage,
    isNotFoundError,
    rememberAccount,
} from "@/lib/utils"
import { useLazyGetAccountQuery } from "@/store/api/account-api"
import {
    openAccountSchema,
    type OpenAccountFormValues,
} from "./components/open-account-schema"

export function OpenAccountForm() {
    const navigate = useNavigate()
    const [getAccount] = useLazyGetAccountQuery()
    const [notFound, setNotFound] = useState(false)

    const form = useForm<OpenAccountFormValues>({
        resolver: zodResolver(openAccountSchema),
        defaultValues: { accountId: "" },
    })

    async function onSubmit(values: OpenAccountFormValues) {
        setNotFound(false)

        try {
            const account = await getAccount(values.accountId).unwrap()
            const accountId = account.accountId ?? values.accountId

            rememberAccount({
                accountId,
                customerId: account.customerId,
            })
            navigate(`/accounts/${accountId}`)
        } catch (error) {
            if (isNotFoundError(error)) {
                setNotFound(true)
                return
            }
            toast.error(apiErrorMessage(error, "Unable to open account"))
        }
    }

    return (
        <Card className="h-full">
            <CardHeader>
                <CardTitle>Open account</CardTitle>
                <CardDescription>
                    Paste an account UUID to inspect it and its transactions.
                </CardDescription>
            </CardHeader>
            <CardContent>
                <form onSubmit={form.handleSubmit(onSubmit)} noValidate>
                    <FieldGroup>
                        <Field data-invalid={!!form.formState.errors.accountId || notFound}>
                            <FieldLabel htmlFor="accountId">Account ID</FieldLabel>
                            <Input
                                id="accountId"
                                placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                                autoComplete="off"
                                spellCheck={false}
                                className="font-mono"
                                aria-invalid={!!form.formState.errors.accountId || notFound}
                                {...form.register("accountId", {
                                    onChange: () => setNotFound(false),
                                })}
                            />
                            <FieldDescription>
                                Must be an existing account UUID.
                            </FieldDescription>
                            <FieldError
                                errors={[
                                    form.formState.errors.accountId,
                                    notFound ? { message: "Account not found" } : undefined,
                                ]}
                            />
                        </Field>

                        <Button type="submit" disabled={form.formState.isSubmitting}>
                            {form.formState.isSubmitting ? "Opening…" : "Open account"}
                        </Button>
                    </FieldGroup>
                </form>
            </CardContent>
        </Card>
    )
}