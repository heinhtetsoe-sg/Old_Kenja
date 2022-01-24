<?php

require_once('for_php7.php');

class knjm020mQuery extends Query
{
    //�N�g�擾�i�����`�F�b�N�j
    function getAuth($year, $semester)
    {
        //�Q�ƁE�X�V��
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query .= "  SELECT DISTINCT ";
            $query .= "      GRADE || HR_CLASS AS VALUE, ";
            $query .= "      HR_NAME AS LABEL  ";
            $query .= "  FROM   ";
            $query .= "      SCHREG_REGD_HDAT ";
            $query .= "  WHERE  ";
            $query .= "      YEAR = '" .$year ."' ";
            $query .= "  AND SEMESTER   = '".$semester."' ";
        }
        //�Q�ƁE�X�V�i�����t���j
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "  SELECT DISTINCT ";
            $query .= "      GRADE || HR_CLASS AS VALUE, ";
            $query .= "      HR_NAME AS LABEL  ";
            $query .= "  FROM   ";
            $query .= "      SCHREG_REGD_HDAT ";
            $query .= "  WHERE  ";
            $query .= "      YEAR = '" .$year ."' ";
            $query .= "  AND SEMESTER   = '".$semester."' ";
            $query .= "  AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "  OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "  OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //�Ȗڃ��X�g�擾
    function getSubclass(&$model)
    {
        $query  = "SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' ||  t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "    t1.SUBCLASSCD, ";
        }
        $query .= "    MAX(t2.SUBCLASSABBV) AS SUBCLASSABBV ";
        $query .= "FROM ";
        $query .= "    REP_STANDARDDATE_DAT t1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         AND t1.CLASSCD = t2.CLASSCD ";
            $query .= "         AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "         AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ";
        }
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '" .CTRL_YEAR."' ";
        $query .= "GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "t1.CLASSCD, ";
            $query .= "t1.SCHOOL_KIND, ";
            $query .= "t1.CURRICULUM_CD, ";
        }
        $query .= "t1.SUBCLASSCD ";
        return $query;
    }
    
    //���|�[�g�񐔎擾
    function getReportcount(&$model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    STANDARD_SEQ ";
        $query .= "FROM ";
        $query .= "    REP_STANDARDDATE_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '" .CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '" .$model->field["KAMOKU"] ."' ";
        } else {
            $query .= "    AND SUBCLASSCD = '" .$model->field["KAMOKU"] ."' ";
        }

        return $query;
    }

}
?>