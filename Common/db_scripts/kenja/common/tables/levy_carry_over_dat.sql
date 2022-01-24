-- kanji=����
-- $Id: 1e671596cf4ab68cd92fab22dc7780e5d149a26c $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--ħ��������Ǥ��ǡ���

drop table LEVY_CARRY_OVER_DAT

create table LEVY_CARRY_OVER_DAT \
( \
        "SCHOOLCD"                  varchar(12) not null, \
        "SCHOOL_KIND"               varchar(2)  not null, \
        "YEAR"                      varchar(4)  not null, \
        "SCHREGNO"                  varchar(8)  not null, \
        "INCOME_L_CD"               varchar(2)  not null, \
        "INCOME_M_CD"               varchar(2)  not null, \
        "ATTACHED_YEAR"             varchar(4), \
        "LEVY_L_NAME"               varchar(90), \
        "LEVY_L_ABBV"               varchar(90), \
        "LEVY_M_NAME"               varchar(90), \
        "LEVY_M_ABBV"               varchar(90), \
        "CARRY_OVER_MONEY"          int, \
        "DIFFERENCE_MONEY"          int, \
        "TO_INCOME_FLG"             varchar(1), \
        "TO_SCHOOL_KIND"            varchar(2), \
        "TO_INCOME_L_CD"            varchar(2), \
        "TO_INCOME_M_CD"            varchar(2), \
        "CARRY_CANCEL"              varchar(1), \
        "REGISTERCD"                varchar(10), \
        "UPDATED"                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_CARRY_OVER_DAT add constraint PK_LEVY_CAR_OVE_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO, INCOME_L_CD, INCOME_M_CD)
