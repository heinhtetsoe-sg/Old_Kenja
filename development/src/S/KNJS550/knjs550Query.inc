<?php

require_once('for_php7.php');

class knjs550Query extends Query {

    //年度取得
    function selectYearQuery() {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学校区分
    function getSchoolkind($model) {
        $query  = " SELECT ";
        $query .= "     T1.NAME1 || ':' || T1.ABBV1 AS LABEL, ";
        $query .= "     T1.NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.NAME1 IN (SELECT ";
                $query .= "                             REGD_G.SCHOOL_KIND ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.NAME1 IN (SELECT ";
            $query .= "                             REGD_G.SCHOOL_KIND ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";
        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->field["YEAR"]."'";
        $query .= " AND     SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."'";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";
        return $query;
    }

    //UNIT_STUDY_CLASS_DATの教科情報を取得
    function getSubclass($model)    
    {
        $query  = "SELECT DISTINCT";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBD.SUBCLASSABBV AS LABEL, ";
            $query .= "     UNIT.CLASSCD || '-' || UNIT.SCHOOL_KIND || '-' || UNIT.CURRICULUM_CD || '-' || UNIT.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBD.SUBCLASSABBV AS LABEL, ";
            $query .= "     UNIT.SUBCLASSCD AS VALUE";
        }
        $query .= " FROM ";
        $query .= "     UNIT_STUDY_CLASS_DAT UNIT ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = UNIT.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     UNIT.YEAR   = '".$model->field["YEAR"]."'";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT RG";
        $query .= "                 WHERE ";
        $query .= "                     RG.YEAR  = '".$model->field["YEAR"]."'";
        $query .= "                     AND RG.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."'";
        $query .= "                     AND UNIT.GRADE  = RG.GRADE";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //UNIT_STUDY_CLASS_DAT（カウント）取得
    function getUnitStudyCount($model, $subclasscd, $grade) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclasscd = substr($subclasscd, 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_STUDY_CLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->field["YEAR"]."'";
        $query .= "     AND GRADE           = '".$grade."'";
        $query .= "     AND SUBCLASSCD      = '".$subclasscd."'";

        return $query;
    }

    //学校設定標準時数データ取得(データがある場合)
    function setUnitClassDat($model, $subclasscd, $grade) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclasscd = substr($subclasscd, 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     STANDARD_TIME ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."'";
        $query .= " AND     YEAR            = '".$model->field["YEAR"]."'";
        $query .= " AND     CURRICULUM_CD   = '1'";
        $query .= " AND     SUBCLASSCD      = '".$subclasscd."'";
        $query .= " AND     GRADE           = '".$grade."'";

        return $query;
    }

    function getUnitClassDelQuery($model, $Gcount) {
        $query  = " DELETE FROM ";
        $query .= "     UNIT_CLASS_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND         = '".$model->field["SCHOOL_KIND"]."'";
        $query .= "     AND YEAR            = '".$model->field["YEAR"]."'";
        $query .= "     AND CURRICULUM_CD   = '1'";
        $query .= "     AND GRADE           = '".$model->optGrade[$Gcount]['value']."'";

        return $query;
    }

    function &getInsertQuery($model, $Scount, $Gcount) {
        //対象データをinsert
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["YEAR"][TEXT]            = $model->field["YEAR"];
            $data["CLASSCD"][TEXT]         = substr($model->optSubclass[$Scount]['value'], 0, 2);
            $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]   = '1';
            $data["SUBCLASSCD"][TEXT]      = substr($model->optSubclass[$Scount]['value'], 7, 6);
            $data["GRADE"][TEXT]           = $model->optGrade[$Gcount]['value'];
            $data["STANDARD_TIME"][TEXT]   = $model->updfield[$Scount]["STANDARD_TIME_".$Gcount];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";
        } else {
            $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHOOL_KIND"];
            $data["YEAR"][TEXT]            = $model->field["YEAR"];
            $data["CURRICULUM_CD"][TEXT]   = '1';
            $data["SUBCLASSCD"][TEXT]      = $model->optSubclass[$Scount]['value'];
            $data["GRADE"][TEXT]           = $model->optGrade[$Gcount]['value'];
            $data["STANDARD_TIME"][TEXT]   = $model->updfield[$Scount]["STANDARD_TIME_".$Gcount];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";
        }
        
        $query = Query::InsertSQL($data, "UNIT_CLASS_LESSON_MST");

        return $query;
    }

    //UNIT_STUDY_CLASS_MST（カウント）チェック
    function getLessonMstCount($model, $year) {
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."'";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."'";
        
        return $query;
    }
    
    //昨年度のデータ取得
    function getLessonMstDate($model, $year) {
    
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."'";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."'";
        
        return $query;
    }
    
    //SCHREG_REGD_GDATに登録されているデータの存在チェック
    function getGradeCount($model, $row) {
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->field["YEAR"]."'";
        $query .= " AND     GRADE        = '".$row["GRADE"]."'";
        $query .= " AND     SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."'";
        
        return $query;
    }
    
    //UNIT_STUDY_CLASS_DATに登録されているデータの存在チェック
    function getDataCheckCount($model, $row) {
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_STUDY_CLASS_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->field["YEAR"]."'";
        $query .= " AND     GRADE        = '".$row["GRADE"]."'";
        $query .= " AND     SUBCLASSCD   = '".$row["SUBCLASSCD"]."'";

        return $query;
    }
    
    //前年度よりチェック条件に合致するデータのみ登録
    function &getCopyQuery($model, $row) {
        //対象データをinsert
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["YEAR"][TEXT]            = $model->field["YEAR"];
            $data["CLASSCD"][TEXT]         = substr($row["SUBCLASSCD"], 0, 2);
            $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]   = '1';
            $data["SUBCLASSCD"][TEXT]      = $row["SUBCLASSCD"];
            $data["GRADE"][TEXT]           = $row["GRADE"];
            $data["STANDARD_TIME"][TEXT]   = $row["STANDARD_TIME"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";
        } else {            
            $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHOOL_KIND"];
            $data["YEAR"][TEXT]            = $model->field["YEAR"];
            $data["CURRICULUM_CD"][TEXT]   = '1';
            $data["SUBCLASSCD"][TEXT]      = $row["SUBCLASSCD"];
            $data["GRADE"][TEXT]           = $row["GRADE"];
            $data["STANDARD_TIME"][TEXT]   = $row["STANDARD_TIME"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";
        }
        
        $query = Query::InsertSQL($data, "UNIT_CLASS_LESSON_MST");

        return $query;
    }
    
}
?>
