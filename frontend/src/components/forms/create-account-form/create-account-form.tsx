import { useState } from "react"
import { zodResolver } from "@hookform/resolvers/zod"
import { Controller, useForm } from "react-hook-form"
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
import { Checkbox } from "@/components/ui/checkbox"
import {
    Field,
    FieldDescription,
    FieldError,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSet,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { applyApiFieldErrors, apiErrorMessage, rememberAccount, toCreateAccountRequest } from "@/lib/utils"
import { useCreateAccountMutation } from "@/store/api/account-api"
import { CURRENCIES } from "@/types/currency"
import type { AccountResponse } from "@/types/dto/account/account-response"
import { CreateAccountOrphanCard } from "../create-transaction-form/components/create-account-orphan-card"
import {
    createAccountSchema,
    type CreateAccountFormValues,
} from "./components/create-transaction-schema"
import { COUNTRIES } from "@/types/countries"

export function CreateAccountForm() {
    const navigate = useNavigate()
    const [createAccount, { isLoading }] = useCreateAccountMutation()
    const [orphanAccount, setOrphanAccount] = useState<AccountResponse | null>(null)

    const form = useForm<CreateAccountFormValues>({
        resolver: zodResolver(createAccountSchema),
        defaultValues: {
            customerId: "",
            country: "",
            currencies: [],
        },
    })

    async function onSubmit(values: CreateAccountFormValues) {
        try {
            const account = await createAccount(
                toCreateAccountRequest(values),
            ).unwrap()

            if (account.accountId) {
                rememberAccount({
                    accountId: account.accountId,
                    customerId: account.customerId,
                })
                toast.success("Account created")
                navigate(`/accounts/${account.accountId}`)
                return
            }

            setOrphanAccount(account)
            toast.success("Account created, but no accountId was returned")
        } catch (error) {
            if (!applyApiFieldErrors(error, form.setError)) {
                toast.error(apiErrorMessage(error, "Unable to create account"))
            }
        }
    }

    if (orphanAccount) {
        return (
            <CreateAccountOrphanCard
                account={orphanAccount}
                onReset={() => {
                    setOrphanAccount(null)
                    form.reset()
                }}
            />
        )
    }

    return (
        <Card className="h-full">
            <CardHeader>
                <CardTitle>Create account</CardTitle>
                <CardDescription>
                    Opens a multi-currency account for a customer.
                </CardDescription>
            </CardHeader>
            <CardContent>
                <form onSubmit={form.handleSubmit(onSubmit)} noValidate>
                    <FieldGroup>
                        <Field data-invalid={!!form.formState.errors.customerId}>
                            <FieldLabel htmlFor="customerId">Customer ID</FieldLabel>
                            <Input
                                id="customerId"
                                placeholder="customer-123"
                                autoComplete="off"
                                aria-invalid={!!form.formState.errors.customerId}
                                {...form.register("customerId")}
                            />
                            <FieldError errors={[form.formState.errors.customerId]} />
                        </Field>

                        <Field data-invalid={!!form.formState.errors.country}>
                            <FieldLabel htmlFor="country">Country</FieldLabel>
                            <Input
                                id="country"
                                list="country-codes"
                                placeholder="EE"
                                autoComplete="off"
                                aria-invalid={!!form.formState.errors.country}
                                {...form.register("country")}
                            />
                            <datalist id="country-codes">
                                {COUNTRIES.map((country) => (
                                    <option key={country.code} value={country.code}>
                                        {country.name}
                                    </option>
                                ))}
                            </datalist>
                            <FieldDescription>ISO 3166-1 alpha-2 code</FieldDescription>
                            <FieldError errors={[form.formState.errors.country]} />
                        </Field>

                        <Controller
                            control={form.control}
                            name="currencies"
                            render={({ field, fieldState }) => (
                                <FieldSet>
                                    <FieldLegend variant="label">Currencies</FieldLegend>
                                    <FieldDescription>
                                        At least one. Duplicates are rejected by the API.
                                    </FieldDescription>
                                    <div className="grid grid-cols-2 gap-2">
                                        {CURRENCIES.map((code) => {
                                            const checked = field.value.includes(code)
                                            return (
                                                <label
                                                    key={code}
                                                    className="flex items-center gap-2 rounded-md border border-border px-2.5 py-2 text-sm"
                                                >
                                                    <Checkbox
                                                        checked={checked}
                                                        onCheckedChange={(next) => {
                                                            field.onChange(
                                                                next === true
                                                                    ? [...field.value, code]
                                                                    : field.value.filter((item) => item !== code),
                                                            )
                                                        }}
                                                    />
                                                    {code}
                                                </label>
                                            )
                                        })}
                                    </div>
                                    <FieldError errors={[fieldState.error]} />
                                </FieldSet>
                            )}
                        />

                        <Button type="submit" disabled={isLoading}>
                            {isLoading ? "Creating…" : "Create account"}
                        </Button>
                    </FieldGroup>
                </form>
            </CardContent>
        </Card>
    )
}