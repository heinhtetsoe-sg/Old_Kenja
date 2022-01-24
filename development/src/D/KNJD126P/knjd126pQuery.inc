<?php

require_once('for_php7.php');

class knjd126pquery extends Query
{
    //校種一覧取得
    public function getSchoolKindList($model, $flg)
    {
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
            $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタの取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getHrClass($model)
    {
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
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER2"]."' AND ";
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
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //選択教科取得
    public function getClassMst($subclasscd, $grade_hr_class, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || ':' || T2.CLASSNAME AS LABEL, ";
        } else {
            $query .= "     T2.CLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || ' ' || T2.CLASSNAME AS LABEL, ";
        }
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     V_CLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class, 0, 2)."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        }
        if ($subclasscd) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T2.CLASSCD          = '".$subclass_array[0]."' ";
                $query .= "     AND T2.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            } else {
                $query .= "     AND T2.CLASSCD = '".substr($subclasscd, 0, 2)."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ(D065)件数取得
    public function getNameMstD065($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'D065' AND ";
        $query .= "     NAME1   = '".$model->field["SUBCLASSCD"]."' ";

        return $query;
    }

    //名称マスタ(D085)の取得
    public function getNameMstD085($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAME2, ";
        $query .= "     NAME3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'D085'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND NAME1 = '".$model->field["SCHOOL_KIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= "     AND '".$model->field["GRADE"]."' BETWEEN NAME2 AND NAME3";
        return $query;
    }

    //科目取得
    public function getSubclassMst($grade_hr_class, $model)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || '：' || T2.SUBCLASSNAME AS LABEL, ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL, ";
        }
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class, 0, 2)."' AND ";
        $query .= "     T2.CLASSCD NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             NAMECD2 ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             NAMECD1 = 'D008' ";
        $query .= "     ) AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタの取得
    public function getKantenHyouka($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV1, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "     NAMECD1 = 'D028' ";
        } else {
            $query .= "     NAMECD1 = 'D029' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //管理者コントロール取得
    public function getAdminContol($model)
    {
        $query  = " SELECT ";
        $query .= "     CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '3' ";
        return $query;
    }

    //編集可能項目の判別
    public function selectViewcdQuery($model)
    {

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     JVIEWNAME_GRADE_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' AND ";
        $query .= "    T1.GRADE = T2.GRADE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD        = '".$substr_classcd."' AND ";
            $query .= "    T1.SCHOOL_KIND    = '".$substr_school_kind."' AND ";
            $query .= "    T1.CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
            $query .= "    T1.SUBCLASSCD     = '".$substr_subclasscd."' AND ";
            $query .= "    T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= "    T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
            $query .= "    T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
            $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        }
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$substr_classcd."' ";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    //データ取得
    public function getJviewstatRecordDat($model, $view_key)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD       = '".$substr_classcd."' ";
            $query .= "     AND SCHOOL_KIND   = '".$substr_school_kind."' ";
            $query .= "     AND CURRICULUM_CD = '".$substr_curriculum_cd."' ";
            $query .= "     AND SUBCLASSCD    = '".$substr_subclasscd."' ";
        } else {
            $query .= "     AND SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
        }
        //観点コード
        if (get_count($view_key) > 0) {
            $query .= "     AND viewcd in('".implode("','", $view_key)."') ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND substr(VIEWCD,1,2) = '".$substr_classcd."' ";
        } else {
            $query .= "     AND substr(viewcd,1,2) = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
        }

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $view_key)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $isSyougaku = ($model->field["SCHOOL_KIND"]=='P' && $model->Properties["KNJD126J_useHyouteiSanshutsu"] =='1');

        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "        T1.HR_NAME, ";
        $query .= "        T2.GRADE, ";
        $query .= "        T2.HR_CLASS, ";
        $query .= "        T2.ATTENDNO, ";
        $query .= "        T2.SCHREGNO, ";
        $query .= "        T3.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "        SCHREG_REGD_HDAT T1, ";
        $query .= "        SCHREG_REGD_DAT T2, ";
        $query .= "        SCHREG_BASE_MST T3 ";
        $query .= "     WHERE ";
        $query .= "        T1.YEAR      = T2.YEAR AND ";
        $query .= "        T1.SEMESTER  = T2.SEMESTER AND ";
        $query .= "        T1.GRADE     = T2.GRADE AND ";
        $query .= "        T1.HR_CLASS  = T2.HR_CLASS AND ";
        $query .= "        T1.GRADE || T1.HR_CLASS  = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "        T2.YEAR      = '".CTRL_YEAR."' AND ";
        $query .= "        T2.SEMESTER  = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "        T2.SCHREGNO  = T3.SCHREGNO ";
        $query .= " ), VIEWSTAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            $query .= "    ,MAX(CASE WHEN T2.VIEWCD = '".$col."' THEN T2.STATUS ELSE NULL END) AS STATUS".$code;
        }
        $query .= "     FROM ";
        $query .= "         SCHINFO T1, ";
        $query .= "         JVIEWSTAT_RECORD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "        T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "        T1.SCHREGNO = T2.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T2.CLASSCD        = '".$substr_classcd."' AND ";
            $query .= "        T2.SCHOOL_KIND    = '".$substr_school_kind."' AND ";
            $query .= "        T2.CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
            $query .= "        T2.SUBCLASSCD     = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "        T2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //観点コード
        if (get_count($view_key) > 0) {
            $query .= "    T2.VIEWCD IN ('".implode("','", $view_key)."') AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        SUBSTR(T2.VIEWCD, 1, 2) = '".$substr_classcd."' ";
        } else {
            $query .= "        SUBSTR(T2.VIEWCD, 1, 2) = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";
        $query .= "    ) ";

        //-----メイン-----//
        $query .= "SELECT ";
        $query .= "    T1.HR_NAME, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.NAME_SHOW ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            if (substr($col, 2) != "99") {
                $query .= ",T2.STATUS".$code;
            } else {
                if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
                    if ($model->field["SEMESTER"] == "1") {
                        $query .= ",T3.SEM1_ASSESS AS STATUS".$code;
                    } elseif ($model->field["SEMESTER"] == "2") {
                        $query .= ",T3.SEM2_ASSESS AS STATUS".$code;
                    } elseif ($model->field["SEMESTER"] == "3") {
                        $query .= ",T3.SEM3_ASSESS AS STATUS".$code;
                    } else {
                        $query .= ",T3.GRADE_ASSESS AS STATUS".$code;
                    }
                } else {
                    if ($isSyougaku) {
                        $query .= ",T3.SCORE AS STATUS".$code;
                    } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                        $query .= ",T3.SCORE AS STATUS".$code;
                    } else {
                        $query .= ",T3.VALUE AS STATUS".$code;
                    }
                }
            }
        }
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
            $query .= "     LEFT JOIN KIN_RECORD_DAT T3 ";
            $query .= "              ON T3.YEAR         = '".CTRL_YEAR."' ";
            $query .= "             AND T3.CLASSCD        = '".$substr_classcd."' ";
            $query .= "             AND T3.SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "             AND T3.CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "             AND T3.SUBCLASSCD     = '".$substr_subclasscd."' ";
        } else {
            $query .= "     LEFT JOIN RECORD_SCORE_DAT T3 ";
            $query .= "              ON T3.YEAR         = '".CTRL_YEAR."' ";
            $query .= "             AND T3.SEMESTER     = '".$model->field["SEMESTER"]."' ";
            $query .= "             AND T3.TESTKINDCD   = '99' ";
            $query .= "             AND T3.TESTITEMCD   = '00' ";
            if ($isSyougaku) {
                $query .= "         AND T3.SCORE_DIV    = '08' ";
            } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                if ($model->Properties["useHyoukaHyouteiFlg"] == '2') {
                    $query .= "             AND T3.SCORE_DIV    = '09' ";
                } else {
                    if ($model->field["SEMESTER"] == '9' && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                        $query .= "             AND T3.SCORE_DIV    = '09' ";
                    } else {
                        $query .= "             AND T3.SCORE_DIV    = '08' ";
                    }
                }
            } else {
                $query .= "             AND T3.SCORE_DIV    = '00' ";
            }
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             AND T3.CLASSCD        = '".$substr_classcd."' ";
                $query .= "             AND T3.SCHOOL_KIND    = '".$substr_school_kind."' ";
                $query .= "             AND T3.CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
                $query .= "             AND T3.SUBCLASSCD     = '".$substr_subclasscd."' ";
            } else {
                $query .= "             AND T3.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
            }
        }
        $query .= "             AND T3.SCHREGNO     = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }

    public function getTransfer($model, $schregNo)
    {
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][$model->field["SEMESTER2"]]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][$model->field["SEMESTER2"]]);

        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregNo}' ";
        $query .= "     AND (TRANSFER_SDATE BETWEEN '{$sdate}' AND '{$edate}' ";
        $query .= "          OR ";
        $query .= "          TRANSFER_EDATE BETWEEN '{$sdate}' AND '{$edate}' ";
        $query .= "          OR ";
        $query .= "          '{$sdate}' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "          OR ";
        $query .= "          '{$edate}' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "         ) ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregNo}' ";
        $query .= "     AND '{$sdate}' >= GRD_DATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUM(CNT) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    //変換表取込
    public function selectJviewnameAssessMst($model, $assesslevel, $jviewCnt)
    {
        $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }

        $query  = " SELECT STATUS FROM JVIEWNAME_ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD       = '".$substr_classcd."' ";
            $query .= " AND SCHOOL_KIND   = '".$substr_school_kind."' ";
            $query .= " AND CURRICULUM_CD = '".$substr_curriculum_cd."' ";
            $query .= " AND SUBCLASSCD    = '".$substr_subclasscd."' ";
        } else {
            $query .= " AND SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND JVIEW_DIV     = '3' ";
        } else {
            $query .= " AND JVIEW_DIV     = '2' ";
        }
        $query .= " AND ASSESSLEVEL   = '".$assesslevel."' ";
        $query .= " AND JVIEW_CNT     = '".$jviewCnt."' ";

        return $query;
    }

    //更新
    public function update($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        for ($i=0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_RECORD_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "    SCHREGNO = '".$model->data["SCHREGNO"][$i]."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD        = '".$substr_classcd."' AND ";
                $query .= "    SCHOOL_KIND    = '".$substr_school_kind."' AND ";
                $query .= "    CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
                $query .= "    SUBCLASSCD     = '".$substr_subclasscd."' ";
            } else {
                $query .= "    SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $db->query($query);

            foreach ($model->data["STATUS"] as $code => $col) {
                //評定は更新しない
                if (substr($col, 2) == "99") {
                    continue;
                }

                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]              = $substr_classcd;
                    $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
                    $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                }
                $data["VIEWCD"][TEXT]       = $col;
                if (
                    ($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] && $model->fields["STATUS".$code][$i] == "") ||
                    ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] && $model->fields["STATUS".$code][$i] == "")
                ) {
                    $data["STATUS"][TEXT]       = $model->nonVisualViewCd;
                } elseif (
                    ($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] && $model->fields["STATUS".$code][$i] == "F") ||
                    ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] && $model->fields["STATUS".$code][$i] == "F")
                ) {
                    $data["STATUS"][TEXT]       = "";
                } else {
                    $data["STATUS"][TEXT]       = $model->fields["STATUS".$code][$i];
                }
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /* 成績データ更新処理 */
    public function updateRecord($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        $isSyougaku = ($model->field["SCHOOL_KIND"]=='P' && $model->Properties["KNJD126J_useHyouteiSanshutsu"] =='1');
        if ($isSyougaku) {
            $isSyou1 = knjd126pQuery::getGradeCd($db, CTRL_YEAR, $model->field["GRADE"], $model->field["SCHOOL_KIND"]) == '01';
        }
        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $col99Code = null;
            $namecd2 = 0;
            foreach ($model->data["STATUS"] as $code => $col) {
                //評定のみ更新
                if (substr($col, 2) != "99") {
                    if ($isSyougaku) {
                        $namecd2 += intval(knjd126pQuery::getNameSpare2($db, $model->fields["STATUS".$code][$i]));
                    }
                    continue;
                } else {
                    $col99Code = $code;
                }
            }
            if (!is_null($col99Code)) {
                $code = $col99Code;
                $col = '99';
                if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
                    //KIN_RECORD_DATの追加
                    knjd126pQuery::insertKinRecord($db, $model, $i);

                    //評定をセット
                    $data = array();
                    if ($model->field["SEMESTER"] == "1") {
                        $data["SEM1_ASSESS"][TEXT]  = $model->fields["STATUS".$code][$i];
                    } elseif ($model->field["SEMESTER"] == "2") {
                        $data["SEM2_ASSESS"][TEXT]  = $model->fields["STATUS".$code][$i];
                    } elseif ($model->field["SEMESTER"] == "3") {
                        $data["SEM3_ASSESS"][TEXT]  = $model->fields["STATUS".$code][$i];
                    } else {
                        $data["GRADE_ASSESS"][TEXT]  = $model->fields["STATUS".$code][$i];
                    }

                    //条件
                    $where  = "WHERE   YEAR         = '".CTRL_YEAR."' ";
                    $where .= "  AND   CLASSCD       = '".$substr_classcd."' ";
                    $where .= "  AND   SCHOOL_KIND   = '".$substr_school_kind."' ";
                    $where .= "  AND   CURRICULUM_CD = '".$substr_curriculum_cd."' ";
                    $where .= "  AND   SUBCLASSCD    = '".$substr_subclasscd."' ";
                    $where .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";

                    //更新
                    $query = Query::updateSQL($data, "KIN_RECORD_DAT", $where);
                } else {
                    //RECORD_SCORE_DATの追加
                    knjd126pQuery::insertRecord($db, $model, $i);

                    //数字に変換
                    $status = $model->fields["STATUS".$code][$i];
                    if ($status == "A") {
                        $status = "11";
                    }
                    if ($status == "B") {
                        $status = "22";
                    }
                    if ($status == "C") {
                        $status = "33";
                    }

                    //条件
                    $where  = "WHERE   YEAR         = '".CTRL_YEAR."' ";
                    $where .= "  AND   SEMESTER     = '".$model->field["SEMESTER"]."' ";
                    $where .= "  AND   TESTKINDCD   = '99' ";
                    $where .= "  AND   TESTITEMCD   = '00' ";
                    if ($isSyougaku) {
                        $where .= "  AND   SCORE_DIV    = '08' ";
                    } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                        if ($model->Properties["useHyoukaHyouteiFlg"] == '2') {
                            $where .= "      AND SCORE_DIV    = '09' ";
                        } else {
                            if ($model->field["SEMESTER"] == '9' && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                                $where .= "  AND   SCORE_DIV    = '09' ";
                            } else {
                                $where .= "  AND   SCORE_DIV    = '08' ";
                            }
                        }
                    } else {
                        $where .= "  AND   SCORE_DIV    = '00' ";
                    }
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "  AND   CLASSCD       = '".$substr_classcd."' ";
                        $where .= "  AND   SCHOOL_KIND   = '".$substr_school_kind."' ";
                        $where .= "  AND   CURRICULUM_CD = '".$substr_curriculum_cd."' ";
                        $where .= "  AND   SUBCLASSCD    = '".$substr_subclasscd."' ";
                    } else {
                        $where .= "  AND   SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' "; //科目更新
                    }
                    $where .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";

                    //評定をセット
                    $data = array();
                    if ($isSyougaku) {
                        if ($model->fields["STATUS".$code][$i] == '') {
                            $data["SCORE"][NUMBER]  = knjd126pQuery::getNameValue($db, $namecd2, $isSyou1);
                        } else {
                            $data["SCORE"][NUMBER]  = $model->fields["STATUS".$code][$i];
                        }
                    } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                        $data["SCORE"][NUMBER]  = $status;
                    } else {
                        $data["VALUE"][NUMBER]  = $status;
                    }
                    $data["VALUE_DI"][TEXT] = "";

                    //更新
                    $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                }
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }

    // RECORD_SCORE_DATの追加
    public function insertRecord($db, $model, $i)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }

        $isSyougaku = ($model->field["SCHOOL_KIND"]=='P' && $model->Properties["KNJD126J_useHyouteiSanshutsu"] =='1');

        // RECORD_SCORE_DATの存在チェック
        $query  = "SELECT  COUNT(*) ";
        $query .= "FROM    RECORD_SCORE_DAT ";
        $query .= "WHERE   YEAR         = '".CTRL_YEAR."' ";
        $query .= "  AND   SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "  AND   TESTKINDCD   = '99' ";
        $query .= "  AND   TESTITEMCD   = '00' ";

        if ($isSyougaku) {
            $query .= "  AND   SCORE_DIV    = '08' ";
        } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->Properties["useHyoukaHyouteiFlg"] == '2') {
                $query .= "      AND   SCORE_DIV    = '09' ";
            } else {
                if ($model->field["SEMESTER"] == '9' && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                    $query .= "  AND   SCORE_DIV    = '09' ";
                } else {
                    $query .= "  AND   SCORE_DIV    = '08' ";
                }
            }
        } else {
            $query .= "  AND   SCORE_DIV    = '00' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND   CLASSCD       = '".$substr_classcd."' ";
            $query .= "  AND   SCHOOL_KIND   = '".$substr_school_kind."' ";
            $query .= "  AND   CURRICULUM_CD = '".$substr_curriculum_cd."' ";
            $query .= "  AND   SUBCLASSCD    = '".$substr_subclasscd."' ";
        } else {
            $query .= "  AND   SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";
        $record_cnt = $db->getOne($query);
        // RECORD_SCORE_DATがなかったら追加する
        if ($record_cnt == 0) {
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]   = "99";
            $data["TESTITEMCD"][TEXT]   = "00";
            if ($isSyougaku) {
                $data["SCORE_DIV"][TEXT]    = "08";
            } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                if ($model->Properties["useHyoukaHyouteiFlg"] == '2') {
                    $data["SCORE_DIV"][TEXT]    = "09";
                } else {
                    if ($model->field["SEMESTER"] == '9' && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                        $data["SCORE_DIV"][TEXT]    = "09";
                    } else {
                        $data["SCORE_DIV"][TEXT]    = "08";
                    }
                }
            } else {
                $data["SCORE_DIV"][TEXT]    = "00";
            }
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]              = $substr_classcd;
                $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
                $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
                $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
            }
            $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }
    }

    // KIN_RECORD_DATの追加
    public function insertKinRecord($db, $model, $i)
    {
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);

        // KIN_RECORD_DATの存在チェック
        $query  = "SELECT  COUNT(*) ";
        $query .= "FROM    KIN_RECORD_DAT ";
        $query .= "WHERE   YEAR         = '".CTRL_YEAR."' ";
        $query .= "  AND   CLASSCD       = '".$substr_classcd."' ";
        $query .= "  AND   SCHOOL_KIND   = '".$substr_school_kind."' ";
        $query .= "  AND   CURRICULUM_CD = '".$substr_curriculum_cd."' ";
        $query .= "  AND   SUBCLASSCD    = '".$substr_subclasscd."' ";
        $query .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";
        $record_cnt = $db->getOne($query);

        // KIN_RECORD_DATがなかったら追加する
        if ($record_cnt == 0) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["CLASSCD"][TEXT]          = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]      = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $substr_subclasscd;
            $data["SCHREGNO"][TEXT]         = $model->data["SCHREGNO"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "KIN_RECORD_DAT");
            $db->query($query);
        }
    }
    public function getNameSpare2($db, $name1)
    {
        $query .= " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='D029' ";
        $query .= "     AND ABBV1='{$name1}' ";

        return $db->getOne($query);
    }
    public function getNameValue($db, $namecd2, $isSyou1)
    {
        $query .= " SELECT ";
        if ($isSyou1) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAME2 ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='D112' ";
        $query .= "     AND NAMECD2='{$namecd2}' ";

        return $db->getOne($query);
    }
    public function getGradeCd($db, $year, $grade, $schoolKind)
    {
        $query .= " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$year}' ";
        $query .= "     AND GRADE='{$grade}' ";
        $query .= "     AND SCHOOL_KIND='{$schoolKind}'; ";

        return $db->getOne($query);
    }
}
