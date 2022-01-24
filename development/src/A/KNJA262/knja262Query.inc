<?php

require_once('for_php7.php');
class knja262query extends Query {

    //合格コース名
    function getExamCourseName($model)
    {
        $query  = " SELECT examcourse_name FROM entexam_course_mst ";
        $query .= "  WHERE entexamyear  = '".$model->examyear."' ";
        $query .= "    AND coursecd     = '".substr($model->examcourse,0,1)."' ";
        $query .= "    AND majorcd      = '".substr($model->examcourse,1,3)."' ";
        $query .= "    AND examcoursecd = '".substr($model->examcourse,4)."' ";
        return $query;
    }

    //実行
    function selectCvsQuery($model)
    {
        $query  = " WITH BASE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t2.NAME, ";
        $query .= "     t2.NAME_KANA, ";
        $query .= "     t2.SEX, ";
        $query .= "     t1.OLD_GRADE AS GRADE, ";
        $query .= "     t1.OLD_HR_CLASS AS HR_CLASS, ";
        $query .= "     t1.OLD_ATTENDNO AS ATTENDNO, ";
        $query .= "     t1.COURSECD, ";
        $query .= "     t1.MAJORCD, ";
        $query .= "     t1.COURSECODE, ";
        $query .= "     t4.TYPE_GROUP_CD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t4.CLASSCD, ";
            $query .= "     t4.SCHOOL_KIND, ";
            $query .= "     t4.CURRICULUM_CD, ";
        }
        $query .= "     t4.SUBCLASSCD, ";
        $query .= "     CASE WHEN t4.TYPE_ASSES_CD is null OR t4.TYPE_ASSES_CD = '' ";
        $query .= "     THEN t4.SEM1_REC_TYPE_ASSES_CD ";
        $query .= "     ELSE t4.TYPE_ASSES_CD END AS SEM1_ASSES_CD, ";
        $query .= "     CASE WHEN t4.TYPE_ASSES_CD is null OR t4.TYPE_ASSES_CD = '' ";
        $query .= "     THEN t4.SEM2_REC_TYPE_ASSES_CD ";
        $query .= "     ELSE t4.TYPE_ASSES_CD END AS SEM2_ASSES_CD ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST t2 ON t2.SCHREGNO = t1.SCHREGNO ";
        $query .= "     LEFT JOIN TYPE_GROUP_HR_DAT t3 ON t3.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t3.GRADE || t3.HR_CLASS = t1.OLD_GRADE || t1.OLD_HR_CLASS ";
        $query .= "     LEFT JOIN TYPE_GROUP_MST t4 ON t4.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t4.TYPE_GROUP_CD = t3.TYPE_GROUP_CD ";
        $query .= "     AND t4.GRADE = t1.OLD_GRADE ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".$model->examyear."' ";
        $query .= "     AND t1.SEMESTER = '1' ";
        $query .= "     AND t1.OLD_GRADE = '".$model->grade."' ";
        $query .= "     AND t1.REMAINGRADE_FLG <> '1' ";
        $query .= "     AND t2.SCHREGNO = t1.SCHREGNO ";
        $query .= "     AND (t2.GRD_DIV is null OR t2.GRD_DIV = '') ";
        $query .= " ), RECORD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SEM1_REC, ";
        $query .= "     SEM2_REC ";
        $query .= " FROM ";
        $query .= "     KIN_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM BASE_T) ";
        //教育課程対応
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN(SELECT ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
        $query .= "                           FROM SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE REPLACECD = '1' ";
        $query .= "                                 AND YEAR = '".CTRL_YEAR."' ";
        $query .= "                           ) ";
        $query .= "     AND (SEM1_REC is not null OR SEM2_REC is not null) ";
        $query .= " ),BASE_REC_T AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.NAME, ";
        $query .= "     t1.NAME_KANA, ";
        $query .= "     t1.SEX, ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.HR_CLASS, ";
        $query .= "     t1.ATTENDNO, ";
        $query .= "     t1.COURSECD, ";
        $query .= "     t1.MAJORCD, ";
        $query .= "     t1.COURSECODE, ";
        $query .= "     t1.TYPE_GROUP_CD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t1.CLASSCD, ";
            $query .= "     t1.SCHOOL_KIND, ";
            $query .= "     t1.CURRICULUM_CD, ";
        }
        $query .= "     t1.SUBCLASSCD, ";
        $query .= "     t1.SEM1_ASSES_CD, ";
        $query .= "     t1.SEM2_ASSES_CD, ";
        $query .= "     t3.SEM1_REC, ";
        $query .= "     t4.SEM2_REC ";
        $query .= " FROM ";
        $query .= "     BASE_T t1 ";
        $query .= "     LEFT JOIN RECORD_T t3 ON t3.SCHREGNO = t1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "   AND t3.CLASSCD    	= t1.CLASSCD ";
	        $query .= "   AND t3.SCHOOL_KIND    = t1.SCHOOL_KIND ";
	        $query .= "   AND t3.CURRICULUM_CD  = t1.CURRICULUM_CD ";
        }
        $query .= "     AND t3.SUBCLASSCD = t1.SUBCLASSCD ";
        $query .= "     LEFT JOIN RECORD_T t4 ON t4.SCHREGNO = t1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "   AND t4.CLASSCD    	= t1.CLASSCD ";
	        $query .= "   AND t4.SCHOOL_KIND    = t1.SCHOOL_KIND ";
	        $query .= "   AND t4.CURRICULUM_CD  = t1.CURRICULUM_CD ";
        }
        $query .= "     AND t4.SUBCLASSCD = t1.SUBCLASSCD ";
        $query .= " ),SUB_MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.NAME, ";
        $query .= "     t1.NAME_KANA, ";
        $query .= "     t1.SEX, ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.HR_CLASS, ";
        $query .= "     t1.ATTENDNO, ";
        $query .= "     t1.COURSECD, ";
        $query .= "     t1.MAJORCD, ";
        $query .= "     t1.COURSECODE, ";
        $query .= "     SUM(SEM1_REC) AS SEM1_SUM, ";
        $query .= "     SUM(SEM2_REC) AS SEM2_SUM, ";
        $query .= "     COUNT(SEM1_REC) AS SEM1_CNT, ";
        $query .= "     COUNT(SEM2_REC) AS SEM2_CNT, ";
        $query .= "     SUM(smallint(t2.TYPE_ASSES_LEVEL)) AS LEVEL1_SUM, ";
        $query .= "     SUM(smallint(t3.TYPE_ASSES_LEVEL)) AS LEVEL2_SUM, ";
        $query .= "     COUNT(t2.TYPE_ASSES_LEVEL) AS LEVEL1_CNT, ";
        $query .= "     COUNT(t3.TYPE_ASSES_LEVEL) AS LEVEL2_CNT ";
        $query .= " FROM ";
        $query .= "     BASE_REC_T t1 ";
        $query .= "     LEFT JOIN TYPE_ASSES_MST t2 ON t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t2.TYPE_ASSES_CD = t1.SEM1_ASSES_CD ";
        $query .= "     AND t1.SEM1_REC BETWEEN t2.TYPE_ASSES_LOW AND t2.TYPE_ASSES_HIGH  ";
        $query .= "     LEFT JOIN TYPE_ASSES_MST t3 ON t3.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND t3.TYPE_ASSES_CD = t1.SEM2_ASSES_CD ";
        $query .= "     AND t1.SEM2_REC BETWEEN t3.TYPE_ASSES_LOW AND t3.TYPE_ASSES_HIGH ";
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.NAME, ";
        $query .= "     t1.NAME_KANA, ";
        $query .= "     t1.SEX, ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.HR_CLASS, ";
        $query .= "     t1.ATTENDNO, ";
        $query .= "     t1.COURSECD, ";
        $query .= "     t1.MAJORCD, ";
        $query .= "     t1.COURSECODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     value(SEM1_SUM,0)+value(SEM2_SUM,0) AS SEM_SUM, ";
        $query .= "     value(SEM1_CNT,0)+value(SEM2_CNT,0) AS SEM_CNT, ";
        $query .= "     value(LEVEL1_SUM,0)+value(LEVEL2_SUM,0) AS LEVEL_SUM, ";
        $query .= "     value(LEVEL1_CNT,0)+value(LEVEL2_CNT,0) AS LEVEL_CNT ";
        $query .= " FROM ";
        $query .= "     SUB_MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO ";

        return $query;
    }
}
?>
