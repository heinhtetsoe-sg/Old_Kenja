<?php

require_once('for_php7.php');

class knjz213Query extends Query {

    //学年
    function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校校種取得
    function getSchoolKind($model) {
        $query  = "SELECT ";
        $query .= "        SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "        SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "        GRADE = '".$model->leftField["GRADE"]."' ";
        $query .= "AND      YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //教科取得
    function getClass($model, $schoolkind)
    {
        $query  = "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || ':' || CLASSNAME AS LABEL ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
            $query .= "     CLASSNAME AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "        CLASS_MST ";
        if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "WHERE";
                $query .= "        SCHOOL_KIND = '".$schoolkind."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE";

        return $query;
    }
    
    //リスト
    function getList($model)
    {
        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ";
        }
        $query .= "     T1.SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME AS COMBINED_SUBCLASS_NAME, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       AS COMBINED_CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND   AS COMBINED_SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD AS COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     L1.ATTEND_SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.ATTEND_CLASSCD, ";
            $query .= "     L1.ATTEND_SCHOOL_KIND, ";
            $query .= "     L1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     L2.SUBCLASSNAME AS ATTEND_SUBCLASSCD_NAME, ";
        $query .= "     L1.WEIGHTING ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_WEIGHTING_GRADES_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND L1.GRADE = '".$model->leftField["GRADE"]."' ";
        $query .= "          AND T1.SUBCLASSCD = L1.COMBINED_SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      AND T1.CLASSCD         = L1.COMBINED_CLASSCD ";
            $query .= "      AND T1.SCHOOL_KIND     = L1.COMBINED_SCHOOL_KIND ";
            $query .= "      AND T1.CURRICULUM_CD   = L1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L1.ATTEND_SUBCLASSCD = L2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      AND L1.ATTEND_CLASSCD       = L2.CLASSCD ";
            $query .= "      AND L1.ATTEND_SCHOOL_KIND   = L2.SCHOOL_KIND ";
            $query .= "      AND L1.ATTEND_CURRICULUM_CD = L2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->leftField["CLASSCD"]) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD         = '".substr($model->leftField["CLASSCD"], 0, 2)."' ";
                $query .= "     AND T1.SCHOOL_KIND     = '".$model->schoolkind."' ";
            } else {
                $query .= "     AND substr(T1.SUBCLASSCD, 1, 2) = '".$model->leftField["CLASSCD"]."' ";
            }
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;

    }

    //読替先名称
    function getCombinedSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     SUBCLASSCD = '".$model->combined_subclasscd."' ";
        //教育課程対応 
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD         = '".$model->combined_classcd."' ";
            $query .= " AND SCHOOL_KIND     = '".$model->combined_schoolkind."' ";
            $query .= " AND CURRICULUM_CD   = '".$model->combined_curriculumcd."' ";
        }
        return $query;
    }

    //教科名称
    function getClassName($classCd)
    {
        $query  = " SELECT ";
        $query .= "     CLASSNAME ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD = '".$classCd."' ";

        return $query;
    }

    //読替元
    function getAttendSubclass($model)
    {
        $query .= " SELECT ";
        $query .= "     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME AS ATTEND_SUBCLASS_NAME, ";
        $query .= "     L1.COMBINED_SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME AS COMBINED_SUBCLASS_NAME, ";
        $query .= "     L1.WEIGHTING ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_WEIGHTING_GRADES_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND L1.GRADE = '".$model->grade."' ";
        $query .= "          AND T1.SUBCLASSCD = L1.ATTEND_SUBCLASSCD ";
        $query .= "          AND L1.COMBINED_SUBCLASSCD = '".$model->combined_subclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L1.COMBINED_CLASSCD = '".$model->combined_classcd."' ";
            $query .= "          AND L1.COMBINED_SCHOOL_KIND = '".$model->combined_schoolkind."' ";
            $query .= "          AND L1.COMBINED_CURRICULUM_CD = '".$model->combined_curriculumcd."' ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L1.COMBINED_SUBCLASSCD = L2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND L1.COMBINED_CLASSCD        = L2.CLASSCD ";
            $query .= "                          AND L1.COMBINED_SCHOOL_KIND    = L2.SCHOOL_KIND ";
            $query .= "                          AND L1.COMBINED_CURRICULUM_CD  = L2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND substr(T1.SUBCLASSCD, 1, 2) = '".substr($model->combined_subclasscd, 0, 2)."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND  T1.SCHOOL_KIND    = '".$model->combined_schoolkind."' ";
            $query .= "     AND  T1.CURRICULUM_CD  = '".$model->combined_curriculumcd."' ";
        }
        $query .= "     AND T1.SUBCLASSCD NOT IN ('".$model->combined_subclasscd."') ";
        $query .= " ORDER BY ";
        //教育課程対応 ※修正
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        return $query;
    }

    //--- UPDATE 
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        foreach ($model->field["SUBCLASSCD"] as $key => $val) {
            if ($model->field["WEIGHTING"][$key] == "") {
                continue;
            }
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["GRADE"][TEXT]                = $model->grade;
            $data["COMBINED_SUBCLASSCD"][TEXT]  = $model->combined_subclasscd;
            $data["ATTEND_SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"][$key];
            $data["WEIGHTING"][NUMBER]          = $model->field["WEIGHTING"][$key];
            //教育課程対応 ※修正
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["COMBINED_CLASSCD"][TEXT]                 = $model->combined_classcd;
                $data["ATTEND_CLASSCD"][TEXT]                   = substr($model->field["SUBCLASSCD"][$key],0,2);
                $data["COMBINED_SCHOOL_KIND"][TEXT]             = $model->combined_schoolkind;
                $data["ATTEND_SCHOOL_KIND"][TEXT]               = $model->combined_schoolkind; //COMBINEDと同一のものを登録
                $data["COMBINED_CURRICULUM_CD"][TEXT]           = $model->combined_curriculumcd;
                $data["ATTEND_CURRICULUM_CD"][TEXT]    = $model->combined_curriculumcd; //COMBINEDと同一のものを登録
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $query = Query::insertSQL($data, "SUBCLASS_WEIGHTING_GRADES_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_GRADES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '" .$model->grade."' ";
        $query .= "     AND COMBINED_SUBCLASSCD = '" .$model->combined_subclasscd."' ";
        //教育課程対応 ※修正
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND COMBINED_CLASSCD                = '" .$model->combined_classcd."' ";
            $query .= "     AND COMBINED_SCHOOL_KIND            = '" .$model->combined_schoolkind."' ";
            $query .= "     AND COMBINED_CURRICULUM_CD = '" .$model->combined_curriculumcd."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
