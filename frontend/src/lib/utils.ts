import type { CreateTransactionFormValues } from "@/components/forms/create-transaction-form/components/create-transaction-schema"
import type { CreateTransactionRequest } from "@/types/dto/transaction/create-transaction-response"
import type { Money } from "@/types/money"
import { clsx, type ClassValue } from "clsx"
import type { FieldValues, Path, UseFormSetError } from "react-hook-form"
import type { ErrorResponse } from "react-router-dom"
import { twMerge } from "tailwind-merge"
import type { FetchBaseQueryError } from "@reduxjs/toolkit/query"
import type { SerializedError } from "@reduxjs/toolkit"
import type { CreateAccountRequest } from "@/types/dto/account/create-account-request"
import type { CreateAccountFormValues } from "@/components/forms/create-account-form/components/create-transaction-schema"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/** Java UUID.fromString format: 8-4-4-4-12 hex. */
export const UUID_PATTERN =
  /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/

export function isUuid(value: string): boolean {
  return UUID_PATTERN.test(value.trim())
}

export function isMoney(value: unknown): value is Money {
  return (
    typeof value === "object" &&
    value !== null &&
    "amount" in value &&
    "currencyCode" in value &&
    (typeof (value as Money).amount === "number" ||
      typeof (value as Money).amount === "string") &&
    typeof (value as Money).currencyCode === "string"
  )
}

export function formatScalar(value: unknown): string {
  if (value === null) return "null"
  if (value === undefined) return "—"
  if (typeof value === "boolean") return formatBoolean(value)
  if (typeof value === "number") {
    return Number.isFinite(value) ? value.toLocaleString() : String(value)
  }
  if (typeof value === "string") {
    if (isIsoInstant(value)) return formatDateTime(value)
    return value
  }
  return String(value)
}

export function formatDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "medium",
  }).format(date)
}

export function isIsoInstant(value: string): boolean {
  return /^\d{4}-\d{2}-\d{2}T/.test(value)
}

export function shortenUuid(value: string): string {
  if (!isUuid(value)) return value
  return `${value.slice(0, 8)}…${value.slice(-4)}`
}

export function formatBoolean(value: boolean): string {
  return value ? "true" : "false"
}

export function formatMoney(amount: number | string, currency: string): string {
  const n = typeof amount === "number" ? amount : Number(amount)
  if (!Number.isFinite(n)) {
    return `${String(amount)} ${currency}`
  }
  try {
    return new Intl.NumberFormat(undefined, {
      style: "currency",
      currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(n)
  } catch {
    return `${n.toFixed(2)} ${currency}`
  }
}

export function formatMoneyValue(money: Money | null | undefined): string {
  if (!money) return "—"
  return formatMoney(money.amount, money.currencyCode)
}

export class ApiError extends Error {
  readonly status: number
  readonly errorCode?: string
  readonly fieldErrors: Record<string, string>
  readonly body?: ErrorResponse

  constructor(options: {
    message: string
    status: number
    errorCode?: string
    fieldErrors?: Record<string, string>
    body?: ErrorResponse
  }) {
    super(options.message)
    this.name = "ApiError"
    this.status = options.status
    this.errorCode = options.errorCode
    this.fieldErrors = options.fieldErrors ?? {}
    this.body = options.body
  }
}


export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}

export function applyApiFieldErrors<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
): boolean {
  if (!isApiError(error)) return false
  const entries = Object.entries(error.fieldErrors)
  if (entries.length === 0) return false
  for (const [field, message] of entries) {
    setError(field as Path<T>, { type: "server", message })
  }
  return true
}

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (isApiError(error)) return error.message
  if (error instanceof Error && error.message) return error.message
  return fallback
}

export function toCreateTransactionRequest(
  values: CreateTransactionFormValues,
): CreateTransactionRequest {
  return {
    money: {
      amount: Number(values.money.amount),
      currencyCode: values.money.currencyCode,
    },
    direction: values.direction,
    description: values.description,
  }
}

const STORAGE_KEY = "corebanking.recentAccounts"
const MAX_RECENT = 12

export interface RecentAccount {
  accountId: string
  customerId?: string
  openedAt: string
}

function read(): RecentAccount[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    const parsed: unknown = JSON.parse(raw)
    if (!Array.isArray(parsed)) return []
    return parsed.filter(
      (item): item is RecentAccount =>
        typeof item === "object" &&
        item !== null &&
        typeof (item as RecentAccount).accountId === "string",
    )
  } catch {
    return []
  }
}

function write(items: RecentAccount[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items.slice(0, MAX_RECENT)))
}

export function getRecentAccounts(): RecentAccount[] {
  return read()
}

export function rememberAccount(account: {
  accountId: string
  customerId?: string
}): RecentAccount[] {
  const next: RecentAccount[] = [
    {
      accountId: account.accountId,
      customerId: account.customerId,
      openedAt: new Date().toISOString(),
    },
    ...read().filter((item) => item.accountId !== account.accountId),
  ]
  write(next)
  return next
}

export function isFetchBaseQueryError(
  error: unknown,
): error is FetchBaseQueryError {
  return typeof error === "object" && error !== null && "status" in error
}

export function isSerializedError(error: unknown): error is SerializedError {
  return (
    typeof error === "object" &&
    error !== null &&
    "message" in error &&
    !("status" in error)
  )
}

export function isNotFoundError(error: unknown): boolean {
  return isFetchBaseQueryError(error) && error.status === 404
}

export function getErrorMessage(error: unknown, fallback: string): string {
  if (isFetchBaseQueryError(error)) {
    if (typeof error.data === "string") return error.data
    if (
      typeof error.data === "object" &&
      error.data !== null &&
      "message" in error.data &&
      typeof error.data.message === "string"
    ) {
      return error.data.message
    }
    return `${fallback} (${error.status})`
  }

  if (error && typeof error === "object" && "message" in error) {
    const message = error.message
    if (typeof message === "string" && message.length > 0) return message
  }

  return fallback
}

export function toCreateAccountRequest(
  values: CreateAccountFormValues,
): CreateAccountRequest {
  return {
    customerId: values.customerId,
    country: values.country,
    currencies: values.currencies,
  }
}