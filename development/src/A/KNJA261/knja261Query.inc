<?php

require_once('for_php7.php');


class knja261Query extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA261' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    // 学期
    function &getSemesterMst($year) {
        $query  = "select SEMESTER, SEMESTERNAME ";
        $query .= "  from SEMESTER_MST ";
        $query .= " where YEAR = '$year' and SEMESTER<>'9' ";
        $query .= " order by SEMESTER ";

        return $query;
    }

    function getHrClassAuth($model, $year,$semester,$auth_user,$staffcd)
    {
        //参照・更新可
        if ($auth_user == DEF_REFERABLE || $auth_user == DEF_UPDATABLE){
            $query  = "SELECT T2.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T2 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
                    $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T2.YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/05
        if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T2.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T2 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
                    $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T2.YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .$staffcd ."' ";
            $query .= " OR TR_CD2 = '" .$staffcd ."' ";
            $query .= " OR TR_CD3 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD1 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD2 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD3 = '" .$staffcd ."') ";
        }

        return $query;
    }

    //生徒データ
    function getSchno($model, $year, $semester, $grade_hr_class)
    {
        $query  = "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW,T2.GRADE,T2.HR_CLASS,T3.HR_NAME ";
        $query .= "FROM SCHREG_BASE_MST T1 ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T2 ON ";
        $query .= "         T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG_REGD_HDAT T3 ON ";
        $query .= "         T2.YEAR = T3.YEAR AND T2.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T2.GRADE = T3.GRADE AND T2.HR_CLASS = T3.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T2.YEAR = '$year' AND ";
        $query .= "         T2.SEMESTER = '$semester' AND ";
        $query .= "         T2.GRADE || T2.HR_CLASS = '$grade_hr_class' ";
        $query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }

    //履修科目データＣＳＶ出力
    function selectStudySubjectData($model, $year, $semester, $selectleft, $kubun, $appdate)
    {
        $arr_selectleft = explode(",", $selectleft);
        if ($kubun == 2) {
            for ($i = 0; $i < get_count($arr_selectleft); $i++) {
                $tmp_selectleft = explode("-", $arr_selectleft[$i]);
                $arr_selectleft[$i] = $tmp_selectleft[0];
            }
        }
        $query  = "";
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO,COURSECD,MAJORCD,COURSECODE ";
        $query .= "       FROM SCHREG_REGD_DAT ";
        $query .= "      WHERE YEAR = '$year' ";
        $query .= "        AND SEMESTER = '$semester' ";
        if ($kubun == 2) {
            $query .= "    AND SCHREGNO IN ('".implode($arr_selectleft, "','")."') ";
        } else {
            $query .= "    AND GRADE||HR_CLASS IN ('".implode($arr_selectleft, "','")."') ";
        }
        $query .= "     ) ";
        $query .= " ,CHAIR_SCHNO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "            W1.SCHREGNO,W1.GRADE,W1.HR_CLASS,W1.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W2.YEAR,W2.SEMESTER,W2.CLASSCD,W2.SCHOOL_KIND,W2.CURRICULUM_CD,W2.SUBCLASSCD ";
        } else {
            $query .= "            W2.YEAR,W2.SEMESTER,W2.SUBCLASSCD ";
        }
        $query .= "       FROM SCHNO W1, ";
        $query .= "            CHAIR_DAT W2, ";
        $query .= "            CHAIR_STD_DAT W3 ";
        $query .= "      WHERE W2.YEAR = '$year' ";
        $query .= "        AND W2.SEMESTER = '$semester' ";
        $query .= "        AND W3.YEAR = W2.YEAR ";
        $query .= "        AND W3.SEMESTER = W2.SEMESTER ";
        $query .= "        AND W3.CHAIRCD = W2.CHAIRCD ";
        $query .= "        AND W3.SCHREGNO = W1.SCHREGNO ";
        $query .= "        AND '$appdate' BETWEEN W3.APPDATE AND W3.APPENDDATE ";
        $query .= "     ) ";
        $query .= " ,CREDIT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "            W1.SCHREGNO,W1.GRADE,W1.HR_CLASS,W1.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W2.CLASSCD,W2.SCHOOL_KIND,W2.CURRICULUM_CD,W2.SUBCLASSCD, ";
            $query .= "            W2.CREDITS,W2.AUTHORIZE_FLG,W2.COMP_UNCONDITION_FLG ";
        } else {
            $query .= "            W2.SUBCLASSCD,W2.CREDITS,W2.AUTHORIZE_FLG,W2.COMP_UNCONDITION_FLG ";
        }
        $query .= "       FROM SCHNO W1, ";
        $query .= "            CREDIT_MST W2 ";
        $query .= "      WHERE W2.YEAR = '$year' ";
        $query .= "        AND W2.COURSECD = W1.COURSECD ";
        $query .= "        AND W2.MAJORCD = W1.MAJORCD ";
        $query .= "        AND W2.GRADE = W1.GRADE ";
        $query .= "        AND W2.COURSECODE = W1.COURSECODE ";
        $query .= "     ) ";
        $query .= "  ";
        $query .= " SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, ";
        $query .= "        T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T3.NAME_SHOW, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, T4.SUBCLASSNAME, ";
            $query .= "        T2.CREDITS, '' AS CHAIRCD, '' AS CHAIRNAME, ";
        } else {
            $query .= "        T1.SUBCLASSCD, T4.SUBCLASSNAME, T2.CREDITS, ";
            $query .= "        '' AS CHAIRCD, '' AS CHAIRNAME, ";
        }
        $query .= "        T2.AUTHORIZE_FLG, T2.COMP_UNCONDITION_FLG ";
        $query .= "   FROM CHAIR_SCHNO T1 ";
        $query .= "        LEFT JOIN CREDIT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "                           AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                           AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                           AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "        LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD = T1.CLASSCD ";
            $query .= "                                 AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                 AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                                 AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, SUBCLASSCD ";
        } else {
            $query .= "        LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SUBCLASSCD ";
        }

        return $query;
    }

    //受講講座取得
    function getChairCd($model, $year, $semester, $schregno, $subclasscd, $appdate, $flg="")
    {
        $query  = " SELECT ";
        if($flg == "count") {
            $query .= "     COUNT(T1.CHAIRCD) ";
        } else {
            $query .= "     MIN(T1.CHAIRCD) AS CHAIRCD ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$subclasscd."' AND ";
        }
        $query .= "     T2.SCHREGNO = '".$schregno."' AND ";
        $query .= "     '".$appdate."' BETWEEN T2.APPDATE AND T2.APPENDDATE ";

        return $query;
    }

    //受講講座名称取得
    function getChairName($year, $semester, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     CHAIRCD = '".$chaircd."' ";

        return $query;
    }
}
?>
