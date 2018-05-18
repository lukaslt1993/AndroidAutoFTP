# AndroidAutoFTP
Automated Android FTP downloader prototype

Server list format:
<ServerAddress>,<userID>,<password>
<ServerAddress>,<userID>,<password>
...

If a server is public, userID and password needn't to be specified and the server will look like this in the list:
<ServerAdress>,,

File list format:
<FilenameOrPartOfIt>
<FilenameOrPartOfIt>
...

All Found files will be downloaded to "Download" folder. Note - not "Downloads".
