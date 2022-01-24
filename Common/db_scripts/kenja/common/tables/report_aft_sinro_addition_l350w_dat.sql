-- kanji=漢字
-- $Id: 7f5349173119feb8557a85776e22a519d596f778 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--伝票データ
drop table REPORT_AFT_SINRO_ADDITION_L350W_DAT

create table REPORT_AFT_SINRO_ADDITION_L350W_DAT ( \
  EDBOARD_SCHOOLCD  varchar(12) not null,  \
  YEAR              varchar(4) not null,  \
  APPLICANTDIV      varchar(1) not null, \
  TESTDIV           varchar(1) not null, \
  RUIKEI_DIV        varchar(2) not null, \
  TESTDIV2          varchar(1) not null, \
  COURSECD          varchar(1) not null,  \
  MAJORCD           varchar(3) not null,  \
  COURSECODE        varchar(4) not null, \
  EXECUTE_DATE      timestamp,  \
  FIXED_FLG         varchar(1),  \
  REGISTERCD        varchar(10),  \
  UPDATED           timestamp default current timestamp  \
)

alter table REPORT_AFT_SINRO_ADDITION_L350W_DAT \
  add constraint PK_REPORT_AFTL350W primary key (EDBOARD_SCHOOLCD,YEAR,APPLICANTDIV,TESTDIV,RUIKEI_DIV,TESTDIV2,COURSECD,MAJORCD,COURSECODE)
