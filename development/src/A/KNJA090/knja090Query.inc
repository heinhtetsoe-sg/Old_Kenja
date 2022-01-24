<?php

require_once('for_php7.php');

class knja090Query extends Query {

    //NO001
    function GetSort()
    {
        $query  = " SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'Z002' AND NAMESPARE1 IS NULL ";

        return $query;
    }
    
    //[3] 対象クラスコンボ
    function GetClass($model, $year, $semester)
    {
        $query  = "select T1.GRADE,T1.HR_CLASS,T2.HR_NAME";
        $query .= "  from CLASS_FORMATION_DAT AS T1 ";
        $query .= "  inner join SCHREG_REGD_HDAT AS T2 ON T2.YEAR=T1.YEAR"; 
        $query .= "   and T2.SEMESTER=T1.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
            $query .= "     AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " where T1.YEAR='".$year."'";
        $query .= "   and T1.SEMESTER='".$semester."'";
        $query .= "   and T1.GRADE=T2.GRADE";
        $query .= "   and T1.HR_CLASS=T2.HR_CLASS";
        $query .= " group by T1.GRADE,T1.HR_CLASS,T2.HR_NAME";
        $query .= " order by T1.GRADE,T1.HR_CLASS";

        return $query;
    }
    
    //年組名取得
    function getHR_Name($model)
    {
        $tmp = explode("-",$model->grade_class);
        $query  = "SELECT hr_name FROM schreg_regd_hdat";
        $query .= " WHERE grade    = '".$tmp[0]."'";
        $query .= "   AND hr_class = '".$tmp[1]."'";
        $query .= "   AND year     = '".$model->control["new_year"]."'";
        $query .= "   AND semester = '".$model->control["new_semes"]."'";
        return $query;
    }
    
    //担任名取得
    function GetTeacher($grade_class, $year, $semester)
    {
        $query  = "select t1.STAFFNAME_SHOW as name";
        $query .= "     from staff_mst t1";
        $query .= "        ,schreg_regd_hdat t2";
        $query .= " where t1.staffcd = t2.tr_cd1";
        $query .= "   AND t2.grade    = '".substr($grade_class,0,2)."'";
        $query .= "   AND t2.hr_class = '".substr($grade_class,3,3)."'";
        $query .= "   and t2.year     = '".$year."'";
        $query .= "   and t2.semester = '".$semester."'";
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
        $query .= " WHERE ";
        $query .= "     NAMECD1='Z010' AND ";
        $query .= "     NAMECD2='00' ";
        return $query;
    }
    
