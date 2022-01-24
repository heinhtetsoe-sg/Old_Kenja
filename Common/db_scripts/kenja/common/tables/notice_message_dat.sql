-- kanji=漢字
-- $Id: 387843dfddb572e9ef47010ffdf2c231ca8ab00f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table NOTICE_MESSAGE_DAT
create table NOTICE_MESSAGE_DAT \
(  \
      REGIST_DATE       DATE        not null , \
      SEQ               INTEGER     not null , \
      STAFFCD           VARCHAR(10)          , \
      FROM_DATE         DATE                 , \
      FROM_PERIODCD     VARCHAR(1)           , \
      FROM_CHAIRCD      VARCHAR(7)           , \
      TO_DATE           DATE                 , \
      TO_PERIODCD       VARCHAR(1)           , \
      TO_CHAIRCD        VARCHAR(7)           , \
      NOTICE_MESSAGE    VARCHAR(600)         , \
      CANCEL_FLG        VARCHAR(1)           , \
      REGISTERCD        VARCHAR(10)          , \
      UPDATED TIMESTAMP default CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table NOTICE_MESSAGE_DAT add constraint PK_NOTICE_MESSAGE_DAT \
primary key (REGIST_DATE, SEQ)
