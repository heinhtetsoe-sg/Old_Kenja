<?php

require_once('for_php7.php');

class knjz210kQuery extends Query {

    var $attendclasscd = array();

    //リスト表示
    function getListQuery($year, $cd, $mode = "", $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $cd);
        }
        $query  = " SELECT ";
        $query .= "   t1.type_group_cd, t1.type_group_name, T1.type_asses_cd ,";
        $query .= "   '第'||t1.grade||'学年' as grade_name, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "   t1.grade, t3.subclassname, t3.CLASSCD, t3.SCHOOL_KIND, t3.CURRICULUM_CD, t3.subclasscd ";
        } else {
	        $query .= "   t1.grade, t3.subclassname, t3.subclasscd ";
        }
        if ($mode != "data") {
            $query .= " ,t2.hr_class ";
            $query .= " FROM ";
            $query .= "   type_group_mst t1 ";
            $query .= "   LEFT OUTER JOIN type_group_hr_dat t2 ";
            $query .= "     ON t1.year=t2.year ";
            $query .= "        AND t1.type_group_cd=t2.type_group_cd, ";
        } else {
            $query .= " FROM ";
            $query .= "   type_group_mst t1, ";
        }
        $query .= "   v_subclass_mst t3 ";
        $query .= " WHERE ";
        $query .= "   t1.year='" . $year . "' ";
        $query .= "   AND t1.year=t3.year ";
        if ($cd != "000000") {
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "   AND t1.CLASSCD          = '".$subclass_array[0]."' ";
	            $query .= "   AND t1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
	            $query .= "   AND t1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
	            $query .= "   AND t1.SUBCLASSCD       = '".$subclass_array[3]."' ";
	        } else {
		        $query .= " AND t1.subclasscd='" . $cd . "' ";
	        }
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "   AND t1.CLASSCD		=t3.CLASSCD ";
	        $query .= "   AND t1.SCHOOL_KIND	=t3.SCHOOL_KIND ";
	        $query .= "   AND t1.CURRICULUM_CD	=t3.CURRICULUM_CD ";
        }
        $query .= "   AND t1.subclasscd=t3.subclasscd ";

        return $query;
    }
    //エディット表示
    function getEditQuery($year, $groupcd, $model)
    {
        $query  = " SELECT ";
        $query .= "   t1.type_group_cd, t1.type_group_name, ";
        $query .= "   t1.grade, t2.hr_class, ";
        $query .= "   t3.subclassname ";
        $query .= " FROM ";
        $query .= "   type_group_mst t1 ";
        $query .= "   LEFT OUTER JOIN type_group_hr_dat t2 ";
        $query .= "     ON t1.year=t2.year ";
        $query .= "        AND t1.type_group_cd=t2.type_group_cd, ";
        $query .= "   v_subclass_mst t3 ";
        $query .= " WHERE ";
        $query .= "   t1.year='" . $year . "' ";
        $query .= "   AND t1.year=t3.year ";
        $query .= "   AND t1.type_group_cd='" . $groupcd . "' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "   AND t1.CLASSCD		=t3.CLASSCD ";
	        $query .= "   AND t1.SCHOOL_KIND	=t3.SCHOOL_KIND ";
	        $query .= "   AND t1.CURRICULUM_CD	=t3.CURRICULUM_CD ";
        }
        $query .= "   AND t1.subclasscd=t3.subclasscd ";

        return $query;
    }

    //年度コピー確認
    function selectYearQuery($year, $table)
    {
        return "SELECT COUNT(*) FROM $table WHERE year='$year'";
    }
    //学年コンボ
    function getGradeQuery($year)
    {
        return "SELECT DISTINCT grade FROM schreg_regd_hdat WHERE year = '" .$year ."' ORDER BY grade";
    }

    //科目コンボ
    function getSubclassQuery($year, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "   	CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
	        $query .= "   	CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        } else {
	        $query .= "   	SUBCLASSCD AS VALUE, ";
	        $query .= "   	SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        }
        $query .= "  FROM ";
        $query .= "  	V_SUBCLASS_MST ";
        $query .= "  WHERE ";
        $query .= "  	YEAR = '" . $year . "' ";
        $query .= "  ORDER BY ";
        $query .= "  	VALUE ";
        
        return $query;
        //教育課程対応
        /*if ($model->Properties["useCurriculumcd"] == '1') {
	        return "SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, subclasscd, subclassname FROM v_subclass_mst WHERE year='" . $year . "' ORDER BY CLASSCD, SCHOOL_KIND, CURRICULUM_CD, subclasscd";
        } else {
	        return "SELECT subclasscd, subclassname FROM v_subclass_mst WHERE year='" . $year . "' ORDER BY subclasscd";
    	}*/
    }

    //類型評定コンボ
    function getAssesQuery($year)
    {
        return "SELECT DISTINCT type_asses_cd FROM TYPE_ASSES_MST WHERE year='" . $year . "' ORDER BY type_asses_cd";
    }

    //類型グループクラス取得
    function getGroupQuery($year, $groupcd, $grade, $subclasscd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $subclasscd);
        }
        $query  = " SELECT DISTINCT ";
