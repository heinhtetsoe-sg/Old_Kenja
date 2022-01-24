<?php

require_once('for_php7.php');

class knjh132Query extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //クラス取得
    function getAuthClass($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "    HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"] ."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "    AND (TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "         TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "         TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= "ORDER BY";
        $query .= "    VALUE ";

        return $query;
    }

    //生徒取得
    function getAuthStudent($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.SCHREGNO AS VALUE, ";
        $query .= "    T1.SCHREGNO || '　' || T1.ATTENDNO ||  '番　' || L1.NAME_SHOW AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.SEMESTER = '".$model->field["SEMESTER"] ."' AND ";
        $query .= "    T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"] ."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.ATTENDNO ";

        return $query;
    }

    //CSV出力データ取得
    function getCsvQuery($model) {
        $sdate = str_replace("/","-",$model->control["学期開始日付"][$model->field["SEMESTER"]]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][$model->field["SEMESTER"]]);

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     L5.STAFFNAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     VALUE(T2.NAME_KANA,'') AS NAME_KANA, ";
        $query .= "     VALUE(CHAR(T2.BIRTHDAY),'') AS BIRTHDAY, ";
        $query .= "     VALUE(L4.ZIPCD,'') AS ZIPCD, ";
        if ($model->z010Name1 == 'sundaikoufu' || $model->z010Name1 == 'mieken') {
            $query .= "     VALUE(L4.ADDR1,'') || VALUE(L4.ADDR2,'') AS ADDR1, ";
        } elseif ($model->z010Name1 == 'bunkyo') {
            $query .= "     VALUE(L4.ADDR1,'') AS ADDR1, ";
            $query .= "     VALUE(L4.ADDR2,'') AS ADDR2, ";
        } else {
            $query .= "     VALUE(L4.ADDR1,'') AS ADDR1, ";
        }
        $query .= "     VALUE(L4.TELNO,'') AS TEL, ";
        $query .= "     VALUE(L1.GUARD_NAME,'') AS GUARD_NAME, ";
        $query .= "     VALUE(T2.EMERGENCYTELNO,'') AS E_TEL, ";
        $query .= "     CASE WHEN GDAT.SCHOOL_KIND = 'P' THEN VALUE(DET002.BASE_REMARK1, L2.FINSCHOOL_NAME,'') ELSE VALUE(L2.FINSCHOOL_NAME,'') END AS FINSCHOOL_NAME, ";
        $query .= "     VALUE(CLUB_M.CLUBNAME,'') AS CLUBNAME, ";
        $query .= "     VALUE(L3.ABBV1,'') AS SEX, ";
        $query .= "     VALUE(L6.NAME1,'') AS ENT_DIV_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO    = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR       = T3.YEAR ";
        $query .= "                                   AND T1.SEMESTER   = T3.SEMESTER ";
        $query .= "                                   AND T1.GRADE      = T3.GRADE ";
        $query .= "                                   AND T1.HR_CLASS   = T3.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR       = GDAT.YEAR ";
        $query .= "                                   AND T1.GRADE      = GDAT.GRADE ";
        $query .= "     LEFT JOIN GUARDIAN_DAT L1 ON T2.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L2 ON T2.FINSCHOOLCD = L2.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' AND L3.NAMECD2 = T2.SEX ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     SCHREGNO, ";
        $query .= "                     ZIPCD, ";
        $query .= "                     TELNO, ";
        $query .= "                     ADDR1, ";
        $query .= "                     ADDR2 ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_ADDRESS_DAT W1 ";
        $query .= "                 WHERE ";
        $query .= "                     (W1.SCHREGNO,W1.ISSUEDATE) IN (SELECT ";
        $query .= "                                                         SCHREGNO, ";
        $query .= "                                                         MAX(ISSUEDATE) ";
        $query .= "                                                     FROM ";
        $query .= "                                                         SCHREG_ADDRESS_DAT W2 ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         W2.ISSUEDATE <= '".$edate."' AND ";
        $query .= "                                                         (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '".$sdate."') ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         SCHREGNO ";
        $query .= "                                                     ) ";
        $query .= "                 ) L4 ON L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN STAFF_MST L5 ON T3.TR_CD1 = L5.STAFFCD ";
        $query .= "     LEFT JOIN NAME_MST L6 ON L6.NAMECD1 = 'A002' AND L6.NAMECD2 = T2.ENT_DIV ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST DET002 ON DET002.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                            AND DET002.BASE_SEQ ='002' ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     SCHREGNO, ";
        $query .= "                     MIN(CLUBCD) AS CLUBCD ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_CLUB_HIST_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     '".CTRL_DATE."' BETWEEN SDATE AND VALUE(EDATE, '9999-12-31') ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 ) CLUB_T ON CLUB_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN CLUB_MST CLUB_M ON CLUB_T.CLUBCD = CLUB_M.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".implode(explode(',', $model->selectdata),"','")."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '". substr($model->field["GRADE_HR_CLASS"],0,2)."' ";
            $query .= "                     ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '". substr($model->field["GRADE_HR_CLASS"],0,2)."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }

    function getArea($model) {
        $query  = " WITH MITOUROKU (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('未登録', '999') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A020' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     MITOUROKU ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getZ010Name1() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }
}
?>
