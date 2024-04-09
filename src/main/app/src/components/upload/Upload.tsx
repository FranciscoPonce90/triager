// Import React and hook utilities from 'react' package
import React, { useRef, useState } from 'react';
// Import the CSS for styling the upload component
import './Upload.css';
// Import the Analysis interface definition
import { Analysis } from "../../interfaces/Analysis";
// Import icons from Font Awesome for UI elements
import { faFolderOpen } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

// Define the properties expected by the Upload component
interface UploadProps {
    onClose: () => void; // Function to call when closing the upload modal
    onNewAnalysis: (analysis: Analysis) => void; // Function to handle a new analysis object
}

// The Upload component allows the user to upload files for analysis
const Upload: React.FC<UploadProps> = ({ onClose, onNewAnalysis }) => {
    // State for managing the selected file
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    // State to track if a file is selected
    const [isFilePicked, setIsFilePicked] = useState(false);
    // State for the name of the analysis
    const [analysisName, setAnalysisName] = useState('');

    // Handles changes to the file input field
    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const files = event.target.files;
        if (files) {
            handleFileValidation(files[0]);
        }
    };

    // Handles file drag over the drop area
    const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault(); // Prevent default behavior of opening the file
    };

    // Validates the file extension and selects the file if valid
    const handleFileValidation = (file: File) => {
        // Check the file extension
        const validExtensions = ['.txt'];
        const fileExtension = file.name.split('.').pop();

        if (fileExtension && validExtensions.includes('.' + fileExtension.toLowerCase())) {
            setSelectedFile(file);
            setIsFilePicked(true);
            setAnalysisName(file.name.replace(/\.[^/.]+$/, "")); // Remove file extension from name
        } else {
            // If the file is not a .txt, display an error message
            alert("Only .txt files are allowed!");
            setIsFilePicked(false);
        }
    };

    // Handles file drop on the drop area
    const handleDrop = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault(); // Prevent default file opening behavior
        const files = event.dataTransfer.files;
        if (files && files.length > 0) {
            handleFileValidation(files[0]); // Validate the first file
        }
    };

    // Handles file upload submission
    const handleSubmission = () => {
        if (selectedFile) {
            const reader = new FileReader();
            reader.onload = async (e) => {
                const text = e.target?.result;

                try {
                    const response = await fetch('http://localhost:8080/analysis', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({ content: text, name: analysisName }),
                    });

                    if (response.ok) {
                        console.log('File sent successfully');
                        const analysis = await response.json();
                        onNewAnalysis(analysis); // Pass the analysis back to the parent component
                        onClose(); // Close the upload modal after submission
                    } else {
                        // Handle different errors based on the response
                        const errorData = await response.json();
                        switch (errorData.id) {
                            case -1:
                                alert("The file is empty!");
                                break;
                            case -2:
                                alert("The file content is not valid!");
                                break;
                            default:
                                alert("An unknown error occurred.");
                                break;
                        }
                    }
                } catch (error) {
                    console.error('Network or server error:', error);
                }
            };
            reader.readAsText(selectedFile); // Read the file content as text
        }
    };

    // Ref for the file input, used to trigger it programmatically
    const fileInputRef = useRef<HTMLInputElement>(null);

    // Function to programmatically click the hidden file input when the upload button is clicked
    const handleFileButtonClick = () => {
        fileInputRef.current?.click();
    };

    // Render the upload modal with a drag-and-drop area and a confirm button
    return (
        <div className="modal-overlay active">
            <div className="upload-modal active" onClick={(e) => e.stopPropagation()}>
                <div className="upload-header">
                    <button className="close-button" onClick={onClose}>x</button>
                </div>

                <button onClick={handleFileButtonClick} className="upload-button">
                    <FontAwesomeIcon icon={faFolderOpen} /> Upload from files
                </button>

                <div className="upload-content"
                     onDrop={handleDrop}
                     onDragOver={handleDragOver}>
                    Drag and drop your file here or use the button above to select a file.
                    <input
                        ref={fileInputRef}
                        type="file"
                        onChange={handleFileChange}
                        accept=".txt"
                        id="file-input"
                        style={{ display: 'none' }}
                    />
                    {selectedFile && <div className="file-info">Selected file: {selectedFile.name}</div>}
                </div>
                <input
                    type="text"
                    value={analysisName}
                    onChange={(e) => setAnalysisName(e.target.value)}
                    placeholder="Insert analysis name"
                    disabled={!isFilePicked}
                    className="analysis-name-input"
                />
                <button
                    onClick={handleSubmission}
                    disabled={!isFilePicked}
                    className={`confirm-button ${!isFilePicked ? 'disabled' : ''}`}
                >
                    Confirm
                </button>

            </div>
        </div>
    );
};

export default Upload;