import { afterEach, describe, expect, it } from 'vitest';
import { appendHidden, readCsrfToken, safeContinuePath } from './auth-form.util';

describe('safeContinuePath', () => {
  it('should keep a relative path when it starts with a single slash', () => {
    expect(safeContinuePath('/oauth2/authorize?client_id=explore-ai')).toBe(
      '/oauth2/authorize?client_id=explore-ai',
    );
  });

  it('should trim surrounding whitespace when the path is otherwise safe', () => {
    expect(safeContinuePath('  /clients  ')).toBe('/clients');
  });

  it.each([
    ['null', null],
    ['undefined', undefined],
    ['blank', '   '],
    ['absolute URL', 'https://evil.example/login'],
    ['protocol-relative URL', '//evil.example/login'],
    ['javascript URL', 'javascript:alert(1)'],
    ['bare path', 'clients'],
  ])('should reject a %s when it could leave the site', (_label, value) => {
    expect(safeContinuePath(value)).toBeNull();
  });
});

describe('readCsrfToken', () => {
  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  it('should return null when the cookie is absent', () => {
    expect(readCsrfToken()).toBeNull();
  });

  it('should decode the token when the cookie is present', () => {
    document.cookie = 'XSRF-TOKEN=a%2Bb';
    expect(readCsrfToken()).toBe('a+b');
  });
});

describe('appendHidden', () => {
  it('should add a hidden input with the name and value when called', () => {
    const form = document.createElement('form');
    appendHidden(form, 'state', 'xyz');
    const input = form.querySelector('input');
    expect(input?.type).toBe('hidden');
    expect(input?.name).toBe('state');
    expect(input?.value).toBe('xyz');
  });
});
