-- kanji=漢字
-- $Id: 7e11cdc29985108eaa33a57f970bb3d7d4bbc686 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ATTEST_CANCEL_HR_ATTEND_DAT

CREATE TABLE ATTEST_CANCEL_HR_ATTEND_DAT  \
(   CANCEL_YEAR         varchar(4) not null , \
    CANCEL_SEQ          smallint   not null , \
    CANCEL_STAFFCD      varchar(8) not null , \
    COUNT_EXECUTEDATE   varchar(4) not null , \
    REGISTERCD          varchar(8)  , \
    UPDATED             timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS
 
ALTER TABLE ATTEST_CANCEL_HR_ATTEND_DAT \
ADD CONSTRAINT PK_ATC_SCHREGSTUDY \
PRIMARY KEY   \
(CANCEL_YEAR, CANCEL_SEQ )
