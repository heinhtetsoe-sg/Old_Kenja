<?php

require_once('for_php7.php');

class knjz401j_2Query extends Query
{
    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //１レコード取得
    public function getRow($grade, $subclasscd, $viewcd, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.*, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  AS CLASSCD";
        } else {
            $query .= "     T1.*, ";
            $query .= "     SUBSTR(VIEWCD,1,2) AS CLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".substr($subclasscd, 0, 2)."' AND ";
            $query .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
            $query .= "     CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     VIEWCD = '".$viewcd."' ";

        return $query;
    }

    //教育課程エラーチェック用
    public function getRow2($grade, $subclasscd, $viewcd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($subclasscd, 0, 2);
            $substr_subclasscd    = substr($subclasscd, 7, 6);
            $substr_school_kind   = substr($subclasscd, 3, 1);
            $substr_curriculum_cd = substr($subclasscd, 5, 1);
        }
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND AS CLASSCD";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$substr_classcd."' AND ";
            $query .= "     SCHOOL_KIND     = '".$substr_school_kind."' AND ";
            $query .= "     CURRICULUM_CD   = '".$substr_curriculum_cd."' AND ";
            $query .= "     SUBCLASSCD      = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$subclasscd."' AND ";
        }
        $query .= "     VIEWCD = '".$viewcd."' ";

        return $query;
    }

    //学年取得
    public function getGrade($model, $grade = "")
    {
        $query  = " WITH G_YEAR AS( ";
        $query .= "     SELECT ";
        $query .= "         MAX(YEAR) AS YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         SCHOOL_KIND ";
        $query .= "     FROM  ";
        $query .= "         SCHREG_REGD_GDAT ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     WHERE ";
            $query .= "         SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         GRADE, ";
        $query .= "         SCHOOL_KIND ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE || '：' || T1.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     G_YEAR T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        if ($grade) {
            $query .= "     AND T1.GRADE <> '".$grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND DESC, ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    //学校校種
    public function getSchoolKind($grade)
    {
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '" .$grade."' ";

        return $query;
    }

    //観点一覧
    public function getJViewNameList($grade, $model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     L1.CLASSNAME, ";
        $query .= "     L2.SUBCLASSNAME ";
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST L1 ON L1.CLASSCD = SUBSTR(T1.VIEWCD,1,2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                       AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                       AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND L2.CLASSCD       = T1.CLASSCD ";
            $query .= "                          AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //教科取得
    public function getClassMst($model)
    {
        $query  = " (SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  || ':' || CLASSNAME AS LABEL ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '：' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM  ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        if ($model->schoolName == "sundaikoufu") {
            $query .= "     CLASSCD <= '91' ";
        } else {
            $query .= "     CLASSCD <= '90' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE) ";
        $query .= " UNION ";
        $query .= " (SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     NAME1 || '-' || NAME2  AS VALUE, ";
            $query .= "     NAME1 || '-' || NAME2  || ':' AS LABEL ";
        } else {
            $query .= "     NAME1  AS VALUE, ";
            $query .= "     NAME1  || ':' AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='D044' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME2 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME2='".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE) ";

        return $query;
    }
    
    //NAME_MSTに存在するかどうか
    public function isNameMst044($classcd, $model)
    {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D044' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND NAME1 = '" . $class_array[0] . "'";
            $query .= "     AND NAME2 = '" . $class_array[1] . "'";
        } else {
            $query .= "     AND NAME1 = '" . $classcd . "'";
        }
        
        return $query;
    }

    //科目取得
    public function getSubClassMst($classcd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '：' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || '：' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM  ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD          = '".$class_array[0]."' ";
            $query .= "     AND SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) = '{$classcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
        
    //科目取得044
    public function getSubClassMst044($classcd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     NAME1 || '-' || NAME2 || '-' || NAME3 || '-' || ABBV1 || '：' AS LABEL, ";
            $query .= "     NAME1 || '-' || NAME2 || '-' || NAME3 || '-' || ABBV1 AS VALUE ";
        } else {
            $query .= "     ABBV1 || '：' AS LABEL, ";
            $query .= "     ABBV1 AS VALUE ";
        }
        $query .= " FROM  ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1='D044' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND NAME1          = '".$class_array[0]."' ";
            $query .= "     AND NAME2      = '".$class_array[1]."' ";
        } else {
            $query .= "     AND SUBSTR(ABBV1,1,2) = '{$classcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //指導要録用科目取得
    public function getStudyrecSubcd($classcd, $grade, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '：' || L2.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     T1.SUBCLASSCD || '：' || L2.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_SUB_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND L2.CLASSCD = T1.CLASSCD ";
            $query .= "                          AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$class_array[1]."' AND ";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' AND ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$classcd."' AND ";
        }
        $query .= "     T1.SCHOOL_KIND IN ( SELECT DISTINCT ";
        $query .= "                             SCHOOL_KIND ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_REGD_GDAT ";
        $query .= "                         WHERE ";
        $query .= "                             GRADE = '".$grade."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //指導要録用観点取得
    public function getStudyrecViewcd($classcd, $subclasscd, $grade, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_subclasscd    = substr($subclasscd, 7, 6);
            $substr_school_kind   = substr($subclasscd, 3, 1);
            $substr_curriculum_cd = substr($subclasscd, 5, 1);
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        
        $query  = " SELECT ";
        $query .= "     VIEWCD AS VALUE, ";
        $query .= "     VIEWCD || '：' || VIEWNAME AS LABEL ";
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_SUB_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBCLASSCD     = '".$substr_subclasscd."' AND ";
            $query .= "     SCHOOL_KIND    = '".$substr_school_kind."' AND ";
            $query .= "     CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$subclasscd."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     SCHOOL_KIND      = '".$class_array[1]."' AND ";
            $query .= "     SUBSTR(VIEWCD,1,2) = '".$class_array[0]."' AND ";
        } else {
            $query .= "     SUBSTR(VIEWCD,1,2) = '".$classcd."' AND ";
        }
        $query .= "     SCHOOL_KIND IN (SELECT DISTINCT ";
        $query .= "                         SCHOOL_KIND ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_GDAT ";
        $query .= "                     WHERE ";
        $query .= "                         GRADE = '".$grade."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($grade, $fields, $model)
    {
        $db = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($fields["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($fields["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($fields["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($fields["SUBCLASSCD"], 7, 6);
            $studyrec_classcd        = substr($fields["STUDYREC_SUBCLASSCD"], 0, 2);
            $studyrec_school_kind    = substr($fields["STUDYREC_SUBCLASSCD"], 3, 1);
            $studyrec_curriculum_cd  = substr($fields["STUDYREC_SUBCLASSCD"], 5, 1);
            $studyrec_subclasscd     = substr($fields["STUDYREC_SUBCLASSCD"], 7, 6);
        }

        $data["GRADE"][TEXT]                = $grade;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]              = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]           = $fields["SUBCLASSCD"];
        }
        $data["VIEWCD"][TEXT]               = $fields["VIEWCD"];
        $data["VIEWNAME"][TEXT]             = $fields["VIEWNAME"];
        $data["VIEWABBV"][TEXT]             = $fields["VIEWABBV"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["STUDYREC_CLASSCD"][TEXT]         = $studyrec_classcd;
            $data["STUDYREC_SCHOOL_KIND"][TEXT]     = $studyrec_school_kind;
            $data["STUDYREC_CURRICULUM_CD"][TEXT]   = $studyrec_curriculum_cd;
            $data["STUDYREC_SUBCLASSCD"][TEXT]      = $studyrec_subclasscd;
        } else {
            $data["STUDYREC_SUBCLASSCD"][TEXT]  = $fields["STUDYREC_SUBCLASSCD"];
        }
        $data["STUDYREC_VIEWCD"][TEXT]      = $fields["STUDYREC_VIEWCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_MST");
        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //UPDATE
    public function &getUpdateQuery($grade, $fields, $model)
    {
        $db = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($fields["SUBCLASSCD"], 0, 2);
            $substr_school_kind     = substr($fields["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd   = substr($fields["SUBCLASSCD"], 5, 1);
            $substr_subclasscd      = substr($fields["SUBCLASSCD"], 7, 6);
            $studyrec_classcd       = substr($fields["STUDYREC_SUBCLASSCD"], 0, 2);
            $studyrec_school_kind   = substr($fields["STUDYREC_SUBCLASSCD"], 3, 1);
            $studyrec_curriculum_cd = substr($fields["STUDYREC_SUBCLASSCD"], 5, 1);
            $studyrec_subclasscd    = substr($fields["STUDYREC_SUBCLASSCD"], 7, 6);
        }

        $data["GRADE"][TEXT]            = $grade;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]              = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]           = $fields["SUBCLASSCD"];
        }
        $data["VIEWCD"][TEXT]           = $fields["VIEWCD"];
        $data["VIEWNAME"][TEXT]         = $fields["VIEWNAME"];
        $data["VIEWABBV"][TEXT]         = $fields["VIEWABBV"];
        $data["SHOWORDER"][NUMBER]      = $fields["SHOWORDER"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["STUDYREC_CLASSCD"][TEXT]         = $studyrec_classcd;
            $data["STUDYREC_SCHOOL_KIND"][TEXT]     = $studyrec_school_kind;
            $data["STUDYREC_CURRICULUM_CD"][TEXT]   = $studyrec_curriculum_cd;
            $data["STUDYREC_SUBCLASSCD"][TEXT]      = $studyrec_subclasscd;
        } else {
            $data["STUDYREC_SUBCLASSCD"][TEXT]  = $fields["STUDYREC_SUBCLASSCD"];
        }
        $data["STUDYREC_VIEWCD"][TEXT]  = $fields["STUDYREC_VIEWCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE GRADE         = '" .$fields["GRADE"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "       CLASSCD       = '" .$substr_classcd."' AND ";
            $where .= "       SCHOOL_KIND   = '" .$substr_school_kind."' AND ";
            $where .= "       CURRICULUM_CD = '" .$substr_curriculum_cd."' AND ";
            $where .= "       SUBCLASSCD    = '" .$substr_subclasscd."' AND ";
        } else {
            $where .= "       SUBCLASSCD    = '" .$fields["SUBCLASSCD"]."' AND ";
        }
        $where .= "       VIEWCD        = '" .$fields["VIEWCD"]."' ";

        $query = Query::updateSQL($data, "JVIEWNAME_GRADE_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);

        return ;
    }

    //DELETE
    public function &getDeleteQuery($grade, $fields, $model)
    {
        $db = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($fields["SUBCLASSCD"], 0, 2);
            $substr_school_kind   = substr($fields["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd = substr($fields["SUBCLASSCD"], 5, 1);
            $substr_subclasscd    = substr($fields["SUBCLASSCD"], 7, 6);
            $query = knjz401j_2Query::checkJViewYdata($grade, $substr_subclasscd, $substr_school_kind, $substr_curriculum_cd, $substr_classcd, $fields["VIEWCD"]);
        } else {
            $query = knjz401j_2Query::checkJViewYdata($grade, $fields["SUBCLASSCD"], $fields["VIEWCD"]);
        }
        $row = $db->getRow($query, null, DB_FETCHMODE_ASSOC);

        if ($row["CNT"] == 0) {
            $query  = " DELETE FROM JVIEWNAME_GRADE_MST ";
            $query .= " WHERE GRADE     = '".$grade."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       CLASSCD       = '".$substr_classcd."' AND ";
                $query .= "       SCHOOL_KIND   = '".$substr_school_kind."' AND ";
                $query .= "       CURRICULUM_CD = '".$substr_curriculum_cd."' AND ";
                $query .= "       SUBCLASSCD    = '".$substr_subclasscd."' AND ";
            } else {
                $query .= "       SUBCLASSCD    = '".$fields["SUBCLASSCD"]."' AND ";
            }
            $query .= "       VIEWCD    = '".$fields["VIEWCD"]."' ";

            $db->query($query);

            $result = true;
        } else {
            $result = false;
        }
        Query::dbCheckIn($db);

        return $result;
    }

    //コピー
    public function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " INSERT INTO JVIEWNAME_GRADE_MST ";
        $query .= " SELECT ";
        $query .= "     '".$model->grade."', ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      CLASSCD, ";
            $query .= "      SCHOOL_KIND, ";
            $query .= "      CURRICULUM_CD, ";
        }
        $query .= "      SUBCLASSCD, ";
        $query .= "      VIEWCD, ";
        $query .= "      VIEWNAME, ";
        $query .= "      VIEWABBV, ";
        $query .= "      SHOWORDER, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      STUDYREC_CLASSCD, ";
            $query .= "      STUDYREC_SCHOOL_KIND, ";
            $query .= "      STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "      STUDYREC_SUBCLASSCD, ";
        $query .= "      STUDYREC_VIEWCD, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE()";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$model->field["R_GRADE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //存在チェック
    public function checkJViewMst($grade)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' ";

        return $query;
    }

    //年度データチェック
    public function checkJViewYdata($model, $subclasscd, $viewcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$model->grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->classcd) {
                $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
                $query .= "     AND SCHOOL_KIND     = '".$model->school_kind."' ";
                $query .= "     AND CURRICULUM_CD   = '".$model->curriculum_cd."' ";
            }
        }
        if ($subclasscd) {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }
        if ($viewcd) {
            $query .= "     AND VIEWCD = '".$viewcd."' ";
        }

        return $query;
    }


    ///////////////////////////////////*　　以下、ＣＳＶ処理用　　*///////////////////////////////////////////

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data = array();
        $data["PROGRAMID"][TEXT] = PROGRAMID;
        $data["MSGROW"][NUMBER]  = $record_no;
        $data["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //ＣＳＶ出力データ取得
    public function getCsvData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        if ($model->Properties["useCurriculumcd"] != '1') {
            $query .= "     L1.CLASSCD, ";
        }
        $query .= "     L1.CLASSNAME, ";
        $query .= "     L2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     LEFT JOIN CLASS_MST L1 ON L1.CLASSCD     = T1.CLASSCD ";
            $query .= "                           AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        } else {
            $query .= "     LEFT JOIN CLASS_MST L1 ON L1.CLASSCD     = SUBSTR(T1.SUBCLASSCD,1,2) ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD      = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND L2.CLASSCD         = T1.CLASSCD ";
            $query .= "                              AND L2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                              AND L2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //学年チェック
    public function checkGrade($data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$data["GRADE"]."' ";

        return $query;
    }

    //科目チェック
    public function checkSubclasscd($model, $data)
    {
        $query .= " SELECT ";
        $query .= "     sum(CNT) ";
        $query .= " FROM ";
        $query .= " (SELECT ";
        $query .= "     COUNT(*) AS CNT";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$data["CLASSCD"]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$data["SCHOOL_KIND"]."' AND ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "    SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            }
            $query .= "     CURRICULUM_CD   = '".$data["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     SUBCLASSCD      = '".$data["SUBCLASSCD"]."' ";

        $query .= " UNION ";

        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D044' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     NAME1     = '".$data["CLASSCD"]."' AND ";
            $query .= "     NAME2     = '".$data["SCHOOL_KIND"]."' AND ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     NAME2 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     NAME2 = '".SCHOOLKIND."' AND ";
            }
            $query .= "     NAME3     = '".$data["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     ABBV1         = '".$data["SUBCLASSCD"]."' ";
        $query .= ") AS A";
        
        return $query;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 1; $j <= $data_arr[$i]["COL_NO"]; $j++) {
                //データ有無チェック
                $check = $db->getOne(knjz401j_2Query::checkJviewnameGradeMstExist($model, $data_arr[$i], $data_arr[$i]["VIEWCD".$j]));
                //データセット
                $data = array();
                if (1 > $check) {
                    $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                        $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                        $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                    }
                    $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                    $data["VIEWCD"][TEXT]           = $data_arr[$i]["VIEWCD".$j];
                }
                $data["VIEWNAME"][TEXT]         = $data_arr[$i]["VIEWNAME".$j];
                $data["VIEWABBV"][TEXT]         = $data_arr[$i]["VIEWABBV".$j];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                //データが一件もなければ、insertする
                if (1 > $check) {
                    $query = Query::insertSQL($data, "JVIEWNAME_GRADE_MST");
                } else {
                    $where  = " WHERE ";
                    $where .= "     GRADE           = '".$data_arr[$i]["GRADE"]."' AND ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "     CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' AND ";
                        $where .= "     SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                        $where .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
                    }
                    $where .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
                    $where .= "     VIEWCD          = '".$data_arr[$i]["VIEWCD".$j]."' ";

                    $query = Query::updateSQL($data, "JVIEWNAME_GRADE_MST", $where);
                }
                $db->query($query);
                $cnt++;
            }
        }
        return $cnt;
    }

    //データ有無チェック
    public function checkJviewnameGradeMstExist($model, $data_arr, $viewcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE           = '".$data_arr["GRADE"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$data_arr["CLASSCD"]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$data_arr["SCHOOL_KIND"]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$data_arr["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     SUBCLASSCD      = '".$data_arr["SUBCLASSCD"]."' AND ";
        $query .= "     VIEWCD          = '".$viewcd."' ";

        return $query;
    }
}
