import './FloatingButton.css';
import {FaCheck, FaCopy} from "react-icons/fa";
import {useState} from "react";


const FloatingCopyButton = ({dataToCopy}) => {
    const [isCopied, setIsCopied] = useState(false);

    const handleCopyToClipboard = () => {
        navigator.clipboard.writeText(dataToCopy).then(r => {
        }); // Use the Clipboard API to write the data to the clipboard
        setIsCopied(true); // Set the copied state to true
        setTimeout(() => {
            setIsCopied(false); // Reset the copied state after a delay
        }, 1500);
    };
    return (
        <button className="fab circle-button" onClick={handleCopyToClipboard}>
            {isCopied ? <FaCheck className="plus"/> : <FaCopy className="plus"/>}
        </button>
    );
};

export default FloatingCopyButton;