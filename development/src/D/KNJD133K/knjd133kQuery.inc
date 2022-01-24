<?php

require_once('for_php7.php');


class knjd133kquery extends Query
{

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
        $query .= "     T3.SUBCLASSCD LIKE '90%' AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     T4.STAFFCD = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD      = T1.CLASSCD AND ";
            $query .= "     T3.SCHOOL_KIND  = T1.SCHOOL_KIND AND ";
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
        //以下追加
        if (CTRL_YEAR === '2012' || CTRL_YEAR === '2013') {
            $query .= "     NOT EXISTS ( ";
            $query .= "                 SELECT DISTINCT ";
            $query .= "                     TT1.CHAIRCD, ";
            $query .= "                     TT1.CHAIRNAME ";
            $query .= "                 FROM ";
            $query .= "                     CHAIR_DAT TT1, ";
            $query .= "                     CHAIR_STD_DAT TT2  ";
            $query .= "                     INNER JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = TT2.SCHREGNO  ";
            $query .= "                                                  AND L1.YEAR = TT2.YEAR  ";
            $query .= "                                                  AND L1.SEMESTER = T2.SEMESTER ";
            if (CTRL_YEAR === '2012') {
                $query .= "                                              AND L1.GRADE IN ('02', '03'), ";
            } else {
                $query .= "                                              AND L1.GRADE = '03', ";
            }
            $query .= "                     CHAIR_STF_DAT TT3 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.CHAIRCD    = TT1.CHAIRCD AND ";
            $query .= "                     T1.CHAIRNAME    = TT1.CHAIRNAME AND ";
            $query .= "                     TT1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                     TT1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                     TT2.YEAR = TT1.YEAR AND ";
            $query .= "                     TT2.SEMESTER = TT1.SEMESTER AND ";
            $query .= "                     TT2.CHAIRCD = TT1.CHAIRCD AND ";
            $query .= "                     TT1.YEAR = TT3.YEAR AND ";
            $query .= "                     TT1.SEMESTER = TT3.SEMESTER AND ";
            $query .= "                     TT1.CHAIRCD = TT3.CHAIRCD AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     TT1.CLASSCD || '-' || TT1.SCHOOL_KIND || '-' || TT1.CURRICULUM_CD || '-' || TT1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "                     TT1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "                 )  AND ";
        }
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
        $query .= "        GRADE_RECORD ";
        $query .= "    FROM ";
        $query .= "        KIN_RECORD_DAT ";
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' ";
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
        $query .= "       ,R2.GRADE_RECORD ";
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

            /*** KIN_RECORD_DAT ***/
            $year   = CTRL_YEAR;
            $schno  = $model->data["SCHREGNO"][$i];
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($clscd, $sch_kind, $cur_cd, $subcd)  = preg_split("/-/", $model->field["SUBCLASSCD"]);
            } else {
                $subcd  = $model->field["SUBCLASSCD"];
            }
            //項目
            $data = array();
            $data["YEAR"][TEXT]               = $year;
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]            = $clscd;
                $data["SCHOOL_KIND"][TEXT]        = $sch_kind;
                $data["CURRICULUM_CD"][TEXT]      = $cur_cd;
            }
            $data["SUBCLASSCD"][TEXT]         = $subcd;
            $data["SCHREGNO"][TEXT]           = $schno;
            $data["CHAIRCD"][TEXT]            = $model->field["CHAIRCD"];
            if ($model->fields["CHK_CALC_CREDIT"][$i] == "on") {
                $data["GRADE_RECORD"][NUMBER]      = 11;
            } else {
                //チェックなしの場合、NULLをセット
                $data["GRADE_RECORD"][NUMBER]      = "";
            }
            $data["REGISTERCD"][TEXT]         = STAFFCD ;
            $data["UPDATED"][FUNC]            = "sysdate()";
            //存在するか？
            $query  = "SELECT COUNT(*) FROM KIN_RECORD_DAT";
            $query .= " WHERE YEAR          = '{$year}'";
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
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "   AND CLASSCD       = '{$clscd}'";
                    $where .= "   AND SCHOOL_KIND   = '{$sch_kind}'";
                    $where .= "   AND CURRICULUM_CD = '{$cur_cd}'";
                }
                $where .= "   AND SUBCLASSCD    = '{$subcd}'";
                $where .= "   AND SCHREGNO      = '{$schno}'";
                $query = Query::updateSQL($data, "KIN_RECORD_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "KIN_RECORD_DAT");
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }
}
