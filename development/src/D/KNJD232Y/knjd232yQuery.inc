<?php

require_once('for_php7.php');

class knjd232yQuery extends Query {
    //1：法定時数・2：実時数
    function getJugyouJisuFlg() {
        $query  = " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //上限値(分数)
    function getSyuutokuJougenti() {
        $query  = " SELECT ";
        $query .= "     SYUTOKU_BUNSI || '/' || SYUTOKU_BUNBO AS SYUTOKU_JOUGENTI, ";
        $query .= "     RISYU_BUNSI   || '/' || RISYU_BUNBO   AS RISYU_JOUGENTI, ";
        $query .= "     SYUTOKU_BUNSI_SPECIAL || '/' || SYUTOKU_BUNBO_SPECIAL AS SYUTOKU_JOUGENTI_SPECIAL, ";
        $query .= "     RISYU_BUNSI_SPECIAL   || '/' || RISYU_BUNBO_SPECIAL   AS RISYU_JOUGENTI_SPECIAL ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     JUGYOU_JISU_FLG = '2' ";

        return $query;
    }

    //学年取得
    function getSelectGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SCHOOL_KIND IN ('H', 'J') ";
        $query .= " ORDER BY ";
        $query .= "     INT(GRADE) ";

        return $query;
    }

    //学年名取得
    function getGradeName($model) {
        $query .= " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '". CTRL_YEAR ."' AND ";
        $query .= "     GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //学期取得
    function getSelectSeme() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER";

        return $query;
    }

    //テスト種別取得
    function getTestKind($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || '：' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["GAKKI2"]."' ";
        if ($model->field["GAKKI2"] == "9") {
            $query .= "     AND TESTITEMCD = '00' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校区分名称取得
    function getSchoolDivName()
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

    function getShr($model) {
        $query  = " SELECT ";
        $query .= "     CLASSNAME AS LABEL, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  AS VALUE ";
        } else {
            $query .= "     CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     INT(CLASSCD) >= 91 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期名取得
    function getSemesterName($semester) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }

    //テスト名取得
    function getTestName($model)
    {
        $testkind = explode('-', $model->field["TESTKIND"]); 

        $query  = " SELECT ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$model->field["GAKKI2"]}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$testkind[0]}' ";

        return $query;
    }


/*************************************************************************************************************/
/**  CSV作成（成績優良者）  **********************************************************************************/
/*************************************************************************************************************/
    function selectCsvQuery1($model, $attend_seme, $month, $attend_sdate, $knjSchoolMst) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);
        $ctrl_year = CTRL_YEAR;
        $testkind = explode('-', $model->field["TESTKIND"]); 

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T3.NAME, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.ATTENDNO, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T2, ";
        $query .= "         SCHREG_BASE_MST T3 ";
        $query .= "     WHERE ";
        $query .= "         T2.YEAR = '{$ctrl_year}'  AND ";
        if ($model->field["GRADE"] != '99') {
            $query .= "         T2.GRADE = '{$model->field["GRADE"]}'   AND ";
        }
        $query .= "         T2.SEMESTER = '{$semester}' AND ";
        $query .= "         T2.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "         NOT (T3.GRD_DIV  IS NOT NULL AND ";
        $query .= "              T3.GRD_DATE IS NOT NULL AND ";
        $query .= "              T3.GRD_DIV IN ('1','2','3') AND ";
        $query .= "              T3.GRD_DATE < '{$date}' OR ";
        $query .= "              T3.ENT_DIV  IS NOT NULL AND ";
        $query .= "              T3.ENT_DATE IS NOT NULL AND ";
        $query .= "              T3.ENT_DATE > '{$date}') ";

        $query .= " ), ATTEND_SUBCLASSES AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.ATTEND_CLASSCD, ";
            $query .= "         T1.ATTEND_SCHOOL_KIND, ";
            $query .= "         T1.ATTEND_CURRICULUM_CD, ";
            $query .= "         T1.ATTEND_SUBCLASSCD, ";
            $query .= "         T1.COMBINED_CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD, ";
            $query .= "         T1.COMBINED_SUBCLASSCD ";
        } else {
            $query .= "         T1.ATTEND_SUBCLASSCD, ";
            $query .= "         T1.COMBINED_SUBCLASSCD ";
        }
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$ctrl_year}' AND ";
        $query .= "         T1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= " ), RECORD_CREDITS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD, ";
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "         T3.SUBCLASSCD, ";
        $query .= "         L4.CREDITS ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR AND ";
        $query .= "             T3.SEMESTER = T1.SEMESTER AND ";
        $query .= "             T3.CHAIRCD = T1.CHAIRCD ";
        $query .= "         INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST L4 ON L4.YEAR = T1.YEAR AND ";
        $query .= "             L4.COURSECD = T2.COURSECD AND ";
        $query .= "             L4.MAJORCD = T2.MAJORCD AND ";
        $query .= "             L4.GRADE = T2.GRADE AND ";
        $query .= "             L4.COURSECODE = T2.COURSECODE AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             L4.CLASSCD          = T3.CLASSCD AND ";
            $query .= "             L4.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
            $query .= "             L4.CURRICULUM_CD    = T3.CURRICULUM_CD AND ";
        }
        $query .= "             L4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$ctrl_year}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["GAKKI2"]}' ";
        $query .= " ), COMBINED_CREDITS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.COMBINED_CLASSCD, ";
            $query .= "         T3.COMBINED_SCHOOL_KIND, ";
            $query .= "         T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         T3.COMBINED_SUBCLASSCD, ";
        $query .= "         SUM(T1.CREDITS) AS SUM_CREDITS ";
        $query .= "     FROM ";
        $query .= "         RECORD_CREDITS T1 ";
        $query .= "     INNER JOIN ATTEND_SUBCLASSES T3 ON T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                    AND T3.ATTEND_CLASSCD        = T1.CLASSCD ";
            $query .= "                                    AND T3.ATTEND_SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "                                    AND T3.ATTEND_CURRICULUM_CD  = T1.CURRICULUM_CD ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.COMBINED_CLASSCD, ";
            $query .= "         T3.COMBINED_SCHOOL_KIND, ";
            $query .= "         T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         T3.COMBINED_SUBCLASSCD ";
        $query .= " ), T_CREDIT_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SUM_CREDITS AS CREDITS ";
        $query .= "     FROM  ";
        $query .= "         RECORD_CREDITS T1 ";
        $query .= "     INNER JOIN COMBINED_CREDITS T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.COMBINED_CLASSCD         = T1.CLASSCD ";
            $query .= "         AND T2.COMBINED_SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "         AND T2.COMBINED_CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CREDITS ";
        $query .= "     FROM  ";
        $query .= "         RECORD_CREDITS T1 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         COMBINED_CREDITS T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T2.SCHREGNO = T1.SCHREGNO AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T2.COMBINED_CLASSCD         = T1.CLASSCD AND ";
            $query .= "                         T2.COMBINED_SCHOOL_KIND     = T1.SCHOOL_KIND AND ";
            $query .= "                         T2.COMBINED_CURRICULUM_CD   = T1.CURRICULUM_CD AND ";
        }
        $query .= "                         T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";

        $query .= " ), RECORD_SCORE AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T3.SUBCLASSNAME, ";
        if($model->school_div_name == 'tottori' && $model->field["GAKKI2"] == "9" && $model->field["TESTKIND"] == "9900-2") {
            $query .= "         T1.SCORE AS VALUE, ";
        } else {
            $query .= "         T1.VALUE, ";
        }
        $query .= "         T4.CREDITS, ";
        $query .= "         T1.COMP_CREDIT, ";
        $query .= "         T1.GET_CREDIT, ";
        $query .= "         DECIMAL(ROUND(FLOAT(T5.AVG)*10,0)/10,5,1) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHNO           T2 ON  T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "     LEFT  JOIN SUBCLASS_MST    T3 ON  T3.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT  JOIN T_CREDIT_MST    T4 ON  T4.SCHREGNO   = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                   AND T4.CLASSCD        = T1.CLASSCD ";
            $query .= "                                   AND T4.SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "                                   AND T4.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        }
        $query .= "                                   AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        if($testkind[1] == "1") {
            $query .= "     INNER JOIN RECORD_RANK_DAT T5 ON  T5.YEAR       = T1.YEAR ";
        } else {
            $query .= "     INNER JOIN RECORD_RANK_V_DAT T5 ON  T5.YEAR     = T1.YEAR ";
        }
        $query .= "                                   AND T5.SEMESTER   = '{$model->field["GAKKI2"]}' ";
        $query .= "                                   AND T5.TESTKINDCD  || T5.TESTITEMCD = '{$testkind[0]}' ";
        $query .= "                                   AND T5.SUBCLASSCD = '999999' ";
        $query .= "                                   AND T5.SCHREGNO   = T1.SCHREGNO ";
        $query .= "                                   AND DECIMAL(ROUND(FLOAT(T5.AVG)*10,0)/10,5,1) >= {$model->field["SEISEKI_YURYOU_HYOUTEI"]} ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '{$ctrl_year}' AND ";
        $query .= "         T1.SEMESTER     = '{$model->field["GAKKI2"]}' AND ";
        $query .= "         T1.TESTKINDCD || T1.TESTITEMCD = '{$testkind[0]}' AND ";
        if($testkind[0] == '9900'){
            $query .= "         T1.SCORE_DIV    = '00' AND ";
        } else {
            $query .= "         T1.SCORE_DIV    = '01' AND ";
        }
        if($model->field["ATTEND_SUBCLASSCD"] == "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "     T1.SUBCLASSCD NOT IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             ATTEND_SUBCLASSCD ";
            }
            $query .= "                         FROM ";
            $query .= "                             SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '{$ctrl_year}' ";
            $query .= "                         ) AND ";
        }
        if($model->field["COMBINED_SUBCLASSCD"] == "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             COMBINED_CLASSCD  || '-' || COMBINED_SCHOOL_KIND  || '-' || COMBINED_CURRICULUM_CD  || '-' || COMBINED_SUBCLASSCD ";
            } else {
                $query .= "     T1.SUBCLASSCD NOT IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             COMBINED_SUBCLASSCD ";
            }
            $query .= "                         FROM ";
            $query .= "                             SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '{$ctrl_year}' ";
            $query .= "                         ) AND ";
        }
        $query .= "         SUBSTR(T1.SUBCLASSCD,1,2) <= '89' AND ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
            $query .= "                             SELECT ";
            $query .= "                                 CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "                             FROM ";
            $query .= "                                 SUBCLASS_DETAIL_DAT ";
            $query .= "                             WHERE ";
            $query .= "                                 YEAR = '{$ctrl_year}' AND ";
            $query .= "                                 SUBCLASS_SEQ = '004' AND ";
            $query .= "                                (CASE '{$model->field["GAKKI2"]}' WHEN '1' THEN SUBCLASS_REMARK4 ";
            $query .= "                                                                  WHEN '2' THEN SUBCLASS_REMARK5 ";
            $query .= "                                                                  WHEN '3' THEN SUBCLASS_REMARK6 ";
            $query .= "                                                                  ELSE SUBCLASS_REMARK7 END) = '1') ";
        } else {
            $query .= "         T1.SUBCLASSCD NOT IN ( ";
            $query .= "                             SELECT ";
            $query .= "                                 NAME1 ";
            $query .= "                             FROM ";
            $query .= "                                 V_NAME_MST ";
            $query .= "                             WHERE ";
            $query .= "                                 YEAR = '{$ctrl_year}' AND ";
            $query .= "                                 NAMECD1 = 'D017' AND ";
            $query .= "                                (CASE '{$model->field["GAKKI2"]}' WHEN '1' THEN ABBV1 ";
            $query .= "                                                                  WHEN '2' THEN ABBV2 ";
            $query .= "                                                                  WHEN '3' THEN ABBV3 ";
            $query .= "                                                                  ELSE NAMESPARE1 END) = '1') ";
        }

        $query .= " ), STUDYREC AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.COMP_CREDIT, 0)) AS ST_COMP, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT, 0)) AS ST_GET_ADD ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR < '{$ctrl_year}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";

        //遅刻・早退・欠席取得
        $query .= " ,ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "        + VALUE(OFFDAYS,0) ";
        }
        $query .= "         ) AS NOTICE, ";
        $query .= "         SUM(VALUE(LATE,0))  LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$ctrl_year}' AND ";
        $query .= "         SEMESTER <= '{$attend_seme}' AND ";
        $query .= "         SEMESTER || MONTH IN ('". implode($month, "','") ."') ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.SCHREGNO, ";      //学籍番号
        $query .= "     T2.GRADE, ";         //学年
        $query .= "     T2.HR_CLASS, ";      //組
        $query .= "     T2.ATTENDNO, ";      //出席番号
        $query .= "     T2.NAME, ";          //氏名
        $query .= "     T4.NOTICE, ";        //欠席
        $query .= "     T4.LATE, ";          //遅刻
        $query .= "     T4.EARLY, ";         //早退
        $query .= "     T1.SUBCLASSNAME, ";  //科目名
        $query .= "     T1.VALUE, ";         //評定
        $query .= "     T1.CREDITS, ";       //見込単位数
        $query .= "     T1.COMP_CREDIT, ";   //履修単位数
        $query .= "     T1.GET_CREDIT, ";    //修得単位数
        $query .= "     T1.VALUATION, ";     //評定平均
        $query .= "     T3.ST_COMP, ";       //履修単位数累計
        $query .= "     T3.ST_GET_ADD ";     //修得単位数累計
        $query .= " FROM ";
        $query .= "     RECORD_SCORE T1 ";
        $query .= " INNER JOIN SCHNO    T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT  JOIN STUDYREC T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT  JOIN ATTEND_SEMES T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.VALUATION DESC, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }


