<?php

require_once('for_php7.php');

class knja113Query extends Query
{

    /****************/
    /*    左画面    */
    /****************/

    //校種取得
    public function getSchKind($model, $val = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->exeYear."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        if ($val) {
            $query .= " AND NAME1 = '".$val."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //減免マスタ取得
    public function getScholarshipMst($model, $schoolkind, $val = "")
    {
        $query  = " SELECT ";
        $query .= "    REDUCTION_DIV_CD || ':' || REDUCTION_DIV_NAME AS LABEL, ";
        $query .= "    REDUCTION_DIV_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolkind."' AND ";
        $query .= "     YEAR        = '".$model->exeYear."' ";
        if ($val) {
            $query .= " AND REDUCTION_DIV_CD = '".$val."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        if ($model->std_div == "1") {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     '新入生' || T1.GRADE AS LABEL ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.ENTERYEAR = T2.YEAR AND T1.GRADE = T2.GRADE AND T2.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= " WHERE ";
            $query .= "     T1.ENTERYEAR = '".$model->exeYear."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$model->exeYear."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //年組取得
    public function getGradeHrClass($model)
    {
        if ($model->std_div == "1") {
            $query  = " SELECT DISTINCT ";
            $query .= "                 T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') AS VALUE, ";
            $query .= "     '新入生' || T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') AS LABEL ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.ENTERYEAR = T2.YEAR AND T1.GRADE = T2.GRADE AND T2.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= " WHERE ";
            $query .= "     T1.ENTERYEAR = '".$model->exeYear."' ";
            if ($model->grade) {
                $query .= " AND T1.GRADE = '".$model->grade."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
            $query .= "          ON T1.YEAR         = T2.YEAR ";
            $query .= "         AND T1.GRADE        = T2.GRADE ";
            $query .= "         AND T2.SCHOOL_KIND  = '".$model->schoolkind."' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".$model->exeYear."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->grade) {
                $query .= " AND T1.GRADE = '".$model->grade."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //クラブ取得
    public function getClub($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD AS VALUE, ";
        $query .= "     T1.CLUBCD || ':' || T1.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST T1, ";
        $query .= "     CLUB_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD   = T2.CLUBCD AND ";
        $query .= "     T2.YEAR     = '".$model->exeYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    public function getList($model)
    {
        $query  = " WITH SCH_DATA AS ( ";
        if ($model->std_div == "1") {
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.GRADE, ";
            $query .= "         VALUE(T1.HR_CLASS, '000') AS HR_CLASS, ";
            $query .= "         VALUE(T1.ATTENDNO, '000') AS ATTENDNO, ";
            $query .= "         T1.NAME AS NAME_SHOW, ";
            $query .= "         '新入生' || T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') AS HR_NAME ";
            $query .= "     FROM ";
            $query .= "         FRESHMAN_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ON T1.ENTERYEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
            if ($model->clubcd) {
                $query .= "         INNER JOIN SCHREG_CLUB_HIST_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO AND L1.CLUBCD = '".$model->clubcd."' ";
            }
            $query .= "     WHERE ";
            $query .= "         T1.ENTERYEAR = '".$model->exeYear."' ";
        } else {
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.GRADE, ";
            $query .= "         T1.HR_CLASS, ";
            $query .= "         T1.ATTENDNO, ";
            $query .= "         T2.NAME_SHOW, ";
            $query .= "         T4.HR_NAME ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= "         INNER JOIN SCHREG_REGD_HDAT T4 ON T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER AND T1.GRADE = T4.GRADE AND T1.HR_CLASS = T4.HR_CLASS ";
            if ($model->clubcd) {
                $query .= "         INNER JOIN SCHREG_CLUB_HIST_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO AND L1.CLUBCD = '".$model->clubcd."' ";
            }
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".$model->exeYear."' AND ";
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.*, ";
        $query .= "     T1.FROM_DATE, ";
        $query .= "     T1.TO_DATE, ";
        $query .= "     T1.REMARK     ";
        $query .= " FROM ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT T1, ";
        $query .= "     SCH_DATA T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     T1.SCHOOL_KIND = '".$model->schoolkind."' AND ";
        $query .= "     T1.SCHOLARSHIP = '".$model->scholarship."' AND ";
        $query .= "     T1.SCHREGNO    = T2.SCHREGNO ";
        if ($model->grade) {
            $query .= " AND T2.GRADE            = '".$model->grade."' ";
        }
        if ($model->grade_hr_class) {
            $query .= " AND T2.GRADE || '-' || T2.HR_CLASS = '".$model->grade_hr_class."' ";
        }
        if (!$model->grade && !$model->grade_hr_class && !$model->clubcd) {
            $query .= " AND T1.SCHREGNO IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.FROM_DATE ";

        return $query;
    }


    /****************/
    /*    右画面    */
    /****************/

    //生徒情報取得
    public function getSchData($model, $schregno)
    {
        if ($model->std_div == "1") {
            $query  = " SELECT ";
            $query .= "     '新入生' || T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') || '-' || VALUE(T1.ATTENDNO, '000') || '番' || '　 ' || T1.NAME ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.ENTERYEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= " WHERE ";
            $query .= "     T1.ENTERYEAR = '".$model->exeYear."' AND ";
            $query .= "     T1.SCHREGNO  = '".$schregno."' ";
        } else {
            $query  = " SELECT ";
            $query .= "     T4.HR_NAME || '-' || T1.ATTENDNO || '番' || '　 ' || T2.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ON T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER AND T1.GRADE = T4.GRADE AND T1.HR_CLASS = T4.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".$model->exeYear."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
        }

        return $query;
    }

    //データ取得
    public function getReductionSchoolStdDat($model, $schregno, $from_date, $div, $flg)
    {
        $equal = ($div == "1") ? "!=" : "=";

        $fld_sYMD = date('Y-m-d', strtotime('first day of ' . str_replace("/", "-", $model->field["FROM_DATE"])));
        $fld_eYMD = date('Y-m-d', strtotime('last day of ' . str_replace("/", "-", $model->field["TO_DATE"])));

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' AND ";
        $query .= "     SCHOLARSHIP = '".$model->scholarship."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";
        if ($from_date) {
            $query .= "     AND FROM_DATE       ".$equal." '{$from_date}-01' ";
        }
        if ($flg) {
            $query .= "     AND (FROM_DATE BETWEEN '".$fld_sYMD."' AND '".$fld_eYMD."' OR TO_DATE BETWEEN '".$fld_sYMD."' AND '".$fld_eYMD."' OR '".$fld_sYMD."' BETWEEN FROM_DATE AND TO_DATE) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     FROM_DATE ";

        return $query;
    }


    /******************/
    /*    更新処理    */
    /******************/

    //更新処理
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query = knja113Query::getLastDayQuery($db, $fields["TO_DATE"]."-01");
        $setTodate = $db->getOne($query);

        $data = array();
        if ($model->cmd == "add") {
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $fields["SCHOOL_KIND"];
            $data["SCHOLARSHIP"][TEXT]  = $fields["SCHOLARSHIP"];
            $data["SCHREGNO"][TEXT]     = $fields["SCHREGNO"];
        }
        $data["FROM_DATE"][TEXT]        = $fields["FROM_DATE"]."-01";
        $data["TO_DATE"][TEXT]          = $setTodate;
        $data["REMARK"][TEXT]           = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($model->cmd == "add") {
            $query = Query::insertSQL($data, "SCHREG_SCHOLARSHIP_HIST_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $where .= "     SCHOOL_KIND     = '".$fields["SCHOOL_KIND"]."' AND ";
            $where .= "     SCHOLARSHIP     = '".$fields["SCHOLARSHIP"]."' AND ";
            $where .= "     SCHREGNO        = '".$fields["SCHREGNO"]."' AND ";
            $where .= "     FROM_DATE       = '".$model->from_date."-01' ";

            $query = Query::updateSQL($data, "SCHREG_SCHOLARSHIP_HIST_DAT", $where);
        }
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

    //削除処理
    public function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND     = '".$fields["SCHOOL_KIND"]."' AND ";
        $query .= "     SCHOLARSHIP     = '".$fields["SCHOLARSHIP"]."' AND ";
        $query .= "     SCHREGNO        = '".$fields["SCHREGNO"]."' AND ";
        $query .= "     FROM_DATE       = '".$model->from_date."-01' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }


    /**********************/
    /*    一括更新画面    */
    /**********************/

    //生徒一覧取得
    public function getSchList($model)
    {
        if ($model->std_div == "1") {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || VALUE(T1.HR_CLASS, '000') || VALUE(T1.ATTENDNO, '000') || '_' || T1.SCHREGNO AS VALUE, ";
            $query .= "     '新入生' || T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') || ' ' || VALUE(T1.ATTENDNO, '000') || '番　' || T1.NAME AS LABEL, ";
            $query .= "     T1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.ENTERYEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
            if ($model->clubcd) {
                $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO AND L1.CLUBCD = '".$model->clubcd."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.ENTERYEAR = '".$model->exeYear."' ";
            if ($model->grade) {
                $query .= " AND T1.GRADE    = '".$model->grade."' ";
            }
            if ($model->grade_hr_class) {
                $query .= " AND T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') = '".$model->grade_hr_class."' ";
            }
            if (!$model->grade && !$model->grade_hr_class && !$model->clubcd) {
                $query .= " AND T1.SCHREGNO IS NULL ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '_' || T1.SCHREGNO AS VALUE, ";
            $query .= "     T4.HR_NAME || ' ' || T1.ATTENDNO || '番　' || T2.NAME_SHOW AS LABEL, ";
            $query .= "     T1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ON T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER AND T1.GRADE = T4.GRADE AND T1.HR_CLASS = T4.HR_CLASS ";
            if ($model->clubcd) {
                $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO AND L1.CLUBCD = '".$model->clubcd."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".$model->exeYear."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->grade) {
                $query .= " AND T1.GRADE    = '".$model->grade."' ";
            }
            if ($model->grade_hr_class) {
                $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->grade_hr_class."' ";
            }
            if (!$model->grade && !$model->grade_hr_class && !$model->clubcd) {
                $query .= " AND T1.SCHREGNO IS NULL ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }


    /**********************/
    /*    一括更新処理    */
    /**********************/

    //データ取得（範囲チェック用）
    public function getReduSchlStdDat($model)
    {
        $fld_sYMD = date('Y-m-d', strtotime('first day of ' . str_replace("/", "-", $model->field["FROM_DATE"])));
        $fld_eYMD = date('Y-m-d', strtotime('last day of ' . str_replace("/", "-", $model->field["TO_DATE"])));

        $query  = " WITH SCH_DATA AS ( ";
        if ($model->std_div == "1") {
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.GRADE, ";
            $query .= "         VALUE(T1.HR_CLASS, '000') AS HR_CLASS, ";
            $query .= "         VALUE(T1.ATTENDNO, '000') AS ATTENDNO, ";
            $query .= "         T1.NAME AS NAME_SHOW, ";
            $query .= "         '新入生' || T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') AS HR_NAME ";
            $query .= "     FROM ";
            $query .= "         FRESHMAN_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ON T1.ENTERYEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= "     WHERE ";
            $query .= "         T1.ENTERYEAR = '".$model->exeYear."' ";
        } else {
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.GRADE, ";
            $query .= "         T1.HR_CLASS, ";
            $query .= "         T1.ATTENDNO, ";
            $query .= "         T2.NAME_SHOW, ";
            $query .= "         T4.HR_NAME ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
            $query .= "         INNER JOIN SCHREG_REGD_HDAT T4 ON T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER AND T1.GRADE = T4.GRADE AND T1.HR_CLASS = T4.HR_CLASS ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".$model->exeYear."' AND ";
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.FROM_DATE, ";
        $query .= "     T2.TO_DATE, ";
        $query .= "     CASE WHEN (T2.FROM_DATE BETWEEN '".$fld_sYMD."' AND '".$fld_eYMD."' OR T2.TO_DATE BETWEEN '".$fld_sYMD."' AND '".$fld_eYMD."' OR '".$fld_sYMD."' BETWEEN T2.FROM_DATE AND T2.TO_DATE) THEN 1 ELSE 0 END AS FLG ";
        $query .= " FROM ";
        $query .= "     SCH_DATA T1, ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO    = T2.SCHREGNO AND ";
        $query .= "     T2.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     T2.SCHOOL_KIND = '".$model->schoolkind."' AND ";
        $query .= "     T2.SCHOLARSHIP = '".$model->scholarship."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.FROM_DATE ";

        return $query;
    }

    //一括更新
    public function &getIkkatsuInsertQuery($model, $fields, $schArray)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knja113Query::getLastDayQuery($db, $fields["TO_DATE"]."-01");
        $setTodate = $db->getOne($query);

        foreach ($schArray as $schregno) {
            $data = array();
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $fields["SCHOOL_KIND"];
            $data["SCHOLARSHIP"][TEXT]      = $fields["SCHOLARSHIP"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["FROM_DATE"][TEXT]        = $fields["FROM_DATE"]."-01";
            $data["TO_DATE"][TEXT]          = $setTodate;
            $data["REMARK"][TEXT]           = $fields["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_SCHOLARSHIP_HIST_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //最終日取得
    public function getLastDayQuery($db, $day)
    {
        $query = "VALUES LAST_DAY(DATE('{$day}')) ";
        return $query;
    }
}
