// src/utils/logger.js
// Simple browser-only logger that goes silent in production builds.
// Backend uses SLF4J/Logback — this is only for the Vue frontend.

const isDev = import.meta.env.DEV

export const logger = {
  log: (...args) => {
    if (isDev) console.log(...args)
  },
  info: (...args) => {
    if (isDev) console.info(...args)
  },
  warn: (...args) => {
    if (isDev) console.warn(...args)
  },
  debug: (...args) => {
    if (isDev) console.debug(...args)
  },
  error: (...args) => {
    // Always log errors, even in production — useful for debugging user reports
    console.error(...args)
  },
}

export default logger