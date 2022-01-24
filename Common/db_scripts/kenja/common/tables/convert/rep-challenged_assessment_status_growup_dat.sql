-- $Id: fc46c4cc0c36d44af4d045bd736c35268cad631e $

drop table CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT_OLD
create table CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT_OLD like CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT
insert into CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT_OLD select * from CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT

drop table CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT
create table CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT( \
    YEAR                    varchar(4)    not null, \
    DATA_DIV                varchar(2)    not null, \
    SHEET_PATTERN           varchar(1), \
    DATA_DIV_NAME           varchar(150), \
    STATUS_NAME             varchar(90), \
    GROWUP_NAME             varchar(90), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \ 
) in usr1dms index in idx1dms

alter table CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT add constraint PK_CHA_AS_SG_D primary key (YEAR, DATA_DIV)

INSERT INTO CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT \
    SELECT \
        YEAR           , \
        DATA_DIV       , \
        case when DATA_DIV = '0' \
             then case when GROWUP_NAME is not null \
                       then '2' \
                       else '1' \
                  end \
             else null \
        end as SHEET_PATTERN, \
        DATA_DIV_NAME  , \
        STATUS_NAME    , \
        GROWUP_NAME    , \
        REGISTERCD     , \
        UPDATED          \ 
    FROM \
        CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT_OLD

