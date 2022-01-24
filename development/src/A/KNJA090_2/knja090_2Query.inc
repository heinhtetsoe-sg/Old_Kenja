<?php

require_once('for_php7.php');

class knja090_2Query extends Query {

    // 学期名取得
    function Getsemes_name($year, $semester)
    {
        $query  = "select SEMESTERNAME";
        $query .= "  from SEMESTER_MST";
        $query .= " where YEAR='".$year."'";
        $query .= "   and SEMESTER='".$semester."'";

        return $query;
    }

    function GetStudent($model)
    {
        $query  = "SELECT a.NAME_SHOW as name";
        $query .= "      ,b.schregno";
        $query .= "      ,b.attendno";
        $query .= "  FROM schreg_base_mst a RIGHT OUTER JOIN ";
        $query .= "       class_formation_dat b ";
        $query .= " ON  a.schregno = b.schregno ";
        $query .= " where b.year     = '".$model->year."'";
        $query .= "   AND b.semester = '".$model->semester."'";
        $query .= "   AND b.grade    = '".$model->grade."'";
        $query .= "   AND b.hr_class = '".$model->hr_class."'";
        $query .= " UNION ";
        $query .= " SELECT a.NAME as name";
        $query .= "      ,b.schregno";
        $query .= "      ,b.attendno";
        $query .= "  FROM freshman_dat a RIGHT OUTER JOIN ";
        $query .= "       class_formation_dat b ";
        $query .= "    ON a.schregno = b.schregno ";
        $query .= " WHERE b.year     = '".$model->year."'";
        $query .= "   AND b.semester = '".$model->semester."'";
        $query .= "   AND b.grade    = '".$model->grade."'";
        $query .= "   AND b.hr_class = '".$model->hr_class."'";
        $query .= " ORDER BY attendno ";
        
        return $query;    
    }
    
    function GetStudent_Course($model)
    {
        $array = explode(",", $model->selectdata);

//        $query  = "select COURSE_PK";
        $query  = "select COURSECODE";
        $query .= "  from class_formation_dat ";
        $query .= " where schregno = '".$array[0]."'";
        $query .= "   and year     = '".$model->year."'";
        $query .= "   and semester = '".$model->semester."'";
        $query .= "   and grade    = '".$model->grade."'";
        $query .= "   and hr_class = '".$model->hr_class."'";

        return $query;    
    }
    
    //コースコード
    function GetCourse($year)
    {
        $db = Query::dbCheckOut();

        $opt = array();

        $query  = " SELECT COURSECODE, COURSECODENAME ";
        $query .= "   FROM V_COURSECODE_MST ";
        $query .= " WHERE YEAR     = '".$year."'";
        $query .= " ORDER BY COURSECODE ";

        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECODENAME"], "value" => $row["COURSECODE"]);
        }
        
        Query::dbCheckIn($db);
        return $opt;            
    }
    
    function &GetUpdateQuery($model)
    {        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $array = explode(",", $model->selectdata);
        
        if ($model->selectdata != ""){
            $query  = "update class_formation_dat ";

            $query .= "   set COURSECODE = '".$model->course["course1"]."'";
/*
            $query .= "   set coursecode1 = '".$model->course["course1"]."'";
            $query .= "        ,coursecode2 = '".$model->course["course2"]."'";
            $query .= "      ,coursecode3 = '".$model->course["course3"]."'";
*/
            $query .= " where schregno IN (";
            for ($i = 0; $i < get_count($array); $i++ ){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query .= "'".$array[$i]."'".$sp;    
            }
            $query .= ")  and year     = '".$model->year."'";
            $query .= "   and semester = '".$model->semester."'";
            $query .= "   and grade       = '".$model->grade."'";
            $query .= "   and hr_class = '".sprintf("%02d",$model->hr_class)."'"; 
            $db->query($query);
            $flg = 1;
        } else {
            $flg = 0;
        }
        $db->commit();

        Query::dbCheckIn($db);
        return $flg;
    }
}
?>
