import { defineConfig, globalIgnores } from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";

const eslintConfig = defineConfig([
  ...nextVitals,
  ...nextTs,
  // eslint-config-next leaves the React version as "detect", and that path in
  // eslint-plugin-react calls context.getFilename(), which ESLint 10 removed —
  // every react/* rule throws on load. Naming the version skips detection.
  // Drop this once eslint-config-next ships an ESLint 10 compatible plugin.
  { settings: { react: { version: "19.2" } } },
  globalIgnores([".next/**", "out/**", "build/**", "next-env.d.ts"]),
]);

export default eslintConfig;
