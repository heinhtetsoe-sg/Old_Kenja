<?php

require_once('for_php7.php');

class knje014query extends Query {
    //課程学科取得
    function getCourseMajor($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || S1.COURSENAME || S1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST S1 ";
        $query .= "          ON T1.YEAR         = S1.YEAR ";
        $query .= "         AND T1.COURSECD     = S1.COURSECD ";
        $query .= "         AND T1.MAJORCD      = S1.MAJORCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
                $query .= "          ON T1.YEAR         = S2.YEAR ";
                $query .= "         AND T1.GRADE        = S2.GRADE ";
                $query .= "         AND S2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   T2.TR_CD1       = '".STAFFCD."' OR ";
            $query .= "         T2.TR_CD2       = '".STAFFCD."' OR ";
            $query .= "         T2.TR_CD3       = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD1    = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD2    = '".STAFFCD."' OR ";
            $query .= "         T2.SUBTR_CD3    = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        list ($grade, $hr_class) = explode('-', $model->field["GROUP_HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$grade."' ";

        return $query;
    }

    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //学校区分取得
    function getSchoolDiv($model, $check_schKind, $schoolkind) {
        $query  = " SELECT ";
        $query .= "     SCHOOLDIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($check_schKind > 0) {
            $query .= " AND SCHOOL_KIND = '".$schoolkind."' ";
        }

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $check_schKind) {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "         GRADE || '-' || HR_CLASS = '".$model->field["GROUP_HR_CLASS"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND COURSECD || MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        $query .= " ), STUDYREC_YEAR AS ( ";
        $query .= "     SELECT  ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ANNUAL, ";
        if ($model->field["SCHOOLDIV"] == '0') {
            $query .= "         MAX(T1.YEAR) AS YEAR ";
        } else {
            $query .= "         T1.YEAR ";
        }
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT T1 ";
        $query .= "         INNER JOIN SCHNO SCH ON T1.SCHREGNO = SCH.SCHREGNO ";
        if ($model->field["SCHOOLDIV"] == '0') {
            $query .= "     GROUP BY ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.ANNUAL ";
        }
        $query .= " ), STUDYREC0 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ANNUAL, ";
        $query .= "         T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ";
        $query .= "         T1.VALUATION           , ";
        $query .= "         CASE WHEN ADD_CREDIT IS NOT NULL OR GET_CREDIT IS NOT NULL THEN VALUE(ADD_CREDIT, 0) + VALUE(GET_CREDIT, 0) END AS CREDIT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT T1 ";
        $query .= "         INNER JOIN SCHNO SCH ON T1.SCHREGNO = SCH.SCHREGNO ";
        $query .= "         INNER JOIN STUDYREC_YEAR REC_YEAR ";
        $query .= "              ON T1.SCHREGNO = REC_YEAR.SCHREGNO ";
        $query .= "             AND T1.YEAR     = REC_YEAR.YEAR ";
        $query .= "         LEFT JOIN SUBCLASS_MST T2 ";
        $query .= "              ON T2.SUBCLASSCD       = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T2.CLASSCD          = T1.CLASSCD ";
            $query .= "             AND T2.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "             AND T2.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        //仮評定フラグ
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "         LEFT JOIN STUDYREC_PROV_FLG_DAT T3 ";
            $query .= "              ON T3.SCHOOLCD         = T1.SCHOOLCD ";
            $query .= "             AND T3.YEAR             = T1.YEAR ";
            $query .= "             AND T3.SCHREGNO         = T1.SCHREGNO ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             AND T3.CLASSCD          = T1.CLASSCD ";
                $query .= "             AND T3.SCHOOL_KIND      = T1.SCHOOL_KIND ";
                $query .= "             AND T3.CURRICULUM_CD    = T1.CURRICULUM_CD ";
            }
            $query .= "             AND T3.SUBCLASSCD       = T1.SUBCLASSCD ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.CLASSCD < '90' AND ";
        $query .= "         0 < T1.VALUATION AND ";
        $query .= "         T1.VALUATION < 11 AND ";
        $query .= "         NOT EXISTS (SELECT ";
        $query .= "                         'x' ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_GDAT E1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = E1.YEAR AND ";
        $query .= "                         T1.ANNUAL   = E1.GRADE AND ";
        $query .= "                         VALUE(E1.SCHOOL_KIND, '-') <> '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "                     ) ";
        $query .= " ), STUDYREC AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         CASE WHEN COUNT(*) = 1 THEN MAX(VALUATION) ";
        $query .= "              WHEN GVAL_CALC = '0' THEN ROUND(AVG(FLOAT(CASE WHEN 0 < VALUATION THEN VALUATION END)),0) ";
        $query .= "              WHEN GVAL_CALC = '1' AND 0 < SUM(CASE WHEN 0 < VALUATION THEN CREDIT END) THEN ROUND(FLOAT(SUM((CASE WHEN 0 < VALUATION THEN VALUATION END)*CREDIT))/SUM(CASE WHEN 0 < VALUATION THEN CREDIT END),0) ";
        $query .= "              ELSE MAX(VALUATION) END AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         STUDYREC0 T1 ";
        $query .= "         LEFT JOIN SCHOOL_MST T2 ";
        $query .= "              ON T2.YEAR         = T1.YEAR ";
        if ($check_schKind > 0) {
            $query .= "             AND T2.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         GVAL_CALC ";
        $query .= " ), STUDYREC_2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,5,1) AS VAL_ALL_AVG ";
        $query .= "     FROM ";
        $query .= "         STUDYREC ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), T_CREDIT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(GET_CREDIT) AS GET_CREDIT, ";
        $query .= "         SUM(COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L2.NAME_SHOW, ";
        $query .= "     L5.COURSECODENAME, ";
        $query .= "     L3.GET_CREDIT, ";
        $query .= "     L3.COMP_CREDIT, ";
        $query .= "     L1.VAL_ALL_AVG, ";
        $query .= "     L4.COMMENTEX_A_CD ";
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     LEFT JOIN STUDYREC_2 L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN T_CREDIT L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HEXAM_ENTREMARK_HDAT L4 ON L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN COURSECODE_MST L5 ON L5.COURSECODE = T1.COURSECODE ";
        if ($model->Properties["useAssessCourseMst"] == '1') {
            $query .= "     INNER JOIN ASSESS_COURSE_MST ASS ON ASS.ASSESSCD = '4' ";
            $query .= "         AND L1.VAL_ALL_AVG BETWEEN ASS.ASSESSLOW AND ASS.ASSESSHIGH ";
            $query .= "         AND ASS.ASSESSLEVEL = 5 ";
            $query .= "         AND ASS.COURSECD = T1.COURSECD ";
            $query .= "         AND ASS.MAJORCD = T1.MAJORCD ";
            $query .= "         AND ASS.COURSECODE = T1.COURSECODE ";
        } else {
            $query .= "     INNER JOIN ASSESS_MST ASS ON ASS.ASSESSCD = '4' ";
            $query .= "         AND L1.VAL_ALL_AVG BETWEEN ASS.ASSESSLOW AND ASS.ASSESSHIGH ";
            $query .= "         AND ASS.ASSESSLEVEL = 5 ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //データ更新処理
    function getUpdateQuery($model) {

        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //データ有無チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HEXAM_ENTREMARK_HDAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->data["SCHREGNO"][$i]."' ";

            //HEXAM_ENTREMARK_HDAT
            $data = array();
            $data["SCHREGNO"][TEXT]         = $model->data["SCHREGNO"][$i];
            $data["COMMENTEX_A_CD"][TEXT]   = $model->fields["COMMENTEX_A_CD"][$i];

            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE SCHREGNO = '".$model->data["SCHREGNO"][$i]."' ";
                $query = Query::updateSQL($data, "HEXAM_ENTREMARK_HDAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "HEXAM_ENTREMARK_HDAT");
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }
}
?>
