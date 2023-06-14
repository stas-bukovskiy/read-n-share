import React, {useState} from 'react';
import {Navigate} from 'react-router-dom';
import './UploadBook.css';
import {FileUploader} from "react-drag-drop-files";

const fileTypes = ["MOBI", "EPUB", "FB2", "PDF", "DOCX", "TXT"];

function UploadBook() {
  const token = localStorage.getItem('token');

  const [message, setMessage] = useState(null);
  const [messageStatus, setMessageStatus] = useState(null);
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [description, setDescription] = useState('');

  const getMessageColor = () => {
    switch (messageStatus) {
      case 'info':
        return 'blue';
      case 'success':
        return 'green';
      case 'error':
        return 'red';
      default:
        return 'black';
    }
  }

  const [file, setFile] = useState(null);
  const handleChange = (file) => {
    setFile(file);
  };

  const handleUpload = async () => {
    try {
      if (!file) {
        throw new Error('Please select a file');
      }


      const formData = new FormData();
      formData.append('file', file);
      formData.append('title', title);
      formData.append('author', author);
      formData.append('description', description);

      setMessage('Uploading...');
      setMessageStatus('info');

      const response = await fetch('http://3.85.229.215:8002/api/v1/upload/book', {
          method: 'POST',
          body: formData,
          headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
      });

      console.log(response)

      if (!response.ok) {
        throw new Error('Upload failed');
      }

      setMessage('Upload successful');
      setMessageStatus('success');
    } catch (error) {
      setMessage(`Upload error: ${error.message}`);
      setMessageStatus('error');
    }
  };

  if (!token) {
    return <Navigate to={'/login'}/>
  }

  return (
    <div>
      <form>
        <label>
          Title:
          <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} />
        </label>
        <label>
          Author:
          <input type="text" value={author} onChange={(e) => setAuthor(e.target.value)} />
        </label>
        <label>
          Description:
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>
      </form>
      <FileUploader
        classes="upload-container"
        handleChange={handleChange}
        name="file"
        types={fileTypes}
        children={<div
          className={"upload-box"}>{file ? file.name : "Drag and drop a file here or click to select a file"}</div>}
      />
      <button className="upload-button" onClick={handleUpload}>Upload</button>
      {message && <p style={{color: getMessageColor()}}>{message}</p>}
    </div>
  );
}

export default UploadBook;