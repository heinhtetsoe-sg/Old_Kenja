<?php

require_once('for_php7.php');


class knjd143query extends Query {

    //科目データの取得
    function selectSubclassQuery($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T3.SUBCLASSCD, ";
        }
        $query .= "     T3.SUBCLASSNAME ";
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //if ($model->Properties["useCurriculumcd"] == '1') {
        //    $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ";
        //} else {
        //    $query .= "     AND T3.SUBCLASSCD ";
        //}
        //$query .= "     IN (SELECT N1.NAME1 FROM NAME_MST N1 WHERE N1.NAMECD1 = 'D080') "; // D080の科目が対象
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
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBCLASSCD ";
        } else {
            $query .= "     T3.SUBCLASSCD ";
        }
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON ";
        $query .= "         T2.YEAR       = T1.YEAR AND ";
        $query .= "         T2.SEMESTER   = T1.SEMESTER AND ";
        $query .= "         T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T3 ON ";
        $query .= "         T1.YEAR       = T3.YEAR AND ";
        $query .= "         T1.SEMESTER   = T3.SEMESTER AND ";
        $query .= "         T1.CHAIRCD    = T3.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER   = '".$model->field["SEMESTER"]."' ";
        }
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       AND T3.STAFFCD = '".STAFFCD."' ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";
        return $query;
    }

    //学期の取得
    function selectSemesterQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTERNAME";
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

    //定型文取得
    function getHtrainremarkTempDat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.PATTERN_CD, ";
        $query .= "     T3.REMARK ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN HTRAINREMARK_TEMP_DAT T3 ON ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T2.GRADE    = T3.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER   = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        $query .= "     AND T3.DATA_DIV = '04' "; // ?
        $query .= " ORDER BY ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.PATTERN_CD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date) {
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
        $query .= "        CONDUCT_CONTENTS, ";
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

        $query .= " SELECT W1.SCHREGNO";
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
        $query .= "       ,R1.CONDUCT_CONTENTS ";
        $query .= "       ,R1.CONDUCT_EVAL ";
        $query .= "       ,R2.GRAD_VALUE ";
        $query .= "   FROM ";
        $query .= "    (SELECT SCHREGNO  ";
        $query .= "       FROM CHAIR_STD_DAT  ";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= "        AND '".$execute_date."' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "    ) W1 ";
        /* 基礎情報 */
        $query .= " INNER JOIN ";
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
        $query .= "   ON W1.schregno = W2.schregno ";

        /* 備考情報 */
        $query .= " LEFT OUTER JOIN T_REMARK R1 ON R1.schregno = W1.schregno ";

        /* 成績情報 */
        $query .= " LEFT OUTER JOIN T_RECORD R2 ON R2.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //教育過程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->field["SUBCLASSCD"]);
            } else {
                $subclasscd  = $model->field["SUBCLASSCD"];
            }
            /*** RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT ***/
            $year = CTRL_YEAR;
            $semcd = $model->field["SEMESTER"];
            $schno = $model->data["SCHREGNO"][$i];
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
            $data["CONDUCT_CONTENTS"][TEXT]      = $model->fields["CONDUCT_CONTENTS"][$i];
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

/*===================================一括更新画面===================================*/

//    //講座名取得
//    function getChairName($model)
//    {
//        $query  = " SELECT ";
//        $query .= "     T2.SUBCLASSNAME || '　　' || T1.CHAIRNAME AS CHAIRNAME ";
//        $query .= " FROM ";
//        $query .= "     CHAIR_DAT T1 ";
//        if ($model->Properties["useCurriculumcd"] == '1') {
//            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.CLASSCD         = T2.CLASSCD ";
//            $query .= "                              AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
//            $query .= "                              AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
//            $query .= "                              AND T1.SUBCLASSCD      = T2.SUBCLASSCD ";
//        } else {
//            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
//        }
//        $query .= " WHERE ";
//        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
//        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
//        $query .= "     T1.CHAIRCD  = '".$model->chaircd."' ";
//
//        return $query;
//    }

