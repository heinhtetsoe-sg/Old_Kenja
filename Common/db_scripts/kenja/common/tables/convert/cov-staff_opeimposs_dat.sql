-- $Id: a857dbecf0e9b0e4a13a5e883c1b0f3ca1a388f4 $

UPDATE \
    STAFF_OPEIMPOSS_DAT \
SET \
    DAYCD = CASE WHEN DAYCD = '0' THEN '1' \
                 WHEN DAYCD = '1' THEN '2' \
                 WHEN DAYCD = '2' THEN '3' \
                 WHEN DAYCD = '3' THEN '4' \
                 WHEN DAYCD = '4' THEN '5' \
                 WHEN DAYCD = '5' THEN '6' \
                 ELSE '7' END
