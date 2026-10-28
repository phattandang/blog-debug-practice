// Converts the form's string state into the JSON body the API expects.
export function toPayload(form) {
  const tags = form.tags
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean);

  return {
    title: form.title,
    content: form.content,
    category: form.category,
    excerpt: form.excerpt.trim() === '' ? undefined : form.excerpt,
    tags,
    readTime: form.readTime === '' ? undefined : Number(form.readTime),
  };
}

export function toForm(post) {
  return {
    title: post?.title ?? '',
    content: post?.content ?? '',
    category: post?.category ?? '',
    excerpt: post?.excerpt ?? '',
    tags: (post?.tags ?? []).join(', '),
    readTime: post?.readTime == null ? '' : String(post.readTime),
  };
}
