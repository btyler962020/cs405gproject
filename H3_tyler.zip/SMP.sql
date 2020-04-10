CREATE DATABASE SMPDB;
USE SMPDB;
CREATE TABLE Identity (idnum INTEGER PRIMARY KEY, handle VARCHAR(100) UNIQUE, password VARCHAR(100),fullname VARCHAR(100) NOT NULL, location VARCHAR(100), email VARCHAR(100) NOT NULL, bdate DATE NOT NULL, joined DATE NOT NULL);
CREATE TABLE Story (sidnum INTEGER, idnum INTEGER, chapter VARCHAR(100),url VARCHAR(100),expires DATETIME, tstamp TIMESTAMP, PRIMARY KEY(sidnum),FOREIGN KEY(idnum) REFERENCES Identity(idnum));
CREATE TABLE Follows (follower INTEGER,followed INTEGER,tstamp TIMESTAMP,FOREIGN KEY(follower) REFERENCES Identity(idnum), FOREIGN KEY(followed) REFERENCES Identity(idnum));
CREATE TABLE Reprint (rpnum INTEGER, idnum INTEGER, sidnum INTEGER, likeit BOOLEAN, tstamp TIMESTAMP,PRIMARY KEY (rpnum), FOREIGN KEY(idnum) REFERENCES Identity(idnum), FOREIGN KEY(sidnum) REFERENCES Story(sidnum));
CREATE TABLE Block (blknum INTEGER, idnum INTEGER, blocked INTEGER,tstamp TIMESTAMP, PRIMARY KEY (blknum), FOREIGN KEY (idnum) REFERENCES Identity(idnum), FOREIGN KEY (blocked) REFERENCES Identity(idnum));
GRANT ALL ON SMPDB.* TO 'bcty222'@'localhost' IDENTIFIED BY 'censored';
GRANT ALL ON SMPDB.* TO 'bcty222'@'%' IDENTIFIED BY 'censored';
GRANT ALL ON SMPDB.* TO 'paul'@'localhost' IDENTIFIED BY 'jellydonuts!';
GRANT ALL ON SMPDB.* TO 'paul'@'belgarath.cs.uky.edu' IDENTIFIED BY 'jellydonuts!';
GRANT ALL ON SMPDB.* TO 'paul'@'paul.cs.uky.edu' IDENTIFIED BY 'jellydonuts!';
FLUSH PRIVILEGES;