import type { Metadata } from "next";
import "./globals.css";

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
    <html lang="ko" suppressHydrationWarning>
      <body>{children}</body>
    </html>
  );
}
