-- $Id: 058ee00024924d90913ca98d0f689c0c306c2fb0 $

ALTER TABLE \
    SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT \
ALTER COLUMN \
    STATUS \
SET DATA TYPE varchar(1800)

ALTER TABLE \
    SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT \
ALTER COLUMN \
    REGISTERCD \
SET DATA TYPE varchar(10)
