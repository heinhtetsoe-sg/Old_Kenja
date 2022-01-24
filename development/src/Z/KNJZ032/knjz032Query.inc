<?php

require_once('for_php7.php');

class knjz032Query extends Query {
    
    //学期カウント
    function getSemesterCount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        
        return $query;
    }
        
    //学年コンボ
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //対象データ一覧を取得する
    function getRecordList($model, $setGrade="")
    {
        $query  = " SELECT ";
        if ($setGrade == "") {
            $query .= "     L1.GRADE_NAME1, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.SEMESTERNAME, ";
            $query .= "     T1.SEMESTER || ':' || T1.SEMESTERNAME AS SET_SEMESTERNAME, ";
            $query .= "     T1.SDATE, ";
            $query .= "     T1.EDATE ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_GRADE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                  AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($setGrade != "") {
            $query .= " AND T1.GRADE = '".$setGrade."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.SEMESTER ";
        }
        
        return $query;
    }

    //対象データ取得
    function getSelectData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.SEMESTERNAME, ";
        $query .= "     L1.SDATE, ";
        $query .= "     L1.EDATE, ";
        $query .= "     T1.SEMESTERNAME AS SEM_SEMESTERNAME, ";
        $query .= "     T1.SDATE AS SEM_SDATE, ";
        $query .= "     T1.EDATE AS SEM_EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= "     LEFT JOIN SEMESTER_GRADE_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                    AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                                    AND L1.GRADE = '".$model->grade."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER ";

        return $query;
    }

    //対象データカウント
    function getSemesterGradeCount($model) {
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "  AND SEMESTER = '".$model->semester."' ";
        $query .= "  AND GRADE = '".$model->grade."' ";
        return $query;
    }

    //INSERT
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        
        $getData = $db->getOne(knjz032Query::getSemesterGradeCount($model));
        
        $data = array();
        //$data["SEMESTER"][TEXT]         = $model->semester;
        $data["SEMESTERNAME"][TEXT]     = $model->semestername;
        $data["SDATE"][TEXT]            = str_replace("/","-",$model->sdate);
        $data["EDATE"][TEXT]            = str_replace("/","-",$model->edate);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($getData > 0) {
            $where  = " WHERE YEAR = '".$model->year."' ";
            $where .= " AND SEMESTER = '".$model->semester."' ";
            $where .= " AND GRADE = '".$model->grade."' ";
            $query = Query::updateSQL($data, "SEMESTER_GRADE_MST", $where);
        } else {
            $data["YEAR"][TEXT]         = $model->year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["GRADE"][TEXT]        = $model->grade;
            $query = Query::insertSQL($data, "SEMESTER_GRADE_MST");
        }
        $db->query($query);
        
        Query::dbCheckIn($db);
        return true;
    }
}
?>
