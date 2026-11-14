import { Select } from "@/shared/components/Select";

export interface SortOption {
  /** Valoarea trimisă în `sort`, ex. `price,asc`. */
  value: string;
  label: string;
}

export interface SortSelectProps {
  options: SortOption[];
  value: string | undefined;
  onChange: (value: string | undefined) => void;
  id?: string;
}

/** Listă de sortare; prima opțiune e cea implicită și nu se scrie în URL. */
export function SortSelect({ options, value, onChange, id = "sort-select" }: SortSelectProps) {
  const defaultValue = options[0]?.value;

  return (
    <div>
      <label htmlFor={id}>Sortează după</label>
      <Select
        id={id}
        value={value ?? defaultValue}
        onChange={(event) =>
          onChange(event.target.value === defaultValue ? undefined : event.target.value)
        }
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </Select>
    </div>
  );
}
