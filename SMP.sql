CREATE DATABASE SMPDB;

USE SMPDB;

CREATE TABLE Identity (
	idnum INT PRIMARY KEY,
	handle VARCHAR(100),
	UNIQUE(handle),
	password VARCHAR(100),
	fullname VARCHAR(100) NOT NULL,
	location VARCHAR(100),
	email VARCHAR(100) NOT NULL,
	bdate DATE NOT NULL,
	joined DATE NOT NULL
);

CREATE TABLE Story (
	sidnum INT PRIMARY KEY,
	idnum INT,
	FOREIGN KEY (idnum) REFERENCES Identity(idnum),
	chapter VARCHAR(100),
	url VARCHAR(100),
	expires datetime,
	tstamp TIMESTAMP
);

CREATE TABLE Follows (
	follower INT,
	FOREIGN KEY (follower) REFERENCES Identity(idnum),
	followed INT,
	FOREIGN KEY (followed) REFERENCES Identity(idnum),
	tstamp TIMESTAMP
);

CREATE TABLE Reprint (
	rpnum INT PRIMARY KEY,
	idnum INT,
	FOREIGN KEY (idnum) REFERENCES Identity(idnum),
	sidnum INT,
	FOREIGN KEY (sidnum) REFERENCES Story(sidnum),
	likeit boolean,
	tstamp TIMESTAMP
);

CREATE TABLE Block (
	blknum INT PRIMARY KEY,
	idnum INT,
	FOREIGN KEY (idnum) REFERENCES Identity(idnum),
	blocked INT,
	FOREIGN KEY (blocked) REFERENCES Identity(idnum),
	tstamp TIMESTAMP
);

GRANT ALL ON SMPDB.* TO 'pbbu223'@'localhost' IDENTIFIED BY 'dejavu11';
GRANT ALL ON SMPDB.* TO 'pbbu223'@'%' IDENTIFIED BY 'dejavu11';            
GRANT ALL ON SMPDB.* TO 'paul'@'localhost' IDENTIFIED BY 'jellydonuts!';             
GRANT ALL ON SMPDB.* TO 'paul'@'belgarath.cs.uky.edu' IDENTIFIED BY 'jellydonuts!';
GRANT ALL ON SMPDB.* TO 'paul'@'paul.cs.uky.edu' IDENTIFIED BY 'jellydonuts!';       
FLUSH PRIVILEGES;
