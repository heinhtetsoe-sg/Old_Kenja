<?php

require_once('for_php7.php');

class knjd623Query extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    CAST(SMALLINT(GRADE) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER='".$sem ."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE";

        return $query;
    }

    //テスト種別取得
    function getTestcd($model)
    {
        $query  = "SELECT ";
        $query .= "    TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "    TESTITEMNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"] ."' ";
        $query .= "    AND TESTKINDCD <> '99' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //テスト科目一覧取得
    function getSubclass($model)
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "WITH SEM_T AS ( ";
        $query .= "SELECT ";
        $query .= "    SDATE, ";
        $query .= "    EDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$sem ."' ";
        $query .= "), SCH_T AS ( ";
        $query .= "SELECT ";
        $query .= "    SCHREGNO ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$sem ."' ";
        $query .= "    AND GRADE = '".$model->field["GRADE"] ."' ";
        $query .= "), CHR_T AS ( ";
        $query .= "SELECT DISTINCT ";
        $query .= "    T1.CHAIRCD ";
        $query .= "FROM ";
        $query .= "    CHAIR_STD_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$sem ."' ";
        $query .= "    AND T1.SCHREGNO IN (SELECT ";
        $query .= "                            W1.SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCH_T W1 ";
        $query .= "                       ) ";
        $query .= "), NAM_T AS ( ";
        $query .= "SELECT DISTINCT ";
        $query .= "    NAME1 ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST  ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->field["GRADE"] < "04") {
            $query .= "AND NAMECD1='D003' ";
        } else {
            $query .= "AND NAMECD1='D002' ";
        }
        $query .= ") ";
        $query .= "SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD AS VALUE, ";
            $query .= "    L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD || ':' || L2.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "    L2.SUBCLASSCD AS VALUE, ";
            $query .= "    L2.SUBCLASSCD || ':' || L2.SUBCLASSNAME AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "    SCH_CHR_TEST T1 ";
        $query .= "    LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND L1.SEMESTER = '".$sem ."' ";
        $query .= "         AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= "    LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = L1.SUBCLASSCD, ";
        $query .= "    SEM_T T2 ";
        $query .= "WHERE ";
        $query .= "    T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "    AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTCD"] ."' ";
        $query .= "    AND T1.CHAIRCD IN (SELECT ";
        $query .= "                           W1.CHAIRCD ";
        $query .= "                       FROM ";
        $query .= "                           CHR_T W1 ";
        $query .= "                      ) ";
        if ($model->field["SUBCLASSDIV"] == "02") {
            $query .= "AND L2.SUBCLASSCD IN (SELECT ";
            $query .= "                         W2.NAME1 ";
            $query .= "                      FROM ";
            $query .= "                         NAM_T W2 ";
            $query .= "                      ) ";
        }

        return $query;
    }


}
?>