/*************************************************************************************************************/
/**  CSV作成（成績不振者）  **********************************************************************************/
/*************************************************************************************************************/
    function selectCsvQuery2($model, $attend_seme, $month, $attend_sdate, $knjSchoolMst) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);
        $absent_cov      = $model->absent_cov;
        $absent_cov_late = $model->absent_cov_late;
        $amari_kuriage   = $model->amari_kuriage;
        $ctrl_year = CTRL_YEAR;
        $testkind = explode('-', $model->field["TESTKIND"]); 

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND VALUE(GRD_DATE, '9999-12-31') >= '{$date}' ";
        $query .= "         AND VALUE(ENT_DATE, '0001-01-01') <= '{$date}' ";
        if ($model->field["GRADE"] != '99') {
            $query .= "         AND GRADE    = '{$model->field["GRADE"]}' ";
        }
        $query .= " ) ";

        $query .= " ,ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER <= '{$attend_seme}' ";
        $query .= "         AND SEMESTER || MONTH IN ('". implode($month, "','") ."') ";
        $query .= " ) ";

        $query .= " ,SCHEDULE AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD, ";
        $query .= "         DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT ";
        $query .= "     WHERE ";
        $query .= "             EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND PERIODCD != '0' ";
        $query .= " ) ";

        $query .= " ,T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.ATTENDDATE, ";
        $query .= "         W1.PERIODCD, ";
        $query .= "         CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ";
        $query .= "         L1.MULTIPLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1 ";
        $query .= "         LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ";
        $query .= "     WHERE ";
        $query .= "             W1.ATTENDDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND NOT EXISTS( SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_TRANSFER_DAT T7 ";
        $query .= "                         WHERE ";
        $query .= "                                 T7.SCHREGNO = W1.SCHREGNO ";
        $query .= "                             AND T7.TRANSFERCD IN('1','2') ";
        $query .= "                             AND W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= " ) ";

        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "     ) ";

        $query .= " ,T_attend AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         S1.CLASSCD, ";
            $query .= "         S1.SCHOOL_KIND, ";
            $query .= "         S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER, ";
        $query .= "         COUNT(S3.SCHREGNO)  AS OFFDAYS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8'  THEN 1 ELSE 0 END)  AS  ABSENT, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9'  THEN 1 ELSE 0 END)  AS  SUSPEND, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ";
        $query .= "         SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE,  ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD, ";
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             T2.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHEDULE T1, ";
        $query .= "             CHAIR_STD_DAT T2, ";
        $query .= "             CHAIR_DAT T3, ";
        $query .= "             SCHNO T4 ";
        $query .= "         WHERE ";
        $query .= "                 T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "             AND T3.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "             AND T2.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCH_CHR_COUNTFLG T5 ";
        $query .= "                             WHERE ";
        $query .= "                                 T5.EXECUTEDATE  = T1.EXECUTEDATE AND ";
        $query .= "                                 T5.PERIODCD     = T1.PERIODCD AND ";
        $query .= "                                 T5.CHAIRCD      = T1.CHAIRCD AND ";
        $query .= "                                 T5.GRADE        = T4.GRADE AND ";
        $query .= "                                 T5.HR_CLASS     = T4.HR_CLASS AND ";
        $query .= "                                 T1.DATADIV      IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                                 T5.COUNTFLG     = '0' ";
        $query .= "                           ) ";
        $query .= "             AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                             WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                               AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                               AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                               AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCHREG_BASE_MST T6 ";
        $query .= "                             WHERE ";
        $query .= "                                 T6.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                  (T6.ENT_DIV IN('4','5')     AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                           ) ";
        $query .= "         GROUP BY ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD, ";
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             T2.SEMESTER)S1 ";
        $query .= "     LEFT JOIN T_attend_dat        S2 ON  S2.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                                      AND S2.PERIODCD   = S1.PERIODCD ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON  S3.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S3.TRANSFERCD = '2' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S4 ON  S4.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S4.TRANSFERCD = '1' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         S1.CLASSCD, ";
            $query .= "         S1.SCHOOL_KIND, ";
            $query .= "         S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER ";
        $query .= " ) ";

        $query .= " ,ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD, ";
            $query .= "         W1.SCHOOL_KIND, ";
            $query .= "         W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER, ";
        $query .= "         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           "+ VALUE(W1.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           "+ VALUE(W1.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           "+ VALUE(W1.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=           "+ VALUE(W1.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           "+ VALUE(W1.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           "+ VALUE(W1.KOUDOME,0)";
        }
        $query .= "            ) AS SICK, ";
        $query .= "         SUM(VALUE(W1.NOTICE,0))     NOTICE, ";
        $query .= "         SUM(VALUE(W1.NONOTICE,0))   NONOTICE, ";
        $query .= "         SUM(VALUE(W1.ABSENT,0))     ABSENT, ";
        $query .= "         SUM(VALUE(W1.SUSPEND,0))    SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     SUM(VALUE(W1.VIRUS,0))      VIRUS, ";
        } else {
            $query .= "     INT(0)          VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     SUM(VALUE(W1.KOUDOME,0))    KOUDOME, ";
        } else {
            $query .= "     INT(0)          KOUDOME, ";
        }
        $query .= "         SUM(VALUE(W1.MOURNING,0))   MOURNING, ";
        $query .= "         SUM(VALUE(W1.LATE,0))       LATE, ";
        $query .= "         SUM(VALUE(W1.EARLY,0))      EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS W1, ";
        $query .= "         SCHNO W0 ";
        $query .= "     WHERE ";
        $query .= "         W1.SCHREGNO = W0.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD, ";
            $query .= "         W1.SCHOOL_KIND, ";
            $query .= "         W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         W2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD, ";
            $query .= "         W2.SCHOOL_KIND, ";
            $query .= "         W2.CURRICULUM_CD, ";
        }
        $query .= "         W2.SUBCLASSCD, ";
        $query .= "         W2.SEMESTER, ";
        $query .= "         VALUE(W2.SICK,0) + VALUE(W2.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "          + VALUE(W2.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "          + VALUE(W2.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "          + VALUE(W2.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "          + VALUE(W2.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "          + VALUE(W2.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .= "          + VALUE(W2.KOUDOME,0)";
        }
        $query .= "                AS SICK, ";
        $query .= "         VALUE(W2.NOTICE,0)      NOTICE, ";
        $query .= "         VALUE(W2.NONOTICE,0)    NONOTICE, ";
        $query .= "         VALUE(W2.ABSENT,0)      ABSENT, ";
        $query .= "         VALUE(W2.SUSPEND,0)     SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     VALUE(W2.VIRUS,0)       VIRUS, ";
        } else {
            $query .= "     INT(0)                  VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     VALUE(W2.KOUDOME,0)     KOUDOME, ";
        } else {
            $query .= "     INT(0)                  KOUDOME, ";
        }
        $query .= "         VALUE(W2.MOURNING,0)    MOURNING, ";
        $query .= "         VALUE(W2.LATE,0)        LATE, ";
        $query .= "         VALUE(W2.EARLY,0)       EARLY ";
        $query .= "     FROM ";
        $query .= "         T_attend W2 ";
        $query .= " ) ";

        //学期毎に清算
        $query .= " ,ATTEND_SUM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(ABSENT)     ABSENT, ";
        $query .= "         SUM(SUSPEND)    SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VIRUS)      VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(KOUDOME)    KOUDOME, ";
        }
        $query .= "         SUM(MOURNING)   MOURNING, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "     (SUM(LATE) + SUM(EARLY)) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     0 AS TIKOKU_SOUTAI, ";
            } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     MOD((sum(LATE) + sum(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
            } else {
                $query .= "     (SUM(LATE) + SUM(EARLY)) AS TIKOKU_SOUTAI, ";
            }
        }
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      decimal((float(sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE, ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ((sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE, ";
        } else {
            $query .= "      sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE, ";
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM ";
        $query .= "     WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD ||'-' || SCHOOL_KIND ||'-' || CURRICULUM_CD ||'-' || SUBCLASSCD NOT IN ( ";
            $query .= "             SELECT DISTINCT ";
            $query .= "                 COMBINED_CLASSCD ||'-' || COMBINED_SCHOOL_KIND ||'-' || COMBINED_CURRICULUM_CD ||'-' || COMBINED_SUBCLASSCD ";
        } else {
            $query .= "         SUBCLASSCD NOT IN ( ";
            $query .= "             SELECT DISTINCT ";
            $query .= "                 COMBINED_SUBCLASSCD ";
        }
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '{$ctrl_year}') ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD AS CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
            $query .= "         T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "         T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        }
        $query .= "         T2.SEMESTER, ";
        $query .= "         SUM(T2.SICK)        SICK, ";
        $query .= "         SUM(T2.NOTICE)      NOTICE, ";
        $query .= "         SUM(T2.NONOTICE)    NONOTICE, ";
        $query .= "         SUM(T2.ABSENT)      ABSENT, ";
        $query .= "         SUM(T2.SUSPEND)     SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(T2.VIRUS)       VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(T2.KOUDOME)     KOUDOME, ";
        }
        $query .= "         SUM(T2.MOURNING)    MOURNING, ";
        $query .= "         SUM(T2.LATE)        LATE, ";
        $query .= "         SUM(T2.EARLY)       EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "     (SUM(T2.LATE) + SUM(T2.EARLY)) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     0 AS TIKOKU_SOUTAI, ";
            } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     MOD((sum(T2.LATE) + sum(T2.EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
            } else {
                $query .= "     (SUM(T2.LATE) + SUM(T2.EARLY)) AS TIKOKU_SOUTAI, ";
            }
        }
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      decimal((float(sum(T2.LATE) + sum(T2.EARLY)) / {$absent_cov_late}) + (sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK)),4,1) as NOTICE_LATE, ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ((sum(T2.LATE) + sum(T2.EARLY)) / {$absent_cov_late}) + (sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK)) as NOTICE_LATE, ";
        } else {
            $query .= "      sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK) as NOTICE_LATE, ";
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "         ATTEND_SUM T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$ctrl_year}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.ATTEND_CLASSCD       = T2.CLASSCD AND ";
            $query .= "         T1.ATTEND_SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "         T1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "         T1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         T1.COMBINED_SUBCLASSCD, ";
        $query .= "         T2.SEMESTER ";
        $query .= " ) ";

        //年間で清算
        $query .= " ,ATTEND_SUM3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(ABSENT)     ABSENT, ";
        $query .= "         SUM(SUSPEND)    SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VIRUS)      VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(KOUDOME)    KOUDOME, ";
        }
        $query .= "         SUM(MOURNING)   MOURNING, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS KETUJISU, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
                $query .= "     SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     SUM(TIKOKU_SOUTAI) TIKOKU_SOUTAI, ";
            } else {
                if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      0 AS TIKOKU_SOUTAI, ";
                } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      MOD((SUM(LATE) + SUM(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
                } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
                    $query .= "           THEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) -  {$amari_kuriage} ";
                    $query .= "           ELSE MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) ";
                    $query .= "      END AS TIKOKU_SOUTAI, ";
                } else {
                    $query .= "      SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI, ";
                }
            }
        }
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "     SUM(NOTICE_LATE) NOTICE_LATE, "; //欠課
        } else {
            if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)),4,1) AS NOTICE_LATE, ";
            } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) AS NOTICE_LATE, ";
            } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) + (CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
                $query .= "                                                                                                          THEN 1 ";
                $query .= "                                                                                                          ELSE 0 ";
                $query .= "                                                                                                     END) AS NOTICE_LATE, ";
            } else {
                $query .= "      SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE, ";
            }
        }
        $query .= "         SUM(LATE) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM2 ";
        if($model->field["ATTEND_SUBCLASSCD"] == "" || $model->field["COMBINED_SUBCLASSCD"] == "") {
            $query .= " WHERE ";
        }
        if($model->field["ATTEND_SUBCLASSCD"] == "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN ( ";
                $query .= "                             SELECT ";
                $query .= "                                 ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "     SUBCLASSCD NOT IN ( ";
                $query .= "                             SELECT ";
                $query .= "                                 ATTEND_SUBCLASSCD ";
            }
            $query .= "                             FROM ";
            $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                             WHERE ";
            $query .= "                                 YEAR = '{$ctrl_year}' ";
            $query .= "                             ) ";
        }
        if($model->field["ATTEND_SUBCLASSCD"] == "" && $model->field["COMBINED_SUBCLASSCD"] == "") {
            $query .= "     AND ";
        }
        if($model->field["COMBINED_SUBCLASSCD"] == "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN ( ";
                $query .= "                             SELECT ";
                $query .= "                                 COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
            } else {
                $query .= "     SUBCLASSCD NOT IN ( ";
                $query .= "                             SELECT ";
                $query .= "                                 COMBINED_SUBCLASSCD ";
            }
            $query .= "                             FROM ";
            $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                             WHERE ";
            $query .= "                                 YEAR = '{$ctrl_year}' ";
            $query .= "                             ) ";
        }

        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";

        $query .= " ), ATTEND_SUBCLASSES AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.ATTEND_CLASSCD, ";
            $query .= "         T1.ATTEND_SCHOOL_KIND, ";
            $query .= "         T1.ATTEND_CURRICULUM_CD, ";
            $query .= "         T1.ATTEND_SUBCLASSCD, ";
            $query .= "         T1.COMBINED_CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD, ";
            $query .= "         T1.COMBINED_SUBCLASSCD ";
        } else {
            $query .= "         T1.ATTEND_SUBCLASSCD, ";
            $query .= "         T1.COMBINED_SUBCLASSCD ";
        }
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$ctrl_year}' AND ";
        $query .= "         T1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= " ), RECORD_CREDITS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD, ";
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "         T3.SUBCLASSCD, ";
        $query .= "         L4.CREDITS ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR AND ";
        $query .= "             T3.SEMESTER = T1.SEMESTER AND ";
        $query .= "             T3.CHAIRCD = T1.CHAIRCD ";
        $query .= "         INNER JOIN SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST L4 ON L4.YEAR = T1.YEAR AND ";
        $query .= "             L4.COURSECD = T2.COURSECD AND ";
        $query .= "             L4.MAJORCD = T2.MAJORCD AND ";
        $query .= "             L4.GRADE = T2.GRADE AND ";
        $query .= "             L4.COURSECODE = T2.COURSECODE AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             L4.CLASSCD          = T3.CLASSCD AND ";
            $query .= "             L4.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
            $query .= "             L4.CURRICULUM_CD    = T3.CURRICULUM_CD AND ";
        }
        $query .= "             L4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$ctrl_year}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["GAKKI2"]}' ";
        $query .= " ), COMBINED_CREDITS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.COMBINED_CLASSCD, ";
            $query .= "         T3.COMBINED_SCHOOL_KIND, ";
            $query .= "         T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         T3.COMBINED_SUBCLASSCD, ";
        $query .= "         SUM(T1.CREDITS) AS SUM_CREDITS ";
        $query .= "     FROM ";
        $query .= "         RECORD_CREDITS T1 ";
        $query .= "     INNER JOIN ATTEND_SUBCLASSES T3 ON T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                    AND T3.ATTEND_CLASSCD        = T1.CLASSCD ";
            $query .= "                                    AND T3.ATTEND_SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "                                    AND T3.ATTEND_CURRICULUM_CD  = T1.CURRICULUM_CD ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.COMBINED_CLASSCD, ";
            $query .= "         T3.COMBINED_SCHOOL_KIND, ";
            $query .= "         T3.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         T3.COMBINED_SUBCLASSCD ";
        $query .= " ), T_CREDIT_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SUM_CREDITS AS CREDITS ";
        $query .= "     FROM  ";
        $query .= "         RECORD_CREDITS T1 ";
        $query .= "     INNER JOIN COMBINED_CREDITS T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.COMBINED_CLASSCD         = T1.CLASSCD ";
            $query .= "         AND T2.COMBINED_SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "         AND T2.COMBINED_CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CREDITS ";
        $query .= "     FROM  ";
        $query .= "         RECORD_CREDITS T1 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         COMBINED_CREDITS T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T2.SCHREGNO = T1.SCHREGNO AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T2.COMBINED_CLASSCD         = T1.CLASSCD AND ";
            $query .= "                         T2.COMBINED_SCHOOL_KIND     = T1.SCHOOL_KIND AND ";
            $query .= "                         T2.COMBINED_CURRICULUM_CD   = T1.CURRICULUM_CD AND ";
        }
        $query .= "                         T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";

        //評定とか拾ってくる
        $query .= " ), RECORD_SCORE AS( ";

        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T3.SUBCLASSNAME, ";
        if($model->school_div_name == 'tottori' && $model->field["GAKKI2"] == "9" && $model->field["TESTKIND"] == "9900-2") {
            $query .= "         T1.SCORE AS VALUE, ";
        } else {
            $query .= "         T1.VALUE, ";
        }
        $query .= "         T4.CREDITS, ";
        $query .= "         T1.COMP_CREDIT, ";
        $query .= "         T1.GET_CREDIT, ";
        $query .= "         DECIMAL(ROUND(FLOAT(T5.AVG)*10,0)/10,5,1) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN SCHNO           T2 ON  T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "     LEFT  JOIN SUBCLASS_MST    T3 ON  T3.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT  JOIN T_CREDIT_MST    T4 ON  T4.SCHREGNO   = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T4.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                   AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        if($testkind[1] == "1") {
            $query .= "     LEFT  JOIN RECORD_RANK_DAT T5 ON  T5.YEAR       = T1.YEAR ";
        } else {
            $query .= "     LEFT  JOIN RECORD_RANK_V_DAT T5 ON  T5.YEAR     = T1.YEAR ";
        }
        $query .= "                                   AND T5.SEMESTER   = '{$model->field["GAKKI2"]}' ";
        $query .= "                                   AND T5.TESTKINDCD || T5.TESTITEMCD = '{$testkind[0]}' ";
        $query .= "                                   AND T5.SUBCLASSCD = '999999' ";
        $query .= "                                   AND T5.SCHREGNO   = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '{$ctrl_year}' AND ";
        $query .= "         T1.SEMESTER     = '{$model->field["GAKKI2"]}' AND ";
        $query .= "         T1.TESTKINDCD || T1.TESTITEMCD = '{$testkind[0]}' AND ";
        if($model->field["ATTEND_SUBCLASSCD"] == "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "     T1.SUBCLASSCD NOT IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             ATTEND_SUBCLASSCD ";
            }
            $query .= "                         FROM ";
            $query .= "                             SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '{$ctrl_year}' ";
            $query .= "                         ) AND ";
        }
        if($model->field["COMBINED_SUBCLASSCD"] == "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
            } else {
                $query .= "     T1.SUBCLASSCD NOT IN ( ";
                $query .= "                         SELECT ";
                $query .= "                             COMBINED_SUBCLASSCD ";
            }
            $query .= "                         FROM ";
            $query .= "                             SUBCLASS_REPLACE_COMBINED_DAT ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '{$ctrl_year}' ";
            $query .= "                         ) AND ";
        }
        if($testkind[0] == '9900') {
            $query .= "         T1.SCORE_DIV    = '00' ";
        } else {
            $query .= "         T1.SCORE_DIV    = '01' ";
        }

        //履修単位数・修得単位数 累計
        $query .= " ), STUDYREC AS ( ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.COMP_CREDIT, 0)) AS ST_COMP, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT, 0)) AS ST_GET_ADD ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR < '{$ctrl_year}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        //授業時数を取得
        $query .= " ), SUB_LESSON AS ( ";
        $query .= knjd232yQuery::getLessonSql($ctrl_year, $attend_seme, $month, $attend_sdate, $date, $knjSchoolMst, $model);

        $query .= " ), SUB_LESSON2 AS ( ";
        $query .= " SELECT * FROM SUB_LESSON ";
        $query .= " UNION ";
        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.COMBINED_CLASSCD AS CLASSCD, ";
            $query .= "     T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     SUM(VALUE(T2.LESSON,0)) AS LESSON, ";
        $query .= "     SUM(VALUE(T2.MLESSON,0)) AS MLESSON ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "     SUB_LESSON T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$ctrl_year}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.ATTEND_CLASSCD       = T2.CLASSCD AND ";
            $query .= "     T1.ATTEND_SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "     T1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.COMBINED_CLASSCD, ";
            $query .= "     T1.COMBINED_SCHOOL_KIND, ";
            $query .= "     T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T2.SCHREGNO ";

        /******************/
        /* 特別活動のとき */
        /******************/
        if ($model->cmd == 'csv2_1') {
            $query .= " ), SUB_MAIN AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T2.CLASSCD, ";
                $query .= "     T2.SCHOOL_KIND, ";
                $query .= "     T2.CURRICULUM_CD, ";
            }
            $query .= "     T2.SUBCLASSCD, ";
            $query .= "     CASE L2.NAMESPARE1 ";
            $query .= "          WHEN '1' ";
            $query .= "             THEN T2.NOTICE_LATE2 "; // 欠時 ＋ 遅刻
            $query .= "          WHEN '2' ";
            $query .= "             THEN T2.NOTICE_LATE3 "; // 欠時のみ
            $query .= "          ELSE    T2.NOTICE_LATE ";
            $query .= "     END AS NOTICE_LATE,  ";
            $query .= "     T5.MLESSON AS LESSON, "; //LESSON:授業時数、MLESSON:出席すべき時数
            $query .= "     L1.COMP_ABSENCE_HIGH AS COMP_ABSENCE_HIGH, ";
            $query .= "     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH ";
            $query .= " FROM ";
            $query .= "     SCHNO T1  ";
            $query .= " INNER JOIN ATTEND_SUM3 T2 ON T1.SCHREGNO    = T2.SCHREGNO  ";
            $query .= " LEFT JOIN SUB_LESSON2 T5   ON  T5.SCHREGNO   = T1.SCHREGNO ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                           AND T5.CLASSCD        = T2.CLASSCD  ";
                $query .= "                           AND T5.SCHOOL_KIND    = T2.SCHOOL_KIND  ";
                $query .= "                           AND T5.CURRICULUM_CD  = T2.CURRICULUM_CD  ";
            }
            $query .= "                           AND T5.SUBCLASSCD = T2.SUBCLASSCD  ";
            $query .= " LEFT JOIN RECORD_SCORE T3 ON T3.SCHREGNO    = T1.SCHREGNO  ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                           AND T3.CLASSCD        = T2.CLASSCD  ";
                $query .= "                           AND T3.SCHOOL_KIND    = T2.SCHOOL_KIND  ";
                $query .= "                           AND T3.CURRICULUM_CD  = T2.CURRICULUM_CD  ";
            }
            $query .= "                           AND T3.SUBCLASSCD = T2.SUBCLASSCD  ";
            $query .= " LEFT JOIN SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '{$ctrl_year}'  ";
            $query .= "                                      AND L1.SCHREGNO   = T1.SCHREGNO ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                      AND L1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                      AND L1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                      AND L1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "                                      AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
            $query .= "                                      AND L1.DIV        = '2' ";
            $query .= " LEFT JOIN V_NAME_MST L2   ON  L2.NAMECD1 = 'C005' ";
            $query .= "                           AND L2.YEAR    = '{$ctrl_year}'  ";
            $query .= "                           AND L2.NAME1   = T2.SUBCLASSCD  ";

            $query .= " ), SUB_MAIN_SPECIAL AS ( ";

            $query .= " SELECT ";
            $query .= "     MAX(L1.YEAR) AS YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(T1.NOTICE_LATE * INT(L1.MINUTES)) AS YASUNDA_HUN, ";
            $query .= "     SUM(T1.LESSON * INT(L1.MINUTES))      AS LESSON_HUN, ";
            $query .= "     L1.SPECIAL_GROUP_CD ";
            $query .= " FROM ";
            $query .= "     SUB_MAIN T1 ";
            $query .= " INNER JOIN ATTEND_SUBCLASS_SPECIAL_DAT L1 ON  L1.SUBCLASSCD = T1.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                           AND L1.CLASSCD        = T1.CLASSCD ";
                $query .= "                                           AND L1.SCHOOL_KIND    = T1.SCHOOL_KIND ";
                $query .= "                                           AND L1.CURRICULUM_CD  = T1.CURRICULUM_CD ";
            }
            $query .= "                                           AND L1.YEAR       = '{$ctrl_year}' ";
            $query .= " WHERE ";
            $query .= "     L1.SPECIAL_GROUP_CD IN ('001','002','003','004') ";
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     L1.SPECIAL_GROUP_CD ";

            $query .= " ), SUB_MAIN_JITU_JIFUN AS ( ";

            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SPECIAL_GROUP_CD, ";
            //五捨六入の処理
            $query .="     INT(FLOOR(ROUND(((FLOAT(T1.YASUNDA_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10)) + ";
            $query .="     CASE VALUE(L1.TOKUBETU_KATUDO_KANSAN, '1') ";
            $query .="      WHEN '2' THEN ";
            $query .="          CASE WHEN ROUND((ROUND(((FLOAT(T1.YASUNDA_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10) - TRUNC((ROUND(((FLOAT(T1.YASUNDA_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10), 0), 1) < 0.5  ";
            $query .="          THEN 0 ";
            $query .="          ELSE 1 END ";
            $query .="      WHEN '3' THEN ";
            $query .="          CASE WHEN ROUND((ROUND(((FLOAT(T1.YASUNDA_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10) - TRUNC((ROUND(((FLOAT(T1.YASUNDA_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10), 0), 1) > 0.0  ";
            $query .="          THEN 1 ";
            $query .="          ELSE 0 END ";
            $query .="      WHEN '4' THEN 0 ";
            $query .="      ELSE ";
            $query .="      CASE WHEN ROUND((ROUND(((FLOAT(T1.YASUNDA_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10) - TRUNC((ROUND(((FLOAT(T1.YASUNDA_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10), 0), 1) < 0.6  ";
            $query .="          THEN 0 ";
            $query .="          ELSE 1 END ";
            $query .="      END AS YASUNDA_LESSON,  ";

            $query .="     INT(FLOOR(ROUND(((FLOAT(T1.LESSON_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10)) + ";
            $query .="     CASE VALUE(L1.TOKUBETU_KATUDO_KANSAN, '1') ";
            $query .="      WHEN '2' THEN ";
            $query .="          CASE WHEN ROUND((ROUND(((FLOAT(T1.LESSON_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10) - TRUNC((ROUND(((FLOAT(T1.LESSON_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10), 0), 1) < 0.5  ";
            $query .="          THEN 0 ";
            $query .="          ELSE 1 END ";
            $query .="      WHEN '3' THEN ";
            $query .="          CASE WHEN ROUND((ROUND(((FLOAT(T1.LESSON_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10) - TRUNC((ROUND(((FLOAT(T1.LESSON_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10), 0), 1) > 0.0  ";
            $query .="          THEN 1 ";
            $query .="          ELSE 0 END ";
            $query .="      WHEN '4' THEN 0 ";
            $query .="      ELSE ";
            $query .="      CASE WHEN ROUND((ROUND(((FLOAT(T1.LESSON_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10) - TRUNC((ROUND(((FLOAT(T1.LESSON_HUN) / FLOAT(int(L1.JITU_JIFUN_SPECIAL)) * 10)), 0) / 10), 0), 1) < 0.6  ";
            $query .="          THEN 0 ";
            $query .="          ELSE 1 END ";
            $query .="      END AS LESSON  ";

            $query .= " FROM ";
            $query .= "     SUB_MAIN_SPECIAL T1 ";
            $query .= " LEFT JOIN V_SCHOOL_MST L1 ON L1.YEAR = T1.YEAR ";

            $query .= " ), SUB_MAIN_GOUKEI AS ( ";

            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(T1.YASUNDA_LESSON) AS YASUNDA_LESSON_GOUKEI ";
            $query .= " FROM ";
            $query .= "     SUB_MAIN_JITU_JIFUN T1 ";
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO ";

            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     L1.HR_CLASS, ";
            $query .= "     L1.ATTENDNO, ";
            $query .= "     L1.SCHREGNO, ";
            $query .= "     L1.GRADE, ";
            $query .= "     L1.NAME, ";
            $query .= "     T1.YASUNDA_LESSON, ";
            $query .= "     T1.LESSON, ";
            $query .= "     T1.SPECIAL_GROUP_CD, ";
            if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                $query .= " L3.ABSENCE_HIGH, ";
            } else {
                $query .= " L3.COMP_ABSENCE_HIGH, ";
            }
            $query .= "     L3.GET_ABSENCE_HIGH ";
            $query .= " FROM ";
            $query .= "     SUB_MAIN_JITU_JIFUN T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHNO L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     SUB_MAIN_GOUKEI L2 ON L2.SCHREGNO = T1.SCHREGNO ";
            if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                $query .= " LEFT JOIN CREDIT_SPECIAL_MST L3 ON  L3.YEAR             = '{$ctrl_year}' ";
                $query .= "                                 AND L3.COURSECD         = L1.COURSECD ";
                $query .= "                                 AND L3.MAJORCD          = L1.MAJORCD ";
                $query .= "                                 AND L3.GRADE            = L1.GRADE ";
                $query .= "                                 AND L3.COURSECODE       = L1.COURSECODE ";
                $query .= "                                 AND L3.SPECIAL_GROUP_CD = '999' ";
            } else {
                $query .= " LEFT JOIN SCHREG_ABSENCE_HIGH_SPECIAL_DAT L3 ON  L3.YEAR             = '{$ctrl_year}' ";
                $query .= "                                              AND L3.DIV              = '2' ";
                $query .= "                                              AND L3.SPECIAL_GROUP_CD = '999' ";
                $query .= "                                              AND L3.SCHREGNO         = L1.SCHREGNO ";
            }
            $query .= " WHERE ";
            if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                $query .= "     VALUE(L2.YASUNDA_LESSON_GOUKEI, 0) > VALUE(L3.ABSENCE_HIGH, 999999) ";
            } else {
                $query .= "     VALUE(L2.YASUNDA_LESSON_GOUKEI, 0) > VALUE(L3.COMP_ABSENCE_HIGH, 999999) ";
            }
            $query .= " ORDER BY ";
            $query .= "     L1.HR_CLASS, ";
            $query .= "     L1.ATTENDNO, ";
            $query .= "     L1.SCHREGNO, ";
            $query .= "     T1.SPECIAL_GROUP_CD ";

        /******************************/
        /* 教科・科目 or 総合的な時間 */
        /******************************/
        } else {
            //対象の生徒を取得
            $query .= " ), SUB_MAIN AS ( ";

            if ($model->field["SEISEKI_HUSIN2"] == '1' || $model->field["SEISEKI_HUSIN3"] == '1') {
                $query .= " SELECT ";
                $query .= "     T1.SCHREGNO, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     T2.CLASSCD, ";
                    $query .= "     T2.SCHOOL_KIND, ";
                    $query .= "     T2.CURRICULUM_CD, ";
                }
                $query .= "     T2.SUBCLASSCD ";
                if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                    $query .= " FROM ";
                    $query .= "     SCHNO T1 ";
                    $query .= " INNER JOIN ";
                    $query .= "     ATTEND_SUM3 T2 ON T2.SCHREGNO = T1.SCHREGNO ";
                    $query .= " LEFT  JOIN ";
                    $query .= "     CREDIT_MST L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                    $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
                    $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
                    $query .= "                   AND L1.GRADE      = T1.GRADE ";
                    $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                   AND L1.CLASSCD        = T2.CLASSCD ";
                        $query .= "                   AND L1.SCHOOL_KIND    = T2.SCHOOL_KIND ";
                        $query .= "                   AND L1.CURRICULUM_CD  = T2.CURRICULUM_CD ";
                    }
                    $query .= "                   AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
                } else {
                    $query .= " FROM ";
                    $query .= "     SCHNO T1 ";
                    $query .= " INNER JOIN ";
                    $query .= "     ATTEND_SUM3 T2 ON T1.SCHREGNO = T2.SCHREGNO ";
                    $query .= " LEFT  JOIN ";
                    $query .= "     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                    $query .= "                                AND L1.SCHREGNO   = T1.SCHREGNO ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                                AND L1.CLASSCD       = T2.CLASSCD ";
                        $query .= "                                AND L1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
                        $query .= "                                AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                    }
                    $query .= "                                AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
                    $query .= "                                AND L1.DIV        = '2' ";
                }

                if ($model->field["SEISEKI_HUSIN2"] == '1' && $model->field["SEISEKI_HUSIN3"] == '1') {
                    $query .= " WHERE ";
                    $query .= "     T2.NOTICE_LATE > L1.GET_ABSENCE_HIGH ";
                } elseif ($model->field["SEISEKI_HUSIN2"] == '1') {
                    $query .= " WHERE ";
                    $query .= "     T2.NOTICE_LATE >  L1.GET_ABSENCE_HIGH AND ";
                    if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                        $query .= "     T2.NOTICE_LATE <= L1.ABSENCE_HIGH ";
                    } else {
                        $query .= "     T2.NOTICE_LATE <= L1.COMP_ABSENCE_HIGH ";
                    }
                } elseif ($model->field["SEISEKI_HUSIN3"] == '1') {
                    $query .= " WHERE ";
                    if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                        $query .= "     T2.NOTICE_LATE > L1.ABSENCE_HIGH ";
                    } else {
                        $query .= "     T2.NOTICE_LATE > L1.COMP_ABSENCE_HIGH ";
                    }
                }
            }

            if (($model->field["SEISEKI_HUSIN2"] == '1' || $model->field["SEISEKI_HUSIN3"] == '1') && $model->field["SEISEKI_HUSIN1"] == '1') {
                $query .= " UNION ";
            }

            if ($model->field["SEISEKI_HUSIN1"] == '1') {
                $query .= " SELECT ";
                $query .= "     T1.SCHREGNO, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     T3.CLASSCD, ";
                    $query .= "     T3.SCHOOL_KIND, ";
                    $query .= "     T3.CURRICULUM_CD, ";
                }
                $query .= "     T3.SUBCLASSCD ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     RECORD_SCORE T3 ON T3.SCHREGNO = T1.SCHREGNO ";

                if ($model->field["SEISEKI_HUSIN1"] == '1') {
                    $query .= " WHERE ";
                    if (strlen($model->field["SEISEKI_HUSIN_HYOUTEI_FROM"]) && strlen($model->field["SEISEKI_HUSIN_HYOUTEI_TO"])) {
                        $query .= "    (T3.VALUE  >= {$model->field["SEISEKI_HUSIN_HYOUTEI_FROM"]} ";
                        $query .= " AND T3.VALUE  <= {$model->field["SEISEKI_HUSIN_HYOUTEI_TO"]}) ";
                    } elseif (strlen($model->field["SEISEKI_HUSIN_HYOUTEI_TO"])) {
                        $query .= "    (T3.VALUE  <= {$model->field["SEISEKI_HUSIN_HYOUTEI_TO"]} ";
                        $query .= " OR T3.VALUE IS NULL) ";
                    } else {
                        $query .= " T3.VALUE IS NULL ";
                    }
                }
            }

            if ($model->field["SEISEKI_HUSIN2"] != '1' && $model->field["SEISEKI_HUSIN3"] != '1' && $model->field["SEISEKI_HUSIN1"] != '1') {
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.SCHREGNO, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     CASE WHEN T2.SUBCLASSCD IS NULL THEN T3.CLASSCD ELSE T2.CLASSCD END AS CLASSCD, ";
                    $query .= "     CASE WHEN T2.SUBCLASSCD IS NULL THEN T3.SCHOOL_KIND ELSE T2.SCHOOL_KIND END AS SCHOOL_KIND, ";
                    $query .= "     CASE WHEN T2.SUBCLASSCD IS NULL THEN T3.CURRICULUM_CD ELSE T2.CURRICULUM_CD END AS CURRICULUM_CD, ";
                }
                $query .= "     CASE WHEN T2.SUBCLASSCD IS NULL ";
                $query .= "          THEN T3.SUBCLASSCD ";
                $query .= "          ELSE T2.SUBCLASSCD ";
                $query .= "     END AS SUBCLASSCD ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " LEFT JOIN ";
                $query .= "     ATTEND_SUM3 T2 ON T1.SCHREGNO = T2.SCHREGNO ";
                $query .= " LEFT JOIN ";
                $query .= "     RECORD_SCORE T3 ON T3.SCHREGNO = T1.SCHREGNO ";
            }
            $query .= " ) ";

            //メイン
            $query .= " SELECT ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.NAME, ";
            $query .= "     L2.SUBCLASSNAME, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     L2.SUBCLASSCD, ";
            }
            $query .= "     T3.VALUE, ";
            $query .= "     T3.CREDITS, ";
            $query .= "     T3.COMP_CREDIT, ";
            $query .= "     T3.GET_CREDIT, ";
            $query .= "     T3.VALUATION, ";
            $query .= "     T4.ST_COMP, ";
            $query .= "     T4.ST_GET_ADD, ";
            $query .= "     T2.KETUJISU, "; //欠次数
            $query .= "     T2.TIKOKU_SOUTAI, "; //遅刻・早退
            $query .= "     T2.NOTICE_LATE, "; //欠課
            $query .= "     T2.ABSENT, ";   //公欠数
            $query .= "     T2.MOURNING, "; //忌引数
            $query .= "     T2.SUSPEND ";
            if ($model->Properties["useVirus"] == "true") {
                $query .= "     + T2.VIRUS ";
            }
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "     + T2.KOUDOME ";
            }
            $query .= "         AS SUSPEND, ";   //出停数
            $query .= "     T5.MLESSON AS LESSON, "; //LESSON:授業時数、MLESSON:出席すべき時数
            if ($model->jugyou_jisu_flg == '1') { //1：法定時数 2：実時数
                $query .= "     L1.ABSENCE_HIGH     AS COMP_ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     SUB_MAIN        M1 ON M1.SCHREGNO    = T1.SCHREGNO ";
                $query .= " LEFT JOIN ";
                $query .= "     SUB_LESSON2     T5 ON T5.SCHREGNO    = M1.SCHREGNO ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                       AND T5.CLASSCD        = M1.CLASSCD ";
                    $query .= "                       AND T5.SCHOOL_KIND    = M1.SCHOOL_KIND ";
                    $query .= "                       AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                       AND T2.CLASSCD        = M1.CLASSCD ";
                    $query .= "                       AND T2.SCHOOL_KIND    = M1.SCHOOL_KIND ";
                    $query .= "                       AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                        AND T3.CLASSCD       = M1.CLASSCD ";
                    $query .= "                        AND T3.SCHOOL_KIND   = M1.SCHOOL_KIND ";
                    $query .= "                        AND T3.CURRICULUM_CD = M1.CURRICULUM_CD ";
                }
                $query .= "                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN STUDYREC T4 ON T4.SCHREGNO    = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     CREDIT_MST L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
                $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
                $query .= "                   AND L1.GRADE      = T1.GRADE ";
                $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND L1.CLASSCD        = M1.CLASSCD ";
                    $query .= "                   AND L1.SCHOOL_KIND    = M1.SCHOOL_KIND ";
                    $query .= "                   AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                   AND L1.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN ";
                $query .= "     SUBCLASS_MST L2 ON  M1.SUBCLASSCD = L2.SUBCLASSCD ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                     AND M1.CLASSCD       = L2.CLASSCD ";
                    $query .= "                     AND M1.SCHOOL_KIND   = L2.SCHOOL_KIND ";
                    $query .= "                     AND M1.CURRICULUM_CD = L2.CURRICULUM_CD ";
                }
            } else {
                $query .= "     L1.COMP_ABSENCE_HIGH AS COMP_ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH  AS GET_ABSENCE_HIGH ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     SUB_MAIN    M1 ON M1.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN ";
                $query .= "     SUB_LESSON2     T5 ON T5.SCHREGNO    = M1.SCHREGNO ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                       AND T5.CLASSCD        = M1.CLASSCD ";
                    $query .= "                       AND T5.SCHOOL_KIND    = M1.SCHOOL_KIND ";
                    $query .= "                       AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                       AND T2.CLASSCD        = M1.CLASSCD ";
                    $query .= "                       AND T2.SCHOOL_KIND    = M1.SCHOOL_KIND ";
                    $query .= "                       AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ";
                }
                $query .= "                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ";
                $query .= " LEFT JOIN ";
                $query .= "     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                        AND T3.CLASSCD       = M1.CLASSCD ";
                    $query .= "                        AND T3.SCHOOL_KIND   = M1.SCHOOL_KIND ";
                    $query .= "                        AND T3.CURRICULUM_CD = M1.CURRICULUM_CD ";
                }
                $query .= "                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= " LEFT  JOIN STUDYREC T4 ON  T4.SCHREGNO   = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '{$ctrl_year}' ";
                $query .= "                                AND L1.SCHREGNO   = M1.SCHREGNO ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                                AND L1.CLASSCD       = M1.CLASSCD ";
                    $query .= "                                AND L1.SCHOOL_KIND   = M1.SCHOOL_KIND ";
                    $query .= "                                AND L1.CURRICULUM_CD = M1.CURRICULUM_CD ";
                }
                $query .= "                                AND L1.SUBCLASSCD = M1.SUBCLASSCD ";
                $query .= "                                AND L1.DIV        = '2' ";
                $query .= " LEFT  JOIN ";
                $query .= "     SUBCLASS_MST L2 ON  M1.SUBCLASSCD = L2.SUBCLASSCD ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                     AND M1.CLASSCD       = L2.CLASSCD ";
                    $query .= "                     AND M1.SCHOOL_KIND   = L2.SCHOOL_KIND ";
                    $query .= "                     AND M1.CURRICULUM_CD = L2.CURRICULUM_CD ";
                }
            }
            if ($model->field["KYOUKA_SOUGOU1"] == '1' || $model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= " WHERE ";
            }
            if ($model->field["KYOUKA_SOUGOU1"] == '1' && $model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) <= '90' ";
            } elseif ($model->field["KYOUKA_SOUGOU1"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) < '90' ";
            } elseif ($model->field["KYOUKA_SOUGOU2"] == '1') {
                $query .= "     SUBSTR(M1.SUBCLASSCD,1,2) = '90' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     L2.CLASSCD, ";
                $query .= "     L2.SCHOOL_KIND, ";
                $query .= "     L2.CURRICULUM_CD, ";
            }
            $query .= "     L2.SUBCLASSCD ";
        }

        return $query;
    }


