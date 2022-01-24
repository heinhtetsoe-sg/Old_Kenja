
------------------------------------------------
-- 表の DDL ステートメント "DB2INST1"."BENEFIT_DAT"
------------------------------------------------
DROP TABLE BENEFIT_DAT

CREATE TABLE BENEFIT_DAT \
( \
 		   PABLISH_NO		VARCHAR(5) NOT NULL , \
 		   PABLISH_DATE		DATE , \
 		   YEAR		        VARCHAR(4) , \
		   SCHREGNO  		VARCHAR(6) ,  \
		   RECEIPT_CD  		VARCHAR(1) check(RECEIPT_CD in ('0','1')),  \
 		   RECEIPT_DATE		DATE , \
 		   RECEIPT_KIN		INTEGER , \
 		   DESASDATE		DATE , \
 		   DESASTIME		TIME , \
		   UPDATED  		TIMESTAMP WITH DEFAULT CURRENT TIMESTAMP)    \
		 IN  USR1DMS  INDEX IN  IDX1DMS

-- 表の 1 次キーの DDL ステートメント  DB2INST1 . BENEFIT_DAT 

ALTER TABLE  DB2INST1 . BENEFIT_DAT \
	ADD CONSTRAINT  PK_BENEFIT_DAT  PRIMARY KEY \
		( PABLISH_NO )
