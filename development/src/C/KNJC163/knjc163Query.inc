<?php

require_once('for_php7.php');

class knjc163Query extends Query
{

    public function getSchkind($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.NAME1 || ':' || T1.ABBV1 AS LABEL ";
        $query .= "   , T1.NAME1 AS VALUE ";
        $query .= " FROM NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY INT(VALUE(ABBV3, '9999')), NAME1 ";
        return $query;
    }

    //年組取得（権限チェック）
    public function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "     AND T2.GRADE = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "     AND T2.SCHOOL_KIND = '{$model->field["SCHKIND"]}' ";
            } else {
                $query .= "     AND T2.SCHOOL_KIND IN ('J', 'H') ";
            }
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= " FROM SCHREG_REGD_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "     AND T2.GRADE = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "     AND T2.SCHOOL_KIND = '{$model->field["SCHKIND"]}' ";
            } else {
                $query .= "     AND T2.SCHOOL_KIND IN ('J', 'H') ";
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
    public function getHrName($grade, $hrClass)
    {
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
    public function getRegd($grade, $hrClass)
    {
        $query  = " SELECT T1.SCHREGNO, T1.ATTENDNO, T2.NAME_SHOW, T2.NAME_ENG ";
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
    public function getSemester()
    {
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

    //職員情報取得
    public function getStaffInfo()
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD ='".STAFFCD."' AND ";
        $query .= "     STAFF_SEQ = '001' ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($year, $namecd1, $namecd2)
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

    public function getSchoolMstMap($model, $db, $year)
    {
        $rtnMap = array();
        $query = "SELECT * FROM V_SCHOOL_MST WHERE YEAR = '" . $year . "' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $rtnMap[$key] = $val;
            }
        }
        return $rtnMap;
    }

    public function attendSemes(
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
        if ($model->Properties["knjc163useAttendSemesDat"] == "1" || $model->Properties["hibiNyuuryokuNasi"] == "1") {
            $attendSql = AttendAccumulate::getAttendSemesSql(
                $hasuuMap["semesFlg"],
                $definecode,
                $knjSchoolMst,
                $year,
                "1",
                "9",
                $hasuuMap["attendSemesInState"],
                $periodInState,
                $hasuuMap["befDayFrom"],
                $hasuuMap["befDayTo"],
                $hasuuMap["aftDayFrom"],
                $hasuuMap["aftDayTo"],
                $grade,
                $hrClass,
                '',
                "SEMESTER",
                $model->Properties["useCurriculumcd"]
            );
        } else {
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
                $model->Properties["useCurriculumcd"]
            );
        }

        return $attendSql;
    }
}
