<?php

require_once('for_php7.php');

class knjs551Query extends Query {

    //年度取得
    function selectYearQuery() {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_SCHOOL_DAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     'YEAR' DESC ";
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

    //学年データ存在取得
    function getGradeCount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->field["YEAR"]."'";
        $query .= " AND     SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."'";

        return $query;
    }

    //UNIT_CLASS_LESSON_SCHOOL_DATのデータ件数取得
    function getUnitClassDataCount($model) {
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."'";
        $query .= " AND     YEAR            = '".$model->field["YEAR"]."'";

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

        return $query;
    }
    
    //UNIT_CLASS_LESSON_SCHOOL_DATの学年の数（カウント）取得
    function getUnitClassGradeCount($model, $subclasscd, $grade) {
    
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd    = substr($subclasscd, 0, 2);
            $subclasscd = substr($subclasscd, 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."'";
        $query .= " AND     YEAR            = '".$model->field["YEAR"]."'";
        $query .= " AND     CURRICULUM_CD   = '1'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND     CLASSCD      = '".$classcd."'";
        }
        $query .= " AND     SUBCLASSCD      = '".$subclasscd."'";
        $query .= " AND     SEMESTER        = '9'";
        $query .= " AND     GRADE           = '".$grade."'";
        $query .= " AND     TIME_DIV        = '1'";

        return $query;
    }
    
    //学校設定標準時数テキストボックス表示
    function setUnitClassNodataDat($model, $subclasscd, $grade) {
    
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd    = substr($subclasscd, 0, 2);
            $subclasscd = substr($subclasscd, 7, 6);
        }
    
