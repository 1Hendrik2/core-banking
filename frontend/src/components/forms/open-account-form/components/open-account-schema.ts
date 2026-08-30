import { z } from "zod"

export const openAccountSchema = z.object({
    accountId: z
        .string()
        .trim()
        .uuid("Enter a valid account UUID"),
})

export type OpenAccountFormValues = z.infer<typeof openAccountSchema>