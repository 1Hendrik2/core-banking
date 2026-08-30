import type { Direction } from "@/types/direction"
import type { Money } from "@/types/money"

export type CreateTransactionRequest = {
    money: Money
    direction: Direction
    description: string
}