        $query  = " SELECT ";
        $query .= "     GRADE || ':' || SUBCLASSCD AS STANDARD_TIME ";
        $query .= " FROM ";
        $query .= "     UNIT_STUDY_CLASS_DAT";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."'";
        $query .= " AND SUBCLASSCD  = '".$subclasscd."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND     CLASSCD      = '".$classcd."'";
        }
        $query .= " AND GRADE       = '".$grade."'";

        return $query;

    }
    
    //学校設定標準時数データ取得(データがある場合)
    function setUnitClassDat($model, $subclasscd, $grade) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd    = substr($subclasscd, 0, 2);
            $subclasscd = substr($subclasscd, 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     STANDARD_TIME ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."'";
        $query .= " AND     YEAR            = '".$model->field["YEAR"]."'";
        $query .= " AND     CURRICULUM_CD   = '1'";
        $query .= " AND     SUBCLASSCD      = '".$subclasscd."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND     CLASSCD      = '".$classcd."'";
        }
        $query .= " AND     SEMESTER        = '9'";
        $query .= " AND     GRADE           = '".$grade."'";
        $query .= " AND     TIME_DIV        = '1'";

        return $query;
    }
    
    /***************************************
    更新用
    ****************************************/
    
    //更新対象データのテーブル内のデータ有無を確認する
    function &getInsertDataCountQuery($model, $Scount, $Gcount) {
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."'";
        $query .= " AND     YEAR            = '".$model->field["YEAR"]."'";
        $query .= " AND     CURRICULUM_CD   = '1'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND     CLASSCD         = '".substr($model->optSubclass[$Scount]['value'], 0, 2)."' ";
            $query .= " AND     SUBCLASSCD      = '".substr($model->optSubclass[$Scount]['value'], 7, 6)."' ";
        } else {
            $query .= " AND     SUBCLASSCD      = '".$model->optSubclass[$Scount]['value']."' ";
        }
        $query .= " AND     SEMESTER        = '9'";
        $query .= " AND     GRADE           = '".$model->optGrade[$Gcount]['value']."' ";
        $query .= " AND     TIME_DIV        = '1'";

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
        } else {
            $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHOOL_KIND"];
            $data["YEAR"][TEXT]            = $model->field["YEAR"];
            $data["CURRICULUM_CD"][TEXT]   = '1';
            $data["SUBCLASSCD"][TEXT]      = $model->optSubclass[$Scount]['value'];
        }
        $data["SEMESTER"][TEXT]        = '9';
        $data["GRADE"][TEXT]           = $model->optGrade[$Gcount]['value'];
        $data["TIME_DIV"][TEXT]        = '1';
        $data["STANDARD_TIME"][TEXT]   = $model->updfield[$Scount]["STANDARD_TIME_".$Gcount];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::InsertSQL($data, "UNIT_CLASS_LESSON_SCHOOL_DAT");

        return $query;
    }
    
    function &getUpdateQuery($model, $Scount, $Gcount) {
        
        //対象データをupdate
        $data["STANDARD_TIME"][TEXT]   = $model->updfield[$Scount]["STANDARD_TIME_".$Gcount];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";
        $where                         = " WHERE ";
        $where                        .= " SCHOOL_KIND          = '".$model->field["SCHOOL_KIND"]."' ";
        $where                        .= " AND  YEAR            = '".$model->field["YEAR"]."' ";
        $where                        .= " AND  CURRICULUM_CD   = '1' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where                        .= " AND  CLASSCD         = '".substr($model->optSubclass[$Scount]['value'], 0, 2)."' ";
            $where                        .= " AND  SUBCLASSCD      = '".substr($model->optSubclass[$Scount]['value'], 7, 6)."' ";
        } else {
            $where                        .= " AND  SUBCLASSCD      = '".$model->optSubclass[$Scount]['value']."' ";
        }
        $where                        .= " AND  SEMESTER        = '9' ";
        $where                        .= " AND  GRADE           = '".$model->optGrade[$Gcount]['value']."' ";
        $where                        .= " AND  TIME_DIV        = '1' ";

        $query = Query::updateSQL($data, "UNIT_CLASS_LESSON_SCHOOL_DAT", $where);

        return $query;
    }
    
    /***************************************
    初期化用
    ****************************************/
    
    //対象年度の行事予定を削除する
    function getUnitClassDelQuery($model) {
        $query  = " DELETE FROM ";
        $query .= "     UNIT_CLASS_LESSON_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND             = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND     YEAR            = '".$model->field["YEAR"]."' ";
        $query .= "     AND     CURRICULUM_CD   = '1'";
        $query .= "     AND     SEMESTER        = '9'";
        $query .= "     AND     TIME_DIV        = '1'";
        
        return $query;
    }
    
    //学校の対象年度の行事予定を取得する（教務データ初期化用）
    function getUnitClassMstQuery($model) {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND             = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND     YEAR            = '".$model->field["YEAR"]."' ";
        $query .= "     AND     CURRICULUM_CD   = '1'";

        return $query;
    }
        
    //学校の対象年度の行事予定を教務データへInsertする
    function &setUnitMstCopyQuery($setUnitMstData, $model) {
    
            //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["YEAR"][TEXT]            = $model->field["YEAR"];
            $data["CLASSCD"][TEXT]         = substr($setUnitMstData["SUBCLASSCD"], 0, 2);
            $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHOOL_KIND"];
        } else {
            $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHOOL_KIND"];
            $data["YEAR"][TEXT]            = $model->field["YEAR"];
        }
        $data["CURRICULUM_CD"][TEXT]   = '1';
        $data["SUBCLASSCD"][TEXT]      = $setUnitMstData["SUBCLASSCD"];
        $data["SEMESTER"][TEXT]        = '9';
        $data["GRADE"][TEXT]           = $setUnitMstData["GRADE"];
        $data["TIME_DIV"][TEXT]        = '1';
        $data["STANDARD_TIME"][TEXT]   = $setUnitMstData["STANDARD_TIME"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";
                        
        $query = Query::InsertSQL($data, "UNIT_CLASS_LESSON_SCHOOL_DAT");
        return $query;
    }
    
}
?>
