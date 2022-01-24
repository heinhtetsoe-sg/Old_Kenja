<?php

require_once('for_php7.php');

class knjd128yquery extends Query
{

    //各種設定を取得・・・洛南要望
    public function getNameMstSettei($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //学校名取得
    public function getNameMstZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtn = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtn;
    }

    //課程学科コンボ取得
    public function getCourseMajor($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV TEST_GCM ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST VCM ON TEST_GCM.YEAR = VCM.YEAR ";
        $query .= "           AND TEST_GCM.COURSECD = VCM.COURSECD ";
        $query .= "           AND TEST_GCM.MAJORCD = VCM.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     TEST_GCM.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TEST_GCM.SCHOOLCD = '".SCHOOLCD."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND TEST_GCM.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND TEST_GCM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //追指導入力設定があるか
    public function getSidouInputCount($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SIDOU_INPUT = '1' "; //1:追指導入力
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        return $query;
    }

    //出欠入力プログラム取得
    public function getNameMstD068()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D068' AND ";
        $query .= "     NAMECD2 = '01' ";

        return $query;
    }

    //「ＣＳＶは、非表示とする？」のフラグを取得
    public function getNameMstD058()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 "; // 1:非表示
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D058' AND ";
        $query .= "     NAMECD2 = '01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }
    //科目名の取得
    public function getSubclassName($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        return $query;
    }
    //講座名の取得
    public function getChairName($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        return $query;
    }

    //「評価にリンクを付けるか？」のフラグを取得
    public function getNameMstD048()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D048' AND ";
        $query .= "     NAMECD2 = '01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //D065登録科目か
    public function getD065Sub($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'D065' ";
        $query .= "     AND NAME1 = '".$model->field["SUBCLASSCD"]."' ";

        return $query;
    }

    //D065登録科目の入力値チェック
    public function getD001List()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS MARK ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'D001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    public function getCalcMethod()
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_REMARK1  ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_SEQ = '007' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //科目別設定があるか
    public function getTestSubCnt($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }

