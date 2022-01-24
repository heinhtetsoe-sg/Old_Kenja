drop table HEXAM_ENTREMARK_REMARK_SEQ_HDAT

create table HEXAM_ENTREMARK_REMARK_SEQ_HDAT( \
    SCHREGNO                varchar(8) not null, \
    PATTERN_SEQ             varchar(1) not null, \
    REMARK                  varchar(7000), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HEXAM_ENTREMARK_REMARK_SEQ_HDAT add constraint PK_HEX_ENTREMARK_REMARK_SEQH primary key(SCHREGNO, PATTERN_SEQ)
