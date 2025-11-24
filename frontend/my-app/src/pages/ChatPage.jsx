import { useEffect, useMemo, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import axios from 'axios';

const apiBase = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').replace(/\/$/, '');

const statusCopy = {
  disconnected: 'Disconnected',
  connecting: 'Connecting…',
  connected: 'Connected',
  error: 'Error',
};

function ChatPage({ username, onChangeUser }) {
  const [status, setStatus] = useState('disconnected');
  const [targetId, setTargetId] = useState('');
  const [notice, setNotice] = useState('Choose who to talk to, then connect.');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newRoomType, setNewRoomType] = useState('private');
  const [newOtherUser, setNewOtherUser] = useState('');
  const [newGroupName, setNewGroupName] = useState('');
  const [newGroupMembers, setNewGroupMembers] = useState(username);
  const [roomLabel, setRoomLabel] = useState('');
  const [roomMembers, setRoomMembers] = useState([]);
  const [presence, setPresence] = useState({});
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState('');
  const [typingUser, setTypingUser] = useState('');
  const clientRef = useRef(null);
  const typingTimeoutRef = useRef(null);
  const lastTypingRef = useRef(0);
  const messagesRef = useRef(null);

  const initials = useMemo(() => {
    if (!username) return '?';
    const trimmed = username.trim();
    if (!trimmed) return '?';
    const parts = trimmed.split(/\s+/);
    const first = parts[0]?.[0] || '';
    const last = parts[1]?.[0] || '';
    return (first + last).toUpperCase() || trimmed[0].toUpperCase();
  }, [username]);

  useEffect(() => {
    return () => {
      clientRef.current?.deactivate();
    };
  }, []);

  const scrollMessagesToEnd = () => {
    if (messagesRef.current) {
      messagesRef.current.scrollTop = messagesRef.current.scrollHeight;
    }
  };

  useEffect(() => {
    scrollMessagesToEnd();
  }, [messages]);

  useEffect(() => {
    if (!roomMembers.length) return undefined;
    refreshPresence();
    const id = setInterval(refreshPresence, 5000);
    return () => clearInterval(id);
  }, [roomMembers]);

  const resolveRoom = async (key) => {
    const trimmed = key.trim();
    if (!trimmed) return { roomId: '', roomName: '', members: [] };

    // 1) Try to resolve by name/id directly.
    try {
      const res = await axios.get(`${apiBase}/api/chatrooms/${trimmed}`);
      return {
        roomId: res?.data?.uid || trimmed,
        roomName: res?.data?.name || trimmed,
        members: res?.data?.memberIds || [],
      };
    } catch (err) {
      // ignore and fall through
    }

    // 2) If a single username was provided (no dash), try/find a private room.
    if (!trimmed.includes('-')) {
      try {
        const res = await axios.post(`${apiBase}/api/chatrooms/private`, {
          createdBy: username,
          user1: username,
          user2: trimmed,
        });
        return {
          roomId: res?.data?.uid || trimmed,
          roomName: res?.data?.name || trimmed,
          members: res?.data?.memberIds || [username, trimmed],
        };
      } catch (err) {
        return { roomId: '', roomName: '', members: [] };
      }
    }

    // 3) Fallback to whatever was typed.
    return { roomId: trimmed, roomName: trimmed, members: [] };
  };

  const fetchRoomMembers = async (roomId) => {
    try {
      const res = await axios.get(`${apiBase}/api/chatrooms/${roomId}`);
      setRoomMembers(res?.data?.memberIds || []);
    } catch (err) {
      // ignore
    }
  };

  const refreshPresence = async () => {
    if (!roomMembers.length) return;
    try {
      const res = await axios.get(`${apiBase}/api/presence`, {
        params: { ids: roomMembers },
      });
      const map = {};
      (res.data || []).forEach((p) => {
        map[p.userId] = p.status;
      });
      setPresence(map);
    } catch (err) {
      // ignore
    }
  };

  const connect = async () => {
    if (status === 'connecting' || status === 'connected') return;
    if (!targetId.trim()) {
      setNotice('Add a room or username to chat with first.');
      return;
    }
    setStatus('connecting');
    setNotice('Resolving room…');

    const { roomId, roomName, members } = await resolveRoom(targetId);
    if (!roomId) {
      setStatus('disconnected');
      setNotice('Room not found.');
      return;
    }
    setTargetId(roomId);
    setRoomLabel(roomName);
    setRoomMembers(members || []);
    setNotice('Connecting…');

    const client = new Client({
      webSocketFactory: () => new SockJS(`${apiBase}/ws`),
      reconnectDelay: 0,
      connectHeaders: { userId: username },
      onConnect: () => {
        setStatus('connected');
        setNotice(`Connected as ${username}. Ready to chat with ${targetId}.`);
        fetchHistory(roomId);
        if (roomMembers.length === 0) {
          fetchRoomMembers(roomId);
        }
        client.subscribe(`/topic/room/${roomId}`, (frame) => {
          const msg = JSON.parse(frame.body);
          setMessages((prev) => {
            const exists = prev.some((m) => m.uid === msg.uid);
            return exists ? prev : [...prev, msg];
          });
        });
        client.subscribe(`/topic/room/${roomId}/typing`, (frame) => {
          const typer = frame.body;
          if (!typer || typer === username) return;
          setTypingUser(typer);
          clearTimeout(typingTimeoutRef.current);
          typingTimeoutRef.current = setTimeout(() => setTypingUser(''), 1500);
        });
      },
      onStompError: (frame) => {
        setStatus('error');
        setNotice(frame?.headers['message'] || 'WebSocket error');
      },
      onWebSocketClose: () => {
        setStatus('disconnected');
        setNotice('Disconnected. You can reconnect anytime.');
      },
    });

    client.activate();
    clientRef.current = client;
  };

  const disconnect = () => {
    setNotice('Disconnecting…');
    clientRef.current?.deactivate();
    clientRef.current = null;
    setStatus('disconnected');
    setNotice('Disconnected.');
  };

  const toggleConnection = () => {
    if (status === 'connected' || status === 'connecting') {
      disconnect();
    } else {
      connect();
    }
  };

  const logout = () => {
    clientRef.current?.deactivate();
    clientRef.current = null;
    setStatus('disconnected');
    setTargetId('');
    setRoomLabel('');
    setMessages([]);
    setMessageText('');
    setNotice('Choose who to talk to, then connect.');
    localStorage.removeItem('chat-username');
    onChangeUser?.('');
  };

  const createChat = async () => {
    try {
      if (newRoomType === 'private') {
        if (!newOtherUser.trim()) {
          setNotice('Enter the other user to start a private chat.');
          return;
        }
        const res = await axios.post(`${apiBase}/api/chatrooms/private`, {
          createdBy: username,
          user1: username,
          user2: newOtherUser.trim(),
        });
        const uid = res?.data?.uid;
        if (uid) {
          setTargetId(uid);
          setRoomLabel(res?.data?.name || newOtherUser.trim());
          setRoomMembers(res?.data?.memberIds || [username, newOtherUser.trim()]);
        }
        setNotice(uid ? `Private chat ready (room: ${res?.data?.name}).` : 'Private chat ready.');
      } else {
        if (!newGroupName.trim()) {
          setNotice('Enter a group name.');
          return;
        }
        const members = newGroupMembers
          .split(',')
          .map((m) => m.trim())
          .filter(Boolean);
        if (!members.includes(username)) {
          members.unshift(username);
        }
        const res = await axios.post(`${apiBase}/api/chatrooms/group`, {
          createdBy: username,
          name: newGroupName.trim(),
          memberIds: members,
        });
        const uid = res?.data?.uid;
        setTargetId(uid || newGroupName.trim());
        setRoomLabel(res?.data?.name || newGroupName.trim());
        setRoomMembers(res?.data?.memberIds || members);
        setNotice(uid ? `Group ready (room: ${uid}).` : 'Group ready.');
      }
      setShowCreateModal(false);
    } catch (err) {
      console.error(err);
      setNotice('Could not create or fetch chat. Check console.');
    }
  };

  const loadConversation = async () => {
    if (!targetId.trim()) {
      setNotice('Add a room name first.');
      return;
    }
    setNotice('Loading conversation…');
    const { roomId, roomName, members } = await resolveRoom(targetId);
    if (!roomId) {
      setNotice('Room not found.');
      return;
    }
    setTargetId(roomId);
    setRoomLabel(roomName);
    setRoomMembers(members || []);
    await fetchHistory(roomId);
  };

  const fetchHistory = async (roomId) => {
    try {
      const res = await axios.get(`${apiBase}/api/messages/conversation`, {
        params: { roomId },
      });
      setMessages(res.data || []);
      setNotice(`Loaded ${res.data?.length || 0} messages.`);
    } catch (err) {
      console.error(err);
      setNotice('Could not load history.');
    }
  };

  const sendTyping = () => {
    const now = Date.now();
    if (!clientRef.current?.active || !targetId.trim()) return;
    if (now - lastTypingRef.current < 900) return;
    lastTypingRef.current = now;
    clientRef.current.publish({
      destination: '/app/typing',
      body: JSON.stringify({ roomId: targetId.trim(), userId: username }),
    });
  };

  const sendMessage = () => {
    const content = messageText.trim();
    if (!content || !clientRef.current?.active || !targetId.trim()) return;
    clientRef.current.publish({
      destination: '/app/message/send',
      body: JSON.stringify({ senderId: username, roomId: targetId.trim(), content }),
    });
    setMessageText('');
  };

  return (
    <div className="page chat-page">
      <div className="bg-accents" />
      <header className="chatbar">
        <div className="identity">
          <div className="avatar">{initials}</div>
          <div className="user-meta">
            <span className="label">Logged in as</span>
            <strong>{username}</strong>
          </div>
          <div className="target">
            <label htmlFor="target">Room / user</label>
            <input
              id="target"
              value={targetId}
              onChange={(e) => setTargetId(e.target.value)}
              placeholder="room name, room id, or username"
            />
          </div>
        </div>
        <div className="actions">
          <button className="secondary" type="button" onClick={logout}>
            Logout
          </button>
          <button className="secondary" type="button" onClick={() => setShowCreateModal(true)}>
            New chat
          </button>
          <span className={`status-badge status-${status}`}>{statusCopy[status] || status}</span>
          <button className="primary" onClick={toggleConnection}>
            {status === 'connected' || status === 'connecting' ? 'Disconnect' : 'Connect'}
          </button>
        </div>
      </header>

      <section className="shell">
        <div className="toolbar">
          <div className="notice">{notice}</div>
          <div className="toolbar-actions">
            <button type="button" className="secondary" onClick={loadConversation}>
              Load conversation
            </button>
          </div>
        </div>

        <div className="chat-shell">
          <div className="chat-header">
            <div>
              <p className="label">Chatting with</p>
              <h3>{roomLabel || targetId || 'Set a room or user'}</h3>
            </div>
            <div className="chat-presence">
              <span className={`dot dot-${status}`} />
              <span>{statusCopy[status] || status}</span>
              {typingUser && <span className="typing-pill">{typingUser} is typing…</span>}
            </div>
          </div>

            <div className="chat-layout">
              <aside className="sidebar">
                <div className="sidebar-head">Participants</div>
                {(roomMembers.length ? roomMembers : [username]).map((u) => {
                  const raw = presence[u] || (u === username ? 'ONLINE' : 'UNKNOWN');
                  const state =
                    typingUser === u ? 'TYPING' : raw === 'TYPING' ? 'ONLINE' : raw;
                  const safeState = state === 'UNKNOWN' ? 'OFFLINE' : state;
                  return (
                    <div key={u} className="user-row">
                      <span className={`dot dot-mini dot-${safeState.toLowerCase()}`} />
                      <span className="user-name">{u}</span>
                      <span className="user-state">{safeState.toLowerCase()}</span>
                    </div>
                  );
                })}
              </aside>

            <div className="chat-main">
              <div className="messages" ref={messagesRef}>
                {messages.map((msg) => (
                  <div
                    key={msg.uid || `${msg.senderId}-${msg.timestamp}`}
                    className={`bubble ${msg.senderId === username ? 'me' : 'them'}`}
                  >
                    <div className="bubble-meta">
                      <span>{msg.senderId}</span>
                      <time>{new Date(msg.timestamp).toLocaleTimeString()}</time>
                    </div>
                    <p>{msg.content}</p>
                  </div>
                ))}
                {messages.length === 0 && (
                  <div className="placeholder ghost">No messages yet. Say hello 👋</div>
                )}
              </div>

              <div className="composer">
                <input
                  value={messageText}
                  onChange={(e) => {
                    setMessageText(e.target.value);
                    sendTyping();
                  }}
                  placeholder="Type a message"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      sendMessage();
                    }
                  }}
                  disabled={!targetId.trim() || status !== 'connected'}
                />
                <button
                  className="primary"
                  onClick={sendMessage}
                  disabled={!targetId.trim() || status !== 'connected'}
                >
                  Send
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {showCreateModal && (
        <div className="modal-backdrop" onClick={() => setShowCreateModal(false)}>
          <div
            className="modal"
            onClick={(e) => {
              e.stopPropagation();
            }}
          >
            <div className="modal-header">
              <div>
                <p className="card-kicker">New chat</p>
                <h2>Create a room</h2>
              </div>
              <button className="close" onClick={() => setShowCreateModal(false)}>
                ×
              </button>
            </div>

            <div className="form-grid-modal">
              <div className="field inline">
                <span>Type</span>
                <div className="segmented">
                  <button
                    type="button"
                    className={newRoomType === 'private' ? 'seg active' : 'seg'}
                    onClick={() => setNewRoomType('private')}
                  >
                    Private
                  </button>
                  <button
                    type="button"
                    className={newRoomType === 'group' ? 'seg active' : 'seg'}
                    onClick={() => setNewRoomType('group')}
                  >
                    Group
                  </button>
                </div>
              </div>

              {newRoomType === 'private' ? (
                <div className="field inline grow">
                  <span>Other user</span>
                  <input
                    value={newOtherUser}
                    onChange={(e) => setNewOtherUser(e.target.value)}
                    placeholder="username to chat with"
                    autoFocus
                  />
                </div>
              ) : (
                <>
                  <div className="field inline grow">
                    <span>Group name</span>
                    <input
                      value={newGroupName}
                      onChange={(e) => setNewGroupName(e.target.value)}
                      placeholder="Project squad"
                      autoFocus
                    />
                  </div>
                  <div className="field inline grow">
                    <span>Members (comma separated)</span>
                    <input
                      value={newGroupMembers}
                      onChange={(e) => setNewGroupMembers(e.target.value)}
                      placeholder="you, teammate1, teammate2"
                    />
                  </div>
                </>
              )}
            </div>

            <div className="modal-actions">
              <button className="secondary" type="button" onClick={() => setShowCreateModal(false)}>
                Cancel
              </button>
              <button className="primary" type="button" onClick={createChat}>
                Create
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ChatPage;
