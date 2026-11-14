import { Button } from "@/shared/components/Button";

export type SearchView = "list" | "map";

export interface ViewToggleProps {
  view: SearchView;
  /** Valoarea vine din URL (`?view=map`); părintele o scrie înapoi prin `useUrlFilters`. */
  onChange: (view: SearchView) => void;
}

export function ViewToggle({ view, onChange }: ViewToggleProps) {
  return (
    <div role="group" aria-label="Mod de afișare">
      <Button
        size="sm"
        variant={view === "list" ? "primary" : "secondary"}
        aria-pressed={view === "list"}
        onClick={() => onChange("list")}
      >
        Listă
      </Button>
      <Button
        size="sm"
        variant={view === "map" ? "primary" : "secondary"}
        aria-pressed={view === "map"}
        onClick={() => onChange("map")}
      >
        Hartă
      </Button>
    </div>
  );
}
