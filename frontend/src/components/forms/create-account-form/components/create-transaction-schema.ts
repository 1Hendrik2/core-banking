import { z } from "zod"
import { CURRENCIES } from "@/types/currency"

export const createAccountSchema = z.object({
    customerId: z.string().trim().min(1, "Customer ID is required"),
    country: z
        .string()
        .trim()
        .length(2, "Use a 2-letter country code")
        .toUpperCase(),
    currencies: z
        .array(z.enum(CURRENCIES))
        .min(1, "Select at least one currency"),
})

export type CreateAccountFormValues = z.infer<typeof createAccountSchema>