import { useEffect, useState } from 'react';
import LoginCard from './components/LoginCard';
import ChatPage from './pages/ChatPage';
import './App.css';

function App() {
  const [username, setUsername] = useState(() => localStorage.getItem('chat-username') || '');

  useEffect(() => {
    if (username) {
      localStorage.setItem('chat-username', username);
    }
  }, [username]);

  if (username) {
    return <ChatPage username={username} onChangeUser={setUsername} />;
  }

  return (
    <div className="page">
      <div className="bg-accents" />
      <header className="topbar">
        <span className="brand">Wavechat</span>
        <span className="pill">Step 1 · Choose a name</span>
      </header>

      <main className="hero-grid">
        <section className="copy">
          <p className="eyebrow">Welcome</p>
          <h1>Pick a username to start chatting</h1>
          <p className="lede">
            There is no password or signup flow. Choose the name you want to appear as, and
            we will bring you to the chat space next.
          </p>
          <ul className="checklist">
            <li>Personalize your handle</li>
            <li>We remember it locally for your next visit</li>
            <li>No auth, no friction</li>
          </ul>
        </section>

        <LoginCard
          defaultName={username}
          onSubmit={(name) => {
            setUsername(name);
          }}
        />
      </main>
    </div>
  );
}

export default App;
