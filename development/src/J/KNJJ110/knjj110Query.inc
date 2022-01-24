<?php

require_once('for_php7.php');


class knjj110Query extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    function getIinkai($model) {
        $query  = " SELECT ";
        $query .= "     D.COMMITTEE_FLG AS CFLG, ";
        $query .= "     D.COMMITTEECD AS CCD, ";
        $query .= "     M.COMMITTEENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COMMITTEE_YDAT D ";
        $query .= "     INNER JOIN COMMITTEE_MST M ON D.COMMITTEE_FLG = M.COMMITTEE_FLG ";
        $query .= "           AND D.COMMITTEECD = M.COMMITTEECD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND M.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND M.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND M.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND M.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE D.YEAR = '" .CTRL_YEAR ."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND D.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND D.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND D.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND D.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CFLG || CCD ";

        return $query;
    }

    //学期コンボ取得
    function getJ004()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'J004' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ-A作成
    function selectCsvQueryA($model, $committeecd) {

        $query .= " WITH SCHINFO AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAMEABBV, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.COMMITTEE_FLG, ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     C1.COMMITTEENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN COMMITTEE_MST C1 ON T1.COMMITTEE_FLG = C1.COMMITTEE_FLG AND T1.COMMITTEECD = C1.COMMITTEECD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND C1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND C1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND C1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND C1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ,SCHINFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.COMMITTEE_FLG || T1.COMMITTEECD = '".$committeecd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["J004"] != "ALL") {
            $query .= "     AND T1.SEMESTER = '{$model->field["J004"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COMMITTEE_FLG, ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //ＣＳＶ-B作成
    function selectCsvQueryB($model) {

        $query .= " WITH SCHINFO AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        if ($model->field["PATTERN2_PRINT_BIRTHDAY"] == "1") {
            $query .= "     T2.BIRTHDAY, ";
        } else {
            $query .= "     '' AS BIRTHDAY, ";
        }
        $query .= "     ADDR.ADDR1, ";
        $query .= "     ADDR.ADDR2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        $query .= "     T2.SEX ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T2.FINSCHOOLCD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    T1.SCHREGNO, ";
        $query .= "                    T1.ADDR1, ";
        $query .= "                    T1.ADDR2, ";
        $query .= "                    T1.TELNO ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_ADDRESS_DAT T1, ";
        $query .= "                    (SELECT ";
        $query .= "                          SCHREGNO, ";
        $query .= "                          MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "                        FROM ";
        $query .= "                          SCHREG_ADDRESS_DAT ";
        $query .= "                        GROUP BY ";
        $query .= "                          SCHREGNO ";
        $query .= "                    ) T2 ";
        $query .= "                WHERE  ";
        $query .= "                    T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                    AND T1.ISSUEDATE = T2.ISSUEDATE ";
        $query .= "         ) ADDR ON T1.SCHREGNO = ADDR.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     C1.COMMITTEENAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAMEABBV, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     N1.NAME1 AS SEX, ";
        $query .= "     VALUE(T2.ADDR1,'') || VALUE(T2.ADDR2,'') AS ADDR, ";
        $query .= "     T2.TELNO, ";
        $query .= "     G1.GUARD_NAME, ";
        $query .= "     G1.GUARD_KANA, ";
        $query .= "     T2.FINSCHOOL_NAME, ";
        $query .= "     T1.COMMITTEE_FLG, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN COMMITTEE_MST C1 ON T1.COMMITTEE_FLG = C1.COMMITTEE_FLG AND T1.COMMITTEECD = C1.COMMITTEECD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND C1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND C1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND C1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND C1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN GUARDIAN_DAT G1 ON T1.SCHREGNO = G1.SCHREGNO, ";
        $query .= "     SCHINFO T2 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T2.SEX = N1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.COMMITTEE_FLG || T1.COMMITTEECD IN ('".implode("','", explode(',', $model->selectdata))."') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["J004"] != "ALL") {
            $query .= "     AND T1.SEMESTER = '{$model->field["J004"]}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     C1.COMMITTEENAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAMEABBV, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     N1.NAME1, ";
        $query .= "     VALUE(T2.ADDR1,'') || VALUE(T2.ADDR2,''), ";
        $query .= "     T2.TELNO, ";
        $query .= "     G1.GUARD_NAME, ";
        $query .= "     G1.GUARD_KANA, ";
        $query .= "     T2.FINSCHOOL_NAME, ";
        $query .= "     T1.COMMITTEE_FLG, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.COMMITTEE_FLG, ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }
}
?>
