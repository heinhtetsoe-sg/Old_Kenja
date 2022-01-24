-- kanji=漢字
-- $Id: 5ce008e5dcaa1d54cad06f4ab0ec55a0982dc0a0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--伝票データ
drop table AFT_SINRO_ADDITION_L350W_FIELD_DAT

create table AFT_SINRO_ADDITION_L350W_FIELD_DAT ( \
  YEAR              VARCHAR(4) not null, \
  APPLICANTDIV      VARCHAR(1) not null, \
  TESTDIV           VARCHAR(1) not null, \
  RUIKEI_DIV        VARCHAR(2) not null, \
  TESTDIV2          VARCHAR(1) not null, \
  LARGE_DIV         VARCHAR(2) not null, \
  LARGE_NAME        VARCHAR(90) not null, \
  REGISTERCD        VARCHAR(10), \
  UPDATED           TIMESTAMP default CURRENT TIMESTAMP \
)

alter table AFT_SINRO_ADDITION_L350W_FIELD_DAT \
  add constraint PK_AFT_SINROL350WF primary key (YEAR,APPLICANTDIV,TESTDIV,RUIKEI_DIV,TESTDIV2,LARGE_DIV)
