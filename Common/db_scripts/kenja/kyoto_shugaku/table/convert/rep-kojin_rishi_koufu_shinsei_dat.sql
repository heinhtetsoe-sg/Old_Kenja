-- $Id: rep-kojin_rishi_koufu_shinsei_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE KOJIN_RISHI_KOUFU_SHINSEI_DAT_OLD
RENAME TABLE KOJIN_RISHI_KOUFU_SHINSEI_DAT TO KOJIN_RISHI_KOUFU_SHINSEI_DAT_OLD
CREATE TABLE KOJIN_RISHI_KOUFU_SHINSEI_DAT( \
    SHUUGAKU_NO             VARCHAR(7)    NOT NULL, \
    SHINSEI_YEAR            VARCHAR(4)    NOT NULL, \
    KOUFU_SEQ               VARCHAR(2)    NOT NULL, \
    SHIKIN_SHUBETSU         VARCHAR(1)    NOT NULL, \
    KOJIN_NO                VARCHAR(7)    NOT NULL, \
    SHUTARU_CD              VARCHAR(7), \
    S_SHINSEI_YEAR          VARCHAR(4)    NOT NULL, \
    UKE_YEAR                VARCHAR(4), \
    UKE_NO                  VARCHAR(4), \
    UKE_EDABAN              VARCHAR(3), \
    SHINSEI_DATE            DATE, \
    YUUSHI_COURSE_DIV       VARCHAR(1), \
    KARIIRE_BANKCD          VARCHAR(4), \
    KARIIRE_GK              INT, \
    KARIIRE_RITSU           DECIMAL(6, 3), \
    KARIIRE_DATE            DATE, \
    S_RISHISHIHARAI_DATE    DATE, \
    E_RISHISHIHARAI_DATE    DATE, \
    REMARK                  VARCHAR(2400), \
    S_KOUFU_SHINSEI_GK      INT, \
    KOUFU_SHINSEI_GK        INT, \
    KOUFU_SHORI_GK          INT, \
    KOUFU_KETTEI_DATE       DATE, \
    KOUFU_STATUS_FLG        VARCHAR(1), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO KOJIN_RISHI_KOUFU_SHINSEI_DAT \
    SELECT \
        SHUUGAKU_NO, \         
        SHINSEI_YEAR, \        
        KOUFU_SEQ, \           
        SHIKIN_SHUBETSU, \     
        KOJIN_NO, \            
        SHUTARU_CD, \
        S_SHINSEI_YEAR, \      
        UKE_YEAR, \            
        UKE_NO, \              
        UKE_EDABAN, \          
        SHINSEI_DATE, \        
        YUUSHI_COURSE_DIV, \   
        KARIIRE_BANKCD, \      
        KARIIRE_GK, \          
        KARIIRE_RITSU, \       
        KARIIRE_DATE, \        
        S_RISHISHIHARAI_DATE, \
        E_RISHISHIHARAI_DATE, \
        REMARK, \
        KOUFU_SHINSEI_GK, \    
        KOUFU_SHINSEI_GK, \    
        KOUFU_SHORI_GK, \      
        KOUFU_KETTEI_DATE, \   
        KOUFU_STATUS_FLG, \    
        REGISTERCD, \
        UPDATED \
    FROM \
        KOJIN_RISHI_KOUFU_SHINSEI_DAT_OLD

ALTER TABLE KOJIN_RISHI_KOUFU_SHINSEI_DAT ADD CONSTRAINT PK_K_R_KOUFU_S_DAT PRIMARY KEY (SHUUGAKU_NO, SHINSEI_YEAR, KOUFU_SEQ)