import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import PostForm from './PostForm.jsx';

async function fillRequired(user) {
  await user.type(screen.getByLabelText(/^title/i), 'My title');
  await user.type(screen.getByLabelText(/^content/i), 'My content');
  await user.selectOptions(screen.getByLabelText(/category/i), 'tech');
}

describe('PostForm', () => {
  it('submits a payload matching the spec', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<PostForm onSubmit={onSubmit} />);

    await fillRequired(user);
    await user.type(screen.getByLabelText(/tags/i), 'a, b');
    await user.type(screen.getByLabelText(/read time/i), '5');
    await user.click(screen.getByRole('button', { name: /create post/i }));

    expect(onSubmit).toHaveBeenCalledWith({
      title: 'My title',
      content: 'My content',
      category: 'tech',
      excerpt: undefined,
      tags: ['a', 'b'],
      readTime: 5,
    });
  });

  it('shows the server error message and field errors', async () => {
    const user = userEvent.setup();
    const err = Object.assign(new Error('Validation failed'), {
      details: { title: 'title is required' },
    });
    const onSubmit = vi.fn().mockRejectedValue(err);
    render(<PostForm onSubmit={onSubmit} />);

    await user.click(screen.getByRole('button', { name: /create post/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Validation failed');
    expect(screen.getByText('title is required')).toBeInTheDocument();
  });

  it('clears the form after a successful create', async () => {
    const user = userEvent.setup();
    render(<PostForm onSubmit={vi.fn().mockResolvedValue(undefined)} />);

    await fillRequired(user);
    await user.click(screen.getByRole('button', { name: /create post/i }));

    expect(screen.getByLabelText(/^title/i)).toHaveValue('');
  });
});
