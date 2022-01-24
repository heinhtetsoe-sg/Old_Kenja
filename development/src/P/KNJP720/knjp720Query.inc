<?php

require_once('for_php7.php');

class knjp720Query extends Query {

    /****************/
    /*    左画面    */
    /****************/

    //校種取得
    function getSchKind($model, $val="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
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
    function getReductionDivMst($model, $schoolkind, $val="") {
        $query  = " SELECT ";
        $query .= "    REDUCTION_DIV_CD || ':' || REDUCTION_DIV_NAME AS LABEL, ";
        $query .= "    REDUCTION_DIV_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$schoolkind."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        if ($val) {
            $query .= " AND REDUCTION_DIV_CD = '".$val."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model) {
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
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->grade) {
            $query .= " AND T1.GRADE = '".$model->grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラブ取得
    function getClub($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD AS VALUE, ";
        $query .= "     T1.CLUBCD || ':' || T1.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST T1, ";
        $query .= "     CLUB_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD   = T2.CLUBCD AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    function getList($model) {
        $query  = " WITH SCH_DATA AS ( ";
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
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.*, ";
        $query .= "     T1.S_YEAR_MONTH, ";
        $query .= "     T1.E_YEAR_MONTH, ";
        $query .= "     T1.REMARK     ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_STD_DAT T1, ";
        $query .= "     SCH_DATA T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     T1.SCHOOL_KIND      = '".$model->schoolkind."' AND ";
        $query .= "     T1.REDUCTION_DIV_CD = '".$model->reduction_div_cd."' AND ";
        $query .= "     T1.SCHREGNO         = T2.SCHREGNO ";
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
        $query .= "     T1.S_YEAR_MONTH ";

        return $query;
    }


    /****************/
    /*    右画面    */
    /****************/

    //生徒情報取得
    function getSchData($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     T4.HR_NAME || '-' || T1.ATTENDNO || '番' || '　 ' || T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE AND T3.SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ON T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER AND T1.GRADE = T4.GRADE AND T1.HR_CLASS = T4.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //データ取得
    function getReductionSchoolStdDat($model, $schregno, $s_year_month, $div) {
        $equal = ($div == "1") ? "!=" : "=";

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD            = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND         = '".$model->schoolkind."' AND ";
        $query .= "     REDUCTION_DIV_CD    = '".$model->reduction_div_cd."' AND ";
        $query .= "     SCHREGNO            = '".$schregno."' ";
        if ($s_year_month) {
            $query .= " AND S_YEAR_MONTH       ".$equal." '".$s_year_month."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     S_YEAR_MONTH ";

        return $query;
    }


    /******************/
    /*    更新処理    */
    /******************/

    //更新処理
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($model->cmd == "add") {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $fields["SCHOOL_KIND"];
            $data["REDUCTION_DIV_CD"][TEXT] = $fields["REDUCTION_DIV_CD"];
            $data["SCHREGNO"][TEXT]         = $fields["SCHREGNO"];
        }
        $data["S_YEAR_MONTH"][TEXT]     = $fields["S_YEAR_MONTH"];
        $data["E_YEAR_MONTH"][TEXT]     = $fields["E_YEAR_MONTH"];
        $data["REMARK"][TEXT]           = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($model->cmd == "add") {
            $query = Query::insertSQL($data, "REDUCTION_SCHOOL_STD_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHOOLCD            = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $where .= "     SCHOOL_KIND         = '".$fields["SCHOOL_KIND"]."' AND ";
            $where .= "     REDUCTION_DIV_CD    = '".$fields["REDUCTION_DIV_CD"]."' AND ";
            $where .= "     SCHREGNO            = '".$fields["SCHREGNO"]."' AND ";
            $where .= "     S_YEAR_MONTH        = '".$model->s_year_month."' ";

            $query = Query::updateSQL($data, "REDUCTION_SCHOOL_STD_DAT", $where);
        }
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

    //削除処理
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     REDUCTION_SCHOOL_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD            = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND         = '".$fields["SCHOOL_KIND"]."' AND ";
        $query .= "     REDUCTION_DIV_CD    = '".$fields["REDUCTION_DIV_CD"]."' AND ";
        $query .= "     SCHREGNO            = '".$fields["SCHREGNO"]."' AND ";
        $query .= "     S_YEAR_MONTH        = '".$model->s_year_month."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }


    /**********************/
    /*    一括更新画面    */
    /**********************/

    //生徒一覧取得
    function getSchList($model) {
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
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
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

        return $query;
    }


    /**********************/
    /*    一括更新処理    */
    /**********************/

    //データ取得（範囲チェック用）
    function getReduSchlStdDat($model) {
        $query  = " WITH SCH_DATA AS ( ";
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
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.S_YEAR_MONTH, ";
        $query .= "     T2.E_YEAR_MONTH ";
        $query .= " FROM ";
        $query .= "     SCH_DATA T1, ";
        $query .= "     REDUCTION_SCHOOL_STD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO         = T2.SCHREGNO AND ";
        $query .= "     T2.SCHOOLCD         = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     T2.SCHOOL_KIND      = '".$model->schoolkind."' AND ";
        $query .= "     T2.REDUCTION_DIV_CD = '".$model->reduction_div_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.S_YEAR_MONTH ";

        return $query;
    }

    //一括更新
    function &getIkkatsuInsertQuery($model, $fields, $schArray) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($schArray as $schregno) {
            $data = array();
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $fields["SCHOOL_KIND"];
            $data["REDUCTION_DIV_CD"][TEXT] = $fields["REDUCTION_DIV_CD"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["S_YEAR_MONTH"][TEXT]     = $fields["S_YEAR_MONTH"];
            $data["E_YEAR_MONTH"][TEXT]     = $fields["E_YEAR_MONTH"];
            $data["REMARK"][TEXT]           = $fields["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "REDUCTION_SCHOOL_STD_DAT");
            $db->query($query);

        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
