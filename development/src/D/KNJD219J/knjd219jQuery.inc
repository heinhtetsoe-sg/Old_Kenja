<?php

require_once('for_php7.php');
class knjd219jQuery extends Query {

    //学年コンボ
    function getGrade($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種コンボ
    function getNameMstA023($model, $ncd1, $ncd2="") {
        $query  = " SELECT ";
        if ($ncd2 === "") {
            $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
            $query .= "     NAME1 AS VALUE ";
        } else {
            $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$ncd1}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        if ($ncd2 !== "") {
            $query .= "     AND NAME1 = '{$ncd2}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //名称マスタコンボ設定値取得($ncd2指定時は、名称のみ取得)
    function getNameMst($ncd1, $ncd2="") {
        $query  = " SELECT ";
        if ($ncd2 === "") {
            if ($ncd1 == "A023") {
                $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
                $query .= "     NAME1 AS VALUE ";
            } else {
                $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
                $query .= "     NAMECD2 AS VALUE ";
            }
        } else {
            if ($ncd1 == "A023") {
                $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
            } else {
                $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
            }
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$ncd1}' ";
        if ($ncd2 !== "") {
            if ($ncd1 == "A023") {
                $query .= "     AND NAME1 = '{$ncd2}' ";
            } else {
                $query .= "     AND NAMECD2 = '{$ncd2}' ";
            }
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //学年から校種コードを取得(※初期値取得用)
    function getSchKindFromGrade($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "   SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$year}' ";
        $query .= "   AND GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //学期コンボ
    function getSemester() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースグループコンボ
    function getGroupCd($model) {
        $year = CTRL_YEAR;

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COURSECODE || ':' || T2.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSECODE_MST T2 ";
        $query .= "            ON T2.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclasscd($model) {
        $year = CTRL_YEAR;
        $staffcd = STAFFCD;

        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD || ':' || V1.SUBCLASSNAME AS LABEL, ";
            $query .= "     V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     V1.SUBCLASSCD || ':' || V1.SUBCLASSNAME AS LABEL, ";
            $query .= "     V1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST V1";
        $query .= " WHERE ";
        $query .= "     V1.YEAR = '{$year}' ";
        //成績データにある科目
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD IN ( ";
        } else {
            $query .= "     AND V1.SUBCLASSCD IN ( ";
        }
        $query .= "         SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD ";
        } else {
            $query .= "             L1.SUBCLASSCD ";
        }
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "             INNER JOIN RECORD_SCORE_DAT L1 ";
        $query .= "                 ON  L1.YEAR         = T1.YEAR ";
        $query .= "                 AND L1.SEMESTER     = T1.SEMESTER ";
        $query .= "                 AND L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "                 T1.YEAR     = '{$year}' ";
        if($model->field["SEMESTER"] == '9'){
            $query .= "             AND T1.SEMESTER <> '9' ";
        } else {
            $query .= "             AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        }
        if ($model->field["GRADE"] != '00'){
            $query .= "             AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        }
        $query .= "         GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             L1.CLASSCD, ";
            $query .= "             L1.SCHOOL_KIND, ";
            $query .= "             L1.CURRICULUM_CD, ";
        }
        $query .= "             L1.SUBCLASSCD ";

        $query .= "     ) ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD IN ( ";
            } else {
                $query .= "     AND V1.SUBCLASSCD IN ( ";
            }
            $query .= "         SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            } else {
                $query .= "             T1.SUBCLASSCD ";
            }
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1, ";
            $query .= "             CHAIR_STD_DAT T2, ";
            $query .= "             CHAIR_STF_DAT T4 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '{$year}' ";
            if($model->field["SEMESTER"] == '9'){
                $query .= "             AND T1.SEMESTER   <> '9' ";
            } else {
                $query .= "             AND T1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
            }
            $query .= "             AND SUBSTR(T1.SUBCLASSCD, 1 ,2) <= '90' ";
            $query .= "             AND T2.YEAR       = T1.YEAR ";
            $query .= "             AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.YEAR       = T1.YEAR ";
            $query .= "             AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.STAFFCD    = '{$staffcd}' "; //ログインした先生
            $query .= "         GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         T1.CLASSCD, ";
                $query .= "         T1.SCHOOL_KIND, ";
                $query .= "         T1.CURRICULUM_CD, ";
            }
            $query .= "             T1.SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //算出先成績
    function getCombined($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     SEMESTER || '-' || TESTKINDCD || TESTITEMCD || '-' || SCORE_DIV AS VALUE, ";
        $query .= "     SEMESTER || '-' || TESTKINDCD || TESTITEMCD || '-' || SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$year}' ";
        $query .= "     AND SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD || TESTITEMCD || SCORE_DIV IN ('990008','990009') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別リスト
    function getTestCd($model, $flg, $testcd = "") {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query = " SELECT ";
        $query .= "     T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || '-' || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ";
        $query .= "             ON T2.YEAR        = T1.YEAR ";
        $query .= "            AND T2.SEMESTER    = T1.SEMESTER ";
        $query .= "            AND T2.TESTKINDCD  = T1.TESTKINDCD ";
        $query .= "            AND T2.TESTITEMCD  = T1.TESTITEMCD ";
        $query .= "            AND T2.SCORE_DIV   = T1.SCORE_DIV ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if($flg == 'kihon'){
                //基本設定を参照
                $query .= "    AND T2.CLASSCD       = '00' ";
                $query .= "    AND T2.SCHOOL_KIND   = '{$model->field["SCHOOL_KIND"]}' ";
                $query .= "    AND T2.CURRICULUM_CD = '00' ";
                $query .= "    AND T2.SUBCLASSCD    = '000000' ";
            } else {
                //指定科目を参照
                $query .= "    AND T2.CLASSCD       = '".$subclass_array[0]."' ";
                $query .= "    AND T2.SCHOOL_KIND   = '".$subclass_array[1]."' ";
                $query .= "    AND T2.CURRICULUM_CD = '".$subclass_array[2]."' ";
                $query .= "    AND T2.SUBCLASSCD    = '".$subclass_array[3]."' ";
            }
        } else {
            if($flg == 'kihon'){
                //基本設定を参照
                $query .= "    AND T2.SUBCLASSCD    = '000000' ";
            } else {
                //指定科目を参照
                $query .= "    AND T2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$year}' ";
        if ($model->Properties["KNJD219J_SeisekiSanshutsuPattern"] == "1") {
            $query .= "     AND T1.SEMESTER <> '9' ";
            if($model->field["SEMESTER"] != '9'){
                $query .= "     AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || '-' || T1.SCORE_DIV <> '{$model->field["COMBINED"]}' ";
            }
        } else if($model->field["SEMESTER"] == '9'){
            $query .= "     AND T1.SEMESTER <> '9' ";
        } else {
            $query .= "     AND T1.SEMESTER   =  '{$model->field["SEMESTER"]}' ";
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('990008','990009') ";
        }
        if ($testcd != "") {
            $query .= "     AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$testcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別数
    function getPerTest($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //算出先成績
        $combined_array = array();
        $combined_array = explode("-", $model->field["COMBINED"]);

        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.ATTEND_SEMESTER || '-' || T1.ATTEND_TESTKINDCD || T1.ATTEND_TESTITEMCD || '-' || T1.ATTEND_SCORE_DIV AS TESTCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '{$year}' ";
        $query .= "     AND T1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE      = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.COURSECODE = '{$model->field["GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "     AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND T1.COMBINED_TESTKINDCD  = '".substr($combined_array[1],0,2)."' ";
        $query .= "     AND T1.COMBINED_TESTITEMCD  = '".substr($combined_array[1],2)."' ";
        $query .= "     AND T1.COMBINED_SCORE_DIV   = '".$combined_array[2]."' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //考査満点マスタ(DIV = 01:科目, 02:学年, 04:コースグループ) //「DIV = 03:コース」は設定されていないこと
    function getPerfect($model, $testcd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;
        $query  = "SELECT ";
        $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_SDIV_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' AND ";
        }
//        $query .= "    SEMESTER = '{$model->field["SEMESTER"]}' AND ";
//        $query .= "    TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        $query .= "    SEMESTER || '-' || TESTKINDCD || TESTITEMCD || '-' || SCORE_DIV = '{$testcd}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$model->field["GRADE"]}' END AND ";
        $query .= "    COURSECD = '0' AND ";
        $query .= "    MAJORCD = CASE WHEN DIV IN ('01','02') THEN '000' ELSE '{$model->field["GROUP_CD"]}' END AND ";
        $query .= "    COURSECODE = '0000' ";
        return $query;
    }

    //リスト取得
    function getListPercent($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //算出先成績
        $combined_array = array();
        $combined_array = explode("-", $model->field["COMBINED"]);

        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.PERCENT, ";
        $query .= "     T1.PERFECT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_SEMVAL_PERCENT_SDIV_HDAT T2 ";
        $query .= "         ON  T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.GRADE      = T1.GRADE ";
        $query .= "         AND T2.GROUP_CD   = T1.GROUP_CD ";
        $query .= "         AND T2.COURSECODE = T1.COURSECODE ";
        $query .= "         AND T2.COMBINED_TESTKINDCD = T1.COMBINED_TESTKINDCD ";
        $query .= "         AND T2.COMBINED_TESTITEMCD = T1.COMBINED_TESTITEMCD ";
        $query .= "         AND T2.COMBINED_SCORE_DIV  = T1.COMBINED_SCORE_DIV ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD       = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '{$year}' ";
        $query .= "     AND T1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE      = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.COURSECODE = '{$model->field["GROUP_CD"]}' ";
        $query .= "     AND T1.COMBINED_TESTKINDCD  = '".substr($combined_array[1],0,2)."' ";
        $query .= "     AND T1.COMBINED_TESTITEMCD  = '".substr($combined_array[1],2)."' ";
        $query .= "     AND T1.COMBINED_SCORE_DIV   = '".$combined_array[2]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND T1.CLASSCD           = '".$subclass_array[0]."' ";
            $query .= "    AND T1.SCHOOL_KIND       = '".$subclass_array[1]."' ";
            $query .= "    AND T1.CURRICULUM_CD     = '".$subclass_array[2]."' ";
            $query .= "    AND T1.SUBCLASSCD        = '".$subclass_array[3]."' ";
        } else {
            $query .= "    AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //Insert作成
    function insAssessLevel($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //算出先成績
        $combined_array = array();
        $combined_array = explode("-", $model->field["COMBINED"]);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;

        //削除(科目別学期成績割合ヘッダデータ)
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_SDIV_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE        = '{$model->field["GRADE"]}' ";
        if ($model->field["GROUP_CD"] != '0000' ){
            $query .= "     AND T1.COURSECODE   = '{$model->field["GROUP_CD"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND T1.SUBCLASSCD       = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "         AND T1.COMBINED_TESTKINDCD  = '".substr($combined_array[1],0,2)."' ";
        $query .= "         AND T1.COMBINED_TESTITEMCD  = '".substr($combined_array[1],2)."' ";
        $query .= "         AND T1.COMBINED_SCORE_DIV   = '".$combined_array[2]."' ";
        $db->query($query);
        //追加
        $data = array();
        $data["YEAR"][TEXT]             = $year;
        $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["GROUP_CD"][TEXT]         = '0';
        $data["COURSECODE"][TEXT]       = $model->field["GROUP_CD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["COMBINED_TESTKINDCD"][TEXT]  = substr($combined_array[1],0,2);
        $data["COMBINED_TESTITEMCD"][TEXT]  = substr($combined_array[1],2);
        $data["COMBINED_SCORE_DIV"][TEXT]   = $combined_array[2];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "SUBCLASS_SEMVAL_PERCENT_SDIV_HDAT");
        $db->query($query);

        //削除（科目別学期成績割合データ）
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE        = '{$model->field["GRADE"]}' ";
        if ($model->field["GROUP_CD"] != '0000' ){
            $query .= "     AND T1.COURSECODE   = '{$model->field["GROUP_CD"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND T1.SUBCLASSCD       = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "         AND T1.COMBINED_TESTKINDCD  = '".substr($combined_array[1],0,2)."' ";
        $query .= "         AND T1.COMBINED_TESTITEMCD  = '".substr($combined_array[1],2)."' ";
        $query .= "         AND T1.COMBINED_SCORE_DIV   = '".$combined_array[2]."' ";
        $db->query($query);
        //追加
        for ($i = 1; $i <= $model->field["TEST_COUNT"]; $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["COURSECODE"][TEXT]       = $model->field["GROUP_CD"];
            $data["GROUP_CD"][TEXT]         = '0';
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]     = $model->field["SUBCLASSCD"];
            }
            $data["COMBINED_TESTKINDCD"][TEXT]  = substr($combined_array[1],0,2);
            $data["COMBINED_TESTITEMCD"][TEXT]  = substr($combined_array[1],2);
            $data["COMBINED_SCORE_DIV"][TEXT]   = $combined_array[2];
            $data["SEQ"][NUMBER]            = $i;
            $ar = explode("-", $model->field["TESTCD".$i]);
            $testDiv  = '1';
            $semes    = $ar[0];
            $testcd   = $ar[1];
            $scoreDiv = $ar[2];
            $data["TESTDIV"][TEXT]            = $testDiv;
            $data["ATTEND_SEMESTER"][TEXT]    = $semes;
            $data["ATTEND_TESTKINDCD"][TEXT]  = substr($testcd,0,2);
            $data["ATTEND_TESTITEMCD"][TEXT]  = substr($testcd,2);
            $data["ATTEND_SCORE_DIV"][TEXT]   = $scoreDiv;
            $data["PERCENT"][NUMBER]        = $model->field["PERCENT".$i];
            $data["PERFECT"][NUMBER]        = $model->field["PERFECT".$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $query = Query::insertSQL($data, "SUBCLASS_SEMVAL_PERCENT_SDIV_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
    
    //Delete作成
    function delAssessLevel($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //算出先成績
        $combined_array = array();
        $combined_array = explode("-", $model->field["COMBINED"]);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;

        //削除(科目別学期成績割合ヘッダデータ)
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_SDIV_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE        = '{$model->field["GRADE"]}' ";
        if ($model->field["GROUP_CD"] != '0000' ){
            $query .= "     AND T1.COURSECODE   = '{$model->field["GROUP_CD"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND T1.SUBCLASSCD       = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "         AND T1.COMBINED_TESTKINDCD  = '".substr($combined_array[1],0,2)."' ";
        $query .= "         AND T1.COMBINED_TESTITEMCD  = '".substr($combined_array[1],2)."' ";
        $query .= "         AND T1.COMBINED_SCORE_DIV   = '".$combined_array[2]."' ";
        $db->query($query);

        //削除（科目別学期成績割合データ）
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_SEMVAL_PERCENT_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE        = '{$model->field["GRADE"]}' ";
        if ($model->field["GROUP_CD"] != '0000' ){
            $query .= "     AND T1.COURSECODE   = '{$model->field["GROUP_CD"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "         AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "         AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "         AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND T1.SUBCLASSCD       = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "         AND T1.COMBINED_TESTKINDCD  = '".substr($combined_array[1],0,2)."' ";
        $query .= "         AND T1.COMBINED_TESTITEMCD  = '".substr($combined_array[1],2)."' ";
        $query .= "         AND T1.COMBINED_SCORE_DIV   = '".$combined_array[2]."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
