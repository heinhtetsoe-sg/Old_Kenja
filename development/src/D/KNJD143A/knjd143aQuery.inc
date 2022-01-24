<?php

require_once('for_php7.php');


class knjd143aquery extends Query {

    //科目データの取得
    function selectSubclassQuery($model) {
        $gradestr = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     T3.SUBCLASSNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON ";
        $query .= "         T2.YEAR       = T1.YEAR AND ";
        $query .= "         T2.SEMESTER   = T1.SEMESTER AND ";
        $query .= "         T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST T3 ON ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD      = T1.CLASSCD AND ";
            $query .= "     T3.SCHOOL_KIND  = T1.SCHOOL_KIND AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "     T3.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T4 ON ";
        $query .= "         T1.YEAR       = T4.YEAR AND ";
        $query .= "         T1.SEMESTER   = T4.SEMESTER AND ";
        $query .= "         T1.CHAIRCD    = T4.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER   = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "    AND T3.SCHOOL_KIND = ( SELECT T5.SCHOOL_KIND FROM SCHREG_REGD_GDAT T5 WHERE T5.YEAR = T1.YEAR AND GRADE = '".$gradestr."') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND T4.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";
        return $query;
    }

    //学期の取得
    function selectSemesterQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER ";
        return $query;
    }

    // 最終学期か
    function isLastSemester($db, $semester) {
        $query  = " SELECT ";
        $query .= "     MAX(T1.SEMESTER) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER <> '9' ";

        $maxSemester = $db->getOne($query);

        if ($maxSemester == $semester) {
            return true;
        }
        return false;
    }

    function getTestitemMstCountflgTestitemname($model, $semester) {
        $query  = " SELECT ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.TESTKINDCD = '99' ";
        $query .= "     AND T1.TESTITEMCD = '00' ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     AND T1.SCORE_DIV = '08' ";
        }
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date) {
        $gradestr = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        //総合的な学習の時間の表
        $query  = "WITH T_REMARK AS(";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= "        CONDUCT_EVAL ";
        $query .= "    FROM ";
        $query .= "        RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "        AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "    ) ";
        //学期成績、学年成績の表
        $query .= ", T_RECORD AS(";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO, ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "        SCORE as GRAD_VALUE ";
        } else {
            $query .= "        VALUE as GRAD_VALUE ";
        }
        $query .= "    FROM ";
        $query .= "        RECORD_SCORE_DAT ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' ";
        if ($model->isLastSemester) {
            $query .= "        AND SEMESTER = '9' ";
        } else {
            $query .= "        AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "        AND TESTKINDCD = '99' ";
        $query .= "        AND TESTITEMCD = '00' ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "        AND SCORE_DIV  = '08'";
        } else {
            $query .= "        AND SCORE_DIV  = '00'";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "        AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "    ) ";

        $query .= " SELECT W2.SCHREGNO";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       ,R1.CLASSCD";
            $query .= "       ,R1.SCHOOL_KIND";
            $query .= "       ,R1.CURRICULUM_CD";
        }
        $query .= "       ,R1.SUBCLASSCD";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.name_show";
        $query .= "       ,CASE WHEN R1.SCHREGNO IS NULL ";
        $query .= "             THEN 0 ";
        $query .= "             ELSE 1 ";
        $query .= "        END AS FLG ";
        $query .= "       ,R1.CONDUCT_EVAL ";
        $query .= "       ,R2.GRAD_VALUE ";
        $query .= "   FROM ";
        /* 基礎情報 */
        $query .= "    (SELECT T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.name_show,T2.schregno ";
        $query .= "       FROM schreg_regd_hdat T0 ";
        $query .= "            INNER JOIN schreg_regd_dat T1 ON ";
        $query .= "            T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "            INNER JOIN schreg_base_mst T2 ON T1.schregno = T2.schregno ";
        $query .= "      WHERE ";
        $query .= "            T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".$model->field["SEMESTER"]."' ";
        $query .= "        ) W2 ";

        /* 備考情報 */
        $query .= " LEFT OUTER JOIN T_REMARK R1 ON R1.schregno = W2.schregno ";

        /* 成績情報 */
        $query .= " LEFT OUTER JOIN T_RECORD R2 ON R2.schregno = W2.schregno ";

        $query .= " WHERE ";
        $query .= "   W2.GRADE || W2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        
        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        //教育過程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->field["SUBCLASSCD"]);
        } else {
            $subclasscd  = $model->field["SUBCLASSCD"];
        }
        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $year = CTRL_YEAR;
            $semcd = $model->field["SEMESTER"];
            $schno = $model->data["SCHREGNO"][$i];
            /*** RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT ***/
            //項目
            $data = array();
            $data["YEAR"][TEXT]               = $year;
            $data["SEMESTER"][TEXT]           = $semcd;
            //教育過程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]         = $classcd;
                $data["SCHOOL_KIND"][TEXT]     = $school_kind;
                $data["CURRICULUM_CD"][TEXT]   = $curriculum_cd;
            }
            $data["SUBCLASSCD"][TEXT]         = $subclasscd;
            $data["SCHREGNO"][TEXT]           = $schno;
            $data["CONDUCT_EVAL"][TEXT]     = $model->fields["CONDUCT_EVAL"][$i];
            $data["REGISTERCD"][TEXT]         = STAFFCD ;
            $data["UPDATED"][FUNC]            = "sysdate()";
            //存在するか？
            $query  = "SELECT COUNT(*) FROM RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT";
            $query .= " WHERE YEAR = '{$year}'";
            $query .= "   AND SEMESTER = '{$semcd}'";
            //教育過程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND CLASSCD       = '".$classcd."' ";
                $query .= "   AND SCHOOL_KIND   = '".$school_kind."' ";
                $query .= "   AND CURRICULUM_CD = '".$curriculum_cd."' ";
            }
            $query .= "   AND SUBCLASSCD    = '".$subclasscd."' ";
            $query .= "   AND SCHREGNO = '{$schno}'";
            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE YEAR = '{$year}'";
                $where .= "   AND SEMESTER = '{$semcd}'";
                //教育過程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "   AND CLASSCD       = '".$classcd."' ";
                    $where .= "   AND SCHOOL_KIND   = '".$school_kind."' ";
                    $where .= "   AND CURRICULUM_CD = '".$curriculum_cd."' ";
                }
                $where .= "   AND SUBCLASSCD    = '".$subclasscd."' ";
                $where .= "   AND SCHREGNO = '{$schno}'";
                $query = Query::updateSQL($data, "RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT");
            }
            $db->query($query);

            /*** RECORD_SCORE_DAT ***/
            $year   = CTRL_YEAR;
            if ($model->isLastSemester) {
                $semcd  = "9";
            } else {
                $semcd  = $model->field["SEMESTER"];
            }
            $kindcd = '99';
            $itemcd = '00';
            //パーツタイプ対応
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $scoreDiv = '08';
            } else {
                $scoreDiv = '00';
            }
            $schno  = $model->data["SCHREGNO"][$i];
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($clscd, $sch_kind, $cur_cd, $subcd)  = preg_split("/-/", $model->field["SUBCLASSCD"]);
            } else {
                $subcd  = $model->field["SUBCLASSCD"];
            }
            //項目
            $data = array();
            $data["YEAR"][TEXT]               = $year;
            $data["SEMESTER"][TEXT]           = $semcd;
            $data["TESTKINDCD"][TEXT]         = $kindcd;
            $data["TESTITEMCD"][TEXT]         = $itemcd;
            $data["SCORE_DIV"][TEXT]          = $scoreDiv;
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]            = $clscd;
                $data["SCHOOL_KIND"][TEXT]        = $sch_kind;
                $data["CURRICULUM_CD"][TEXT]      = $cur_cd;
            }
            $data["SUBCLASSCD"][TEXT]         = $subcd;
            $data["SCHREGNO"][TEXT]           = $schno;
            //パーツタイプ対応
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $data["SCORE"][NUMBER]            = $model->fields["GRAD_VALUE"][$i];
            } else {
                $data["VALUE"][NUMBER]            = $model->fields["GRAD_VALUE"][$i];
            }
            
            $data["REGISTERCD"][TEXT]         = STAFFCD ;
            $data["UPDATED"][FUNC]            = "sysdate()";

            //存在するか？
            $query  = "SELECT COUNT(*) FROM RECORD_SCORE_DAT";
            $query .= " WHERE YEAR          = '{$year}'";
            $query .= "   AND SEMESTER      = '{$semcd}'";
            $query .= "   AND TESTKINDCD    = '{$kindcd}'";
            $query .= "   AND TESTITEMCD    = '{$itemcd}'";
            $query .= "   AND SCORE_DIV     = '{$scoreDiv}'";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND CLASSCD       = '{$clscd}'";
                $query .= "   AND SCHOOL_KIND   = '{$sch_kind}'";
                $query .= "   AND CURRICULUM_CD = '{$cur_cd}'";
            }
            $query .= "   AND SUBCLASSCD    = '{$subcd}'";
            $query .= "   AND SCHREGNO      = '{$schno}'";
            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE YEAR          = '{$year}'";
                $where .= "   AND SEMESTER      = '{$semcd}'";
                $where .= "   AND TESTKINDCD    = '{$kindcd}'";
                $where .= "   AND TESTITEMCD    = '{$itemcd}'";
                $where .= "   AND SCORE_DIV     = '{$scoreDiv}'";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "   AND CLASSCD       = '{$clscd}'";
                    $where .= "   AND SCHOOL_KIND   = '{$sch_kind}'";
                    $where .= "   AND CURRICULUM_CD = '{$cur_cd}'";
                }
                $where .= "   AND SUBCLASSCD    = '{$subcd}'";
                $where .= "   AND SCHREGNO      = '{$schno}'";
                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    (    T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "      OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "      OR T1.TR_CD3 = '".STAFFCD."' ";
            $query .= "      OR T1.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "      OR T1.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "      OR T1.SUBTR_CD3 = '".STAFFCD."') AND ";
        }
        $query .= "     T2.SCHOOL_KIND IN ('P','J', 'H') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種一覧取得
    function getSchoolKindList($model, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     NAME1 AS VALUE  ";
        } else {
            $query .= "     NAME1 AS VALUE, ";
            $query .= "     ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->selectSchoolKind) {
            $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //Z010
    function getZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //通知表パターン
    function getPattern($model) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT T1 ";
        $query .= "     INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'A035' AND T2.NAMECD2 = T1.REMARK1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND T1.GRADE = '00' ";
        $query .= "     AND T1.COURSECD = '0' ";
        $query .= "     AND T1.MAJORCD = '000' ";
        $query .= "     AND T1.COURSECODE = '0000' ";
        $query .= "     AND T1.SEQ = '202' ";

        return $query;
    }

}
?>