//    //置換画面用の生徒一覧取得
//    function getStudent($model, $execute_date, $flg="")
//    {
//        $selectdata = str_replace(",","','",$model->replace_data["selectdata"]);
//
//        $query  = " WITH SCHINFO AS ( ";
//        $query .= "     SELECT ";
//        $query .= "         T1.SCHREGNO, ";
//        $query .= "         T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE, ";
//        $query .= "         T3.HR_NAME || ' ' || T1.ATTENDNO || '番' AS ATTENDNO, ";
//        $query .= "         T2.NAME_SHOW ";
//        $query .= "     FROM ";
//        $query .= "         SCHREG_REGD_DAT T1 ";
//        $query .= "         LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
//        $query .= "         LEFT OUTER JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR AND ";
//        $query .= "                 T1.SEMESTER = T3.SEMESTER AND T1.GRADE = T3.GRADE AND T1.HR_CLASS = T3.HR_CLASS ";
//        $query .= "     WHERE ";
//        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
//        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
//        $query .= " ), CHR_STD AS ( ";
//        $query .= "     SELECT ";
//        $query .= "         SCHREGNO ";
//        $query .= "     FROM ";
//        $query .= "         CHAIR_STD_DAT ";
//        $query .= "     WHERE ";
//        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
//        $query .= "         SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
//        $query .= "         CHAIRCD     = '".$model->chaircd."' AND ";
//        $query .= "         '".$execute_date."' BETWEEN APPDATE AND APPENDDATE ";
//        $query .= " ) ";
//
//        $query .= " SELECT ";
//        $query .= "     T2.VALUE, ";
//        $query .= "     T2.ATTENDNO, ";
//        $query .= "     T2.NAME_SHOW ";
//        $query .= " FROM ";
//        $query .= "     CHR_STD T1, ";
//        $query .= "     SCHINFO T2 ";
//        $query .= " WHERE ";
//        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
//        if($flg == "1"){
//            $query .= "     T1.SCHREGNO IN ('".$selectdata."') ";
//        } else {
//            $query .= "     T1.SCHREGNO NOT IN ('".$selectdata."') ";
//        }
//        $query .= " ORDER BY";
//        $query .= "     T2.VALUE ";
//
//        return $query;
//    }

