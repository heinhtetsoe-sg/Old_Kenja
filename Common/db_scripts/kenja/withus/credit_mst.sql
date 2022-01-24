-- kanji=����
-- $Id: credit_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table CREDIT_MST

create table CREDIT_MST \
	(YEAR                 varchar(4) not null, \
	 COURSECD             varchar(1) not null, \
	 MAJORCD              varchar(3) not null, \
	 GRADE                varchar(2) not null, \
	 COURSECODE           varchar(4) not null, \
	 CLASSCD              varchar(2) not null, \
	 CURRICULUM_CD        varchar(1) not null, \
	 SUBCLASSCD           varchar(6) not null, \
	 CREDITS              smallint, \
	 ABSENCE_HIGH         smallint, \
	 ABSENCE_WARN         smallint, \
	 REQUIRE_FLG          varchar(1), \
	 AUTHORIZE_FLG        varchar(1), \
	 COMP_UNCONDITION_FLG varchar(1), \
	 REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CREDIT_MST add constraint pk_credit_mst primary key \
      (YEAR, COURSECD, MAJORCD, GRADE, COURSECODE, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
