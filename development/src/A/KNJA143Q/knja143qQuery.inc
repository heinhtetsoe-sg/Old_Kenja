<?php

require_once('for_php7.php');


class knja143qQuery extends Query {

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }

    //クラス取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "      TR_CD3 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }

    //クラス取得
    function getMajorHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
        $query .= "    AND T3.SEMESTER = T1.SEMESTER AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "      TR_CD3 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.GRADE || T1.HR_CLASS, HR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象データ（身分証発行用）
    function getCsvSql1($model) {
        $selected = "('";
        foreach ($model->select_data["selectdata"] as $d) {
            $selected .= $comma . $d;
            $comma = "', '";
        }
        $selected .= "')";

        $query = "SELECT "
                    . "T2.NAME,"
                    . "T2.BIRTHDAY,"
                    . "T2.ENT_DATE,"
                    . "CASE WHEN NMZ002.NAME1 LIKE '%男%' THEN '0' "
                    . "     WHEN NMZ002.NAME1 LIKE '%女%' THEN '1' "
                    . "END AS MIBUNSHO_SEX_CD,"
                    . "T1.GRADE,"
                    . "T1.HR_CLASS,"
                    . "T1.ATTENDNO,"
                    . "T1.SCHREGNO, "
                    . " CAST(". CTRL_YEAR." + INT(T4.MAX_GRADE) - INT(T1.GRADE) + 1 AS VARCHAR(4)) || '-03-31' AS LIMIT, "
                    . "T1.SCHREGNO AS MIBUNSHO_BANGOU, "
                    . " RIGHT('0000' || CAST(YEAR(ENT_DATE) AS VARCHAR(4)), 2) || '4' AS ENT_MONTH_CD, "
                    . " RIGHT('0000' || CAST(YEAR(BIRTHDAY) AS VARCHAR(4)), 2) "
                    . "  || RIGHT('00' || CAST(MONTH(BIRTHDAY) AS VARCHAR(2)), 2) "
                    . "  || RIGHT('00' || CAST(DAY(BIRTHDAY) AS VARCHAR(2)), 2) AS BIRTHDAYCD  "
                . "FROM SCHREG_REGD_DAT T1 "
                . "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO  "
                . "LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T2.SEX "
                . "LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE "
                . "LEFT JOIN (SELECT YEAR, SCHOOL_KIND, MAX(GRADE) AS MAX_GRADE FROM SCHREG_REGD_GDAT GROUP BY YEAR, SCHOOL_KIND) T4 ON T4.YEAR = T1.YEAR AND T4.SCHOOL_KIND = T3.SCHOOL_KIND "
               . "WHERE "
                    . "T1.YEAR='" .CTRL_YEAR ."' "
                    . "AND T1.SEMESTER='" .CTRL_SEMESTER ."' ";
        if ($model->field["OUTPUT"] == '2') {
            $query .= " AND T1.GRADE || T1.HR_CLASS IN {$selected} ";
            $query .= "ORDER BY "
                        . " T1.COURSECD"
                        . " , T1.MAJORCD"
                        . " , T1.GRADE "
                        . " , T1.HR_CLASS "
                        . " , T1.ATTENDNO ";
        } else {
            $query .= " AND T1.SCHREGNO IN {$selected} ";
            $query .= "ORDER BY "
                        . " T1.GRADE "
                        . " , T1.HR_CLASS "
                        . " , T1.ATTENDNO ";
        }

        return $query;
    }

    //対象データ（身分証発行用 磁気なし）
    function getCsvSql2($model) {
        $selected = "('";
        foreach ($model->select_data["selectdata"] as $d) {
            $selected .= $comma . $d;
            $comma = "', '";
        }
        $selected .= "')";

        $query = "SELECT "
                    . "T2.NAME,"
                    . "T2.BIRTHDAY,"
                    . "T2.ENT_DATE,"
                    . "CASE WHEN NMZ002.NAME1 LIKE '%男%' THEN '0' "
                    . "     WHEN NMZ002.NAME1 LIKE '%女%' THEN '1' "
                    . "END AS MIBUNSHO_SEX_CD,"
                    . "T1.GRADE,"
                    . "T1.HR_CLASS,"
                    . "T1.ATTENDNO,"
                    . "T1.SCHREGNO, "
                    . " CAST(". CTRL_YEAR." + INT(T4.MAX_GRADE) - INT(T1.GRADE) + 1 AS VARCHAR(4)) || '-03-31' AS LIMIT, "
                    . "T1.SCHREGNO AS MIBUNSHO_BANGOU, "
                    . " RIGHT('0000' || CAST(YEAR(ENT_DATE) AS VARCHAR(4)), 2) || '4' AS ENT_MONTH_CD, "
                    . " RIGHT('0000' || CAST(YEAR(BIRTHDAY) AS VARCHAR(4)), 2) "
                    . "  || RIGHT('00' || CAST(MONTH(BIRTHDAY) AS VARCHAR(2)), 2) "
                    . "  || RIGHT('00' || CAST(DAY(BIRTHDAY) AS VARCHAR(2)), 2) AS BIRTHDAYCD  "
                . "FROM SCHREG_REGD_DAT T1 "
                . "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO  "
                . "LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T2.SEX "
                . "LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE "
                . "LEFT JOIN (SELECT YEAR, SCHOOL_KIND, MAX(GRADE) AS MAX_GRADE FROM SCHREG_REGD_GDAT GROUP BY YEAR, SCHOOL_KIND) T4 ON T4.YEAR = T1.YEAR AND T4.SCHOOL_KIND = T3.SCHOOL_KIND "
               . "WHERE "
                    . "T1.YEAR='" .CTRL_YEAR ."' "
                    . "AND T1.SEMESTER='" .CTRL_SEMESTER ."' ";
        if ($model->field["OUTPUT"] == '2') {
            $query .= " AND T1.GRADE || T1.HR_CLASS IN {$selected} ";
            $query .= "ORDER BY "
                        . " T1.COURSECD"
                        . " , T1.MAJORCD"
                        . " , T1.GRADE "
                        . " , T1.HR_CLASS "
                        . " , T1.ATTENDNO ";
        } else {
            $query .= " AND T1.SCHREGNO IN {$selected} ";
            $query .= "ORDER BY "
                        . " T1.GRADE "
                        . " , T1.HR_CLASS "
                        . " , T1.ATTENDNO ";
        }

        return $query;
    }

    //対象データ（食券機連携用）
    function getCsvSql3($model) {
        $selected = "('";
        foreach ($model->select_data["selectdata"] as $d) {
            $selected .= $comma . $d;
            $comma = "', '";
        }
        $selected .= "')";

        $query  = " WITH ADDRESS_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         A1.SCHREGNO, ";
        $query .= "         A1.ZIPCD, ";
        $query .= "         A1.ADDR1, ";
        $query .= "         A1.ADDR2, ";
        $query .= "         A1.TELNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT A1 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_ADDRESS_DAT ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        $query .= "         ) A2 ON  A2.SCHREGNO = A1.SCHREGNO ";
        $query .= "              AND A2.ISSUEDATE = A1.ISSUEDATE ";
        $query .= "     ), ZIPCD_M AS ( ";
        $query .= "     SELECT ";
        $query .= "         Z1.NEW_ZIPCD, ";
        $query .= "         Z1.PREF ";
        $query .= "     FROM ";
        $query .= "         ZIPCD_MST Z1 ";
        $query .= "         INNER JOIN (SELECT ";
        $query .= "                         NEW_ZIPCD, ";
        $query .= "                         MAX(ZIPNO) AS ZIPNO ";
        $query .= "                     FROM ";
        $query .= "                         ZIPCD_MST ";
        $query .= "                     GROUP BY ";
        $query .= "                         NEW_ZIPCD ";
        $query .= "                     ) Z2 ON Z2.ZIPNO = Z1.ZIPNO ";
        $query .= "     ),G_ADDRESS_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         G1.SCHREGNO, ";
        $query .= "         G1.GUARD_ZIPCD, ";
        $query .= "         G1.GUARD_ADDR1, ";
        $query .= "         G1.GUARD_ADDR2, ";
        $query .= "         G1.GUARD_TELNO ";
        $query .= "     FROM ";
        $query .= "         GUARDIAN_ADDRESS_DAT G1 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "         FROM ";
        $query .= "             GUARDIAN_ADDRESS_DAT ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        $query .= "         ) G2 ON  G2.SCHREGNO  = G1.SCHREGNO ";
        $query .= "              AND G2.ISSUEDATE = G1.ISSUEDATE ";
        $query .= "     ), G_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GUARD_NAME, ";
        $query .= "         GUARD_KANA, ";
        $query .= "         RELATIONSHIP ";
        $query .= "     FROM ";
        $query .= "         GUARDIAN_DAT ";
        $query .= "     ), GUARDIAN_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         G1.*, ";
        $query .= "         G2.GUARD_ZIPCD, ";
        $query .= "         G2.GUARD_ADDR1, ";
        $query .= "         G2.GUARD_ADDR2, ";
        $query .= "         G2.GUARD_TELNO ";
        $query .= "     FROM ";
        $query .= "         G_DAT G1 ";
        $query .= "     LEFT JOIN G_ADDRESS_DAT G2 ";
        $query .= "             ON G1.SCHREGNO = G2.SCHREGNO ";
        $query .= "     ), SUNSYO AS ( ";
        $query .= "     SELECT ";
        $query .= "         R1.SCHREGNO, ";
        $query .= "         V1.FINSCHOOL_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT R1 ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT H1 ";
        $query .= "                  ON H1.SCHREGNO    = R1.SCHREGNO ";
        $query .= "                 AND H1.SCHOOL_KIND = 'J' ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST V1 ";
        $query .= "                  ON V1.YEAR        = R1.YEAR ";
        $query .= "                 AND V1.FINSCHOOLCD = H1.FINSCHOOLCD ";
        $query .= "     WHERE ";
        $query .= "             R1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND R1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["OUTPUT"] == '2') {
            $query .= "     AND R1.GRADE || R1.HR_CLASS IN {$selected} ";
        } else {
            $query .= "     AND R1.SCHREGNO IN {$selected} ";
        }
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     R1.GRADE, ";
        $query .= "     D1.BASE_REMARK1, ";
        $query .= "     B1.SCHREGNO, ";
        $query .= "     G1.GRADE_CD, ";
        $query .= "     H1.HR_CLASS_NAME1, ";
        $query .= "     R1.ATTENDNO, ";
        $query .= "     VC1.COURSECODENAME, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     CASE WHEN B1.SEX = '1' THEN '男' ";
        $query .= "          WHEN B1.SEX = '2' THEN '女' END AS SEX, ";
        $query .= "     B1.BIRTHDAY, ";
        $query .= "     Z1.PREF, ";
        $query .= "     B1.FINSCHOOLCD, ";
        $query .= "     V_FM.FINSCHOOL_NAME, ";
        $query .= "     A1.ZIPCD, ";
        $query .= "     A1.ADDR1, ";
        $query .= "     A1.ADDR2, ";
        $query .= "     A1.TELNO, ";
        $query .= "     G2.GUARD_ZIPCD, ";
        $query .= "     G2.GUARD_ADDR1, ";
        $query .= "     G2.GUARD_ADDR2, ";
        $query .= "     G2.GUARD_TELNO, ";
        $query .= "     G2.GUARD_NAME, ";
        $query .= "     G2.GUARD_KANA, ";
        $query .= "     N4.NAME1 AS RELATIONSHIP, ";
        $query .= "     N1.NAME1 AS RYO, ";
        $query .= "     D1.BASE_REMARK3 AS RYONO, ";
        $query .= "     N2.NAME1 AS TOKUTAI, ";
        $query .= "     N3.NAME1 AS SUKARA, ";
        $query .= "     CASE WHEN (B1.ENT_DIV = '1' OR B1.ENT_DIV = '2' OR B1.ENT_DIV = '3') THEN B1.ENT_DATE END AS NYUGAKU, ";
        $query .= "     CASE WHEN B1.GRD_DIV = '1' THEN B1.GRD_DATE END AS SOTUGYO, ";
        $query .= "     CASE WHEN B1.ENT_DIV = '4' THEN B1.ENT_DATE END AS TENNYU, ";
        $query .= "     CASE WHEN B1.GRD_DIV = '3' THEN B1.GRD_DATE END AS TENGAKU, ";
        $query .= "     CASE WHEN B1.GRD_DIV = '2' THEN B1.GRD_DATE END AS TAIGAKU, ";
        $query .= "     CASE WHEN B1.ENT_DIV = '4' THEN B1.ENT_SCHOOL END AS ENT_SCHOOL, ";
        $query .= "     CASE WHEN B1.GRD_DIV = '3' THEN B1.GRD_SCHOOL END AS GRD_SCHOOL, ";
        $query .= "     B1.REMARK1 || B1.REMARK2 || B1.REMARK3 AS BIKOU, ";
        $query .= "     SUN1.FINSCHOOL_NAME AS SUNSYO, ";
        $query .= "     CASE WHEN G1.SCHOOL_KIND = 'P' THEN '10' ";
        $query .= "          WHEN G1.SCHOOL_KIND = 'J' THEN '20' ";
        $query .= "          WHEN G1.SCHOOL_KIND = 'H' AND R1.MAJORCD = '100' THEN '30' ";
        $query .= "          WHEN G1.SCHOOL_KIND = 'H' AND R1.MAJORCD = '803' THEN '30' ";
        $query .= "          END AS SUBID, ";
        $query .= "     R1.GRADE, ";
        $query .= "     CASE WHEN B1.GRD_DIV IS NULL THEN '1' ELSE '0' END AS ZAISEKI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT R1 ";
        $query .= " INNER JOIN SCHREG_BASE_MST B1 ";
        $query .= "              ON R1.SCHREGNO = B1.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_BASE_DETAIL_MST D1 ";
        $query .= "              ON R1.SCHREGNO = D1.SCHREGNO ";
        $query .= "             AND D1.BASE_SEQ = '003' ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "              ON G1.YEAR  = R1.YEAR ";
        $query .= "             AND G1.GRADE = R1.GRADE ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT H1 ";
        $query .= "              ON H1.YEAR     = R1.YEAR ";
        $query .= "             AND H1.SEMESTER = R1.SEMESTER ";
        $query .= "             AND H1.GRADE    = R1.GRADE ";
        $query .= "             AND H1.HR_CLASS = R1.HR_CLASS ";
        $query .= " LEFT JOIN V_COURSECODE_MST VC1 ";
        $query .= "              ON VC1.YEAR       = R1.YEAR ";
        $query .= "             AND VC1.COURSECODE = R1.COURSECODE  ";
        $query .= " LEFT JOIN V_FINSCHOOL_MST V_FM ";
        $query .= "              ON V_FM.YEAR        = R1.YEAR ";
        $query .= "             AND V_FM.FINSCHOOLCD = B1.FINSCHOOLCD ";
        $query .= " LEFT JOIN ADDRESS_DAT A1 ";
        $query .= "              ON A1.SCHREGNO = R1.SCHREGNO ";
        $query .= " LEFT JOIN ZIPCD_M Z1 ";
        $query .= "              ON Z1.NEW_ZIPCD = A1.ZIPCD ";
        $query .= " LEFT JOIN GUARDIAN_DATA G2 ";
        $query .= "              ON G2.SCHREGNO = R1.SCHREGNO ";
        $query .= " LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR    = H1.YEAR ";
        $query .= "             AND N1.NAMECD1 = 'L042' ";
        $query .= "             AND N1.NAMECD2 = D1.BASE_REMARK2 ";
        $query .= " LEFT JOIN V_NAME_MST N2 ";
        $query .= "              ON N2.YEAR    = H1.YEAR ";
        $query .= "             AND N2.NAMECD1 = 'L025' ";
        $query .= "             AND N2.NAMECD2 = D1.BASE_REMARK4 ";
        $query .= " LEFT JOIN V_NAME_MST N3 ";
        $query .= "              ON N3.YEAR    = H1.YEAR ";
        $query .= "             AND N3.NAMECD1 = 'L043' ";
        $query .= "             AND N3.NAMECD2 = D1.BASE_REMARK5 ";
        $query .= " LEFT JOIN V_NAME_MST N4 ";
        $query .= "              ON N4.YEAR    = H1.YEAR ";
        $query .= "             AND N4.NAMECD1 = 'H201' ";
        $query .= "             AND N4.NAMECD2 = G2.RELATIONSHIP ";
        $query .= " LEFT JOIN SUNSYO SUN1 ";
        $query .= "              ON SUN1.SCHREGNO = R1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         R1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND R1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["OUTPUT"] == '2') {
            $query .= " AND R1.GRADE || R1.HR_CLASS IN {$selected} ";
            $query .= "ORDER BY "
                        . " R1.COURSECD"
                        . " , R1.MAJORCD"
                        . " , R1.GRADE "
                        . " , R1.HR_CLASS "
                        . " , R1.ATTENDNO ";
        } else {
            $query .= " AND R1.SCHREGNO IN {$selected} ";
            $query .= "ORDER BY "
                        . " R1.GRADE "
                        . " , R1.HR_CLASS "
                        . " , R1.ATTENDNO ";
        }

        return $query;
    }

}
?>
