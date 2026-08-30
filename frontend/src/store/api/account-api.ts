import { api } from "@/store/api/api"
import type { AccountResponse } from "@/types/dto/account/account-response"
import type { CreateAccountRequest } from "@/types/dto/account/create-account-request"

export const accountApi = api.injectEndpoints({
    endpoints: (build) => ({
        getAccount: build.query<AccountResponse, string>({
            query: (accountId) => `/accounts/${accountId}`,
            providesTags: (_result, _error, accountId) => [
                { type: "Account", id: accountId },
            ],
        }),
        createAccount: build.mutation<AccountResponse, CreateAccountRequest>({
            query: (body) => ({
                url: "/accounts",
                method: "POST",
                body,
            }),
            invalidatesTags: (result) =>
                result ? [{ type: "Account", id: result.accountId }] : [],
        }),
    }),
})

export const { useGetAccountQuery, useCreateAccountMutation, useLazyGetAccountQuery } = accountApi