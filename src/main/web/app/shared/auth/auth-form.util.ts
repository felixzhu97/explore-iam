/** Appends a hidden form field. */
export function appendHidden(form: HTMLFormElement, name: string, value: string): void {
  const input = document.createElement('input');
  input.type = 'hidden';
  input.name = name;
  input.value = value;
  form.appendChild(input);
}

/** Reads the SPA CSRF cookie. */
export function readCsrfToken(): string | null {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
  if (!match) {
    return null;
  }
  try {
    return decodeURIComponent(match[1]);
  } catch {
    return match[1];
  }
}

/** Returns a safe relative continue path from a query value. */
export function safeContinuePath(value: string | null | undefined): string | null {
  const path = value?.trim() || null;
  if (!path?.startsWith('/') || path.startsWith('//')) {
    return null;
  }
  return path;
}