    //生徒一覧
    function GetStudent($model)
    {

        $order = array("desc", "asc");
        //NO001
        if ($model->sorttype == "0"){
            $sex_order = array("desc", "asc");
        }else {
            $sex_order = array("asc", "desc");
        }

        $query .= "SELECT a.schregno";
        $query .= "      ,b.name_show as name";
        $query .= "      ,b.name_kana as kana";
        if ('KINDAI' == $model->schoolName || 'KINJUNIOR' == $model->schoolName) {
            $query .= "      ,b.name_kana as t_kana";
        } else {
            $query .= "      ,TRANSLATE_KANA(b.name_kana) as t_kana";
        }
        $query .= "      ,b.sex as sexcd";
        $query .= "      ,MEISYOU_GET(b.sex,'Z002',1) as sex";
        $query .= "      ,a.attendno as attendno";
        $query .= "      ,a.coursecd || a.majorcd AS COURSEMAJORCD ";
        $query .= "      ,a.coursecd || a.majorcd || ' ' || d.coursename || d.majorname as coursemajor";
        $query .= "      ,a.coursecode ";
        $query .= "      ,c.coursecodename ";
        $query .= "      ,a.old_attendno ";
        $query .= "  FROM (SELECT schregno,coursecode,attendno, coursecd, majorcd, old_attendno";
        $query .= "          FROM class_formation_dat";
        $query .= "         WHERE year     = '".$model->control["new_year"]."'";
        $query .= "           AND semester = '".$model->control["new_semes"]."'";
        $query .= "           AND grade    = '".substr($model->grade_class,0,2)."'";
        $query .= "           AND hr_class = '".substr($model->grade_class,3,3)."'";
        $query .= "       ) a LEFT OUTER JOIN v_coursecode_mst c ";
        $query .= "             ON c.year       = '".$model->control["new_year"]."'";
        $query .= "            AND c.coursecode = a.coursecode ";
        $query .= "           LEFT OUTER JOIN v_course_major_mst d";
        $query .= "             ON d.coursecd || d.majorcd = a.coursecd || a.majorcd";
        $query .= "      ,schreg_base_mst b";
        $query .= " WHERE a.schregno = b.schregno";
        $query .= " UNION ";
        $query .= "SELECT a.schregno";
        $query .= "      ,b.NAME as name";
        $query .= "      ,b.NAME_KANA as kana";
        if ('KINDAI' == $model->schoolName || 'KINJUNIOR' == $model->schoolName) {
            $query .= "      ,b.name_kana as t_kana";
        } else {
            $query .= "      ,TRANSLATE_KANA(b.name_kana) as t_kana";
        }        
        $query .= "      ,b.sex as sexcd";
        $query .= "      ,MEISYOU_GET(b.sex,'Z002',1) as sex";
        $query .= "      ,a.attendno as attendno";
        $query .= "      ,a.coursecd || a.majorcd AS COURSEMAJORCD ";
        $query .= "      ,a.coursecd || a.majorcd || ' ' || d.coursename || d.majorname as coursemajor";
        $query .= "      ,a.coursecode";
        $query .= "      ,c.coursecodename";
        $query .= "      ,a.old_attendno ";
        $query .= "  FROM (SELECT schregno,coursecode,attendno,coursecd,majorcd, old_attendno";
        $query .= "          FROM class_formation_dat";
        $query .= "         WHERE year     = '".$model->control["new_year"]."'";
        $query .= "           AND semester = '".$model->control["new_semes"]."'";
        $query .= "           AND grade    = '".substr($model->grade_class,0,2)."'";
        $query .= "           AND hr_class = '".substr($model->grade_class,3,3)."'";
        $query .= "       ) a LEFT OUTER JOIN v_coursecode_mst c";
        $query .= "             ON c.YEAR       = '".$model->control["new_year"]."'";
        $query .= "            AND c.coursecode = a.coursecode ";
        $query .= "           LEFT OUTER JOIN v_course_major_mst d";
        $query .= "             ON d.coursecd || d.majorcd = a.coursecd || a.majorcd";
        $query .= "     ,freshman_dat b";
        $query .= " WHERE a.schregno=b.schregno";
        $query .= "   AND NOT EXISTS (SELECT 'x' FROM schreg_base_mst w1 WHERE w1.schregno=a.schregno)";//既に登録されている生徒名を重複しないようにする(2行ずつ表示されるのを防ぐ）

        switch ($model->s_id)
        {
            case "1";
//NO001
//              $query .= " ORDER BY  sexcd ".$order[$model->sort[$model->s_id]].", kana ".$order[$model->sort[$model->s_id]]; break;
                $query .= " ORDER BY  sexcd ".$sex_order[$model->sort[$model->s_id]].", t_kana ".$order[$model->sort[$model->s_id]]; break;
            case "2":
//NO001
//              $query .= " ORDER BY  kana ".$order[$model->sort[$model->s_id]].", sexcd ".$order[$model->sort[$model->s_id]]; break;
                $query .= " ORDER BY  t_kana ".$order[$model->sort[$model->s_id]].", sexcd ".$sex_order[$model->sort[$model->s_id]]; break;
            case "3":
                $query .= " ORDER BY  attendno ".$order[$model->sort[$model->s_id]]; break;
            case "4":
                $query .= " ORDER BY  t_kana ".$order[$model->sort[$model->s_id]].", schregno ".$order[$model->sort[$model->s_id]]; break;
        }

        return $query;    
    }
    //ＨＲ割振り未処理生徒チェック
    function IsClass_NULL($model, $year, $semester)
    {
        $query  = "SELECT COUNT(*) FROM class_formation_dat ";
        $query .= " WHERE year = '".$year."' AND semester = '".$semester."' AND (hr_class IS NULL OR hr_class = '')";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '".$year."' AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (SELECT GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '".$year."' AND SCHOOL_KIND = '".SCHOOLKIND."') ";
        }
        if ($model->schoolName == 'ksogo') {
            if (CTRL_SEMESTER == $model->control["学期数"]) { //最終学期のとき
                $query .= " AND SCHREGNO NOT IN ( SELECT SCHREGNO ";
                $query .= "                         FROM CLASS_FORMATION_DAT ";
                $query .= "                        WHERE YEAR     = '".$year."' ";
                $query .= "                          AND SEMESTER = '1' ";
                $query .= "                          AND GRADE    = '09' ";
                $query .= "                     ) ";
            }
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
            $data["attendno"][TEXT]     = sprintf("%03d",$model->attendno[$i]);

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $where  = " WHERE YEAR      = '".$model->control["new_year"]."'";
            $where .= "   AND SEMESTER  = '".$model->control["new_semes"]."'";
            $where .= "   AND GRADE     = '".$class[0]."'";
            $where .= "   AND HR_CLASS  = '".$class[1]."'"; 
            $where .= "   AND schregno  = '".$model->schregno[$i]."'"; 

            $query = Query::updateSQL($data, "class_formation_dat", $where);

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
            $query  = "update class_formation_dat ";

            $query .= "   set COURSECD   = '".substr($model->coursemajorcd1, 0, 1)."', ";
            $query .= "       MAJORCD    = '".substr($model->coursemajorcd1, 1, 3)."', ";
            $query .= "       COURSECODE = '".$model->course1."', ";
            $query .= "       REGISTERCD = '".STAFFCD."', ";
            $query .= "       UPDATED    = sysdate() ";
            $query .= " where schregno IN (";
            for ($i = 0; $i < get_count($array); $i++ ){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query .= "'".$array[$i]."'".$sp;    
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
