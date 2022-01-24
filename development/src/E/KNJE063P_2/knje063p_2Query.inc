<?php

require_once('for_php7.php');

class knje063p_2Query extends Query {

    //生徒氏名取得
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "   NAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '" .$schregno."' ";
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     J_SUB_D.YEAR, ";
        $query .= "     J_SUB_D.SCHREGNO, ";
        $query .= "     substr(J_SUB_D.SUBCLASSCD, 1, 2) AS CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     J_SUB_D.SCHOOL_KIND, ";
            $query .= "     J_SUB_D.CURRICULUM_CD, ";
            $query .= "     J_SUB_D.CLASSCD || '-' || J_SUB_D.SCHOOL_KIND || '-' || J_SUB_D.CURRICULUM_CD || '-' || J_SUB_D.SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "     J_SUB_D.SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUB_DAT J_SUB_D ";
        $query .= " WHERE ";
        $query .= "     J_SUB_D.SCHREGNO = '".$model->schregno."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     STUDY.YEAR, ";
        $query .= "     STUDY.SCHREGNO, ";
        $query .= "     STUDY.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     STUDY.SCHOOL_KIND, ";
            $query .= "     STUDY.CURRICULUM_CD, ";
            $query .= "     STUDY.CLASSCD || '-' || STUDY.SCHOOL_KIND || '-' || STUDY.CURRICULUM_CD || '-' || STUDY.SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "     STUDY.SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT STUDY ";
        $query .= " WHERE ";
        $query .= "     STUDY.SCHOOLCD = '0' ";
        $query .= "     AND STUDY.SCHREGNO = '".$model->schregno."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.YEAR, ";
        $query .= "     MAIN_T.SCHREGNO, ";
        $query .= "     CL_M.CLASSCD, ";
        $query .= "     CL_M.CLASSNAME, ";
        $query .= "     MAIN_T.SUBCLASSCD, ";
        $query .= "     J_SUB_D.VIEWCD, ";
        $query .= "     SUB_CL_M.SUBCLASSNAME, ";
        $query .= "     J_SUB_M.SHOWORDER, ";
        $query .= "     J_SUB_D.STATUS, ";
        $query .= "     REGD_G.GRADE_NAME1, ";
        $query .= "     STUDY.VALUATION ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     LEFT JOIN JVIEWSTAT_SUB_DAT J_SUB_D ON MAIN_T.YEAR = J_SUB_D.YEAR ";
        $query .= "           AND MAIN_T.SCHREGNO = J_SUB_D.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND MAIN_T.SUBCLASSCD = J_SUB_D.CLASSCD || '-' || J_SUB_D.SCHOOL_KIND || '-' || J_SUB_D.CURRICULUM_CD || '-' || J_SUB_D.SUBCLASSCD ";
        } else {
            $query .= "           AND MAIN_T.SUBCLASSCD = J_SUB_D.SUBCLASSCD ";
        }
        $query .= "           AND MAIN_T.CLASSCD = substr(J_SUB_D.SUBCLASSCD, 1, 2) ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUB_CL_M ON ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           MAIN_T.SUBCLASSCD = SUB_CL_M.CLASSCD || '-' || SUB_CL_M.SCHOOL_KIND || '-' || SUB_CL_M.CURRICULUM_CD || '-' || SUB_CL_M.SUBCLASSCD ";
        } else {
            $query .= "           MAIN_T.SUBCLASSCD = SUB_CL_M.SUBCLASSCD ";
        }
        $query .= "     LEFT JOIN JVIEWNAME_SUB_MST J_SUB_M ON J_SUB_D.SUBCLASSCD = J_SUB_M.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND J_SUB_D.CLASSCD = J_SUB_M.CLASSCD ";
            $query .= "          AND J_SUB_D.SCHOOL_KIND = J_SUB_M.SCHOOL_KIND ";
            $query .= "          AND J_SUB_D.CURRICULUM_CD = J_SUB_M.CURRICULUM_CD ";
        }
        $query .= "          AND J_SUB_D.VIEWCD = J_SUB_M.VIEWCD ";
        $query .= "     LEFT JOIN SCHREG_STUDYREC_DAT STUDY ON STUDY.SCHOOLCD = '0' ";
        $query .= "           AND MAIN_T.YEAR = STUDY.YEAR ";
        $query .= "           AND MAIN_T.SCHREGNO = STUDY.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND MAIN_T.SUBCLASSCD = STUDY.CLASSCD || '-' || STUDY.SCHOOL_KIND || '-' || STUDY.CURRICULUM_CD || '-' || STUDY.SUBCLASSCD ";
        } else {
            $query .= "           AND MAIN_T.SUBCLASSCD = STUDY.SUBCLASSCD ";
        }
        $query .= "           AND MAIN_T.CLASSCD = STUDY.CLASSCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ";
        $query .= "          ON STUDY.ANNUAL      = REGD_G.GRADE ";
        $query .= "         AND STUDY.YEAR        = REGD_G.YEAR ";
        $query .= "         AND STUDY.SCHOOL_KIND = REGD_G.SCHOOL_KIND ";
        $query .= "     LEFT JOIN CLASS_MST CL_M ON MAIN_T.CLASSCD = CL_M.CLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND MAIN_T.SCHOOL_KIND = CL_M.SCHOOL_KIND ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     MAIN_T.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     MAIN_T.YEAR DESC, ";
        $query .= "     MAIN_T.CLASSCD, ";
        $query .= "     MAIN_T.SUBCLASSCD, ";
        $query .= "     J_SUB_M.SHOWORDER, ";
        $query .= "     J_SUB_D.VIEWCD                                                                                                                     ";

        return $query;
    }

}
?>
