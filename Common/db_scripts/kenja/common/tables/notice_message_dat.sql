-- kanji=����
-- $Id: 387843dfddb572e9ef47010ffdf2c231ca8ab00f $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
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
