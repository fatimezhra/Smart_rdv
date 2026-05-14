import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';

describe('Button Component', () => {
  test('renders button with text', () => {
    render(<button>Click me</button>);
    expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument();
  });

  test('calls onClick when clicked', () => {
    const handleClick = jest.fn();
    render(<button onClick={handleClick}>Click me</button>);
    
    fireEvent.click(screen.getByRole('button', { name: /click me/i }));
    
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  test('applies correct className', () => {
    render(<button className="btn-primary">Click me</button>);
    expect(screen.getByRole('button')).toHaveClass('btn-primary');
  });

  test('is disabled when disabled prop is true', () => {
    render(<button disabled>Click me</button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
