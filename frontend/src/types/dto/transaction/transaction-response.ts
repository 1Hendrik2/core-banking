import type { Direction } from "@/types/direction"
import type { Money } from "@/types/money"

export type TransactionResponse = {
    accountId: string
    transactionId: string
    money: Money
    direction: Direction
    description: string
    balanceAfter: number | string
}