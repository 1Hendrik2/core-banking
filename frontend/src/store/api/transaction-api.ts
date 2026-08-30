import { api } from "@/store/api/api"
import type { CreateTransactionRequest } from "@/types/dto/transaction/create-transaction-response"
import type { TransactionResponse } from "@/types/dto/transaction/transaction-response"

type CreateTransactionArg = {
    accountId: string
    body: CreateTransactionRequest
}

export const transactionApi = api.injectEndpoints({
    endpoints: (build) => ({
        getTransactions: build.query<TransactionResponse[], string>({
            query: (accountId) => `/accounts/${accountId}/transactions`,
            providesTags: (_result, _error, accountId) => [
                { type: "Transaction", id: accountId },
            ],
        }),

        createTransaction: build.mutation<TransactionResponse, CreateTransactionArg>({
            query: ({ accountId, body }) => ({
                url: `/accounts/${accountId}/transactions`,
                method: "POST",
                body,
            }),
            invalidatesTags: (_result, _error, { accountId }) => [
                { type: "Transaction", id: accountId },
                { type: "Account", id: accountId },
            ],
        }),
    }),
})

export const { useGetTransactionsQuery, useCreateTransactionMutation } =
    transactionApi