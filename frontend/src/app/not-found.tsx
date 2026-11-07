import Link from "next/link";

export default function NotFound() {
  return (
    <main className="flex flex-1 flex-col items-center justify-center gap-4 p-8 text-center">
      <h1 className="text-3xl font-semibold tracking-tight">Pagina nu a fost găsită</h1>
      <p className="text-zinc-600 dark:text-zinc-400">
        Adresa pe care ai accesat-o nu există sau a fost mutată.
      </p>
      <Link href="/" className="font-medium underline">
        Înapoi la pagina principală
      </Link>
    </main>
  );
}