//    //一括更新
//    function ReplaceUpdate($model)
//    {
//        $db = Query::dbCheckOut();
//        $model->replace_data["selectdata"] = explode(",", $model->replace_data["selectdata"]);
//
//        for ($i = 0; $i < get_count($model->replace_data["selectdata"]); $i++) {
//            $year     = CTRL_YEAR;
//            $semester = '9';
//            $schregno = $model->replace_data["selectdata"][$i];
//            $kindcd = '99';
//            $itemcd = '00';
//            //パーツタイプ対応
//            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
//                $scoreDiv = '09';
//            } else {
//                $scoreDiv = '00';
//            }
//            if ($model->Properties["useCurriculumcd"] == '1') {
//                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->subclasscd);
//            } else {
//                $subclasscd  = $model->subclasscd;
//            }
//
//            if(($model->replace_data["check"][0] == "1") || ($model->replace_data["check"][1] == "1")){
//                /*** RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT ***/
//                //項目
//                $data = array();
//                $data["YEAR"][TEXT]             = $year;
//                $data["SEMESTER"][TEXT]         = $semester;
//                //教育過程対応
//                if ($model->Properties["useCurriculumcd"] == '1') {
//                    $data["CLASSCD"][TEXT]         = $classcd;
//                    $data["SCHOOL_KIND"][TEXT]     = $school_kind;
//                    $data["CURRICULUM_CD"][TEXT]   = $curriculum_cd;
//                }
//                $data["SUBCLASSCD"][TEXT]      = $subclasscd;
//                $data["SCHREGNO"][TEXT]         = $schregno;
//                if($model->replace_data["check"][0] == "1") $data["CONDUCT_CONTENTS"][TEXT]    = $model->replace_data["field"]["CONDUCT_CONTENTS"];
//                if($model->replace_data["check"][1] == "1") $data["CONDUCT_EVAL"][TEXT]   = $model->replace_data["field"]["CONDUCT_EVAL"];
//                $data["REGISTERCD"][TEXT]       = STAFFCD ;
//                $data["UPDATED"][FUNC]          = "sysdate()";
//
//                //存在チェック
//                $query  = " SELECT COUNT(*) FROM RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT ";
//                $query .= " WHERE YEAR     = '".$year."' AND ";
//                $query .= "       SEMESTER = '".$semester."' AND ";
//                //教育過程対応
//                if ($model->Properties["useCurriculumcd"] == '1') {
//                    $query .= "       CLASSCD       = '".$classcd."' AND ";
//                    $query .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
//                    $query .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
//                }
//                $query .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
//                $query .= "       SCHREGNO = '".$schregno."' ";
//                if (0 < $db->getOne($query)) {
//                    //更新
//                    $where  = " WHERE YEAR     = '".$year."' AND ";
//                    $where .= "       SEMESTER = '".$semester."' AND ";
//                    //教育過程対応
//                    if ($model->Properties["useCurriculumcd"] == '1') {
//                        $where .= "       CLASSCD       = '".$classcd."' AND ";
//                        $where .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
//                        $where .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
//                    }
//                    $where .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
//                    $where .= "       SCHREGNO = '".$schregno."'";
//                    $query = Query::updateSQL($data, "RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT", $where);
//                } else {
//                    //追加
//                    $query = Query::insertSQL($data, "RECORD_EDUCATE_GUIDANCE_CONDUCT_DAT");
//                }
//                $db->query($query);
//            }
//
//            if(($model->replace_data["check"][2] == "1") || ($model->replace_data["check"][3] == "1") || ($model->replace_data["check"][4] == "1")){
//                /*** RECORD_SCORE_DAT ***/
//                //項目
//                $data = array();
//                $data["YEAR"][TEXT]             = $year;
//                $data["SEMESTER"][TEXT]         = $semester;
//                $data["TESTKINDCD"][TEXT]       = $kindcd;
//                $data["TESTITEMCD"][TEXT]       = $itemcd;
//                $data["SCORE_DIV"][TEXT]        = $scoreDiv;
//                if ($model->Properties["useCurriculumcd"] == '1') {
//                    $data["CLASSCD"][TEXT]          = $classcd;
//                    $data["SCHOOL_KIND"][TEXT]      = $school_kind;
//                    $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
//                }
//                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
//                $data["SCHREGNO"][TEXT]         = $schregno;
//                //パーツタイプ対応
//                if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
//                    if($model->replace_data["check"][2] == "1") $data["SCORE"][NUMBER]          = $model->replace_data["field"]["GRAD_VALUE"];
//                } else {
//                    if($model->replace_data["check"][2] == "1") $data["VALUE"][NUMBER]          = $model->replace_data["field"]["GRAD_VALUE"];
//                }
//                if($model->replace_data["check"][3] == "1") $data["COMP_CREDIT"][NUMBER]    = $model->replace_data["field"]["COMP_CREDIT"];
//                if($model->replace_data["check"][4] == "1") $data["GET_CREDIT"][NUMBER]     = $model->replace_data["field"]["GET_CREDIT"];
//                $data["REGISTERCD"][TEXT]       = STAFFCD ;
//                $data["UPDATED"][FUNC]          = "sysdate()";
//
//                //存在チェック
//                $query  = "SELECT COUNT(*) FROM RECORD_SCORE_DAT";
//                $query .= " WHERE YEAR          = '".$year."' AND ";
//                $query .= "       SEMESTER      = '".$semester."' AND ";
//                $query .= "       TESTKINDCD    = '".$kindcd."' AND ";
//                $query .= "       TESTITEMCD    = '".$itemcd."' AND ";
//                $query .= "       SCORE_DIV     = '".$scoreDiv."' AND ";
//                if ($model->Properties["useCurriculumcd"] == '1') {
//                    $query .= "       CLASSCD       = '".$classcd."' AND ";
//                    $query .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
//                    $query .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
//                }
//                $query .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
//                $query .= "       SCHREGNO      = '".$schregno."' ";
//                if (0 < $db->getOne($query)) {
//                    //更新
//                    $where  = " WHERE YEAR          = '".$year."' AND ";
//                    $where .= "       SEMESTER      = '".$semester."' AND ";
//                    $where .= "       TESTKINDCD    = '".$kindcd."' AND ";
//                    $where .= "       TESTITEMCD    = '".$itemcd."' AND ";
//                    $where .= "       SCORE_DIV     = '".$scoreDiv."' AND ";
//                    if ($model->Properties["useCurriculumcd"] == '1') {
//                        $where .= "       CLASSCD       = '".$classcd."' AND ";
//                        $where .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
//                        $where .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
//                    }
//                    $where .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
//                    $where .= "       SCHREGNO      = '".$schregno."' ";
//                    $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
//                } else {
//                    //追加
//                    $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
//                }
//                $db->query($query);
//            }
//        }
//        Query::dbCheckIn($db);
//    }

/*===================================定型文選択===================================*/

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.GRADE    = T2.GRADE ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T3 ON ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T3.CHAIRCD  = '".$model->chaircd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //定型文マスタ一覧取得
    function getHtrainRemarkTempDatIkkatsu($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$model->subField["GRADE"]."' AND ";
        $query .= "     DATA_DIV    = '03' "; // ?
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }
}
?>
