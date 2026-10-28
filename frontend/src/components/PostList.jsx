import { CATEGORIES } from './PostForm.jsx';

export default function PostList({ posts, category, onCategoryChange, onEdit, onDelete, userId }) {
  return (
    <section className="card">
      <div className="list-header">
        <h2>Posts</h2>
        <label className="inline">
          Filter:
          <select
            aria-label="Filter by category"
            value={category}
            onChange={(e) => onCategoryChange(e.target.value)}
          >
            <option value="">all</option>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </label>
      </div>

      {posts.length === 0 && <p className="muted">No posts yet.</p>}

      <ul className="posts">
        {posts.map((post) => (
          <li key={post.id}>
            <h3>{post.title}</h3>
            <p className="meta">
              #{post.id} · {post.category} · by {post.authorId}
              {post.readTime != null && ` · ${post.readTime} min read`}
              {post.published && ' · published'}
            </p>
            <p>{post.excerpt}</p>
            {post.tags.length > 0 && (
              <p className="tags">
                {post.tags.map((t) => (
                  <span key={t} className="tag">{t}</span>
                ))}
              </p>
            )}
            {post.authorId === userId && (
              <div className="actions">
                <button className="secondary" onClick={() => onEdit(post)}>Edit</button>
                <button className="danger" onClick={() => onDelete(post)}>Delete</button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </section>
  );
}
