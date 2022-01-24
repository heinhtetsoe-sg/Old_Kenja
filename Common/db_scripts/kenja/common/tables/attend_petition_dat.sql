-- kanji=����
-- $Id: efb1201e6258e0a342c441e608333ca39ecaedf2 $
-- �з��Ϥ��ǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ATTEND_PETITION_DAT

create table ATTEND_PETITION_DAT ( \
    YEAR            varchar(4) not null, \
    SEQNO           integer not null, \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    PERIODCD        varchar(1) not null, \
    DI_CD           varchar(2), \
    DI_REMARK_CD    varchar(3), \
    DI_REMARK       varchar(30), \
    INPUT_FLG       varchar(1), \
    EXECUTED        varchar(1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_PETITION_DAT add constraint PK_ATTEND_P_DAT \
        primary key (YEAR, SEQNO, SCHREGNO, ATTENDDATE, PERIODCD)
