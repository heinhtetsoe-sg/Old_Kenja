<?php

require_once('for_php7.php');

class knjb224Query extends Query
{

    //学期
    public function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->year}' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //教科
    public function getClassMst($model)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.SCHOOL_KIND, ";
            $query .= "     L1.CLASSCD, ";
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || ':' || L1.CLASSNAME AS LABEL, ";
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     L1.CLASSCD, ";
            $query .= "     L1.CLASSCD || ':' || L1.CLASSNAME AS LABEL, ";
            $query .= "     L1.CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_CLASS_MST L1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST L2 ";
        $query .= "             ON  L2.YEAR = L1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L2.CLASSCD = L1.CLASSCD ";
            $query .= "         AND L2.SCHOOL_KIND = L1.SCHOOL_KIND ";
        } else {
            $query .= "         AND SUBSTR(L2.SUBCLASSCD, 1, 2) = L1.CLASSCD ";
        }
        //2:講座
        if ($model->field["OUTPUT"] == '2') {
            $query .= "     INNER JOIN CHAIR_DAT T1 ";
            $query .= "             ON  T1.YEAR = L2.YEAR ";
            $query .= "             AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND T1.CLASSCD = L2.CLASSCD ";
                $query .= "         AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ";
                $query .= "         AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ";
            } else {
                $query .= "         AND T1.SUBCLASSCD = L2.SUBCLASSCD ";
            }
        }
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.SCHOOL_KIND, ";
            $query .= "     L1.CLASSCD ";
        } else {
            $query .= "     L1.CLASSCD ";
        }

        return $query;
    }

    //ＣＳＶ出力
    public function getCsvQuery($model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     L2.SUBCLASSCD AS SUBCLASSCD, ";
        }
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.TAKESEMES, ";
        $query .= "     T1.LESSONCNT, ";
        $query .= "     T1.FRAMECNT ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST L1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST L2 ";
        $query .= "             ON  L2.YEAR = L1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L2.CLASSCD = L1.CLASSCD ";
            $query .= "         AND L2.SCHOOL_KIND = L1.SCHOOL_KIND ";
        } else {
            $query .= "         AND SUBSTR(L2.SUBCLASSCD, 1, 2) = L1.CLASSCD ";
        }
        //2:講座
        if ($model->field["OUTPUT"] == '2') {
            $query .= "     INNER JOIN CHAIR_DAT T1 ";
        } else {
            $query .= "     LEFT JOIN CHAIR_DAT T1 ";
        }
        $query .= "             ON  T1.YEAR = L2.YEAR ";
        $query .= "             AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.CLASSCD = L2.CLASSCD ";
            $query .= "         AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "         AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        } else {
            $query .= "         AND T1.SUBCLASSCD = L2.SUBCLASSCD ";
        }
        $query .= " WHERE ";
        $query .= "     L1.YEAR = '{$model->year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND L1.CLASSCD || '-' || L1.SCHOOL_KIND IN ('".implode("','", $model->selectdata)."') ";
        } else {
            $query .= "     AND L1.CLASSCD IN ('".implode("','", $model->selectdata)."') ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L2.SCHOOL_KIND, ";
            $query .= "     L2.CURRICULUM_CD, ";
            $query .= "     L2.CLASSCD, ";
            $query .= "     L2.SUBCLASSCD, ";
        } else {
            $query .= "     L2.SUBCLASSCD, ";
        }
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //単位マスタ
    public function getCreditMst($model)
    {
        $query  = "";
        $query .= " WITH T_CREDIT AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD AS SUBCLASSCD, ";
        }
        $query .= "         T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS GRADE_COURSE, ";
        $query .= "         T1.CREDITS, ";
        $query .= "         T1.REQUIRE_FLG ";
        $query .= "     FROM ";
        $query .= "         CREDIT_MST T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND IN ('".implode("','", $model->selectdata)."') ";
        } else {
            $query .= "     AND T1.CLASSCD IN ('".implode("','", $model->selectdata)."') ";
        }
        $query .= " ) ";
        //学年・課程・学科・コースのＭＩＮを参照
        $query .= " , MIN_CREDIT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         MIN(T1.GRADE_COURSE) AS GRADE_COURSE ";
        $query .= "     FROM ";
        $query .= "         T_CREDIT T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CREDITS, ";
        $query .= "     T1.REQUIRE_FLG ";
        $query .= " FROM ";
        $query .= "     T_CREDIT T1, ";
        $query .= "     MIN_CREDIT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     AND T1.GRADE_COURSE = T2.GRADE_COURSE ";
        $query .= " ORDER BY ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //講座名簿
    public function getChairStdDat($model, $rowChaircd)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.GRADE, ";
        $query .= "     COUNT(T1.SCHREGNO) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "         ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CHAIRCD = '{$rowChaircd}' ";
        $query .= "     AND DATE('" . str_replace("/", "-", $model->field["DATE"]) . "') BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= " GROUP BY ";
        $query .= "     T2.GRADE ";

        return $query;
    }

    //学年
    public function getRegdGdat($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE ";

        return $query;
    }

    //講座職員
    public function getChairStfDat($model, $rowChaircd)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.STAFFCD, ";
        $query .= "     T2.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1 ";
        $query .= "     INNER JOIN V_STAFF_MST T2 ";
        $query .= "         ON  T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.STAFFCD = T1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CHAIRCD = '{$rowChaircd}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.STAFFCD ";

        return $query;
    }

    //講座施設
    public function getChairFacDat($model, $rowChaircd)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.FACCD, ";
        $query .= "     T2.FACILITYNAME, ";
        $query .= "     T2.CAPACITY ";
        $query .= " FROM ";
        $query .= "     CHAIR_FAC_DAT T1 ";
        $query .= "     INNER JOIN V_FACILITY_MST T2 ";
        $query .= "         ON  T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.FACCD = T1.FACCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.CHAIRCD = '{$rowChaircd}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.FACCD ";

        return $query;
    }
}
