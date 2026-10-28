import { useEffect, useState } from 'react';
import { toForm, toPayload } from '../postPayload.js';

export const CATEGORIES = ['tech', 'life', 'travel', 'food'];

export default function PostForm({ initialPost, onSubmit, onCancel }) {
  const [form, setForm] = useState(toForm(initialPost));
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setForm(toForm(initialPost));
    setError(null);
  }, [initialPost]);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit(toPayload(form));
      if (!initialPost) setForm(toForm(null));
    } catch (err) {
      setError(err);
    } finally {
      setSubmitting(false);
    }
  }

  const fieldError = (name) => error?.details?.[name];

  return (
    <form className="card" onSubmit={handleSubmit} noValidate>
      <h2>{initialPost ? `Edit post #${initialPost.id}` : 'New post'}</h2>

      {error && (
        <div className="error" role="alert">
          {error.message}
        </div>
      )}

      <label>
        Title *
        <input name="title" value={form.title} onChange={handleChange} />
        {fieldError('title') && <span className="field-error">{fieldError('title')}</span>}
      </label>

      <label>
        Content *
        <textarea name="content" rows={5} value={form.content} onChange={handleChange} />
        {fieldError('content') && <span className="field-error">{fieldError('content')}</span>}
      </label>

      <label>
        Category *
        <select name="category" value={form.category} onChange={handleChange}>
          <option value="">-- choose --</option>
          {CATEGORIES.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
        {fieldError('category') && <span className="field-error">{fieldError('category')}</span>}
      </label>

      <label>
        Excerpt (optional, defaults to first 150 chars of content)
        <input name="excerpt" value={form.excerpt} onChange={handleChange} />
      </label>

      <label>
        Tags (comma separated)
        <input name="tags" value={form.tags} onChange={handleChange} />
      </label>

      <label>
        Read time (minutes)
        <input name="readTime" type="number" min="0" value={form.readTime} onChange={handleChange} />
        {fieldError('readTime') && <span className="field-error">{fieldError('readTime')}</span>}
      </label>

      <div className="actions">
        <button type="submit" disabled={submitting}>
          {submitting ? 'Saving...' : initialPost ? 'Save changes' : 'Create post'}
        </button>
        {onCancel && (
          <button type="button" className="secondary" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
