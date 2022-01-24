-- kanji=����
-- $Id: 5a2be9a0e57bd5a3c06ee65b9f8537a29332d5c9 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table CHAIR_GROUP_MST

create table CHAIR_GROUP_MST( \
    YEAR              VARCHAR(4) NOT NULL, \
    SEMESTER          VARCHAR(1) NOT NULL, \
    CHAIR_GROUP_CD    VARCHAR(6) NOT NULL, \
    CLASSCD           VARCHAR(2) NOT NULL, \
    SCHOOL_KIND       VARCHAR(2) NOT NULL, \
    CURRICULUM_CD     VARCHAR(2) NOT NULL, \
    SUBCLASSCD        VARCHAR(6) NOT NULL, \
    CHAIR_GROUP_NAME  VARCHAR(60), \
    CHAIR_GROUP_ABBV  VARCHAR(60), \
    REGISTERCD        VARCHAR(10), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table CHAIR_GROUP_MST  \
add constraint PK_CHAIR_GROUP_MST \
primary key  \
(YEAR, SEMESTER, CHAIR_GROUP_CD)
