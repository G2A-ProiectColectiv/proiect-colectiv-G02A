"use client";

import { useRef, type KeyboardEvent } from "react";
import styles from "./Tabs.module.css";

export interface TabItem {
  id: string;
  label: string;
  /** Numărul afișat lângă etichetă (ex. „Active 5"). */
  count?: number;
}

export interface TabsProps {
  tabs: TabItem[];
  activeId: string;
  onChange: (id: string) => void;
  ariaLabel: string;
  /** Prefix pentru `id`-uri; panoul asociat are `id="{idPrefix}-panel"`. */
  idPrefix: string;
}

export function tabPanelId(idPrefix: string): string {
  return `${idPrefix}-panel`;
}

export function tabButtonId(idPrefix: string, tabId: string): string {
  return `${idPrefix}-tab-${tabId}`;
}

export function Tabs({ tabs, activeId, onChange, ariaLabel, idPrefix }: TabsProps) {
  const buttons = useRef<Array<HTMLButtonElement | null>>([]);

  function onKeyDown(event: KeyboardEvent<HTMLButtonElement>, index: number) {
    let next = index;
    if (event.key === "ArrowRight") next = (index + 1) % tabs.length;
    else if (event.key === "ArrowLeft") next = (index - 1 + tabs.length) % tabs.length;
    else if (event.key === "Home") next = 0;
    else if (event.key === "End") next = tabs.length - 1;
    else return;

    event.preventDefault();
    buttons.current[next]?.focus();
    onChange(tabs[next].id);
  }

  return (
    <div className={styles.tabs} role="tablist" aria-label={ariaLabel}>
      {tabs.map((tab, index) => {
        const isActive = tab.id === activeId;
        return (
          <button
            key={tab.id}
            ref={(element) => {
              buttons.current[index] = element;
            }}
            id={tabButtonId(idPrefix, tab.id)}
            type="button"
            role="tab"
            aria-selected={isActive}
            aria-controls={tabPanelId(idPrefix)}
            tabIndex={isActive ? 0 : -1}
            className={`${styles.tab} ${isActive ? styles.active : ""}`}
            onClick={() => onChange(tab.id)}
            onKeyDown={(event) => onKeyDown(event, index)}
          >
            {tab.label}
            {tab.count !== undefined && <span className={styles.count}>{tab.count}</span>}
          </button>
        );
      })}
    </div>
  );
}