/*************************************************************************************************************/
/**  CSV作成（成績不振者・過去の不認定科目）  ****************************************************************/
/*************************************************************************************************************/
    function selectCsvQuery2_2($model) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);
        $ctrl_year = CTRL_YEAR;

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '{$ctrl_year}' AND ";
        $query .= "         T1.SEMESTER = '{$semester}' AND ";
        $query .= "         VALUE(T2.ENT_DATE, '0001-01-01') <= '{$date}' AND ";
        $query .= "         VALUE(T2.GRD_DATE, '9999-12-31') >= '{$date}' ";
        if ($model->field["GRADE"] != '99') {
            $query .= "         AND GRADE    = '{$model->field["GRADE"]}' ";
        }
        $query .= " ) ";

        $query .= " ,ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD, ";
            $query .= "         W1.SCHOOL_KIND, ";
            $query .= "         W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.YEAR, ";
        $query .= "         W1.SEMESTER, ";
        $query .= "         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0)  ";
        $query .= "             + (CASE W2.SUB_ABSENT   WHEN '1' THEN VALUE(W1.ABSENT,0)    ELSE 0 END ) ";
        $query .= "             + (CASE W2.SUB_SUSPEND  WHEN '1' THEN VALUE(W1.SUSPEND,0)   ELSE 0 END ) ";
        $query .= "             + (CASE W2.SUB_MOURNING WHEN '1' THEN VALUE(W1.MOURNING,0)  ELSE 0 END ) ";
        $query .= "             + (CASE W2.SUB_OFFDAYS  WHEN '1' THEN VALUE(W1.OFFDAYS,0)   ELSE 0 END ) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             + (CASE W2.SUB_VIRUS    WHEN '1' THEN VALUE(W1.VIRUS,0)     ELSE 0 END ) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             + (CASE W2.SUB_KOUDOME  WHEN '1' THEN VALUE(W1.KOUDOME,0)   ELSE 0 END ) ";
        }
        $query .= "         ) AS SICK, ";
        $query .= "         SUM(VALUE(W1.NOTICE,0)) NOTICE, ";
        $query .= "         SUM(VALUE(W1.NONOTICE,0)) NONOTICE, ";
        $query .= "         SUM(VALUE(W1.ABSENT,0)) ABSENT, ";
        $query .= "         SUM(VALUE(W1.SUSPEND,0)) SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VALUE(W1.VIRUS,0)) VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(VALUE(W1.KOUDOME,0)) KOUDOME, ";
        }
        $query .= "         SUM(VALUE(W1.MOURNING,0)) MOURNING, ";
        $query .= "         SUM(VALUE(W1.LATE,0)) LATE, ";
        $query .= "         SUM(VALUE(W1.EARLY,0)) EARLY, ";
        $query .= "         SUM(VALUE(W1.LESSON,0) - VALUE(W1.ABROAD,0) ";
        $query .= "             - (CASE W2.SUB_OFFDAYS WHEN '1' THEN 0 ELSE VALUE(W1.OFFDAYS,0) END ) ";
        $query .= "         ) AS LESSON, ";
        $query .= "         SUM(VALUE(W1.LESSON,0) - VALUE(W1.ABROAD,0) ";
        $query .= "             - (CASE W2.SUB_OFFDAYS WHEN '1' THEN 0 ELSE VALUE(W1.OFFDAYS,0) END ) ";
        $query .= "             - (CASE W2.SUB_SUSPEND WHEN '1' THEN 0 ELSE VALUE(W1.SUSPEND,0) END ) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             - (CASE W2.SUB_VIRUS WHEN '1' THEN 0 ELSE VALUE(W1.VIRUS,0) END ) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             - (CASE W2.SUB_KOUDOME WHEN '1' THEN 0 ELSE VALUE(W1.KOUDOME,0) END ) ";
        }
        $query .= "             - (CASE W2.SUB_MOURNING WHEN '1' THEN 0 ELSE VALUE(W1.MOURNING,0) END ) ";
        $query .= "         ) AS MLESSON ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT W1, ";
        $query .= "         SCHNO W0, ";
        $query .= "         V_SCHOOL_MST W2 ";
        $query .= "     WHERE ";
        $query .= "         W1.SCHREGNO = W0.SCHREGNO AND ";
        $query .= "         W1.YEAR = W2.YEAR ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD, ";
            $query .= "         W1.SCHOOL_KIND, ";
            $query .= "         W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.YEAR, ";
        $query .= "         W1.SEMESTER ";
        $query .= " ) ";

        //学期毎に清算
        $query .= " ,ATTEND_SUM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         SUM(T1.SICK) SICK, ";
        $query .= "         SUM(T1.NOTICE) NOTICE, ";
        $query .= "         SUM(T1.NONOTICE) NONOTICE, ";
        $query .= "         SUM(T1.NOTICE) + SUM(T1.NONOTICE) + SUM(T1.SICK) AS KETUJISU, ";
        $query .= "         SUM(T1.LATE) LATE, ";
        $query .= "         SUM(T1.EARLY) EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "         (SUM(T1.LATE) + SUM(T1.EARLY)) AS TIKOKU_SOUTAI, ";
        } else {
            $query .= "         SUM(CASE WHEN INT(T2.ABSENT_COV_LATE) > 0 THEN  ";
            $query .= "             CASE T2.ABSENT_COV WHEN '3' THEN 0  ";
            $query .= "                                WHEN '1' THEN MOD((T1.LATE + T1.EARLY), T2.ABSENT_COV_LATE) ";
            $query .= "                                ELSE (T1.LATE + T1.EARLY) END ";
            $query .= "             ELSE (T1.LATE + T1.EARLY) END) AS TIKOKU_SOUTAI, ";
        }
        $query .= "         SUM(CASE WHEN INT(T2.ABSENT_COV_LATE) > 0 THEN  ";
        $query .= "             CASE T2.ABSENT_COV WHEN '3' THEN DECIMAL((FLOAT(T1.LATE + T1.EARLY) / T2.ABSENT_COV_LATE) + (T1.NOTICE + T1.NONOTICE + T1.SICK), 4, 1) ";
        $query .= "                                WHEN '1' THEN ((T1.LATE + T1.EARLY) / T2.ABSENT_COV_LATE) + (T1.NOTICE + T1.NONOTICE + T1.SICK) ";
        $query .= "                                ELSE (T1.NOTICE + T1.NONOTICE + T1.SICK) END ";
        $query .= "         ELSE (T1.NOTICE + T1.NONOTICE + T1.SICK) END) AS NOTICE_LATE, ";
        $query .= "         SUM(T1.ABSENT) ABSENT, ";
        $query .= "         SUM(T1.SUSPEND) SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(T1.VIRUS) VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(T1.KOUDOME) KOUDOME, ";
        }
        $query .= "         SUM(T1.MOURNING) MOURNING, ";
        $query .= "         SUM(T1.LESSON) AS LESSON, ";
        $query .= "         SUM(T1.MLESSON) AS MLESSON ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM T1, ";
        $query .= "         V_SCHOOL_MST T2 ";
        $query .= "     WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (  SELECT DISTINCT ";
            $query .= "                                     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
        } else {
            $query .= "         T1.SUBCLASSCD NOT IN (  SELECT DISTINCT ";
            $query .= "                                     COMBINED_SUBCLASSCD ";
        }
        $query .= "                                 FROM ";
        $query .= "                                     SUBCLASS_REPLACE_COMBINED_DAT R1 ";
        $query .= "                                 WHERE ";
        $query .= "                                     R1.YEAR = T1.YEAR ";
        $query .= "                              ) AND ";
        $query .= "         T1.YEAR = T2.YEAR ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD AS CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "         T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         T2.YEAR, ";
        $query .= "         T2.SEMESTER, ";
        $query .= "         SUM(T2.SICK) SICK, ";
        $query .= "         SUM(T2.NOTICE) NOTICE, ";
        $query .= "         SUM(T2.NONOTICE) NONOTICE, ";
        $query .= "         SUM(T2.NOTICE) + SUM(T2.NONOTICE) + SUM(T2.SICK) AS KETUJISU, ";
        $query .= "         SUM(T2.LATE) LATE, ";
        $query .= "         SUM(T2.EARLY) EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "         (SUM(T2.LATE) + SUM(T2.EARLY)) AS TIKOKU_SOUTAI, ";
        } else {
            $query .= "         SUM(CASE WHEN INT(T3.ABSENT_COV_LATE) > 0 THEN  ";
            $query .= "             CASE T3.ABSENT_COV WHEN '3' THEN 0  ";
            $query .= "                                WHEN '1' THEN MOD((T2.LATE + T2.EARLY), T3.ABSENT_COV_LATE) ";
            $query .= "                                ELSE (T2.LATE + T2.EARLY) END ";
            $query .= "             ELSE (T2.LATE + T2.EARLY) END) AS TIKOKU_SOUTAI, ";
        }
        $query .= "         SUM(CASE WHEN INT(T3.ABSENT_COV_LATE) > 0 THEN  ";
        $query .= "             CASE T3.ABSENT_COV WHEN '3' THEN DECIMAL((FLOAT(T2.LATE + T2.EARLY) / T3.ABSENT_COV_LATE) + (T2.NOTICE + T2.NONOTICE + T2.SICK), 4, 1) ";
        $query .= "                                WHEN '1' THEN ((T2.LATE + T2.EARLY) / T3.ABSENT_COV_LATE) + (T2.NOTICE + T2.NONOTICE + T2.SICK) ";
        $query .= "                                ELSE (T2.NOTICE + T2.NONOTICE + T2.SICK) END ";
        $query .= "         ELSE (T2.NOTICE + T2.NONOTICE + T2.SICK) END) AS NOTICE_LATE, ";
        $query .= "         SUM(T2.ABSENT) ABSENT, ";
        $query .= "         SUM(T2.SUSPEND) SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(T2.VIRUS) VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(T2.KOUDOME) KOUDOME, ";
        }
        $query .= "         SUM(T2.MOURNING) MOURNING, ";
        $query .= "         SUM(T2.LESSON) AS LESSON, ";
        $query .= "         SUM(T2.MLESSON) AS MLESSON ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "         ATTEND_SUM T2, ";
        $query .= "         V_SCHOOL_MST T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.YEAR = T3.YEAR AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.ATTEND_CLASSCD       = T2.CLASSCD AND ";
            $query .= "         T1.ATTEND_SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "         T1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "         T1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         T1.COMBINED_SUBCLASSCD, ";
        $query .= "         T2.YEAR, ";
        $query .= "         T2.SEMESTER ";
        $query .= " ) ";

        //年間で清算
        $query .= " ,ATTEND_SUM3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         YEAR, ";
        $query .= "         SUM(SICK) SICK, ";
        $query .= "         SUM(NOTICE) NOTICE, ";
        $query .= "         SUM(NONOTICE) NONOTICE, ";
        $query .= "         SUM(KETUJISU) KETUJISU, ";
        $query .= "         SUM(LATE) LATE, ";
        $query .= "         SUM(EARLY) EARLY, ";
        $query .= "         SUM(TIKOKU_SOUTAI) TIKOKU_SOUTAI, ";
        $query .= "         SUM(NOTICE_LATE) NOTICE_LATE, ";
        $query .= "         SUM(ABSENT) ABSENT, ";
        $query .= "         SUM(SUSPEND) SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         SUM(VIRUS) VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         SUM(KOUDOME) KOUDOME, ";
        }
        $query .= "         SUM(MOURNING) MOURNING, ";
        $query .= "         SUM(LESSON) LESSON, ";
        $query .= "         SUM(MLESSON) MLESSON ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM2  ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         YEAR ";
        $query .= " ) ";

        $query .= " ,ATTEND_SUM4 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SICK, ";
        $query .= "         T1.NOTICE, ";
        $query .= "         T1.NONOTICE, ";
        $query .= "         T1.KETUJISU, ";
        $query .= "         T1.LATE, ";
        $query .= "         T1.EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "         T1.LATE + T1.EARLY AS TIKOKU_SOUTAI, ";
        } else {
            $query .= "         CASE WHEN INT(T2.ABSENT_COV_LATE) > 0 THEN  ";
            $query .= "             CASE T2.ABSENT_COV WHEN '3' THEN T1.TIKOKU_SOUTAI ";
            $query .= "                                WHEN '1' THEN T1.TIKOKU_SOUTAI ";
            $query .= "                                WHEN '4' THEN 0 ";
            $query .= "                                WHEN '2' THEN  MOD((T1.TIKOKU_SOUTAI), T2.ABSENT_COV_LATE) ";
            $query .= "                                WHEN '5' THEN ";
            $query .= "                                     CASE WHEN MOD(T1.TIKOKU_SOUTAI , T2.ABSENT_COV_LATE) >= INT(T2.AMARI_KURIAGE) ";
            $query .= "                                          THEN MOD(T1.TIKOKU_SOUTAI , T2.ABSENT_COV_LATE) -  INT(T2.AMARI_KURIAGE) ";
            $query .= "                                          ELSE MOD(T1.TIKOKU_SOUTAI , T2.ABSENT_COV_LATE) END ";
            $query .= "                                ELSE T1.TIKOKU_SOUTAI END ";
            $query .= "             ELSE T1.TIKOKU_SOUTAI END AS TIKOKU_SOUTAI, ";
        }
        $query .= "         CASE WHEN INT(T2.ABSENT_COV_LATE) > 0 THEN  ";
        $query .= "             CASE T2.ABSENT_COV WHEN '3' THEN T1.NOTICE_LATE ";
        $query .= "                                WHEN '1' THEN T1.NOTICE_LATE ";
        $query .= "                                WHEN '4' THEN DECIMAL((FLOAT(T1.TIKOKU_SOUTAI) / T2.ABSENT_COV_LATE) + T1.NOTICE_LATE,4,1) ";
        $query .= "                                WHEN '2' THEN (T1.TIKOKU_SOUTAI / T2.ABSENT_COV_LATE) + T1.NOTICE_LATE ";
        $query .= "                                WHEN '5' THEN (T1.TIKOKU_SOUTAI / T2.ABSENT_COV_LATE) + T1.NOTICE_LATE +  ";
        $query .= "                                              (CASE WHEN MOD(T1.TIKOKU_SOUTAI , T2.ABSENT_COV_LATE) >= INT(T2.AMARI_KURIAGE) THEN 1 ELSE 0 END) ";
        $query .= "                                ELSE T1.NOTICE_LATE END ";
        $query .= "         ELSE T1.NOTICE_LATE END AS NOTICE_LATE, ";
        $query .= "         T1.ABSENT, ";
        $query .= "         T1.SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         T1.VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         T1.KOUDOME, ";
        }
        $query .= "         T1.MOURNING, ";
        $query .= "         T1.LESSON, ";
        $query .= "         T1.MLESSON ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM3 T1, ";
        $query .= "         V_SCHOOL_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR ";
        if($model->field["ATTEND_SUBCLASSCD"] == "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
                $query .= "                                 SELECT ";
                $query .= "                                     ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "     AND T1.SUBCLASSCD NOT IN ( ";
                $query .= "                                 SELECT ";
                $query .= "                                     ATTEND_SUBCLASSCD ";
            }
            $query .= "                                 FROM ";
            $query .= "                                     SUBCLASS_REPLACE_COMBINED_DAT R1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     R1.YEAR = T1.YEAR ";
            $query .= "                              ) ";
        }
        if($model->field["COMBINED_SUBCLASSCD"] == "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ";
                $query .= "                                 SELECT ";
                $query .= "                                     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
            } else {
                $query .= "     AND T1.SUBCLASSCD NOT IN ( ";
                $query .= "                                 SELECT ";
                $query .= "                                     COMBINED_SUBCLASSCD ";
            }
            $query .= "                                 FROM ";
            $query .= "                                     SUBCLASS_REPLACE_COMBINED_DAT R1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     R1.YEAR = T1.YEAR ";
            $query .= "                              ) ";
        }
        $query .= " ) ";

        //評定等の取得
        $query .= " , STUDYREC AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.YEAR, ";
            $query .= "         T1.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         T1.CLASSCD, ";
                $query .= "         T1.SCHOOL_KIND, ";
                $query .= "         T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD, ";
            $query .= "         T3.SUBCLASSNAME, ";
            $query .= "         T1.VALUATION AS VALUE, ";
            $query .= "         T4.CREDITS, ";
            $query .= "         T1.COMP_CREDIT, ";
            $query .= "         T1.GET_CREDIT ";
            $query .= "     FROM ";
            $query .= "         SCHREG_STUDYREC_DAT T1 ";
            $query .= "     INNER JOIN ";
            $query .= "         SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "     LEFT JOIN ";
            $query .= "         SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                        AND T3.CLASSCD       = T1.CLASSCD ";
                $query .= "                        AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                $query .= "                        AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "     LEFT JOIN ";
            $query .= "         CREDIT_MST T4 ON T4.YEAR = T1.YEAR AND T4.COURSECD = T2.COURSECD AND ";
            $query .= "                          T4.MAJORCD = T2.MAJORCD AND T4.GRADE = T2.GRADE AND ";
            $query .= "                          T4.COURSECODE = T2.COURSECODE AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          T4.CLASSCD         = T1.CLASSCD AND ";
                $query .= "                          T4.SCHOOL_KIND     = T1.SCHOOL_KIND AND ";
                $query .= "                          T4.CURRICULUM_CD   = T1.CURRICULUM_CD AND ";
            }
            $query .= "                          T4.SUBCLASSCD = T1.SUBCLASSCD ";
            if($model->field["ATTEND_SUBCLASSCD"] == "" || $model->field["COMBINED_SUBCLASSCD"] == "") {
                $query .= "     WHERE ";
            }
            if($model->field["ATTEND_SUBCLASSCD"] == "") {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (  SELECT ";
                    $query .= "                                     ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                } else {
                    $query .= "         T1.SUBCLASSCD NOT IN (  SELECT ";
                    $query .= "                                     ATTEND_SUBCLASSCD ";
                }
                $query .= "                                 FROM ";
                $query .= "                                     SUBCLASS_REPLACE_COMBINED_DAT R1 ";
                $query .= "                                 WHERE ";
                $query .= "                                     T1.YEAR = R1.YEAR ";
                $query .= "                                 ) ";
            }
            if($model->field["ATTEND_SUBCLASSCD"] == "" && $model->field["COMBINED_SUBCLASSCD"] == "") {
                $query .= "                                 AND ";
            }
            if($model->field["COMBINED_SUBCLASSCD"] == "") {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (  SELECT ";
                    $query .= "                                     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                } else {
                    $query .= "         T1.SUBCLASSCD NOT IN (  SELECT ";
                    $query .= "                                     COMBINED_SUBCLASSCD ";
                }
                $query .= "                                 FROM ";
                $query .= "                                     SUBCLASS_REPLACE_COMBINED_DAT R2 ";
                $query .= "                                 WHERE ";
                $query .= "                                     T1.YEAR = R2.YEAR ";
                $query .= "                                 ) ";
            }
        $query .= " ) ";

        //対象の生徒を取得
        $query .= " , SUB_MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "         L1.SUBCLASSCD, ";
        $query .= "         L1.YEAR ";
        $query .= "     FROM ";
        $query .= "         SCHNO T1  ";
        $query .= "     INNER JOIN ";
        $query .= "         SCHREG_STUDYREC_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR < '{$ctrl_year}' ";
        $query .= "     WHERE ";
        $query .= "         SUBSTR(L1.SUBCLASSCD,1,2) < '90' AND ";
        $query .= "         VALUE(L1.GET_CREDIT,0) + VALUE(L1.ADD_CREDIT, 0) = 0 ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     M1.YEAR, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     M1.CLASSCD || '-' || M1.SCHOOL_KIND || '-' || M1.CURRICULUM_CD || '-' || M1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     M1.SUBCLASSCD, ";
        }
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T3.VALUE, ";
        $query .= "     T3.CREDITS, ";
        $query .= "     VALUE(T3.COMP_CREDIT, 0) AS COMP_CREDIT, ";
        $query .= "     VALUE(T3.GET_CREDIT, 0) AS GET_CREDIT, ";
        $query .= "     T2.KETUJISU, ";
        $query .= "     T2.TIKOKU_SOUTAI, ";
        $query .= "     T2.NOTICE_LATE, ";
        $query .= "     T2.ABSENT, ";
        $query .= "     T2.MOURNING, ";
        $query .= "     T2.SUSPEND ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     + T2.VIRUS ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     + T2.KOUDOME ";
        }
        $query .= "         AS SUSPEND, ";
        $query .= "     T2.MLESSON AS LESSON ";
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         SUB_MAIN M1     ON M1.SCHREGNO      = T1.SCHREGNO ";
        $query .= "     LEFT JOIN ";
        $query .= "         ATTEND_SUM4 T2  ON T2.SCHREGNO      = M1.SCHREGNO  ";
        $query .= "                        AND T2.YEAR          = M1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T2.CLASSCD       = M1.CLASSCD ";
            $query .= "                        AND T2.SCHOOL_KIND   = M1.SCHOOL_KIND ";
            $query .= "                        AND T2.CURRICULUM_CD = M1.CURRICULUM_CD ";
        }
        $query .= "                        AND T2.SUBCLASSCD    = M1.SUBCLASSCD ";
        $query .= "     LEFT JOIN ";
        $query .= "         STUDYREC T3     ON T3.SCHREGNO      = M1.SCHREGNO ";
        $query .= "                        AND T3.YEAR          = M1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T3.CLASSCD       = M1.CLASSCD ";
            $query .= "                        AND T3.SCHOOL_KIND   = M1.SCHOOL_KIND ";
            $query .= "                        AND T3.CURRICULUM_CD = M1.CURRICULUM_CD ";
        }
        $query .= "                        AND T3.SUBCLASSCD    = M1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     M1.YEAR, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     M1.CLASSCD, ";
            $query .= "     M1.SCHOOL_KIND, ";
            $query .= "     M1.CURRICULUM_CD, ";
        }
        $query .= "     M1.SUBCLASSCD ";

        return $query;
    }

    //教科担任を取得
    function getSubclassStf($model, $year, $schregno, $subclasscd) {

        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];

        $query  = " WITH STAFF_MAX_DAY AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         MAX(T1.APPDATE) AS APPDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         CHAIR_STF_DAT T2, ";
        $query .= "         CHAIR_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.YEAR = T3.YEAR AND ";
        $query .= "         T1.YEAR = '".$year."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "         T1.CHAIRCD = T3.CHAIRCD AND ";
        $query .= "         T2.CHARGEDIV = 1 AND ";
        $query .= "         T1.SCHREGNO = '".$schregno."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "         T3.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), STAFF AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(T2.STAFFCD) AS STAFFCD ";
        $query .= "     FROM ";
        $query .= "         STAFF_MAX_DAY T0, ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         CHAIR_STF_DAT T2, ";
        $query .= "         CHAIR_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T0.YEAR AND ";
        $query .= "         T1.SEMESTER = T0.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T0.SCHREGNO AND ";
        $query .= "         T1.APPDATE = T0.APPDATE AND ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.YEAR = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "         T1.CHAIRCD = T3.CHAIRCD AND ";
        $query .= "         T2.CHARGEDIV = 1 AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "         T3.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF T1 ";
        $query .= "     LEFT JOIN STAFF_MST T2 ON T1.STAFFCD = T2.STAFFCD ";

        return $query;
    }

    //特別活動名を取得
    function getAttend_subclass_special_mst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_MST ";
        $query .= " WHERE ";
        $query .= "     SPECIAL_GROUP_CD IN ('001','002','003','004') ";
        $query .= " ORDER BY ";
        $query .= "     SPECIAL_GROUP_CD ";

        return $query;
    }

    //授業時数(LESSON) 出席すべき時数(MLESSON)
    function getLessonSql($ctrl_year, $attend_seme, $month, $attend_sdate, $date, $knjSchoolMst, $model) {
        $query  = "";
        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     TT1.CLASSCD, ";
            $query .= "     TT1.SCHOOL_KIND, ";
            $query .= "     TT1.CURRICULUM_CD, ";
        }
        $query .= "     TT1.SUBCLASSCD, ";
        $query .= "     TT1.SCHREGNO, ";
        $query .= "     SUM(VALUE(TT1.LESSON,0)) AS LESSON, ";
        $query .= "     SUM(VALUE(TT1.MLESSON,0)) AS MLESSON ";
        $query .= " FROM ";
        $query .= "     ( ";
        //集計テーブル参照
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "     SUM(VALUE(LESSON,0) - VALUE(ABROAD,0)) AS LESSON, ";
            $query .= "     SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) ";
            if ($knjSchoolMst["SUB_SUSPEND"] != "1") {
                $query .=           " - VALUE(SUSPEND,0) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] != "1" && $model->Properties["useVirus"] == "true") {
                $query .=           " - VALUE(VIRUS,0) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] != "1" && $model->Properties["useKoudome"] == "true") {
                $query .=           " - VALUE(KOUDOME,0) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] != "1") {
                $query .=           " - VALUE(MOURNING,0) ";
            }
            $query .= " ) AS MLESSON ";
        } else {
            $query .= "     SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0)) AS LESSON, ";
            $query .= "     SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ";
            if ($knjSchoolMst["SUB_SUSPEND"] != "1") {
                $query .=           " - VALUE(SUSPEND,0) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] != "1" && $model->Properties["useVirus"] == "true") {
                $query .=           " - VALUE(VIRUS,0) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] != "1" && $model->Properties["useKoudome"] == "true") {
                $query .=           " - VALUE(KOUDOME,0) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] != "1") {
                $query .=           " - VALUE(MOURNING,0) ";
            }
            $query .= " ) AS MLESSON ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER <= '{$attend_seme}' ";
        $query .= "         AND SEMESTER || MONTH IN ('". implode($month, "','") ."') ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO ";
        $query .= "     UNION ALL ";
        //時間割テーブル参照
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         TBL.CLASSCD, ";
            $query .= "         TBL.SCHOOL_KIND, ";
            $query .= "         TBL.CURRICULUM_CD, ";
        }
        $query .= "         TBL.SUBCLASSCD, ";
        $query .= "         TBL.SCHREGNO, ";
        $query .= "         SUM(LESSON) AS LESSON, ";
        $query .= "         SUM(LESSON ";
        if ($knjSchoolMst["SUB_SUSPEND"] != "1") {
            $query .=           " - SUSPEND";
        }
        if ($knjSchoolMst["SUB_VIRUS"] != "1" && $model->Properties["useVirus"] == "true") {
            $query .=           " - VIRUS";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] != "1" && $model->Properties["useKoudome"] == "true") {
            $query .=           " - KOUDOME";
        }
        if ($knjSchoolMst["SUB_MOURNING"] != "1") {
            $query .=           " - MOURNING";
        }
        $query .= "         ) AS MLESSON ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T4.CLASSCD, ";
            $query .= "             T4.SCHOOL_KIND, ";
            $query .= "             T4.CURRICULUM_CD, ";
        }
        $query .= "             T4.SUBCLASSCD, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        $query .= "             1 AS LESSON, ";
        $query .= "             (CASE WHEN VALUE(ATT_DI.REP_DI_CD, '0') IN ('2', '9') THEN 1 ELSE 0 END) AS SUSPEND, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             (CASE WHEN VALUE(ATT_DI.REP_DI_CD, '0') IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             (CASE WHEN VALUE(ATT_DI.REP_DI_CD, '0') IN ('25', '26') THEN 1 ELSE 0 END) AS KOUDOME, ";
        }
        $query .= "             (CASE WHEN VALUE(ATT_DI.REP_DI_CD, '0') IN ('3' ,'10') THEN 1 ELSE 0 END) AS MOURNING ";
        $query .= "         FROM ";
        $query .= "             SCH_CHR_DAT     T1 ";
        $query .= "         INNER JOIN ";
        $query .= "             CHAIR_STD_DAT   T2 ON  T2.YEAR     = T1.YEAR ";
        $query .= "                                AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "         INNER JOIN ";
        $query .= "             SCHREG_REGD_DAT T3 ON  T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "                                AND T3.YEAR     = T1.YEAR ";
        $query .= "                                AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         INNER JOIN ";
        $query .= "             CHAIR_DAT       T4 ON  T4.YEAR     = T1.YEAR ";
        $query .= "                                AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "                                AND T4.CHAIRCD  = T1.CHAIRCD ";
        $query .= "         LEFT JOIN ";
        $query .= "             ATTEND_DAT      L1 ON  L1.PERIODCD   = T1.PERIODCD ";
        $query .= "                                AND L1.CHAIRCD    = T1.CHAIRCD ";
        $query .= "                                AND L1.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                                AND L1.SCHREGNO   = T2.SCHREGNO ";
        $query .= "         LEFT JOIN ";
        $query .= "             ATTEND_DI_CD_DAT ATT_DI  ON ATT_DI.YEAR     = L1.YEAR ";
        $query .= "                                     AND ATT_DI.DI_CD    = L1.DI_CD ";
        $query .= "         WHERE T1.YEAR     = '{$ctrl_year}' ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCH_CHR_COUNTFLG E1 ";
        $query .= "                            WHERE E1.EXECUTEDATE   = T1.EXECUTEDATE AND ";
        $query .= "                                  E1.PERIODCD      = T1.PERIODCD AND ";
        $query .= "                                  E1.CHAIRCD       = T1.CHAIRCD AND ";
        $query .= "                                  E1.GRADE         = T3.GRADE AND ";
        $query .= "                                  E1.HR_CLASS      = T3.HR_CLASS AND ";
        $query .= "                                  T1.DATADIV      IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                                  E1.COUNTFLG      = '0') ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                            WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                              AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                              AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                              AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST E2 ";
        $query .= "                            WHERE E2.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                                  ((E2.GRD_DIV IN('1','2','3') AND E2.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                   (E2.ENT_DIV IN('4','5')     AND E2.ENT_DATE > T1.EXECUTEDATE))) ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT E3 ";
        $query .= "                            WHERE E3.SCHREGNO = T2.SCHREGNO AND ";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "                              E3.TRANSFERCD IN('1') AND ";
        } else {
            $query .= "                              E3.TRANSFERCD IN('1','2') AND ";
        }
        $query .= "                                  T1.EXECUTEDATE BETWEEN E3.TRANSFER_SDATE AND E3.TRANSFER_EDATE) ";
        $query .= "         GROUP BY ";
        $query .= "             T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T4.CLASSCD, ";
            $query .= "             T4.SCHOOL_KIND, ";
            $query .= "             T4.CURRICULUM_CD, ";
        }
        $query .= "             T4.SUBCLASSCD, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        $query .= "             ATT_DI.REP_DI_CD ";
        $query .= "         ) TBL ";
        $query .= "     GROUP BY ";
        $query .= "         TBL.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         TBL.CLASSCD, ";
            $query .= "         TBL.SCHOOL_KIND, ";
            $query .= "         TBL.CURRICULUM_CD, ";
        }
        $query .= "         TBL.SUBCLASSCD ";
        $query .= "     ) TT1 ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     TT1.CLASSCD || '-' || TT1.SCHOOL_KIND || '-' || TT1.CURRICULUM_CD || '-' || TT1.SUBCLASSCD NOT IN ( ";
            $query .= "         SELECT DISTINCT ";
            $query .= "             COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
        } else {
            $query .= "     TT1.SUBCLASSCD NOT IN ( ";
            $query .= "         SELECT DISTINCT ";
            $query .= "             COMBINED_SUBCLASSCD ";
        }
        $query .= "         FROM ";
        $query .= "             SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$ctrl_year}') ";
        $query .= " GROUP BY ";
        $query .= "     TT1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     TT1.CLASSCD, ";
            $query .= "     TT1.SCHOOL_KIND, ";
            $query .= "     TT1.CURRICULUM_CD, ";
        }
        $query .= "     TT1.SUBCLASSCD ";
        return $query;
    }

    // 遅刻何回で欠課とするかの指数取得
    function getScAbsentCov($model) {
        $db = Query::dbCheckOut();
        $query = "SELECT * FROM v_school_mst WHERE year = '". CTRL_YEAR ."'";
        $absent = array();
        $absent = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $absent;
    }

    //出欠集計開始日付などを取得
    function getAttendDate($model) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR, ";
        $query .= "     MONTH, ";
        $query .= "     MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <= '{$semester}' AND ";
        $query .= "     SCHREGNO IN (SELECT ";
        $query .= "                     SCHREGNO ";
        $query .= "                  FROM ";
        $query .= "                     SCHREG_REGD_DAT ";
        $query .= "                  WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     SEMESTER <= '{$semester}' ";
        if ($model->field["GRADE"] != '99') {
            $query .= "                 AND GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= "                  ) ";
        $query .= " GROUP BY ";
        $query .= "     SEMESTER, MONTH ";
        $query .= " ORDER BY ";
        $query .= "     2, 3 ";

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

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('H', 'J') ";
        if ($model->field["GRADE"] != '99') {
            $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        }

        return $query;
    }


