// BookDetails.js

import React, {useEffect, useRef, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {ReactReader} from 'react-reader';
import ShareLinkModal from './ShareLinkModal';
import LinksListModal from './LinksListModal';
import './BookDetail.css';

function BookDetail() {
  const {id} = useParams();
  const [userBook, setUserBook] = useState(null);
  const [url, setUrl] = useState(null);
  const [showShareLinkModal, setShowShareLinkModal] = useState(false);
  const [showLinksListModal, setShowLinksListModal] = useState(false);
  const socket = useRef(null);
  const [onlineUsers, setOnlineUsers] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');

    if (!socket.current) {
      socket.current = new WebSocket("ws://localhost:8002/api/v1/ws/reader?auth=" + token + "&bookID=" + id);

      socket.current.onopen = () => {
        console.log("ws opened");
        socket.current.send(JSON.stringify({type: "CONNECT", userId: "someUserId"})); // Replace with the actual user ID
      }

      socket.current.onmessage = (message) => {
        const data = JSON.parse(message.data);
        setOnlineUsers(data);
        console.log(data);
      }

      socket.current.onerror = (error) => {
        console.log("ws error", error);
      }

      socket.current.onclose = () => {
        console.log("ws closed");
      }
    }

    return () => {
      if (socket.current && socket.current.readyState === WebSocket.OPEN) {
        socket.current.close();
      }
    };
  }, [id]); // Only run the effect when 'id' changes

  const [page, setPage] = useState('')
  const renditionRef = useRef(null)
  const tocRef = useRef(null)
  const navigate = useNavigate();

  // Handler for location changes
  const locationChanged = epubcifi => {
    if (renditionRef.current && tocRef.current) {
      const {displayed, href} = renditionRef.current.location.start
      const chapter = tocRef.current.find(item => item.href === href)
      setPage(
        `Page ${displayed.page} of ${displayed.total} in chapter ${
          chapter ? chapter.label : 'n/a'
        }`
      )

      // Send new location to server over WebSocket
      if (socket.current && socket.current.readyState === WebSocket.OPEN) {
        const message = JSON.stringify({
          bookID: id,
          location: epubcifi,
          chapter: chapter ? chapter.label : 'n/a'
        });
        socket.current.send(message);
      }
    }
  }

  useEffect(() => {
    const token = localStorage.getItem('token');

    if (!token) {
      navigate('/login');
      return;
    }

    const fetchData = async () => {
      const bookDetailsResponse = await fetch(`http://localhost:8002/api/v1/book/${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!bookDetailsResponse.ok) {
        console.error('Failed to fetch book details');
        return;
      }

      const bookData = await bookDetailsResponse.json();
      setUserBook(bookData.book);

      const bookUrlResponse = await fetch(`http://localhost:8002/api/v1/book/${id}/url`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!bookUrlResponse.ok) {
        console.error('Failed to fetch book URL');
        return;
      }

      const urlData = await bookUrlResponse.json();
      setUrl(urlData.url);
    };

    fetchData();
  }, [id, navigate]);

  if (!userBook || !url) {
    return <p>Loading...</p>;
  }

  return (
    <div>
      <table className="info-table">
        <tbody>
        <tr>
          <td><strong>Title:</strong></td>
          <td>{userBook.book.title}</td>
        </tr>
        <tr>
          <td><strong>Author:</strong></td>
          <td>{userBook.book.author}</td>
        </tr>
        <tr>
          <td><strong>Description:</strong></td>
          <td>{userBook.book.description}</td>
        </tr>
        </tbody>
      </table>
      <div className="book-buttons">
        <button onClick={() => setShowShareLinkModal(true)}>Create share link</button>
        <button onClick={() => setShowLinksListModal(true)}>See all share links</button>
        {showShareLinkModal &&
          <ShareLinkModal closeModal={() => setShowShareLinkModal(false)} bookId={userBook.book.id}/>}
        {showLinksListModal &&
          <LinksListModal closeModal={() => setShowLinksListModal(false)} bookId={userBook.book.id}/>}
      </div>
      <div className="reader-container" style={{height: '600px', position: 'relative'}}>
        <ReactReader
          url={url}
          title={userBook.book.title}
          location={userBook.settings?.location || "epubcfi(/6/2[cover]!/6)"}
          locationChanged={locationChanged}
          getRendition={rendition => (renditionRef.current = rendition)}
          tocChanged={toc => (tocRef.current = toc)}
          showToc={true}
          epubOptions={{
            allowPopups: true, // Adds `allow-popups` to sandbox-attribute
            allowScriptedContent: true, // Adds `allow-scripts` to sandbox-attribute
          }}
        />
      </div>
      {page}
      <div style={{height: '50px'}}></div>
      {onlineUsers && onlineUsers.connected_users && onlineUsers.connected_users.length > 0 &&
        <div className="online-users">
          <h3>Online Users:</h3>
          <div style={{ position: 'relative', width: '100%', height: '60px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ width: '100%', height: '20px', border: '2px solid #007BFF', borderRadius: '20px', position: 'relative' }}>
              {onlineUsers.connected_users.map((user) => {
                const progress = user.user_settings.progress;
                return (
                  <div key={user.user_settings.user_id} style={{ position: 'absolute', left: `calc(${progress}% - 10px)`, top: '50%', transform: 'translateY(-50%)'}}>
                    <div
                      style={{
                        width: '20px',
                        height: '20px',
                        backgroundColor: user.user_settings.colour !== "" ? user.user_settings.colour : '#007BFF',
                        borderRadius: '50%',
                      }}
                      title={user.user_settings.user_id}
                    />
                    <div style={{ position: 'absolute', top: '110%', width: '50px', textAlign: 'center', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {`${progress}%`}
                    </div>
                  </div>
                );
              })}
            </div>
            <div style={{ position: 'absolute', top: '101%', left: '0' }}>0%</div>
            <div style={{ position: 'absolute', top: '101%', right: '0' }}>100%</div>
          </div>
        </div>
      }
      <div style={{height: '50px'}}></div>
    </div>
  );
}

export default BookDetail;
