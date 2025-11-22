import { useState } from 'react';

function LoginCard({ defaultName = '', onSubmit }) {
  const [name, setName] = useState(defaultName);
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    const trimmed = name.trim();
    if (!trimmed) {
      setError('Please choose a username.');
      return;
    }
    setError('');
    onSubmit(trimmed);
  };

  return (
    <div className="card">
      <div>
        <p className="card-kicker">Login</p>
        <h2>Choose your display name</h2>
        <p className="card-sub">You can change this anytime.</p>
      </div>

      <form className="form" onSubmit={handleSubmit}>
        <label className="field">
          <span>Username</span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. alex, skywalker"
            autoFocus
          />
        </label>
        {error && <p className="error">{error}</p>}

        <button type="submit" className="primary">
          Continue to chat
        </button>
      </form>

      <div className="tip">We don&apos;t ask for passwords or email. Just a name.</div>
    </div>
  );
}

export default LoginCard;