/*************************************************************************************************************/
/**  CSV作成（出欠状況）  ************************************************************************************/
/*************************************************************************************************************/
    //CVS作成用のQUERY(科目別:対象学期までの累計)
    function selectCsvQuery3($model, $attend_seme, $month, $attend_sdate, $knjSchoolMst) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);
        $absent_cov      = $model->absent_cov;
        $absent_cov_late = $model->absent_cov_late;
        $amari_kuriage   = $model->amari_kuriage;
        $ctrl_year = CTRL_YEAR;
        $last_year = CTRL_YEAR-1;

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND VALUE(GRD_DATE, '9999-12-31') >= '{$date}' ";
        $query .= "         AND VALUE(ENT_DATE, '0001-01-01') <= '{$date}' ";
        if ($model->field["GRADE"] != '99') {
            $query .= "         AND GRADE    = '{$model->field["GRADE"]}' ";
        }
        $query .= " ) ";

        //過去の出欠集計の開始年度
        $query .= " , YEAR_FROM AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         MIN(T3.YEAR) AS S_YEAR ";
        $query .= "     FROM ";
        $query .= "         SCHNO T1 ";
        $query .= "         LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'A023' AND T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";
        $query .= "         LEFT JOIN SCHREG_REGD_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO AND T2.NAME2 <= T3.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T3.YEAR < '{$ctrl_year}' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ) ";

        //留年生の判定
        $query .= " , YEAR_GRADE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.GRADE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHNO T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";
        $query .= " , GRADE_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         COUNT(YEAR) CNT ";
        $query .= "     FROM ";
        $query .= "         YEAR_GRADE ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE ";
        $query .= " ) ";
        $query .= " , REMAIN_CHECK AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "     SUM(CASE CNT WHEN 1 THEN 0 ELSE CNT END) AS REMAIN ";
        $query .= "     FROM ";
        $query .= "         GRADE_CNT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " ,ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER <= '{$attend_seme}' ";
        $query .= "         AND SEMESTER || MONTH IN ('". implode($month, "','") ."') ";
        $query .= " ) ";

        $query .= " ,SCHEDULE AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT ";
        $query .= "     WHERE ";
        $query .= "             EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND PERIODCD != '0' ";
        $query .= "     GROUP BY ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD ";
        $query .= " ) ";

        $query .= " ,T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.ATTENDDATE, ";
        $query .= "         W1.PERIODCD, ";
        $query .= "         CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ";
        $query .= "         L1.MULTIPLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1 ";
        $query .= "         LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ";
        $query .= "     WHERE ";
        $query .= "             W1.ATTENDDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND NOT EXISTS( SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_TRANSFER_DAT T7 ";
        $query .= "                         WHERE ";
        $query .= "                                 T7.SCHREGNO = W1.SCHREGNO ";
        $query .= "                             AND T7.TRANSFERCD IN('1','2') ";
        $query .= "                             AND W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= " ) ";

        $query .= " ,T_attend AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         S1.CLASSCD, ";
            $query .= "         S1.SCHOOL_KIND, ";
            $query .= "         S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER, ";
        $query .= "         COUNT(S3.SCHREGNO)  AS OFFDAYS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8'  THEN 1 ELSE 0 END)  AS  ABSENT, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9'  THEN 1 ELSE 0 END)  AS  SUSPEND, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ";
        $query .= "         SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD, ";
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             T2.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHEDULE T1, ";
        $query .= "             CHAIR_STD_DAT T2, ";
        $query .= "             CHAIR_DAT T3, ";
        $query .= "             SCHNO T4 ";
        $query .= "         WHERE ";
        $query .= "                 T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "             AND T3.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "             AND T2.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCH_CHR_COUNTFLG T5 ";
        $query .= "                             WHERE ";
        $query .= "                                 T5.EXECUTEDATE  = T1.EXECUTEDATE AND ";
        $query .= "                                 T5.PERIODCD     = T1.PERIODCD AND ";
        $query .= "                                 T5.CHAIRCD      = T1.CHAIRCD AND ";
        $query .= "                                 T5.GRADE        = T4.GRADE AND ";
        $query .= "                                 T5.HR_CLASS     = T4.HR_CLASS AND ";
        $query .= "                                 T5.COUNTFLG     = '0' ";
        $query .= "                           ) ";
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCHREG_BASE_MST T6 ";
        $query .= "                             WHERE ";
        $query .= "                                 T6.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                  (T6.ENT_DIV IN('4','5')     AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                           ) ";
        $query .= "     GROUP BY ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD, ";
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         T2.SEMESTER)S1 ";
        $query .= "     LEFT JOIN T_attend_dat        S2 ON  S2.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                                      AND S2.PERIODCD   = S1.PERIODCD ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON  S3.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S3.TRANSFERCD = '2' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S4 ON  S4.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S4.TRANSFERCD = '1' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         S1.CLASSCD, ";
            $query .= "         S1.SCHOOL_KIND, ";
            $query .= "         S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER ";
        $query .= " ) ";

        $query .= " ,ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD, ";
            $query .= "         W1.SCHOOL_KIND, ";
            $query .= "         W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER, ";
        $query .= "         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           "+ VALUE(W1.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           "+ VALUE(W1.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           "+ VALUE(W1.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=           "+ VALUE(W1.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           "+ VALUE(W1.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           "+ VALUE(W1.KOUDOME,0)";
        }
        $query .= "            ) AS SICK, ";
        $query .= "         SUM(VALUE(W1.NOTICE,0))     NOTICE, ";
        $query .= "         SUM(VALUE(W1.NONOTICE,0))   NONOTICE, ";
        $query .= "         SUM(VALUE(W1.LATE,0))       LATE, ";
        $query .= "         SUM(VALUE(W1.EARLY,0))      EARLY ";
        if (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0) {
            $query .= "    ,SUM(VALUE(W1.LATE,0) + VALUE(W1.EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}     CONVERTED_LATE_EARLY ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS W1, ";
        $query .= "         SCHNO W0 ";
        $query .= "     WHERE ";
        $query .= "         W1.SCHREGNO = W0.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD, ";
            $query .= "         W1.SCHOOL_KIND, ";
            $query .= "         W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         W2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD, ";
            $query .= "         W2.SCHOOL_KIND, ";
            $query .= "         W2.CURRICULUM_CD, ";
        }
        $query .= "         W2.SUBCLASSCD, ";
        $query .= "         W2.SEMESTER, ";
        $query .= "         VALUE(W2.SICK,0) + VALUE(W2.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "          + VALUE(W2.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "          + VALUE(W2.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "          + VALUE(W2.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "          + VALUE(W2.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "          + VALUE(W2.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .= "          + VALUE(W2.KOUDOME,0)";
        }
        $query .= "                AS SICK, ";
        $query .= "         VALUE(W2.NOTICE,0)      NOTICE, ";
        $query .= "         VALUE(W2.NONOTICE,0)    NONOTICE, ";
        $query .= "         VALUE(W2.LATE,0)        LATE, ";
        $query .= "         VALUE(W2.EARLY,0)       EARLY ";
        if (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0) {
            $query .= "    ,(VALUE(W2.LATE,0) + VALUE(W2.EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}     CONVERTED_LATE_EARLY ";
        }
        $query .= "     FROM ";
        $query .= "         T_attend W2 ";
        $query .= " ) ";

        //過去の出欠データ加算
        if ($model->field["ZAISEKI_ALL"] == "1") {
            $query .= " ,ATTEND_PAST AS ( ";
            $query .= "     SELECT * FROM ATTEND_SUM ";
            $query .= "     UNION ALL ";
            $query .= "     SELECT ";
            $query .= "         W3.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         W3.CLASSCD, ";
                $query .= "         W3.SCHOOL_KIND, ";
                $query .= "         W3.CURRICULUM_CD, ";
            }
            $query .= "         W3.SUBCLASSCD, ";
            $query .= "         '{$semester}' AS SEMESTER, ";
            $query .= "         SUM(VALUE(W3.SICK,0) + VALUE(W3.NURSEOFF,0) ";
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "          + VALUE(W3.ABSENT,0) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "          + VALUE(W3.SUSPEND,0) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "          + VALUE(W3.MOURNING,0) ";
            }
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "          + VALUE(W3.OFFDAYS,0) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "          + VALUE(W3.VIRUS,0) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "          + VALUE(W3.KOUDOME,0) ";
            }
            $query .= "             ) AS SICK, ";
            $query .= "         SUM(VALUE(W3.NOTICE,0))    NOTICE, ";
            $query .= "         SUM(VALUE(W3.NONOTICE,0))  NONOTICE, ";
            $query .= "         SUM(VALUE(W3.LATE,0))      LATE, ";
            $query .= "         SUM(VALUE(W3.EARLY,0))     EARLY ";
            if (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0) {
                $query .= "    ,SUM(VALUE(W3.LATE,0) + VALUE(W3.EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}    CONVERTED_LATE_EARLY ";
            }
            $query .= "     FROM ";
            $query .= "         ATTEND_SUBCLASS_DAT W3, ";
            $query .= "         YEAR_FROM W4 ";
            $query .= "     WHERE ";
            $query .= "         W3.SCHREGNO = W4.SCHREGNO AND ";
            $query .= "         W3.SCHREGNO IN (SELECT SCHREGNO FROM ATTEND_SUM) AND ";
            $query .= "         W3.YEAR BETWEEN W4.S_YEAR AND '{$last_year}' ";
            $query .= "     GROUP BY ";
            $query .= "         W3.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         W3.CLASSCD, ";
                $query .= "         W3.SCHOOL_KIND, ";
                $query .= "         W3.CURRICULUM_CD, ";
            }
            $query .= "         W3.SUBCLASSCD ";
            $query .= " ) ";
        }

        //学期毎に清算
        $query .= " ,ATTEND_SUM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE) AS KETSUJI, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "     SUM(LATE)       LATE, ";
            $query .= "     SUM(EARLY)      EARLY, ";
        } else {
            if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     0 AS LATE, ";
                $query .= "     0 AS EARLY, ";
            } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     SUM(CASE WHEN CONVERTED_LATE_EARLY >= LATE THEN 0 ELSE (LATE - CONVERTED_LATE_EARLY) END) AS LATE, ";
                $query .= "     SUM(CASE WHEN CONVERTED_LATE_EARLY >= LATE THEN (EARLY + LATE - CONVERTED_LATE_EARLY) ELSE EARLY END) AS EARLY, ";
            } else {
                $query .= "     SUM(LATE)       LATE, ";
                $query .= "     SUM(EARLY)      EARLY, ";
            }
        }
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "     decimal((float(sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE, ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "    ((sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE, ";
        } else {
            $query .= "     sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE, ";
        }
        $query .= "         SUM(LATE)       SUBCLASS_LATE, ";
        $query .= "         SUM(EARLY)      SUBCLASS_EARLY ";
        $query .= "     FROM ";
        if ($model->field["ZAISEKI_ALL"] == "1") {
            $query .= "     ATTEND_PAST ";
        } else {
            $query .= "     ATTEND_SUM ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER ";
        $query .= " ) ";

        //年間で清算
        $query .= " ,ATTEND_SUM3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(KETSUJI)    KETSUJI, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "     SUM(LATE)       LATE, ";
            $query .= "     SUM(EARLY)      EARLY, ";
        } else {
            if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     0 AS LATE, ";
                $query .= "     0 AS EARLY, ";
            } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     CASE WHEN (SUM(VALUE(LATE,0) + VALUE(EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}) >= SUM(VALUE(LATE,0)) THEN 0 ";
                $query .= "          ELSE SUM(VALUE(LATE,0)) - (SUM(VALUE(LATE,0) + VALUE(EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}) END AS LATE, ";
                $query .= "     CASE WHEN (SUM(VALUE(LATE,0) + VALUE(EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}) >= SUM(VALUE(LATE,0)) ";
                $query .= "          THEN SUM(VALUE(LATE,0)) + SUM(VALUE(EARLY,0)) - (SUM(VALUE(LATE,0) + VALUE(EARLY,0)) / {$absent_cov_late} * {$absent_cov_late})  ";
                $query .= "          ELSE SUM(VALUE(EARLY,0)) END AS EARLY, ";
            } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     CASE WHEN ((SUM(VALUE(LATE,0) + VALUE(EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}) + (CASE WHEN MOD(SUM(VALUE(LATE,0) + VALUE(EARLY,0)) , {$absent_cov_late}) >= {$amari_kuriage} THEN {$amari_kuriage} ELSE 0 END)) >= SUM(VALUE(LATE,0)) THEN 0 ";
                $query .= "          ELSE SUM(VALUE(LATE,0)) - ((SUM(VALUE(LATE,0) + VALUE(EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}) + (CASE WHEN MOD(SUM(VALUE(LATE,0) + VALUE(EARLY,0)) , {$absent_cov_late}) >= {$amari_kuriage} THEN {$amari_kuriage} ELSE 0 END)) END AS LATE, ";
                $query .= "     CASE WHEN ((SUM(VALUE(LATE,0) + VALUE(EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}) + (CASE WHEN MOD(SUM(VALUE(LATE,0) + VALUE(EARLY,0)) , {$absent_cov_late}) >= 2 THEN 2 ELSE 0 END)) >= SUM(VALUE(LATE,0))  ";
                $query .= "          THEN SUM(VALUE(LATE,0)) + SUM(VALUE(EARLY,0)) - ((SUM(VALUE(LATE,0) + VALUE(EARLY,0)) / {$absent_cov_late} * {$absent_cov_late}) + (CASE WHEN MOD(SUM(VALUE(LATE,0) + VALUE(EARLY,0)) , {$absent_cov_late}) >= 2 THEN 2 ELSE 0 END)) ";
                $query .= "          ELSE SUM(VALUE(EARLY,0)) END AS EARLY,  ";
            } else {
                $query .= "     SUM(LATE)       LATE, ";
                $query .= "     SUM(EARLY)      EARLY, ";
            }
        }
        $query .= "         SUM(NOTICE_LATE) NOTICE_LATE1, ";
        if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "     DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)),4,1) AS NOTICE_LATE2, ";
        } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "     ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) AS NOTICE_LATE2, ";
        } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "     ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) + (CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
            $query .= "                                                                                                         THEN 1 ";
            $query .= "                                                                                                         ELSE 0 ";
            $query .= "                                                                                                    END) AS NOTICE_LATE2, ";
        } else {
            $query .= "     SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        }
        $query .= "         SUM(SUBCLASS_LATE)  SUBCLASS_LATE, ";
        $query .= "         SUM(SUBCLASS_EARLY)  SUBCLASS_EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM2 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= " ) ";

        //学籍番号でグルーピング
        $query .= ",ATTEND_SUM4 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "  SUM(NOTICE_LATE1) AS NOTICE_LATE, ";
        } else {
            $query .= "  SUM(NOTICE_LATE2) AS NOTICE_LATE, ";
        }
        $query .= "         SUM(SUBCLASS_LATE) AS SUBCLASS_LATE, ";
        $query .= "         SUM(SUBCLASS_EARLY) AS SUBCLASS_EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM3 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        //遅刻・早退・欠席取得
        $query .= " ,ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "        + VALUE(OFFDAYS,0) ";
        }
        $query .= "         ) AS NOTICE, ";
        $query .= "         SUM(VALUE(LATE,0))  LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$ctrl_year}' AND ";
        $query .= "         SEMESTER <= '{$attend_seme}' AND ";
        $query .= "         SEMESTER || MONTH IN ('". implode($month, "','") ."') ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        //過去の出欠データ加算
        if ($model->field["ZAISEKI_ALL"] == "1") {
            //遅刻・早退・欠席取得
            $query .= " ,ATTEND_SEMES_PAST AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ";
            if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "        + VALUE(OFFDAYS,0) ";
            }
            $query .= "         ) AS NOTICE, ";
            $query .= "         SUM(VALUE(LATE,0))  LATE, ";
            $query .= "         SUM(VALUE(EARLY,0)) EARLY ";
            $query .= "     FROM ";
            $query .= "         SCHNO T1 ";
            $query .= "         LEFT JOIN YEAR_FROM L1 ";
            $query .= "                  ON T1.SCHREGNO = L1.SCHREGNO ";
            $query .= "         LEFT JOIN ATTEND_SEMES_DAT L2 ";
            $query .= "                  ON T1.SCHREGNO = L2.SCHREGNO ";
            $query .= "                 AND (  (L2.YEAR BETWEEN L1.S_YEAR AND '{$last_year}') OR ";
            $query .= "                        (YEAR = '{$ctrl_year}' AND ";
            $query .= "                         SEMESTER <= '{$attend_seme}' AND ";
            $query .= "                         SEMESTER || MONTH IN ('". implode($month, "','") ."'))) ";
            $query .= "     GROUP BY ";
            $query .= "         T1.SCHREGNO ";
            $query .= " ) ";
        }

        //メイン
        /**********/
        /* 皆勤者 */
        /**********/
        if ($model->cmd  == 'csv5') { //皆勤者
            if ($model->field["KAIKINSYA"] == '1') {
                $query .= " ,SUB_MAIN AS ( ";
                $query .= " SELECT ";
                $query .= "     T1.GRADE, ";
                $query .= "     T1.HR_CLASS, ";
                $query .= "     T1.ATTENDNO, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T1.NAME, ";
                $query .= "     0 AS LATE, ";
                $query .= "     0 AS EARLY, ";
                $query .= "     0 AS NOTICE, ";
                $query .= "     T2.NOTICE_LATE, ";
                $query .= "     T2.SUBCLASS_LATE, ";
                $query .= "     T2.SUBCLASS_EARLY ";
                $query .= " FROM ";
                $query .= "     SCHNO T1, ";
                $query .= "     ATTEND_SUM4 T2 ";
                $query .= " WHERE ";
                $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
                $query .= " UNION ALL ";
                $query .= " SELECT ";
                $query .= "     T1.GRADE, ";
                $query .= "     T1.HR_CLASS, ";
                $query .= "     T1.ATTENDNO, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T1.NAME, ";
                $query .= "     T3.LATE, ";
                $query .= "     T3.EARLY, ";
                $query .= "     T3.NOTICE, ";
                $query .= "     0 AS NOTICE_LATE, ";
                $query .= "     0 AS SUBCLASS_LATE, ";
                $query .= "     0 AS SUBCLASS_EARLY ";
                $query .= " FROM ";
                $query .= "     SCHNO T1, ";
                if ($model->field["ZAISEKI_ALL"] == '1') {
                    $query .= "     ATTEND_SEMES_PAST T3 ";
                } else {
                    $query .= "     ATTEND_SEMES T3 ";
                }
                $query .= " WHERE ";
                $query .= "     T1.SCHREGNO = T3.SCHREGNO ";
                $query .= " ) ";

                //留年生は除く
                if ($model->field["ZAISEKI_ALL"] == '1') {
                    $query .= " ,MAIN AS ( ";
                    $query .= "     SELECT ";
                    $query .= "         * ";
                    $query .= "     FROM ";
                    $query .= "         SUB_MAIN ";
                    $query .= "     WHERE ";
                    $query .= "         SCHREGNO IN (SELECT ";
                    $query .= "                         SCHREGNO ";
                    $query .= "                     FROM ";
                    $query .= "                         REMAIN_CHECK ";
                    $query .= "                     WHERE ";
                    $query .= "                         REMAIN = 0 ";
                    $query .= "                     ) ";
                    $query .= " )  ";
                }

                $query .= " SELECT ";
                $query .= "     GRADE, ";
                $query .= "     HR_CLASS, ";
                $query .= "     ATTENDNO, ";
                $query .= "     SCHREGNO, ";
                $query .= "     NAME, ";
                $query .= "     SUM(LATE) AS LATE, ";
                $query .= "     SUM(EARLY) AS EARLY, ";
                $query .= "     SUM(NOTICE) AS NOTICE, ";
                $query .= "     SUM(NOTICE_LATE) AS NOTICE_LATE, ";
                $query .= "     SUM(SUBCLASS_LATE) AS SUBCLASS_LATE, ";
                $query .= "     SUM(SUBCLASS_EARLY) AS SUBCLASS_EARLY ";
                $query .= " FROM ";
                if ($model->field["ZAISEKI_ALL"] == '1') {
                    $query .= "     MAIN ";
                } else {
                    $query .= "     SUB_MAIN ";
                }
                $query .= " GROUP BY ";
                $query .= "     GRADE, ";
                $query .= "     HR_CLASS, ";
                $query .= "     ATTENDNO, ";
                $query .= "     SCHREGNO, ";
                $query .= "     NAME ";
                $query .= " HAVING ";

                $and = '';
                if (strlen($model->field["KAIKIN_KAIKIN_TIKOKU"])) {
                    $query .= "  {$and} SUM(LATE)   <= {$model->field["KAIKIN_KAIKIN_TIKOKU"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_KAIKIN_SOUTAI"])) {
                    $query .= "  {$and} SUM(EARLY)   <= {$model->field["KAIKIN_KAIKIN_SOUTAI"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_KAIKIN_KESSEKI"])) {
                    $query .= "  {$and} SUM(NOTICE)   <= {$model->field["KAIKIN_KAIKIN_KESSEKI"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_KAIKIN_KEKKA"])) {
                    $query .= "  {$and} SUM(NOTICE_LATE)   <= {$model->field["KAIKIN_KAIKIN_KEKKA"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_KAIKIN_JUGYO_TIKOKU"])) {
                    $query .= "  {$and} SUM(SUBCLASS_LATE)   <= {$model->field["KAIKIN_KAIKIN_JUGYO_TIKOKU"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_KAIKIN_JUGYO_SOUTAI"])) {
                    $query .= "  {$and} SUM(SUBCLASS_EARLY)   <= {$model->field["KAIKIN_KAIKIN_JUGYO_SOUTAI"]} ";
                    $and = 'AND';
                }
                $query .= " ORDER BY ";
                $query .= "     LATE DESC, ";
                $query .= "     EARLY DESC, ";
                $query .= "     NOTICE DESC, ";
                $query .= "     NOTICE_LATE DESC, ";
                $query .= "     SUBCLASS_LATE DESC, ";
                $query .= "     SUBCLASS_EARLY DESC, ";
                $query .= "     GRADE, ";
                $query .= "     HR_CLASS, ";
                $query .= "     ATTENDNO ";

                return $query;
            }
            if ($model->field["KAIKINSYA"] == '2') { //精勤者の時
                $query .= " ,SUB_MAIN AS ( ";
                $query .= " SELECT ";
                $query .= "     T1.GRADE, ";
                $query .= "     T1.HR_CLASS, ";
                $query .= "     T1.ATTENDNO, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T1.NAME, ";
                $query .= "     0 AS LATE, ";
                $query .= "     0 AS EARLY, ";
                $query .= "     0 AS NOTICE, ";
                $query .= "     0 AS NOTICE_LATE, ";
                $query .= "     0 AS SUBCLASS_LATE, ";
                $query .= "     0 AS SUBCLASS_EARLY, ";
                $query .= "     T4.LATE AS SHR_LATE, ";
                $query .= "     T4.EARLY AS SHR_EARLY ";
                $query .= " FROM ";
                $query .= "     SCHNO T1, ";
                $query .= "     ATTEND_SUM3 T4 ";
                $query .= " WHERE ";
                $query .= "     T1.SCHREGNO = T4.SCHREGNO AND ";
                $query .= "     SUBSTR(T4.SUBCLASSCD,1,2) >= '91' ";
                $query .= " UNION ALL ";
                $query .= " SELECT ";
                $query .= "     T1.GRADE, ";
                $query .= "     T1.HR_CLASS, ";
                $query .= "     T1.ATTENDNO, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T1.NAME, ";
                $query .= "     0 AS LATE, ";
                $query .= "     0 AS EARLY, ";
                $query .= "     0 AS NOTICE, ";
                $query .= "     T2.NOTICE_LATE, ";
                $query .= "     T2.SUBCLASS_LATE, ";
                $query .= "     T2.SUBCLASS_EARLY, ";
                $query .= "     0 AS SHR_LATE, ";
                $query .= "     0 AS SHR_EARLY ";
                $query .= " FROM ";
                $query .= "     SCHNO T1, ";
                $query .= "     ATTEND_SUM4 T2 ";
                $query .= " WHERE ";
                $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
                $query .= " UNION ALL ";
                $query .= " SELECT ";
                $query .= "     T1.GRADE, ";
                $query .= "     T1.HR_CLASS, ";
                $query .= "     T1.ATTENDNO, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T1.NAME, ";
                $query .= "     T3.LATE, ";
                $query .= "     T3.EARLY, ";
                $query .= "     T3.NOTICE, ";
                $query .= "     0 AS NOTICE_LATE, ";
                $query .= "     0 AS SUBCLASS_LATE, ";
                $query .= "     0 AS SUBCLASS_EARLY, ";
                $query .= "     0 AS SHR_LATE, ";
                $query .= "     0 AS SHR_EARLY ";
                $query .= " FROM ";
                $query .= "     SCHNO T1, ";
                if ($model->field["ZAISEKI_ALL"] == '1') {
                    $query .= "     ATTEND_SEMES_PAST T3 ";
                } else {
                    $query .= "     ATTEND_SEMES T3 ";
                }
                $query .= " WHERE ";
                $query .= "     T1.SCHREGNO = T3.SCHREGNO ";
                $query .= " ) ";

                //留年生は除く
                if ($model->field["ZAISEKI_ALL"] == '1') {
                    $query .= " ,MAIN AS ( ";
                    $query .= "     SELECT ";
                    $query .= "         * ";
                    $query .= "     FROM ";
                    $query .= "         SUB_MAIN ";
                    $query .= "     WHERE ";
                    $query .= "         SCHREGNO IN (SELECT ";
                    $query .= "                         SCHREGNO ";
                    $query .= "                     FROM ";
                    $query .= "                         REMAIN_CHECK ";
                    $query .= "                     WHERE ";
                    $query .= "                         REMAIN = 0 ";
                    $query .= "                     ) ";
                    $query .= " )  ";
                }

                $query .= " SELECT ";
                $query .= "     GRADE, ";
                $query .= "     HR_CLASS, ";
                $query .= "     ATTENDNO, ";
                $query .= "     SCHREGNO, ";
                $query .= "     NAME, ";
                $query .= "     SUM(LATE) AS LATE, ";
                $query .= "     SUM(EARLY) AS EARLY, ";
                $query .= "     SUM(NOTICE) AS NOTICE, ";
                $query .= "     SUM(NOTICE_LATE) AS NOTICE_LATE, ";
                $query .= "     SUM(SUBCLASS_LATE) AS SUBCLASS_LATE, ";
                $query .= "     SUM(SUBCLASS_EARLY) AS SUBCLASS_EARLY, ";
                $query .= "     SUM(SHR_LATE) AS SHR_LATE, ";
                $query .= "     SUM(SHR_EARLY) AS SHR_EARLY ";
                $query .= " FROM ";
                if ($model->field["ZAISEKI_ALL"] == '1') {
                    $query .= "     MAIN ";
                } else {
                    $query .= "     SUB_MAIN ";
                }
                $query .= " GROUP BY ";
                $query .= "     GRADE, ";
                $query .= "     HR_CLASS, ";
                $query .= "     ATTENDNO, ";
                $query .= "     SCHREGNO, ";
                $query .= "     NAME ";
                $query .= " HAVING ";

                $and = '';
                if (strlen($model->field["KAIKIN_SEIKIN_TIKOKU"])) {
                    $query .= "  {$and} SUM(LATE)   <= {$model->field["KAIKIN_SEIKIN_TIKOKU"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_SEIKIN_SOUTAI"])) {
                    $query .= "  {$and} SUM(EARLY)   <= {$model->field["KAIKIN_SEIKIN_SOUTAI"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_SEIKIN_KESSEKI"])) {
                    $query .= "  {$and} SUM(NOTICE)   <= {$model->field["KAIKIN_SEIKIN_KESSEKI"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_SEIKIN_KEKKA"])) {
                    $query .= "  {$and} SUM(NOTICE_LATE)   <= {$model->field["KAIKIN_SEIKIN_KEKKA"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_SEIKIN_JUGYO_TIKOKU"])) {
                    $query .= "  {$and} SUM(SUBCLASS_LATE)   <= {$model->field["KAIKIN_SEIKIN_JUGYO_TIKOKU"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_SEIKIN_JUGYO_SOUTAI"])) {
                    $query .= "  {$and} SUM(SUBCLASS_EARLY)   <= {$model->field["KAIKIN_SEIKIN_JUGYO_SOUTAI"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_SEIKIN_SHR_TIKOKU"])) {
                    $query .= "  {$and} SUM(SHR_LATE)   <= {$model->field["KAIKIN_SEIKIN_SHR_TIKOKU"]} ";
                    $and = 'AND';
                }
                if (strlen($model->field["KAIKIN_SEIKIN_SHR_SOUTAI"])) {
                    $query .= "  {$and} SUM(SHR_EARLY)   <= {$model->field["KAIKIN_SEIKIN_SHR_SOUTAI"]} ";
                }
                $query .= " ORDER BY ";
                $query .= "     LATE DESC, ";
                $query .= "     EARLY DESC, ";
                $query .= "     NOTICE DESC, ";
                $query .= "     NOTICE_LATE DESC, ";
                $query .= "     SUBCLASS_LATE DESC, ";
                $query .= "     SHR_LATE DESC, ";
                $query .= "     SHR_EARLY DESC, ";
                $query .= "     GRADE, ";
                $query .= "     HR_CLASS, ";
                $query .= "     ATTENDNO ";

                return $query;
            }
        }

        /************/
        /* 出欠状況 */
        /************/
        if ($model->field["SYUKKETU_JOUKYOU"] == '1') {
            $tikoku  = strlen($model->field["SYUKKETU_SYUKKETU_TIKOKU"])  ? $model->field["SYUKKETU_SYUKKETU_TIKOKU"]  : 99999;
            $soutai  = strlen($model->field["SYUKKETU_SYUKKETU_SOUTAI"])  ? $model->field["SYUKKETU_SYUKKETU_SOUTAI"]  : 99999;
            $kesseki = strlen($model->field["SYUKKETU_SYUKKETU_KESSEKI"]) ? $model->field["SYUKKETU_SYUKKETU_KESSEKI"] : 99999;
            $kekka   = 99999;
        } elseif ($model->field["SYUKKETU_JOUKYOU"] == '2') {
            $tikoku  = strlen($model->field["SYUKKETU_KYOUKA_TIKOKU"]) ? $model->field["SYUKKETU_KYOUKA_TIKOKU"] : 99999;
            $soutai  = strlen($model->field["SYUKKETU_KYOUKA_SOUTAI"]) ? $model->field["SYUKKETU_KYOUKA_SOUTAI"] : 99999;
            $kesseki = 99999;
            $kekka   = strlen($model->field["SYUKKETU_KYOUKA_KEKKA"])  ? $model->field["SYUKKETU_KYOUKA_KEKKA"] : 99999;
        } else {
            $tikoku  = strlen($model->field["SYUKKETU_IGAI_TIKOKU"]) ? $model->field["SYUKKETU_IGAI_TIKOKU"] : 99999;
            $soutai  = strlen($model->field["SYUKKETU_IGAI_SOUTAI"]) ? $model->field["SYUKKETU_IGAI_SOUTAI"] : 99999;
            $kesseki = 99999;
            $kekka   = strlen($model->field["SYUKKETU_IGAI_KEKKA"])  ? $model->field["SYUKKETU_IGAI_KEKKA"]  : 99999;
        }

        if ($model->field["SYUKKETU_JOUKYOU"] == '1') { //出欠状況
            $query .= " SELECT ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T3.LATE, ";
            $query .= "     T3.EARLY, ";
            $query .= "     T3.NOTICE ";
            $query .= " FROM ";
            $query .= "     SCHNO T1, ";
            $query .= "     ATTEND_SEMES T3 ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = T3.SCHREGNO AND ";
            $query .= "     (T3.LATE   >= {$tikoku}  OR ";
            $query .= "      T3.EARLY  >= {$soutai} OR ";
            $query .= "      T3.NOTICE >= {$kesseki} ) ";
            $query .= " ORDER BY ";
            $query .= "     T3.LATE DESC, ";
            $query .= "     T3.EARLY DESC, ";
            $query .= "     T3.NOTICE DESC, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        } else { //教科・科目、教科以外
            $query .= " SELECT ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.NAME, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T2.SUBCLASSCD, ";
            }
            $query .= "     T3.SUBCLASSNAME, ";
            $query .= "     T2.KETSUJI, ";
            $query .= "     T2.LATE, ";
            $query .= "     T2.EARLY, ";
            if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "  T2.NOTICE_LATE1 AS NOTICE_LATE ";
            } else {
                $query .= "  T2.NOTICE_LATE2 AS NOTICE_LATE ";
            }
            $query .= " FROM ";
            $query .= "     SCHNO T1 ";
            $query .= " INNER JOIN ";
            $query .= "     ATTEND_SUM3 T2 ON T2.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     SUBCLASS_MST T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                    AND T3.CLASSCD       = T2.CLASSCD ";
                $query .= "                    AND T3.SCHOOL_KIND   = T2.SCHOOL_KIND ";
                $query .= "                    AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            $query .= " WHERE ";
            if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     (T2.NOTICE_LATE1 >= {$kekka}  OR ";
            } else {
                $query .= "     (T2.NOTICE_LATE2 >= {$kekka}  OR ";
            }
            $query .= "      T2.LATE        >= {$tikoku} OR ";
            $query .= "      T2.EARLY       >= {$soutai} ) AND ";

            if ($model->field["SYUKKETU_JOUKYOU"] == '2') {
                $query .= "         SUBSTR(T2.SUBCLASSCD,1,2) <= '89' ";
            }
            if ($model->field["SYUKKETU_JOUKYOU"] == '3') {
                $query .= "         SUBSTR(T2.SUBCLASSCD,1,2) >= '90' ";
            }

            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T2.CLASSCD, ";
                $query .= "     T2.SCHOOL_KIND, ";
                $query .= "     T2.CURRICULUM_CD, ";
            }
            $query .= "     T2.SUBCLASSCD ";
        }

        return $query;
    }
}
?>
