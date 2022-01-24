-- $Id: v_rec_schooling_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP VIEW v_rec_schooling_dat

CREATE VIEW v_rec_schooling_dat AS \
SELECT \
    year, \
    classcd, \
    curriculum_cd, \
    subclasscd, \
    schregno, \
    schooling_type, \
    MAX(seq) AS seq, \
    SUM(get_value) as total_minute \
FROM \
    rec_schooling_dat \
GROUP BY \
    year, \
    classcd, \
    curriculum_cd, \
    subclasscd, \
    schregno, \
    schooling_type
