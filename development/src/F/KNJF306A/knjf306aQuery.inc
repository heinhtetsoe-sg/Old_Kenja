<?php

require_once('for_php7.php');

class knjf306aQuery extends Query {
    //教育委員会チェック
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }

    //年度
    function getYear() {
        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS VALUE, ";
        $query .= "     '".CTRL_YEAR."' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象学校
    function getSchoolData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN L1.ANSWER_FLG = '1' ";
        $query .= "          THEN '【回済】' ";
        $query .= "          ELSE '　　　　' ";
        $query .= "     END AS FLG_NAME, ";
        $query .= "     '0' AS FLG, ";
        $query .= "     L1.EDBOARD_SCHOOLCD, ";
        $query .= "     L2.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT L1 ";
        $query .= "              ON L1.YEAR         = T1.YEAR ";
        $query .= "             AND L1.DOC_NUMBER   = T1.DOC_NUMBER ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L2 ";
        $query .= "              ON L2.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "     T1.REQUEST_ANSWER_PRG = 'KNJF306'  ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '【未発】' AS FLG_NAME, ";
        $query .= "     '1' AS FLG, ";
        $query .= "     L1.EDBOARD_SCHOOLCD, ";
        $query .= "     L2.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT L1 ";
        $query .= "              ON L1.YEAR         = T1.YEAR  ";
        $query .= "             AND L1.DOC_NUMBER   = T1.DOC_NUMBER  ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L2 ";
        $query .= "              ON L2.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "     L1.EDBOARD_SCHOOLCD NOT IN ( ";
        $query .= "                                 SELECT DISTINCT ";
        $query .= "                                     N2.EDBOARD_SCHOOLCD ";
        $query .= "                                 FROM ";
        $query .= "                                     AFT_SEARCH_REPORT_DAT N1 ";
        $query .= "                                     INNER JOIN AFT_SEARCH_REPORT_SCHOOL_DAT N2 ";
        $query .= "                                          ON N1.YEAR         = N2.YEAR ";
        $query .= "                                         AND N1.DOC_NUMBER   = N2.DOC_NUMBER ";
        $query .= "                                 WHERE ";
        $query .= "                                     N1.YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "                                     N1.REQUEST_ANSWER_PRG = 'KNJF306' ";
        $query .= "                                 ) ";
        $query .= " ORDER BY ";
        $query .= "     EDBOARD_SCHOOLCD ";

        return $query;
    }

    //学校名取得
    function getEdboardSchoolName($schoolCd) {
        $query  = " SELECT ";
        $query .= "     EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '{$schoolCd}' ";

        return $query;
    }

    //年齢取得
    function getAge($model, $schoolCd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     AGE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION306_FIXED_DAT ";
        $query .= " WHERE ";
        if ($schoolCd == "ALL") {
            $query .= "     EDBOARD_SCHOOLCD IN ('".implode("','", $model->edboard_schoolcd)."') ";
        } else {
            $query .= "     EDBOARD_SCHOOLCD = '{$schoolCd}' ";
        }
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     AGE ";

        return $query;
    }

    //CSV出力データ取得
    function selectCsvQuery($model, $dataDiv, $schoolCd) {

        $query  = " WITH MAX_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EDBOARD_SCHOOLCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.FIXED_DATE ";
        $query .= "     FROM ";
        $query .= "         REPORT_MEDEXAM_DISEASE_ADDITION306_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR, ";
        $query .= "             MAX(EXECUTE_DATE) AS EXECUTE_DATE ";
        $query .= "         FROM ";
        $query .= "             REPORT_MEDEXAM_DISEASE_ADDITION306_DAT ";
        $query .= "         WHERE ";
        if ($schoolCd == "ALL") {
            $query .= "             EDBOARD_SCHOOLCD IN ('".implode("','", $model->edboard_schoolcd)."') AND ";
        } else {
            $query .= "             EDBOARD_SCHOOLCD = '{$schoolCd}' AND ";
        }
        $query .= "             YEAR = '".$model->field["YEAR"]."' ";
        $query .= "         GROUP BY ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD AND ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.EXECUTE_DATE = T2.EXECUTE_DATE ";
        $query .= " ), AGEDATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         MD1.YEAR, ";
        $query .= "         MD1.DATA_DIV, ";
        $query .= "         MD1.COURSECD, ";
        $query .= "         MD1.MAJORCD, ";
        $query .= "         MD1.AGE, ";
        $query .= "         MD1.SEX, ";
        $query .= "         SUM(MD1.INT_VAL) AS INT_VAL1, ";
        $query .= "         SUM(MD2.INT_VAL) AS INT_VAL2, ";
        $query .= "         SUM(MD2.INT_VAL) * 100 / SUM(MD1.INT_VAL) AS INT_VAL3 ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DISEASE_ADDITION306_FIXED_DAT MD1 ";
        $query .= "     INNER JOIN MEDEXAM_DISEASE_ADDITION306_FIXED_DAT MD2 ";
        $query .= "                                     ON MD1.EDBOARD_SCHOOLCD = MD2.EDBOARD_SCHOOLCD ";
        $query .= "                                    AND MD1.YEAR       = MD2.YEAR ";
        $query .= "                                    AND MD1.DATA_DIV   = MD2.DATA_DIV ";
        $query .= "                                    AND MD1.FIXED_DATE = MD2.FIXED_DATE ";
        $query .= "                                    AND MD1.COURSECD   = MD2.COURSECD ";
        $query .= "                                    AND MD1.MAJORCD    = MD2.MAJORCD ";
        $query .= "                                    AND MD1.AGE        = MD2.AGE ";
        $query .= "                                    AND MD1.SEX        = MD2.SEX ";
        $query .= "                                    AND MD2.SEQ        = '02' ";
        $query .= "     INNER JOIN MEDEXAM_DISEASE_ADDITION306_FIXED_DAT MD3 ";
        $query .= "                                     ON MD1.EDBOARD_SCHOOLCD = MD3.EDBOARD_SCHOOLCD ";
        $query .= "                                    AND MD1.YEAR       = MD3.YEAR ";
        $query .= "                                    AND MD1.DATA_DIV   = MD3.DATA_DIV ";
        $query .= "                                    AND MD1.FIXED_DATE = MD3.FIXED_DATE ";
        $query .= "                                    AND MD1.COURSECD   = MD3.COURSECD ";
        $query .= "                                    AND MD1.MAJORCD    = MD3.MAJORCD ";
        $query .= "                                    AND MD1.AGE        = MD3.AGE ";
        $query .= "                                    AND MD1.SEX        = MD3.SEX ";
        $query .= "                                    AND MD3.SEQ        = '03' ";
        $query .= "     INNER JOIN MAX_DATE MXD ";
        $query .= "                  ON MD1.EDBOARD_SCHOOLCD = MXD.EDBOARD_SCHOOLCD ";
        $query .= "                 AND MD1.YEAR             = MXD.YEAR ";
        $query .= "                 AND MD1.FIXED_DATE       = MXD.FIXED_DATE ";
        $query .= "     WHERE ";
        $query .= "             MD1.YEAR         = '".$model->field["YEAR"]."' ";
        $query .= "         AND MD1.DATA_DIV     = '{$dataDiv}' ";
        $query .= "         AND MD1.SEQ          = '01' ";
        $query .= "     GROUP BY ";
        $query .= "         MD1.YEAR, ";
        $query .= "         MD1.DATA_DIV, ";
        $query .= "         MD1.COURSECD, ";
        $query .= "         MD1.MAJORCD, ";
        $query .= "         MD1.AGE, ";
        $query .= "         MD1.SEX ";
        $query .= "     ORDER BY ";
        $query .= "         MD1.COURSECD, ";
        $query .= "         MD1.MAJORCD, ";
        $query .= "         MD1.SEX ";
        $query .= "  ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     V_CM.COURSENAME || V_CM.MAJORNAME ";
        $query .= "     ,VN1.NAME1 ";     //性別
        foreach ($model->age as $ageVal) {
            $query .= "     ,CASE WHEN A{$ageVal}.INT_VAL1 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL1 END ";  //受験者数
            $query .= "     ,CASE WHEN A{$ageVal}.INT_VAL2 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL2 END ";  //該当者数
            $query .= "     ,CAST(ROUND(CASE WHEN A{$ageVal}.INT_VAL3 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL3 END, 2) AS DECIMAL(10,2)) ";  //出現率（％）
        }
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         MD1.YEAR, ";
        $query .= "         MD1.DATA_DIV, ";
        $query .= "         MD1.COURSECD, ";
        $query .= "         MD1.MAJORCD, ";
        $query .= "         MD1.SEX ";
        $query .= "      FROM ";
        $query .= "         MEDEXAM_DISEASE_ADDITION306_FIXED_DAT MD1 ";
        $query .= "         INNER JOIN MAX_DATE MXD ";
        $query .= "                  ON MD1.EDBOARD_SCHOOLCD = MXD.EDBOARD_SCHOOLCD ";
        $query .= "                 AND MD1.YEAR             = MXD.YEAR ";
        $query .= "                 AND MD1.FIXED_DATE       = MXD.FIXED_DATE ";
        $query .= "      ) MD1 ";
        foreach ($model->age as $ageVal) {
            $query .= " LEFT JOIN AGEDATA A{$ageVal} ON MD1.YEAR       = A{$ageVal}.YEAR ";
            $query .= "                             AND MD1.DATA_DIV   = A{$ageVal}.DATA_DIV ";
            $query .= "                             AND MD1.COURSECD   = A{$ageVal}.COURSECD ";
            $query .= "                             AND MD1.MAJORCD    = A{$ageVal}.MAJORCD ";
            $query .= "                             AND A{$ageVal}.AGE = {$ageVal} ";
            $query .= "                             AND MD1.SEX        = A{$ageVal}.SEX ";
        }
        $query .= " JOIN V_COURSE_MAJOR_MST V_CM ON MD1.YEAR     = V_CM.YEAR ";
        $query .= "                             AND MD1.COURSECD = V_CM.COURSECD ";
        $query .= "                             AND MD1.MAJORCD  = V_CM.MAJORCD ";
        $query .= " JOIN V_NAME_MST VN1 ON MD1.YEAR    = VN1.YEAR ";
        $query .= "                    AND VN1.NAMECD1 = 'Z002' ";
        $query .= "                    AND MD1.SEX     = VN1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         MD1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "     AND MD1.DATA_DIV = '{$dataDiv}' ";
        $query .= "     AND MD1.SEX      IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     MD1.COURSECD, ";
        $query .= "     MD1.MAJORCD, ";
        $query .= "     MD1.SEX ";

        return $query;
    }

    //CSV合計データ
    function selectCsvQuery2($model, $dataDiv, $schoolCd) {

        $query  = " WITH MAX_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EDBOARD_SCHOOLCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.FIXED_DATE ";
        $query .= "     FROM ";
        $query .= "         REPORT_MEDEXAM_DISEASE_ADDITION306_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR, ";
        $query .= "             MAX(EXECUTE_DATE) AS EXECUTE_DATE ";
        $query .= "         FROM ";
        $query .= "             REPORT_MEDEXAM_DISEASE_ADDITION306_DAT ";
        $query .= "         WHERE ";
        if ($schoolCd == "ALL") {
            $query .= "             EDBOARD_SCHOOLCD IN ('".implode("','", $model->edboard_schoolcd)."') AND ";
        } else {
            $query .= "             EDBOARD_SCHOOLCD = '{$schoolCd}' AND ";
        }
        $query .= "             YEAR = '".$model->field["YEAR"]."' ";
        $query .= "         GROUP BY ";
        $query .= "             EDBOARD_SCHOOLCD, ";
        $query .= "             YEAR ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD AND ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.EXECUTE_DATE = T2.EXECUTE_DATE ";
        $query .= " ), AGEDATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         MD1.YEAR, ";
        $query .= "         MD1.DATA_DIV, ";
        $query .= "         MD1.AGE, ";
        $query .= "         SUM(MD1.INT_VAL) AS SUM1, ";
        $query .= "         SUM(MD2.INT_VAL) AS SUM2, ";
        $query .= "         SUM(MD3.INT_VAL) AS SUM3 ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DISEASE_ADDITION306_FIXED_DAT MD1 ";
        $query .= "     INNER JOIN MEDEXAM_DISEASE_ADDITION306_FIXED_DAT MD2 ";
        $query .= "                                     ON MD1.EDBOARD_SCHOOLCD = MD2.EDBOARD_SCHOOLCD ";
        $query .= "                                    AND MD1.YEAR       = MD2.YEAR ";
        $query .= "                                    AND MD1.DATA_DIV   = MD2.DATA_DIV ";
        $query .= "                                    AND MD1.FIXED_DATE = MD2.FIXED_DATE ";
        $query .= "                                    AND MD1.COURSECD   = MD2.COURSECD ";
        $query .= "                                    AND MD1.MAJORCD    = MD2.MAJORCD ";
        $query .= "                                    AND MD1.AGE        = MD2.AGE ";
        $query .= "                                    AND MD1.SEX        = MD2.SEX ";
        $query .= "                                    AND MD2.SEQ        = '02' ";
        $query .= "     INNER JOIN MEDEXAM_DISEASE_ADDITION306_FIXED_DAT MD3 ";
        $query .= "                                     ON MD1.EDBOARD_SCHOOLCD = MD3.EDBOARD_SCHOOLCD ";
        $query .= "                                    AND MD1.YEAR       = MD3.YEAR ";
        $query .= "                                    AND MD1.DATA_DIV   = MD3.DATA_DIV ";
        $query .= "                                    AND MD1.FIXED_DATE = MD3.FIXED_DATE ";
        $query .= "                                    AND MD1.COURSECD   = MD3.COURSECD ";
        $query .= "                                    AND MD1.MAJORCD    = MD3.MAJORCD ";
        $query .= "                                    AND MD1.AGE        = MD3.AGE ";
        $query .= "                                    AND MD1.SEX        = MD3.SEX ";
        $query .= "                                    AND MD3.SEQ        = '03' ";
        $query .= "     INNER JOIN MAX_DATE MXD ";
        $query .= "                  ON MD1.EDBOARD_SCHOOLCD = MXD.EDBOARD_SCHOOLCD ";
        $query .= "                 AND MD1.YEAR             = MXD.YEAR ";
        $query .= "                 AND MD1.FIXED_DATE       = MXD.FIXED_DATE ";
        $query .= "     WHERE ";
        $query .= "             MD1.YEAR         = '".$model->field["YEAR"]."' ";
        $query .= "         AND MD1.DATA_DIV     = '{$dataDiv}' ";
        $query .= "         AND MD1.SEQ          = '01' ";
        $query .= "     GROUP BY ";
        $query .= "         MD1.YEAR, ";
        $query .= "         MD1.DATA_DIV, ";
        $query .= "         MD1.AGE ";
        $query .= "  ) ";
        //メイン（合計）
        $query .= " SELECT ";
        $query .= "     '全体' ";
        $query .= "     ,'全体' ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,CASE WHEN SUM(A{$ageVal}.SUM1) IS NULL THEN 0 ELSE SUM(A{$ageVal}.SUM1) END ";  //受験者数
            $query .= "     ,CASE WHEN SUM(A{$ageVal}.SUM2) IS NULL THEN 0 ELSE SUM(A{$ageVal}.SUM2) END ";  //該当者数
            $query .= "     ,CAST(ROUND(CASE WHEN SUM(A{$ageVal}.SUM2) * 100 / SUM(A{$ageVal}.SUM1) IS NULL THEN 0 ELSE SUM(A{$ageVal}.SUM2) * 100 / SUM(A{$ageVal}.SUM1) END, 2) AS DECIMAL(10,2)) ";  //出現率（％）
        }
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         MD1.YEAR, ";
        $query .= "         MD1.DATA_DIV ";
        $query .= "      FROM ";
        $query .= "         MEDEXAM_DISEASE_ADDITION306_FIXED_DAT MD1 ";
        $query .= "         INNER JOIN MAX_DATE MXD ";
        $query .= "                  ON MD1.EDBOARD_SCHOOLCD = MXD.EDBOARD_SCHOOLCD ";
        $query .= "                 AND MD1.YEAR             = MXD.YEAR ";
        $query .= "                 AND MD1.FIXED_DATE       = MXD.FIXED_DATE ";
        $query .= "      ) MD1 ";
        foreach ($model->age as $ageVal) {
            $query .= " LEFT JOIN AGEDATA A{$ageVal}  ON MD1.YEAR       = A{$ageVal}.YEAR ";
            $query .= "                              AND MD1.DATA_DIV   = A{$ageVal}.DATA_DIV ";
            $query .= "                              AND A{$ageVal}.AGE = {$ageVal} ";
        }
        $query .= " WHERE ";
        $query .= "         MD1.YEAR         = '".$model->field["YEAR"]."' ";
        $query .= "     AND MD1.DATA_DIV     = '{$dataDiv}' ";

        return $query;
    }
}
?>
