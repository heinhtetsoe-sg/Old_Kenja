-- $Id: 8b356166bbf4675538e933678b30e7e75ba9d3b6 $

DROP TABLE CHALLENGED_TRAINING_MST
CREATE TABLE CHALLENGED_TRAINING_MST( \
    NAMECD                  VARCHAR(3)    NOT NULL, \
    TRAINING_CONTENTS       VARCHAR(30), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CHALLENGED_TRAINING_MST ADD CONSTRAINT PK_CHA_TRAIN_MST PRIMARY KEY (NAMECD)