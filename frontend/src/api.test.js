import { afterEach, describe, expect, it, vi } from 'vitest';
import { createPost, deletePost, listPosts } from './api.js';

function mockFetch(status, body) {
  const fn = vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
  });
  vi.stubGlobal('fetch', fn);
  return fn;
}

afterEach(() => vi.unstubAllGlobals());

describe('api', () => {
  it('createPost POSTs JSON with the x-user-id header', async () => {
    const fetchMock = mockFetch(201, { id: 1 });
    await createPost({ title: 'T', content: 'C', category: 'tech' }, 'user-1');

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/posts');
    expect(init.method).toBe('POST');
    expect(init.headers['x-user-id']).toBe('user-1');
    expect(init.headers['Content-Type']).toBe('application/json');
    expect(JSON.parse(init.body)).toEqual({ title: 'T', content: 'C', category: 'tech' });
  });

  it('listPosts passes category as ?category=', async () => {
    const fetchMock = mockFetch(200, []);
    await listPosts('tech');
    expect(fetchMock.mock.calls[0][0]).toBe('http://localhost:8080/api/posts?category=tech');
  });

  it('listPosts without category has no query string', async () => {
    const fetchMock = mockFetch(200, []);
    await listPosts('');
    expect(fetchMock.mock.calls[0][0]).toBe('http://localhost:8080/api/posts');
  });

  it('deletePost sends DELETE with header and handles 204', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 204 });
    vi.stubGlobal('fetch', fetchMock);
    await expect(deletePost(5, 'user-1')).resolves.toBeNull();
    expect(fetchMock.mock.calls[0][1].method).toBe('DELETE');
    expect(fetchMock.mock.calls[0][1].headers['x-user-id']).toBe('user-1');
  });

  it('throws ApiError carrying the server message and field details', async () => {
    mockFetch(400, { error: 'Validation failed', details: { title: 'title is required' } });
    await expect(createPost({}, 'user-1')).rejects.toMatchObject({
      status: 400,
      message: 'Validation failed',
      details: { title: 'title is required' },
    });
  });
});
