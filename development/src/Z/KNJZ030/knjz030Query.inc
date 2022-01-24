<?php

require_once('for_php7.php');


class knjz030Query extends Query
{
    public function getSemester($year, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "") {
            $query .= "     SEMESTER, ";
            $query .= "     SEMESTERNAME, ";
            $query .= "     SDATE, ";
            $query .= "     EDATE ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if ($flg == "") {
            $query .= " ORDER BY ";
            $query .= "     SEMESTER ";
        }
        
        return $query;
    }

    public function getYears()
    {
        return "SELECT DISTINCT year FROM semester_mst ORDER BY year DESC";
    }

    public function getSemesterCount($year, $model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERDIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."'";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["SEMESTERNAME"][TEXT]     = $model->semestername;
        $data["SDATE"][TEXT]            = str_replace("/", "-", $model->sdate);
        $data["EDATE"][TEXT]            = str_replace("/", "-", $model->edate);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $where = "WHERE YEAR = '".$model->year."' AND SEMESTER = '".$model->semester."' ";
        $query = Query::updateSQL($data, "SEMESTER_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
