<?php

require_once('for_php7.php');

class knjd133hquery extends Query {
    //学校判定
    function getSchoolHantei($schoolname) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = '".$schoolname."' ";

        return $query;
    }

    //学期取得
    function getSemester($semester="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        if ($semester) {
            $query .= " AND SEMESTER = '".$semester."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //D校種08カウント取得
    function getNameMstche($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS COUNT";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '$model->che_school_kind' ";

        return $query;
    }

    //科目データの取得
    function selectSubclassQuery($model, $semester) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || ' ' || VALUE(T3.SUBCLASSNAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STF_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = T4.YEAR AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = T4.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$semester."' AND ";
        $query .= "     T1.CHAIRCD      = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD      = T4.CHAIRCD AND ";
        if(0 < $model->count){
            $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = '$model->che_school_kind') OR ";
        } else {
            $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = 'D008') OR ";
        }
        $query .= "     T3.SUBCLASSCD LIKE '90%') AND ";
        //権限(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T4.STAFFCD  = '".STAFFCD."' AND ";
        }
        $query .= "     T1.CLASSCD      = T3.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND  = T3.SCHOOL_KIND AND ";
        //校種
        if ($model->isBunkyo > 0) {
            $query .= "     T1.SCHOOL_KIND  = 'H' AND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD   = T3.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座データの取得
    function selectChairQuery($model, $semester, $subclasscd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || ' ' || VALUE(T1.CHAIRNAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = T2.YEAR AND ";
        $query .= "     T1.YEAR       = T3.YEAR AND ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER   = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER   = '".$semester."' AND ";
        $query .= "     T1.CHAIRCD    = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD    = T3.CHAIRCD AND ";
        //権限(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T3.STAFFCD      = '".STAFFCD."' AND ";
        }
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date, $semester, $subclasscd, $chaircd) {
        //総合的な学習の時間の表
        $query  = " WITH T_REMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         TOTALSTUDYACT, ";
        $query .= "         TOTALSTUDYTIME ";
        $query .= "     FROM ";
        $query .= "         RECORD_TOTALSTUDYTIME_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$semester."' AND ";
        $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        $query .= " ) ";
        //学年評定の表
        $query .= ", T_RECORD AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "        SCORE as GRAD_VALUE, ";
        } else {
            $query .= "        VALUE as GRAD_VALUE, ";
        }
        $query .= "         GET_CREDIT, ";
        $query .= "         COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '9' AND ";
        $query .= "         TESTKINDCD  = '99' AND ";
        $query .= "         TESTITEMCD  = '00' AND ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "         SCORE_DIV  = '09' AND ";
        } else {
            $query .= "         SCORE_DIV  = '00' AND ";
        }
        $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        $query .= "    ) ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            //仮評定の表
            $query .= ", T_PROV_FLG AS ( ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         PROV_FLG ";
            $query .= "     FROM ";
            $query .= "         RECORD_PROV_FLG_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
            $query .= " ) ";
        }

        $query .= " SELECT ";
        $query .= "     W1.SCHREGNO, ";
        $query .= "     R1.CLASSCD, ";
        $query .= "     R1.SCHOOL_KIND, ";
        $query .= "     R1.CURRICULUM_CD, ";
        $query .= "     R1.SUBCLASSCD, ";
        $query .= "     W2.GRADE, ";
        $query .= "     W2.HR_CLASS, ";
        $query .= "     W2.HR_NAME, ";
        $query .= "     W2.ATTENDNO, ";
        $query .= "     W2.NAME_SHOW, ";
        $query .= "     CASE WHEN R1.SCHREGNO IS NULL THEN 0 ELSE 1 END AS FLG , ";
        $query .= "     R1.TOTALSTUDYACT, ";
        $query .= "     R1.TOTALSTUDYTIME, ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "     P1.PROV_FLG, ";
        }
        $query .= "     R2.GRAD_VALUE, ";
        $query .= "     R2.GET_CREDIT, ";
        $query .= "     R2.COMP_CREDIT ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "         SCHREGNO  ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT  ";
        $query .= "     WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         CHAIRCD = '".$chaircd."' AND ";
        $query .= "         '".$execute_date."' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "    ) W1 ";
        $query .= "     INNER JOIN (SELECT ";
        $query .= "                     T1.GRADE, ";
        $query .= "                     T0.HR_NAME, ";
        $query .= "                     T1.HR_CLASS, ";
        $query .= "                     T1.ATTENDNO, ";
        $query .= "                     T2.NAME_SHOW, ";
        $query .= "                     T2.SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_HDAT T0, ";
        $query .= "                     SCHREG_REGD_DAT T1, ";
        $query .= "                     SCHREG_BASE_MST T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T0.YEAR     = T1.YEAR AND ";
        $query .= "                     T0.SEMESTER = T1.SEMESTER AND ";
        $query .= "                     T0.GRADE    = T1.GRADE AND ";
        $query .= "                     T0.HR_CLASS = T1.HR_CLASS AND ";
        $query .= "                     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.SEMESTER = '".$semester."' AND ";
        $query .= "                     T1.SCHREGNO = T2.SCHREGNO ) W2 ON W1.SCHREGNO = W2.SCHREGNO ";
        $query .= "     LEFT JOIN T_REMARK R1 ON R1.SCHREGNO = W1.SCHREGNO ";
        $query .= "     LEFT OUTER JOIN T_RECORD R2 ON R2.SCHREGNO = W1.SCHREGNO ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "     LEFT JOIN T_PROV_FLG P1 ON P1.SCHREGNO = W1.SCHREGNO ";
        }
        $query .= " ORDER BY ";
        $query .= "     W2.GRADE, ";
        $query .= "     W2.HR_CLASS, ";
        $query .= "     W2.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->field["SUBCLASSCD"]);

            if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] != '1') {
                /*** RECORD_TOTALSTUDYTIME_DAT ***/
                $year = CTRL_YEAR;
                $semcd = $model->field["SEMESTER"];
                $schno = $model->data["SCHREGNO"][$i];
                //項目
                $data = array();
                $data["YEAR"][TEXT]             = $year;
                $data["SEMESTER"][TEXT]         = $semcd;
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["SCHREGNO"][TEXT]         = $schno;
                if ($model->Properties["useOnlyTotalstudyTime"] == '1') {
                    $data["TOTALSTUDYTIME"][TEXT]   = $model->fields["TOTALSTUDYTIME"][$i];
                } else {
                    $data["TOTALSTUDYACT"][TEXT]    = $model->fields["TOTALSTUDYACT"][$i];
                    if ($model->Properties["useTotalstudyTime"] == '1') {
                        $data["TOTALSTUDYTIME"][TEXT]   = $model->fields["TOTALSTUDYTIME"][$i];
                    }
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";
                //存在するか？
                $query  = "SELECT COUNT(*) FROM RECORD_TOTALSTUDYTIME_DAT";
                $query .= " WHERE YEAR          = '{$year}'";
                $query .= "   AND SEMESTER      = '{$semcd}'";
                $query .= "   AND CLASSCD       = '{$classcd}' ";
                $query .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
                $query .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
                $query .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
                $query .= "   AND SCHREGNO      = '{$schno}'";
                if (0 < $db->getOne($query)) {
                    //更新
                    $where  = " WHERE YEAR          = '{$year}'";
                    $where .= "   AND SEMESTER      = '{$semcd}'";
                    $where .= "   AND CLASSCD       = '{$classcd}' ";
                    $where .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
                    $where .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
                    $where .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
                    $where .= "   AND SCHREGNO      = '{$schno}'";
                    $query = Query::updateSQL($data, "RECORD_TOTALSTUDYTIME_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "RECORD_TOTALSTUDYTIME_DAT");
                }
                $db->query($query);
            }

            /*** RECORD_SCORE_DAT ***/
            $year   = CTRL_YEAR;
            $semcd  = '9';
            $kindcd = '99';
            $itemcd = '00';
            //パーツタイプ対応
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $scoreDiv = '09';
            } else {
                $scoreDiv = '00';
            }
            $schno  = $model->data["SCHREGNO"][$i];
            list($clscd, $sch_kind, $cur_cd, $subcd)  = preg_split("/-/", $model->field["SUBCLASSCD"]);

            //項目
            $data = array();
            $data["YEAR"][TEXT]               = $year;
            $data["SEMESTER"][TEXT]           = $semcd;
            $data["TESTKINDCD"][TEXT]         = $kindcd;
            $data["TESTITEMCD"][TEXT]         = $itemcd;
            $data["SCORE_DIV"][TEXT]          = $scoreDiv;
            $data["CLASSCD"][TEXT]            = $clscd;
            $data["SCHOOL_KIND"][TEXT]        = $sch_kind;
            $data["CURRICULUM_CD"][TEXT]      = $cur_cd;
            $data["SUBCLASSCD"][TEXT]         = $subcd;
            $data["SCHREGNO"][TEXT]           = $schno;
            if ($model->Properties["useKnjd133hOnlyGradValue"] == '2') {
            } else if ($model->Properties["useKnjd133hOnlyGradValue"] == '1') {
                //パーツタイプ対応
                if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    $data["SCORE"][NUMBER]            = $model->fields["GRAD_VALUE"][$i];
                } else {
                    $data["VALUE"][NUMBER]            = $model->fields["GRAD_VALUE"][$i];
                }
            } else {
                if ($model->fields["CHK_CALC_CREDIT"][$i] == "on") {
                    //チェックありの場合、単位マスタの単位数をセット
                    $query  = " SELECT ";
                    $query .= "     T2.CREDITS ";
                    $query .= " FROM ";
                    $query .= "     SCHREG_REGD_DAT T1, ";
                    $query .= "     CREDIT_MST T2 ";
                    $query .= " WHERE ";
                    $query .= "         T1.SCHREGNO     = '{$schno}' ";
                    $query .= "     AND T1.YEAR         = '{$year}' ";
                    $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
                    $query .= "     AND T2.YEAR         = '{$year}' ";
                    $query .= "     AND T2.COURSECD     = T1.COURSECD ";
                    $query .= "     AND T2.MAJORCD      = T1.MAJORCD ";
                    $query .= "     AND T2.GRADE        = T1.GRADE ";
                    $query .= "     AND T2.COURSECODE   = T1.COURSECODE ";
                    $query .= "     AND T2.CLASSCD      = '{$clscd}'";
                    $query .= "     AND T2.SCHOOL_KIND  = '{$sch_kind}'";
                    $query .= "     AND T2.CURRICULUM_CD = '{$cur_cd}'";
                    $query .= "     AND T2.SUBCLASSCD   = '{$subcd}' ";

                    $data["COMP_CREDIT"][NUMBER]      = $db->getOne($query);
                    $data["GET_CREDIT"][NUMBER]       = $db->getOne($query);
                } else {
                    //チェックなしの場合、画面の入力値をセット
                    $data["COMP_CREDIT"][NUMBER]      = $model->fields["COMP_CREDIT"][$i];
                    $data["GET_CREDIT"][NUMBER]       = $model->fields["GET_CREDIT"][$i];
                }
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
            $query .= "   AND CLASSCD       = '{$clscd}'";
            $query .= "   AND SCHOOL_KIND   = '{$sch_kind}'";
            $query .= "   AND CURRICULUM_CD = '{$cur_cd}'";
            $query .= "   AND SUBCLASSCD    = '{$subcd}'";
            $query .= "   AND SCHREGNO      = '{$schno}'";
            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE YEAR          = '{$year}'";
                $where .= "   AND SEMESTER      = '{$semcd}'";
                $where .= "   AND TESTKINDCD    = '{$kindcd}'";
                $where .= "   AND TESTITEMCD    = '{$itemcd}'";
                $where .= "   AND SCORE_DIV     = '{$scoreDiv}'";
                $where .= "   AND CLASSCD       = '{$clscd}'";
                $where .= "   AND SCHOOL_KIND   = '{$sch_kind}'";
                $where .= "   AND CURRICULUM_CD = '{$cur_cd}'";
                $where .= "   AND SUBCLASSCD    = '{$subcd}'";
                $where .= "   AND SCHREGNO      = '{$schno}'";
                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            }
            $db->query($query);

            /*** RECORD_PROV_FLG_DAT ***/
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                //削除
                $query  = "DELETE FROM RECORD_PROV_FLG_DAT";
                $query .= " WHERE YEAR          = '{$year}'";
                $query .= "   AND CLASSCD       = '{$clscd}'";
                $query .= "   AND SCHOOL_KIND   = '{$sch_kind}'";
                $query .= "   AND CURRICULUM_CD = '{$cur_cd}'";
                $query .= "   AND SUBCLASSCD    = '{$subcd}'";
                $query .= "   AND SCHREGNO      = '{$schno}'";
                $db->query($query);
                //追加
                if ($model->fields["PROV_FLG"][$i] == '1') {
                    //項目
                    $data = array();
                    $data["YEAR"][TEXT]               = $year;
                    $data["CLASSCD"][TEXT]            = $clscd;
                    $data["SCHOOL_KIND"][TEXT]        = $sch_kind;
                    $data["CURRICULUM_CD"][TEXT]      = $cur_cd;
                    $data["SUBCLASSCD"][TEXT]         = $subcd;
                    $data["SCHREGNO"][TEXT]           = $schno;
                    $data["PROV_FLG"][TEXT]           = "1";
                    $data["REGISTERCD"][TEXT]         = STAFFCD;
                    $data["UPDATED"][FUNC]            = "sysdate()";
                    $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
                    $db->query($query);
                }
            }
        }

        Query::dbCheckIn($db);
    }

