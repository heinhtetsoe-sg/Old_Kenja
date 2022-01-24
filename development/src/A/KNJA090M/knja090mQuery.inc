<?php

require_once('for_php7.php');

class knja090mQuery extends Query {

    //NO001
    function GetSort()
    {
        $query  = " SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'Z002' AND NAMESPARE1 IS NULL ";

        return $query;
    }

    function GetYearSeme() {
        $query  = " SELECT * ";
        $query .= " FROM SEMESTER_MST ";
        $query .= " WHERE YEAR IN ( '".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "   AND SEMESTER <> '9' ";
        return $query;
    }


    //[3] 対象クラスコンボ
    function GetClass($model, $year, $semester)
    {
        $query  = " SELECT ";
        $query .= "   T1.GRADE ";
        $query .= "   , T1.HR_CLASS ";
        $query .= "   , T2.HR_NAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT AS T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT AS T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T1.GRADE = T2.GRADE ";
        $query .= "     AND T1.HR_CLASS = T2.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   INNER JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "     ON T3.YEAR = T1.YEAR ";
                $query .= "     AND T3.GRADE = T1.GRADE ";
                $query .= "     AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "     ON T3.YEAR = T1.YEAR ";
            $query .= "     AND T3.GRADE = T1.GRADE ";
            $query .= "     AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$year."' ";
        $query .= "   AND T1.SEMESTER = '".$semester."' ";
        $query .= " GROUP BY ";
        $query .= "   T1.GRADE ";
        $query .= "   , T1.HR_CLASS ";
        $query .= "   , T2.HR_NAME ";
        $query .= " ORDER BY ";
        $query .= "   T1.GRADE ";
        $query .= "   , T1.HR_CLASS ";

        return $query;
    }
    
    //年組名取得
    function getHR_Name($model)
    {
        $tmp = explode("-",$model->grade_class);
        $query  = "SELECT HR_NAME FROM SCHREG_REGD_HDAT";
        $query .= " WHERE GRADE    = '".$tmp[0]."'";
        $query .= "   AND HR_CLASS = '".$tmp[1]."'";
        $query .= "   AND YEAR     = '".$model->control["new_year"]."'";
        $query .= "   AND SEMESTER = '".$model->control["new_semes"]."'";
        return $query;
    }
    
    //担任名取得
    function GetTeacher($grade_class, $year, $semester)
    {
        $query  = " SELECT ";
        $query .= "   T1.STAFFNAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "   STAFF_MST T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "     ON T1.STAFFCD = T2.TR_CD1 ";
        $query .= " WHERE T2.GRADE = '".substr($grade_class,0,2)."' ";
        $query .= "   AND T2.HR_CLASS = '".substr($grade_class,3,3)."' ";
        $query .= "   AND T2.YEAR = '".$year."' ";
        $query .= "   AND T2.SEMESTER = '".$semester."' ";
        return $query;
    }

