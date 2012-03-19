use tourenplaner;
DELETE Requests FROM Requests LEFT JOIN Users ON Requests.UserID = Users.id 
WHERE Users.Email = 'userinactivenotadmin@teufel.de' 
OR Users.Email='userinactiveadmin@teufel.de'
OR Users.Email='useractivenotadmin@teufel.de'
OR Users.Email='updatedActiveAdmin@teufel.de' 
OR Users.Email='registeredByNotInDBNoAuth@teufel.de'
OR Users.Email='updatedByActiveAdmin@teufel.de';
DELETE FROM Users 
WHERE email='userinactivenotadmin@teufel.de' 
OR email='userinactiveadmin@teufel.de' 
OR email='useractivenotadmin@teufel.de' 
OR email='updatedActiveAdmin@teufel.de'
OR email='registeredByNotInDBNoAuth@teufel.de'
OR email='updatedByActiveAdmin@teufel.de';
