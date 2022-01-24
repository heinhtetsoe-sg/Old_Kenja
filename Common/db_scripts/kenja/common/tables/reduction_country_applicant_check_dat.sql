-- $Id: abdb644d2ea4f268651a9f3f1cb8201ed9a1d058 $

drop table REDUCTION_COUNTRY_APPLICANT_CHECK_DAT
create table REDUCTION_COUNTRY_APPLICANT_CHECK_DAT( \
    SCHOOLCD                        varchar(12) not null, \
    SCHOOL_KIND                     varchar(2)  not null, \
    YEAR                            varchar(4)  not null, \
    MONTH                           varchar(2)  not null, \
    SCHREGNO                        varchar(8)  not null, \
    PASSNO                          varchar(25), \
    INTENTION_YES_FLG               varchar(1), \
    INTENTION_NO_FLG                varchar(1), \
    FORMS_YES_FLG                   varchar(1), \
    FORMS_NO_FLG                    varchar(1), \
    FATHER_TAX_CERTIFICATE_FLG      varchar(1), \
    FATHER_SPECIAL_TAX_DEC_FLG      varchar(1), \
    FATHER_TAX_NOTICE_FLG           varchar(1), \
    MOTHER_TAX_CERTIFICATE_FLG      varchar(1), \
    MOTHER_SPECIAL_TAX_DEC_FLG      varchar(1), \
    MOTHER_TAX_NOTICE_FLG           varchar(1), \
    MAINTAINER_TAX_CERTIFICATE_FLG  varchar(1), \
    MAINTAINER_SPECIAL_TAX_DEC_FLG  varchar(1), \
    MAINTAINER_TAX_NOTICE_FLG       varchar(1), \
    SCHOLARSHIP_PAYMENT_YES_NO_FLG  varchar(1), \
    REMARK                          varchar(45), \
    REGISTERCD                      varchar(10), \
    UPDATED                         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_COUNTRY_APPLICANT_CHECK_DAT add constraint PK_REDUCTION_CAC_D primary key(SCHOOLCD, SCHOOL_KIND, YEAR, MONTH, SCHREGNO)