        return $query;
    }

    //CSV取込不可チェック
    public function getNotusecsvCnt($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L1 ON L1.YEAR = T1.YEAR ";
        } else {
            $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON L1.YEAR = T1.YEAR ";
        }
        $query .= "                                                AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                                                AND L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                                AND L1.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                                AND L1.SCORE_DIV = T1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "                                            AND L1.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "                                            AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L1.GRADE = T1.GRADE ";
            $query .= "                                            AND L1.COURSECD || L1.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.CONTROL_FLG = '1' ";
        $query .= " AND L1.NOT_USE_CSV_FLG = '1' ";
        $query .= " AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }

        return $query;
    }

    //名称マスタA023に登録されているの学校校種を取得
    public function getSchoolkindQuery()
    {
        $query  = " SELECT ";
        $query .= "     MIN(NAME1) AS SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";

        return $query;
    }

    //管理者コントロールの基本のSCHOOL_KINDについて、混在は、MAXを取得する。
    //例
    //「00-00-00-000000」・・・これはゴミ
    //「00-H-00-000000」・・・これを参照する
    public function getAdminSchoolKind($model, $schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SCHOOL_KIND) ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     ADMIN_CONTROL_GCM_SDIV_DAT ";
        } else {
            $query .= "     ADMIN_CONTROL_SDIV_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD         = '00' ";
        $query .= "     AND SCHOOL_KIND     IN ('00','{$schoolKind}') "; //宮城県は'00'がある(古いデータ)
        $query .= "     AND CURRICULUM_CD   = '00' ";
        $query .= "     AND SUBCLASSCD      = '000000' ";
        return $query;
    }

    //テスト名取得
    public function getTestName($model, $testSubCnt)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE, ";
        $query .= "     T3.TESTITEMABBV1 AS TESTITEMNAME, ";
        $query .= "     T3.SIDOU_INPUT, ";
        $query .= "     T3.SIDOU_INPUT_INF, ";
        if ($model->Properties["use_school_detail_gcm_dat"] != '1') {
            if ($model->Properties["useMikomiFlg"] == '1') {
                $query .= "     T3.MIKOMI_FLG, ";
            }
            if ($model->Properties["useSankouFlg"] == '1') {
                $query .= "     T3.SANKOU_FLG, ";
            }
            if ($model->Properties["useRemarkFlg"] == '1') {
                $query .= "     T3.REMARK_FLG, ";
            }
        }
        $query .= "     T1.CONTROL_FLG ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
        }
        $query .= "     LEFT JOIN SEMESTER_MST T2 ";
        $query .= "         ON  T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T3 ";
        } else {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T3 ";
        }
        $query .= "         ON  T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    = T1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T3.SCHOOLCD     = T1.SCHOOLCD ";
            $query .= "     AND T3.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "     AND T3.GRADE        = T1.GRADE ";
            $query .= "     AND T3.COURSECD || T3.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        //科目別設定がある場合は、科目別設定。それ以外は基本設定されたパーツのみ表示する。
        $subclasscd = (0 < $testSubCnt) ? $model->field["SUBCLASSCD"] : $model->subclassAll;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
            $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND T1.SUBCLASSCD     = '".$subclasscd."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "          AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "          AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //算出元科目別設定があるか
    public function getTestMotoSubCnt($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_CALC_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " ADMIN_CONTROL_CALC_SDIV_DAT T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }

        return $query;
    }

    //算出元テスト取得
    public function getTestMoto($model, $testSubCnt)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T2.TESTITEMNAME AS TESTNAME, ";
        $query .= "     T1.MOTO_SEMESTER || T1.MOTO_TESTKINDCD || T1.MOTO_TESTITEMCD || T1.MOTO_SCORE_DIV AS MOTO_TESTCD, ";
        $query .= "     T3.TESTITEMNAME AS MOTO_TESTNAME, ";
        $query .= "     T1.SEMESTER ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_CALC_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " ADMIN_CONTROL_CALC_SDIV_DAT T1 ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T2 ";
        } else {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        }
        $query .= "         ON  T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T2.SCORE_DIV    = T1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T2.SCHOOLCD     = T1.SCHOOLCD ";
            $query .= "     AND T2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "     AND T2.GRADE        = T1.GRADE ";
            $query .= "     AND T2.COURSECD || T2.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T3 ";
        } else {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T3 ";
        }
        $query .= "         ON  T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.MOTO_SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.MOTO_TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.MOTO_TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    = T1.MOTO_SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T3.SCHOOLCD     = T1.SCHOOLCD ";
            $query .= "     AND T3.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "     AND T3.GRADE        = T1.GRADE ";
            $query .= "     AND T3.COURSECD || T3.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        //科目別設定がある場合は、科目別設定。それ以外は基本設定されたパーツのみ表示する。
        $subclasscd = (0 < $testSubCnt) ? $model->field["SUBCLASSCD"] : $model->subclassAll;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
            $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND T1.SUBCLASSCD     = '".$subclasscd."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "          AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "          AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTCD, ";
        $query .= "     MOTO_TESTCD ";

        return $query;
    }

    //科目データの取得
    public function getSubclassMst($model, $cmCnt)
    {
        $query  = "";
        $query .= " WITH REPLACE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1' AS DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         COMBINED_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     CASE WHEN L1.COMBINED_SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     CASE WHEN L1.COMBINED_SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "          ON T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T4 ";
        $query .= "          ON T4.YEAR       = T1.YEAR ";
        $query .= "         AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T4.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST T3 ON ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "         T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN REPLACE L1 ON L1.DIV = '1' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD = L1.COMBINED_CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = L1.COMBINED_SCHOOL_KIND ";
            $query .= "     AND T3.CURRICULUM_CD = L1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "         AND T3.SUBCLASSCD = L1.COMBINED_SUBCLASSCD ";
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1' && 1 < $cmCnt) {
            $query .= "     INNER JOIN SCHREG_REGD_DAT W2 ON W2.SCHREGNO = T2.SCHREGNO ";
            $query .= "       AND W2.YEAR = T2.YEAR ";
            $query .= "       AND W2.SEMESTER = T2.SEMESTER ";
            $query .= "       AND W2.COURSECD || '-' || W2.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT W3 ON W3.YEAR = W2.YEAR ";
            $query .= "       AND W3.GRADE = W2.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "   AND W3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "   AND W3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        //制限付で使用
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T2.SCHREGNO AND T5.YEAR = T2.YEAR AND T5.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        //1:総学非表示（９０未満を表示とする）
        if ($model->noUseSouGaku == '1') {
            $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' ) ";
        } else {
            $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";
        }
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND ((T4.STAFFCD = '".STAFFCD."') ";
            $query .= "         OR (T5.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         OR (T1.CLASSCD IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '1050')) ";
            $query .= "         ) ";
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     L1.COMBINED_SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model, $cmCnt)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
    
        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || '：' || T1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "          ON T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T4 ";
        $query .= "          ON T4.YEAR       = T1.YEAR ";
        $query .= "         AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T4.CHAIRCD    = T1.CHAIRCD ";
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1' && 1 < $cmCnt) {
            $query .= "     INNER JOIN SCHREG_REGD_DAT W2 ON W2.SCHREGNO = T2.SCHREGNO ";
            $query .= "       AND W2.YEAR = T2.YEAR ";
            $query .= "       AND W2.SEMESTER = T2.SEMESTER ";
            $query .= "       AND W2.COURSECD || '-' || W2.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT W3 ON W3.YEAR = W2.YEAR ";
            $query .= "       AND W3.GRADE = W2.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "   AND W3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "   AND W3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        //制限付で使用
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T2.SCHREGNO AND T5.YEAR = T2.YEAR AND T5.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND ((T4.STAFFCD = '".STAFFCD."') ";
            $query .= "         OR (T5.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         OR (T1.CLASSCD IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '1050')) ";
            $query .= "         ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //V学校マスタ
    public function getVSchoolMst()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //累積最大日取得
    public function getMax($model, $isSubclassSaki)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "SELECT DISTINCT T1.year ";
        $query .= "              ,T1.month ";
        $query .= "              ,CASE T1.MONTH WHEN '01' THEN CHAR(SMALLINT(T1.month) + 12)  ";
        $query .= "                             WHEN '02' THEN CHAR(SMALLINT(T1.month) + 12)  ";
        $query .= "                             WHEN '03' THEN CHAR(SMALLINT(T1.month) + 12) ELSE T1.month END AS tmp_month ";
        $query .= "              ,T1.semester ";
        $query .= "              ,T1.appointed_day ";
        $query .= "         FROM attend_subclass_dat T1 ";
        //合併先は合併元の合算値
        if ($isSubclassSaki) {
            $query .= "           INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT S1 ";
            $query .= "                 ON  S1.YEAR = T1.YEAR ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             AND S1.COMBINED_CLASSCD         = '".$subclass_array[0]."' ";
                $query .= "             AND S1.COMBINED_SCHOOL_KIND     = '".$subclass_array[1]."' ";
                $query .= "             AND S1.COMBINED_CURRICULUM_CD   = '".$subclass_array[2]."' ";
                $query .= "             AND S1.COMBINED_SUBCLASSCD      = '".$subclass_array[3]."' ";
                $query .= "             AND S1.ATTEND_CLASSCD           = T1.CLASSCD ";
                $query .= "             AND S1.ATTEND_SCHOOL_KIND       = T1.SCHOOL_KIND ";
                $query .= "             AND S1.ATTEND_CURRICULUM_CD     = T1.CURRICULUM_CD ";
                $query .= "             AND S1.ATTEND_SUBCLASSCD        = T1.SUBCLASSCD ";
            } else {
                $query .= "             AND S1.COMBINED_SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' ";
                $query .= "             AND S1.ATTEND_SUBCLASSCD        = T1.SUBCLASSCD ";
            }
        }
        $query .= "        WHERE T1.COPYCD     = '0' ";
        $query .= "          AND T1.year       = '".CTRL_YEAR."' ";
        //合併先以外
        if (!$isSubclassSaki) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND T1.CLASSCD        = '".$subclass_array[0]."' ";
                $query .= "   AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
                $query .= "   AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
                $query .= "   AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
            } else {
                $query .= "   AND T1.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
            }
        }
        $query .= "ORDER BY T1.semester, tmp_month ";
        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date, $cmCnt)
    {
        $query  = "";
        $query .= " SELECT W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "   FROM ";
        /* 生徒一覧 */
        $query .= "    (SELECT schregno ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd = '".$model->field["CHAIRCD"]."'  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
        $query .= "    ) W1 ";
        /* 基礎情報 */
        $query .= " INNER JOIN ";
        $query .= "    (SELECT T1.year,T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.inoutcd,T2.name_show,T2.schregno, T2.grd_date, T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE ";
        $query .= "       FROM schreg_regd_hdat T0, schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "      WHERE T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "        AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".CTRL_SEMESTER."' ";
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1' && 1 < $cmCnt) {
            $query .= "        AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W2.schregno = W1.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    //成績データ
    public function getScore($model, $execute_date, $cmCnt)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd128yQuery::selectQuery($model, $execute_date, $cmCnt).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.COMP_CREDIT, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.VALUE_DI "; //欠試'*'
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
//        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";
        return $query;
    }

    //見込点データ
    public function getRecordInfo($model, $execute_date, $cmCnt)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd128yQuery::selectQuery($model, $execute_date, $cmCnt).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SCORE AS MIKOMI ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
