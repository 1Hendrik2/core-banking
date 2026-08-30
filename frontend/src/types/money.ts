import type { Currency } from "./currency"

export type Money = {
    amount: number | string
    currencyCode: Currency
}