    //学校情報
    function getNameMstZ010() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE NAMECD1='Z010' ";
        $query .= "   AND NAMECD2='00' ";
        return $query;
    }
    
    //生徒一覧
    function GetStudent($model)
    {

        $order = array("DESC", "ASC");
        //NO001
        if ($model->sorttype == "0"){
            $sex_order = array("DESC", "ASC");
        }else {
            $sex_order = array("ASC", "DESC");
        }

        $query  = " WITH REGD AS ( ";
        $query .= "   SELECT ";
        $query .= "     SCHREGNO ";
        $query .= "     , COURSECODE ";
        $query .= "     , ATTENDNO ";
        $query .= "     , COURSECD ";
        $query .= "     , MAJORCD ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".$model->control["new_year"]."' ";
        $query .= "     AND SEMESTER = '".$model->control["new_semes"]."' ";
        $query .= "     AND GRADE = '".substr($model->grade_class,0,2)."' ";
        $query .= "     AND HR_CLASS = '".substr($model->grade_class,3,3)."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   A.SCHREGNO ";
        $query .= "   , B.NAME_SHOW AS NAME ";
        $query .= "   , B.NAME_KANA AS KANA ";
        $query .= "   , TRANSLATE_KANA(B.NAME_KANA) AS T_KANA ";
        $query .= "   , B.SEX AS SEXCD ";
        $query .= "   , MEISYOU_GET(B.SEX, 'Z002', 1) AS SEX ";
        $query .= "   , A.ATTENDNO AS ATTENDNO ";
        $query .= "   , A.COURSECD || A.MAJORCD AS COURSEMAJORCD ";
        $query .= "   , A.COURSECD || A.MAJORCD || ' ' || D.COURSENAME || D.MAJORNAME AS COURSEMAJOR ";
        $query .= "   , A.COURSECODE ";
        $query .= "   , C.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "   REGD A ";
        $query .= "   INNER JOIN SCHREG_BASE_MST B ";
        $query .= "     ON A.SCHREGNO = B.SCHREGNO ";
        $query .= "   LEFT OUTER JOIN V_COURSECODE_MST C ";
        $query .= "     ON C.YEAR = '".$model->control["new_year"]."' ";
        $query .= "     AND C.COURSECODE = A.COURSECODE ";
        $query .= "   LEFT OUTER JOIN V_COURSE_MAJOR_MST D ";
        $query .= "     ON D.YEAR = '".$model->control["new_year"]."' ";
        $query .= "     AND A.COURSECD = D.COURSECD ";
        $query .= "     AND A.MAJORCD = D.MAJORCD ";

        switch ($model->s_id) {
            case "1";
                $query .= " ORDER BY  SCHREGNO ".$order[$model->sort[$model->s_id]]; break;
            case "2":
                $query .= " ORDER BY  ATTENDNO ".$order[$model->sort[$model->s_id]]; break;
            case "3":
                $query .= " ORDER BY  SEXCD ".$sex_order[$model->sort[$model->s_id]].", T_KANA ".$order[$model->sort[$model->s_id]]; break;
            case "4":
                $query .= " ORDER BY  T_KANA ".$order[$model->sort[$model->s_id]].", SEXCD ".$sex_order[$model->sort[$model->s_id]]; break;
            case "5":
                $query .= " ORDER BY  T_KANA ".$order[$model->sort[$model->s_id]].", SCHREGNO ".$order[$model->sort[$model->s_id]]; break;
        }

        return $query;
    }
    
    //更新 
    function &getUpdateQuery($model)
    {        
        if (get_count($model->schregno) == 0) {
            return false;
        }
        $db = Query::dbCheckOut();
        $class = explode("-",$model->grade_class);
        for ($i = 0; $i < get_count($model->schregno); $i++)
        {
            $data["ATTENDNO"][TEXT]     = sprintf("%03d",$model->attendno[$i]);

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $where  = " WHERE YEAR      = '".$model->control["new_year"]."'";
            $where .= "   AND SEMESTER  = '".$model->control["new_semes"]."'";
            $where .= "   AND GRADE     = '".$class[0]."'";
            $where .= "   AND HR_CLASS  = '".$class[1]."'"; 
            $where .= "   AND SCHREGNO  = '".$model->schregno[$i]."'"; 

            $query = Query::updateSQL($data, "SCHREG_REGD_DAT", $where);
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return true;
    }

    //課程学科取得
    function GetMajorCoursecd($year, $db) {
    
        $query  = " SELECT ";
        $query .= "     COURSENAME || '　' || MAJORNAME AS LABEL, ";
        $query .= "     COURSECD || MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        $opt = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
        return $opt;            
    }

    //コースコード取得
    function GetCourse($year, $db)
    {
        $opt = array();
        $query  = " SELECT COURSECODE, COURSECODENAME ";
        $query .= "   FROM V_COURSECODE_MST ";
        $query .= "  WHERE YEAR     = '".$year."'";
        $query .= "  ORDER BY COURSECODE ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECODENAME"], "value" => $row["COURSECODE"]);
        }
        return $opt;            
    }
    
    //コース設定
    function &GetSubUpdateQuery($model)
    {        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $array = explode(",", $model->selectdata);
        $class = explode("-", $model->grade_class);
        
        if ($model->selectdata != ""){
            $query  = "UPDATE SCHREG_REGD_DAT ";

            $query .= "   SET COURSECD   = '".substr($model->coursemajorcd1, 0, 1)."', ";
            $query .= "       MAJORCD    = '".substr($model->coursemajorcd1, 1, 3)."', ";
            $query .= "       COURSECODE = '".$model->course1."', ";
            $query .= "       REGISTERCD = '".STAFFCD."', ";
            $query .= "       UPDATED    = sysdate() ";
            $query .= " WHERE SCHREGNO IN (";

            $sp = "";
            for ($i = 0; $i < get_count($array); $i++ ){
                $query .= $sp."'".$array[$i]."'";
                $sp = ",";
            }
            $query .= ")  and year     = '".$model->control["new_year"]."'";
            $query .= "   and semester = '".$model->control["new_semes"]."'";
            $query .= "   and grade    = '".$class[0]."'";
            $query .= "   and hr_class = '".$class[1]."'"; 
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
