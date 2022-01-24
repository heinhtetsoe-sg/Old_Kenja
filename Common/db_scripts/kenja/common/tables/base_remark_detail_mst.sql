-- $Id: ec82aed7c12d1235eed03ae92d2d406c08c73a8f $

drop table BASE_REMARK_DETAIL_MST

create table BASE_REMARK_DETAIL_MST( \
    CODE                     VARCHAR(2)   NOT NULL, \
    SEQ                      VARCHAR(2)   NOT NULL, \
    NAME                     VARCHAR(45), \
    QUESTION_CONTENTS        VARCHAR(1000) , \
    ANSWER_PATTERN           VARCHAR(1), \
    ANSWER_SELECT_COUNT      VARCHAR(2), \
    REGISTERCD               VARCHAR(10), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table BASE_REMARK_DETAIL_MST add constraint PK_BASE_RMRK_DTL_M primary key (CODE, SEQ)