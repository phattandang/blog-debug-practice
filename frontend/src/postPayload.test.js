import { describe, expect, it } from 'vitest';
import { toPayload } from './postPayload.js';

const base = { title: 'T', content: 'C', category: 'tech', excerpt: '', tags: '', readTime: '' };

describe('toPayload', () => {
  it('converts readTime to a number', () => {
    expect(toPayload({ ...base, readTime: '7' }).readTime).toBe(7);
  });

  it('omits readTime when empty', () => {
    expect(toPayload(base).readTime).toBeUndefined();
  });

  it('splits tags into a trimmed array', () => {
    expect(toPayload({ ...base, tags: ' java, spring ,, ' }).tags).toEqual(['java', 'spring']);
  });

  it('sends an empty tags array when no tags are entered', () => {
    expect(toPayload(base).tags).toEqual([]);
  });

  it('omits blank excerpt so the server generates one', () => {
    expect(toPayload({ ...base, excerpt: '   ' }).excerpt).toBeUndefined();
  });
});
