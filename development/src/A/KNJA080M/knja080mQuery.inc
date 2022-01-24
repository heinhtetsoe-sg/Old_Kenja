<?php

require_once('for_php7.php');

class knja080mQuery extends Query {

    function GetYearSeme() {
        $query  = " SELECT * ";
        $query .= " FROM SEMESTER_MST ";
        $query .= " WHERE YEAR IN ( '".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "   AND SEMESTER <> '9' ";
        return $query;
    }

    function getA023SchoolKind($model, $name1) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 = '{$name1}' ";
        $query .= " ORDER BY ";
        $query .= "     ABBV2 ";
        return $query;
    }

    function getA023($model) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ABBV2 ";
        return $query;
    }


    //クラス取得
    function getHr_Class($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.GRADE ";
        $query .= "   , T1.HR_CLASS ";
        $query .= "   , T1.HR_NAME ";
        $query .= "   , T1.HR_NAMEABBV ";
        $query .= "   , T2.STAFFCD ";
        $query .= "   , T2.STAFFNAME ";
        $query .= "   , T2.STAFFNAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
                $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "   LEFT JOIN STAFF_MST T2 ";
        $query .= "     ON T1.TR_CD1 = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->year}' ";
        $query .= "   AND T1.SEMESTER = '{$model->semester}' ";

        $query .= " ORDER BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS ";

        return $query;
    }


    function getfreshMan($model) {

        $query .= " SELECT * ";
        $query .= " FROM FRESHMAN_DAT ";
        $query .= " WHERE ENTERYEAR = '{$model->year}' ";

        return $query;
    }

    function GetStudent($model, $hrClass) {
        list($grade, $hr_class) = explode("-", $hrClass);

        $query .= " SELECT * ";
        $query .= " FROM ( ";

        $query .= " SELECT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T2.NAME_SHOW AS NAME ";
        $query .= "   , T1.GRADE ";
        $query .= "   , T1.HR_CLASS ";
        $query .= "   , T3.HR_NAME ";
        $query .= "   , T1.ATTENDNO ";
        $query .= "   , CASE ";
        $query .= "     WHEN T4.SCHREGNO IS NULL ";
        $query .= "       THEN '' ";
        $query .= "     ELSE '1' ";
        $query .= "     END AS FRESHMAN_FLG ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "     ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "   LEFT JOIN FRESHMAN_DAT T4 ";
        $query .= "     ON T1.YEAR = T4.ENTERYEAR ";
        $query .= "     AND T1.SCHREGNO = T4.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->year."' ";
        $query .= "   AND T1.SEMESTER = '".$model->semester."' ";
        $query .= "   AND T1.GRADE = '".$grade."' ";
        $query .= "   AND T1.HR_CLASS = '".$hr_class."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "   T1.ENTERYEAR AS YEAR ";
        $query .= "   , '".$model->semester."' AS SEMESTER ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T1.NAME ";
        $query .= "   , '00' AS GRADE ";
        $query .= "   , '000' AS HR_CLASS ";
        $query .= "   , T3.HR_NAME ";
        $query .= "   , T1.ATTENDNO ";
        $query .= "   , '1' AS FRESHMAN_FLG ";
        $query .= " FROM ";
        $query .= "   FRESHMAN_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "     ON T3.YEAR = '".$model->year."' ";
        $query .= "     AND T3.SEMESTER = '".$model->semester."' ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "   T1.ENTERYEAR = '".$model->year."' ";
        $query .= "   AND NOT EXISTS ( ";
        $query .= "     SELECT ";
        $query .= "       1 ";
        $query .= "     FROM ";
        $query .= "       SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "       YEAR = '".$model->year."' ";
        $query .= "       AND SEMESTER = '".$model->semester."' ";
        $query .= "       AND SCHREGNO = T1.SCHREGNO ";
        $query .= "   ) ";

        $query .= " ) ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->year."' ";
        $query .= "   AND SEMESTER = '".$model->semester."' ";
        $query .= "   AND GRADE = '".$grade."' ";
        $query .= "   AND HR_CLASS = '".$hr_class."' ";
        
        $query .= " ORDER BY ";
        $query .= "   GRADE ";
        $query .= "   , HR_CLASS ";
        $query .= "   , ATTENDNO ";
        
        return $query;
    }

    function &getUpdateQuery($model)
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($gradeL, $hr_classL) = explode("-", $model->leftclass);
        list($gradeR, $hr_classR) = explode("-", $model->rightclass);
        if ($gradeR == '00') {
            //右に移動した生徒
            $afterStdL = explode(",", $model->leftData);
            $idouStdR = array_diff($model->schregnoL, array_intersect($model->schregnoL, $afterStdL));
            //REGDから削除
            $query  = " DELETE FROM ";
            $query .= "   SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "       YEAR      = '{$model->year}' ";
            $query .= "   AND SEMESTER  = '{$model->semester}' ";
            $query .= "   AND SCHREGNO IN ('" .implode($idouStdR, "','") ."') ";
            $db->query($query);

            //左に移動した生徒
            $afterStdR = explode(",", $model->rightData);
            $idouStdL = array_diff($model->schregnoR, array_intersect($model->schregnoR, $afterStdR));
            //MAX出席番号取得
            $query  = " SELECT ";
            $query .= "     MAX(ATTENDNO) AS MAX_ATTENDNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "       YEAR      = '{$model->year}' ";
            $query .= "   AND SEMESTER  = '{$model->semester}' ";
            $query .= "   AND GRADE     = '{$gradeL}' ";
            $query .= "   AND HR_CLASS  = '{$hr_classL}' ";
            $attendNo = $db->getOne($query);
            $attendNo = strlen($attendNo) > 0 ? $attendNo + 1 : 1;

            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTERYEAR = '{$model->year}' ";
            $query .= "     AND SCHREGNO IN ('" .implode($idouStdL, "','") ."') ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array();
                $data["SCHREGNO"][TEXT]   = $row["SCHREGNO"];
                $data["YEAR"][TEXT]       = $model->year;
                $data["SEMESTER"][TEXT]   = $model->semester;
                $data["GRADE"][TEXT]      = $gradeL;
                $data["HR_CLASS"][TEXT]   = $hr_classL;
                $data["ATTENDNO"][TEXT]   = sprintf("%03d", $attendNo);
                $data["ANNUAL"][TEXT]     = '01';
                $data["COURSECD"][TEXT]   = $row["COURSECD"];
                $data["MAJORCD"][TEXT]    = $row["MAJORCD"];
                $data["COURSECODE"][TEXT] = $row["COURSECODE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $query = Query::insertSQL($data,"SCHREG_REGD_DAT");
                $db->query($query);
                $attendNo++;
            }
            $result->free();

        } else {
            $query = knja080mQuery::getGradeHrUpd($model, $model->leftData, $model->schregnoL, $gradeR, $hr_classR);
            $db->query($query);
            $query = knja080mQuery::getGradeHrUpd($model, $model->rightData, $model->schregnoR, $gradeL, $hr_classL);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //新入生以外の年組更新
    function getGradeHrUpd($model, $aftData, $befData, $grade, $hr_class) {
        //移動した生徒
        $afterStd = explode(",", $aftData);
        $idouStd = array_diff($befData, array_intersect($befData, $afterStd));
        //年組更新
        $query  = " UPDATE ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " SET ";
        $query .= "   GRADE      = '{$grade}', ";
        $query .= "   HR_CLASS   = '{$hr_class}', ";
        $query .= "   ATTENDNO   = NULL, ";
        $query .= "   REGISTERCD = '".STAFFCD."', ";
        $query .= "   UPDATED    = sysdate() ";
        $query .= " WHERE ";
        $query .= "       YEAR      = '{$model->year}' ";
        $query .= "   AND SEMESTER  = '{$model->semester}' ";
        $query .= "   AND SCHREGNO IN ('" .implode($idouStd, "','") ."') ";

        return $query;
    }

    //次年度の最小の学期を求める
    function getNextYearMinSemes($model){
        $query = " SELECT ";
        $query .= "   MIN(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".(CTRL_YEAR+1) ."' ";

        return $query;
    }
}
?>
