import type { Metadata } from "next";
import { IBM_Plex_Sans_KR, Manrope } from "next/font/google";
import "./globals.css";

const bodyFont = IBM_Plex_Sans_KR({
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
  variable: "--font-body",
  display: "swap",
});

const brandFont = Manrope({
  subsets: ["latin"],
  weight: ["500", "600", "700", "800"],
  variable: "--font-brand",
  display: "swap",
});

export const metadata: Metadata = {
  title: "SINWOO Platform",
  description: "Sinwoo B2B operations platform",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" suppressHydrationWarning className={`${bodyFont.variable} ${brandFont.variable}`}>
      <body>{children}</body>
    </html>
  );
}
