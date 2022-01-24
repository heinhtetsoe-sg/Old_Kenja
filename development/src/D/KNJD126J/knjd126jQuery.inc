<?php

require_once('for_php7.php');
class knjd126jquery extends Query
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
    public function selectNamemstQuery($model)
    {
        $setNameCd = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."09";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '{$setNameCd}' ";
        $query .= " ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    //教科データの取得
    public function selectSubclassQuery($model)
    {
        $query  = "SELECT ";
        $query .= "    T3.classcd, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.SCHOOL_KIND, ";
        }
        $query .= "    T3.classname, ";
        $query .= "    value(T3.electdiv,'0') as electdiv ";
        $query .= "FROM ";
        $query .= "    chair_dat T1, ";
        $query .= "    chair_std_dat T2, ";
        $query .= "    class_mst T3, ";
        $query .= "    chair_stf_dat T4 ";
        $query .= "WHERE ";
        $query .= "    T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "    T1.semester   = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "    T2.year       = T1.year AND ";
        $query .= "    T1.year       = T4.year AND ";
        $query .= "    T1.semester   = T4.semester AND ";
        $query .= "    T1.chaircd    = T4.chaircd AND ";
        $query .= "    T2.semester   = T1.semester AND ";
        $query .= "    T2.chaircd    = T1.chaircd AND ";
        $query .= "    T3.classcd IN (SELECT substr(J1.viewcd,1,2) FROM jviewname_grade_ydat J1 WHERE J1.year = '".CTRL_YEAR."' GROUP BY substr(J1.viewcd,1,2)) AND ";
        //更新可能(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD       = T1.CLASSCD  AND ";
            $query .= "    T3.SCHOOL_KIND   = T1.SCHOOL_KIND  ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "    AND T3.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "    AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "    T3.classcd = substr(T1.subclasscd,1,2) ";
        }
        $query .= "GROUP BY ";
        $query .= "    T3.classcd, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.SCHOOL_KIND, ";
        }
        $query .= "    T3.classname, ";
        $query .= "    value(T3.electdiv,'0') ";
        $query .= "ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD, ";
            $query .= "    T3.SCHOOL_KIND ";
        } else {
            $query .= "    T3.classcd ";
        }
        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASS_VALUE, ";
        } else {
            $query .= "    T1.SUBCLASSCD AS SUBCLASS_VALUE, ";
        }
        $query .= "     T1.chaircd, ";
        $query .= "     T1.chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        $query .= "     T1.classcd    not in ( ";
        $query .= "                               SELECT ";
        $query .= "                                   NAMECD2 ";
        $query .= "                               FROM ";
        $query .= "                                   V_NAME_MST ";
        $query .= "                               WHERE ";
        $query .= "                                   YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                                   NAMECD1 = 'D008' ";
        $query .= "                          ) AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    substr(T1.subclasscd,1,2) = '".$class_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND            = '".$class_array[1]."'  ";
        } else {
            $query .= "    substr(T1.subclasscd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //名称マスタ(D065)件数取得
    public function getNameMstD065($model)
    {
        $chaircd = $subclasscd = "";
        if ($model->field["CHAIRCD_SUBCLASS"]) {
            list($chaircd, $subclasscd) = preg_split("{:}", $model->field["CHAIRCD_SUBCLASS"]);
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'D065' AND ";
        $query .= "     NAME1   = '".$subclasscd."' ";

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
        $query .= " AND NAME1 = '".$model->schoolKindFromClassCd."' ";
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

    //編集可能項目の判別1
    public function selectContolCodeQuery($model)
    {
        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
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

    //編集可能項目の判別2
    public function selectViewcdQuery($model, $execute_date)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }
        $query  = "WITH MAX_GRADE AS ( ";
        $query .= "    SELECT ";
        $query .= "        max(t2.grade) as grade ";
        $query .= "    FROM ";
        $query .= "        schreg_regd_hdat t1, ";
        $query .= "        schreg_regd_dat t2, ";
        $query .= "        schreg_base_mst t3, ";
        $query .= "        chair_std_dat t4 ";
        $query .= "    WHERE ";
        $query .= "        t1.year      = t2.year AND ";
        $query .= "        t1.semester  = t2.semester AND ";
        $query .= "        t1.grade     = t2.grade AND ";
        $query .= "        t1.hr_class  = t2.hr_class AND ";
        $query .= "        t2.year      = '".CTRL_YEAR."' AND ";
        $query .= "        t2.semester  = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "        t2.schregno  = t3.schregno AND ";
        $query .= "        t4.year      = t2.year AND ";
        $query .= "        t4.chaircd   = '".$chaircd_subclass_array[0]."' AND ";
        $query .= "        t4.schregno  = t2.schregno AND ";
        $query .= "        '".$execute_date."' BETWEEN t4.appdate AND t4.appenddate ";
        $query .= "    ) ";

        $query .= "SELECT ";
        $query .= "    t1.viewcd, t2.viewname ";
        $query .= "FROM ";
        $query .= "    jviewname_grade_ydat t1, jviewname_grade_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year = '".CTRL_YEAR."' AND ";
        $query .= "    t1.grade = t2.grade AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    t1.CLASSCD        = '".$subclass_array[0]."' AND ";
            $query .= "    t1.SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
            $query .= "    t1.CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
            $query .= "    t1.SUBCLASSCD     = '".$subclass_array[3]."' AND ";
            $query .= "    t1.CLASSCD       = t2.CLASSCD AND ";
            $query .= "    t1.SCHOOL_KIND   = t2.SCHOOL_KIND AND ";
            $query .= "    t1.CURRICULUM_CD = t2.CURRICULUM_CD AND ";
            $query .= "    t1.SUBCLASSCD    = t2.SUBCLASSCD AND ";
        } else {
            $query .= "    t1.SUBCLASSCD = '".$chaircd_subclass_array[1]."' AND ";
            $query .= "    t1.SUBCLASSCD = t2.SUBCLASSCD AND ";
        }
        $query .= "    t1.viewcd = t2.viewcd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(t1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    substr(t1.viewcd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "    AND t1.grade in (SELECT w1.grade FROM MAX_GRADE w1) ";
        $query .= "ORDER BY ";
        $query .= "    t1.viewcd ";

        return $query;
    }

    //データ取得
    public function getJviewstatRecordDat($model, $view_key)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
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
            $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD    = '".$chaircd_subclass_array[1]."' ";
        }
        //観点コード
        if (get_count($view_key) > 0) {
            $query .= "     AND viewcd in('".implode("','", $view_key)."') ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND substr(VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "     AND substr(viewcd,1,2) = '".$model->field["CLASSCD"]."' ";
        }

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date, $view_key)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }

        $isSyougaku = ($model->field["SCHOOL_KIND"]=='P' && $model->Properties["KNJD126J_useHyouteiSanshutsu"] =='1');

        $query  = "WITH VIEWSTAT AS ( ";
        $query .= "    SELECT ";
        $query .= "        schregno ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            $query .= "    ,max(case when viewcd = '".$col."' then status else null end) as status".$code;
        }
        $query .= "    FROM ";
        $query .= "        JVIEWSTAT_RECORD_DAT ";
        $query .= "    WHERE ";
        $query .= "        year = '".CTRL_YEAR."' AND ";
        $query .= "        semester = '".$model->field["SEMESTER"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD        = '".$subclass_array[0]."' AND ";
            $query .= "        SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
            $query .= "        CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
            $query .= "        SUBCLASSCD     = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "        SUBCLASSCD = '".$chaircd_subclass_array[1]."' AND ";
        }
        //観点コード
        if (get_count($view_key) > 0) {
            $query .= "    viewcd in('".implode("','", $view_key)."') AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        SUBSTR(VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "        substr(viewcd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "    GROUP BY schregno ";
        $query .= "    ) ";

        //-----メイン-----//
        $query .= "SELECT ";
        $query .= "    tbl1.hr_name, tbl1.grade, tbl1.hr_class, tbl1.attendno, tbl1.schregno, tbl1.name_show ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            if (substr($col, 2) != "99") {
                if (substr($col, 2) == "98" && $model->Properties["KNJD126J_useCtlHyoutei"] == '1' && $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
                    $query .= ",T4.PROV_FLG AS STATUS".$code;
                } else {
                    $query .= ",tbl2.status".$code;
                }
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
        $query .= "    (SELECT ";
        $query .= "        t1.hr_name, t2.grade, t2.hr_class, t2.attendno, t4.schregno, t3.name_show ";
        $query .= "    FROM ";
        $query .= "        schreg_regd_hdat t1, ";
        $query .= "        schreg_regd_dat t2, ";
        $query .= "        schreg_base_mst t3, ";
        $query .= "        chair_std_dat t4 ";
        $query .= "    WHERE ";
        $query .= "        t1.year      = t2.year AND ";
        $query .= "        t1.semester  = t2.semester AND ";
        $query .= "        t1.grade     = t2.grade AND ";
        $query .= "        t1.hr_class  = t2.hr_class AND ";
        $query .= "        t2.year      = '".CTRL_YEAR."' AND ";
        $query .= "        t2.semester  = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "        t2.schregno  = t3.schregno AND ";
        $query .= "        t4.year      = t2.year AND ";
        //$query .= "        t4.semester  = t2.semester AND ";
        $query .= "        t4.chaircd   = '".$chaircd_subclass_array[0]."' AND ";
        $query .= "        t4.schregno  = t2.schregno AND ";
        $query .= "        '".$execute_date."' BETWEEN t4.appdate AND t4.appenddate ";
        $query .= "    ) tbl1 ";
        $query .= "    left join VIEWSTAT tbl2 on tbl2.schregno = tbl1.schregno ";
        if ($model->Properties["KNJD126J_useCtlHyoutei"] == '1' && $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
            $query .= "     LEFT JOIN JVIEWSTAT_RECORD_PROV_FLG_DAT T4 ";
            $query .= "       ON T4.YEAR = '".CTRL_YEAR."' ";
            $query .= "      AND T4.SEMESTER = '9' ";
            $query .= "      AND T4.TESTKINDCD = '99' ";
            $query .= "      AND T4.TESTITEMCD = '00' ";
            $query .= "      AND T4.SCORE_DIV = '09' ";
            $query .= "      AND T4.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "      AND T4.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "      AND T4.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "      AND T4.SUBCLASSCD    = '".$subclass_array[3]."' ";
            $query .= "      AND T4.SCHREGNO      = tbl1.SCHREGNO ";
            $query .= "      AND T4.PROV_DIV = '01' ";
        }
        if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
            $query .= "     LEFT JOIN KIN_RECORD_DAT T3 ";
            $query .= "              ON T3.YEAR         = '".CTRL_YEAR."' ";
            $query .= "             AND T3.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "             AND T3.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "             AND T3.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "             AND T3.SUBCLASSCD     = '".$subclass_array[3]."' ";
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
                $query .= "             AND T3.CLASSCD        = '".$subclass_array[0]."' ";
                $query .= "             AND T3.SCHOOL_KIND    = '".$subclass_array[1]."' ";
                $query .= "             AND T3.CURRICULUM_CD  = '".$subclass_array[2]."' ";
                $query .= "             AND T3.SUBCLASSCD     = '".$subclass_array[3]."' ";
            } else {
                $query .= "             AND T3.SUBCLASSCD   = '".$chaircd_subclass_array[1]."' ";
            }
        }
        $query .= "             AND T3.SCHREGNO     = tbl1.SCHREGNO ";
        $query .= "ORDER BY ";
        $query .= "    tbl1.grade, tbl1.hr_class, tbl1.attendno ";

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
    public function selectJviewnameAssessMst($model, $grade, $assesslevel, $jviewCnt)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }

        $query  = " SELECT STATUS FROM JVIEWNAME_ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= " AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= " AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= " AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND SUBCLASSCD   = '".$chaircd_subclass_array[1]."' ";
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

    /* データ更新処理 */
    public function update($model)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_RECORD_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "    SCHREGNO = '".$model->data["SCHREGNO"][$i]."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD        = '".$subclass_array[0]."' AND ";
                $query .= "    SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
                $query .= "    CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
                $query .= "    SUBCLASSCD     = '".$subclass_array[3]."' ";
            } else {
                $query .= "    SUBCLASSCD = '".$chaircd_subclass_array[1]."' ";
            }
            $db->query($query);

            foreach ($model->data["STATUS"] as $code => $col) {
                //評定は更新しない
                if (substr($col, 2) == "99") {
                    continue;
                }
                if (substr($col, 2) == "98") {
                    ////画面にチェックボックスが表示されるのであれば、ここでは更新しない(後の処理で更新)
                    if ($model->Properties["KNJD126J_useCtlHyoutei"] == "1" && $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
                        continue;
                    }
                }

                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]              = $subclass_array[0];
                    $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
                    $data["CURRICULUM_CD"][TEXT]        = $subclass_array[2];
                    $data["SUBCLASSCD"][TEXT]           = $subclass_array[3];
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $chaircd_subclass_array[1];
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
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }
        $db = Query::dbCheckOut();

        $isSyougaku = ($model->field["SCHOOL_KIND"]=='P' && $model->Properties["KNJD126J_useHyouteiSanshutsu"] =='1');

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            if ($isSyougaku) {
                $isSyou1 = knjd126jQuery::getGradeCd($db, CTRL_YEAR, $model->field["SEMESTER"], $model->data["SCHREGNO"][$i], $model->field["SCHOOL_KIND"]) == '01';
            }
            $col99Code = null;
            $namecd2 = 0;
            foreach ($model->data["STATUS"] as $code => $col) {
                //評定の更新に合わせて、チェックの値も更新
                if (substr($col, 2) == "98") {
                    //評定がこの画面で入力可能で、かつKNJD126J_useCtlHyouteiプロパティが立っている時のみ、更新する
                    if ($model->Properties["KNJD126J_useCtlHyoutei"] == "1" && $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
                        $where  = " WHERE YEAR         = '".CTRL_YEAR."' ";
                        $where .= "  AND SEMESTER      = '9' ";
                        $where .= "  AND TESTKINDCD    = '99' ";
                        $where .= "  AND TESTITEMCD    = '00' ";
                        $where .= "  AND SCORE_DIV     = '09' ";
                        $where .= "  AND CLASSCD       = '{$subclass_array[0]}' ";
                        $where .= "  AND SCHOOL_KIND   = '{$subclass_array[1]}' ";
                        $where .= "  AND CURRICULUM_CD = '{$subclass_array[2]}' ";
                        $where .= "  AND SUBCLASSCD    = '{$subclass_array[3]}' ";
                        $where .= "  AND SCHREGNO      = '{$model->data["SCHREGNO"][$i]}' ";
                        $where .= "  AND PROV_DIV      = '01' ";
                        $cnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM JVIEWSTAT_RECORD_PROV_FLG_DAT ".$where);

                        $data = array();
                        $data["PROV_FLG"][TEXT]   = $model->fields["STATUS".$code][$i];
                        $data["REGISTERCD"][TEXT] = STAFFCD;
                        $data["UPDATED"][FUNC]    = "sysdate()";
                        if ($cnt > 0) {
                            //既存なので、更新
                            $query = Query::updateSQL($data, "JVIEWSTAT_RECORD_PROV_FLG_DAT", $where);
                        } else {
                            $data["YEAR"][TEXT]          = CTRL_YEAR;
                            $data["SEMESTER"][TEXT]      = '9';
                            $data["TESTKINDCD"][TEXT]    = '99';
                            $data["TESTITEMCD"][TEXT]    = '00';
                            $data["SCORE_DIV"][TEXT]     = '09';
                            $data["CLASSCD"][TEXT]       = $subclass_array[0];
                            $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
                            $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
                            $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
                            $data["SCHREGNO"][TEXT]      = $model->data["SCHREGNO"][$i];
                            $data["PROV_DIV"][TEXT]      = '01';

                            //新規追加
                            $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_PROV_FLG_DAT");
                        }
                        $db->query($query);
                    }
                }

                //評定のみ更新
                if (substr($col, 2) != "99") {
                    if ($isSyougaku) {
                        $namecd2 += intval(knjd126jQuery::getNameSpare2($db, $model->fields["STATUS".$code][$i]));
                    }
                    continue;
                } else {
                    $col99Code = $code;
                }
                if (!is_null($col99Code)) {
                    if ($model->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
                        //KIN_RECORD_DATの追加
                        knjd126jQuery::insertKinRecord($db, $model, $i);

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
                        $where .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
                        $where .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
                        $where .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
                        $where .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
                        $where .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";

                        //更新
                        $query = Query::updateSQL($data, "KIN_RECORD_DAT", $where);
                    } else {
                        //RECORD_SCORE_DATの追加
                        knjd126jQuery::insertRecord($db, $model, $i);

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
                                $where .= "  AND   SCORE_DIV    = '09' ";
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
                            $where .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
                            $where .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
                            $where .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
                            $where .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
                        } else {
                            $where .= "  AND   SUBCLASSCD   = '".$chaircd_subclass_array[1]."' "; //科目更新
                        }
                        $where .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";

                        //評定をセット
                        $data = array();
                        if ($isSyougaku) {
                            if ($model->fields["STATUS".$code][$i] == '') {
                                $data["SCORE"][NUMBER]  = knjd126jQuery::getNameValue($db, $namecd2, $isSyou1);
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
        }

        Query::dbCheckIn($db);
    }

    // RECORD_SCORE_DATの追加
    public function insertRecord($db, $model, $i)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
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
                $query .= "  AND   SCORE_DIV    = '09' ";
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
            $query .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND   SUBCLASSCD   = '".$chaircd_subclass_array[1]."' ";
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
                $data["CLASSCD"][TEXT]              = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]        = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]           = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]   = $chaircd_subclass_array[1];
            }
            $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
            $data["CHAIRCD"][TEXT]      = $chaircd_subclass_array[0];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }
    }

    // KIN_RECORD_DATの追加
    public function insertKinRecord($db, $model, $i)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);

        //科目対応
        $subclass_array = array();
        $subclass_array = explode("-", $chaircd_subclass_array[1]);

        // KIN_RECORD_DATの存在チェック
        $query  = "SELECT  COUNT(*) ";
        $query .= "FROM    KIN_RECORD_DAT ";
        $query .= "WHERE   YEAR         = '".CTRL_YEAR."' ";
        $query .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
        $query .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
        $query .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
        $query .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
        $query .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";
        $record_cnt = $db->getOne($query);

        // KIN_RECORD_DATがなかったら追加する
        if ($record_cnt == 0) {
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["CLASSCD"][TEXT]              = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]        = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]           = $subclass_array[3];
            $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
            $data["CHAIRCD"][TEXT]      = $chaircd_subclass_array[0];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

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
    public function getGradeCd($db, $year, $semester, $schregno, $schoolKind)
    {
        $query .= " SELECT ";
        $query .= "     t2.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_GDAT t2 ";
        $query .= " ON ";
        $query .= "     t1.GRADE=t2.GRADE ";
        $query .= "     AND t1.YEAR=t2.YEAR ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '{$year}' ";
        $query .= "     AND t1.SEMESTER='{$semester}' ";
        $query .= "     AND t1.SCHREGNO='{$schregno}' ";
        $query .= "     AND t2.SCHOOL_KIND='{$schoolKind}' ";

        return $db->getOne($query);
    }
}
