<?php

require_once('for_php7.php');

class knjl370mQuery extends Query {
    //入試制度
    function getApplicantdiv($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.NAMECD2 || ' ' || L1.NAME1 AS LABEL, ";
        $query .= "     L1.NAMECD2 AS VALUE, ";
        $query .= "     L1.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "                 AND L1.NAMECD1 = 'L003' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMESPARE2, ";
        $query .= "     L1.NAMECD2 ";

        return $query;
    }

    function getKamokuSql($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 AS TESTSUBCLASSCD, ";
        $query .= "     L1.TESTPAPERCD, ";
        $query .= "     T1.NAME1 || L1.TESTPAPERCD AS TESTPAPERNAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_PERFECT_DETAIL_MST L1 ON T1.NAMECD2 = L1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->test_year."' ";
        $query .= "     AND T1.NAMECD1 = 'L009' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 AS TESTSUBCLASSCD, ";
        $query .= "     '9' AS TESTPAPERCD, ";
        $query .= "     T1.NAME1 AS TESTPAPERNAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->test_year."' ";
        $query .= "     AND T1.NAMECD1 = 'L009' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     TESTPAPERCD, ";
        $query .= "     TESTPAPERNAME, ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY TESTSUBCLASSCD, TESTPAPERCD) AS ORDERCD ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     TESTPAPERCD ";

        return $query;
    }

    function getStudentSql($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T1.FS_NAME, ";
        $query .= "     T1.FS_NATPUBPRIDIV, ";
        $query .= "     NATP.NATPUBPRI_NAME, ";
        $query .= "     T1.FS_AREA_DIV, ";
        $query .= "     AREA_D.AREA_DIV_NAME, ";
        $query .= "     T1.FS_AREA_CD, ";
        $query .= "     ARE_M.AREA_NAME, ";
        $query .= "     RECEPT.TOTAL4, ";
        $query .= "     RECEPT.TOTAL_RANK4, ";
        $query .= "     RECEPT.JUDGE_DEVIATION, ";
        $query .= "     RECEPT.JUDGE_DEVIATION_RANK, ";
        $query .= "     RECEPT.LINK_JUDGE_DEVIATION, ";
        $query .= "     RECEPT.LINK_JUDGE_DEVIATION_RANK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON T1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "          AND T1.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "          AND T1.TESTDIV = RECEPT.TESTDIV ";
        $query .= "          AND RECEPT.EXAM_TYPE = '1' ";
        $query .= "          AND T1.EXAMNO = RECEPT.RECEPTNO ";
        $query .= "          AND T1.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_NATPUBPRI_MST NATP ON T1.FS_NATPUBPRIDIV = NATP.NATPUBPRI_CD ";
        $query .= "     LEFT JOIN ENTEXAM_AREA_DIV_MST AREA_D ON T1.FS_NATPUBPRIDIV = AREA_D.NATPUBPRI_CD ";
        $query .= "          AND T1.FS_AREA_DIV = AREA_D.AREA_DIV_CD ";
        $query .= "     LEFT JOIN ENTEXAM_AREA_MST ARE_M ON T1.FS_NATPUBPRIDIV = ARE_M.NATPUBPRI_CD ";
        $query .= "          AND T1.FS_AREA_DIV = ARE_M.AREA_DIV_CD ";
        $query .= "          AND T1.FS_AREA_CD = ARE_M.AREA_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->test_year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV = '1' ";
        $query .= "     AND T1.SHDIV = '1' ";
        $query .= "     AND T1.DESIREDIV = '1' ";
        $query .= " ORDER BY ";
        if ($model->field["ORDER_DIV"] == "2") {
            $query .= "     VALUE(RECEPT.TOTAL4, 0) DESC, ";
            $query .= "     T1.EXAMNO ";
        } else if ($model->field["ORDER_DIV"] == "3") {
            $query .= "     VALUE(RECEPT.JUDGE_DEVIATION_RANK, 999999), ";
            $query .= "     T1.EXAMNO ";
        } else if ($model->field["ORDER_DIV"] == "4") {
            $query .= "     VALUE(RECEPT.LINK_JUDGE_DEVIATION_RANK, 999999), ";
            $query .= "     T1.EXAMNO ";
        } else {
            $query .= "     T1.EXAMNO ";
        }

        return $query;
    }

    function getScoreDataSql($model, $examNo, $subclassCd, $paperCd) {
        if ($paperCd == "9") {
            $query .= " SELECT ";
            $query .= "     SCORE, ";
            $query .= "     DECIMAL(STD_SCORE, 5, 1) AS STD_SCORE ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '".$model->test_year."' ";
            $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "     AND TESTDIV = '1' ";
            $query .= "     AND EXAM_TYPE = '1' ";
            $query .= "     AND RECEPTNO = '".$examNo."' ";
            $query .= "     AND TESTSUBCLASSCD = '".$subclassCd."' ";
        } else {
            $query .= " SELECT ";
            $query .= "     SCORE, ";
            $query .= "     CAST(NULL AS DECIMAL) STD_SCORE ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_SCORE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '".$model->test_year."' ";
            $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "     AND TESTDIV = '1' ";
            $query .= "     AND EXAM_TYPE = '1' ";
            $query .= "     AND RECEPTNO = '".$examNo."' ";
            $query .= "     AND TESTSUBCLASSCD = '".$subclassCd."' ";
            $query .= "     AND TESTPAPERCD = '".$paperCd."' ";
        }

        return $query;
    }

    function getTotalAvgDev($model, $testSubclassCd) {
        $query .= " SELECT ";
        if ($testSubclassCd != "9") {
            $query .= "     TESTSUBCLASSCD, ";
        }
        $query .= "     DECIMAL(ROUND(STDDEV(SCORE),2), 5, 2) AS STD_SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(DOUBLE(SCORE)) * 10, 0) / 10, 5, 1) AS AVG_SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->test_year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        if ($testSubclassCd != "9") {
            $query .= "     AND TESTSUBCLASSCD = '".$testSubclassCd."' ";
            $query .= " GROUP BY ";
            $query .= "     TESTSUBCLASSCD ";
        }

        return $query;
    }

    function getTotalJuken($model, $examType) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->test_year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= "     AND EXAMINEE_DIV = '".$examType."' ";

        return $query;
    }

}
?>
