-- kanji=漢字
-- $Id: 65357135e5bf6751d535a02b93cc21d0652d8d88 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--伝票データ
drop table AFT_SINRO_ADDITION_L350W_DAT

create table AFT_SINRO_ADDITION_L350W_DAT ( \
  EDBOARD_SCHOOLCD  VARCHAR(12) not null, \
  YEAR              VARCHAR(4) not null, \
  APPLICANTDIV      VARCHAR(1) not null, \
  TESTDIV           VARCHAR(1) not null, \
  RUIKEI_DIV        VARCHAR(2) not null, \
  TESTDIV2          VARCHAR(1) not null, \
  COURSECD          VARCHAR(1) not null, \
  MAJORCD           VARCHAR(3) not null, \
  COURSECODE        VARCHAR(4) not null, \
  LARGE_DIV         VARCHAR(2) not null, \
  SEX               VARCHAR(1) not null, \
  COUNT             SMALLINT, \
  REGISTERCD        VARCHAR(10), \
  UPDATED           TIMESTAMP default CURRENT TIMESTAMP \
)

alter table AFT_SINRO_ADDITION_L350W_DAT \
  add constraint PK_AFT_SINRO_L350W primary key (EDBOARD_SCHOOLCD,YEAR,APPLICANTDIV,TESTDIV,RUIKEI_DIV,TESTDIV2,COURSECD,MAJORCD,COURSECODE,LARGE_DIV,SEX)
