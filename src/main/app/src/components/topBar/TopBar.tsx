import React from 'react';
import './TopBar.css';
import logoImage from '../../resources/Logo_SST.webp';
import {useNavigate} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faHome} from "@fortawesome/free-solid-svg-icons";
import Tooltip from "@mui/material/Tooltip";


interface TopBarProps {
    onHomeClick?: () => void;  // Make this prop optional
}

const TopBar: React.FC<TopBarProps> = ({ onHomeClick }) => {
    const navigate = useNavigate();

    const handleHomeLogoClick = () => {
        if (onHomeClick) {
            onHomeClick();  // Call the sync function if provided
        }
        navigate(`/`);
    };
    return (
        <div className="top-bar_container">
            <div className="top-bar_logo">
                <img src={logoImage} alt="Logo" />
            </div>
            <div className="top-bar_title">
                <h1>SECURITY SMELL TRIAGE</h1>
            </div>
            <Tooltip title="Home" arrow>
                <FontAwesomeIcon icon={faHome} size="2x" className="home-button" onClick={handleHomeLogoClick} />
            </Tooltip>
        </div>
    );
};

export default TopBar;
