export const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.error ?? `Request failed with status ${status}`);
    this.status = status;
    this.details = body?.details ?? {};
  }
}

async function request(path, { method = 'GET', userId, body } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (userId) headers['x-user-id'] = userId;

  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}

export function listPosts(category) {
  const query = category ? `?category=${encodeURIComponent(category)}` : '';
  return request(`/api/posts${query}`);
}

export function createPost(post, userId) {
  return request('/api/posts', { method: 'POST', userId, body: post });
}

export function updatePost(id, post, userId) {
  return request(`/api/posts/${id}`, { method: 'PUT', userId, body: post });
}

export function deletePost(id, userId) {
  return request(`/api/posts/${id}`, { method: 'DELETE', userId });
}
