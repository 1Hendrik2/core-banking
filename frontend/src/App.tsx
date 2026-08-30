import { Navigate, Route, Routes } from "react-router-dom"
import { Layout } from "@/components/layout/layout"
import { AccountWorkspacePage } from "./pages/account-workspace-page"
import { AccountsHubPage } from "./pages/accounts-hub-page"
import { DocsPage } from "./pages/docs-page"

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Navigate to="/accounts" replace />} />
        <Route path="/accounts" element={<AccountsHubPage />} />
        <Route path="/accounts/:accountId" element={<AccountWorkspacePage />} />
        <Route path="/docs" element={<DocsPage />} />
        <Route path="*" element={<Navigate to="/accounts" replace />} />
      </Route>
    </Routes>
  )
}
