import { CreateAccountForm } from "@/components/forms/create-account-form/create-account-form"
import { OpenAccountForm } from "@/components/forms/open-account-form/open-account-form"
import { RecentAccounts } from "@/components/shared/recent-accounts"

export function AccountsHubPage() {
  return (
    <div className="mx-auto flex w-full max-w-5xl flex-col gap-8">
      <div>
        <h1 className="text-2xl font-medium tracking-tight text-foreground">
          Accounts
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Open an existing account by UUID or create a new one. There is no
          bank-wide account list.
        </p>
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <OpenAccountForm />
        <CreateAccountForm />
      </div>
      <RecentAccounts />
    </div>
  )
}
