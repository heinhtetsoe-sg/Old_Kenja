<?php

require_once('for_php7.php');

class knjf074Query extends Query {
    //年度取得
    function getSelectYear($model){
        $query  = " SELECT DISTINCT ";
        $query .= "   YEAR AS LABEL, ";
        $query .= "   YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "   MEDEXAM_DET_DAT ";
        $query .= " ORDER BY ";
        $query .= "   YEAR ";
        return $query;
    }

    //学校名称取得
    function getSchoolName($model) {
        $query .= " SELECT SCHOOLNAME1 FROM SCHOOL_MST ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' AND SCHOOLCD = '".SCHOOLCD."' AND SCHOOL_KIND = '".SCHOOL_KIND."' ";
        return $query;

    }

    //CVS作成用のQUERY
    function selectCsvQuery($model) {

        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query .= " WITH SCHREG_B_DIST_MST AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T2.SEX, ";
        $query .= "     YEAR('". CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
        $query .= "     1 AS REC_CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "       ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "       ON T3.YEAR = T1.YEAR ";
        $query .= "      AND T3.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . $model->field["YEAR"] . "' ";
        if ($model->Properties["useSpecial_Support_School"] != "1") {
            // 高校のみ対象
            $query .= "  AND T3.SCHOOL_KIND = 'H' ";
            $query .= "  AND YEAR('". CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) BETWEEN 15 AND 17 ";
        }
        $query .= " ), STD_CNT1 AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "     SCHOOL_KIND, ";
        } else {
            $query .= "     AGE, ";
        }
        $query .= "     SEX, ";
        $query .= "     SUM(REC_CNT) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_B_DIST_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SEX, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "     SCHOOL_KIND ";
        } else {
            $query .= "     AGE ";
        }
        $query .= " ), STD_CNT2 AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEX, ";
        $query .= "     SUM(REC_CNT) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_B_DIST_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SEX ";
        //目の視力レベル判別用のデータを整理(矯正データを優先し、NULLなら裸眼データを取得。)
        $query .= " ), CUT_EYE_ELM AS ( ";
        $query .= " SELECT  ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     VALUE(R_VISION_MARK, '') AS VISION_MARK, ";
        $query .= "     VALUE(R_VISION, '99') AS VISION ";
        $query .= " FROM  ";
        $query .= "  MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . $model->field["YEAR"] . "' ";
        $query .= "     AND (R_VISION_MARK IS NOT NULL OR R_VISION IS NOT NULL) ";
        $query .= " UNION ";
        $query .= " SELECT  ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     VALUE(L_VISION_MARK, '') AS VISION_MARK, ";
        $query .= "     VALUE(L_VISION, '99') AS VISION ";
        $query .= " FROM  ";
        $query .= "  MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . $model->field["YEAR"] . "' ";
        $query .= "     AND (L_VISION_MARK IS NOT NULL OR L_VISION IS NOT NULL) ";
        $query .= " UNION ";
        $query .= " SELECT  ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     VALUE(R_BAREVISION_MARK, '') AS VISION_MARK, ";
        $query .= "     VALUE(R_BAREVISION, '99') AS VISION ";
        $query .= " FROM  ";
        $query .= "     MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . $model->field["YEAR"] . "' ";
        $query .= "     AND R_VISION_MARK IS NULL AND R_VISION IS NULL ";
        $query .= "     AND L_VISION_MARK IS NULL AND L_VISION IS NULL ";
        $query .= "     AND (R_BAREVISION_MARK IS NOT NULL OR R_BAREVISION IS NOT NULL) ";
        $query .= " UNION ";
        $query .= " SELECT  ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     VALUE(L_BAREVISION_MARK, '') AS VISION_MARK, ";
        $query .= "     VALUE(L_BAREVISION, '99') AS VISION ";
        $query .= " FROM  ";
        $query .= "     MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . $model->field["YEAR"] . "' ";
        $query .= "     AND R_VISION_MARK IS NULL AND R_VISION IS NULL ";
        $query .= "     AND L_VISION_MARK IS NULL AND L_VISION IS NULL ";
        $query .= "     AND (L_BAREVISION_MARK IS NOT NULL OR L_BAREVISION IS NOT NULL) ";
        $query .= " ), MIN_EYE_SEARCH AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(VALUE(VISION_MARK, '')) AS VISION_MARK, ";
        $query .= "     CASE WHEN MIN(VALUE(VISION, '')) = '99' ";
        $query .= "          THEN '' ";
        $query .= "          ELSE MIN(VALUE(VISION, '')) ";
        $query .= "     END AS VISION ";
        $query .= " FROM  ";
        $query .= "     CUT_EYE_ELM ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        //肥満度で判定
        //1.やせ傾向（-20%以下）
        //2.肥満傾向（+20%以上）
        $query .= " ), MAX_YEAR AS ( ";
        $query .= "     SELECT ";
        $query .= "         MAX(YEAR) AS YEAR ";
        $query .= "     FROM ";
        $query .= "         HEXAM_PHYSICAL_AVG_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR <= '" . $model->field["YEAR"] . "' ";
        $query .= " ), MAX_NENREI_MONTH AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEX, ";
        $query .= "         T1.NENREI_YEAR, ";
        $query .= "         MAX(T1.NENREI_MONTH) AS NENREI_MONTH ";
        $query .= "     FROM ";
        $query .= "         HEXAM_PHYSICAL_AVG_DAT T1 ";
        $query .= "         INNER JOIN MAX_YEAR T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEX, ";
        $query .= "         T1.NENREI_YEAR ";
        $query .= " ), HEXAM_PHYSICAL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEX, ";
        $query .= "         T1.NENREI_YEAR, ";
        $query .= "         T1.STD_WEIGHT_KEISU_A, ";
        $query .= "         T1.STD_WEIGHT_KEISU_B ";
        $query .= "     FROM ";
        $query .= "         HEXAM_PHYSICAL_AVG_DAT T1 ";
        $query .= "         INNER JOIN MAX_NENREI_MONTH T2 ON T2.YEAR = T1.YEAR AND T2.SEX = T1.SEX AND T2.NENREI_YEAR = T1.NENREI_YEAR AND T2.NENREI_MONTH = T1.NENREI_MONTH ";
        $query .= " ), HIMANDO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.YEAR, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         DECIMAL(ROUND((T1.WEIGHT - (HP.STD_WEIGHT_KEISU_A * T1.HEIGHT - HP.STD_WEIGHT_KEISU_B)) / (HP.STD_WEIGHT_KEISU_A * T1.HEIGHT - HP.STD_WEIGHT_KEISU_B) * 100, 1), 5, 1) AS HIMANDO ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DET_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_B_DIST_MST T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN HEXAM_PHYSICAL HP ON HP.SEX = T2.SEX AND HP.NENREI_YEAR = T2.AGE ";
        $query .= "     WHERE ";
        $query .= "         T2.YEAR = '" . $model->field["YEAR"] . "' ";
        //各項目のデータ件数カウント判定
        $query .= " ), DAT_CNT_LST AS ( ";
        $query .= " SELECT ";
        $query .= "  T2.YEAR AS YEAR, ";
        $query .= "  T2.SCHOOL_KIND AS SCHOOL_KIND, ";
        $query .= "  T2.SCHREGNO AS SCHREGNO, ";
        //年齢は4/1時点の年齢
        $query .= "  T2.AGE, ";
        $query .= "  T2.SEX AS SEX, ";
        $query .= "  CASE WHEN T7.HIMANDO IS NULL THEN NULL ";
        $query .= "       WHEN T7.HIMANDO <= -20  THEN 1 ELSE 0 END AS YASE_VAL, ";
        $query .= "  CASE WHEN T7.HIMANDO IS NULL THEN NULL ";
        $query .= "       WHEN T7.HIMANDO >=  20  THEN 1 ELSE 0 END AS HIMAN_VAL, ";
        $query .= "  CASE WHEN T1.SPINERIBCD IS NULL THEN NULL ";
        $query .= "       WHEN T1.SPINERIBCD = '02' THEN 1 ELSE 0 END AS SEKICYU_SOKUWAN_VAL, ";
        $query .= "  CASE WHEN T1.SPINERIBCD IS NULL THEN NULL ";
        $query .= "       WHEN T1.SPINERIBCD = '03' THEN 1 ELSE 0 END AS OTHER_SEKICYU_SOKUWAN_VAL, ";
        $query .= "  CASE WHEN T1.SPINERIBCD IS NULL THEN NULL ";
        $query .= "       WHEN T1.SPINERIBCD = '04' THEN 1 ELSE 0 END AS SISI_VAL, ";
        $query .= "  CASE WHEN T1.SKINDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.SKINDISEASECD = '02' THEN 1 ELSE 0 END AS ATOPY_SKIN_VAL, ";
        $query .= "  CASE WHEN T1.SKINDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.SKINDISEASECD NOT IN ('01', '02') THEN 1 ELSE 0 END AS OTHER_SKIN_VAL, ";
        $query .= "  CASE WHEN T1.HEARTDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.HEARTDISEASECD <> '01' THEN 1 ELSE 0 END AS HEART_DIS_VAL, ";
        $query .= "  CASE WHEN T1.HEART_MEDEXAM IS NULL THEN NULL ";
        $query .= "       WHEN T1.HEART_MEDEXAM <> '01' THEN 1 ELSE 0 END AS HEARTBEAT_DIS_VAL, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '01' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ";
        $query .= "       ELSE 0 END AS ZENSOKU_VAL, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '02' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ";
        $query .= "       ELSE 0 END AS JINZOU_VAL, ";
        $query .= "  CASE WHEN T1.TB_NAMECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.TB_NAMECD = '02' THEN 1 ELSE 0 END AS KENTOU_VAL, ";
        $query .= "  CASE WHEN T1.TB_NAMECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.TB_NAMECD = '03' THEN 1 ELSE 0 END AS KEKKAKU_CHK_DETAIL_VAL, ";
        $query .= "  CASE WHEN T1.TB_NAMECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.TB_NAMECD = '04' THEN 1 ELSE 0 END AS KEKKAKU_VAL, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '06' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ";
        $query .= "       ELSE 0 END AS OTHER_ARELGY_VAL, ";
        $query .= "  CASE WHEN T1.OTHERDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.OTHERDISEASECD <> '01' THEN 1 ELSE 0 END AS OTHER_DISEASE_VAL, ";
        $query .= "  CASE WHEN T1.NOSEDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.NOSEDISEASECD = '02' THEN 1 ELSE 0 END AS NANCYOU_VAL, ";
        $query .= "  CASE WHEN T3_003.DET_REMARK7 IS NULL THEN NULL ";
        $query .= "       WHEN T3_003.DET_REMARK7 <> '01' THEN 1 ELSE 0 END AS EAR_SIPPEI_VAL, ";
        $query .= "  CASE WHEN T3_003.DET_REMARK8 IS NULL THEN NULL ";
        $query .= "       WHEN T3_003.DET_REMARK8 <> '01' THEN 1 ELSE 0 END AS NOSE_SUB_SIPPEI_VAL, ";
        $query .= "  CASE WHEN T3_003.DET_REMARK9 IS NULL THEN NULL ";
        $query .= "       WHEN T3_003.DET_REMARK9 <> '01' THEN 1 ";
        $query .= "       ELSE 0 END AS THROAT_HEAD_SIPPEI_VAL, ";
        $query .= "  CASE WHEN T1.EYEDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.EYEDISEASECD <> '01' THEN 1 ";
        $query .= "       ELSE 0 END AS EYE_SIPPEI_VAL, ";
        $query .= "  CASE WHEN T1.R_BAREVISION IS NULL AND T1.R_BAREVISION_MARK IS NULL ";
        $query .= "            AND T1.L_BAREVISION IS NULL AND T1.L_BAREVISION_MARK IS NULL ";
        $query .= "            AND T1.R_VISION IS NULL AND T1.R_VISION_MARK IS NULL ";
        $query .= "            AND T1.L_VISION IS NULL AND T1.L_VISION_MARK IS NULL ";
        $query .= "            THEN NULL ";
        $query .= "       WHEN T1.R_BAREVISION IS NOT NULL THEN 1 ";
        $query .= "       WHEN T1.R_BAREVISION_MARK IS NOT NULL THEN 1 ";
        $query .= "       WHEN T1.L_BAREVISION IS NOT NULL THEN 1 ";
        $query .= "       WHEN T1.L_BAREVISION_MARK IS NOT NULL THEN 1 ";
        $query .= "       WHEN T1.R_VISION IS NOT NULL THEN 1 ";
        $query .= "       WHEN T1.R_VISION_MARK IS NOT NULL THEN 1 ";
        $query .= "       WHEN T1.L_VISION IS NOT NULL THEN 1 ";
        $query .= "       WHEN T1.L_VISION_MARK IS NOT NULL THEN 1 ";
        $query .= "       ELSE 0 END AS EYECHK_INSPECT_VAL, ";
        if ($model->Properties["useSpecial_Support_School"] != "1") {
            // 高校のみ対象
            $query .= "  CASE WHEN T1.R_BAREVISION IS NULL AND T1.L_BAREVISION IS NULL THEN NULL ";
            $query .= "       WHEN T1.R_BAREVISION >= '1.0' AND T1.L_BAREVISION >= '1.0' THEN 1 ELSE 0 END AS NAKEDEYE_1_OVER_VAL, ";
        }
        $query .= "  CASE WHEN T6.VISION_MARK = '' AND T6.VISION = '' THEN NULL ";
        $query .= "       WHEN T6.VISION_MARK = 'B' THEN 1 ";
        $query .= "       WHEN T6.VISION_MARK = '' AND ('0.7' <= T6.VISION AND T6.VISION < '1.0') THEN 1 ";
        $query .= "       ELSE 0 END AS B_VAL, ";
        $query .= "  CASE WHEN T6.VISION_MARK = '' AND T6.VISION = '' THEN NULL ";
        $query .= "       WHEN T6.VISION_MARK = 'C' THEN 1 ";
        $query .= "       WHEN T6.VISION_MARK = '' AND ('0.3' <= T6.VISION AND T6.VISION < '0.7') THEN 1 ";
        $query .= "       ELSE 0 END AS C_VAL, ";
        $query .= "  CASE WHEN T6.VISION_MARK = '' AND T6.VISION = '' THEN NULL ";
        $query .= "       WHEN T6.VISION_MARK = 'D' THEN 1 ";
        $query .= "       WHEN T6.VISION_MARK = '' AND T6.VISION < '0.3' THEN 1 ";
        $query .= "       ELSE 0 END AS D_VAL, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  CASE WHEN T1.R_BAREVISION IS NULL AND T1.R_BAREVISION_MARK IS NULL AND T1.L_BAREVISION IS NULL AND T1.L_BAREVISION_MARK IS NULL THEN 1 ";
            $query .= "       ELSE 0 END AS EYECHK_OMIT_VAL, ";
        }
        $query .= "  CASE WHEN T4.SCHREGNO IS NULL THEN NULL ";
        $query .= "       WHEN T4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS TOOTHCHK_INSPECT_VAL, ";
        $query .= "  CASE WHEN T4.REMAINBABYTOOTH IS NULL AND T4.REMAINADULTTOOTH IS NULL AND T4.TREATEDBABYTOOTH IS NULL AND T4.TREATEDADULTTOOTH IS NULL THEN NULL ";
        $query .= "       WHEN T4.REMAINBABYTOOTH = 0 AND T4.REMAINADULTTOOTH = 0 AND T4.TREATEDBABYTOOTH = 0 AND T4.TREATEDADULTTOOTH = 0 THEN 1 ELSE 0 END AS NO_REMAIN_UBA_TOOTH_VAL, ";
        $query .= "  CASE WHEN T4.REMAINBABYTOOTH IS NULL AND T4.REMAINADULTTOOTH IS NULL AND T4.TREATEDBABYTOOTH IS NULL AND T4.TREATEDADULTTOOTH IS NULL THEN NULL ";
        $query .= "       WHEN T4.REMAINBABYTOOTH = 0 AND T4.REMAINADULTTOOTH = 0 AND T4.TREATEDBABYTOOTH + T4.TREATEDADULTTOOTH > 0 THEN 1 ELSE 0 END AS UBA_TOOTH_HOLDER_VAL, ";
        $query .= "  CASE WHEN T4.REMAINBABYTOOTH IS NULL AND T4.REMAINADULTTOOTH IS NULL THEN NULL ";
        $query .= "       WHEN T4.REMAINBABYTOOTH + T4.REMAINADULTTOOTH > 0 THEN 1 ELSE 0 END AS REMAIN_UBA_TOOTH_VAL, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  CASE WHEN T4.REMAINADULTTOOTH IS NULL AND T4.TREATEDADULTTOOTH IS NULL THEN NULL ELSE T4.REMAINADULTTOOTH + T4.TREATEDADULTTOOTH END AS UBA_TOOTH_CNT, ";
            $query .= "  T4.LOSTADULTTOOTH AS LOSTADULTTOOTH_CNT, ";
        }
        $query .= "  CASE WHEN T4.JAWS_JOINTCD2 IS NULL THEN NULL ";
        $query .= "       WHEN T4.JAWS_JOINTCD2 = '03' THEN 1 ELSE 0 END AS OTHER_CHIN_JOINT_VAL, ";
        $query .= "  CASE WHEN T4.JAWS_JOINTCD IS NULL THEN NULL ";
        $query .= "       WHEN T4.JAWS_JOINTCD = '03' THEN 1 ELSE 0 END AS OTHER_TOOTH_ALIGNMENT_VAL, ";
        $query .= "  CASE WHEN T4.PLAQUECD IS NULL THEN NULL ";
        $query .= "       WHEN T4.PLAQUECD = '03' THEN 1 ELSE 0 END AS PLAQUE_VAL, ";
        $query .= "  CASE WHEN T4.GUMCD IS NULL THEN NULL  ";
        $query .= "       WHEN T4.GUMCD = '03' THEN 1 ELSE 0 END AS GUM_VAL, ";
        $query .= "  CASE WHEN T4.OTHERDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T4.OTHERDISEASECD <> '01' THEN 1 ELSE 0 END AS OTHER_TOOTH_SIPPEI_VAL, ";
        $query .= "  CASE WHEN T5_002.TOOTH_REMARK1 IS NULL THEN NULL ";
        $query .= "       WHEN T5_002.TOOTH_REMARK1 <> '01' THEN 1 ELSE 0 END AS TOOTH_DISEASE_VAL, ";
        $query .= "  CASE WHEN T1.ALBUMINURIA1CD IS NULL AND T1.URICSUGAR1CD IS NULL AND T1.URICBLEED1CD IS NULL ";
        $query .= "            AND T1.ALBUMINURIA2CD IS NULL AND T1.URICSUGAR2CD IS NULL AND T1.URICBLEED2CD IS NULL THEN NULL ";
        $query .= "       WHEN T1.ALBUMINURIA1CD IS NOT NULL OR T1.URICSUGAR1CD IS NOT NULL OR T1.URICBLEED1CD IS NOT NULL ";
        $query .= "            OR T1.ALBUMINURIA2CD IS NOT NULL OR T1.URICSUGAR2CD IS NOT NULL OR T1.URICBLEED2CD IS NOT NULL ";
        $query .= "            THEN 1 ELSE 0 END AS URINE_CHK_INSPECT_VAL, ";
        $query .= "  CASE WHEN T1.ALBUMINURIA2CD IS NULL THEN NULL  ";
        $query .= "       WHEN T1.ALBUMINURIA2CD >= '03' THEN 1 ELSE 0 END AS URINE_CHKSND_PROTEIN_VAL, ";
        $query .= "  CASE WHEN T1.URICSUGAR2CD IS NULL THEN NULL ";
        $query .= "       WHEN T1.URICSUGAR2CD >= '03' THEN 1 ELSE 0 END AS URINE_CHKSND_SUGAR_VAL, ";
        $query .= "  CASE WHEN T1.URICBLEED2CD IS NULL THEN NULL ";
        $query .= "       WHEN T1.URICBLEED2CD >= '03' THEN 1 ELSE 0 END AS URINE_CHKSND_HIDEBLOOD_VAL, ";
        $query .= "  CASE WHEN T1.SKINDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.SKINDISEASECD = '02' THEN 1 ELSE 0 END AS ATOPY_SKIN_VAL2, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '01' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS ZENSOKU_VAL2, ";
        $query .= "  CASE WHEN T3_003.DET_REMARK8 IS NULL THEN NULL ";
        $query .= "       WHEN T3_003.DET_REMARK8 = '02' THEN 1 ELSE 0 END AS NOSE_ARELGY_VAL, ";
        $query .= "  CASE WHEN T1.EYEDISEASECD IS NULL THEN NULL ";
        $query .= "       WHEN T1.EYEDISEASECD = '02' THEN 1 ELSE 0 END AS EYE_ARELGY_VAL, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '03' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS FOOD_ALLERGY_VAL, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '07' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS HAY_FEVER_VAL, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '04' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS MEDICINE_ALLERGY_VAL, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '08' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS JINMASIN_VAL, ";
        $query .= "  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ";
        $query .= "       WHEN '06' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS OTHER_ALLERGY_VAL ";
        $query .= " FROM ";
        $query .= "  MEDEXAM_DET_DAT T1 ";
        $query .= "  LEFT JOIN SCHREG_B_DIST_MST T2 ";
        $query .= "    ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "  LEFT JOIN MEDEXAM_DET_DETAIL_DAT T3_003 ";
        $query .= "    ON T3_003.YEAR = T1.YEAR ";
        $query .= "   AND T3_003.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND T3_003.DET_SEQ = '003' ";
        $query .= "  LEFT JOIN MEDEXAM_TOOTH_DAT T4 ";
        $query .= "    ON T4.YEAR = T1.YEAR ";
        $query .= "   AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND (T4.ADULTTOOTH IS NOT NULL ";
        $query .= "        OR T4.REMAINADULTTOOTH IS NOT NULL ";
        $query .= "        OR T4.REMAINBABYTOOTH IS NOT NULL ";
        $query .= "        OR T4.TREATEDADULTTOOTH IS NOT NULL ";
        $query .= "        OR T4.TREATEDBABYTOOTH IS NOT NULL ";
        $query .= "        OR T4.BRACK_ADULTTOOTH IS NOT NULL ";
        $query .= "        OR T4.BRACK_BABYTOOTH IS NOT NULL ";
        $query .= "        OR T4.LOSTADULTTOOTH IS NOT NULL) ";
        $query .= "  LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT T5_002 ";
        $query .= "    ON T5_002.YEAR = T1.YEAR ";
        $query .= "   AND T5_002.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND T5_002.TOOTH_SEQ = '002' ";
        $query .= "  LEFT JOIN MIN_EYE_SEARCH T6 ";
        $query .= "    ON T6.YEAR = T1.YEAR ";
        $query .= "   AND T6.SCHREGNO = T1.SCHREGNO ";
        $query .= "  LEFT JOIN HIMANDO T7 ON T7.YEAR = T1.YEAR AND T7.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "  T2.YEAR = '" . $model->field["YEAR"] . "' ";
        if ($model->Properties["useSpecial_Support_School"] != "1") {
            // 高校のみ対象
            $query .= "  AND T2.SCHOOL_KIND = 'H' ";
        }
        //①集計(男女/年齢(or校種)別)
        $query .= " ), SUMMARY_TBL_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "  T1.YEAR, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  T1.SCHOOL_KIND, ";
            $query .= "  MIN(CAST(T1.AGE AS CHAR(2))) AS AGE, "; //年齢でソート
        } else {
            $query .= "  CAST(T1.AGE AS CHAR(2)) AS AGE, ";
        }
        $query .= "  T1.SEX, ";
        $query .= "  SUM(T1.YASE_VAL) AS YASE_SUM, ";
        $query .= "  SUM(T1.HIMAN_VAL) AS HIMAN_SUM, ";
        $query .= "  SUM(T1.SEKICYU_SOKUWAN_VAL) AS SEKICYU_SOKUWAN_SUM, ";
        $query .= "  SUM(T1.OTHER_SEKICYU_SOKUWAN_VAL) AS OTHER_SEKICYU_SOKUWAN_SUM, ";
        $query .= "  SUM(T1.SISI_VAL) AS SISI_SUM, ";
        $query .= "  SUM(T1.ATOPY_SKIN_VAL) AS ATOPY_SKIN_SUM, ";
        $query .= "  SUM(T1.OTHER_SKIN_VAL) AS OTHER_SKIN_SUM, ";
        $query .= "  SUM(T1.HEART_DIS_VAL) AS HEART_DIS_SUM, ";
        $query .= "  SUM(T1.HEARTBEAT_DIS_VAL) AS HEARTBEAT_DIS_SUM, ";
        $query .= "  SUM(T1.ZENSOKU_VAL) AS ZENSOKU_SUM, ";
        $query .= "  SUM(T1.JINZOU_VAL) AS JINZOU_SUM, ";
        $query .= "  SUM(T1.KENTOU_VAL) AS KENTOU_SUM, ";
        $query .= "  SUM(T1.KEKKAKU_CHK_DETAIL_VAL) AS KEKKAKU_CHK_DETAIL_SUM, ";
        $query .= "  SUM(T1.KEKKAKU_VAL) AS KEKKAKU_SUM, ";
        $query .= "  SUM(T1.OTHER_ARELGY_VAL) AS OTHER_ARELGY_SUM, ";
        $query .= "  SUM(T1.OTHER_DISEASE_VAL) AS OTHER_DISEASE_SUM, ";
        $query .= "  SUM(T1.NANCYOU_VAL) AS NANCYOU_SUM, ";
        $query .= "  SUM(T1.EAR_SIPPEI_VAL) AS EAR_SIPPEI_SUM, ";
        $query .= "  SUM(T1.NOSE_SUB_SIPPEI_VAL) AS NOSE_SUB_SIPPEI_SUM, ";
        $query .= "  SUM(T1.THROAT_HEAD_SIPPEI_VAL) AS THROAT_HEAD_SIPPEI_SUM, ";
        $query .= "  SUM(T1.EYE_SIPPEI_VAL) AS EYE_SIPPEI_SUM, ";
        $query .= "  SUM(T1.EYECHK_INSPECT_VAL) AS EYECHK_INSPECT_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] != "1") {
            $query .= "  SUM(T1.NAKEDEYE_1_OVER_VAL) AS NAKEDEYE_1_OVER_SUM, ";
        }
        $query .= "  SUM(T1.B_VAL) AS B_SUM, ";
        $query .= "  SUM(T1.C_VAL) AS C_SUM, ";
        $query .= "  SUM(T1.D_VAL) AS D_SUM, ";
        $query .= "  SUM(T1.B_VAL) + SUM(T1.C_VAL) + SUM(T1.D_VAL) AS BCD_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  SUM(T1.EYECHK_OMIT_VAL) AS EYECHK_OMIT_SUM, ";
        }
        $query .= "  SUM(T1.TOOTHCHK_INSPECT_VAL) AS TOOTHCHK_INSPECT_SUM, ";
        $query .= "  SUM(T1.NO_REMAIN_UBA_TOOTH_VAL) AS NO_REMAIN_UBA_TOOTH_SUM, ";
        $query .= "  SUM(T1.UBA_TOOTH_HOLDER_VAL) AS UBA_TOOTH_HOLDER_SUM, ";
        $query .= "  SUM(T1.REMAIN_UBA_TOOTH_VAL) AS REMAIN_UBA_TOOTH_SUM, ";
        $query .= "  SUM(T1.UBA_TOOTH_HOLDER_VAL) + SUM(T1.REMAIN_UBA_TOOTH_VAL) AS TOTAL_UBA_TOOTH_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  SUM(T1.UBA_TOOTH_CNT) AS UBA_TOOTH_SUM, ";
            $query .= "  SUM(T1.LOSTADULTTOOTH_CNT) AS LOSTADULTTOOTH_SUM, ";
            $query .= "  SUM(T1.UBA_TOOTH_CNT) + SUM(T1.LOSTADULTTOOTH_CNT) AS TOOTH_DMFT_INDEX, ";
        }
        $query .= "  SUM(T1.OTHER_CHIN_JOINT_VAL) AS OTHER_CHIN_JOINT_SUM, ";
        $query .= "  SUM(T1.OTHER_TOOTH_ALIGNMENT_VAL) AS OTHER_TOOTH_ALIGNMENT_SUM, ";
        $query .= "  SUM(T1.PLAQUE_VAL) AS PLAQUE_SUM, ";
        $query .= "  SUM(T1.GUM_VAL) AS GUM_SUM, ";
        $query .= "  SUM(T1.OTHER_TOOTH_SIPPEI_VAL) AS OTHER_TOOTH_SIPPEI_SUM, ";
        $query .= "  SUM(T1.TOOTH_DISEASE_VAL) AS TOOTH_DISEASE_SUM, ";
        $query .= "  SUM(T1.URINE_CHK_INSPECT_VAL) AS URINE_CHK_INSPECT_SUM, ";
        $query .= "  SUM(T1.URINE_CHKSND_PROTEIN_VAL) AS URINE_CHKSND_PROTEIN_SUM, ";
        $query .= "  SUM(T1.URINE_CHKSND_SUGAR_VAL) AS URINE_CHKSND_SUGAR_SUM, ";
        $query .= "  SUM(T1.URINE_CHKSND_HIDEBLOOD_VAL) AS URINE_CHKSND_HIDEBLOOD_SUM, ";
        //下部の表に出力する項目については、斜線不要なのでNULL=0になるようにする。
        $query .= "  VALUE(SUM(T1.ATOPY_SKIN_VAL2), 0) AS ATOPY_SKIN_SUM2, ";
        $query .= "  VALUE(SUM(T1.ZENSOKU_VAL2), 0) AS ZENSOKU_SUM2, ";
        $query .= "  VALUE(SUM(T1.NOSE_ARELGY_VAL), 0) AS NOSE_ARELGY_SUM, ";
        $query .= "  VALUE(SUM(T1.EYE_ARELGY_VAL), 0) AS EYE_ARELGY_SUM, ";
        $query .= "  VALUE(SUM(T1.FOOD_ALLERGY_VAL), 0) AS FOOD_ALLERGY_SUM, ";
        $query .= "  VALUE(SUM(T1.HAY_FEVER_VAL), 0) AS HAY_FEVER_SUM, ";
        $query .= "  VALUE(SUM(T1.MEDICINE_ALLERGY_VAL), 0) AS MEDICINE_ALLERGY_SUM, ";
        $query .= "  VALUE(SUM(T1.JINMASIN_VAL), 0) AS JINMASIN_SUM, ";
        $query .= "  VALUE(SUM(T1.OTHER_ALLERGY_VAL), 0) AS OTHER_ALLERGY_SUM ";
        $query .= " FROM ";
        $query .= "  DAT_CNT_LST T1 ";
        $query .= " GROUP BY ";
        $query .= "  T1.YEAR, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  T1.SCHOOL_KIND, ";
        } else {
            $query .= "  T1.AGE, ";
        }
        $query .= "  T1.SEX ";
        $query .= "), SUMMARY_TBL_ADDTOTALSTU_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     SC1.CNT AS STU_CNT, ";
        $query .= "     TT1.* ";
        $query .= " FROM ";
        $query .= "     SUMMARY_TBL_DAT TT1 ";
        $query .= "     LEFT JOIN STD_CNT1 SC1 ON TT1.YEAR = SC1.YEAR ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "          AND TT1.SCHOOL_KIND = SC1.SCHOOL_KIND ";
        } else {
            $query .= "          AND TT1.AGE = CAST(SC1.AGE AS CHAR(2)) ";
        }
        $query .= "          AND TT1.SEX = SC1.SEX ";

        //②集計(男女別)
        $query .= " ), TOTAL_SUMMARY_TBL_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "  T1.YEAR, ";
        $query .= "  T1.SEX, ";
        $query .= "  SUM(T1.YASE_VAL) AS YASE_SUM, ";
        $query .= "  SUM(T1.HIMAN_VAL) AS HIMAN_SUM, ";
        $query .= "  SUM(T1.SEKICYU_SOKUWAN_VAL) AS SEKICYU_SOKUWAN_SUM, ";
        $query .= "  SUM(T1.OTHER_SEKICYU_SOKUWAN_VAL) AS OTHER_SEKICYU_SOKUWAN_SUM, ";
        $query .= "  SUM(T1.SISI_VAL) AS SISI_SUM, ";
        $query .= "  SUM(T1.ATOPY_SKIN_VAL) AS ATOPY_SKIN_SUM, ";
        $query .= "  SUM(T1.OTHER_SKIN_VAL) AS OTHER_SKIN_SUM, ";
        $query .= "  SUM(T1.HEART_DIS_VAL) AS HEART_DIS_SUM, ";
        $query .= "  SUM(T1.HEARTBEAT_DIS_VAL) AS HEARTBEAT_DIS_SUM, ";
        $query .= "  SUM(T1.ZENSOKU_VAL) AS ZENSOKU_SUM, ";
        $query .= "  SUM(T1.JINZOU_VAL) AS JINZOU_SUM, ";
        $query .= "  SUM(T1.KENTOU_VAL) AS KENTOU_SUM, ";
        $query .= "  SUM(T1.KEKKAKU_CHK_DETAIL_VAL) AS KEKKAKU_CHK_DETAIL_SUM, ";
        $query .= "  SUM(T1.KEKKAKU_VAL) AS KEKKAKU_SUM, ";
        $query .= "  SUM(T1.OTHER_ARELGY_VAL) AS OTHER_ARELGY_SUM, ";
        $query .= "  SUM(T1.OTHER_DISEASE_VAL) AS OTHER_DISEASE_SUM, ";
        $query .= "  SUM(T1.NANCYOU_VAL) AS NANCYOU_SUM, ";
        $query .= "  SUM(T1.EAR_SIPPEI_VAL) AS EAR_SIPPEI_SUM, ";
        $query .= "  SUM(T1.NOSE_SUB_SIPPEI_VAL) AS NOSE_SUB_SIPPEI_SUM, ";
        $query .= "  SUM(T1.THROAT_HEAD_SIPPEI_VAL) AS THROAT_HEAD_SIPPEI_SUM, ";
        $query .= "  SUM(T1.EYE_SIPPEI_VAL) AS EYE_SIPPEI_SUM, ";
        $query .= "  SUM(T1.EYECHK_INSPECT_VAL) AS EYECHK_INSPECT_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] != "1") {
            $query .= "  SUM(T1.NAKEDEYE_1_OVER_VAL) AS NAKEDEYE_1_OVER_SUM, ";
        }
        $query .= "  SUM(T1.B_VAL) AS B_SUM, ";
        $query .= "  SUM(T1.C_VAL) AS C_SUM, ";
        $query .= "  SUM(T1.D_VAL) AS D_SUM, ";
        $query .= "  SUM(T1.B_VAL) + SUM(T1.C_VAL) + SUM(T1.D_VAL) AS BCD_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  SUM(T1.EYECHK_OMIT_VAL) AS EYECHK_OMIT_SUM, ";
        }
        $query .= "  SUM(T1.TOOTHCHK_INSPECT_VAL) AS TOOTHCHK_INSPECT_SUM, ";
        $query .= "  SUM(T1.NO_REMAIN_UBA_TOOTH_VAL) AS NO_REMAIN_UBA_TOOTH_SUM, ";
        $query .= "  SUM(T1.UBA_TOOTH_HOLDER_VAL) AS UBA_TOOTH_HOLDER_SUM, ";
        $query .= "  SUM(T1.REMAIN_UBA_TOOTH_VAL) AS REMAIN_UBA_TOOTH_SUM, ";
        $query .= "  SUM(T1.UBA_TOOTH_HOLDER_VAL) + SUM(T1.REMAIN_UBA_TOOTH_VAL) AS TOTAL_UBA_TOOTH_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  SUM(T1.UBA_TOOTH_CNT) AS UBA_TOOTH_SUM, ";
            $query .= "  SUM(T1.LOSTADULTTOOTH_CNT) AS LOSTADULTTOOTH_SUM, ";
            $query .= "  SUM(T1.UBA_TOOTH_CNT) + SUM(T1.LOSTADULTTOOTH_CNT) AS TOOTH_DMFT_INDEX, ";
        }
        $query .= "  SUM(T1.OTHER_CHIN_JOINT_VAL) AS OTHER_CHIN_JOINT_SUM, ";
        $query .= "  SUM(T1.OTHER_TOOTH_ALIGNMENT_VAL) AS OTHER_TOOTH_ALIGNMENT_SUM, ";
        $query .= "  SUM(T1.PLAQUE_VAL) AS PLAQUE_SUM, ";
        $query .= "  SUM(T1.GUM_VAL) AS GUM_SUM, ";
        $query .= "  SUM(T1.OTHER_TOOTH_SIPPEI_VAL) AS OTHER_TOOTH_SIPPEI_SUM, ";
        $query .= "  SUM(T1.TOOTH_DISEASE_VAL) AS TOOTH_DISEASE_SUM, ";
        $query .= "  SUM(T1.URINE_CHK_INSPECT_VAL) AS URINE_CHK_INSPECT_SUM, ";
        $query .= "  SUM(T1.URINE_CHKSND_PROTEIN_VAL) AS URINE_CHKSND_PROTEIN_SUM, ";
        $query .= "  SUM(T1.URINE_CHKSND_SUGAR_VAL) AS URINE_CHKSND_SUGAR_SUM, ";
        $query .= "  SUM(T1.URINE_CHKSND_HIDEBLOOD_VAL) AS URINE_CHKSND_HIDEBLOOD_SUM, ";
        //下部の表に出力する項目については、斜線不要なのでNULL=0になるようにする。
        $query .= "  VALUE(SUM(T1.ATOPY_SKIN_VAL2), 0) AS ATOPY_SKIN_SUM2, ";
        $query .= "  VALUE(SUM(T1.ZENSOKU_VAL2), 0) AS ZENSOKU_SUM2, ";
        $query .= "  VALUE(SUM(T1.NOSE_ARELGY_VAL), 0) AS NOSE_ARELGY_SUM, ";
        $query .= "  VALUE(SUM(T1.EYE_ARELGY_VAL), 0) AS EYE_ARELGY_SUM, ";
        $query .= "  VALUE(SUM(T1.FOOD_ALLERGY_VAL), 0) AS FOOD_ALLERGY_SUM, ";
        $query .= "  VALUE(SUM(T1.HAY_FEVER_VAL), 0) AS HAY_FEVER_SUM, ";
        $query .= "  VALUE(SUM(T1.MEDICINE_ALLERGY_VAL), 0) AS MEDICINE_ALLERGY_SUM, ";
        $query .= "  VALUE(SUM(T1.JINMASIN_VAL), 0) AS JINMASIN_SUM, ";
        $query .= "  VALUE(SUM(T1.OTHER_ALLERGY_VAL), 0) AS OTHER_ALLERGY_SUM ";
        $query .= " FROM ";
        $query .= "  DAT_CNT_LST T1 ";
        $query .= " GROUP BY ";
        $query .= "  T1.YEAR, ";
        $query .= "  T1.SEX ";
        $query .= "), TOTAL_SUMMARY_TBL_DAT_ADDTOTALSTU_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     SC2.CNT AS STU_CNT, ";
        $query .= "     TT2.* ";
        $query .= " FROM ";
        $query .= "     TOTAL_SUMMARY_TBL_DAT TT2 ";
        $query .= "     LEFT JOIN STD_CNT2 SC2 ON TT2.YEAR = SC2.YEAR ";
        $query .= "          AND TT2.SEX = SC2.SEX ";
        $query .= " ) ";

        //①、②を並列に並べる(DISP_DATTYPEで、①、②を判別)
        $query .= " SELECT ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "   CASE WHEN T1.SCHOOL_KIND = 'K' THEN '0' ";
            $query .= "    WHEN T1.SCHOOL_KIND = 'P' THEN '1' ";
            $query .= "    WHEN T1.SCHOOL_KIND = 'J' THEN '2' ";
            $query .= "    WHEN T1.SCHOOL_KIND = 'H' THEN '3' ";
            $query .= "    END AS DISP_DATTYPE, ";
        } else {
            $query .= "    '0' AS DISP_DATTYPE, ";
        }
        $query .= "  T1.YEAR, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  T1.SCHOOL_KIND, ";
            $query .= "  T1.AGE, ";
        } else {
            $query .= "  T1.AGE, ";
        }
        $query .= "  T1.SEX, ";
        $query .= "  T1.STU_CNT, ";
        $query .= "  T1.YASE_SUM, ";
        $query .= "  T1.HIMAN_SUM, ";
        $query .= "  T1.SEKICYU_SOKUWAN_SUM, ";
        $query .= "  T1.OTHER_SEKICYU_SOKUWAN_SUM, ";
        $query .= "  T1.SISI_SUM, ";
        $query .= "  T1.ATOPY_SKIN_SUM, ";
        $query .= "  T1.OTHER_SKIN_SUM, ";
        $query .= "  T1.HEART_DIS_SUM, ";
        $query .= "  T1.HEARTBEAT_DIS_SUM, ";
        $query .= "  T1.ZENSOKU_SUM, ";
        $query .= "  T1.JINZOU_SUM, ";
        $query .= "  T1.KENTOU_SUM, ";
        $query .= "  T1.KEKKAKU_CHK_DETAIL_SUM, ";
        $query .= "  T1.KEKKAKU_SUM, ";
        $query .= "  T1.OTHER_ARELGY_SUM, ";
        $query .= "  T1.OTHER_DISEASE_SUM, ";
        $query .= "  T1.NANCYOU_SUM, ";
        $query .= "  T1.EAR_SIPPEI_SUM, ";
        $query .= "  T1.NOSE_SUB_SIPPEI_SUM, ";
        $query .= "  T1.THROAT_HEAD_SIPPEI_SUM, ";
        $query .= "  T1.EYE_SIPPEI_SUM, ";
        $query .= "  T1.EYECHK_INSPECT_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] != "1") {
            $query .= "  T1.NAKEDEYE_1_OVER_SUM, ";
        }
        $query .= "  T1.B_SUM, ";
        $query .= "  T1.C_SUM, ";
        $query .= "  T1.D_SUM, ";
        $query .= "  T1.BCD_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  T1.EYECHK_OMIT_SUM, ";
        }
        $query .= "  T1.TOOTHCHK_INSPECT_SUM, ";
        $query .= "  T1.NO_REMAIN_UBA_TOOTH_SUM, ";
        $query .= "  T1.UBA_TOOTH_HOLDER_SUM, ";
        $query .= "  T1.REMAIN_UBA_TOOTH_SUM, ";
        $query .= "  T1.TOTAL_UBA_TOOTH_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  T1.UBA_TOOTH_SUM, ";
            $query .= "  T1.LOSTADULTTOOTH_SUM, ";
            $query .= "  T1.TOOTH_DMFT_INDEX, ";
        }
        $query .= "  T1.OTHER_CHIN_JOINT_SUM, ";
        $query .= "  T1.OTHER_TOOTH_ALIGNMENT_SUM, ";
        $query .= "  T1.PLAQUE_SUM, ";
        $query .= "  T1.GUM_SUM, ";
        $query .= "  T1.OTHER_TOOTH_SIPPEI_SUM, ";
        $query .= "  T1.TOOTH_DISEASE_SUM, ";
        $query .= "  T1.URINE_CHK_INSPECT_SUM, ";
        $query .= "  T1.URINE_CHKSND_PROTEIN_SUM, ";
        $query .= "  T1.URINE_CHKSND_SUGAR_SUM, ";
        $query .= "  T1.URINE_CHKSND_HIDEBLOOD_SUM, ";
        $query .= "  T1.ATOPY_SKIN_SUM2, ";
        $query .= "  T1.ZENSOKU_SUM2, ";
        $query .= "  T1.NOSE_ARELGY_SUM, ";
        $query .= "  T1.EYE_ARELGY_SUM, ";
        $query .= "  T1.FOOD_ALLERGY_SUM, ";
        $query .= "  T1.HAY_FEVER_SUM, ";
        $query .= "  T1.MEDICINE_ALLERGY_SUM, ";
        $query .= "  T1.JINMASIN_SUM, ";
        $query .= "  T1.OTHER_ALLERGY_SUM ";
        $query .= " FROM ";
        $query .= "  SUMMARY_TBL_ADDTOTALSTU_DAT T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "  '9' AS DISP_DATTYPE, ";
        $query .= "  T2.YEAR, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  '合計' AS SCHOOL_KIND, ";
            $query .= "  '' AS AGE, ";
        } else {
            $query .= "  '合計' AS AGE, ";
        }
        $query .= "  T2.SEX, ";
        $query .= "  T2.STU_CNT, ";
        $query .= "  T2.YASE_SUM, ";
        $query .= "  T2.HIMAN_SUM, ";
        $query .= "  T2.SEKICYU_SOKUWAN_SUM, ";
        $query .= "  T2.OTHER_SEKICYU_SOKUWAN_SUM, ";
        $query .= "  T2.SISI_SUM, ";
        $query .= "  T2.ATOPY_SKIN_SUM, ";
        $query .= "  T2.OTHER_SKIN_SUM, ";
        $query .= "  T2.HEART_DIS_SUM, ";
        $query .= "  T2.HEARTBEAT_DIS_SUM, ";
        $query .= "  T2.ZENSOKU_SUM, ";
        $query .= "  T2.JINZOU_SUM, ";
        $query .= "  T2.KENTOU_SUM, ";
        $query .= "  T2.KEKKAKU_CHK_DETAIL_SUM, ";
        $query .= "  T2.KEKKAKU_SUM, ";
        $query .= "  T2.OTHER_ARELGY_SUM, ";
        $query .= "  T2.OTHER_DISEASE_SUM, ";
        $query .= "  T2.NANCYOU_SUM, ";
        $query .= "  T2.EAR_SIPPEI_SUM, ";
        $query .= "  T2.NOSE_SUB_SIPPEI_SUM, ";
        $query .= "  T2.THROAT_HEAD_SIPPEI_SUM, ";
        $query .= "  T2.EYE_SIPPEI_SUM, ";
        $query .= "  T2.EYECHK_INSPECT_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] != "1") {
            $query .= "  T2.NAKEDEYE_1_OVER_SUM, ";
        }
        $query .= "  T2.B_SUM, ";
        $query .= "  T2.C_SUM, ";
        $query .= "  T2.D_SUM, ";
        $query .= "  T2.BCD_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  T2.EYECHK_OMIT_SUM, ";
        }
        $query .= "  T2.TOOTHCHK_INSPECT_SUM, ";
        $query .= "  T2.NO_REMAIN_UBA_TOOTH_SUM, ";
        $query .= "  T2.UBA_TOOTH_HOLDER_SUM, ";
        $query .= "  T2.REMAIN_UBA_TOOTH_SUM, ";
        $query .= "  T2.TOTAL_UBA_TOOTH_SUM, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  T2.UBA_TOOTH_SUM, ";
            $query .= "  T2.LOSTADULTTOOTH_SUM, ";
            $query .= "  T2.TOOTH_DMFT_INDEX, ";
        }
        $query .= "  T2.OTHER_CHIN_JOINT_SUM, ";
        $query .= "  T2.OTHER_TOOTH_ALIGNMENT_SUM, ";
        $query .= "  T2.PLAQUE_SUM, ";
        $query .= "  T2.GUM_SUM, ";
        $query .= "  T2.OTHER_TOOTH_SIPPEI_SUM, ";
        $query .= "  T2.TOOTH_DISEASE_SUM, ";
        $query .= "  T2.URINE_CHK_INSPECT_SUM, ";
        $query .= "  T2.URINE_CHKSND_PROTEIN_SUM, ";
        $query .= "  T2.URINE_CHKSND_SUGAR_SUM, ";
        $query .= "  T2.URINE_CHKSND_HIDEBLOOD_SUM, ";
        $query .= "  T2.ATOPY_SKIN_SUM2, ";
        $query .= "  T2.ZENSOKU_SUM2, ";
        $query .= "  T2.NOSE_ARELGY_SUM, ";
        $query .= "  T2.EYE_ARELGY_SUM, ";
        $query .= "  T2.FOOD_ALLERGY_SUM, ";
        $query .= "  T2.HAY_FEVER_SUM, ";
        $query .= "  T2.MEDICINE_ALLERGY_SUM, ";
        $query .= "  T2.JINMASIN_SUM, ";
        $query .= "  T2.OTHER_ALLERGY_SUM ";
        $query .= " FROM ";
        $query .= "  TOTAL_SUMMARY_TBL_DAT_ADDTOTALSTU_DAT T2 ";
        $query .= " ORDER BY ";
        $query .= "  YEAR, ";
        $query .= "  SEX, ";
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "  DISP_DATTYPE, ";
            $query .= "  AGE, "; //校種では並ばないので、最少年齢でソート。空文字は合計なので、最大に。
            $query .= "  SCHOOL_KIND ";
        } else {
            $query .= "  AGE ";
        }

        return $query;
    }
}
?>
