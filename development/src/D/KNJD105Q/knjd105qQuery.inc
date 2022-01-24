<?php

require_once('for_php7.php');

class knjd105qQuery extends Query {
    //学校名取得の取得(
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //学期取得
    function getSemester($model, $semester = '')
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($semester == '') {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model, $div = "")
    {
        //参照・更新可
        $query  = " SELECT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "  INNER JOIN SCHREG_REGD_DAT REGD ";
            $query .= "     ON REGD.YEAR = T1.YEAR ";
            $query .= "     AND REGD.SEMESTER = T1.SEMESTER ";
            $query .= "     AND REGD.GRADE = T1.GRADE ";
            $query .= "     AND REGD.HR_CLASS = T1.HR_CLASS ";
            $query .= "     AND REGD.COURSECD || '-' || REGD.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T2.SCHOOL_KIND <> 'P' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        if ($div == "GRADE") {
        } else {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        if ($div == "GRADE") {
            $query .= " GROUP BY ";
            $query .= "     T1.GRADE, T2.GRADE_NAME1 ";
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE ";
        } else {
            $query .= " GROUP BY ";
            $query .= "     T1.GRADE, T1.HR_CLASS, T1.HR_NAME ";
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS ";
        }

        return $query;
    }

    //テスト取得
    function getBeforeTestNextDate($db, $model)
    {

        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD, ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY(TESTKINDCD || TESTITEMCD || SCORE_DIV)) AS ORDER ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     ,TEST_START_DATE ";
            $query .= "     ,TEST_END_DATE ";
        } else {
            $query .= "     ,CAST(NULL AS DATE) AS TEST_START_DATE ";
            $query .= "     ,CAST(NULL AS DATE) AS TEST_END_DATE ";
        }
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NOT (T1.SEMESTER <> '9' AND T1.SCORE_DIV = '09') ";
        $query .= " AND NOT (T1.SEMESTER <> '9' AND T1.TESTKINDCD = '99') ";
//        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.GRADE = '00' ";
            $query .= " AND T1.COURSECD || '-' || MAJORCD = '".$model->field["MAJOR"]."' ";
            if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER, TESTKINDCD || TESTITEMCD || SCORE_DIV ";

        $result = $db->query($query);
        $before = '';
        $beforeEndDate = '';
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SEMESTER"] . $row["TESTCD"] == $model->field["SEMESTER"] . $model->field["TESTCD"]) {
                break;
            }
            $before = $row["SEMESTER"] . $row["TESTCD"];
            $beforeEndDate = $row["TEST_END_DATE"];
        }
        $result->free();
        //echo "before = ".$before.", enddate = ".$beforeEndDate."<br>";
        if ($beforeEndDate != '') {
            $nextDate = $db->getOne("VALUES(DATE('".$beforeEndDate."') + 1 days)");
            return $nextDate;
        }
        $query = knjd105qQuery::getSemester($model, $model->field["SEMESTER"]);
        $sdate = '';
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sdate = $row["SDATE"];
        }
        $result->free();
        return $sdate;
    }

    //テスト取得
    function getEdate($db, $model)
    {
        $query = knjd105qQuery::getTest($model, $model->field["SEMESTER"].$model->field["TESTCD"]);
        $result = $db->query($query);
        $enddate = '';
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $enddate = $row["TEST_END_DATE"];
        }
        $result->free();
        if ($enddate != '') {
            return $enddate;
        }
        $query = knjd105qQuery::getSemester($model, $model->field["SEMESTER"]);
        $enddate = '';
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $enddate = $row["EDATE"];
        }
        return $enddate;
    }


    //テスト取得
    function getTest($model, $semeTestcd = '')
    {

        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL, ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TEST_START_DATE, ";
            $query .= "     TEST_END_DATE ";
        } else {
            $query .= "     CAST(NULL AS DATE) AS TEST_START_DATE, ";
            $query .= "     CAST(NULL AS DATE) AS TEST_END_DATE ";
        }
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NOT (T1.SEMESTER <> '9' AND T1.SCORE_DIV = '09') ";
        $query .= " AND NOT (T1.SEMESTER <> '9' AND T1.TESTKINDCD = '99') ";
        if ($semeTestcd == '') {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.GRADE = '00' ";
            $query .= " AND T1.COURSECD || '-' || MAJORCD = '".$model->field["MAJOR"]."' ";
            if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            }
        }
        if ($semeTestcd) {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$semeTestcd."' ";
        }

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $seme)
    {

        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE || '-' || T2.HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T2.COURSECD || '-' || T2.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model,$semester)
    {
        $date = str_replace("/", "-", CTRL_DATE);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$semester."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["HR_CLASS"]."' AND ( ";
                           //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                           //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                     (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
                           //留学(1)・休学(2)者
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                      S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                      CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                           THEN T2.EDATE ELSE '".$date."' END  ";
        $query .= "                      BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    function getGdat($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     GRADE = '$grade' ";

        return $query;
    }

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
