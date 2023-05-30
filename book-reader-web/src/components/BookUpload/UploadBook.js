import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import './UploadBook.css';
import {FileUploader} from "react-drag-drop-files";

const fileTypes = ["MOBI", "EPUB", "FB2", "PDF", "DOCX", "TXT"];
const token = localStorage.getItem('token');

function UploadBook() {

  const navigation = useNavigate();
  if (!token) {
    navigation('/login');
  }

  const [message, setMessage] = useState(null);
  const [messageStatus, setMessageStatus] = useState(null);

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

      setMessage('Uploading...');
      setMessageStatus('info');

      const response = await fetch('http://localhost:8002/api/v1/upload/book', {
        method: 'POST',
        body: formData,
        headers: {
          'Authorization': `Bearer ${token}`
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

  return (
    <div>
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