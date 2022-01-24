-- $Id: 118d079b8998b3528c09a27463d12646975b11c5 $

UPDATE \
    MEDEXAM_DET_DAT \
SET \
    R_BAREVISION_MARK = CASE WHEN R_BAREVISION IS null THEN '' \
                             WHEN R_BAREVISION < '0.3' THEN 'D' \
                             WHEN R_BAREVISION < '0.7' THEN 'C' \
                             WHEN R_BAREVISION < '1.0'   THEN 'B' \
                             ELSE 'A' END, \
    L_BAREVISION_MARK = CASE WHEN L_BAREVISION IS null THEN '' \
                             WHEN L_BAREVISION < '0.3' THEN 'D' \
                             WHEN L_BAREVISION < '0.7' THEN 'C' \
                             WHEN L_BAREVISION < '1.0'   THEN 'B' \
                             ELSE 'A' END, \
    R_VISION_MARK     = CASE WHEN R_VISION IS null THEN '' \
                             WHEN R_VISION < '0.3' THEN 'D' \
                             WHEN R_VISION < '0.7' THEN 'C' \
                             WHEN R_VISION < '1.0'   THEN 'B' \
                             ELSE 'A' END, \
    L_VISION_MARK     = CASE WHEN L_VISION IS null THEN '' \
                             WHEN L_VISION < '0.3' THEN 'D' \
                             WHEN L_VISION < '0.7' THEN 'C' \
                             WHEN L_VISION < '1.0'   THEN 'B' \
                             ELSE 'A' END
