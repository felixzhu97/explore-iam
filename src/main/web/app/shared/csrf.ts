export function csrfHeaders(): Record<string, string> {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
  if (!match) {
    return {};
  }
  try {
    return { 'X-XSRF-TOKEN': decodeURIComponent(match[1]) };
  } catch {
    return { 'X-XSRF-TOKEN': match[1] };
  }
}
