export interface ResultsCountProps {
  total: number;
  /** Substantiv la singular și plural, ex. ["anunț", "anunțuri"]. */
  noun?: [singular: string, plural: string];
}

export function ResultsCount({ total, noun = ["rezultat", "rezultate"] }: ResultsCountProps) {
  const word = total === 1 ? noun[0] : noun[1];
  return (
    <p role="status" aria-live="polite">
      {total} {word}
    </p>
  );
}
