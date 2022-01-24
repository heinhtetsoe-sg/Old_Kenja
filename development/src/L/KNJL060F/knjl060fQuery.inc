<?php

require_once('for_php7.php');
class knjl060fQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入試回数(2:高校のみ)
    public function getTestdiv0($year, $testdiv)
    {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = 'L034' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //--- DELETE(1:中学) 算数
    public function getDeleteJsubclass2Query($model)
    {
        $query  = "";
        //基本母集団
        $query .= " DELETE FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T1.TESTSUBCLASSCD = '2' "; // 算数
        $query .= "     AND EXISTS (SELECT 'X' ";
        $query .= "         FROM V_ENTEXAM_RECEPT_DAT I1 ";
        //特別入試対象者を取得
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT I2 ";
        $query .= "              ON I2.ENTEXAMYEAR = I1.ENTEXAMYEAR ";
        $query .= "             AND I2.EXAMNO = I1.EXAMNO ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND I2.SPECIAL_REASON_DIV IS NOT NULL ";
        }
        $query .= "         WHERE ";
        $query .= "            I1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "            AND I1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND I1.TESTDIV = T1.TESTDIV ";
        $query .= "            AND I1.RECEPTNO = T1.RECEPTNO ";
        $query .= "     ) ";

        return $query;
    }

    //--- UPDATE(1:中学) 算数=算数1+算数2
    public function getUpdateJsubclass2Query($model)
    {
        $query  = "";
        //基本母集団
        $query .= " INSERT INTO ENTEXAM_SCORE_DAT ";
        $query .= "     (ENTEXAMYEAR, ";
        $query .= "      APPLICANTDIV, ";
        $query .= "      TESTDIV, ";
        $query .= "      EXAM_TYPE, ";
        $query .= "      RECEPTNO, ";
        $query .= "      TESTSUBCLASSCD, ";
        $query .= "      ATTEND_FLG, ";
        $query .= "      SCORE, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED) ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, "; // ENTEXAM_SCORE_DAT.EXAM_TYPE = '1'
        $query .= "     T1.RECEPTNO, ";
        $query .= "     '2' AS TESTSUBCLASSCD, ";
        // 両方欠席なら0、それ以外は1
        $query .= "     CASE WHEN (M1.ATTEND_FLG = '0' AND M2.ATTEND_FLG = '0') THEN '0' ";
        $query .= "          ELSE '1' ";
        $query .= "     END AS ATTEND_FLG, ";
        // 両方欠席ならNULL、それ以外で片方でも得点があれば合計、それ以外はNULL
        $query .= "     CASE WHEN (M1.ATTEND_FLG = '0' AND M2.ATTEND_FLG = '0')  THEN CAST(NULL AS SMALLINT)  ";
        $query .= "          WHEN (M1.SCORE IS NOT NULL OR M2.SCORE IS NOT NULL) THEN VALUE(M1.SCORE, 0) + VALUE(M2.SCORE,0) ";
        $query .= "          ELSE CAST(NULL AS SMALLINT) ";
        $query .= "     END AS SCORE, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        //特別入試対象者を取得
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO = T1.EXAMNO ";
        // 算数1
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT M1 ";
        $query .= "              ON M1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND M1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND M1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "             AND M1.RECEPTNO = T1.RECEPTNO ";
        $query .= "             AND M1.TESTSUBCLASSCD = '7' ";
        // 算数2
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT M2 ";
        $query .= "              ON M2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND M2.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND M2.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "             AND M2.RECEPTNO = T1.RECEPTNO ";
        $query .= "             AND M2.TESTSUBCLASSCD = '8' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        }

        return $query;
    }

    //--- DELETE(1:中学) 理科
    public function getDeleteJsubclass3Query($model)
    {
        $query  = "";
        //基本母集団
        $query .= " DELETE FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T1.TESTSUBCLASSCD = '3' "; // 理科
        $query .= "     AND EXISTS (SELECT 'X' ";
        $query .= "         FROM V_ENTEXAM_RECEPT_DAT I1 ";
        //特別入試対象者を取得
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT I2 ";
        $query .= "              ON I2.ENTEXAMYEAR = I1.ENTEXAMYEAR ";
        $query .= "             AND I2.EXAMNO = I1.EXAMNO ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND I2.SPECIAL_REASON_DIV IS NOT NULL ";
        }
        $query .= "         WHERE ";
        $query .= "            I1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "            AND I1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND I1.TESTDIV = T1.TESTDIV ";
        $query .= "            AND I1.RECEPTNO = T1.RECEPTNO ";
        $query .= "     ) ";

        return $query;
    }

    //--- UPDATE(1:中学) 理科=理科1+理科2
    public function getUpdateJsubclass3Query($model)
    {
        $query  = "";
        //基本母集団
        $query .= " INSERT INTO ENTEXAM_SCORE_DAT ";
        $query .= "     (ENTEXAMYEAR, ";
        $query .= "      APPLICANTDIV, ";
        $query .= "      TESTDIV, ";
        $query .= "      EXAM_TYPE, ";
        $query .= "      RECEPTNO, ";
        $query .= "      TESTSUBCLASSCD, ";
        $query .= "      ATTEND_FLG, ";
        $query .= "      SCORE, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED) ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, "; // ENTEXAM_SCORE_DAT.EXAM_TYPE = '1'
        $query .= "     T1.RECEPTNO, ";
        $query .= "     '3' AS TESTSUBCLASSCD, ";
        // 両方欠席なら0、それ以外は1
        $query .= "     CASE WHEN (M1.ATTEND_FLG = '0' AND M2.ATTEND_FLG = '0') THEN '0' ";
        $query .= "          ELSE '1' ";
        $query .= "     END AS ATTEND_FLG, ";
        // 両方欠席ならNULL、それ以外で片方でも得点があれば合計、それ以外はNULL
        $query .= "     CASE WHEN (M1.ATTEND_FLG = '0' AND M2.ATTEND_FLG = '0')  THEN CAST(NULL AS SMALLINT)  ";
        $query .= "          WHEN (M1.SCORE IS NOT NULL OR M2.SCORE IS NOT NULL) THEN VALUE(M1.SCORE, 0) + VALUE(M2.SCORE,0) ";
        $query .= "          ELSE CAST(NULL AS SMALLINT) ";
        $query .= "     END AS SCORE, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        //特別入試対象者を取得
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO = T1.EXAMNO ";
        // 理科1
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT M1 ";
        $query .= "              ON M1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND M1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND M1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "             AND M1.RECEPTNO = T1.RECEPTNO ";
        $query .= "             AND M1.TESTSUBCLASSCD = 'A' ";
        // 理科2
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT M2 ";
        $query .= "              ON M2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND M2.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND M2.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "             AND M2.RECEPTNO = T1.RECEPTNO ";
        $query .= "             AND M2.TESTSUBCLASSCD = 'B' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        }

        return $query;
    }

    //--- DELETE(1:中学) 社会
    public function getDeleteJsubclass4Query($model)
    {
        $query  = "";
        //基本母集団
        $query .= " DELETE FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T1.TESTSUBCLASSCD = '4' "; // 社会
        $query .= "     AND EXISTS (SELECT 'X' ";
        $query .= "         FROM V_ENTEXAM_RECEPT_DAT I1 ";
        //特別入試対象者を取得
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT I2 ";
        $query .= "              ON I2.ENTEXAMYEAR = I1.ENTEXAMYEAR ";
        $query .= "             AND I2.EXAMNO = I1.EXAMNO ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND I2.SPECIAL_REASON_DIV IS NOT NULL ";
        }
        $query .= "         WHERE ";
        $query .= "            I1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "            AND I1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND I1.TESTDIV = T1.TESTDIV ";
        $query .= "            AND I1.RECEPTNO = T1.RECEPTNO ";
        $query .= "     ) ";

        return $query;
    }

    //--- UPDATE(1:中学) 社会=社会1+社会2
    public function getUpdateJsubclass4Query($model)
    {
        $query  = "";
        //基本母集団
        $query .= " INSERT INTO ENTEXAM_SCORE_DAT ";
        $query .= "     (ENTEXAMYEAR, ";
        $query .= "      APPLICANTDIV, ";
        $query .= "      TESTDIV, ";
        $query .= "      EXAM_TYPE, ";
        $query .= "      RECEPTNO, ";
        $query .= "      TESTSUBCLASSCD, ";
        $query .= "      ATTEND_FLG, ";
        $query .= "      SCORE, ";
        $query .= "      REGISTERCD, ";
        $query .= "      UPDATED) ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, "; // ENTEXAM_SCORE_DAT.EXAM_TYPE = '1'
        $query .= "     T1.RECEPTNO, ";
        $query .= "     '4' AS TESTSUBCLASSCD, ";
        // 両方欠席なら0、それ以外は1
        $query .= "     CASE WHEN (M1.ATTEND_FLG = '0' AND M2.ATTEND_FLG = '0') THEN '0' ";
        $query .= "          ELSE '1' ";
        $query .= "     END AS ATTEND_FLG, ";
        // 両方欠席ならNULL、それ以外で片方でも得点があれば合計、それ以外はNULL
        $query .= "     CASE WHEN (M1.ATTEND_FLG = '0' AND M2.ATTEND_FLG = '0')  THEN CAST(NULL AS SMALLINT)  ";
        $query .= "          WHEN (M1.SCORE IS NOT NULL OR M2.SCORE IS NOT NULL) THEN VALUE(M1.SCORE, 0) + VALUE(M2.SCORE,0) ";
        $query .= "          ELSE CAST(NULL AS SMALLINT) ";
        $query .= "     END AS SCORE, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        //特別入試対象者を取得
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO = T1.EXAMNO ";
        // 社会1
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT M1 ";
        $query .= "              ON M1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND M1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND M1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "             AND M1.RECEPTNO = T1.RECEPTNO ";
        $query .= "             AND M1.TESTSUBCLASSCD = 'C' ";
        // 社会2
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT M2 ";
        $query .= "              ON M2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND M2.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND M2.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "             AND M2.RECEPTNO = T1.RECEPTNO ";
        $query .= "             AND M2.TESTSUBCLASSCD = 'D' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        }

        return $query;
    }

    //--- UPDATE(1:中学)
    public function getUpdateJQuery($model)
    {
        $query  = "";
        //基本母集団
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.JUDGEDIV, ";
        $query .= "     T2.GENERAL_FLG, ";
        $query .= "     TT2.TOTAL AS TOTAL2, ";
        $query .= "     CASE WHEN L2.REMARK{$model->field["TESTDIV"]} = '1' THEN TT2.TOTAL ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '2' THEN TT2.TOTAL ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '3' THEN T34.TOTAL ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '4' THEN T44.TOTAL ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '5' THEN T54.TOTAL ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} in ('6','7','8') THEN T678.TOTAL ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '9' THEN T9.TOTAL ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = 'A' THEN TA.TOTAL ";
        $query .= "     END AS TOTAL4, ";
        $query .= "     CASE WHEN L2.REMARK{$model->field["TESTDIV"]} = '1' THEN TT2.ATTEND_ALL_FLG ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '2' THEN TT2.ATTEND_ALL_FLG ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '3' THEN T34.ATTEND_ALL_FLG ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '4' THEN T44.ATTEND_ALL_FLG ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '5' THEN T54.ATTEND_ALL_FLG ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} in ('6','7','8') THEN T678.ATTEND_ALL_FLG ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = '9' THEN T9.ATTEND_ALL_FLG ";
        $query .= "          WHEN L2.REMARK{$model->field["TESTDIV"]} = 'A' THEN TA.ATTEND_ALL_FLG ";
        $query .= "     END AS ATTEND_ALL_FLG4, ";
        $query .= "     L2.REMARK{$model->field["TESTDIV"]} AS EXAM_TYPE, ";
        $query .= "     BASE_D014.REMARK9 AS EIKEN ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        //特別入試対象者を取得
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO = T1.EXAMNO ";
        //受験型
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT L2 ";
        $query .= "              ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND L2.EXAMNO = T1.EXAMNO ";
        $query .= "             AND L2.SEQ = '011' ";
        //英検
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D014 ";
        $query .= "              ON BASE_D014.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND BASE_D014.EXAMNO      = T1.EXAMNO ";
        $query .= "             AND BASE_D014.SEQ         = '014' ";
        //TOTAL2・・・1:国、2:算
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        //入試区分「16:得意型文京方式」「17:得意型２科」の場合、国語または算数の高い得点を2倍にし、200点満点で判定
        if ($model->field["TESTDIV"] == "16" || $model->field["TESTDIV"] == "17") {
            $query .= "               ,MAX(T1.SCORE) * 2                AS TOTAL ";
        } else {
            $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        }
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('1','2') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) TT2 ON TT2.RECEPTNO  = T1.RECEPTNO ";
        //受験型3:TOTAL4・・・1:国、2:算、3:理、4:社
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('1','2','3','4') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T34 ON T34.RECEPTNO  = T1.RECEPTNO ";
        //受験型4:TOTAL4・・・5:英
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('5') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T44 ON T44.RECEPTNO  = T1.RECEPTNO ";
        //受験型5:TOTAL4・・・1:国、2:算、5:英
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('1','2','5') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T54 ON T54.RECEPTNO  = T1.RECEPTNO ";
        //受験型6:グローバル・・・科目G:思考力総合問題
        //受験型7:サイエンス・・・科目H:レポート作成
        //受験型8:スポーツ・・・・科目H:レポート作成
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('G','H') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T678 ON T678.RECEPTNO  = T1.RECEPTNO ";
        //受験型9:インタラクティブ・・・・科目I:英活
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('I') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T9 ON T9.RECEPTNO  = T1.RECEPTNO ";
        //受験型A:適性検査・・・・科目J:適性検査Ⅰ、K:適性検査Ⅱ
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('J','K') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) TA ON TA.RECEPTNO  = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        }

        return $query;
    }

    //--- UPDATE(1:中学)
    public function &getUpdateJ($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //教科型入試の時、計算する。
        if (in_array($model->field["TESTDIV"], array("1", "2", "3", "5", "16", "17"))) {
            // 対象の2:算数のレコードを削除
            $query = knjl060fQuery::getDeleteJsubclass2Query($model);
            $db->query($query);
            // 対象の2:算数のレコードを7:算数1、8:算数2の合計で作成
            $query = knjl060fQuery::getUpdateJsubclass2Query($model);
            $db->query($query);
        }

        //第５回の時、計算する。
        if ($model->field["TESTDIV"] == "5") {
            // 対象の3:理科のレコードを削除
            $query = knjl060fQuery::getDeleteJsubclass3Query($model);
            $db->query($query);
            // 対象の3:理科のレコードをA:理科1、B:理科2の合計で作成
            $query = knjl060fQuery::getUpdateJsubclass3Query($model);
            $db->query($query);

            // 対象の4:社会のレコードを削除
            $query = knjl060fQuery::getDeleteJsubclass4Query($model);
            $db->query($query);
            // 対象の4:社会のレコードをC:社会1、D:社会2の合計で作成
            $query = knjl060fQuery::getUpdateJsubclass4Query($model);
            $db->query($query);
        }

        //合計を取得
        $query = knjl060fQuery::getUpdateJQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //選択科目TOP2----------START
            //受験型2
            $totalTop2 = "";
            $attendTop2 = "";
            if ($row["EXAM_TYPE"] == "2") {
                $cnt = 0;
                //選択科目TOP2を取得
                $query = knjl060fQuery::getUpdateJSelectSubclassTop2($model, $row["RECEPTNO"]);
                $resultTop2 = $db->query($query);
                while ($rowTop2 = $resultTop2->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $totalTop2 += $rowTop2["SCORE"];
                    $attendTop2 = "1";
                    $cnt++;
                    //TOP2の選択科目コードをセット
                    $where  = " WHERE ";
                    $where .= "     ENTEXAMYEAR = '{$model->year}' ";
                    $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                    $where .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
                    $where .= "     AND RECEPTNO = '{$rowTop2["RECEPTNO"]}' ";
                    $where .= "     AND SEQ = '004' ";
                    //データチェック
                    $getCount = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_RECEPT_DETAIL_DAT " .$where);
                    //UPDATE
                    $data = array();
                    $data["REMARK{$cnt}"][TEXT] = $rowTop2["TESTSUBCLASSCD"];
                    $fieldName = ($cnt == 1) ? "REMARK3" : "REMARK4";
                    $data[$fieldName][TEXT]     = $rowTop2["SCORE"]; //最高1,2
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";
                    if ($getCount == 0) {
                        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
                        $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
                        $data["EXAM_TYPE"][TEXT]    = "1";
                        $data["RECEPTNO"][TEXT]     = $rowTop2["RECEPTNO"];
                        $data["SEQ"][TEXT]          = "004";
                        $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                    } else {
                        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
                    }
                    $db->query($query);
                    //TOP2の時、ループから抜ける。
                    if ($cnt == 2) {
                        break 1;
                    }
                }
                $resultTop2->free();
            }
            //選択科目TOP2----------END

            //選択科目TOP2----------START(英検取得者への対応)
            //受験型2
            //英検取得級 1:5級 2:4級 3:3級以上
            //5級は、　　最高1:25点、最高2:最もよい点　をセット
            //4級は、　　最高1:25点、最高2:25点　　　　をセット
            //3級以上は、最高1:25点、最高2:25点　　　　をセット
            if ($row["EXAM_TYPE"] == "2" && strlen($row["EIKEN"]) && $row["JUDGEDIV"] != "4") { //欠席者以外
                // チャレンジイングリッシュは英検を使用しない
                if ($row["EIKEN"] != "6") {
                    //4級と3級以上は、25点を最高1,最高2にセット
                    $score1 = 25; //最高1
                    $score2 = 25; //最高2
                    //5級の時のみ、最もよい点を最高2にセット
                    if ($row["EIKEN"] == "1") {
                        $query = knjl060fQuery::getUpdateJSelectSubclassTop2($model, $row["RECEPTNO"]);
                        $rowTop2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $score2 = $rowTop2["SCORE"];
                    }
                    //選択合計
                    $totalTop2 = strlen($score2) ? $score1 + $score2 : $score1;
                }
                //WHERE
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR = '{$model->year}' ";
                $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
                $where .= "     AND RECEPTNO = '{$row["RECEPTNO"]}' ";
                $where .= "     AND SEQ = '004' ";
                //データチェック
                $getCount = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_RECEPT_DETAIL_DAT " .$where);
                //UPDATE
                $data = array();
                if ($row["EIKEN"] != "6") {
                    $data["REMARK3"][TEXT]      = $score1; //最高1
                    $data["REMARK4"][TEXT]      = $score2; //最高2
                }
                $data["REMARK5"][TEXT]      = $row["EIKEN"]; //英検取得級
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                if ($getCount == 0) {
                    $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                    $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
                    $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
                    $data["EXAM_TYPE"][TEXT]    = "1";
                    $data["RECEPTNO"][TEXT]     = $row["RECEPTNO"];
                    $data["SEQ"][TEXT]          = "004";
                    $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
            //選択科目TOP2----------END(英検取得者への対応)

            //echo "RECEPTNO=".$row["RECEPTNO"] .", TOTAL2=".$row["TOTAL2"]  .", TOTAL4=".$row["TOTAL4"] .", EXAM_TYPE=".$row["EXAM_TYPE"] .", totalTop2=".$totalTop2 ."<BR>";

            //条件
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR = '{$model->year}' ";
            $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $where .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
            $where .= "     AND EXAM_TYPE = '1' ";
            $where .= "     AND RECEPTNO = '{$row["RECEPTNO"]}' ";
            //UPDATE
            $data = array();
            //受験型2
            if ($row["EXAM_TYPE"] == "2" && !strlen($totalTop2) && !strlen($row["ATTEND_ALL_FLG4"])) {
                $data["ATTEND_ALL_FLG"][TEXT]   = "";
            } elseif ($row["EXAM_TYPE"] == "2" && strlen($totalTop2) && $row["ATTEND_ALL_FLG4"] == "1") {
                $data["ATTEND_ALL_FLG"][TEXT]   = "1";
            } elseif ($row["EXAM_TYPE"] == "2") {
                $data["ATTEND_ALL_FLG"][TEXT]   = "0";
            } else {
                $data["ATTEND_ALL_FLG"][TEXT]   = $row["ATTEND_ALL_FLG4"];
            }
            $data["TOTAL4"][NUMBER]         = ($row["EXAM_TYPE"] == "2" && strlen($totalTop2)) ? ($row["TOTAL4"] + $totalTop2) : $row["TOTAL4"];
            $data["TOTAL2"][NUMBER]         = ($row["EXAM_TYPE"] == "1" || $row["EXAM_TYPE"] == "2" || $row["EXAM_TYPE"] == "3") ? $row["TOTAL2"] : $row["TOTAL4"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //第５回の時、特別入試対象者は、合格とする。
            if ($model->field["TESTDIV"] == "5" && $row["GENERAL_FLG"] == "1" && $row["JUDGEDIV"] != "4") {
                $data["JUDGEDIV"][TEXT]         = "1";//1:合格
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- 選択科目TOP2(1:中学)
    public function getUpdateJSelectSubclassTop2($model, $receptno)
    {
        $query  = "";
        //基本母集団
        $query .= " SELECT ";
        $query .= "     S1.RECEPTNO, ";
        $query .= "     S1.TESTSUBCLASSCD, ";
        $query .= "     S1.SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT S1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L009' AND N1.NAMECD2 = S1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     S1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND S1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND S1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND S1.EXAM_TYPE = '1' ";
        $query .= "     AND S1.RECEPTNO = '{$receptno}' ";
        $query .= "     AND N1.NAMESPARE1 IS NOT NULL ";//選択科目
        $query .= "     AND S1.SCORE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     S1.SCORE DESC, ";
        $query .= "     S1.TESTSUBCLASSCD ";

        return $query;
    }

    //--- UPDATE(2:高校)
    public function getUpdateHQuery($model)
    {
        $query  = "";
        //基本母集団
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        //Ａ推薦・帰国生Ａ用
        if ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "4") {
            $query .= "     CASE WHEN L1.REMARK10 = '1001' THEN T22.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '1002' THEN T22.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '2001' THEN T52.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '2002' THEN T52.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '3001' AND L2.SELECT_SUBCLASS_DIV = '2' THEN T22.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '3001' AND L2.SELECT_SUBCLASS_DIV = '5' THEN T52.TOTAL ";
            $query .= "     END AS TOTAL2, ";
            $query .= "     CASE WHEN L1.REMARK10 = '1001' THEN T24.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '1002' THEN T24.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '2001' THEN T54.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '2002' THEN T54.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '3001' AND L2.SELECT_SUBCLASS_DIV = '2' THEN T24.TOTAL ";
            $query .= "          WHEN L1.REMARK10 = '3001' AND L2.SELECT_SUBCLASS_DIV = '5' THEN T54.TOTAL ";
            $query .= "     END AS TOTAL4, ";
            $query .= "     CASE WHEN L1.REMARK10 = '1001' THEN T24.ATTEND_ALL_FLG ";
            $query .= "          WHEN L1.REMARK10 = '1002' THEN T24.ATTEND_ALL_FLG ";
            $query .= "          WHEN L1.REMARK10 = '2001' THEN T54.ATTEND_ALL_FLG ";
            $query .= "          WHEN L1.REMARK10 = '2002' THEN T54.ATTEND_ALL_FLG ";
            $query .= "          WHEN L1.REMARK10 = '3001' AND L2.SELECT_SUBCLASS_DIV = '2' THEN T24.ATTEND_ALL_FLG ";
            $query .= "          WHEN L1.REMARK10 = '3001' AND L2.SELECT_SUBCLASS_DIV = '5' THEN T54.ATTEND_ALL_FLG ";
            $query .= "     END AS ATTEND_ALL_FLG4, ";
        } else {
            $query .= "     TT4.TOTAL AS TOTAL2, ";
            $query .= "     TT4.TOTAL AS TOTAL4, ";
            $query .= "     TT4.ATTEND_ALL_FLG AS ATTEND_ALL_FLG4, ";
        }
        $query .= "     L1.REMARK10 AS EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        //入試回数
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "             AND T2.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "             AND T2.RECEPTNO = T1.RECEPTNO ";
        $query .= "             AND T2.SEQ = '003' ";
        //受験コース
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.EXAMNO = T1.EXAMNO ";
        $query .= "             AND L1.SEQ = '001' ";
        //受験科目・・・スポーツ科学のＡ推薦・推薦または帰国生Ａ方式受験の方
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT L2 ";
        $query .= "              ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND L2.EXAMNO = T1.EXAMNO ";
        //TOTAL4・・・1:国、2:数、5:英、9:プレゼンテーション
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        if ($model->field["TESTDIV"] == "7") {
            $query .= "            AND T1.TESTSUBCLASSCD IN ('2','5','9') ";
        } else {
            $query .= "            AND T1.TESTSUBCLASSCD IN ('1','2','5') ";
        }
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) TT4 ON TT4.RECEPTNO  = T1.RECEPTNO ";
        //以下は、Ａ推薦・帰国生Ａ用
        //TOTAL2・・・2:数
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('2') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T22 ON T22.RECEPTNO  = T1.RECEPTNO ";
        //TOTAL4・・・2:数、6:作
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('2','6') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T24 ON T24.RECEPTNO  = T1.RECEPTNO ";
        //TOTAL2・・・5:英
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('5') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T52 ON T52.RECEPTNO  = T1.RECEPTNO ";
        //TOTAL4・・・5:英、6:作
        $query .= "     LEFT JOIN ( ";
        $query .= "        SELECT T1.RECEPTNO ";
        $query .= "               ,CASE WHEN MIN(T1.ATTEND_FLG) = '0' THEN '0' ELSE '1' END AS ATTEND_ALL_FLG ";
        $query .= "               ,SUM(T1.SCORE)                    AS TOTAL ";
        $query .= "               ,ROUND(AVG(DECIMAL(T1.SCORE)),1)  AS AVERAGE ";
        $query .= "           FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "          WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "            AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "            AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "            AND T1.TESTSUBCLASSCD IN ('5','6') ";
        $query .= "         GROUP BY T1.RECEPTNO ";
        $query .= "     ) T54 ON T54.RECEPTNO  = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T2.REMARK1 = '{$model->field["TESTDIV0"]}' ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     AND L2.SPECIAL_REASON_DIV IS NOT NULL ";
        }

        return $query;
    }

    //--- UPDATE(2:高校)
    public function &getUpdateH($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合計を取得
        $query = knjl060fQuery::getUpdateHQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //echo "RECEPTNO=".$row["RECEPTNO"] .", TOTAL2=".$row["TOTAL2"] .", TOTAL4=".$row["TOTAL4"] ."<BR>";
            //条件
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR = '{$model->year}' ";
            $where .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $where .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
            $where .= "     AND EXAM_TYPE = '1' ";
            $where .= "     AND RECEPTNO = '{$row["RECEPTNO"]}' ";
            //UPDATE
            $data = array();
            $data["ATTEND_ALL_FLG"][TEXT]   = $row["ATTEND_ALL_FLG4"];
            $data["TOTAL4"][NUMBER]         = $row["TOTAL4"];
            $data["TOTAL2"][NUMBER]         = $row["TOTAL2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    public function getDataCheck($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //対象人数チェック
        $query  = "SELECT COUNT(*) FROM V_ENTEXAM_RECEPT_DAT T1 ";
        //入試回数
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT T2 ";
            $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "             AND T2.TESTDIV = T1.TESTDIV ";
            $query .= "             AND T2.EXAM_TYPE = T1.EXAM_TYPE ";
            $query .= "             AND T2.RECEPTNO = T1.RECEPTNO ";
            $query .= "             AND T2.SEQ = '003' ";
            $query .= "             AND T2.REMARK1 = '{$model->field["TESTDIV0"]}' ";
        }
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "              ON BASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "             AND BASE.EXAMNO = T1.EXAMNO ";
            $query .= "             AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
        }
        $query .= " WHERE T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db);
            return false;
        }
        //得点データチェック
        $query  = "SELECT COUNT(*) FROM ENTEXAM_SCORE_DAT T3 ";
        $query .= "     INNER JOIN V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "              ON T1.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV = T3.TESTDIV ";
        $query .= "             AND T1.EXAM_TYPE = T3.EXAM_TYPE ";
        $query .= "             AND T1.RECEPTNO = T3.RECEPTNO ";
        //入試回数
        if ($model->field["APPLICANTDIV"] == "2") {
            $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT T2 ";
            $query .= "              ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "             AND T2.TESTDIV = T1.TESTDIV ";
            $query .= "             AND T2.EXAM_TYPE = T1.EXAM_TYPE ";
            $query .= "             AND T2.RECEPTNO = T1.RECEPTNO ";
            $query .= "             AND T2.SEQ = '003' ";
            $query .= "             AND T2.REMARK1 = '{$model->field["TESTDIV0"]}' ";
        }
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "              ON BASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "             AND BASE.EXAMNO = T1.EXAMNO ";
            $query .= "             AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
        }
        $query .= " WHERE T3.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND T3.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND T3.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        if ($db->getOne($query) == "0") {
            Query::dbCheckIn($db);
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }
}
