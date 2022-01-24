-- kanji=����
-- $Id: db27fa8510624a180d38c8df64710f40af77a7ea $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--ǯ���������¹Գ�ǧ�ơ��֥�

drop table LEVY_CLOSE_GRADE_DAT

create table LEVY_CLOSE_GRADE_DAT( \
        SCHOOLCD        varchar(12) not null, \
        SCHOOL_KIND     varchar(2)  not null, \
        YEAR            varchar(4)  not null, \
        GRADE           varchar(2)  not null, \
        CLOSE_FLG       varchar(1)  , \
        REGISTERCD      varchar(10) , \
        UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_CLOSE_GRADE_DAT \
add constraint PK_LEVY_CLOSE_G_D \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, GRADE)
