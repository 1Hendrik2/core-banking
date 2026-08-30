import type { BalanceResponse } from "./balance-response"

export type AccountResponse = {
    accountId: string
    customerId: string
    balances: BalanceResponse[]
}