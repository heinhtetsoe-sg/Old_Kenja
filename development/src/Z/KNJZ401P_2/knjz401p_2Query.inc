<?php

require_once('for_php7.php');

class knjz401p_2Query extends Query
{

    //学期取得
    public function getSemester($semester = "")
    {
        $query .= " SELECT ";
        if ($semester != "") {
            $query .= "     SEMESTERNAME ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";
        if ($semester) {
            $query .= " AND SEMESTER = '".$semester."' ";
        }
        return $query;
    }
        
    //１レコード取得
    public function getRow($grade, $subclasscd, $viewcd, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.*, ";
            $query .= "     L4.REMARK1 AS RECORD_DIV, ";
            $query .= "     L1.REMARK1 AS VIEWNAME1, ";
            $query .= "     L1.REMARK2 AS VIEWABBV1, ";
            $query .= "     L1.REMARK3 AS SHOWORDER1, ";
            $query .= "     L1.REMARK4 AS STUDYREC_CLASSCD1, ";
            $query .= "     L1.REMARK5 AS STUDYREC_SCHOOL_KIND1, ";
            $query .= "     L1.REMARK6 AS STUDYREC_CURRICULUM_CD1, ";
            $query .= "     L1.REMARK7 AS STUDYREC_SUBCLASSCD1, ";
            $query .= "     L1.REMARK8 AS STUDYREC_VIEWCD1, ";
            $query .= "     L2.REMARK1 AS VIEWNAME2, ";
            $query .= "     L2.REMARK2 AS VIEWABBV2, ";
            $query .= "     L2.REMARK3 AS SHOWORDER2, ";
            $query .= "     L2.REMARK4 AS STUDYREC_CLASSCD2, ";
            $query .= "     L2.REMARK5 AS STUDYREC_SCHOOL_KIND2, ";
            $query .= "     L2.REMARK6 AS STUDYREC_CURRICULUM_CD2, ";
            $query .= "     L2.REMARK7 AS STUDYREC_SUBCLASSCD2, ";
            $query .= "     L2.REMARK8 AS STUDYREC_VIEWCD2, ";
            if ($model->getSemesterCount > 2) {
                $query .= "     L3.REMARK1 AS VIEWNAME3, ";
                $query .= "     L3.REMARK2 AS VIEWABBV3, ";
                $query .= "     L3.REMARK3 AS SHOWORDER3, ";
                $query .= "     L3.REMARK4 AS STUDYREC_CLASSCD3, ";
                $query .= "     L3.REMARK5 AS STUDYREC_SCHOOL_KIND3, ";
                $query .= "     L3.REMARK6 AS STUDYREC_CURRICULUM_CD3, ";
                $query .= "     L3.REMARK7 AS STUDYREC_SUBCLASSCD3, ";
                $query .= "     L3.REMARK8 AS STUDYREC_VIEWCD3, ";
            }
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND  AS CLASSCD";
        } else {
            $query .= "     T1.*, ";
            $query .= "     L4.REMARK1 AS RECORD_DIV, ";
            $query .= "     L1.REMARK1 AS VIEWNAME1, ";
            $query .= "     L1.REMARK2 AS VIEWABBV1, ";
            $query .= "     L1.REMARK3 AS SHOWORDER1, ";
            $query .= "     L1.REMARK7 AS STUDYREC_SUBCLASSCD1, ";
            $query .= "     L1.REMARK8 AS STUDYREC_VIEWCD1, ";
            $query .= "     L2.REMARK1 AS VIEWNAME2, ";
            $query .= "     L2.REMARK2 AS VIEWABBV2, ";
            $query .= "     L2.REMARK3 AS SHOWORDER2, ";
            $query .= "     L2.REMARK7 AS STUDYREC_SUBCLASSCD2, ";
            $query .= "     L2.REMARK8 AS STUDYREC_VIEWCD2, ";
            if ($model->getSemesterCount > 2) {
                $query .= "     L3.REMARK1 AS VIEWNAME3, ";
                $query .= "     L3.REMARK2 AS VIEWABBV3, ";
                $query .= "     L3.REMARK3 AS SHOWORDER3, ";
                $query .= "     L3.REMARK7 AS STUDYREC_SUBCLASSCD3, ";
                $query .= "     L3.REMARK8 AS STUDYREC_VIEWCD3, ";
            }
            $query .= "     SUBSTR(T1.VIEWCD,1,2) AS CLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L4 ON L4.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND L4.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L4.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L4.VIEW_SEQ = '004' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L1 ON L1.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L1.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L1.VIEW_SEQ = '001' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L2 ON L2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND L2.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L2.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L2.VIEW_SEQ = '002' ";
        if ($model->getSemesterCount > 2) {
            $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L3 ON L3.GRADE = T1.GRADE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                            AND L3.CLASSCD = T1.CLASSCD ";
                $query .= "                                            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                                            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                                            AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                                            AND L3.VIEWCD = T1.VIEWCD ";
            $query .= "                                            AND L3.VIEW_SEQ = '003' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD         = '".substr($subclasscd, 0, 2)."' AND ";
            $query .= "     T1.SCHOOL_KIND     = '".$model->school_kind."' AND ";
            $query .= "     T1.CURRICULUM_CD   = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     T1.SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     T1.VIEWCD = '".$viewcd."' ";

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
        $query .= "     L4.REMARK1 AS RECORD_DIV, ";
        $query .= "     L1.REMARK1 AS VIEWNAME1, ";
        $query .= "     L1.REMARK2 AS VIEWABBV1, ";
        $query .= "     L1.REMARK3 AS SHOWORDER1, ";
        $query .= "     L1.REMARK4 AS STUDYREC_CLASSCD1, ";
        $query .= "     L1.REMARK5 AS STUDYREC_SCHOOL_KIND1, ";
        $query .= "     L1.REMARK6 AS STUDYREC_CURRICULUM_CD1, ";
        $query .= "     L1.REMARK7 AS STUDYREC_SUBCLASSCD1, ";
        $query .= "     L1.REMARK8 AS STUDYREC_VIEWCD1, ";
        $query .= "     L2.REMARK1 AS VIEWNAME2, ";
        $query .= "     L2.REMARK2 AS VIEWABBV2, ";
        $query .= "     L2.REMARK3 AS SHOWORDER2, ";
        $query .= "     L2.REMARK4 AS STUDYREC_CLASSCD2, ";
        $query .= "     L2.REMARK5 AS STUDYREC_SCHOOL_KIND2, ";
        $query .= "     L2.REMARK6 AS STUDYREC_CURRICULUM_CD2, ";
        $query .= "     L2.REMARK7 AS STUDYREC_SUBCLASSCD2, ";
        $query .= "     L2.REMARK8 AS STUDYREC_VIEWCD2, ";
        if ($model->getSemesterCount > 2) {
            $query .= "     L3.REMARK1 AS VIEWNAME3, ";
            $query .= "     L3.REMARK2 AS VIEWABBV3, ";
            $query .= "     L3.REMARK3 AS SHOWORDER3, ";
            $query .= "     L3.REMARK4 AS STUDYREC_CLASSCD3, ";
            $query .= "     L3.REMARK5 AS STUDYREC_SCHOOL_KIND3, ";
            $query .= "     L3.REMARK6 AS STUDYREC_CURRICULUM_CD3, ";
            $query .= "     L3.REMARK7 AS STUDYREC_SUBCLASSCD3, ";
            $query .= "     L3.REMARK8 AS STUDYREC_VIEWCD3, ";
        }
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L4 ON L4.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND L4.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L4.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L4.VIEW_SEQ = '004' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L1 ON L1.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L1.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L1.VIEW_SEQ = '001' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L2 ON L2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND L2.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L2.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L2.VIEW_SEQ = '002' ";
        if ($model->getSemesterCount > 2) {
            $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L3 ON L3.GRADE = T1.GRADE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                            AND L3.CLASSCD = T1.CLASSCD ";
                $query .= "                                            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                                            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                                            AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                                            AND L3.VIEWCD = T1.VIEWCD ";
            $query .= "                                            AND L3.VIEW_SEQ = '003' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD         = '".$substr_classcd."' AND ";
            $query .= "     T1.SCHOOL_KIND     = '".$substr_school_kind."' AND ";
            $query .= "     T1.CURRICULUM_CD   = '".$substr_curriculum_cd."' AND ";
            $query .= "     T1.SUBCLASSCD      = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$subclasscd."' AND ";
        }
        $query .= "     T1.VIEWCD = '".$viewcd."' ";

        return $query;
    }

    //学年取得
    public function getGrade($grade = "")
    {
        $query  = " WITH G_YEAR AS( ";
        $query .= "     SELECT ";
        $query .= "         MAX(YEAR) AS YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         SCHOOL_KIND ";
        $query .= "     FROM  ";
        $query .= "         SCHREG_REGD_GDAT ";
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
        $query .= "     D4.REMARK1 AS RECORD_DIV, ";
        $query .= "     L1.CLASSNAME, ";
        $query .= "     L2.SUBCLASSNAME, ";
        if ($model->getSemesterCount > 2) {
            $query .= "     D3.REMARK1 AS VIEWNAME3, ";
            $query .= "     D3.REMARK2 AS VIEWABBV3, ";
            $query .= "     D3.REMARK3 AS SHOWORDER3, ";
            $query .= "     D3.REMARK4 AS STUDYREC_CLASSCD3, ";
            $query .= "     D3.REMARK5 AS STUDYREC_SCHOOL_KIND3, ";
            $query .= "     D3.REMARK6 AS STUDYREC_CURRICULUM_CD3, ";
            $query .= "     D3.REMARK7 AS STUDYREC_SUBCLASSCD3, ";
            $query .= "     D3.REMARK8 AS STUDYREC_VIEWCD3, ";
        }
        $query .= "     D1.REMARK1 AS VIEWNAME1, ";
        $query .= "     D1.REMARK2 AS VIEWABBV1, ";
        $query .= "     D1.REMARK3 AS SHOWORDER1, ";
        $query .= "     D1.REMARK4 AS STUDYREC_CLASSCD1, ";
        $query .= "     D1.REMARK5 AS STUDYREC_SCHOOL_KIND1, ";
        $query .= "     D1.REMARK6 AS STUDYREC_CURRICULUM_CD1, ";
        $query .= "     D1.REMARK7 AS STUDYREC_SUBCLASSCD1, ";
        $query .= "     D1.REMARK8 AS STUDYREC_VIEWCD1, ";
        $query .= "     D2.REMARK1 AS VIEWNAME2, ";
        $query .= "     D2.REMARK2 AS VIEWABBV2, ";
        $query .= "     D2.REMARK3 AS SHOWORDER2, ";
        $query .= "     D2.REMARK4 AS STUDYREC_CLASSCD2, ";
        $query .= "     D2.REMARK5 AS STUDYREC_SCHOOL_KIND2, ";
        $query .= "     D2.REMARK6 AS STUDYREC_CURRICULUM_CD2, ";
        $query .= "     D2.REMARK7 AS STUDYREC_SUBCLASSCD2, ";
        $query .= "     D2.REMARK8 AS STUDYREC_VIEWCD2 ";
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST D4 ON D4.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND D4.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND D4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND D4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND D4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND D4.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND D4.VIEW_SEQ = '004' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST D1 ON D1.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND D1.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND D1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND D1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND D1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND D1.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND D1.VIEW_SEQ = '001' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST D2 ON D2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND D2.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND D2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND D2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND D2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND D2.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND D2.VIEW_SEQ = '002' ";
        if ($model->getSemesterCount > 2) {
            $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST D3 ON D3.GRADE = T1.GRADE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                            AND D3.CLASSCD = T1.CLASSCD ";
                $query .= "                                            AND D3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                                            AND D3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                                            AND D3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                                            AND D3.VIEWCD = T1.VIEWCD ";
            $query .= "                                            AND D3.VIEW_SEQ = '003' ";
        }
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
        $query  = " SELECT ";
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
        $query .= "     CLASSCD <= '90' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
            $studyrec_classcd1        = substr($fields["STUDYREC_SUBCLASSCD1"], 0, 2);
            $studyrec_school_kind1    = substr($fields["STUDYREC_SUBCLASSCD1"], 3, 1);
            $studyrec_curriculum_cd1  = substr($fields["STUDYREC_SUBCLASSCD1"], 5, 1);
            $studyrec_subclasscd1     = substr($fields["STUDYREC_SUBCLASSCD1"], 7, 6);
            $studyrec_classcd2        = substr($fields["STUDYREC_SUBCLASSCD2"], 0, 2);
            $studyrec_school_kind2    = substr($fields["STUDYREC_SUBCLASSCD2"], 3, 1);
            $studyrec_curriculum_cd2  = substr($fields["STUDYREC_SUBCLASSCD2"], 5, 1);
            $studyrec_subclasscd2     = substr($fields["STUDYREC_SUBCLASSCD2"], 7, 6);
            if ($model->getSemesterCount > 2) {
                $studyrec_classcd3        = substr($fields["STUDYREC_SUBCLASSCD3"], 0, 2);
                $studyrec_school_kind3    = substr($fields["STUDYREC_SUBCLASSCD3"], 3, 1);
                $studyrec_curriculum_cd3  = substr($fields["STUDYREC_SUBCLASSCD3"], 5, 1);
                $studyrec_subclasscd3     = substr($fields["STUDYREC_SUBCLASSCD3"], 7, 6);
            }
        }
        $data = array();
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
        //$data["VIEWNAME"][TEXT]             = $fields["VIEWNAME"];
        //$data["VIEWABBV"][TEXT]             = $fields["VIEWABBV"];
        /*$data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["STUDYREC_CLASSCD"][TEXT]         = $studyrec_classcd;
            $data["STUDYREC_SCHOOL_KIND"][TEXT]     = $studyrec_school_kind;
            $data["STUDYREC_CURRICULUM_CD"][TEXT]   = $studyrec_curriculum_cd;
            $data["STUDYREC_SUBCLASSCD"][TEXT]      = $studyrec_subclasscd;
        } else {
            $data["STUDYREC_SUBCLASSCD"][TEXT]  = $fields["STUDYREC_SUBCLASSCD"];
        }
        $data["STUDYREC_VIEWCD"][TEXT]      = $fields["STUDYREC_VIEWCD"];*/
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_MST");
        $db->query($query);

        //クラス形態
        $data = array();
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
        $data["VIEW_SEQ"][TEXT]             = "004";
        $data["REMARK1"][TEXT]              = $fields["RECORD_DIV"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
        $db->query($query);

        //名称1学期
        $data = array();
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
        $data["VIEW_SEQ"][TEXT]             = "001";
        $data["REMARK1"][TEXT]              = $fields["VIEWNAME1"];
        $data["REMARK2"][TEXT]              = $fields["VIEWABBV1"];
        $data["REMARK3"][TEXT]              = $fields["SHOWORDER1"];
        $data["REMARK4"][TEXT]              = $studyrec_classcd1      ;
        $data["REMARK5"][TEXT]              = $studyrec_school_kind1  ;
        $data["REMARK6"][TEXT]              = $studyrec_curriculum_cd1;
        $data["REMARK7"][TEXT]              = $studyrec_subclasscd1   ;
        $data["REMARK8"][TEXT]              = $fields["STUDYREC_VIEWCD1"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
        $db->query($query);
        
        //名称2学期
        $data = array();
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
        $data["VIEW_SEQ"][TEXT]             = "002";
        $data["REMARK1"][TEXT]              = $fields["VIEWNAME2"];
        $data["REMARK2"][TEXT]              = $fields["VIEWABBV2"];
        $data["REMARK3"][TEXT]              = $fields["SHOWORDER2"];
        $data["REMARK4"][TEXT]              = $studyrec_classcd2      ;
        $data["REMARK5"][TEXT]              = $studyrec_school_kind2  ;
        $data["REMARK6"][TEXT]              = $studyrec_curriculum_cd2;
        $data["REMARK7"][TEXT]              = $studyrec_subclasscd2   ;
        $data["REMARK8"][TEXT]              = $fields["STUDYREC_VIEWCD2"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
        $db->query($query);
        
        //3学期ある時
        if ($model->getSemesterCount > 2) {
            $data = array();
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
            $data["VIEW_SEQ"][TEXT]             = "003";
            $data["REMARK1"][TEXT]              = $fields["VIEWNAME3"];
            $data["REMARK2"][TEXT]              = $fields["VIEWABBV3"];
            $data["REMARK3"][TEXT]              = $fields["SHOWORDER3"];
            $data["REMARK4"][TEXT]              = $studyrec_classcd3      ;
            $data["REMARK5"][TEXT]              = $studyrec_school_kind3  ;
            $data["REMARK6"][TEXT]              = $studyrec_curriculum_cd3;
            $data["REMARK7"][TEXT]              = $studyrec_subclasscd3   ;
            $data["REMARK8"][TEXT]              = $fields["STUDYREC_VIEWCD3"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
            $db->query($query);
        }
        
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
            $studyrec_classcd1        = substr($fields["STUDYREC_SUBCLASSCD1"], 0, 2);
            $studyrec_school_kind1    = substr($fields["STUDYREC_SUBCLASSCD1"], 3, 1);
            $studyrec_curriculum_cd1  = substr($fields["STUDYREC_SUBCLASSCD1"], 5, 1);
            $studyrec_subclasscd1     = substr($fields["STUDYREC_SUBCLASSCD1"], 7, 6);
            $studyrec_classcd2        = substr($fields["STUDYREC_SUBCLASSCD2"], 0, 2);
            $studyrec_school_kind2    = substr($fields["STUDYREC_SUBCLASSCD2"], 3, 1);
            $studyrec_curriculum_cd2  = substr($fields["STUDYREC_SUBCLASSCD2"], 5, 1);
            $studyrec_subclasscd2     = substr($fields["STUDYREC_SUBCLASSCD2"], 7, 6);
            if ($model->getSemesterCount > 2) {
                $studyrec_classcd3        = substr($fields["STUDYREC_SUBCLASSCD3"], 0, 2);
                $studyrec_school_kind3    = substr($fields["STUDYREC_SUBCLASSCD3"], 3, 1);
                $studyrec_curriculum_cd3  = substr($fields["STUDYREC_SUBCLASSCD3"], 5, 1);
                $studyrec_subclasscd3     = substr($fields["STUDYREC_SUBCLASSCD3"], 7, 6);
            }
        }

        $data = array();
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
        //$data["VIEWNAME"][TEXT]         = $fields["VIEWNAME"];
        //$data["VIEWABBV"][TEXT]         = $fields["VIEWABBV"];
        /*$data["SHOWORDER"][NUMBER]      = $fields["SHOWORDER"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["STUDYREC_CLASSCD"][TEXT]         = $studyrec_classcd;
            $data["STUDYREC_SCHOOL_KIND"][TEXT]     = $studyrec_school_kind;
            $data["STUDYREC_CURRICULUM_CD"][TEXT]   = $studyrec_curriculum_cd;
            $data["STUDYREC_SUBCLASSCD"][TEXT]      = $studyrec_subclasscd;
        } else {
            $data["STUDYREC_SUBCLASSCD"][TEXT]  = $fields["STUDYREC_SUBCLASSCD"];
        }
        $data["STUDYREC_VIEWCD"][TEXT]  = $fields["STUDYREC_VIEWCD"];*/
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

        //名称1学期、2学期(もしくは3学期)の更新(Delete Insert)
        $query  = " DELETE FROM JVIEWNAME_GRADE_DETAIL_MST ";
        $query .= " WHERE GRADE         = '" .$fields["GRADE"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD       = '" .$substr_classcd."' AND ";
            $query .= "       SCHOOL_KIND   = '" .$substr_school_kind."' AND ";
            $query .= "       CURRICULUM_CD = '" .$substr_curriculum_cd."' AND ";
            $query .= "       SUBCLASSCD    = '" .$substr_subclasscd."' AND ";
        } else {
            $query .= "       SUBCLASSCD    = '" .$fields["SUBCLASSCD"]."' AND ";
        }
        $query .= "       VIEWCD        = '" .$fields["VIEWCD"]."' ";
        $db->query($query);
        
        //クラス形態
        $data = array();
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
        $data["VIEW_SEQ"][TEXT]             = "004";
        $data["REMARK1"][TEXT]              = $fields["RECORD_DIV"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
        $db->query($query);
        
        //名称1学期
        $data = array();
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
        $data["VIEW_SEQ"][TEXT]             = "001";
        $data["REMARK1"][TEXT]              = $fields["VIEWNAME1"];
        $data["REMARK2"][TEXT]              = $fields["VIEWABBV1"];
        $data["REMARK3"][TEXT]              = $fields["SHOWORDER1"];
        $data["REMARK4"][TEXT]              = $studyrec_classcd1      ;
        $data["REMARK5"][TEXT]              = $studyrec_school_kind1  ;
        $data["REMARK6"][TEXT]              = $studyrec_curriculum_cd1;
        $data["REMARK7"][TEXT]              = $studyrec_subclasscd1   ;
        $data["REMARK8"][TEXT]              = $fields["STUDYREC_VIEWCD1"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
        $db->query($query);
        
        //名称2学期
        $data = array();
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
        $data["VIEW_SEQ"][TEXT]             = "002";
        $data["REMARK1"][TEXT]              = $fields["VIEWNAME2"];
        $data["REMARK2"][TEXT]              = $fields["VIEWABBV2"];
        $data["REMARK3"][TEXT]              = $fields["SHOWORDER2"];
        $data["REMARK4"][TEXT]              = $studyrec_classcd2      ;
        $data["REMARK5"][TEXT]              = $studyrec_school_kind2  ;
        $data["REMARK6"][TEXT]              = $studyrec_curriculum_cd2;
        $data["REMARK7"][TEXT]              = $studyrec_subclasscd2   ;
        $data["REMARK8"][TEXT]              = $fields["STUDYREC_VIEWCD2"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
        $db->query($query);

        //3学期制のとき
        if ($model->getSemesterCount > 2) {
            $data = array();
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
            $data["VIEW_SEQ"][TEXT]             = "003";
            $data["REMARK1"][TEXT]              = $fields["VIEWNAME3"];
            $data["REMARK2"][TEXT]              = $fields["VIEWABBV3"];
            $data["REMARK3"][TEXT]              = $fields["SHOWORDER3"];
            $data["REMARK4"][TEXT]              = $studyrec_classcd3      ;
            $data["REMARK5"][TEXT]              = $studyrec_school_kind3  ;
            $data["REMARK6"][TEXT]              = $studyrec_curriculum_cd3;
            $data["REMARK7"][TEXT]              = $studyrec_subclasscd3   ;
            $data["REMARK8"][TEXT]              = $fields["STUDYREC_VIEWCD3"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
            $db->query($query);
        }

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
            $query = knjz401p_2Query::checkJViewYdata($grade, $substr_subclasscd, $substr_school_kind, $substr_curriculum_cd, $substr_classcd, $fields["VIEWCD"]);
        } else {
            $query = knjz401p_2Query::checkJViewYdata($grade, $fields["SUBCLASSCD"], $fields["VIEWCD"]);
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

            $query  = " DELETE FROM JVIEWNAME_GRADE_DETAIL_MST ";
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
        
        //Detail
        $query  = " DELETE FROM JVIEWNAME_GRADE_DETAIL_MST ";
        $query .= " WHERE GRADE     = '".$model->grade."'  ";
        $db->query($query);
        
        $query  = " INSERT INTO JVIEWNAME_GRADE_DETAIL_MST ";
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
        $query .= "      VIEW_SEQ, ";
        $query .= "      REMARK1, ";
        $query .= "      REMARK2, ";
        $query .= "      REMARK3, ";
        $query .= "      REMARK4, ";
        $query .= "      REMARK5, ";
        $query .= "      REMARK6, ";
        $query .= "      REMARK7, ";
        $query .= "      REMARK8, ";
        $query .= "      REMARK9, ";
        $query .= "      REMARK10, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE()";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_DETAIL_MST";
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
            if ($model->$classcd) {
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
        $query .= "     D4.REMARK1 AS RECORD_DIV, ";
        if ($model->Properties["useCurriculumcd"] != '1') {
            $query .= "     L1.CLASSCD, ";
        }
        $query .= "     D1.REMARK1 AS VIEWNAME1, ";
        $query .= "     D1.REMARK2 AS VIEWABBV1, ";
        $query .= "     D2.REMARK1 AS VIEWNAME2, ";
        $query .= "     D2.REMARK2 AS VIEWABBV2, ";
        //3学期制のとき
        if ($model->getSemesterCount > 2) {
            $query .= "     D3.REMARK1 AS VIEWNAME3, ";
            $query .= "     D3.REMARK2 AS VIEWABBV3, ";
        }
        $query .= "     L1.CLASSNAME, ";
        $query .= "     L2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST D4 ON D4.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND D4.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND D4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND D4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND D4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND D4.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND D4.VIEW_SEQ = '004' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST D1 ON D1.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND D1.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND D1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND D1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND D1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND D1.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND D1.VIEW_SEQ = '001' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST D2 ON D2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                            AND D2.CLASSCD = T1.CLASSCD ";
            $query .= "                                            AND D2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND D2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND D2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND D2.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND D2.VIEW_SEQ = '002' ";
        //3学期制のとき
        if ($model->getSemesterCount > 2) {
            $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST D3 ON D3.GRADE = T1.GRADE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                            AND D3.CLASSCD = T1.CLASSCD ";
                $query .= "                                            AND D3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                                            AND D3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                                            AND D3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                                            AND D3.VIEWCD = T1.VIEWCD ";
            $query .= "                                            AND D3.VIEW_SEQ = '003' ";
        }
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
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$data["CLASSCD"]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$data["SCHOOL_KIND"]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$data["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     SUBCLASSCD      = '".$data["SUBCLASSCD"]."' ";
        return $query;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 1; $j <= $data_arr[$i]["COL_NO"]; $j++) {
                //データ有無チェック
                $check = $db->getOne(knjz401p_2Query::checkJviewnameGradeMstExist($model, $data_arr[$i], $data_arr[$i]["VIEWCD".$j]));
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
                
                //Detail用
                //データ有無チェック
                $check4 = $db->getOne(knjz401p_2Query::checkJviewnameGradeDetailMstExist($model, $data_arr[$i], $data_arr[$i]["VIEWCD".$j], "004"));
                //クラス形態
                $data = array();
                if (1 > $check4) {
                    $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                        $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                        $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                    }
                    $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                    $data["VIEWCD"][TEXT]           = $data_arr[$i]["VIEWCD".$j];
                    $data["VIEW_SEQ"][TEXT]         = "004";
                }
                $data["REMARK1"][TEXT]          = $data_arr[$i]["RECORD_DIV".$j];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                //データが一件もなければ、insertする
                if (1 > $check4) {
                    $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
                } else {
                    $where  = " WHERE ";
                    $where .= "     GRADE           = '".$data_arr[$i]["GRADE"]."' AND ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "     CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' AND ";
                        $where .= "     SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                        $where .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
                    }
                    $where .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
                    $where .= "     VIEWCD          = '".$data_arr[$i]["VIEWCD".$j]."' AND ";
                    $where .= "     VIEW_SEQ        = '004' ";

                    $query = Query::updateSQL($data, "JVIEWNAME_GRADE_DETAIL_MST", $where);
                }
                $db->query($query);

                //前期
                //データ有無チェック
                $check1 = $db->getOne(knjz401p_2Query::checkJviewnameGradeDetailMstExist($model, $data_arr[$i], $data_arr[$i]["VIEWCD".$j], "001"));
                $data = array();
                if (1 > $check1) {
                    $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                        $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                        $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                    }
                    $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                    $data["VIEWCD"][TEXT]           = $data_arr[$i]["VIEWCD".$j];
                    $data["VIEW_SEQ"][TEXT]         = "001";
                }
                $data["REMARK1"][TEXT]          = $data_arr[$i]["VIEWNAME1".$j];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                //データが一件もなければ、insertする
                if (1 > $check1) {
                    $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
                } else {
                    $where  = " WHERE ";
                    $where .= "     GRADE           = '".$data_arr[$i]["GRADE"]."' AND ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "     CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' AND ";
                        $where .= "     SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                        $where .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
                    }
                    $where .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
                    $where .= "     VIEWCD          = '".$data_arr[$i]["VIEWCD".$j]."' AND ";
                    $where .= "     VIEW_SEQ        = '001' ";

                    $query = Query::updateSQL($data, "JVIEWNAME_GRADE_DETAIL_MST", $where);
                }
                $db->query($query);
                
                //2学期
                //データ有無チェック
                $check2 = $db->getOne(knjz401p_2Query::checkJviewnameGradeDetailMstExist($model, $data_arr[$i], $data_arr[$i]["VIEWCD".$j], "002"));
                $data = array();
                if (1 > $check2) {
                    $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                        $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                        $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                    }
                    $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                    $data["VIEWCD"][TEXT]           = $data_arr[$i]["VIEWCD".$j];
                    $data["VIEW_SEQ"][TEXT]         = "002";
                }
                $data["REMARK1"][TEXT]          = $data_arr[$i]["VIEWNAME2".$j];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                //データが一件もなければ、insertする
                if (1 > $check2) {
                    $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
                } else {
                    $where  = " WHERE ";
                    $where .= "     GRADE           = '".$data_arr[$i]["GRADE"]."' AND ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "     CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' AND ";
                        $where .= "     SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                        $where .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
                    }
                    $where .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
                    $where .= "     VIEWCD          = '".$data_arr[$i]["VIEWCD".$j]."' AND ";
                    $where .= "     VIEW_SEQ        = '002' ";

                    $query = Query::updateSQL($data, "JVIEWNAME_GRADE_DETAIL_MST", $where);
                }
                $db->query($query);
                
                //3学期制のとき
                if ($model->getSemesterCount > 2) {
                    //データ有無チェック
                    $check3 = $db->getOne(knjz401p_2Query::checkJviewnameGradeDetailMstExist($model, $data_arr[$i], $data_arr[$i]["VIEWCD".$j], "003"));
                    $data = array();
                    if (1 > $check3) {
                        $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                            $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                            $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                        }
                        $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                        $data["VIEWCD"][TEXT]           = $data_arr[$i]["VIEWCD".$j];
                        $data["VIEW_SEQ"][TEXT]         = "003";
                    }
                    $data["REMARK1"][TEXT]          = $data_arr[$i]["VIEWNAME3".$j];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "SYSDATE()";

                    //データが一件もなければ、insertする
                    if (1 > $check3) {
                        $query = Query::insertSQL($data, "JVIEWNAME_GRADE_DETAIL_MST");
                    } else {
                        $where  = " WHERE ";
                        $where .= "     GRADE           = '".$data_arr[$i]["GRADE"]."' AND ";
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $where .= "     CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' AND ";
                            $where .= "     SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                            $where .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
                        }
                        $where .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
                        $where .= "     VIEWCD          = '".$data_arr[$i]["VIEWCD".$j]."' AND ";
                        $where .= "     VIEW_SEQ        = '003' ";

                        $query = Query::updateSQL($data, "JVIEWNAME_GRADE_DETAIL_MST", $where);
                    }
                    $db->query($query);
                }
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

    //データ有無チェック(Detail用)
    public function checkJviewnameGradeDetailMstExist($model, $data_arr, $viewcd, $viewseq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE           = '".$data_arr["GRADE"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$data_arr["CLASSCD"]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$data_arr["SCHOOL_KIND"]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$data_arr["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     SUBCLASSCD      = '".$data_arr["SUBCLASSCD"]."' AND ";
        $query .= "     VIEWCD          = '".$viewcd."' AND ";
        $query .= "     VIEW_SEQ        = '".$viewseq."' ";

        return $query;
    }
}
