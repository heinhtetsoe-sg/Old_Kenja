<?php

require_once('for_php7.php');


class knjd124cquery extends Query
{

    //学期コンボ
    public function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    SEMESTERNAME, ";
        $query .= "    SDATE, ";
        $query .= "    EDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";
        return $query;
    }

    //高校３年の判別
    public function getGDat()
    {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE_CD, ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = 'H' AND ";
        $query .= "     GRADE_CD = '03' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";
        return $query;
    }

    //編集可能学期の判別
    public function selectContolCodeQuery($model)
    {
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$schoolKind."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '1' AND ";
        $query .= "     CONTROL_CODE in ('0182','0282','0382','0882') ";
        return $query;
    }

    //科目データの取得
    public function selectSubclassQuery($model)
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
        $query .= "     T1.semester   = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T1.year       = T4.year AND ";
        $query .= "     T1.semester   = T4.semester AND ";
        $query .= "     T1.chaircd    = T4.chaircd AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T3.subclasscd NOT LIKE '9%' AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "     T3.subclasscd = T1.subclasscd ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
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
    public function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
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
        $query .= "     T1.semester   = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD        = '".$substr_classcd."' AND ";
            $query .= "    T1.SCHOOL_KIND    = '".$substr_school_kind."' AND ";
            $query .= "    T1.CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
            $query .= "    T1.SUBCLASSCD     = '".$substr_subclasscd."'  ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."'  ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        //成績データの表
        $query  = " WITH RECORD_SCORE AS ( ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SELECT  YEAR CLASSCD, SCHOOL_KIND, CURRICULUM_CD ,SUBCLASSCD ,SCHREGNO as SCHREGNO2 ";
        } else {
            $query .= "     SELECT  YEAR ,SUBCLASSCD ,SCHREGNO as SCHREGNO2 ";
        }
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1990000' THEN SCORE END) as SEM1_SCORE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2990000' THEN SCORE END) as SEM2_SCORE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3990000' THEN SCORE END) as SEM3_SCORE ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN SCORE END) as GRAD_SCORE ";
        //未入力・見込点
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1990000' THEN VALUE_DI END) as SEM1_SCORE_DI ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2990000' THEN VALUE_DI END) as SEM2_SCORE_DI ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3990000' THEN VALUE_DI END) as SEM3_SCORE_DI ";
        $query .= "             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN VALUE_DI END) as GRAD_SCORE_DI ";
        $query .= "     FROM   RECORD_SCORE_DAT ";
        $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD          = '".$substr_classcd."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "       AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
            $query .= "     GROUP BY YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ,SCHREGNO ";
        } else {
            $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            $query .= "     GROUP BY YEAR ,SUBCLASSCD ,SCHREGNO ";
        }
        $query .= "     ) ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " ,RECORD_PROV_FLG AS ( ";
            $query .= "     SELECT  SCHREGNO, PROV_FLG ";
            $query .= "     FROM   RECORD_PROV_FLG_DAT ";
            $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       AND  CLASSCD          = '".$substr_classcd."' ";
                $query .= "       AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
                $query .= "       AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                $query .= "       AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
            } else {
                $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "     ) ";
        }

        $query .= " SELECT W0.*";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "       ,P0.PROV_FLG";
        }
        $query .= "       ,W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.name_eng, W2.grd_date, W2.GRD_DIV, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W5.chair_sem1 ";
        $query .= "       ,W6.chair_sem2 ";
        $query .= "       ,W4.chair_sem3 ";
        $query .= "   FROM ";
        $query .= "    (SELECT schregno ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd = '".$model->field["CHAIRCD"]."'  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
        $query .= "    ) W1 ";
        /* 成績情報 */
        $query .= " LEFT JOIN RECORD_SCORE W0 ON W0.SCHREGNO2 = W1.schregno ";
        /* 仮評定情報 */
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " LEFT JOIN RECORD_PROV_FLG P0 ON P0.SCHREGNO = W1.SCHREGNO ";
        }
        /* 基礎情報 */
        $query .= " INNER JOIN ";
        $query .= "    (SELECT T1.year,T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.inoutcd,T2.name_show,T2.name_eng,T2.schregno, T2.grd_date, T2.GRD_DIV, T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE ";
        $query .= "       FROM schreg_regd_hdat T0, schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "      WHERE T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "        AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".$model->field["SEMESTER"]."' ";
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W1.schregno = W2.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 在籍情報(１学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem1";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '1') W5";
        $query .= "   ON W5.schregno = W1.schregno ";
        /* 在籍情報(２学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem2";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '2') W6";
        $query .= "   ON W6.schregno = W1.schregno ";
        /* 在籍情報(３学期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem3";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '3') W4";
        $query .= "   ON W4.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    //考査満点マスタ
    public function getPerfect($year, $subclasscd, $testcd, $grade, $course, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "";
        $query .= "SELECT ";
        $query .= "    case when DIV IS NULL then 10 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "    SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "    CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "    SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "    SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$course}' END ";
        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        $db = Query::dbCheckOut();

        //管理者コントロール
        $admin_key = array();
        $results = $db->query(knjd124cquery::selectContolCodeQuery($model));
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]]=$row_ct["CONTROL_CODE"];
        }
        $results->free();
        
        for ($i=0; $i<get_count($model->data["SCHREGNO"]); $i++) {
            $data = array();
            $val = explode("-", $model->data["SCHREGNO"][$i]);

            $schno = $val[0];
            //１学期(0182)
            if (isset($admin_key["0182"])) {
                knjd124cQuery::insertUpdateRecordScore(CTRL_YEAR, "1", "99", "00", "00", $model, $schno, $i, $db);
            }
            //２学期(0282)
            if (isset($admin_key["0282"])) {
                knjd124cQuery::insertUpdateRecordScore(CTRL_YEAR, "2", "99", "00", "00", $model, $schno, $i, $db);
            }
            //３学期(0382)
            if (isset($admin_key["0382"])) {
                knjd124cQuery::insertUpdateRecordScore(CTRL_YEAR, "3", "99", "00", "00", $model, $schno, $i, $db);
            }
            //学年末(0882)
            if (isset($admin_key["0882"])) {
                knjd124cQuery::insertUpdateRecordScore(CTRL_YEAR, "9", "99", "00", "00", $model, $schno, $i, $db);
            }
        }

        Query::dbCheckIn($db);
    }

    //追加および更新
    public function insertUpdateRecordScore($year, $sem, $kind, $item, $div, $model, $schno, $i, $db)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $data = array();

        $sem_kind_item = $sem.$kind.$item;
        $deleteFlg = false;
        //１学期
        if ($sem_kind_item == "19900") {
            if (!$model->isScoreAllNull["SEM1_SCORE"]) {
                $deleteFlg = true;
            } //レコード削除フラグ
            $data["SCORE"][NUMBER]  = $model->fields["SEM1_SCORE"][$i];
            $data["VALUE_DI"][TEXT] = $model->fields["SEM1_SCORE_DI"][$i];
        //２学期
        } elseif ($sem_kind_item == "29900") {
            if (!$model->isScoreAllNull["SEM2_SCORE"]) {
                $deleteFlg = true;
            } //レコード削除フラグ
            $data["SCORE"][NUMBER]  = $model->fields["SEM2_SCORE"][$i];
            $data["VALUE_DI"][TEXT] = $model->fields["SEM2_SCORE_DI"][$i];
        //３学期
        } elseif ($sem_kind_item == "39900") {
            if (!$model->isScoreAllNull["SEM3_SCORE"]) {
                $deleteFlg = true;
            } //レコード削除フラグ
            $data["SCORE"][NUMBER]  = $model->fields["SEM3_SCORE"][$i];
            $data["VALUE_DI"][TEXT] = $model->fields["SEM3_SCORE_DI"][$i];
        //学年末
        } elseif ($sem_kind_item == "99900") {
            if (!$model->isScoreAllNull["GRAD_SCORE"]) {
                $deleteFlg = true;
            } //レコード削除フラグ
            $data["SCORE"][NUMBER]  = $model->fields["GRAD_SCORE"][$i];
            $data["VALUE_DI"][TEXT] = $model->fields["GRAD_SCORE_DI"][$i];
            //評定フラグ・・・仮評定(1)、本評定(NULL)
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                $query = knjd124cQuery::delRecordProvFlgDat($year, $model->field["SUBCLASSCD"], $schno, $model);
                $db->query($query);
                if ($model->fields["PROV_FLG"][$i] == '1') {
                    $query = knjd124cQuery::insRecordProvFlgDat($year, $model->field["SUBCLASSCD"], $schno, $model);
                    $db->query($query);
                }
            }
        }

        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        $data["SCORE_DIV"][TEXT]    = $div;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]    = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT]  = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]     = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($deleteFlg) {
            //成績が全てNULLの場合、レコードを削除する（つまり、レコードを作成しない）
            $query = knjd124cQuery::deleteRecordScore($year, $sem, $kind, $item, $div, $model->field["SUBCLASSCD"], $schno, $model);
            $db->query($query);
        } else {
            //追加および更新を判定
            $query = knjd124cQuery::existRecordScore($year, $sem, $kind, $item, $div, $model->field["SUBCLASSCD"], $schno, $model);
            $existflg = $db->getOne($query);
            if (0 < $existflg) {
                $where  = "WHERE  YEAR          = '$year' ";
                $where .= "  AND  SEMESTER      = '$sem' ";
                $where .= "  AND  TESTKINDCD    = '$kind' ";
                $where .= "  AND  TESTITEMCD    = '$item' ";
                $where .= "  AND  SCORE_DIV     = '$div' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "  AND  CLASSCD          = '".$substr_classcd."' ";
                    $where .= "  AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
                    $where .= "  AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                    $where .= "  AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
                } else {
                    $where .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
                }
                $where .= "  AND  SCHREGNO      = '$schno' ";
                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            }
            $db->query($query);
        }
    }

    public function updateExecuted2($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();
        $testcdArray = array("0182" => "19900"
                            ,"0282" => "29900"
                            ,"0382" => "39900"
                            ,"0882" => "99900");

        //管理者コントロール
        $query = knjd124cQuery::selectContolCodeQuery($model);
        $results = $db->query($query);
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]] = $row_ct["CONTROL_CODE"];
        }
        $results->free();
        //初期化
        $j = 1;
        foreach ($testcdArray as $testkey => $testval) {
            $query = knjd124cQuery::getRecordChkfinDat($model, $testval);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow) && isset($admin_key[$testkey])) {
                $chkfg = ($model->field["CHK_COMP_VALUE".$j] == 'on') ? '1' : '0';

                $data = array();
                $data["EXECUTED"][TEXT]   = $chkfg ;     //成績入力区分
                $data["REGISTERCD"][TEXT] = STAFFCD ;    //登録者コード
                $data["UPDATED"][FUNC]    = "sysdate()"; //更新日付

                $where  = " WHERE ";
                $where .= "         YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$testval}' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     AND  CLASSCD          = '".$substr_classcd."' ";
                    $where .= "     AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
                    $where .= "     AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                    $where .= "     AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
                } else {
                    $where .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";
                $where .= "     AND RECORD_DIV = '2' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_DAT", $where);

                $db->query($query);
            }
            $j++;
        }
        Query::dbCheckIn($db);
    }

    //テーブルRECORD_PROV_FLG_DATのレコードを削除
    public function delRecordProvFlgDat($year, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclass, 0, 2);
            $substr_school_kind     = substr($subclass, 3, 1);
            $substr_curriculum_cd   = substr($subclass, 5, 1);
            $substr_subclasscd      = substr($subclass, 7, 6);
        }
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_PROV_FLG_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  CLASSCD       = '{$substr_classcd}' ";
            $query .= "  AND  SCHOOL_KIND   = '{$substr_school_kind}' ";
            $query .= "  AND  CURRICULUM_CD = '{$substr_curriculum_cd}' ";
            $query .= "  AND  SUBCLASSCD    = '{$substr_subclasscd}' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '{$subclass}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    public function insRecordProvFlgDat($year, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclass, 0, 2);
            $substr_school_kind     = substr($subclass, 3, 1);
            $substr_curriculum_cd   = substr($subclass, 5, 1);
            $substr_subclasscd      = substr($subclass, 7, 6);
        }
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]   = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT] = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]    = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]    = $subclass;
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        //評定フラグ・・・仮評定(1)、本評定(NULL)
        $data["PROV_FLG"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }

    //テーブルRECORD_SCORE_DATのレコードを削除
    public function deleteRecordScore($year, $sem, $kind, $item, $div, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($subclass, 0, 2);
            $substr_school_kind      = substr($subclass, 3, 1);
            $substr_curriculum_cd    = substr($subclass, 5, 1);
            $substr_subclasscd       = substr($subclass, 7, 6);
        }
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '$year' ";
        $query .= "  AND  SEMESTER      = '$sem' ";
        $query .= "  AND  TESTKINDCD    = '$kind' ";
        $query .= "  AND  TESTITEMCD    = '$item' ";
        $query .= "  AND  SCORE_DIV     = '$div' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND CLASSCD          = '".$substr_classcd."' ";
            $query .= "  AND SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "  AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "  AND SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '$subclass' ";
        }
        $query .= "  AND  SCHREGNO      = '$schno' ";
        return $query;
    }

    //テーブルRECORD_SCORE_DATの存在チェック用SQL
    public function existRecordScore($year, $sem, $kind, $item, $div, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($subclass, 0, 2);
            $substr_school_kind      = substr($subclass, 3, 1);
            $substr_curriculum_cd    = substr($subclass, 5, 1);
            $substr_subclasscd       = substr($subclass, 7, 6);
        }
        $query  = "";
        $query .= "SELECT COUNT(*) ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '$year' ";
        $query .= "  AND  SEMESTER      = '$sem' ";
        $query .= "  AND  TESTKINDCD    = '$kind' ";
        $query .= "  AND  TESTITEMCD    = '$item' ";
        $query .= "  AND  SCORE_DIV     = '$div' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  CLASSCD          = '".$substr_classcd."' ";
            $query .= "  AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "  AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "  AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '$subclass' ";
        }
        $query .= "  AND  SCHREGNO      = '$schno' ";
        return $query;
    }

    //未入力チェックのテーブルにレコードがあるのか
    public function getRecordChkfinDat($model, $testcd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD          = '".$substr_classcd."' ";
            $query .= "     AND SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "     AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "     AND SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";
        $query .= "     AND RECORD_DIV = '2' ";

        return $query;
    }
}
