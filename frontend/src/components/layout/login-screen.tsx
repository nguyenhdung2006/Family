"use client";

export function LoginScreen() {
  const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
  const googleClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
  const loginUrl = `${apiBaseUrl}/oauth2/authorization/google`;

  return (
    <main className="grid min-h-screen place-items-center px-4">
      <section className="w-full max-w-xl rounded-lg border border-border-warm bg-surface p-6 text-center shadow-[0_24px_70px_rgba(80,55,33,0.12)] sm:p-8">
        <div className="mx-auto grid h-20 w-20 place-items-center rounded-full bg-warm-yellow/25 text-3xl font-black text-wood">HT</div>
        <h1 className="mt-5 text-4xl font-black leading-tight text-ink">Welcome to HomeTree</h1>
        <p className="mt-3 text-lg font-semibold leading-8 text-muted">
          Sign in with Google to enter your private family space.
        </p>
        <a
          href={loginUrl}
          className="mt-6 inline-flex min-h-12 w-full items-center justify-center rounded-lg bg-wood px-5 text-lg font-bold text-white shadow-sm transition hover:bg-wood-dark sm:w-auto"
        >
          Continue with Google
        </a>
        <p className="mt-4 text-sm font-bold text-muted">
          {googleClientId ? "OAuth client configured from environment." : "Set NEXT_PUBLIC_GOOGLE_CLIENT_ID for local OAuth metadata."}
        </p>
      </section>
    </main>
  );
}