//        $query .= "   t2.hr_name, t2.hr_class ";      2004/09/02 arakaki
        $query .= "   t2.hr_class||'：'||t2.hr_name AS hr_name, t2.hr_class ";
        $query .= " FROM ";
        $query .= "   type_group_mst t1 ";
        $query .= "   LEFT OUTER JOIN schreg_regd_hdat t2 ";
        $query .= "     ON t1.year=t2.year ";
        $query .= "        AND t1.grade=t2.grade ";
        $query .= "        AND t2.semester='" . CTRL_SEMESTER . "', ";
        $query .= "   type_group_hr_dat t3 ";
        $query .= " WHERE ";
        $query .= "   t1.year='$year' ";
        $query .= "   AND t1.year=t3.year ";
        $query .= "   AND t1.type_group_cd='$groupcd' ";
        $query .= "   AND t1.type_group_cd=t3.type_group_cd ";
        $query .= "   AND t1.grade='$grade' ";
        $query .= "   AND t1.grade=t3.grade ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND t1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "   AND t1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "   AND t1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "   AND t1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
	        $query .= "   AND t1.subclasscd='$subclasscd' ";
        }
        $query .= "   AND t2.hr_class=t3.hr_class ";
        $query .= " ORDER BY ";
        $query .= "     t2.hr_class ";
        
        return $query;
    }
    //クラス一覧取得
    function getClassQuery($year, $groupcd, $grade, $subclasscd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $subclasscd);
        }
        $query  = " SELECT DISTINCT ";
