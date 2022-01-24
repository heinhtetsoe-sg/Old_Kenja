-- kanji=漢字
-- $Id: e3d74455987efbbff10d73f725b47f80c380e494 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--伝票データ
drop table REPORT_AFT_SINRO_ADDITION_L350W_DAT

create table REPORT_AFT_SINRO_ADDITION_L350W_DAT ( \
  EDBOARD_SCHOOLCD  VARCHAR(12) not null,  \
  YEAR              VARCHAR(4) not null,  \
  APPLICANTDIV      VARCHAR(1) not null, \
  TESTDIV           VARCHAR(1) not null, \
  RUIKEI_DIV        VARCHAR(2) not null, \
  TESTDIV2          VARCHAR(1) not null, \
  COURSECD          VARCHAR(1) not null,  \
  MAJORCD           VARCHAR(3) not null,  \
  COURSECODE        VARCHAR(4) not null, \
  EXECUTE_DATE      DATE,  \
  REGISTERCD        VARCHAR(10),  \
  UPDATED           TIMESTAMP default CURRENT TIMESTAMP  \
)

alter table REPORT_AFT_SINRO_ADDITION_L350W_DAT \
  add constraint PK_REPORT_AFTL350W primary key (EDBOARD_SCHOOLCD,YEAR,APPLICANTDIV,TESTDIV,RUIKEI_DIV,TESTDIV2,COURSECD,MAJORCD,COURSECODE)
