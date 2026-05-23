import type { Metadata, Viewport } from "next";
import { Nunito_Sans } from "next/font/google";
import "./globals.css";
import { Providers } from "@/app/providers";

const nunito = Nunito_Sans({
  subsets: ["latin", "vietnamese"],
  variable: "--font-nunito",
  display: "swap"
});

export const metadata: Metadata = {
  title: "HomeTree - Digital Family Hub",
  description: "A private, warm, multi-generation family hub for memories, stories, recipes, and connection."
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#FFF8EA"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body className={`${nunito.variable} antialiased`}>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