//        $query .= "     t1.hr_class, t1.hr_name ";        2004/09/02 arakaki
        $query .= "     t1.hr_class, t1.hr_class||'：'||t1.hr_name as hr_name ";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat t1 ";
        $query .= " WHERE ";
        $query .= "     not exists(SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     type_group_hr_dat t2, ";
        $query .= "                     type_group_mst t3 ";
        $query .= "                 WHERE ";
        $query .= "                     t2.year='$year' ";
        $query .= "                     AND t2.year=t3.year ";
        $query .= "                     AND t2.type_group_cd='$groupcd' ";
        $query .= "                     AND t2.type_group_cd=t3.type_group_cd ";
        $query .= "                     AND t2.grade=t1.grade ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   				AND t3.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "   				AND t3.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "   				AND t3.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "   				AND t3.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
	        $query .= "                 AND t3.subclasscd='$subclasscd' ";
        }
        $query .= "                     AND t2.hr_class=t1.hr_class ";
        $query .= "               ) ";
        $query .= "     AND t1.year = '$year' ";
        $query .= "     AND t1.semester = '". CTRL_SEMESTER . "' ";
        $query .= "     AND t1.grade = '$grade' ";
        $query .= " ORDER BY ";
        $query .= "     t1.hr_class ";
        
        return $query;
    }

    //更新時の重複チェック(cmd == 'add')
    function IsExisting($fields)
    {
        $selectvalue = explode(",", $fields["SELECTVALUE"]);
        
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     type_group_mst ";
        $query .= " WHERE ";
        $query .= "     year='" . $fields["YEAR"] . "' ";
        $query .= "     AND type_group_cd='" . $fields["TYPE_GROUP_CD"] . "' ";
        $flag = $db->getOne($query);
        
        if ($flag) {
            Query::dbCheckIn($db);
            return $flag;
        }
        if (is_array($selectvalue)) {
            for ($i = 0; $i < get_count($selectvalue); $i++)
            {
                $query  = " SELECT ";
                $query .= "     COUNT(*) ";
                $query .= " FROM ";
                $query .= "     type_group_hr_dat ";
                $query .= " WHERE ";
                $query .= "     year='" . $fields["YEAR"] . "' ";
                $query .= "     AND type_group_cd='" . $fields["TYPE_GROUP_CD"] . "' ";
                $query .= "     AND grade='" . $fields["GRADE"] . "' ";
                $query .= "     AND hr_class='" . $selectvalue[$i] . "' ";
                $flag = $db->getOne($query);
                if ($flag) {
                    break;
                }
            }
        }
        
        Query::dbCheckIn($db);
        return $flag;
    }

    //追加処理
    function &getInsertQuery($fields, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $fields["SUBCLASSCD"]);
        }
        $selectvalue = explode(",", $fields["SELECTVALUE"]);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        if (is_array($selectvalue)) {
            for ($i = 0; $i < get_count($selectvalue); $i++)
            {
                $data["YEAR"][TEXT]             = $fields["YEAR"];
                $data["TYPE_GROUP_CD"][TEXT]    = $fields["TYPE_GROUP_CD"];
                $data["GRADE"][TEXT]            = $fields["GRADE"];
                $data["HR_CLASS"][TEXT]         = $selectvalue[$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                
                $query = Query::insertSQL($data, "type_group_hr_dat");
                $db->query($query);
            }
            
            $mst["YEAR"][TEXT]              = $fields["YEAR"];
            $mst["TYPE_GROUP_CD"][TEXT]     = $fields["TYPE_GROUP_CD"];
            $mst["GRADE"][TEXT]             = $fields["GRADE"];
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $mst["CLASSCD"][TEXT]        = $subclass_array[0];
	            $mst["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
	            $mst["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
	            $mst["SUBCLASSCD"][TEXT]     = $subclass_array[3];
	        } else {
	            $mst["SUBCLASSCD"][TEXT]        = $fields["SUBCLASSCD"];
            }
            $mst["TYPE_GROUP_NAME"][TEXT]   = $fields["TYPE_GROUP_NAME"];
            $mst["TYPE_ASSES_CD"][TEXT]     = $fields["TYPE_ASSES_CD"];
            $mst["REGISTERCD"][TEXT]        = STAFFCD;
            $mst["UPDATED"][FUNC]           = "sysdate()";
            
            $query = Query::insertSQL($mst, "type_group_mst");
            $db->query($query);
            $db->commit();
        }

        $db->autoCommit(true);

        Query::dbCheckIn($db);
        return;
    }

    //削除処理
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $selectvalue = explode(",", $fields["SELECTVALUE"]);
        
        if (is_array($selectvalue)) {
            $query  = " DELETE ";
            $query .= " FROM ";
            $query .= "     type_group_hr_dat ";
            $query .= " WHERE ";
            $query .= "     year = '" . $fields["YEAR"] . "' ";
            $query .= "     AND type_group_cd = '" . $fields["TYPE_GROUP_CD"] . "' ";
            
            $db->query($query);
            
            $query  = " DELETE ";
            $query .= " FROM ";
            $query .= "     type_group_mst ";
            $query .= " WHERE ";
            $query .= "     year = '" . $fields["YEAR"] . "' ";
            $query .= "     AND type_group_cd = '" . $fields["TYPE_GROUP_CD"] . "' ";
            
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return;
    }
    
    function &getCopyYearQuery($year, $model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "INSERT INTO type_group_mst(";
        $query .= " year,";
        $query .= " type_group_cd,";
        $query .= " grade,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= " subclasscd,";
        $query .= " type_group_name,";
        $query .= " type_asses_cd,";
        $query .= " registercd,";
        $query .= " updated)";
        $query .= " SELECT";
        $query .= " '$year',";
        $query .= " type_group_cd,";
        $query .= " grade,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= " subclasscd,";
        $query .= " type_group_name,";
        $query .= " type_asses_cd,";
        $query .= " '" .STAFFCD. "', sysdate()";
        $query .= " FROM type_group_mst";
        $query .= " WHERE ";
        $query .= "     year='" . ($year - 1) . "' ";

        $db->query($query);


        $query  = "INSERT INTO type_group_hr_dat(";
        $query .= " year,";
        $query .= " type_group_cd,";
        $query .= " grade,";
        $query .= " hr_class,";
        $query .= " registercd,";
        $query .= " updated)";
        $query .= " SELECT";
        $query .= " '$year',";
        $query .= " type_group_cd,";
        $query .= " grade,";
        $query .= " hr_class,";
        $query .= " '" .STAFFCD. "', sysdate()";
        $query .= " FROM type_group_hr_dat";
        $query .= " WHERE ";
        $query .= "     year='" . ($year - 1) . "' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
//以下コメントアウト、必要なし？
#        $query  = " INSERT INTO ";
#        $query .= "     type_group_mst ";
#        $query .= " SELECT ";
#        $query .= "     '$year', type_group_cd, grade, ";
#        $query .= "     subclasscd, type_group_name,  type_asses_cd ";
#        $query .= "     registercd, sysdate() ";
#        $query .= " FROM ";
#        $query .= "     type_group_mst ";
#        $query .= " WHERE ";
#        $query .= "     year='" . ($year - 1) . "' ";

#        $db->query($query);


#        $query  = " INSERT INTO ";
#        $query .= "     (type_group_hr_dat ";
#        $query .= " SELECT ";
#        $query .= "     '$year', type_group_cd, grade, ";
#        $query .= "     hr_class, average, ";
#        $query .= "     judge_pattern, judge_semester, ";
#        $query .= "     registercd, sysdate() ";
#        $query .= " FROM ";
#        $query .= "     type_group_hr_dat ";
#        $query .= " WHERE ";
#        $query .= "     year='" . ($year - 1) . "' ";

#        $db->query($query);

    }
}
?>
