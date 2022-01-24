<?php

require_once('for_php7.php');


class knjd133query extends Query
{

    //学校区分
    public function getSchoolDivName($schoolname)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = '".$schoolname."' ";

        $retVal = $db->getOne($query);
        Query::dbCheckIn($db);

        return $retVal;
    }

    // 名称マスタ取得
    public function getNameMst($nameCd1, $nameCd2)
    {
        $query  = " SELECT * ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = '{$nameCd1}' ";
        if ($nameCd2) {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }
        return $query;
    }

    public function getNameMstche($model)
    {
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
    public function selectSubclassQuery($model, $gen_ed)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T3.SUBCLASSCD, ";
        }
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STF_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.YEAR       = T1.YEAR AND ";
        $query .= "     T1.YEAR       = T4.YEAR AND ";
        $query .= "     T1.SEMESTER   = T4.SEMESTER AND ";
        $query .= "     T1.CHAIRCD    = T4.CHAIRCD AND ";
        $query .= "     T2.SEMESTER   = T1.SEMESTER AND ";
        $query .= "     T2.CHAIRCD    = T1.CHAIRCD AND ";
        //教科科目名称マスタのマスタ化
        /*if ($model->Properties["useClassDetailDat"] == '1' && $model->Properties["useCurriculumcd"] == '1') {
            $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) || '-' || T3.SCHOOL_KIND IN (SELECT N1.CLASSCD || '-' || N1.SCHOOL_KIND AS SETCD FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '".CTRL_YEAR."' AND N1.CLASS_SEQ = '003') OR ";
        } else {
            $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = 'D008') OR ";
        }*/
        if (0 < $model->count) {
            $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = '$model->che_school_kind') OR ";
        } else {
            $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = 'D008') OR ";
        }
        
        $query .= "     T3.SUBCLASSCD LIKE '90%') AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     T4.STAFFCD = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD      = T1.CLASSCD AND ";
            $query .= "     T3.SCHOOL_KIND  = T1.SCHOOL_KIND AND ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " T3.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            }
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            $query .= "     T3.SUBCLASSCD   = T1.SUBCLASSCD ";
            $query .= " GROUP BY ";
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
            $query .= "     T3.SUBCLASSCD, ";
            $query .= "     T3.SUBCLASSNAME ";
            $query .= " ORDER BY ";
            $query .= "     SUBCLASSCD ";
        } else {
            $query .= "     T3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " GROUP BY ";
            $query .= "     T3.SUBCLASSCD, ";
            $query .= "     T3.SUBCLASSNAME ";
            $query .= " ORDER BY ";
            $query .= "     T3.SUBCLASSCD ";
        }
        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.YEAR       = T1.YEAR AND ";
        $query .= "     T2.SEMESTER   = T1.SEMESTER AND ";
        $query .= "     T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "     T1.YEAR       = T3.YEAR AND ";
        $query .= "     T1.SEMESTER   = T3.SEMESTER AND ";
        $query .= "     T1.CHAIRCD    = T3.CHAIRCD AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "       T3.STAFFCD = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";
        return $query;
    }

    //定型文取得
    public function getHtrainremarkTempDat($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.PATTERN_CD, ";
        $query .= "     T3.REMARK ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2, ";
        $query .= "     HTRAINREMARK_TEMP_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "     T2.GRADE    = T3.GRADE AND ";
        $query .= "     T3.DATA_DIV = '04' ";
        $query .= " ORDER BY ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.PATTERN_CD ";

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date)
    {
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
        $query .= "        TOTALSTUDYACT, ";
        $query .= "        TOTALSTUDYTIME ";
        $query .= "    FROM ";
        $query .= "        RECORD_TOTALSTUDYTIME_DAT ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '9' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "        AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "    ) ";
        //学年評定の表
        $query .= ", T_RECORD AS(";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO, ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "        SCORE as GRAD_VALUE, ";
        } else {
            $query .= "        VALUE as GRAD_VALUE, ";
        }
        $query .= "        GET_CREDIT, ";
        $query .= "        COMP_CREDIT ";
        $query .= "    FROM ";
        $query .= "        RECORD_SCORE_DAT ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND SEMESTER = '9' ";
        $query .= "        AND TESTKINDCD = '99' ";
        $query .= "        AND TESTITEMCD = '00' ";
        //パーツタイプ対応
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "        AND SCORE_DIV  = '09'";
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
        $query .= "       ,R1.TOTALSTUDYACT ";
        $query .= "       ,R1.TOTALSTUDYTIME ";
        $query .= "       ,R2.GRAD_VALUE ";
        $query .= "       ,R2.GET_CREDIT ";
        $query .= "       ,R2.COMP_CREDIT ";
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
        $query .= "       FROM schreg_regd_hdat T0, schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "      WHERE T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "        AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".CTRL_SEMESTER."' ";
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W1.schregno = W2.schregno ";

        /* 備考情報 */
        $query .= " LEFT OUTER JOIN T_REMARK R1 ON R1.schregno = W1.schregno ";

        /* 成績情報 */
        $query .= " LEFT OUTER JOIN T_RECORD R2 ON R2.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";

        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //教育過程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->field["SUBCLASSCD"]);
            } else {
                $subclasscd  = $model->field["SUBCLASSCD"];
            }
            if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] != '1') {
                /*** RECORD_TOTALSTUDYTIME_DAT ***/
                $year = CTRL_YEAR;
                $semcd = '9';
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
                $data["TOTALSTUDYACT"][TEXT]      = $model->fields["TOTALSTUDYACT"][$i];
                $data["TOTALSTUDYTIME"][TEXT]     = $model->fields["TOTALSTUDYTIME"][$i];
                $data["REGISTERCD"][TEXT]         = STAFFCD ;
                $data["UPDATED"][FUNC]            = "sysdate()";
                //存在するか？
                $query  = "SELECT COUNT(*) FROM RECORD_TOTALSTUDYTIME_DAT";
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
                $query .= "     AND T1.SEMESTER     = '".CTRL_SEMESTER."' ";
                $query .= "     AND T2.YEAR         = '{$year}' ";
                $query .= "     AND T2.COURSECD     = T1.COURSECD ";
                $query .= "     AND T2.MAJORCD      = T1.MAJORCD ";
                $query .= "     AND T2.GRADE        = T1.GRADE ";
                $query .= "     AND T2.COURSECODE   = T1.COURSECODE ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     AND T2.CLASSCD       = '{$clscd}'";
                    $query .= "     AND T2.SCHOOL_KIND   = '{$sch_kind}'";
                    $query .= "     AND T2.CURRICULUM_CD = '{$cur_cd}'";
                }
                $query .= "     AND T2.SUBCLASSCD   = '{$subcd}' ";
                $data["COMP_CREDIT"][NUMBER]      = $db->getOne($query);
                $data["GET_CREDIT"][NUMBER]       = $db->getOne($query);
            } else {
                //チェックなしの場合、画面の入力値をセット
                $data["COMP_CREDIT"][NUMBER]      = $model->fields["COMP_CREDIT"][$i];
                $data["GET_CREDIT"][NUMBER]       = $model->fields["GET_CREDIT"][$i];
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

    //講座名取得
    public function getChairName($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SUBCLASSNAME || '　　' || T1.CHAIRNAME AS CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.CLASSCD         = T2.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            $query .= "                              AND T1.SUBCLASSCD      = T2.SUBCLASSCD ";
        } else {
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.CHAIRCD  = '".$model->chaircd."' ";

        return $query;
    }

    //置換画面用の生徒一覧取得
    public function getStudent($model, $execute_date, $flg = "")
    {
        $selectdata = str_replace(",", "','", $model->replace_data["selectdata"]);

        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE, ";
        $query .= "         T3.HR_NAME || ' ' || T1.ATTENDNO || '番' AS ATTENDNO, ";
        $query .= "         T2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         LEFT OUTER JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR AND ";
        $query .= "                 T1.SEMESTER = T3.SEMESTER AND T1.GRADE = T3.GRADE AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ), CHR_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "         CHAIRCD     = '".$model->chaircd."' AND ";
        $query .= "         '".$execute_date."' BETWEEN APPDATE AND APPENDDATE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.VALUE, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     CHR_STD T1, ";
        $query .= "     SCHINFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        if ($flg == "1") {
            $query .= "     T1.SCHREGNO IN ('".$selectdata."') ";
        } else {
            $query .= "     T1.SCHREGNO NOT IN ('".$selectdata."') ";
        }
        $query .= " ORDER BY";
        $query .= "     T2.VALUE ";

        return $query;
    }

    //一括更新
    public function replaceUpdate($model)
    {
        $db = Query::dbCheckOut();
        $model->replace_data["selectdata"] = explode(",", $model->replace_data["selectdata"]);

        for ($i = 0; $i < get_count($model->replace_data["selectdata"]); $i++) {
            $year     = CTRL_YEAR;
            $semester = '9';
            $schregno = $model->replace_data["selectdata"][$i];
            $kindcd = '99';
            $itemcd = '00';
            //パーツタイプ対応
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $scoreDiv = '09';
            } else {
                $scoreDiv = '00';
            }
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->subclasscd);
            } else {
                $subclasscd  = $model->subclasscd;
            }

            if (($model->replace_data["check"][0] == "1") || ($model->replace_data["check"][1] == "1")) {
                /*** RECORD_TOTALSTUDYTIME_DAT ***/
                //項目
                $data = array();
                $data["YEAR"][TEXT]             = $year;
                $data["SEMESTER"][TEXT]         = $semester;
                //教育過程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]         = $classcd;
                    $data["SCHOOL_KIND"][TEXT]     = $school_kind;
                    $data["CURRICULUM_CD"][TEXT]   = $curriculum_cd;
                }
                $data["SUBCLASSCD"][TEXT]      = $subclasscd;
                $data["SCHREGNO"][TEXT]         = $schregno;
                if ($model->replace_data["check"][0] == "1") {
                    $data["TOTALSTUDYACT"][TEXT]    = $model->replace_data["field"]["TOTALSTUDYACT"];
                }
                if ($model->replace_data["check"][1] == "1") {
                    $data["TOTALSTUDYTIME"][TEXT]   = $model->replace_data["field"]["TOTALSTUDYTIME"];
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";

                //存在チェック
                $query  = " SELECT COUNT(*) FROM RECORD_TOTALSTUDYTIME_DAT ";
                $query .= " WHERE YEAR     = '".$year."' AND ";
                $query .= "       SEMESTER = '".$semester."' AND ";
                //教育過程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "       CLASSCD       = '".$classcd."' AND ";
                    $query .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
                    $query .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
                }
                $query .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
                $query .= "       SCHREGNO = '".$schregno."' ";
                if (0 < $db->getOne($query)) {
                    //更新
                    $where  = " WHERE YEAR     = '".$year."' AND ";
                    $where .= "       SEMESTER = '".$semester."' AND ";
                    //教育過程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "       CLASSCD       = '".$classcd."' AND ";
                        $where .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
                        $where .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
                    }
                    $where .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
                    $where .= "       SCHREGNO = '".$schregno."'";
                    $query = Query::updateSQL($data, "RECORD_TOTALSTUDYTIME_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "RECORD_TOTALSTUDYTIME_DAT");
                }
                $db->query($query);
            }

            if (($model->replace_data["check"][2] == "1") || ($model->replace_data["check"][3] == "1") || ($model->replace_data["check"][4] == "1")) {
                /*** RECORD_SCORE_DAT ***/
                //項目
                $data = array();
                $data["YEAR"][TEXT]             = $year;
                $data["SEMESTER"][TEXT]         = $semester;
                $data["TESTKINDCD"][TEXT]       = $kindcd;
                $data["TESTITEMCD"][TEXT]       = $itemcd;
                $data["SCORE_DIV"][TEXT]        = $scoreDiv;
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = $classcd;
                    $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                    $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                }
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["SCHREGNO"][TEXT]         = $schregno;
                //パーツタイプ対応
                if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    if ($model->replace_data["check"][2] == "1") {
                        $data["SCORE"][NUMBER]          = $model->replace_data["field"]["GRAD_VALUE"];
                    }
                } else {
                    if ($model->replace_data["check"][2] == "1") {
                        $data["VALUE"][NUMBER]          = $model->replace_data["field"]["GRAD_VALUE"];
                    }
                }
                if ($model->replace_data["check"][3] == "1") {
                    $data["COMP_CREDIT"][NUMBER]    = $model->replace_data["field"]["COMP_CREDIT"];
                }
                if ($model->replace_data["check"][4] == "1") {
                    $data["GET_CREDIT"][NUMBER]     = $model->replace_data["field"]["GET_CREDIT"];
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";

                //存在チェック
                $query  = "SELECT COUNT(*) FROM RECORD_SCORE_DAT";
                $query .= " WHERE YEAR          = '".$year."' AND ";
                $query .= "       SEMESTER      = '".$semester."' AND ";
                $query .= "       TESTKINDCD    = '".$kindcd."' AND ";
                $query .= "       TESTITEMCD    = '".$itemcd."' AND ";
                $query .= "       SCORE_DIV     = '".$scoreDiv."' AND ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "       CLASSCD       = '".$classcd."' AND ";
                    $query .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
                    $query .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
                }
                $query .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
                $query .= "       SCHREGNO      = '".$schregno."' ";
                if (0 < $db->getOne($query)) {
                    //更新
                    $where  = " WHERE YEAR          = '".$year."' AND ";
                    $where .= "       SEMESTER      = '".$semester."' AND ";
                    $where .= "       TESTKINDCD    = '".$kindcd."' AND ";
                    $where .= "       TESTITEMCD    = '".$itemcd."' AND ";
                    $where .= "       SCORE_DIV     = '".$scoreDiv."' AND ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "       CLASSCD       = '".$classcd."' AND ";
                        $where .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
                        $where .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
                    }
                    $where .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
                    $where .= "       SCHREGNO      = '".$schregno."' ";
                    $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
                }
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /*===================================定型文選択===================================*/

    //学年取得
    public function getGrade($model)
    {
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
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "     T3.CHAIRCD  = '".$model->chaircd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //定型文マスタ一覧取得
    public function getHtrainRemarkTempDatIkkatsu($grade, $dataDiv)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE YEAR        = '".CTRL_YEAR."' ";
        $query .= "   AND GRADE       = '{$grade}' ";
        if ($dataDiv) {
            $query .= "   AND DATA_DIV    = '{$dataDiv}' ";
        } else {
            $query .= "   AND DATA_DIV    = '03' ";
        }
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }
}
