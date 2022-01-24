<?php

require_once('for_php7.php');

class knjc163Query extends Query {
    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "     AND T2.GRADE = T1.GRADE ";
            $query .= "     AND T2.SCHOOL_KIND IN ('J', 'H') ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "     AND T2.GRADE = T1.GRADE ";
            $query .= "     AND T2.SCHOOL_KIND IN ('J', 'H') ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
            $query .= "     AND (  T1.TR_CD1  = '".STAFFCD."' ";
            $query .= "         OR T1.TR_CD2  = '".STAFFCD."' ";
            $query .= "         OR T1.TR_CD3  = '".STAFFCD."') ";
        }

        return $query;
    }

    //年組取得
    function getHrName($grade, $hrClass) {
        $query  = " SELECT T1.HR_NAME ";
        $query .= " FROM SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE    = '".$grade."' ";
        $query .= "     AND T1.HR_CLASS = '".$hrClass."' ";
        return $query;
    }

    //在籍データ取得
    function getRegd($grade, $hrClass) {
        $query  = " SELECT T1.SCHREGNO, T1.ATTENDNO, T2.NAME_SHOW ";
        $query .= " FROM SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE    = '".$grade."' ";
        $query .= "     AND T1.HR_CLASS = '".$hrClass."' ";
        $query .= " ORDER BY T1.ATTENDNO ";
        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . $year . "' ";
        $query .= "     AND NAMECD1 = '" . $namecd1 . "' ";
        $query .= "     AND NAMECD2 = '" . $namecd2 . "' ";
        return $query;
    }

    function getSchoolMstMap($db, $year) {
        $rtnMap = array();
        $query = "SELECT * FROM V_SCHOOL_MST WHERE YEAR = '" . $year . "'";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $rtnMap[$key] = $val;
            }
        }
        return $rtnMap;
    }

    function attendSemes(
                            $definecode,
                            $year,
                            $defineSchoolCode,
                            $knjSchoolMst,
                            $periodInState,
                            $sdate,
                            $edate,
                            $hasuuMap,
                            $grade,
                            $hrClass,
                            $model
    ) {
        $attendSql = AttendAccumulate::getAttendSemesSql(
                                    false, // $hasuuMap["semesFlg"],
                                    $definecode,
                                    $knjSchoolMst,
                                    $year,
                                    "1",
                                    "9",
                                    $hasuuMap["attendSemesInState"],
                                    $periodInState,
                                    "", // $hasuuMap["befDayFrom"],
                                    "", // $hasuuMap["befDayTo"],
                                    $sdate, // $hasuuMap["aftDayFrom"],
                                    $edate, // $hasuuMap["aftDayTo"],
                                    $grade,
                                    $hrClass,
                                    '',
                                    "SEMESTER",
                                    $model->Properties["useCurriculumcd"]);

        return $attendSql;
    }
}
?>