//        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";
        $query .= "     AND T1.SCORE_FLG = '2' "; //2:見込点入力
        return $query;
    }

    //追指導データ
    public function getRecordSlump($year, $testcd, $schregno, $grade, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        //相対評定マスタ
        $query .= "WITH RELA_CNT AS ( ";
        $query .= "    SELECT ";
        $query .= "        'CNT' AS KEY, ";
        $query .= "        COUNT(*) AS CNT ";
        $query .= "    FROM ";
        $query .= "        RELATIVEASSESS_MST RELA ";
        $query .= "    WHERE ";
        $query .= "        RELA.ASSESSCD = '3' ";
        $query .= "        AND RELA.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND RELA.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "    AND RELA.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "    AND RELA.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "    AND RELA.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "    AND RELA.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "    ) ";
        //メイン
        $query .= "SELECT ";
        $query .= "    CASE WHEN 0 < RELA_CNT.CNT THEN RELA.ASSESSLEVEL ELSE ASSESS.ASSESSLEVEL END AS ASSESSLEVEL, ";
        $query .= "    CASE WHEN 0 < RELA_CNT.CNT THEN RELA2.ASSESSLOW ELSE ASSESS2.ASSESSLOW END AS ASSESSLOW2, ";
        $query .= "    SLUMP.SCORE, ";
        $query .= "    SLUMP.MARK ";
        $query .= "FROM   RECORD_SLUMP_SDIV_DAT SLUMP ";
        $query .= "       LEFT JOIN ASSESS_MST ASSESS ";
        $query .= "             ON  ASSESS.ASSESSCD = '3' ";
        $query .= "             AND SLUMP.SCORE BETWEEN ASSESS.ASSESSLOW AND ASSESS.ASSESSHIGH ";
        $query .= "       LEFT JOIN RELA_CNT RELA_CNT ";
        $query .= "             ON  RELA_CNT.KEY = 'CNT' ";
        $query .= "       LEFT JOIN RELATIVEASSESS_MST RELA ";
        $query .= "             ON  RELA.ASSESSCD = '3' ";
        $query .= "             AND SLUMP.SCORE BETWEEN RELA.ASSESSLOW AND RELA.ASSESSHIGH ";
        $query .= "             AND RELA.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND RELA.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "         AND RELA.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "         AND RELA.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "         AND RELA.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND RELA.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "       LEFT JOIN ASSESS_MST ASSESS2 ";
        $query .= "             ON  ASSESS2.ASSESSCD = '3' ";
        $query .= "             AND ASSESS2.ASSESSLEVEL = 2 ";
        $query .= "       LEFT JOIN RELATIVEASSESS_MST RELA2 ";
        $query .= "             ON  RELA2.ASSESSCD = '3' ";
        $query .= "             AND RELA2.ASSESSLEVEL = 2 ";
        $query .= "             AND RELA2.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND RELA2.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "         AND RELA2.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "         AND RELA2.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "         AND RELA2.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND RELA2.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "WHERE  SLUMP.YEAR          = '{$year}' ";
        $query .= "  AND  SLUMP.SEMESTER || SLUMP.TESTKINDCD || SLUMP.TESTITEMCD || SLUMP.SCORE_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND SLUMP.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND SLUMP.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND SLUMP.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND SLUMP.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND SLUMP.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "  AND  SLUMP.SCHREGNO      = '{$schregno}' ";
        return $query;
    }

    //考査満点マスタ
    public function getPerfect($year, $subclasscd, $testcd, $grade, $course, $model, $fieldDiv = "")
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
        if ($fieldDiv == "PASS") {
            $query .= "    PASS_SCORE ";
        } else {
            $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        }
        $query .= "FROM ";
        if ($model->Properties["usePerfectRecordSdivDat"] == '1') {
            $query .= "    PERFECT_RECORD_SDIV_DAT ";
        } else {
            $query .= "    PERFECT_RECORD_DAT ";
        }
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
        if ($model->Properties["usePerfectRecordSdivDat"] == '1') {
            $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' AND ";
        } else {
            $testcd2 = substr($testcd, 0, 5);
            $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd2}' AND ";
        }
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$course}' END ";
        return $query;
    }

    //欠課数上限値
    public function getKeekaOver($model, $schno, $year, $grade, $course, $schoolRow)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }

        $query  = "";
        //法定時数(1)
        if ($schoolRow["JUGYOU_JISU_FLG"] == "1") {
            $query .= " SELECT ";
            $query .= "     ABSENCE_HIGH AS COMP_ABSENCE_HIGH ";
            $query .= " FROM ";
            $query .= "     CREDIT_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= "     AND GRADE = '{$grade}' ";
            $query .= "     AND COURSECD || MAJORCD || COURSECODE = '{$course}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
                $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
                $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
                $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
            } else {
                $query .= "     AND SUBCLASSCD    = '{$model->field["SUBCLASSCD"]}' ";
            }

            //実時数(2)
        } else {
            $query .= " SELECT ";
            $query .= "     COMP_ABSENCE_HIGH ";
            $query .= " FROM ";
            $query .= "     SCHREG_ABSENCE_HIGH_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= "     AND DIV = '1' "; //年間
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
                $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
                $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
                $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
            } else {
                $query .= "     AND SUBCLASSCD    = '{$model->field["SUBCLASSCD"]}' ";
            }
            $query .= "     AND SCHREGNO = '{$schno}' ";
        }
        return $query;
    }

    //合併先科目か
    public function getCntSubclassSaki($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= " AND COMBINED_SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        return $query;
    }

    //(年間ごとに集計）する場合
    public function getAttendData($schno, $absent_cov, $absent_cov_late, $schoolRow, $model, $isSubclassSaki, $attendSM)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_ABSENT"]   == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"] == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query  = "SELECT T1.schregno ";
        $query .= "      ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0))";
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        $query .= "       AS LESSON "; //授業時数
        $query .= "      ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0)) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0))";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " - SUM(VALUE(VIRUS,0))";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " - SUM(VALUE(KOUDOME,0))";
        }
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $query .= " + SUM(VALUE(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $query .= " + SUM(VALUE(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $query .= " + SUM(VALUE(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $query .= " + SUM(VALUE(KOUDOME,0))";
        }
        $query .= "       AS MLESSON "; //出席すべき時数
        $query .= "       ,".$notice."  AS T_NOTICE ";
        //小数点ありAdd
        if ($absent_cov == "4" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "5" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$schoolRow["AMARI_KURIAGE"]} ";
            $query .= "         THEN 0 ";
            $query .= "         ELSE MOD({$late_early},{$absent_cov_late}) ";
            $query .= "    END AS T_LATEEARLY ";
            $query .= "   ,(({$late_early}) / {$absent_cov_late}) + ({$notice}) + (CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$schoolRow["AMARI_KURIAGE"]} ";
            $query .= "                                                  THEN 1 ";
            $query .= "                                                  ELSE 0 ";
            $query .= "                                             END) AS NOTICE_LATE ";
        } elseif ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "   ,".$late_early."     AS T_LATEEARLY ";
            $query .= "   ,".$notice."  AS NOTICE_LATE ";
        }
        $query .= "       ,".$late_early."     AS LATE_EARLY ";
        $query .= "  FROM attend_subclass_dat T1 ";
        //合併先は合併元の合算値
        if ($isSubclassSaki) {
            $query .= "           INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT S1 ";
            $query .= "                 ON  S1.YEAR = T1.YEAR ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             AND S1.COMBINED_CLASSCD         = '".$subclass_array[0]."' ";
                $query .= "             AND S1.COMBINED_SCHOOL_KIND     = '".$subclass_array[1]."' ";
                $query .= "             AND S1.COMBINED_CURRICULUM_CD   = '".$subclass_array[2]."' ";
                $query .= "             AND S1.COMBINED_SUBCLASSCD      = '".$subclass_array[3]."' ";
                $query .= "             AND S1.ATTEND_CLASSCD           = T1.CLASSCD ";
                $query .= "             AND S1.ATTEND_SCHOOL_KIND       = T1.SCHOOL_KIND ";
                $query .= "             AND S1.ATTEND_CURRICULUM_CD     = T1.CURRICULUM_CD ";
                $query .= "             AND S1.ATTEND_SUBCLASSCD        = T1.SUBCLASSCD ";
            } else {
                $query .= "             AND S1.COMBINED_SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' ";
                $query .= "             AND S1.ATTEND_SUBCLASSCD        = T1.SUBCLASSCD ";
            }
        }
        $query .= " WHERE T1.COPYCD     = '0' ";
        $query .= "   AND T1.year       = '".CTRL_YEAR."' ";
        //合併先以外
        if (!$isSubclassSaki) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND T1.CLASSCD        = '".$subclass_array[0]."' ";
                $query .= "   AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
                $query .= "   AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
                $query .= "   AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
            } else {
                $query .= "   AND T1.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
            }
        }
        $query .= "   AND T1.schregno  = '{$schno}' ";
        $query .= "   AND T1.MONTH || '-' || T1.SEMESTER = '".$model->field["MONTH_SEMESTER"]."' ";
