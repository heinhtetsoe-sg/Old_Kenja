-- $Id: kojin_hist_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_HIST_DAT
CREATE TABLE KOJIN_HIST_DAT( \
    KOJIN_NO          		 VARCHAR(7)     NOT NULL, \
    ISSUEDATE         		 DATE    		NOT NULL, \
    FAMILY_NAME              VARCHAR(60)    NOT NULL, \
    FIRST_NAME               VARCHAR(60)    NOT NULL, \
    FAMILY_NAME_KANA         VARCHAR(120)   NOT NULL, \
    FIRST_NAME_KANA          VARCHAR(120)   NOT NULL, \
    BIRTHDAY               	 DATE, \
    KIKON_FLG                VARCHAR(1), \
    J_SCHOOL_CD              VARCHAR(7), \
    J_GRAD_DIV               VARCHAR(1), \
    J_GRAD_YM				 VARCHAR(7), \
    ZIPCD             	 	 VARCHAR(8), \
    CITYCD              	 VARCHAR(5), \
    ADDR1             	 	 VARCHAR(150), \
    ADDR2             	 	 VARCHAR(150), \
    TELNO1    	 			 VARCHAR(14), \
    TELNO2      	 		 VARCHAR(14), \
    TSUUGAKU_DIV    	 	 VARCHAR(1), \
    OLD_SHINKEN_NAME1    	 VARCHAR(60), \
    OLD_SHINKEN_NAME2    	 VARCHAR(60), \
    REMARK					 VARCHAR(2400), \
    REGISTERCD           	 VARCHAR(8), \
    UPDATED              	 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KOJIN_HIST_DAT ADD CONSTRAINT PK_K_HIST_DAT PRIMARY KEY (KOJIN_NO, ISSUEDATE)