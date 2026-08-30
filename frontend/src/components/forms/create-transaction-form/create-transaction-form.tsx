import { zodResolver } from "@hookform/resolvers/zod"
import { Controller, useForm } from "react-hook-form"
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
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { applyApiFieldErrors, apiErrorMessage } from "@/lib/utils"
import { CURRENCIES, type Currency } from "@/types/currency"
import { useCreateTransactionMutation } from "@/store/api/transaction-api"
import type { AccountResponse } from "@/types/dto/account/account-response"
import { DIRECTIONS } from "@/types/direction"
import { createTransactionSchema, type CreateTransactionFormValues } from "./components/create-transaction-schema"

export function CreateTransactionForm({
    accountId,
    account,
}: {
    accountId: string
    account?: AccountResponse
}) {
    const [createTransaction, { isLoading }] = useCreateTransactionMutation()


    const heldCurrencies = (account?.balances ?? [])
        .map((balance) => balance.money.currencyCode)
        .filter((code): code is Currency =>
            (CURRENCIES as readonly string[]).includes(code),
        )
    const currencyOptions =
        heldCurrencies.length > 0 ? heldCurrencies : [...CURRENCIES]

    const form = useForm<CreateTransactionFormValues>({
        resolver: zodResolver(createTransactionSchema),
        defaultValues: {
            money: {
                amount: "",
                currencyCode: currencyOptions[0] ?? "EUR",
            },
            direction: "IN",
            description: "",
        },
    })

    async function onSubmit(values: CreateTransactionFormValues) {
        try {
            await createTransaction({
                accountId,
                body: {
                    money: {
                        amount: Number(values.money.amount),
                        currencyCode: values.money.currencyCode,
                    },
                    direction: values.direction,
                    description: values.description,
                },
            }).unwrap()

            form.reset({
                money: {
                    amount: "",
                    currencyCode: values.money.currencyCode,
                },
                direction: values.direction,
                description: "",
            })
            toast.success("Transaction created")
        } catch (error) {
            const mapped = applyApiFieldErrors(error, form.setError)
            if (!mapped) {
                toast.error(apiErrorMessage(error, "Unable to create transaction"))
            }
        }
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Create transaction</CardTitle>
                <CardDescription>
                    Posted to this account. Direction IN credits, OUT debits.
                </CardDescription>
            </CardHeader>
            <CardContent>
                <form onSubmit={form.handleSubmit(onSubmit)} noValidate>
                    <FieldGroup>
                        <div className="grid gap-4 sm:grid-cols-2">
                            <Field data-invalid={!!form.formState.errors.money?.amount}>
                                <FieldLabel htmlFor="amount">Amount</FieldLabel>
                                <Input
                                    id="amount"
                                    type="number"
                                    inputMode="decimal"
                                    step="0.01"
                                    min="0.01"
                                    placeholder="0.00"
                                    aria-invalid={!!form.formState.errors.money?.amount}
                                    {...form.register("money.amount")}
                                />
                                <FieldError errors={[form.formState.errors.money?.amount]} />
                            </Field>

                            <Controller
                                control={form.control}
                                name="money.currencyCode"
                                render={({ field, fieldState }) => (
                                    <Field data-invalid={!!fieldState.error}>
                                        <FieldLabel>Currency</FieldLabel>
                                        <Select value={field.value} onValueChange={field.onChange}>
                                            <SelectTrigger className="w-full">
                                                <SelectValue placeholder="Select currency" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {currencyOptions.map((code) => (
                                                    <SelectItem key={code} value={code}>
                                                        {code}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                        <FieldError errors={[fieldState.error]} />
                                    </Field>
                                )}
                            />
                        </div>

                        <Controller
                            control={form.control}
                            name="direction"
                            render={({ field, fieldState }) => (
                                <Field data-invalid={!!fieldState.error}>
                                    <FieldLabel>Direction</FieldLabel>
                                    <Select value={field.value} onValueChange={field.onChange}>
                                        <SelectTrigger className="w-full">
                                            <SelectValue placeholder="Select direction" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {DIRECTIONS.map((direction) => (
                                                <SelectItem key={direction} value={direction}>
                                                    {direction === "IN" ? "IN — credit" : "OUT — debit"}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                    <FieldError errors={[fieldState.error]} />
                                </Field>
                            )}
                        />

                        <Field data-invalid={!!form.formState.errors.description}>
                            <FieldLabel htmlFor="description">Description</FieldLabel>
                            <Textarea
                                id="description"
                                rows={3}
                                placeholder="Settlement, deposit, withdrawal…"
                                aria-invalid={!!form.formState.errors.description}
                                {...form.register("description")}
                            />
                            <FieldDescription>
                                Required by the API. Account ID comes from the URL, not this
                                form.
                            </FieldDescription>
                            <FieldError errors={[form.formState.errors.description]} />
                        </Field>

                        <Button type="submit" disabled={isLoading}>
                            {isLoading ? "Posting…" : "Create transaction"}
                        </Button>
                    </FieldGroup>
                </form>
            </CardContent>
        </Card>
    )
}