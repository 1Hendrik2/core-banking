import { z } from "zod"
import { CURRENCIES } from "@/types/currency"
import { DIRECTIONS } from "@/types/direction"

export const createTransactionSchema = z.object({
    money: z.object({
        amount: z.string().trim().min(1, "Amount is required"),
        currencyCode: z.enum(CURRENCIES),
    }),
    direction: z.enum(DIRECTIONS),
    description: z.string().trim().min(1, "Description is required"),
})

export type CreateTransactionFormValues = z.infer<typeof createTransactionSchema>