/*===================================定型文選択===================================*/

    //学年取得
    function getGrade($model, $semester='', $chaircd='') {
        if ($semester == '' && $chaircd == ''){
            $semester = $model->sem;
            $chaircd = $model->chr;
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     CHAIR_STD_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "     T3.CHAIRCD  = '".$chaircd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //定型文マスタ一覧取得
    function getHtrainRemarkTempDat($model,$grade) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$grade."' AND ";
        if ($model->cmd == "teikei") {
            $query .= "     DATA_DIV    = '03' ";
        } else if ($model->cmd == "teikei2" || $model->cmd == "replace1") {
            $query .= "     DATA_DIV    = '04' ";
        }
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }
    
    //評価一括更新
    function getReplaceQuery($model, $totalstudytime){
    
        $db = Query::dbCheckOut();

        $selectdata = explode(',',$model->replace_data["selectdata"]);
            
        for ($i = 0; $i < get_count($selectdata); $i++) {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->replace_get["SUBCLASSCD"]);

            if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] != '1') {
                /*** RECORD_TOTALSTUDYTIME_DAT ***/
                $year = CTRL_YEAR;
                $semcd = $model->replace_get["SEMESTER"];
                $schno = $selectdata[$i];
                //項目
                $data = array();
                $data["YEAR"][TEXT]             = $year;
                $data["SEMESTER"][TEXT]         = $semcd;
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["SCHREGNO"][TEXT]         = $schno;
                $data["TOTALSTUDYTIME"][TEXT]   = $totalstudytime;
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";
                //存在するか？
                $query  = "SELECT COUNT(*) FROM RECORD_TOTALSTUDYTIME_DAT";
                $query .= " WHERE YEAR          = '{$year}'";
                $query .= "   AND SEMESTER      = '{$semcd}'";
                $query .= "   AND CLASSCD       = '{$classcd}' ";
                $query .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
                $query .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
                $query .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
                $query .= "   AND SCHREGNO      = '{$schno}'";
                if (0 < $db->getOne($query)) {
                    //更新
                    $where  = " WHERE YEAR          = '{$year}'";
                    $where .= "   AND SEMESTER      = '{$semcd}'";
                    $where .= "   AND CLASSCD       = '{$classcd}' ";
                    $where .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
                    $where .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
                    $where .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
                    $where .= "   AND SCHREGNO      = '{$schno}'";
                    $query = Query::updateSQL($data, "RECORD_TOTALSTUDYTIME_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "RECORD_TOTALSTUDYTIME_DAT");
                }
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }
}
?>
