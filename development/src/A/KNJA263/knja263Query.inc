<?php

require_once('for_php7.php');

class knja263Query extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA263' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //学年取得
    function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.OLD_GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR      = '".$model->examyear."' AND ";
        $query .= "     T2.YEAR      = '".CTRL_YEAR."' AND ";
        $query .= "     T1.OLD_GRADE = T2.GRADE AND ";
        $query .= "     VALUE(REMAINGRADE_FLG, '0') != '1' AND ";
        $query .= "     VALUE(OLD_GRADE, '0') != '0' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        if (CTRL_SEMESTER == $model->maxSemester) {
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " UNION   ";
                $query .= " SELECT DISTINCT ";
                $query .= "   T1.GRADE AS VALUE, ";
                $query .= "   T3.GRADE_NAME1 AS LABEL ";
                $query .= " FROM ";
                $query .= "   SCHREG_REGD_HDAT T1 ";
                $query .= "   INNER JOIN SCHREG_REGD_GDAT T3 ON ";
                $query .= "       T3.YEAR     = T1.YEAR AND ";
                $query .= "       T3.GRADE = T1.GRADE ";
                $query .= "   INNER JOIN NAME_MST A023 ON ";
                $query .= "       A023.NAMECD1 = 'A023' AND ";
                $query .= "       A023.NAME1 = T3.SCHOOL_KIND AND ";
                $query .= "       A023.NAME3 = T3.GRADE ";
                $query .= "   INNER JOIN NAME_MST A023_AFT ON ";
                $query .= "       A023_AFT.NAMECD1 = 'A023' AND ";
                $query .= "       (INT(A023_AFT.NAME2) = INT(A023.NAME3) + 1 ";
                $query .= "     OR INT(A023_AFT.ABBV2) = INT(A023.ABBV2) + 1) ";
                $query .= " WHERE ";
                $query .= "   T1.YEAR     = '" .CTRL_YEAR ."' AND ";
                $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER ."' ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND A023_AFT.NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND A023_AFT.NAME1 = '".SCHOOLKIND."' ";
                }
            }
        }

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校区分取得 0：学年制 1：単位制
    function getSchoolDiv()
    {
        $query  = "SELECT ";
        $query .= "    SCHOOLDIV ";
        $query .= "FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //実行
    function selectCvsQuery($model)
    {
        $query  = " WITH BASE_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T2.NAME, ";
        $query .= "         T2.NAME_KANA, ";
        $query .= "         T2.SEX, ";
        if (CTRL_SEMESTER == $model->maxSemester) {
            $query .= "         T1.GRADE, ";
            $query .= "         T1.HR_CLASS, ";
            $query .= "         T1.ATTENDNO, ";
        } else {
            $query .= "         T1.OLD_GRADE AS GRADE, ";
            $query .= "         T1.OLD_HR_CLASS AS HR_CLASS, ";
            $query .= "         T1.OLD_ATTENDNO AS ATTENDNO, ";
        }
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.REMAINGRADE_FLG, ";
        $query .= "         T1.OLD_SCHREGNO, ";
        $query .= "         T1.OLD_GRADE, ";
        $query .= "         T1.OLD_HR_CLASS, ";
        $query .= "         T1.OLD_ATTENDNO ";
        $query .= "     FROM ";
        $query .= "         CLASS_FORMATION_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$model->examyear."' AND ";
        $query .= "         T1.SEMESTER = '1' AND ";
        $query .= "         T1.OLD_GRADE = '".$model->grade."' AND ";
        $query .= "         T1.REMAINGRADE_FLG <> '1' AND ";
        $query .= "         T2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "         (T2.GRD_DIV is null OR T2.GRD_DIV = '') ";
        $query .= " ) , RECORD AS ( ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         AVG(AVG) AS AVG ";
            $query .= "     FROM ";
            $query .= "         RECORD_RANK_SDIV_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "         SEMESTER    = '9' AND ";
            $query .= "         TESTKINDCD  = '99' AND ";
            $query .= "         TESTITEMCD  = '00' AND ";
            $query .= "         SCORE_DIV   = '09' AND ";
            $query .= "         SUBCLASSCD  = '999999' ";
            $query .= "     GROUP BY ";
            $query .= "         SCHREGNO ";
        } else if ($model->recordTableDiv == "1") {
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         AVG(AVG) AS AVG ";
            $query .= "     FROM ";
            $query .= "         RECORD_RANK_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "         SEMESTER    = '9' AND ";
            $query .= "         TESTKINDCD  = '99' AND ";
            $query .= "         TESTITEMCD  = '00' AND ";
            $query .= "         SUBCLASSCD  = '999999' ";
            $query .= "     GROUP BY ";
            $query .= "         SCHREGNO ";
        } else {
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         GRAD_VALUE ";
            $query .= "     FROM ";
            $query .= "         RECORD_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '".CTRL_YEAR."' AND ";
            $query .= "         GRAD_VALUE IS NOT NULL AND ";
            $query .= "         SUBCLASSCD NOT IN ( SELECT DISTINCT ";
            $query .= "                                 ATTEND_SUBCLASSCD ";
            $query .= "                             FROM ";
            $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                             WHERE ";
            $query .= "                                 REPLACECD = '1' AND ";
            $query .= "                                 YEAR = '".CTRL_YEAR."' ";
            $query .= "                           ) ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        
        $query .= "     T1.SEMESTER, ";
        
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        
        $query .= "     T1.REMAINGRADE_FLG, ";
        $query .= "     T1.OLD_SCHREGNO, ";
        $query .= "     T1.OLD_GRADE, ";
        $query .= "     T1.OLD_HR_CLASS, ";
        $query .= "     T1.OLD_ATTENDNO , ";
        
        if ($model->recordTableDiv == "1" || $model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     MAX(DECIMAL(ROUND(FLOAT(L1.AVG)*10,0)/10,5,1)) AS LEVEL_AVG, ";
        } else {
            $query .= "     SUM(VALUE(L1.GRAD_VALUE, 0)) AS LEVEL_SUM, ";
            $query .= "     COUNT(*) AS LEVEL_CNT, ";
        }
        $query .= "     '' AS SEM_SUM, ";
        $query .= "     '' AS SEM_CNT ";
        $query .= " FROM ";
        $query .= "     BASE_T T1 ";
        $query .= "     LEFT JOIN RECORD L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.REMAINGRADE_FLG, ";
        $query .= "     T1.OLD_SCHREGNO, ";
        $query .= "     T1.OLD_GRADE, ";
        $query .= "     T1.OLD_HR_CLASS, ";
        $query .= "     T1.OLD_ATTENDNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
    function getNameMst() {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1, ";
        $query .= "    NAMESPARE1, ";
        $query .= "    NAMESPARE2 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z010' AND ";
        $query .= "    NAMECD2='00' ";
        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $rtnRow;
    }

    //学年名称取得
    function getGradeName($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->examyear."' AND ";
        $query .= "     GRADE   = '".sprintf("%02d", ((int)$model->grade+1))."' ";

        return $query;
    }
}
?>
