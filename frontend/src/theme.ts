export type ColorMode = "light" | "dark"
export type ThemePreference = ColorMode | "system"

export const THEME_STORAGE_KEY = "vite-ui-theme"

export const lightTheme = {
    background: "#ffffff",
    foreground: "#0a0a0a",
    card: "#ffffff",
    cardForeground: "#0a0a0a",
    primary: "#171717",
    primaryForeground: "#fafafa",
    secondary: "#f5f5f5",
    secondaryForeground: "#171717",
    muted: "#f5f5f5",
    mutedForeground: "#737373",
    accent: "#f5f5f5",
    accentForeground: "#171717",
    destructive: "#dc2626",
    border: "#e5e5e5",
    input: "#e5e5e5",
    ring: "#a3a3a3",
    radius: "0.625rem",
} as const

export const darkTheme = {
    background: "#0a0a0a",
    foreground: "#fafafa",
    card: "#171717",
    cardForeground: "#fafafa",
    primary: "#e5e5e5",
    primaryForeground: "#171717",
    secondary: "#262626",
    secondaryForeground: "#fafafa",
    muted: "#262626",
    mutedForeground: "#a3a3a3",
    accent: "#262626",
    accentForeground: "#fafafa",
    destructive: "#ef4444",
    border: "rgba(255, 255, 255, 0.10)",
    input: "rgba(255, 255, 255, 0.15)",
    ring: "#737373",
    radius: "0.625rem",
} as const

export type ThemeTokens = typeof lightTheme

const cssVarMap: Record<keyof ThemeTokens, string> = {
    background: "--background",
    foreground: "--foreground",
    card: "--card",
    cardForeground: "--card-foreground",
    primary: "--primary",
    primaryForeground: "--primary-foreground",
    secondary: "--secondary",
    secondaryForeground: "--secondary-foreground",
    muted: "--muted",
    mutedForeground: "--muted-foreground",
    accent: "--accent",
    accentForeground: "--accent-foreground",
    destructive: "--destructive",
    border: "--border",
    input: "--input",
    ring: "--ring",
    radius: "--radius",
}

export function getSystemColorMode(): ColorMode {
    return window.matchMedia("(prefers-color-scheme: dark)").matches
        ? "dark"
        : "light"
}

export function resolveColorMode(preference: ThemePreference): ColorMode {
    return preference === "system" ? getSystemColorMode() : preference
}

export function applyTheme(preference: ThemePreference) {
    const root = document.documentElement
    const mode = resolveColorMode(preference)
    const tokens = mode === "dark" ? darkTheme : lightTheme

    root.classList.remove("light", "dark")
    root.classList.add(mode)
    root.style.colorScheme = mode

    for (const [key, cssVar] of Object.entries(cssVarMap)) {
        root.style.setProperty(cssVar, tokens[key as keyof ThemeTokens])
    }
}