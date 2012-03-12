use tourenplaner;
INSERT INTO Users (Email, Passwordhash, Salt, AdminFlag, Status, 
  FirstName, LastName, Address, RegistrationDate, VerifiedDate) 
  VALUES('userinactivenotadmin@teufel.de', 'd1fdc373fd6ec38fb4ed38c815c5b7da42a21c6c', '7699f567b663737689fc39d5e05f7aa0a22a086c',
    '0', 'needs_verification', 'InactiveNotAdmin', 'Teufel', '', NULL, CURRENT_TIMESTAMP),
('userinactiveadmin@teufel.de', 'de020883205f198280efcda0426337080a0b3086', '19277acf80bf6a0a1fb67ef0023096775136cc3d',
    '1', 'needs_verification', 'InactiveAdmin', 'Teufel', '', NULL, CURRENT_TIMESTAMP),
('useractivenotadmin@teufel.de', '301611c442931677cc82d6d4187cfcb792153b27', '02997d8ed783ff11b91e354d599a0c00d4c4cce8',
    '0', 'verified', 'ActiveNotAdmin', 'Teufel', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('useractiveadmin@teufel.de', 'c278db902f9bb36ab1a665170fdc9b3383f64032', '07f19d6f77a769a56b7ff47ffb5f093b632da62b',
    '1', 'verified', 'ActiveAdmin', 'Teufel', '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
