<?php

require_once('for_php7.php');

class knjd120tquery extends Query {

    //RECORD_DATに存在しない生徒をinsert(履修学期が複数あれば複数レコード作成）
    function insertEx_Std_RecQuery($model){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " INSERT INTO RECORD_DAT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " (YEAR,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,TAKESEMES,SCHREGNO,CHAIRCD,REGISTERCD,UPDATED) ";
        } else {
            $query .= " (YEAR,SUBCLASSCD,TAKESEMES,SCHREGNO,CHAIRCD,REGISTERCD,UPDATED) ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "    t1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t2.CLASSCD, ";
            $query .= "     t2.SCHOOL_KIND, ";
            $query .= "     t2.CURRICULUM_CD, ";
        }
        $query .= "    t2.SUBCLASSCD, ";
        $query .= "    T2.TAKESEMES, ";
        $query .= "    t1.SCHREGNO, ";
        $query .= "    t1.CHAIRCD, ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    sysdate() ";
        $query .= " FROM ";
        $query .= "    CHAIR_STD_DAT t1, ";
        $query .= "    CHAIR_DAT t2 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR       = t2.YEAR AND ";
        $query .= "     t1.CHAIRCD    = t2.CHAIRCD AND ";
        $query .= "     T1.semester   = T2.semester AND ";
        $query .= "     t1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     t1.CHAIRCD    = '".$model->field["CHAIRCD"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t2.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     t2.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     t2.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     t2.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     t2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     t1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             RECORD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR='".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "                             SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "                             CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "                             SUBCLASSCD       = '".$subclass_array[3]."' )";
        } else {
            $query .= "                             SUBCLASSCD = '".$model->field["SUBCLASSCD"]."') ";
        }

      return $query;
    }

    //編集可能学期の判別
    function selectContolCodeQuery($model){

        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '1' ";

      return $query;
    }

    //科目データの取得
    function selectSubclassQuery($gen_ed, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     subclass_mst T3, ";
        $query .= "     chair_stf_dat T4 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T1.year       = T4.year AND ";
        $query .= "     T1.semester   = T4.semester AND ";
        $query .= "     T1.chaircd    = T4.chaircd AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "    (T3.subclasscd NOT LIKE '9%' OR T3.subclasscd = '".$gen_ed."' ) AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD  AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND  AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD  AND ";
        }
        $query .= "     T3.subclasscd = T1.subclasscd ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd ";
        return $query;
    }
    
    //講座データの取得
    function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.chaircd, ";
        $query .= "     T1.chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //遅刻何回で欠課とするかの指数取得
    function getScAbsentCov()
    {
        return "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '".CTRL_YEAR."'";
    }

    //累積最大日取得
    function GetMax($subclasscd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "SELECT DISTINCT year ";
        $query .= "              ,month ";
        $query .= "              ,CASE MONTH WHEN '01' THEN CHAR(SMALLINT(month) + 12)  ";
        $query .= "                          WHEN '02' THEN CHAR(SMALLINT(month) + 12)  ";
        $query .= "                          WHEN '03' THEN CHAR(SMALLINT(month) + 12) ELSE month END AS tmp_month ";
        $query .= "              ,semester ";
        $query .= "              ,appointed_day ";
        $query .= "         FROM attend_subclass_dat ";
        $query .= "        WHERE COPYCD     = '0' ";
        $query .= "          AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "          AND SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "          AND CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "          AND SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND subclasscd = '".$subclasscd."' ";
        }
        $query .= "          AND semester   = (SELECT MAX(semester) FROM attend_subclass_dat ";
        $query .= "                             WHERE COPYCD     = '0' ";
        $query .= "                               AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "                               AND SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "                               AND CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "                               AND SUBCLASSCD       = '".$subclass_array[3]."') ";
        } else {
            $query .= "                               AND subclasscd = '".$subclasscd."') ";
        }
        $query .= "ORDER BY tmp_month DESC ";
        return $query;
    }

    //評定マスタ
    function GetAssessMark()
    {
        $query  = "SELECT assessmark,assesslevel";
        $query .= "  FROM assess_mst ";
        $query .= " WHERE assesscd = '4'";
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query .= " SELECT W1.*";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W5.chair_sem1 ";
        $query .= "       ,W6.chair_sem2 ";
        $query .= "   FROM ";
        $query .= "    (SELECT * ";
        $query .= "       FROM record_dat ";
        $query .= "      WHERE year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        AND CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "        AND SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "        AND CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "        AND SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "        AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
# 2005/05/27 EDIT START
#        $query .= "        AND chaircd    = '".$model->field["CHAIRCD"]."' ) W1 ";
        $query .= "        AND SCHREGNO IN(SELECT SCHREGNO  ";
        $query .= "                          FROM CHAIR_STD_DAT  ";
        $query .= "                         WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "                           AND CHAIRCD = '".$model->field["CHAIRCD"]."') ";
        $query .= "    ) W1 ";
