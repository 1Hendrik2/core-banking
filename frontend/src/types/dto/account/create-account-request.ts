import type { Currency } from "../../currency"

export type CreateAccountRequest = {
    customerId: string
    country: string
    currencies: Currency[]
}