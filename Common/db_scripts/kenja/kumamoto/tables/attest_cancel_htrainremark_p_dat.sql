-- kanji=漢字
-- $Id: attest_cancel_htrainremark_p_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_HTRAINREMARK_P_DAT

CREATE TABLE ATTEST_CANCEL_HTRAINREMARK_P_DAT \
(   CANCEL_YEAR      VARCHAR(4) NOT NULL,  \
    CANCEL_SEQ       SMALLINT   NOT NULL,  \
    CANCEL_STAFFCD   VARCHAR(8) NOT NULL,  \
    YEAR             VARCHAR(4) NOT NULL,  \
    SCHREGNO         VARCHAR(8) NOT NULL,  \
    ANNUAL           VARCHAR(2) NOT NULL,  \
    TOTALSTUDYACT    VARCHAR(678),  \
    TOTALSTUDYVAL    VARCHAR(678),  \
    SPECIALACTREMARK VARCHAR(678),  \
    TOTALREMARK      VARCHAR(1598),  \
    ATTENDREC_REMARK VARCHAR(242),  \
    VIEWREMARK       VARCHAR(226),  \
    BEHAVEREC_REMARK VARCHAR(122),  \
    CLASSACT         VARCHAR(300),  \
    STUDENTACT       VARCHAR(218),  \
    CLUBACT          VARCHAR(225),  \
    SCHOOLEVENT      VARCHAR(218),  \
    FOREIGNLANGACT1  VARCHAR(150),  \
    FOREIGNLANGACT2  VARCHAR(150),  \
    FOREIGNLANGACT3  VARCHAR(150),  \
    FOREIGNLANGACT4  VARCHAR(150),  \
    REGISTERCD       VARCHAR(8),  \
    UPDATED          TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS
 
ALTER TABLE ATTEST_CANCEL_HTRAINREMARK_P_DAT \
ADD CONSTRAINT PK_ATC_HTRAINP \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ, YEAR, SCHREGNO)
