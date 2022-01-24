<?php

require_once('for_php7.php');

class knjf072Query extends Query {

    function getNameMst($namecd1, $namecd2) {
        $query = "";
        $query .= " SELECT NAMECD2, NAME2 ";
        $query .= " FROM V_NAME_MST ";
        $query .="  WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
          $query .="  AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY NAMECD1 ";
        return $query;
    }

    //学校名
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .="  WHERE ";
        $query .="      YEAR = '".CTRL_YEAR."' ";
        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model) {
        $query  = "";
        $query .= " WITH GRADE4 (SCHREGNO, YEAR, GRADE) AS ( ";
        $query .= "     VALUES ";
        $query .= "         ('00000000', '".CTRL_YEAR."', '04') ";
        $query .= " ), REGD AS ( ";
        $query .= "   SELECT T1.SCHREGNO, T1.YEAR, T1.GRADE ";
        $query .= "   FROM SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO  ";
        $query .= "   WHERE T1.YEAR = '".CTRL_YEAR."' AND  T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND '".CTRL_YEAR."-05-01' BETWEEN VALUE(T2.ENT_DATE, '1900-01-01') AND VALUE(T2.GRD_DATE, '9999-12-31') ";
        $query .= "     AND T2.SEX = '".$model->field["SEX"]."' ";
        $query .= "   UNION ";
        $query .= "   SELECT SCHREGNO, YEAR, GRADE ";
        $query .= "   FROM GRADE4 ";
        $query .= " ), SCHREG_NENREI AS ( ";
        $query .= "   SELECT T2.SCHREGNO, T2.SEX, T2.BIRTHDAY,  ";
        $query .= "      CASE WHEN T2.BIRTHDAY IS NULL THEN INT(REGD.GRADE) + 15 ELSE T2.NENREI_YEAR END AS NENREI_YEAR, ";
        $query .= "      CASE WHEN T2.BIRTHDAY IS NULL THEN 0 ELSE T2.NENREI_MONTH END AS NENREI_MONTH ";
        $query .= "   FROM ( ";
        $query .= "     SELECT SCHREGNO, SEX, BIRTHDAY, ";
        $query .= "               DIFFY - CASE WHEN DIFFM < 0 THEN 1 ELSE 0 END AS NENREI_YEAR, ";
        $query .= "               DIFFM + CASE WHEN DIFFM < 0 THEN 12 ELSE 0 END AS NENREI_MONTH ";
        $query .= "     FROM ( ";
        $query .= "       SELECT SCHREGNO, SEX, BIRTHDAY, DIFFY, CASE WHEN DIFFD < 0 THEN DIFFM - 1 ELSE DIFFM END AS DIFFM ";
        $query .= "       FROM  ";
        $query .= "       ( ";
        $query .= "         SELECT  ";
        $query .= "           SCHREGNO, SEX, BIRTHDAY, ";
        $query .= "           YEAR('".CTRL_YEAR."-05-01') - YEAR(BIRTHDAY) AS DIFFY, ";
        $query .= "           MONTH('".CTRL_YEAR."-05-01') - MONTH(BIRTHDAY) AS DIFFM, ";
        $query .= "           DAY('".CTRL_YEAR."-05-01') - DAY(BIRTHDAY) AS DIFFD ";
        $query .= "         FROM SCHREG_BASE_MST ";
        $query .= "       ) T0 ";
        $query .= "     ) T1 ";
        $query .= "   ) T2 ";
        $query .= "   INNER JOIN REGD ON REGD.SCHREGNO = T2.SCHREGNO  ";
        $query .= " ), T_PHYS AS ( ";
        $query .= "   SELECT T1.SCHREGNO, MAX(T2.YEAR) AS PHYS_YEAR ";
        $query .= "   FROM SCHREG_NENREI T1 ";
        $query .= "   LEFT JOIN HEXAM_PHYSICAL_AVG_DAT T2 ON T2.SEX = T1.SEX AND T2.NENREI_YEAR = T1.NENREI_YEAR ";
        $query .= "     AND T2.NENREI_MONTH <= T1.NENREI_MONTH ";
        $query .= "   GROUP BY T1.SCHREGNO ";
        $query .= " ), T_PHYS2 AS ( ";
        $query .= "   SELECT  T1.SCHREGNO, T2.PHYS_YEAR, MAX(T3.NENREI_MONTH) AS PHYS_NENREI_MONTH ";
        $query .= "   FROM SCHREG_NENREI T1 ";
        $query .= "   INNER JOIN T_PHYS T2 ON T2.SCHREGNO = T1.SCHREGNO  ";
        $query .= "   INNER JOIN HEXAM_PHYSICAL_AVG_DAT T3 ON T3.YEAR = T2.PHYS_YEAR AND T3.SEX = T1.SEX AND T3.NENREI_YEAR = T1.NENREI_YEAR ";
        $query .= "   GROUP BY T1.SCHREGNO, T2.PHYS_YEAR ";
        $query .= " ), SCHREG_PHYS_AVG AS ( ";
        $query .= "   SELECT T1.SCHREGNO,  T3.YEAR, T3.SEX, T3.NENREI_YEAR, T3.NENREI_MONTH, T3.STD_WEIGHT_KEISU_A, T3.STD_WEIGHT_KEISU_B ";
        $query .= "   FROM SCHREG_NENREI T1 ";
        $query .= "   INNER JOIN T_PHYS2 T2 ON T2.SCHREGNO = T1.SCHREGNO  ";
        $query .= "   INNER JOIN HEXAM_PHYSICAL_AVG_DAT T3 ON T3.YEAR = T2.PHYS_YEAR AND T3.SEX = T1.SEX AND T3.NENREI_YEAR = T1.NENREI_YEAR AND T3.NENREI_MONTH = T2.PHYS_NENREI_MONTH ";
        $query .= " ), GRADES AS ( ";
        $query .= "   SELECT T1.GRADE, T1.GRADE_NAME1, COUNT(T2.SCHREGNO) AS COUNT ";
        $query .= "   FROM SCHREG_REGD_GDAT T1 ";
        $query .= "   INNER JOIN REGD T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
        $query .= "   GROUP BY T1.GRADE, T1.GRADE_NAME1 ";
        $query .= " ), DETDAT AS ( ";
        $query .= "   SELECT T2.GRADE, T1.*, ";
        $query .= "     CASE WHEN T1.R_BAREVISION IS NOT NULL OR T1.R_BAREVISION_MARK IS NOT NULL OR T1.L_BAREVISION IS NOT NULL OR T1.L_BAREVISION_MARK IS NOT NULL THEN 1 END AS BARE_COUNT, ";
        $query .= "     CASE WHEN T1.R_BAREVISION IS NOT NULL AND '1.0' <= T1.R_BAREVISION                             OR T1.R_BAREVISION_MARK IS NOT NULL AND T1.R_BAREVISION_MARK = 'A' THEN 1 ";
        $query .= "          WHEN T1.R_BAREVISION IS NOT NULL AND '0.7' <= T1.R_BAREVISION AND T1.R_BAREVISION < '1.0' OR T1.R_BAREVISION_MARK IS NOT NULL AND T1.R_BAREVISION_MARK = 'B' THEN 2 ";
        $query .= "          WHEN T1.R_BAREVISION IS NOT NULL AND '0.3' <= T1.R_BAREVISION AND T1.R_BAREVISION < '1.0' OR T1.R_BAREVISION_MARK IS NOT NULL AND T1.R_BAREVISION_MARK = 'C' THEN 3 ";
        $query .= "          WHEN T1.R_BAREVISION IS NOT NULL AND                              T1.R_BAREVISION < '0.3' OR T1.R_BAREVISION_MARK IS NOT NULL AND T1.R_BAREVISION_MARK = 'D' THEN 4 ";
        $query .= "     END AS R_BARE_RANK, ";
        $query .= "     CASE WHEN T1.L_BAREVISION IS NOT NULL AND '1.0' <= T1.L_BAREVISION                             OR T1.L_BAREVISION_MARK IS NOT NULL AND T1.L_BAREVISION_MARK = 'A' THEN 1 ";
        $query .= "          WHEN T1.L_BAREVISION IS NOT NULL AND '0.7' <= T1.L_BAREVISION AND T1.L_BAREVISION < '1.0' OR T1.L_BAREVISION_MARK IS NOT NULL AND T1.L_BAREVISION_MARK = 'B' THEN 2 ";
        $query .= "          WHEN T1.L_BAREVISION IS NOT NULL AND '0.3' <= T1.L_BAREVISION AND T1.L_BAREVISION < '1.0' OR T1.L_BAREVISION_MARK IS NOT NULL AND T1.L_BAREVISION_MARK = 'C' THEN 3 ";
        $query .= "          WHEN T1.L_BAREVISION IS NOT NULL AND                              T1.L_BAREVISION < '0.3' OR T1.L_BAREVISION_MARK IS NOT NULL AND T1.L_BAREVISION_MARK = 'D' THEN 4 ";
        $query .= "     END AS L_BARE_RANK, ";
        $query .= "     T3.STD_WEIGHT_KEISU_A * T1.HEIGHT - STD_WEIGHT_KEISU_B AS STD_WEIGHT ";
        $query .= "   FROM V_MEDEXAM_DET_DAT T1 ";
        $query .= "   INNER JOIN MEDEXAM_HDAT HD ON HD.SCHREGNO = T1.SCHREGNO AND HD.YEAR = T1.YEAR AND HD.DATE IS NOT NULL ";
        $query .= "   INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ";
        $query .= "   LEFT JOIN SCHREG_PHYS_AVG T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " ), TOOTHDAT AS ( ";
        $query .= "   SELECT T2.GRADE, T1.* ";
        $query .= "   FROM V_MEDEXAM_TOOTH_DAT T1 ";
        $query .= "   INNER JOIN MEDEXAM_HDAT HD ON HD.SCHREGNO = T1.SCHREGNO AND HD.YEAR = T1.YEAR AND HD.TOOTH_DATE IS NOT NULL ";
        $query .= "   INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ";
        $query .= " ) ";
        $query .= "  SELECT ";
        $query .= "   VALUE(T0.GRADE, '99') AS GRADE, ";
        $query .= "   COUNT(case when T0.SCHREGNO = '00000000' then null else T0.SCHREGNO end) AS B, ";
        $query .= "   COUNT(T1.BARE_COUNT) AS C1, ";
        $query .= "   SUM(CASE WHEN (CASE WHEN T1.R_BARE_RANK < T1.L_BARE_RANK THEN T1.L_BARE_RANK ELSE T1.R_BARE_RANK END) BETWEEN 1 AND 4 THEN 1 END) AS C2, ";
        $query .= "   SUM(CASE WHEN (CASE WHEN T1.R_BARE_RANK < T1.L_BARE_RANK THEN T1.L_BARE_RANK ELSE T1.R_BARE_RANK END) = 1 THEN 1 END) AS C2_A, ";
        $query .= "   SUM(CASE WHEN (CASE WHEN T1.R_BARE_RANK < T1.L_BARE_RANK THEN T1.L_BARE_RANK ELSE T1.R_BARE_RANK END) = 2 THEN 1 END) AS C2_B, ";
        $query .= "   SUM(CASE WHEN (CASE WHEN T1.R_BARE_RANK < T1.L_BARE_RANK THEN T1.L_BARE_RANK ELSE T1.R_BARE_RANK END) = 3 THEN 1 END) AS C2_C, ";
        $query .= "   SUM(CASE WHEN (CASE WHEN T1.R_BARE_RANK < T1.L_BARE_RANK THEN T1.L_BARE_RANK ELSE T1.R_BARE_RANK END) = 4 THEN 1 END) AS C2_D, ";
        $query .= "   SUM(CASE WHEN (T1.R_VISION IS NOT NULL OR T1.L_VISION IS NOT NULL OR T1.R_VISION_MARK IS NOT NULL OR T1.L_VISION_MARK IS NOT NULL) THEN 1 END) AS C3, ";
        $query .= "   SUM(CASE WHEN T1.EYEDISEASECD <> '02' THEN 1 END) AS D1, ";
        $query .= "   SUM(CASE WHEN T1.EYEDISEASECD = '05' THEN 1 END) AS D2, ";
        $query .= "   SUM(CASE WHEN T1.EYEDISEASECD <> '01' AND T1.EYEDISEASECD <> '02' AND T1.EYEDISEASECD <> '05' THEN 1 END) AS D3, ";
        $query .= "   SUM(CASE WHEN T1.R_EAR = '01' OR T1.R_EAR = '03' OR T1.R_EAR_DB IS NOT NULL OR ";
        $query .= "                 T1.L_EAR = '01' OR T1.L_EAR = '03' OR T1.L_EAR_DB IS NOT NULL THEN 1 END) AS E1, ";
        $query .= "   SUM(CASE WHEN T1.R_EAR = '03' OR T1.L_EAR = '03' OR T1.NOSEDISEASECD <> '06' THEN 1 END) AS E2, ";
        $query .= "   SUM(CASE WHEN T1.NOSEDISEASECD <> '02' THEN 1 END) AS F1, ";
        $query .= "   SUM(CASE WHEN T1.NOSEDISEASECD  IN ('03', '04', '05', '08') OR  ";
        $query .= "                 T1.NOSEDISEASECD2 IN ('03', '04', '05', '08') OR  ";
        $query .= "                 T1.NOSEDISEASECD3 IN ('03', '04', '05', '08') OR  ";
        $query .= "                 T1.NOSEDISEASECD4 IN ('03', '04', '05', '08')  ";
        $query .= "         THEN 1 END) AS F2, ";
        $query .= "   SUM(CASE WHEN T1.NOSEDISEASECD  = '09' OR  ";
        $query .= "                 T1.NOSEDISEASECD2 = '09' OR  ";
        $query .= "                 T1.NOSEDISEASECD3 = '09' OR  ";
        $query .= "                 T1.NOSEDISEASECD4 = '09'  ";
        $query .= "         THEN 1 END) AS F3, ";
        $query .= "   SUM(CASE WHEN T1.NOSEDISEASECD  IN ('10', '11', '13') OR  ";
        $query .= "                 T1.NOSEDISEASECD2 IN ('10', '11', '13') OR  ";
        $query .= "                 T1.NOSEDISEASECD3 IN ('10', '11', '13') OR  ";
        $query .= "                 T1.NOSEDISEASECD4 IN ('10', '11', '13')  ";
        $query .= "         THEN 1 END) AS F4, ";
        $query .= "   SUM(CASE WHEN T1.NOSEDISEASECD  IN ('14', '15', '17', '18') OR  ";
        $query .= "                 T1.NOSEDISEASECD2 IN ('14', '15', '17', '18') OR  ";
        $query .= "                 T1.NOSEDISEASECD3 IN ('14', '15', '17', '18') OR  ";
        $query .= "                 T1.NOSEDISEASECD4 IN ('14', '15', '17', '18')  ";
        $query .= "         THEN 1 END) AS F5, ";
        $query .= "   COUNT(T2.SCHREGNO) AS G1, ";
        $query .= "   SUM(CASE WHEN VALUE(T2.REMAINBABYTOOTH, 0) + VALUE(T2.REMAINADULTTOOTH, 0) = 0 AND VALUE(T2.TREATEDBABYTOOTH, 0) + VALUE(T2.TREATEDADULTTOOTH, 0) >= 1 THEN 1 END) AS G2, ";
        $query .= "   SUM(CASE WHEN VALUE(T2.REMAINBABYTOOTH, 0) > 0 OR VALUE(T2.REMAINADULTTOOTH, 0) > 0 THEN 1 END) AS G3, ";
        $query .= "   SUM(CASE WHEN VALUE(T2.BRACK_ADULTTOOTH, 0) > 0 THEN 1 END) AS G4, ";
        $query .= "   SUM(CASE WHEN VALUE(T2.DENTISTREMARK_CO, 0) > 0 THEN 1 END) AS G5, ";
        $query .= "   SUM(CASE WHEN T2.JAWS_JOINTCD2 = '03' THEN 1 END) AS G6, ";
        $query .= "   SUM(CASE WHEN T2.JAWS_JOINTCD = '03' OR T2.JAWS_JOINTCD3 = '03' THEN 1 END) AS G7, ";
        $query .= "   SUM(CASE WHEN T2.PLAQUECD = '03' THEN 1 END) AS G8, ";
        $query .= "   SUM(CASE WHEN T2.GUMCD = '03' THEN 1 END) AS G9, ";
        $query .= "   SUM(CASE WHEN T2.OTHERDISEASECD <> '01' THEN 1 END) AS G10, ";
        $query .= "   SUM(CASE WHEN T1.SPINERIBCD <> '02' THEN 1 END) AS H, ";
        $query .= "   SUM(CASE WHEN (WEIGHT - STD_WEIGHT) / STD_WEIGHT * 100.0  >= 20.0 THEN 1 END) AS I1, ";
        $query .= "   SUM(CASE WHEN (WEIGHT - STD_WEIGHT) / STD_WEIGHT * 100.0  <= -20.0 THEN 1 END) AS I2, ";
        $query .= "   SUM(CASE WHEN T1.NUTRITIONCD = '03' THEN 1 END) AS I3, ";
        $query .= "   SUM(CASE WHEN T1.SPINERIBCD <> '01' AND T1.SPINERIBCD <> '02' AND T1.SPINERIBCD <> '08' THEN 1 END) AS J1, ";
        $query .= "   SUM(CASE WHEN T1.SPINERIBCD = '08' THEN 1 END) AS J2, ";
        $query .= "   SUM(CASE WHEN T1.SKINDISEASECD = '04' OR T1.OTHERDISEASECD = '99' AND T1.OTHER_REMARK LIKE '%アトピー性皮膚炎%' THEN 1 END) AS K1, ";
        $query .= "   SUM(CASE WHEN T1.SKINDISEASECD <> '01' AND T1.SKINDISEASECD <> '02' AND T1.SKINDISEASECD <> '04' THEN 1 END) AS K2, ";
        $query .= "   SUM(CASE WHEN T1.TB_REMARKCD <> '02' THEN 1 END) AS L1, ";
        $query .= "   SUM(CASE WHEN T1.TB_OTHERTESTCD = '03' THEN 1 END) AS L2, ";
        $query .= "   SUM(CASE WHEN T1.TB_REMARKCD = '03' THEN 1 END) AS L3, ";
        $query .= "   SUM(CASE WHEN T1.HEART_MEDEXAM <> '02' THEN 1 END) AS M1, ";
        $query .= "   SUM(CASE WHEN T1.HEARTDISEASECD IN ('09', '10', '11', '12', '13', '14', '99') THEN 1 END) AS M2, ";
        $query .= "   SUM(CASE WHEN T1.HEART_MEDEXAM = '03' THEN 1 END) AS M3, ";
        $query .= "   SUM(CASE WHEN T1.ALBUMINURIA1CD <> '01' OR T1.URICSUGAR1CD <> '01' OR T1.URICBLEED1CD <> '01' THEN 1 END) AS N1, ";
        $query .= "   SUM(CASE WHEN T1.ALBUMINURIA1CD IN ('03', '04') THEN 1 END) AS N2, ";
        $query .= "   SUM(CASE WHEN T1.URICSUGAR1CD IN ('04', '05', '06') THEN 1 END) AS N3, ";
        $query .= "   SUM(CASE WHEN T1.URICBLEED1CD IN ('04', '05', '06') THEN 1 END) AS N4, ";
        $query .= "   SUM(CASE WHEN T1.OTHERDISEASECD = '03' THEN 1 END) AS O1, ";
        $query .= "   SUM(CASE WHEN T1.MEDICAL_HISTORY1 = '7' OR T1.MEDICAL_HISTORY2 = '7' OR T1.MEDICAL_HISTORY3 = '7' OR ";
        $query .= "                 T1.OTHER_REMARK IN ('ネフローゼ', '急性腎炎', '慢性腎炎', '腎炎', '腎臓疾患') OR ";
        $query .= "                 T1.OTHER_REMARK2 IN ('ネフローゼ', '急性腎炎', '慢性腎炎', '腎炎', '腎臓疾患') OR ";
        $query .= "                 T1.OTHER_REMARK3 IN ('ネフローゼ', '急性腎炎', '慢性腎炎', '腎炎', '腎臓疾患')  ";
        $query .= "       THEN 1 END) AS O2, ";
        $query .= "   SUM(CASE WHEN T1.OTHERDISEASECD = '09' THEN 1 END) AS O3, ";
        $query .= "   SUM(CASE WHEN VALUE(T1.OTHERDISEASECD, '') <> '03' AND ";
        $query .= "                (VALUE(T1.MEDICAL_HISTORY1, '') <> '7' OR VALUE(T1.MEDICAL_HISTORY2, '') <> '7' OR VALUE(T1.MEDICAL_HISTORY3, '') <> '7') AND ";
        $query .= "                (VALUE(T1.OTHER_REMARK, '') NOT IN ('ネフローゼ', '急性腎炎', '慢性腎炎', '腎炎', '腎臓疾患') AND ";
        $query .= "                 VALUE(T1.OTHER_REMARK2, '') NOT IN ('ネフローゼ', '急性腎炎', '慢性腎炎', '腎炎', '腎臓疾患') AND ";
        $query .= "                 VALUE(T1.OTHER_REMARK3, '') NOT IN ('ネフローゼ', '急性腎炎', '慢性腎炎', '腎炎', '腎臓疾患')) AND ";
        $query .= "                 VALUE(T1.OTHERDISEASECD, '') <> '09' AND ";
        $query .= "                 (T1.OTHERDISEASECD <> '01' OR T1.MEDICAL_HISTORY1 <> '0' OR T1.MEDICAL_HISTORY2 <> '0' OR T1.MEDICAL_HISTORY3 <> '0' OR T1.OTHER_REMARK IS NOT NULL OR T1.OTHER_REMARK2 IS NOT NULL OR T1.OTHER_REMARK3 IS NOT NULL) ";
        $query .= "       THEN 1 END) AS O4 ";
        $query .= "  FROM REGD T0 ";
        $query .= "  LEFT JOIN DETDAT T1 ON T1.SCHREGNO = T0.SCHREGNO ";
        $query .= "  LEFT JOIN TOOTHDAT T2 ON T2.SCHREGNO = T0.SCHREGNO ";
        $query .= "  GROUP BY GROUPING SETS ((T0.GRADE), ()) ";
        $query .= "  ORDER BY GRADE ";

        return $query;
    }
}
?>
