<?php

require_once('for_php7.php');

class knja610vQuery extends Query {

    function getZ010Name() {
        $query  = " SELECT NAME1 FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'Z010' AND ";
        $query .= "    NAMECD2 = '00' ";
        $db = Query::dbCheckOut();
        $rtnRow = $db->getOne($query);
        Query::dbCheckIn($db);
        return $rtnRow;
    }

    //学期取得
    function getSelectSeme($year)
    {
        $query = "SELECT semester,semestername FROM semester_mst where year='" .$year ."' order by semester";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //課程学科コンボ取得
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
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($model, $div="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.TESTITEMNAME AS LABEL, ";
        if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE ";
        } else {
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     {$model->tableName} T1 ";
        if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV') {
            $query .= "     INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T2 ";
            $query .= "          ON T1.YEAR         = T2.YEAR ";
            $query .= "         AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "         AND T1.SEMESTER     = T2.SEMESTER ";
            $query .= "         AND T1.TESTKINDCD   = T2.TESTKINDCD ";
            $query .= "         AND T1.TESTITEMCD   = T2.TESTITEMCD ";
            $query .= "         AND T1.SCORE_DIV    = T2.SCORE_DIV ";
            $query .= "         AND T1.GRADE        = T2.GRADE ";
            $query .= "         AND T1.COURSECD     = T2.COURSECD ";
            $query .= "         AND T1.MAJORCD      = T2.MAJORCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW', 'TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            $query .= "     AND T1.SEMESTER = '".$model->field["GAKKI"]."' ";
        }
        if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV') {
            $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        if ($div == "TESTNAME") {
            $query .= " AND ";
            if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
                $query .= "    T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
            } else {
                $query .= "    T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTKINDCD"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //追指導情報取得
    function getSidouInputInf($model) {
        $query  = "SELECT ";
        $query .= "    SIDOU_INPUT, ";
        $query .= "    SIDOU_INPUT_INF ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= "WHERE ";
        $query .= "    YEAR = '" . $model->field["YEAR"] . "' ";
        $query .= "    AND SEMESTER = '" . $model->field["GAKKI"] . "' ";
        $query .= "    AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" . $model->field["TESTKINDCD"] . "' ";

        return $query;
    }

    //最終学期取得
    function getSelectSemester($year)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT MAX(semester) AS GAKKI_MAX FROM schreg_regd_hdat where year='" .$year ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //年度取得
    function getSelectYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "   AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "   AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_DAT T3 ";
            $query .= "      ON T3.YEAR     = T1.YEAR ";
            $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "     AND T3.GRADE    = T1.GRADE ";
            $query .= "     AND T3.HR_CLASS = T1.HR_CLASS ";
            $query .= "     AND T3.COURSECD || T3.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->field["GAKKI"] != "9") {
            $query .= " AND T1.SEMESTER = '".$model->field["GAKKI"]."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3, T1.SUBTR_CD1, T1.SUBTR_CD2, T1.SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSelectSubClass1($model, $semester, $div = "")
    {
        $query  = "SELECT DISTINCT ";
        if ($div == "DATE") {
            $query .= "    t1.SCHREGNO ";
        } else {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "    t1.subclasscd, ";
            }
            $query .= "    t2.subclassname ";
        }
        $query .= "FROM ";
        if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            $query .= "    RECORD_SCORE_DAT t1, ";
        } else {
            $query .= "    V_RECORD_SCORE_DAT t1, ";
        }
        $query .= "    subclass_mst t2 ";
        $query .= "WHERE ";
        if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            $query .= "    t1.YEAR = '{$model->field["YEAR"]}' AND ";
            $query .= "    t1.SEMESTER = '{$model->field["GAKKI"]}' AND ";
            $query .= "    t1.TESTKINDCD || t1.TESTITEMCD || t1.SCORE_DIV = '{$model->field["TESTKINDCD"]}' AND ";
        }
        $query .= "    t1.year='" .$model->field["YEAR"] ."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    t1.CLASSCD       = t2.CLASSCD AND ";
            $query .= "    t1.SCHOOL_KIND   = t2.SCHOOL_KIND AND ";
            $query .= "    t1.CURRICULUM_CD = t2.CURRICULUM_CD AND ";
        }
        $query .= "    t1.subclasscd=t2.subclasscd AND ";
        $query .= "    t1.schregno IN (SELECT ";
        $query .= "                        schregno ";
        $query .= "                    FROM ";
        $query .= "                        schreg_regd_dat ";
        $query .= "                    WHERE ";
        $query .= "                        year='" .$model->field["YEAR"] ."' AND ";
        if ($semester != "9"){
            $query .= "                        semester='" .$semester ."' AND ";
        }else {
            $query .= "                        semester='" .CTRL_SEMESTER."' AND ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "                        COURSECD || MAJORCD = '" .$model->field["COURSE_MAJOR"]."' AND ";
        }
        $query .= "                        grade || hr_class IN ('".implode($model->select_data["selectdata"],"','")."') ";
        $query .= "                    ) ";
        if ($div == "") {
            $query .= "ORDER BY ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    SUBCLASSCD ";
            } else {
                $query .= "    t1.subclasscd ";
            }
        }

        return $query;
    }

    //出欠集計開始日付などを取得
    function getAttendDate($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR, ";
        $query .= "    MONTH, ";
        $query .= "    MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM ";
        $query .= "    ATTEND_SEMES_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR='".$model->field["YEAR"]."' ";
        if ($model->field["GAKKI"] != "9"){
            $query .= "    AND SEMESTER = '".$model->field["GAKKI"]."' ";
        }else {
            $query .= "    AND SEMESTER <= '".CTRL_SEMESTER."' ";
        }
        $query .= "GROUP BY ";
        $query .= "    SEMESTER, ";
        $query .= "    MONTH ";
        $query .= "ORDER BY ";
        $query .= "    2, ";
        $query .= "    3 ";

        return $query;
    }

    //学校マスタの取得
    function getSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //CVS作成用のQUERY（テスト毎）
    function selectCsvQuery1($model, $testdi, $absent, $semester, $month, $attend_sdate, $knjSchoolMst, $sidouRow, $z010name)
    {
        //科目コードのIN文作成
        $sep = "";
        $subclasscdInState = "";
        for ($i = 0; $i < get_count($model->opt_subclass); $i++) {
            $subclasscdInState .= $sep."'".$model->opt_subclass[$i]."'";
            $sep = ",";
        }
        if ($subclasscdInState == "") {
            $subclasscdInState = "''";
        }

        //成績データ取得のフィールド指定
        if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
                $intr_term = "SCORE";
        } else if ($model->field["GAKKI"] == "9"){
            if($model->field["TESTKINDCD"] == "9900"){
                $intr_term = "GRAD_VALUE";
            } elseif($model->field["TESTKINDCD"] == "9901") {
                $intr_term = "GRAD_SCORE";
            } else {//DBエラー回避
                $intr_term = "GRAD_VALUE";
            }
        } else {
            if($model->field["TESTKINDCD"] == "0101"){
                $intr_term = "SEM".$model->field["GAKKI"]."_INTR_SCORE";
            } elseif($model->field["TESTKINDCD"] == "0201"){
                $intr_term = "SEM".$model->field["GAKKI"]."_TERM_SCORE";
            } elseif($model->field["TESTKINDCD"] == "0202"){
                $intr_term = "SEM".$model->field["GAKKI"]."_TERM2_SCORE";
            } elseif($model->field["TESTKINDCD"] == "9900") {
                $intr_term = "SEM".$model->field["GAKKI"]."_VALUE";
            } else {//DBエラー回避
                $intr_term = "SEM".$model->field["GAKKI"]."_VALUE";
            }
        }

        //対象生徒
        $query  = "WITH SCH_T AS ( ";
        $query .= "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->field["YEAR"]."' AND ";
        if ($model->field["GAKKI"] != "9"){
            $query .= "    SEMESTER = '".$model->field["GAKKI"]."' AND ";
        }else {
            $query .= "    SEMESTER = '".CTRL_SEMESTER."' AND ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "    COURSECD || MAJORCD = '".$model->field["COURSE_MAJOR"]."' AND ";
        }
        $query .= "    GRADE || HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') ";
        $query .= ") ";

        //出欠データ
        $query .= knja610vQuery::getAttendData($model, $semester, $month, $attend_sdate, $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $knjSchoolMst);
        $query .= ", ATTEND_T AS ( ";
        $query .= knja610vQuery::GetAttendData2($model, $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"]);
        $query .= ") ";

        if ($z010name == "meikei") {
            $query .= " , CSVDATA_SUMMARY AS ( ";
        }
        //追指導
        $sidouQuery = "";
        if ($sidouRow["SIDOU_INPUT"] == '1' && $sidouRow["SIDOU_INPUT_INF"] == '1') {
            //1:記号
            $sidouQuery = "NAME_M.NAME1";
        } else if ($sidouRow["SIDOU_INPUT"] == '1' && $sidouRow["SIDOU_INPUT_INF"] == '2') {
            //2:得点
            $sidouQuery = "REC_S.SCORE";
        } else {
            //NULL
            $sidouQuery = "CASE WHEN m1.schregno IS NULL THEN m1.schregno ELSE NULL END";
        }
        //欠席者「*」
        $kessekiQuery = "";
        if ($model->field["KESSEKI_FLG"] == '1' && in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            //「*」を印字する
            $kessekiQuery = "CASE WHEN m1.VALUE_DI = '*' THEN 7777 ELSE m1.".$intr_term." END";
        } else {
            //「*」を印字しない
            $kessekiQuery = "m1.".$intr_term."";
        }

        //メインデータ
        $query .= "SELECT ";
        $query .= "    m1.schregno,m2.grade,m2.hr_class,m2.attendno,m3.name_show,'".$testdi."' AS TESTDATANAME ";
        for($i=0;$i<get_count($model->opt_subclass);$i++){
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    ,SUM( CASE m1.CLASSCD || '-' || m1.SCHOOL_KIND || '-' || m1.CURRICULUM_CD || '-' || m1.SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN {$kessekiQuery} ELSE NULL END ) as tokuten" .$i ." ";
                if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    $query .= "    ,MAX( CASE m1.CLASSCD || '-' || m1.SCHOOL_KIND || '-' || m1.CURRICULUM_CD || '-' || m1.SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN {$sidouQuery} ELSE NULL END ) as SIDOU" .$i ." ";
                    $query .= "    ,MAX( CASE m1.CLASSCD || '-' || m1.SCHOOL_KIND || '-' || m1.CURRICULUM_CD || '-' || m1.SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN REC_S.REMARK ELSE NULL END ) as REMARK" .$i ." ";
                }
                $query .= "    ,SUM( CASE m1.CLASSCD || '-' || m1.SCHOOL_KIND || '-' || m1.CURRICULUM_CD || '-' || m1.SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
            } else {
                $query .= "    ,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN {$kessekiQuery} ELSE NULL END ) as tokuten" .$i ." ";
                if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    $query .= "    ,MAX( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN {$sidouQuery} ELSE NULL END ) as SIDOU" .$i ." ";
                    $query .= "    ,MAX( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN REC_S.REMARK ELSE NULL END ) as REMARK" .$i ." ";
                }
                $query .= "    ,SUM( CASE m1.subclasscd WHEN '".$model->opt_subclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
            }
        }
        $query .= "FROM ";
        if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            $query .= "    RECORD_SCORE_DAT m1 ";
            if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $query .= "    LEFT JOIN RECORD_SLUMP_SDIV_DAT REC_S ON REC_S.YEAR = m1.YEAR ";
                $query .= "         AND REC_S.SEMESTER = '{$model->field["GAKKI"]}' ";
                $query .= "         AND REC_S.TESTKINDCD || REC_S.TESTITEMCD || REC_S.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND REC_S.CLASSCD = m1.CLASSCD ";
                    $query .= "         AND REC_S.SCHOOL_KIND = m1.SCHOOL_KIND ";
                    $query .= "         AND REC_S.CURRICULUM_CD = m1.CURRICULUM_CD ";
                }
                $query .= "         AND REC_S.SUBCLASSCD = m1.SUBCLASSCD ";
                $query .= "         AND REC_S.SCHREGNO = m1.SCHREGNO ";
                $query .= "    LEFT JOIN NAME_MST NAME_M ON NAME_M.NAMECD1 = 'D054' AND NAME_M.NAMECD2 = REC_S.MARK ";
            }
        } else {
            $query .= "    V_RECORD_SCORE_DAT m1 ";
        }
        $query .= "    LEFT JOIN ATTEND_T L1 ON L1.SCHREGNO = m1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD       = m1.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND   = m1.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD = m1.CURRICULUM_CD ";
        }
        $query .= "         AND L1.SUBCLASSCD = m1.SUBCLASSCD, ";
        $query .= "    SCH_T m2, ";
        $query .= "    schreg_base_mst m3 ";
        $query .= "WHERE ";
        if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            $query .= "    m1.YEAR = '{$model->field["YEAR"]}' AND ";
            $query .= "    m1.SEMESTER = '{$model->field["GAKKI"]}' AND ";
            $query .= "    m1.TESTKINDCD || m1.TESTITEMCD || m1.SCORE_DIV = '{$model->field["TESTKINDCD"]}' AND ";
        }
        $query .= "    m2.year = m1.year AND ";
        $query .= "    m2.schregno = m1.schregno AND ";
        $query .= "    m3.schregno = m2.schregno ";
        $query .= "GROUP BY ";
        $query .= "    m1.schregno,m2.grade,m2.hr_class,m2.attendno,m3.name_show ";
        $query .= "ORDER BY ";
        $query .= "    m2.grade,m2.hr_class,m2.attendno ";
        if ($z010name == "meikei") {
            $query .= " ), REGDDAT AS ( ";
            $query .= " SELECT ";
            $query .= "    W1.SCHREGNO, ";
            $query .= "    W1.YEAR, ";
            $query .= "    W1.GRADE, ";
            $query .= "    W1.HR_CLASS, ";
            $query .= "    W1.ATTENDNO, ";
            $query .= "    CASE WHEN DOMI.SCHREGNO IS NOT NULL THEN '寮' ELSE '' END AS RYOU, ";
            $query .= "    CASE WHEN BASE_DE.BASE_REMARK1 = '3' THEN '海' ELSE '' END AS KAIGAI, ";
            $query .= "    CASE WHEN BASE_DE.BASE_REMARK1 = '4' THEN '推' ELSE '' END AS SUISEN, ";
            $query .= "    CASE WHEN W1.COURSECODE = '0002' THEN 'IB' ELSE '' END AS IB, ";
            $query .= "    CASE WHEN BASE_DE.BASE_REMARK2 IN ('01','05','08') THEN 'Ａ' ELSE '' END AS AHOUSHIKI, ";
            $query .= "    COURSECD || MAJORCD AS MAJOR, ";
            $query .= "    COURSECD || MAJORCD || COURSECODE AS COURSE ";
            $query .= " FROM ";
            $query .= "    SCHREG_REGD_DAT W1 ";
            $query .= "    INNER JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ";
            $query .= "    LEFT JOIN SCHREG_BASE_DETAIL_MST BASE_DE ON W1.SCHREGNO = BASE_DE.SCHREGNO ";
            $query .= "         AND BASE_DE.BASE_SEQ = '014' ";
            $query .= "    LEFT JOIN ( ";
            $query .= "            SELECT ";
            $query .= "                * ";
            $query .= "            FROM ";
            $query .= "                SCHREG_DOMITORY_HIST_DAT ";
            $query .= "            WHERE ";
            $query .= "                '".CTRL_DATE."' BETWEEN DOMI_ENTDAY AND VALUE(DOMI_OUTDAY, '9999-12-31') ";
            $query .= "    ) DOMI ON W1.SCHREGNO = DOMI.SCHREGNO ";
            $query .= "  LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND W3.SEX = Z002.NAMECD2 ";
            $query .= " WHERE ";
            $query .= "    W1.YEAR = '".$model->field["YEAR"]."' ";
            if ($model->field["GAKKI"] != "9"){
                $query .= "    AND W1.SEMESTER = '".$model->field["GAKKI"]."' ";
            } else {
                $query .= "    AND W1.SEMESTER = '".CTRL_SEMESTER."' ";
            }
            $query .= "    AND W1.GRADE||W1.HR_CLASS IN ('".implode($model->select_data["selectdata"],"','")."') ";
            $query .= ") ";


            $query .= " SELECT ";
            $query .= "    CSVD.schregno, ";
            $query .= "    CSVD.grade, ";
            $query .= "    CSVD.hr_class, ";
            $query .= "    CSVD.attendno, ";
            $query .= "    CSVD.name_show, ";
            $query .= "    CSVD.TESTDATANAME, ";
            $query .= "    m4.RYOU, ";
            $query .= "    m4.KAIGAI, ";
            $query .= "    m4.SUISEN, ";
            $query .= "    m4.IB, ";
            $query .= "    m4.AHOUSHIKI ";
            for($i=0;$i<get_count($model->opt_subclass);$i++){
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "    ,CSVD.tokuten" .$i ." ";
                    if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                        $query .= "    ,CSVD.SIDOU" .$i ." ";
                        $query .= "    ,CSVD.REMARK" .$i ." ";
                    }
                    $query .= "    ,CSVD.DI" .$i ." ";
                } else {
                    $query .= "    ,CSVD.tokuten" .$i ." ";
                    if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                        $query .= "    ,CSVD.SIDOU" .$i ." ";
                        $query .= "    ,CSVD.REMARK" .$i ." ";
                    }
                    $query .= "    ,CSVD.DI" .$i ." ";
                }
                $query .= " ,( ";
                $query .= "       SELECT ";
                $query .= "           GRADE_DEVIATION ";
                $query .= "       FROM ";
                $query .= "           RECORD_RANK_SDIV_DAT m5 ";
                $query .= "       WHERE ";
                $query .= "           m5.YEAR = '".$model->field["YEAR"]."' ";
                if ($model->field["GAKKI"] != "9"){
                    $query .= "       AND m5.SEMESTER = '".$model->field["GAKKI"]."' ";
                } else {
                    $query .= "       AND m5.SEMESTER = '".CTRL_SEMESTER."' ";
                }
                $query .= "           AND m5.CLASSCD || '-' || m5.SCHOOL_KIND || '-' || m5.CURRICULUM_CD || '-' || m5.SUBCLASSCD = '".$model->opt_subclass[$i]."' ";
                $query .= "           AND m5.TESTKINDCD || m5.TESTITEMCD || m5.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
                $query .= "           AND M5.SCHREGNO = CSVD.SCHREGNO ";
                $query .= " ) AS GRADE_DEVIATION".$i." ";
            }

            $query .= " FROM ";
            $query .= "    CSVDATA_SUMMARY CSVD ";
            $query .= "    LEFT JOIN REGDDAT m4 ";
            $query .= "      ON m4.SCHREGNO = CSVD.SCHREGNO ";
            $query .= "     AND m4.GRADE = CSVD.GRADE ";
            $query .= "     AND m4.HR_CLASS = CSVD.HR_CLASS ";
            $query .= "     AND m4.ATTENDNO = CSVD.ATTENDNO ";
        }

        return $query;
    }

    //遅刻何回で欠課とするかの指数取得
    function getScAbsentCov($model) {
        $query  = " SELECT ";
        $query .= "     ABSENT_COV, ";
        $query .= "     ABSENT_COV_LATE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //端数処理
    function getAttendData($model, $semester, $month, $attend_sdate, $absent_cov, $absent_cov_late, $knjSchoolMst)
    {
        $query  = ",ATTEND_SUBCLASS AS ( ";
        $query .= "    SELECT * ";
        $query .= "    FROM   ATTEND_SUBCLASS_DAT ";
        $query .= "    WHERE  YEAR='".$model->field["YEAR"]."' ";
        $query .= "      AND  SEMESTER <= '".$semester."' ";
        $query .= "      AND  MONTH IN ('" .implode($month, "','") ."') ";
        $query .= "    ) ";
        $query .= ",SCHEDULE AS( ";
        $query .= "    SELECT EXECUTEDATE, PERIODCD, CHAIRCD, DATADIV ";
        $query .= "    FROM   SCH_CHR_DAT  ";
        $query .= "    WHERE  EXECUTEDATE BETWEEN DATE('".$attend_sdate."') AND DATE('".str_replace("/","-",$model->field["ATTENDDATE"])."')  ";
        $query .= "      AND  PERIODCD != '0'  ";
        $query .= "    ) ";
        $query .= ",T_attend_dat AS( ";
        $query .= "     SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD, ";
        $query .= "            CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ";
        $query .= "            L1.MULTIPLY ";
        $query .= "     FROM   ATTEND_DAT W1  ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT L1 ON W1.YEAR = L1.YEAR AND L1.DI_CD = W1.DI_CD ";
        $query .= "     WHERE  W1.ATTENDDATE BETWEEN DATE('".$attend_sdate."') AND DATE('".str_replace("/","-",$model->field["ATTENDDATE"])."')  ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7  ";
        $query .= "                           WHERE  T7.SCHREGNO = W1.SCHREGNO  ";
        $query .= "                             AND  T7.TRANSFERCD IN('1','2')  ";
        $query .= "                             AND  W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE )  ";
        $query .= "    ) ";
        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        } else {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        }
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        if ($model->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "         AND T2.SCORE_DIV = '01' ";
        }
        $query .= "     ) ";
        $query .= ",T_attend AS( ";
        $query .= "SELECT S1.SCHREGNO  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      ,S1.CLASSCD  ";
            $query .= "      ,S1.SCHOOL_KIND  ";
            $query .= "      ,S1.CURRICULUM_CD  ";
        }
        $query .= "      ,S1.SUBCLASSCD  ";
        $query .= "      ,S1.SEMESTER  ";
        $query .= "      ,COUNT(*)  AS LESSON  ";
        $query .= "      ,SUM(CASE WHEN S3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END)  AS  OFFDAYS  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF  ";
        $query .= "      ,SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY  ";
        $query .= "FROM  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, ";
            $query .= "         T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD, T2.SEMESTER ";
        } else {
            $query .= " (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SUBCLASSCD, T2.SEMESTER ";
        }
        $query .= "  FROM   SCHEDULE T1  ";
        $query .= "        ,CHAIR_STD_DAT T2  ";
        $query .= "        ,CHAIR_DAT T3  ";
        $query .= "        ,SCH_T T4  ";
        $query .= "  WHERE  T1.CHAIRCD  = T3.CHAIRCD  ";
        $query .= "    AND  T3.YEAR     = '".$model->field["YEAR"]."'  ";
        $query .= "    AND  T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ";
        $query .= "    AND  T2.YEAR     = '".$model->field["YEAR"]."'  ";
        $query .= "    AND  T2.SEMESTER = T3.SEMESTER  ";
        $query .= "    AND  T2.CHAIRCD  = T1.CHAIRCD  ";
        $query .= "    AND  T4.SCHREGNO = T2.SCHREGNO  ";
        $query .= "    AND     NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T5  ";
        $query .= "                    WHERE   T5.EXECUTEDATE = T1.EXECUTEDATE AND  ";
        $query .= "                            T5.PERIODCD = T1.PERIODCD AND  ";
        $query .= "                            T5.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "                            T5.GRADE = T4.GRADE AND  ";
        $query .= "                            T5.HR_CLASS = T4.HR_CLASS AND  ";
        $query .= "                            T1.DATADIV IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                            T5.COUNTFLG = '0') ";
        $query .= "    AND     NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                        WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                          AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                          AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                          AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        $query .= "    AND     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6  ";
        $query .= "                    WHERE   T6.SCHREGNO = T4.SCHREGNO AND  ";
        $query .= "                          ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ";
        $query .= "                           (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) )  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD, T2.SEMESTER)S1  ";
        } else {
            $query .= "  GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SUBCLASSCD, T2.SEMESTER)S1  ";
        }
        $query .= " LEFT JOIN T_attend_dat S2 ON S2.SCHREGNO = S1.SCHREGNO AND S2.ATTENDDATE = S1.EXECUTEDATE AND S2.PERIODCD = S1.PERIODCD  ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.TRANSFERCD = '2' AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = S1.SCHREGNO AND S4.TRANSFERCD = '1' AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "GROUP BY S1.SCHREGNO, S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, S1.SUBCLASSCD, S1.SEMESTER ";
        } else {
            $query .= "GROUP BY S1.SCHREGNO, S1.SUBCLASSCD, S1.SEMESTER ";
        }
        $query .= "    ) ";
        $query .= ",ATTEND_SUM AS( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT W1.SCHREGNO,W1.CLASSCD,W1.SCHOOL_KIND,W1.CURRICULUM_CD,W1.SUBCLASSCD,W1.SEMESTER, ";
        } else {
            $query .= "    SELECT W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER, ";
        }
        $query .= "           SUM(W1.LESSON) LESSON, ";
        $query .= "           SUM(W1.OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(W1.ABSENT) ABSENT, ";
        $query .= "           SUM(W1.SUSPEND) SUSPEND, ";
        $query .= "           SUM(W1.MOURNING) MOURNING, ";
        $query .= "           SUM(W1.SICK) SICK, ";
        if ($model->virus) {
            $query .= "           SUM(W1.VIRUS) VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(W1.KOUDOME) KOUDOME, ";
        }
        $query .= "           SUM(W1.NOTICE) NOTICE, ";
        $query .= "           SUM(W1.NONOTICE) NONOTICE, ";
        $query .= "           SUM(W1.NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(W1.LATE) LATE, ";
        $query .= "           SUM(W1.EARLY) EARLY ";
        $query .= "    FROM   ATTEND_SUBCLASS W1, SCH_T W0 ";
        $query .= "    WHERE  W1.SCHREGNO = W0.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY W1.SCHREGNO,W1.CLASSCD,W1.SCHOOL_KIND,W1.CURRICULUM_CD,W1.SUBCLASSCD,W1.SEMESTER ";
            $query .= "     UNION ALL ";
            $query .= "    SELECT W2.SCHREGNO,W2.CLASSCD,W2.SCHOOL_KIND,W2.CURRICULUM_CD,W2.SUBCLASSCD,W2.SEMESTER, ";
        } else {
            $query .= "    GROUP BY W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER ";
            $query .= "     UNION ALL ";
            $query .= "    SELECT W2.SCHREGNO,W2.SUBCLASSCD,W2.SEMESTER, ";
        }
        $query .= "           W2.LESSON, ";
        $query .= "           W2.OFFDAYS, ";
        $query .= "           W2.ABSENT, ";
        $query .= "           W2.SUSPEND, ";
        $query .= "           W2.MOURNING, ";
        $query .= "           W2.SICK, ";
        if ($model->virus) {
            $query .= "           W2.VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           W2.KOUDOME, ";
        }
        $query .= "           W2.NOTICE, ";
        $query .= "           W2.NONOTICE, ";
        $query .= "           W2.NURSEOFF, ";
        $query .= "           W2.LATE, ";
        $query .= "           W2.EARLY ";
        $query .= "     FROM   T_attend W2 ";
        $query .= "    ) ";
        //学期毎に清算
        $query .= ",ATTEND_SUM2 AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEMESTER, ";
        } else {
            $query .= "    SELECT SCHREGNO,SUBCLASSCD,SEMESTER, ";
        }
        $query .= "           SUM(VALUE(LESSON, 0)) LESSON, ";
        $query .= "           SUM(VALUE(OFFDAYS, 0)) OFFDAYS, ";
        $query .= "           SUM(VALUE(ABSENT, 0)) ABSENT, ";
        $query .= "           SUM(VALUE(SUSPEND, 0)) SUSPEND, ";
        $query .= "           SUM(VALUE(MOURNING, 0)) MOURNING, ";
        $query .= "           SUM(VALUE(SICK, 0)) SICK, ";
        if ($model->virus) {
            $query .= "           SUM(VALUE(VIRUS, 0)) VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(VALUE(KOUDOME, 0)) KOUDOME, ";
        }
        $query .= "           SUM(VALUE(NOTICE, 0)) NOTICE, ";
        $query .= "           SUM(VALUE(NONOTICE, 0)) NONOTICE, ";
        $query .= "           SUM(VALUE(NURSEOFF, 0)) NURSEOFF, ";
        $query .= "           SUM(VALUE(LATE, 0)) LATE, ";
        $query .= "           SUM(VALUE(EARLY, 0)) EARLY ";
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(sum(VALUE(LATE, 0)) + sum(VALUE(EARLY, 0))) / ".$absent_cov_late.") + (sum(VALUE(NOTICE, 0)) + sum(VALUE(NONOTICE, 0)) + sum(VALUE(NURSEOFF, 0)) + sum(VALUE(SICK, 0)) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(VALUE(OFFDAYS, 0)) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(VALUE(SUSPEND, 0)) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VALUE(VIRUS, 0)) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(VALUE(KOUDOME, 0)) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(VALUE(MOURNING, 0)) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(VALUE(ABSENT, 0)) ";
            }
            $query .= "      ), 4, 1) as NOTICE_LATE ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((sum(VALUE(LATE, 0)) + sum(VALUE(EARLY, 0))) / ".$absent_cov_late.") + (sum(VALUE(NOTICE, 0)) + sum(VALUE(NONOTICE, 0)) + sum(VALUE(NURSEOFF, 0)) + sum(VALUE(SICK, 0)) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(VALUE(OFFDAYS, 0)) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(VALUE(SUSPEND, 0)) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VALUE(VIRUS, 0)) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(VALUE(KOUDOME, 0)) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(VALUE(MOURNING, 0)) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(VALUE(ABSENT, 0)) ";
            }
            $query .= "      ) as NOTICE_LATE ";
        } else {
            $query .= "      ,sum(VALUE(NOTICE, 0)) + sum(VALUE(NONOTICE, 0)) + sum(VALUE(NURSEOFF, 0)) + sum(VALUE(SICK, 0)) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(VALUE(OFFDAYS, 0)) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(VALUE(SUSPEND, 0)) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VALUE(VIRUS, 0)) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(VALUE(KOUDOME, 0)) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(VALUE(MOURNING, 0)) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(VALUE(ABSENT, 0)) ";
            }
            $query .= "      as NOTICE_LATE ";
        }
        $query .= "    FROM   ATTEND_SUM ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEMESTER ";
        } else {
            $query .= "    GROUP BY SCHREGNO,SUBCLASSCD,SEMESTER ";
        }
        $query .= "    ) ";
        //年間で清算
        $query .= ",ATTEND_SUM3 AS ( ";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD, ";
        } else {
            $query .= "        SUBCLASSCD, ";
        }
        $query .= "        SUM(NOTICE_LATE) NOTICE_LATE1 ";
        if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(sum(VALUE(LATE, 0)) + sum(VALUE(EARLY, 0))) / ".$absent_cov_late.") + (sum(VALUE(NOTICE, 0)) + sum(VALUE(NONOTICE, 0)) + sum(VALUE(NURSEOFF, 0)) + sum(VALUE(SICK, 0)) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(VALUE(OFFDAYS, 0)) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(VALUE(SUSPEND, 0)) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VALUE(VIRUS, 0)) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(VALUE(KOUDOME, 0)) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(VALUE(MOURNING, 0)) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(VALUE(ABSENT, 0)) ";
            }
            $query .= "      ), 4, 1) as NOTICE_LATE2 ";
        } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((sum(VALUE(LATE, 0)) + sum(VALUE(EARLY, 0))) / ".$absent_cov_late.") + (sum(VALUE(NOTICE, 0)) + sum(VALUE(NONOTICE, 0)) + sum(VALUE(NURSEOFF, 0)) + sum(VALUE(SICK, 0)) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(VALUE(OFFDAYS, 0)) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(VALUE(SUSPEND, 0)) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VALUE(VIRUS, 0)) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(VALUE(KOUDOME, 0)) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(VALUE(MOURNING, 0)) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(VALUE(ABSENT, 0)) ";
            }
            $query .= "      ) as NOTICE_LATE2 ";
        } else {
            $query .= "      ,sum(VALUE(NOTICE, 0)) + sum(VALUE(NONOTICE, 0)) + sum(VALUE(NURSEOFF, 0)) + sum(VALUE(SICK, 0)) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(VALUE(OFFDAYS, 0)) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(VALUE(SUSPEND, 0)) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VALUE(VIRUS, 0)) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(VALUE(KOUDOME, 0)) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(VALUE(MOURNING, 0)) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(VALUE(ABSENT, 0)) ";
            }
            $query .= "      as NOTICE_LATE2 ";
        }
        $query .= "    FROM ";
        $query .= "        ATTEND_SUM2 ";
        $query .= "    GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD ";
        } else {
            $query .= "        SCHREGNO,SUBCLASSCD ";
        }
        $query .= "    ) ";

        return $query;
    }

    //出欠メイン
    function GetAttendData2($model, $absent_cov, $absent_cov_late)
    {
        $query .= "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T2.CLASSCD, ";
            $query .= "    T2.SCHOOL_KIND, ";
            $query .= "    T2.CURRICULUM_CD, ";
        }
        $query .= "    T2.SUBCLASSCD, ";
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "    NOTICE_LATE1 AS NOTICE_LATE ";
        } else {
            $query .= "    NOTICE_LATE2 AS NOTICE_LATE ";
        }
        $query .= "FROM ";
        $query .= "    SCH_T T1, ";
        $query .= "    ATTEND_SUM3 T2 ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = T2.SCHREGNO ";

        return $query;
    }
}
?>
