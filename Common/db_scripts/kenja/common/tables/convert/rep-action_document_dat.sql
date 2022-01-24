-- kanji=漢字
-- $Id: 227293de4a514d4edb893e2a231d77d77e3fa8d7 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE ACTION_DOCUMENT_DAT_OLD
RENAME ACTION_DOCUMENT_DAT TO ACTION_DOCUMENT_DAT_OLD
CREATE TABLE ACTION_DOCUMENT_DAT ( \
        SCHREGNO              varchar(8) not null, \
        ACTIONDATE            date       not null, \
        SEQ                   smallint   not null, \
        ACTIONTIME            time , \
        STAFFCD               varchar(10), \
        DIVIDECD              varchar(2) , \
        TITLE                 varchar(120) , \
        TEXT                  varchar(700) , \
        PRIVATE               varchar(1) , \
        REGISTERCD            varchar(10) , \
        UPDATED               timestamp default current timestamp  \
) in usr1dms index in idx1dms

ALTER TABLE ACTION_DOCUMENT_DAT ADD CONSTRAINT PK_ACTION_DOC_DAT PRIMARY KEY (SCHREGNO, ACTIONDATE, SEQ)

INSERT INTO ACTION_DOCUMENT_DAT \
    SELECT \
        SCHREGNO , \
        ACTIONDATE, \
        SEQ, \
        ACTIONTIME, \
        STAFFCD, \
        DIVIDECD, \
        TITLE, \
        TEXT, \
        CAST(NULL AS VARCHAR(1)) AS PRIVATE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ACTION_DOCUMENT_DAT_OLD
