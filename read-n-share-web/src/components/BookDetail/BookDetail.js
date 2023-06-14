// BookDetails.js

import React, {useEffect, useRef, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {ReactReader} from 'react-reader';
import ShareLinkModal from './ShareLinkModal';
import LinksListModal from './LinksListModal';
import './BookDetail.css';

function BookDetail() {
  const {id} = useParams();
  const [userId, setUserId] = useState(null);
  const [userBook, setUserBook] = useState(null);
  const [url, setUrl] = useState(null);
  const [usersSettings, setUsersSettings] = useState(null);
  const [showShareLinkModal, setShowShareLinkModal] = useState(false);
  const [showLinksListModal, setShowLinksListModal] = useState(false);
  const socket = useRef(null);
  const [onlineUsers, setOnlineUsers] = useState([]);

  const updateUserSettings = (usersSettings, onlineUsers) => {
    if (!usersSettings || !onlineUsers) {
      return usersSettings;
    }

    const updatedSettings = [...usersSettings.settings];  // create a shallow copy
    onlineUsers.connected_users.forEach((onlineUser) => {
      const index = updatedSettings.findIndex(user => user.user_id === onlineUser.user_settings.user_id);
      if (index !== -1) {
        // user found, update settings
        updatedSettings[index] = {...updatedSettings[index], ...onlineUser.user_settings};
      } else {
        // user not found, insert new settings
        updatedSettings.push({user_id: onlineUser.user_settings.user_id, ...onlineUser.user_settings});
      }

      if (!onlineUser.user_settings.selections) {
        return;
      }
      onlineUser.user_settings.selections.forEach((selection) => {
        const selectionsIndex = selections.findIndex(s => s.cfiRange === selection.cfi_range);
        if (selectionsIndex === -1) {
          if (renditionRef.current) {
            renditionRef.current.annotations.add('highlight', selection.cfi_range, {}, null, 'hl', {
              fill: 'red',
              'fill-opacity': '0.5',
              'mix-blend-mode': 'multiply'
            })
            // contents.window.getSelection().removeAllRanges()
          }
        }
      });
    });

    return {...usersSettings, settings: updatedSettings};
  };

  useEffect(() => {
    const token = localStorage.getItem('token');

    if (!socket.current) {
      socket.current = new WebSocket("ws://3.85.229.215:8002/api/v1/ws/reader?auth=" + token + "&bookID=" + id);

      socket.current.onopen = () => {
        console.log("ws opened");
        socket.current.send(JSON.stringify({type: "CONNECT", userId: "someUserId"})); // Replace with the actual user ID
      }

      socket.current.onmessage = (message) => {
        const data = JSON.parse(message.data);
        setOnlineUsers(data);
        setUsersSettings(prevUsersSettings => updateUserSettings(prevUsersSettings, data));
        console.log(data);
      }

      socket.current.onerror = (error) => {
        console.log("ws error", error);
      }

      socket.current.onclose = () => {
        setOnlineUsers(null);
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
      setPage(`Page ${displayed.page} of ${displayed.total} in chapter ${chapter ? chapter.label : 'n/a'}`)

      // Send new location to server over WebSocket
      if (socket.current && socket.current.readyState === WebSocket.OPEN) {
        const message = JSON.stringify({
          bookID: id, location: epubcifi, chapter: chapter ? chapter.label : 'n/a'
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
        const bookDetailsResponse = await fetch(`http://3.85.229.215:8002/api/v1/book/${id}`, {
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
      setUserId(bookData.userId);

        const bookUrlResponse = await fetch(`http://3.85.229.215:8002/api/v1/book/${id}/url`, {
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

        const bookUserSettingsResponse = await fetch(`http://3.85.229.215:8002/api/v1/book/${id}/settings`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

      if (!bookUserSettingsResponse.ok) {
        console.error('Failed to fetch book user settings');
        return;
      }

      const userSettingsData = await bookUserSettingsResponse.json();
      setUsersSettings(userSettingsData);
    };

    fetchData();
  }, [id, navigate]);

  const [selections, setSelections] = useState([]);

  useEffect(() => {
    if (renditionRef.current) {
      function setRenderSelection(cfiRange, contents) {
        setUsersSettings(prevState => {
          const newUserSettings = {...prevState};  // Copy the previous state
          const userSetting = newUserSettings.settings.find(x => x.user_id === userId);

          // Initialize selections array if it doesn't exist
          if (!userSetting.selections) {
            userSetting.selections = [];
          }

          // Append the new selection to the array
          userSetting.selections.push({
            cfi_range: cfiRange, user_id: userId, book_id: id, text: contents.window.getSelection().toString()
          });

          console.log(userSetting);

          // Send updated user settings through WebSocket
          if (socket.current && socket.current.readyState === WebSocket.OPEN) {
            socket.current.send(JSON.stringify(userSetting));
          }

          // Add a highlight annotation
          renditionRef.current.annotations.add('highlight', cfiRange, {}, null, 'hl', {
            fill: 'red',
            'fill-opacity': '0.5',
            'mix-blend-mode': 'multiply'
          });
          contents.window.getSelection().removeAllRanges();

          // Return updated user settings as new state
          return newUserSettings;
        });
      }

      renditionRef.current.on('selected', setRenderSelection);
      return () => {
        renditionRef.current.off('selected', setRenderSelection);
      };
    }
  }, [selections, setSelections]);

  const [editing, setEditing] = useState(null);
  const [note, setNote] = useState('');

  const submitNote = (selection) => {
    // Update state
    setUsersSettings(prevState => {
      const newUserSettings = {...prevState};  // Copy the previous state
      const userSetting = newUserSettings.settings.find(x => x.user_id === userId);

      // Find the selected text in user's selections array and update note
      const userSelection = userSetting.selections.find(sel => sel.cfi_range === selection.cfi_range);
      if (userSelection) {
        userSelection.user_text = note;
      }

      // Send updated user settings through WebSocket
      if (socket.current && socket.current.readyState === WebSocket.OPEN) {
        socket.current.send(JSON.stringify(userSetting));
      }

      // Return updated user settings as new state
      return newUserSettings;
    });

    setEditing(null);
    setNote('');
  };

  if (!userBook || !url) {
    return <p>Loading...</p>;
  }

  return (<div>
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
          getRendition={rendition => {
            renditionRef.current = rendition
            renditionRef.current.themes.default({
              '::selection': {
                background: 'orange'
              }
            })
            setSelections([])
          }}
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
          <div style={{
            position: 'relative',
            width: '100%',
            height: '60px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <div style={{
              width: '100%', height: '20px', border: '2px solid #007BFF', borderRadius: '20px', position: 'relative'
            }}>
              {onlineUsers.connected_users.map((user) => {
                const progress = user.user_settings.progress;
                return (<div key={user.user_settings.user_id} style={{
                  position: 'absolute', left: `calc(${progress}% - 10px)`, top: '50%', transform: 'translateY(-50%)'
                }}>
                  <div
                    style={{
                      width: '20px',
                      height: '20px',
                      backgroundColor: user.user_settings.colour !== "" ? user.user_settings.colour : '#007BFF',
                      borderRadius: '50%',
                    }}
                    title={user.user_settings.user_id}
                  />
                  <div style={{
                    position: 'absolute',
                    top: '110%',
                    width: '50px',
                    textAlign: 'center',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis'
                  }}>
                    {`${progress}%`}
                  </div>
                </div>);
              })}
            </div>
            <div style={{position: 'absolute', top: '101%', left: '0'}}>0%</div>
            <div style={{position: 'absolute', top: '101%', right: '0'}}>100%</div>
          </div>
        </div>}
      <div style={{height: '50px'}}></div>
      {usersSettings && usersSettings.settings && usersSettings.settings.length > 0 && <div className="user-settings">
        <h3>Selections:</h3>
        <table className="table">
          <thead>
          <tr>
            <th scope="col" style={{width: '10%'}}>Username</th>
            <th scope="col" style={{width: '40%'}}>Selected Text</th>
            <th scope="col" style={{width: '40%'}}>Note</th>
            <th scope="col" style={{width: '6%'}}>Show</th>
            <th scope="col" style={{width: '4%'}}>Remove</th>
          </tr>
          </thead>
          <tbody>
          {usersSettings.settings.map((user) => {
            return (user.selections && user.selections.map((selection, index) => {
              const isEditing = editing === selection.cfi_range;
              return (<tr key={user.user_id + index}>
                <td>{user.username}</td>
                <td>{selection.text}</td>
                <td>
                  {isEditing ? (<div style={{width: '100%', height: '90%'}}>
                    <textarea
                      style={{width: '100%', height: '100%'}}
                      value={note}
                      onChange={(e) => setNote(e.target.value)}
                    />
                    <div style={{marginTop: '10px'}}>
                      <button onClick={() => submitNote(selection)}>Submit</button>
                      <button onClick={() => {
                        setNote('');
                        setEditing(null);
                      }}>Cancel
                      </button>
                    </div>
                  </div>) : (<span>
                    {selection.user_text || 'No note'}
                    {selection.user_id === userId && <button onClick={() => {
                      setNote(selection.user_text || '');  // Update note state with the userText or empty string if userText is not available
                      setEditing(selection.cfi_range);
                    }}>
                      Edit Note
                    </button>}
                  </span>)}
                </td>
                <td>
                  <button
                    onClick={() => {
                      renditionRef.current.display(selection.cfi_range)
                    }}
                  >
                    Show
                  </button>
                </td>
                <td>
                  {selection.user_id === userId && <button
                    onClick={() => {
                      renditionRef.current.annotations.remove(selection.cfi_range, 'highlight')

                      setUsersSettings(prevState => {
                        const newUserSettings = {...prevState};  // Copy the previous state
                        const userSetting = newUserSettings.settings.find(x => x.user_id === userId);
                        // Remove the selected note from user's settings
                        userSetting.selections = userSetting.selections.filter((item, j) => j !== index);

                        socket.current.send(JSON.stringify(userSetting));

                        return newUserSettings;
                      });

                      setSelections(user.selections.filter((item, j) => j !== index))
                    }}
                  >
                    x
                  </button>}
                </td>
              </tr>);
            }));
          })}
          </tbody>
        </table>
        <h3>Users:</h3>
        <table className="table">
          <thead>
          <tr>
            <th scope="col">Username</th>
            <th scope="col">Colour</th>
            <th scope="col">Progress</th>
            <th scope="col">Chapter</th>
            <th scope="col">Online</th>
          </tr>
          </thead>
          <tbody>
          {usersSettings.settings.map((user) => {
            let isOnline = false;
            if (onlineUsers) {
              isOnline = onlineUsers.connected_users.some(onlineUser => onlineUser.user_settings.user_id === user.user_id);
            }
            return (<tr key={user.user_id}>
              <td>{user.username}</td>
              <td>
                <div className="color-circle"
                     style={{backgroundColor: user.colour !== "" ? user.colour : '#007BFF'}}/>
              </td>
              <td>{user.progress}%</td>
              <td>{user.chapter}</td>
              <td>{isOnline ? 'Online' : 'Not Online'}</td>
            </tr>);
          })}
          </tbody>
        </table>
      </div>
      }

    </div>
  )
    ;
}

export default BookDetail;