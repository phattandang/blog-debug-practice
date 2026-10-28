import { useCallback, useEffect, useState } from 'react';
import { createPost, deletePost, listPosts, updatePost } from './api.js';
import PostForm from './components/PostForm.jsx';
import PostList from './components/PostList.jsx';

export default function App() {
  const [userId, setUserId] = useState('user-1');
  const [posts, setPosts] = useState([]);
  const [category, setCategory] = useState('');
  const [editing, setEditing] = useState(null);
  const [loadError, setLoadError] = useState(null);

  const refresh = useCallback(async () => {
    try {
      setLoadError(null);
      setPosts(await listPosts(category));
    } catch (err) {
      setLoadError(err.message);
    }
  }, [category]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function handleSubmit(payload) {
    if (editing) {
      await updatePost(editing.id, payload, userId);
      setEditing(null);
    } else {
      await createPost(payload, userId);
    }
    await refresh();
  }

  async function handleDelete(post) {
    try {
      await deletePost(post.id, userId);
      await refresh();
    } catch (err) {
      setLoadError(err.message);
    }
  }

  return (
    <main>
      <header>
        <h1>Blog Debug Practice</h1>
        <label className="inline">
          Acting as user:
          <input aria-label="User id" value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
      </header>

      {loadError && <div className="error" role="alert">Could not load posts: {loadError}</div>}

      <div className="layout">
        <PostForm
          initialPost={editing}
          onSubmit={handleSubmit}
          onCancel={editing ? () => setEditing(null) : undefined}
        />
        <PostList
          posts={posts}
          category={category}
          onCategoryChange={setCategory}
          onEdit={setEditing}
          onDelete={handleDelete}
          userId={userId}
        />
      </div>
    </main>
  );
}
