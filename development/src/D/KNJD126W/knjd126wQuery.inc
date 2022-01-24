<?php

require_once('for_php7.php');

class knjd126wquery extends Query
{
    //学校確認
    public function getNameMstZ010()
    {
        $query = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学期の取得
    public function getSemesterMst($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER ";
        return $query;
    }

    //校種一覧取得
    public function getSchoolKindList($model, $flg)
    {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     NAME1 AS VALUE,  ";
        } else {
            $query .= "     NAME1 AS VALUE, ";
            $query .= "     ABBV1 AS LABEL, ";
        }
        $query .= "     ABBV3  ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->selectSchoolKind) {
            $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     ABBV3 ";

        return $query;
    }

    //印刷学期
    public function getPrintSemester($model)
    {
        $query  = " SELECT ";
        $query .= "   NM.NAME1 AS LABEL, ";
        $query .= "   NM.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "   V_NAME_MST NM ";
        $query .= " WHERE ";
        $query .= "   NM.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "   NM.NAMECD1 = 'Z009' AND ";
        $query .= "   NM.NAMECD2 <> '9' ";
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
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " INNER JOIN SCHREG_REGD_DAT T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T3.GRADE = T1.GRADE ";
        $query .= "     AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " INNER JOIN SCHREG_BASE_MST T4 ";
        $query .= "     ON T4.SCHREGNO = T3.SCHREGNO ";
        $query .= "     AND T4.HANDICAP = '002' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '2006' AND ";
        $query .= "     T1.SEMESTER = '1' AND ";
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

    //年組取得（評価する学年）
    public function getHrClassEvaluation($model)
    {
        $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);

        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || ':' || GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE <= '{$grade}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

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
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->HyoukaCount == 0) {
            $query .= "     NAMECD1 = 'D029' ";
        } else {
            $query .= "     NAMECD1 = 'DP29' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //名称マスタの取得(小学部カウント)
    public function getKantenHyoukaCount($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "     NAMECD1 = 'DP28' ";
        } else {
            $query .= "     NAMECD1 = 'DP29' ";
        }

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

    //下学年のデータカウント取得
    public function getTargetgradecount($model)
    {
        if ($model->field["SUBCLASSCD_EVALUATION"]) {
            $target_grade            = $model->field["GRADE_EVALUATION"] ;
            $target_classcd          = substr($model->field["SUBCLASSCD_EVALUATION"], 0, 2);
            $target_school_kind      = substr($model->field["SUBCLASSCD_EVALUATION"], 3, 1);
            $target_curriculum_cd    = substr($model->field["SUBCLASSCD_EVALUATION"], 5, 1);
            $target_subclasscd       = substr($model->field["SUBCLASSCD_EVALUATION"], 7, 6);
        } else {
            $target_grade            = substr($model->field["GRADE_HR_CLASS"], 0, 2);
            $target_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $target_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $target_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $target_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_TARGETGRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO = '".$model->field["SCHREGNO"]."' AND ";
        $query .= "     CLASSCD        = '".$substr_classcd."' AND ";
        $query .= "     SCHOOL_KIND    = '".$substr_school_kind."' AND ";
        $query .= "     CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD     = '".$substr_subclasscd."' AND ";
        $query .= "     TARGET_GRADE     = '".$target_grade."' AND ";
        $query .= "     TARGET_CLASSCD        = '".$target_classcd."' AND ";
        $query .= "     TARGET_SCHOOL_KIND    = '".$target_school_kind."' AND ";
        $query .= "     TARGET_CURRICULUM_CD  = '".$target_curriculum_cd."' AND ";
        $query .= "     TARGET_SUBCLASSCD     = '".$target_subclasscd."' ";

        return $query;
    }

    //編集可能項目の判別
    public function selectViewcdQuery($model)
    {

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->field["SUBCLASSCD_EVALUATION"]) {
                $substr_classcd          = substr($model->field["SUBCLASSCD_EVALUATION"], 0, 2);
                $substr_school_kind      = substr($model->field["SUBCLASSCD_EVALUATION"], 3, 1);
                $substr_curriculum_cd    = substr($model->field["SUBCLASSCD_EVALUATION"], 5, 1);
                $substr_subclasscd       = substr($model->field["SUBCLASSCD_EVALUATION"], 7, 6);
            } else {
                $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
                $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
                $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
                $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
            }
        }
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     JVIEWNAME_GRADE_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SUBCLASSCD_EVALUATION"]) {
            $query .= "    T1.GRADE = '".$model->field["GRADE_EVALUATION"]."' AND ";
        } else {
            $query .= "    T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' AND ";
        }
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
            if ($model->field["SUBCLASSCD_EVALUATION"]) {
                $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD_EVALUATION"]."' AND ";
            } else {
                $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
            }
            $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        }
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$substr_classcd."' ";
        } else {
            if ($model->field["SUBCLASSCD_EVALUATION"]) {
                $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".substr($model->field["SUBCLASSCD_EVALUATION"], 0, 2)."' ";
            } else {
                $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    //各生徒毎の観点を取得
    public function getSchJviewCd($model, $grade, $subclasscd)
    {
        $query   = "  SELECT ";
        $query  .= "    LISTAGG(T2.VIEWCD, ',') WITHIN GROUP(ORDER BY T2.VIEWCD) AS VIEW_KEYCD ";
        $query  .= "  FROM ";
        $query  .= "    JVIEWNAME_GRADE_YDAT T1 ";
        $query  .= "    LEFT JOIN JVIEWNAME_GRADE_MST T2 ";
        $query  .= "      ON T2.GRADE = T1.GRADE ";
        $query  .= "      AND T2.CLASSCD = T1.CLASSCD ";
        $query  .= "      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query  .= "      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query  .= "      AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query  .= "      AND T2.VIEWCD = T1.VIEWCD ";
        $query  .= "      AND T2.GRADE = T1.GRADE ";
        $query  .= "      AND T2.GRADE = T1.GRADE ";
        $query  .= "  WHERE ";
        $query  .= "    T1.YEAR                 = '".CTRL_YEAR."' ";
        $query  .= "    AND T1.GRADE            = '{$grade}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  .= "    AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclasscd}' ";
        } else {
            $query  .= "    AND T1.SUBCLASSCD = '{$subclasscd}' ";
        }

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
        $query .= "     JVIEWSTAT_RECORD_TARGETGRADE_DAT ";
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
            $viewarry = array();
            foreach ($view_key as $code => $col) {
                if ($col != "") {
                    $viewarry[] = $col;
                }
            }
            $query .= "     AND TARGET_VIEWCD in('".implode("','", $viewarry)."') ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND substr(TARGET_VIEWCD,1,2) = '".$substr_classcd."' ";
        } else {
            $query .= "     AND substr(TARGET_VIEWCD,1,2) = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
        }

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $view_key, $check)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
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
        $query .= "        T2.SEMESTER  = '".CTRL_SEMESTER."' AND ";
        $query .= "        T2.SCHREGNO  = T3.SCHREGNO AND ";
        $query .= "        T3.HANDICAP  = '002' ";
        if ($check == false) {
            if ($model->field["SCHREGNO"]) {
                $query .= "      AND T3.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
            }
        }
        $query .= " ), VIEWSTAT AS ( ";
        $query  .= "  SELECT ";
        $query  .= "    SCHREGNO, ";
        $query  .= "    LISTAGG(TARGET_VIEWCD || '-' || STATUS, ',') WITHIN GROUP(ORDER BY TARGET_VIEWCD) AS VIEW_KEY_STATUS, ";
        $query  .= "    TARGET_GRADE, ";
        $query  .= "    TARGET_CLASSCD, ";
        $query  .= "    TARGET_SCHOOL_KIND, ";
        $query  .= "    TARGET_CURRICULUM_CD, ";
        $query  .= "    TARGET_SUBCLASSCD ";
        $query  .= "  FROM ";
        $query  .= "    JVIEWSTAT_RECORD_TARGETGRADE_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".CTRL_YEAR."' ";
        $query  .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        AND CLASSCD        = '".$substr_classcd."' ";
            $query .= "        AND SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "        AND CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "        AND SUBCLASSCD     = '".$substr_subclasscd."' ";
        } else {
            $query .= "        SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query  .= "  GROUP BY ";
        $query  .= "    SCHREGNO, ";
        $query  .= "    TARGET_GRADE, ";
        $query  .= "    TARGET_CLASSCD, ";
        $query  .= "    TARGET_SCHOOL_KIND, ";
        $query  .= "    TARGET_CURRICULUM_CD, ";
        $query  .= "    TARGET_SUBCLASSCD ";
        $query .= "    ) ";

        //-----メイン-----//
        $query .= "SELECT ";
        if ($check) {
            $query .= "    T1.SCHREGNO AS VALUE,";
            $query .= "    T1.NAME_SHOW AS LABEL ";
        } else {
            $query .= "    T1.HR_NAME, ";
            $query .= "    T1.GRADE, ";
            $query .= "    T1.HR_CLASS, ";
            $query .= "    T1.ATTENDNO, ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.NAME_SHOW, ";
            $query .= "    T2.TARGET_GRADE, ";
            $query .= "    T2.TARGET_CLASSCD || '-' || T2.TARGET_SCHOOL_KIND || '-' || T2.TARGET_CURRICULUM_CD || '-' || T2.TARGET_SUBCLASSCD AS TARGET_SUBCLASSCD, ";
            $query .= "    T2.VIEW_KEY_STATUS, ";
            $query .= "    T3.SCORE AS STATUS8, ";
            $query .= "    JREP.REMARK1 AS STATUS9 ";
        }
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN JVIEWSTAT_REPORTREMARK_DAT JREP ";
        $query .= "              ON JREP.YEAR          = '".CTRL_YEAR."' ";
        $query .= "             AND JREP.SEMESTER      = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND JREP.SCHREGNO      = T1.SCHREGNO ";
        $query .= "             AND JREP.CLASSCD       = '".$substr_classcd."' ";
        $query .= "             AND JREP.SCHOOL_KIND   = '".$substr_school_kind."' ";
        $query .= "             AND JREP.CURRICULUM_CD = '".$substr_curriculum_cd."' ";
        $query .= "             AND JREP.SUBCLASSCD    = '".$substr_subclasscd."' ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT T3 ";
        $query .= "       ON T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T3.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "      AND T3.TESTKINDCD = '99' ";
        $query .= "      AND T3.TESTITEMCD = '00' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9'
            && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                $query .= "             AND T3.SCORE_DIV    = '09' ";
            } else {
                $query .= "             AND T3.SCORE_DIV    = '08' ";
            }
        } else {
            $query .= "             AND T3.SCORE_DIV    = '00' ";
        }
        $query .= "      AND T3.CLASSCD = '".$substr_classcd."' ";
        $query .= "      AND T3.SCHOOL_KIND = '".$substr_school_kind."' ";
        $query .= "      AND T3.CURRICULUM_CD = '".$substr_curriculum_cd."' ";
        $query .= "      AND T3.SUBCLASSCD = '".$substr_subclasscd."' ";
        $query .= "      AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }

    //TARGET科目取得
    public function getTargetsubclasscd($model, $row, $code)
    {
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        $substr_viewcd           = $substr_classcd.'0'.$code;

        $query  = " SELECT ";
        $query .= "     TARGET_GRADE || '-' || TARGET_CLASSCD || '-' || TARGET_SCHOOL_KIND || '-' || TARGET_CURRICULUM_CD || '-' || TARGET_SUBCLASSCD AS SUBCLASSCD";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_TARGETGRADE_DAT ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "      AND CLASSCD = '".$substr_classcd."' ";
        $query .= "      AND SCHOOL_KIND = '".$substr_school_kind."' ";
        $query .= "      AND CURRICULUM_CD = '".$substr_curriculum_cd."' ";
        $query .= "      AND SUBCLASSCD = '".$substr_subclasscd."' ";
        $query .= "      AND SCHREGNO = '".$row."' ";
        $query .= "      AND TARGET_VIEWCD = '".$substr_viewcd."' ";

        return $query;
    }

    //コンボ初期値を取得
    //TARGET科目取得
    public function getSchRow($model)
    {
        $query   = "  SELECT DISTINCT ";
        $query  .= "    TARGET_GRADE AS GRADE_EVALUATION, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  .= "    TARGET_CLASSCD || '-' || TARGET_SCHOOL_KIND || '-' || TARGET_CURRICULUM_CD || '-' || TARGET_SUBCLASSCD AS SUBCLASSCD_EVALUATION ";
        } else {
            $query  .= "    TARGET_SUBCLASSCD AS SUBCLASSCD_EVALUATION";
        }
        $query  .= "  FROM ";
        $query  .= "    JVIEWSTAT_RECORD_TARGETGRADE_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".CTRL_YEAR."' ";
        $query  .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query  .= "    AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query  .= "    AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //評定・所見取得
    public function getRowStatus($model, $code)
    {
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);

        $query  = " SELECT ";
        if ($code == '8') {
            $query .= "    T3.SCORE AS STATUS8 ";
        } else {
            $query .= "    JREP.REMARK1 AS STATUS9 ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_REPORTREMARK_DAT JREP ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT T3 ";
        $query .= "       ON JREP.YEAR          = T3.YEAR ";
        $query .= "      AND JREP.SEMESTER      = T3.SEMESTER ";
        $query .= "      AND JREP.SCHREGNO      = T3.SCHREGNO ";
        $query .= "      AND JREP.CLASSCD       = T3.CLASSCD ";
        $query .= "      AND JREP.SCHOOL_KIND   = T3.SCHOOL_KIND ";
        $query .= "      AND JREP.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "      AND JREP.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query .= "      AND T3.TESTKINDCD = '99' ";
        $query .= "      AND T3.TESTITEMCD = '00' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9'
            && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                $query .= "             AND T3.SCORE_DIV    = '09' ";
            } else {
                $query .= "             AND T3.SCORE_DIV    = '08' ";
            }
        } else {
            $query .= "             AND T3.SCORE_DIV    = '00' ";
        }
        $query .= " WHERE ";
        $query .= "      JREP.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND JREP.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "      AND JREP.CLASSCD = '".$substr_classcd."' ";
        $query .= "      AND JREP.SCHOOL_KIND = '".$substr_school_kind."' ";
        $query .= "      AND JREP.CURRICULUM_CD = '".$substr_curriculum_cd."' ";
        $query .= "      AND JREP.SUBCLASSCD = '".$substr_subclasscd."' ";
        $query .= "      AND JREP.SCHREGNO = '".$model->field["SCHREGNO"]."' ";

        return $query;
    }


    public function getReportCondition($model)
    {
        $query  = " SELECT ";
        $query .= "   N1.NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "   HREPORT_CONDITION_DAT T1 ";
        $query .= "   LEFT JOIN NAME_MST N1 ";
        $query .= "     ON N1.NAMECD1 = 'A035' ";
        $query .= "    AND N1.NAMECD2 = T1.REMARK1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "   AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "   AND T1.GRADE = '00' ";
        $query .= "   AND T1.COURSECD = '0' ";
        $query .= "   AND T1.MAJORCD = '000' ";
        $query .= "   AND T1.COURSECODE = '0000' ";
        $query .= "   AND T1.SEQ = '202' ";

        return $query;
    }

    public function getTransfer($model, $schregNo)
    {
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);

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
        /*
        if($model->field["SUBCLASSCD_EVALUATION"]){
            $target_grade            = $model->field["GRADE_EVALUATION"];
            $target_classcd          = substr($model->field["SUBCLASSCD_EVALUATION"], 0, 2);
            $target_school_kind      = substr($model->field["SUBCLASSCD_EVALUATION"], 3, 1);
            $target_curriculum_cd    = substr($model->field["SUBCLASSCD_EVALUATION"], 5, 1);
            $target_subclasscd       = substr($model->field["SUBCLASSCD_EVALUATION"], 7, 6);
        } else {
            $target_grade            = substr($model->field["GRADE_HR_CLASS"], 0, 2);
            $target_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $target_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $target_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $target_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        */

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i=0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];
            $target_grade = $model->data[$schregno]["TARGET_GRADE"];
            $targetTotalSubCd = $model->data[$schregno]["TARGET_TOTAL_SUBCLASSCD"];
            if ($model->Properties["useCurriculumcd"] == '1') {
                $target_classcd          = substr($targetTotalSubCd, 0, 2);
                $target_school_kind      = substr($targetTotalSubCd, 3, 1);
                $target_curriculum_cd    = substr($targetTotalSubCd, 5, 1);
                $target_subclasscd       = substr($targetTotalSubCd, 7, 6);
            } else {
                $target_subclasscd       = $targetTotalSubCd;
            }

            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_RECORD_TARGETGRADE_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR     = '".CTRL_YEAR."' ";
            $query .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "   AND SCHREGNO = '".$schregno."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$substr_classcd."-".$substr_school_kind."-".$substr_curriculum_cd."-".$substr_subclasscd."' ";
            } else {
                $query .= "    AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $db->query($query);

            foreach ($model->data[$schregno]["STATUS"] as $code => $viewcd) {
                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $schregno;
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]              = $substr_classcd;
                    $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
                    $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                }
                $data["TARGET_VIEWCD"][TEXT]         = $viewcd;
                $data["TARGET_GRADE"][TEXT]          = $target_grade;
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["TARGET_CLASSCD"][TEXT]        = $target_classcd;
                    $data["TARGET_SCHOOL_KIND"][TEXT]    = $target_school_kind;
                    $data["TARGET_CURRICULUM_CD"][TEXT]  = $target_curriculum_cd;
                }
                $data["TARGET_SUBCLASSCD"][TEXT]     = $target_subclasscd;
                if (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] && $model->fields["STATUS".$code][$i] == "") ||
                    ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] && $model->fields["STATUS".$code][$i] == "")) {
                    $data["STATUS"][TEXT]       = $model->nonVisualViewCd;
                } elseif (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] && $model->fields["STATUS".$code][$i] == "F") ||
                           ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] && $model->fields["STATUS".$code][$i] == "F")) {
                    $data["STATUS"][TEXT]       = "";
                } else {
                    $data["STATUS"][TEXT]       = $model->fields["STATUS".$code][$i];
                }
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_TARGETGRADE_DAT");
                $db->query($query);
            }
        }
        $db->commit();
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
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //評定を更新
            //RECORD_SCORE_DAT
            if ($model->code8InputFlg == "1") {
                knjd126wQuery::updateRecordScore($db, $model, $i);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //JVIEWSTAT_REPORTREMARK_DATカウント
    public function countJviewStatReportRemardDat($model, $i)
    {
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_REPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR          = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER      = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO      = '".$model->data["SCHREGNO"][$i]."' ";
        $query .= "     AND CLASSCD       = '".$substr_classcd."' ";
        $query .= "     AND SCHOOL_KIND   = '".$substr_school_kind."' ";
        $query .= "     AND CURRICULUM_CD = '".$substr_curriculum_cd."' ";
        $query .= "     AND SUBCLASSCD    = '".$substr_subclasscd."' ";

        return $query;
    }

    /* 成績データ更新処理 */
    public function updateRecordScore($db, $model, $i)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }

        $code = "8";

        //RECORD_SCORE_DATの追加
        knjd126wQuery::insertRecordScore($db, $model, $i);

        //数字に変換
        $status = $model->fields["STATUS".$code][$i];
        if ($model->Properties["useHyoukaHyouteiFlg"] != "1" && $model->electdiv != 0) {
            if ($status == "A") {
                $status = "11";
            }
            if ($status == "B") {
                $status = "22";
            }
            if ($status == "C") {
                $status = "33";
            }
        }

        //評定をセット
        $data = array();
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $data["SCORE"][NUMBER]  = $status;
        } else {
            $data["VALUE"][NUMBER]  = $status;
        }
        $data["VALUE_DI"][TEXT] = "";

        //条件
        $where  = "WHERE   YEAR         = '".CTRL_YEAR."' ";
        $where .= "  AND   SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $where .= "  AND   TESTKINDCD   = '99' ";
        $where .= "  AND   TESTITEMCD   = '00' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9'
            && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                $where .= "  AND   SCORE_DIV    = '09' ";
            } else {
                $where .= "  AND   SCORE_DIV    = '08' ";
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

        //更新
        $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);

        $db->query($query);
    }

    // RECORD_SCORE_DATの追加
    public function insertRecordScore($db, $model, $i)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        // RECORD_SCORE_DATの存在チェック
        $query  = "SELECT  COUNT(*) ";
        $query .= "FROM    RECORD_SCORE_DAT ";
        $query .= "WHERE   YEAR         = '".CTRL_YEAR."' ";
        $query .= "  AND   SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "  AND   TESTKINDCD   = '99' ";
        $query .= "  AND   TESTITEMCD   = '00' ";

        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9'
            && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                $query .= "  AND   SCORE_DIV    = '09' ";
            } else {
                $query .= "  AND   SCORE_DIV    = '08' ";
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
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                if ($model->field["SEMESTER"] == '9'
                && $model->Properties["useHyoukaHyouteiFlg"] != '1') {
                    $data["SCORE_DIV"][TEXT]    = "09";
                } else {
                    $data["SCORE_DIV"][TEXT]    = "08";
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

    //削除処理
    public function delete($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i=0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_RECORD_TARGETGRADE_DAT ";
            $query .= "WHERE ";
            $query .= "        YEAR     = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "    AND SCHREGNO = '".$model->data["SCHREGNO"][$i]."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND SUBCLASSCD = '".$substr_subclasscd."' ";
            } else {
                $query .= "    AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
