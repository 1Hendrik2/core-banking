import { createContext } from "react"
import type { ThemePreference } from "@/theme"

export type ThemeProviderState = {
    theme: ThemePreference
    setTheme: (theme: ThemePreference) => void
}

export const ThemeProviderContext = createContext<ThemeProviderState | undefined>(
    undefined,
)