# 2005/05/27 EDIT END
        /* 基礎情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.year,T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.inoutcd,T2.name_show,T2.schregno, T2.grd_date, T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE ";
        $query .= "       FROM schreg_regd_hdat T0, schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "      WHERE T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "        AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".CTRL_SEMESTER."' ";
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W1.year = W2.year AND W1.schregno = W2.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 在籍情報(前期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem1";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '1') W5";
        $query .= "   ON W5.schregno = W1.schregno ";
        /* 在籍情報(後期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem2";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '2') W6";
        $query .= "   ON W6.schregno = W1.schregno ";
        $query .= " ORDER BY W2.hr_class,W2.attendno ";
        return $query;
    }

    //考査満点マスタ
    function getPerfect($year, $subclasscd, $testcd, $grade, $course, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= "SELECT ";
        $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$course}' END ";
        return $query;
    }

    function GetAttendData($chaircd, $subclasscd, $absent_cov, $absent_cov_late, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        /* 出欠累積情報 */
        $query .= "SELECT schregno ";
        $query .= "       ,SUM(notice) + SUM(nonotice)  AS T_NOTICE ";

        if ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(SUM(late) + SUM(early),".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,((SUM(late)   + SUM(early)) / ".$absent_cov_late.") + (SUM(notice) + SUM(nonotice)) AS NOTICE_LATE ";
        } else {
            $query .= "   ,SUM(late)   + SUM(early)     AS T_LATEEARLY ";
            $query .= "   ,SUM(notice) + SUM(nonotice)  AS NOTICE_LATE ";
        }
        $query .= "  FROM attend_subclass_dat  ";
        $query .= " WHERE COPYCD     = '0' ";
        $query .= "   AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "   AND SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "   AND CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "   AND SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "   AND subclasscd = '".$subclasscd."' ";
        }
        $query .= "   AND EXISTS (SELECT 'X' FROM chair_std_dat ";
        $query .= "                WHERE year    = '".CTRL_YEAR."'";
        $query .= "                  AND chaircd = '".$chaircd."')";
        $query .= " GROUP BY schregno";
        //echo $query;
        return $query;
    }

    //ABSENT_COVが１(学期ごとに集計）する場合
    function GetAttendData2($chaircd,$subclasscd, $absent_cov_late, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno";
        $query .= "               ,T1.semester ";
        $query .= "               ,SUM(notice) + SUM(nonotice) AS T_NOTICE ";
        if ((is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(SUM(late)  + SUM(early),".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,((SUM(late) + SUM(early)) / ".$absent_cov_late.") + (SUM(notice) + SUM(nonotice)) AS NOTICE_LATE ";
        } else {
            $query .= "           ,SUM(late)   + SUM(early)    AS T_LATEEARLY ";
            $query .= "           ,SUM(notice) + SUM(nonotice) AS NOTICE_LATE ";
        }
        $query .= "          FROM attend_subclass_dat T1   ";
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "           AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "           AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "           AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "           AND T1.subclasscd = '".$subclasscd."' ";
        }
        $query .= "           AND EXISTS (SELECT 'X' FROM chair_std_dat T2 ";
        $query .= "                        WHERE T1.year     = T2.year ";
        $query .= "                          AND T1.semester = T2.semester ";
        $query .= "                          AND T1.schregno = T2.schregno "; 
        $query .= "                          AND T2.chaircd  = '".$chaircd."')";
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " GROUP BY schregno ";
        //echo $query;
        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();
        
        for($i=0;$i<get_count($model->data["SCHREGNO"]);$i++){

            $data = array();
            $val = explode("-", $model->data["SCHREGNO"][$i]);

            //出欠情報
            $data["SEM1_INTR_SCORE_DI"][TEXT] = $model->fields["SEM1_INTR_SCORE_DI"][$i];
            $data["SEM1_TERM_SCORE_DI"][TEXT] = $model->fields["SEM1_TERM_SCORE_DI"][$i];
            $data["SEM2_INTR_SCORE_DI"][TEXT] = $model->fields["SEM2_INTR_SCORE_DI"][$i];
            $data["SEM2_TERM_SCORE_DI"][TEXT] = $model->fields["SEM2_TERM_SCORE_DI"][$i];

            //前期
            $data["SEM1_INTR_SCORE"][NUMBER]  = $model->fields["SEM1_INTR_SCORE"][$i];
            $data["SEM1_INTR_VALUE"][NUMBER]  = $model->fields["SEM1_INTR_VALUE"][$i];
            $data["SEM1_TERM_SCORE"][NUMBER]  = $model->fields["SEM1_TERM_SCORE"][$i];
            $data["SEM1_TERM_VALUE"][NUMBER]  = $model->fields["SEM1_TERM_VALUE"][$i];
            $data["SEM1_VALUE"][NUMBER]       = $model->fields["SEM1_VALUE"][$i];     

            //後期
            $data["SEM2_INTR_SCORE"][NUMBER]  = $model->fields["SEM2_INTR_SCORE"][$i];
            $data["SEM2_INTR_VALUE"][NUMBER]  = $model->fields["SEM2_INTR_VALUE"][$i];
            $data["SEM2_TERM_SCORE"][NUMBER]  = $model->fields["SEM2_TERM_SCORE"][$i];
            $data["SEM2_TERM_VALUE"][NUMBER]  = $model->fields["SEM2_TERM_VALUE"][$i];
            $data["SEM2_VALUE"][NUMBER]       = $model->fields["SEM2_VALUE"][$i];     

            $data["GRAD_VALUE"][NUMBER]       = $model->fields["GRAD_VALUE"][$i];

            $data["REGISTERCD"][TEXT]        = STAFFCD ;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE YEAR           = '".CTRL_YEAR."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "       CLASSCD          = '".$subclass_array[0]."' AND ";
                $where .= "       SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
                $where .= "       CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
                $where .= "       SUBCLASSCD       = '".$subclass_array[3]."' AND ";
            } else {
                $where .= "       SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' AND ";
            }
            $where .= "       SCHREGNO       = '".$val[0]."' AND ";
            $where .= "       TAKESEMES      = '".$val[1]."' ";
#            $where .= "       CHAIRCD        = '".$model->field["CHAIRCD"]."'";    2005/05/27 EDIT

            $query = Query::updateSQL($data, "RECORD_DAT",$where);

            $db->query($query);
        }

        Query::dbCheckIn($db);

    }
}
?>
