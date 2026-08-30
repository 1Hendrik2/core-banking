import { useEffect, useState } from "react"
import { ThemeProviderContext } from "@/components/theme-context"
import {
    THEME_STORAGE_KEY,
    applyTheme,
    type ThemePreference,
} from "@/theme"

type ThemeProviderProps = {
    children: React.ReactNode
    defaultTheme?: ThemePreference
    storageKey?: string
}

export function ThemeProvider({
    children,
    defaultTheme = "system",
    storageKey = THEME_STORAGE_KEY,
}: ThemeProviderProps) {
    const [theme, setTheme] = useState<ThemePreference>(
        () =>
            (localStorage.getItem(storageKey) as ThemePreference) || defaultTheme,
    )

    useEffect(() => {
        applyTheme(theme)
        if (theme !== "system") return

        const media = window.matchMedia("(prefers-color-scheme: dark)")
        const onChange = () => applyTheme("system")
        media.addEventListener("change", onChange)
        return () => media.removeEventListener("change", onChange)
    }, [theme])

    return (
        <ThemeProviderContext.Provider
            value={{
                theme,
                setTheme: (next) => {
                    localStorage.setItem(storageKey, next)
                    setTheme(next)
                },
            }}
        >
            {children}
        </ThemeProviderContext.Provider>
    )
}