//        $query .= "   AND T1.SEMESTER || T1.MONTH IN ('" . implode("','", $attendSM) . "') ";
        $query .= " GROUP BY T1.schregno";

        return $query;
    }
    //(学期ごとに集計）する場合
    public function getAttendData2($schno, $absent_cov, $absent_cov_late, $schoolRow, $model, $isSubclassSaki, $attendSM)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_ABSENT"]   == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"] == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(LESSON)      AS LESSON";
        $query .= "       ,SUM(MLESSON)     AS MLESSON";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "       ,SUM(LATE_EARLY)  AS LATE_EARLY";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno ";
        $query .= "               ,T1.semester ";

        $query .= "               ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0))";
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        $query .= "                AS LESSON "; //授業時数

        $query .= "               ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0)) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0))";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " - SUM(VALUE(VIRUS,0))";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " - SUM(VALUE(KOUDOME,0))";
        }
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $query .= " + SUM(VALUE(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $query .= " + SUM(VALUE(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $query .= " + SUM(VALUE(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $query .= " + SUM(VALUE(KOUDOME,0))";
        }
        $query .= "                AS MLESSON "; //出席すべき時数

        $query .= "               ,".$notice." AS T_NOTICE ";
        //小数点ありAdd
        if ($absent_cov == "3" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "1" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "           ,".$late_early."    AS T_LATEEARLY ";
            $query .= "           ,".$notice." AS NOTICE_LATE ";
        }
        $query .= "               ,".$late_early."    AS LATE_EARLY ";
        $query .= "          FROM attend_subclass_dat T1   ";
        //合併先は合併元の合算値
        if ($isSubclassSaki) {
            $query .= "           INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT S1 ";
            $query .= "                 ON  S1.YEAR = T1.YEAR ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             AND S1.COMBINED_CLASSCD         = '".$subclass_array[0]."' ";
                $query .= "             AND S1.COMBINED_SCHOOL_KIND     = '".$subclass_array[1]."' ";
                $query .= "             AND S1.COMBINED_CURRICULUM_CD   = '".$subclass_array[2]."' ";
                $query .= "             AND S1.COMBINED_SUBCLASSCD      = '".$subclass_array[3]."' ";
                $query .= "             AND S1.ATTEND_CLASSCD           = T1.CLASSCD ";
                $query .= "             AND S1.ATTEND_SCHOOL_KIND       = T1.SCHOOL_KIND ";
                $query .= "             AND S1.ATTEND_CURRICULUM_CD     = T1.CURRICULUM_CD ";
                $query .= "             AND S1.ATTEND_SUBCLASSCD        = T1.SUBCLASSCD ";
            } else {
                $query .= "             AND S1.COMBINED_SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' ";
                $query .= "             AND S1.ATTEND_SUBCLASSCD        = T1.SUBCLASSCD ";
            }
        }
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.year       = '".CTRL_YEAR."' ";
        //合併先以外
        if (!$isSubclassSaki) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND T1.CLASSCD        = '".$subclass_array[0]."' ";
                $query .= "   AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
                $query .= "   AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
                $query .= "   AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
            } else {
                $query .= "   AND T1.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
            }
        }
        $query .= "           AND T1.schregno   = '{$schno}' ";
        $query .= "           AND T1.SEMESTER || T1.MONTH IN ('" . implode("','", $attendSM) . "') ";
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " GROUP BY schregno ";

        return $query;
    }

    //成績入力完了チェック
    public function getRecordChkfinDat($model, $testcd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || RECORD_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

        return $query;
    }

    //成績入力完了更新
    public function updateRecordChkfin($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();

        //初期化
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];

            $query = knjd128yQuery::getRecordChkfinDat($model, $testcd);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow) && $controlFlg == "1") {
                $chkfg = ($model->field["CHK_COMP".$testcd] == 'on') ? '1' : '0';

                $data = array();
                $data["EXECUTED"][TEXT]   = $chkfg ;     //成績入力区分
                $data["REGISTERCD"][TEXT] = STAFFCD ;    //登録者コード
                $data["UPDATED"][FUNC]    = "sysdate()"; //更新日付

                $where  = " WHERE ";
                $where .= "         YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || RECORD_DIV = '{$testcd}' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     AND CLASSCD          = '".$subclass_array[0]."' ";
                    $where .= "     AND SCHOOL_KIND      = '".$subclass_array[1]."' ";
                    $where .= "     AND CURRICULUM_CD    = '".$subclass_array[2]."' ";
                    $where .= "     AND SUBCLASSCD       = '".$subclass_array[3]."' ";
                } else {
                    $where .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_SDIV_DAT", $where);
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /* 成績データ更新処理 */
    public function update($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $year = CTRL_YEAR;

        //算出先も更新するため、算出先の配列を作成
        $testSakiArray = array();
        foreach ($model->testcdMotoArray as $testSaki => $codeArray) {
            $testSakiArray[] = $testSaki;
        }

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $schno = $model->fields["SCHREGNO"][$i];

            foreach ($model->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];
                $controlFlg = $codeArray["CONTROL_FLG"];
                $score = $model->fields["SCORE".$testcd][$i];

                //欠席者で見込点があれば、更新不可
                if ($model->Properties["useMikomiFlg"] == '1') {
                    $mikomi = $model->fields["MIKOMI".$testcd][$i];
                    if (is_numeric($mikomi) && $score == "*") {
                        continue;
                    }
                }

                //管理者コントロール
                if ($controlFlg == "1" || in_array($testcd, $testSakiArray)) {
                    knjd128yQuery::delInsRecordScore($year, $testcd, $model, $schno, $score, $db);
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //更新
    public function delInsRecordScore($year, $testcd, $model, $schno, $score, $db)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }

        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = substr($testcd, 0, 1);
        $data["TESTKINDCD"][TEXT]   = substr($testcd, 1, 2);
        $data["TESTITEMCD"][TEXT]   = substr($testcd, 3, 2);
        $data["SCORE_DIV"][TEXT]    = substr($testcd, 5, 2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["SCORE"][NUMBER]      = $score == "*" ? "" : $score;
        $data["VALUE_DI"][TEXT]     = $score == "*" ? $score : "";
        $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        //削除
        $query = knjd128yQuery::deleteRecordScore($year, $testcd, $schno, $model);
        $db->query($query);
        //成績が1件も入力されていない場合、RECORD_SCORE_DATのレコードは作成しない（考査ごと）
        //testInsArray：成績が１つでも入力された考査コードを配列に保管
        if (in_array($testcd, $model->testInsArray)) {
            //追加
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }
    }

    //テーブルRECORD_SCORE_DATのレコードを削除
    public function deleteRecordScore($year, $testcd, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //出欠情報更新
    public function updateAttendSubclass($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //対象月（学期）
        $month_array = array();
        $month_array = explode("-", $model->field["MONTH_SEMESTER"]);

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $data = array();
            $data["COPYCD"][TEXT]           = "0";
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["MONTH"][TEXT]            = $month_array[0];
            $data["SEMESTER"][TEXT]         = $month_array[1];
            $data["SCHREGNO"][TEXT]         = $model->fields["SCHREGNO"][$i];
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            $data["APPOINTED_DAY"][TEXT]    = $model->field["APPOINTED_DAY"];   //締め日
            $data["LESSON"][NUMBER]         = $model->fields["LESSON"][$i];     //授業時数
            $data["NONOTICE"][NUMBER]       = $model->fields["NONOTICE"][$i];   //欠時
            $data["LATE"][NUMBER]           = $model->fields["LATE"][$i];       //遅刻
            $data["EARLY"][NUMBER]          = $model->fields["EARLY"][$i];      //早退
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ";
            $where .= "     COPYCD = '0' ";
            $where .= "     AND YEAR        = '".CTRL_YEAR."' ";
            $where .= "     AND MONTH       = '".$month_array[0]."' ";
            $where .= "     AND SEMESTER    = '".$month_array[1]."' ";
            $where .= "     AND SCHREGNO    = '".$model->fields["SCHREGNO"][$i]."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
                $where .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
                $where .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
                $where .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
            } else {
                $where .= "  AND SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' ";
            }

            $query = " SELECT COUNT(*) FROM ATTEND_SUBCLASS_DAT ".$where;
            if (0 < $db->getOne($query)) {
                $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
            }
            $db->query($query);
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //対象月コンボ
    public function selectMonthQuery($model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $subclassArray = array();
            $subclassArray = explode("-", $model->field["SUBCLASSCD"]);
            $setNameCd = "Z".$subclassArray[1]."05";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }

        $query  = " SELECT ";
        $query .= "     N1.NAME1 || ' (' || S1.SEMESTERNAME || ') ' AS LABEL, ";
        $query .= "     T1.MONTH || '-' || T1.SEMESTER AS VALUE, ";
        $query .= "     CASE WHEN SMALLINT(T1.MONTH) < 4 ";
        $query .= "          THEN SMALLINT(T1.MONTH) + 12 ";
        $query .= "          ELSE SMALLINT(T1.MONTH) END AS MONTH_INT ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST T1 ";
        $query .= "     INNER JOIN SEMESTER_MST S1 ON S1.YEAR = T1.YEAR AND S1.SEMESTER = T1.SEMESTER ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '{$model->school_kind}' ";
        }
        $query .= "     INNER JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR AND N1.NAMECD1 = '{$setNameCd}' AND N1.NAMECD2 = T1.MONTH ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.APPOINTED_DAY IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     MONTH_INT ";

        return $query;
    }

    //締め日の取得
    public function getAppointedDay($model)
    {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".$model->school_kind."' ";
        }
        $query .= "     AND MONTH || '-' || SEMESTER = '".$model->field["MONTH_SEMESTER"]."' ";

        return $query;
    }

    //出欠情報
    public function getAttendSubclassDat($schno, $absent_cov, $absent_cov_late, $schoolRow, $model)
    {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }

        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        $late_early = "value(late,0) + value(early,0)";
        $notice = "value(notice,0) + value(nonotice,0) + value(sick,0) + value(nurseoff,0)";
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $notice .= " + value(OFFDAYS,0)";
        }
        if ($schoolRow["SUB_ABSENT"]   == "1") {
            $notice .= " + value(ABSENT,0)";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $notice .= " + value(SUSPEND,0)";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $notice .= " + value(MOURNING,0)";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $notice .= " + value(VIRUS,0)";
        }
        if ($schoolRow["SUB_KOUDOME"] == "1") {
            $notice .= " + value(KOUDOME,0)";
        }

        //SQL
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.LESSON, ";    //授業時数
        $query .= "     T1.NONOTICE, ";  //欠時
        $query .= "     T1.LATE, ";      //遅刻
        $query .= "     T1.EARLY, ";     //早退
        //出席すべき時数
        $query .= "     VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0)";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " - VALUE(VIRUS,0)";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " - VALUE(KOUDOME,0)";
        }
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + VALUE(OFFDAYS,0)";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $query .= " + VALUE(SUSPEND,0)";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $query .= " + VALUE(MOURNING,0)";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $query .= " + VALUE(VIRUS,0)";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $query .= " + VALUE(KOUDOME,0)";
        }
        $query .= "     AS MLESSON, ";
        //欠課
        if (($absent_cov == "1" || $absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS KEKKA ";
        } elseif (($absent_cov == "3" || $absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS KEKKA ";
        } elseif ($absent_cov == "5" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   (({$late_early}) / {$absent_cov_late}) + ({$notice}) + (CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$schoolRow["AMARI_KURIAGE"]} THEN 1 ELSE 0 END) AS KEKKA ";
        } else {
            $query .= "   ".$notice." AS KEKKA ";
        }
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.COPYCD = '0' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.MONTH || '-' || T1.SEMESTER = '".$model->field["MONTH_SEMESTER"]."' ";
        $query .= "     AND T1.SCHREGNO = '{$schno}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "   AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "   AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "   AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "   AND T1.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }
}
