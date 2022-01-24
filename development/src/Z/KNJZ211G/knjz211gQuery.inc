<?php

require_once('for_php7.php');

class knjz211gquery extends Query {
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
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->selectSchoolKind) {
            $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
        }
        $query .= "     AND NAME1 IN ('P','J', 'H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "      AND T2.SEMESTER = '".$model->semester."' ";
        $query .= "      AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    (    T2.TR_CD1 = '".STAFFCD."' ";
            $query .= "      OR T2.TR_CD2 = '".STAFFCD."' ";
            $query .= "      OR T2.TR_CD3 = '".STAFFCD."' ";
            $query .= "      OR T2.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "      OR T2.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "      OR T2.SUBTR_CD3 = '".STAFFCD."') AND ";
        }
        $query .= "     T1.SCHOOL_KIND IN ('P','J', 'H') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //選択教科取得
    function getClassMst($subclasscd, $grade, $model)
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
        $query .= "     T1.YEAR = '".$model->year."' AND ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        }
        if($subclasscd){
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
    function getNameMstD065($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'D065' AND ";
        $query .= "     NAME1   = '".$model->field["SUBCLASSCD"]."' ";

        return $query;
    }

    //名称マスタ(D085)の取得
    function getNameMstD085($model) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAME2, ";
        $query .= "     NAME3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'D085'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND NAME1 = '".$model->field["SCHOOL_KIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= "     AND '".$model->field["GRADE"]."' BETWEEN NAME2 AND NAME3";
        return $query;
    }

    //科目取得
    function getSubclassMst($grade, $model)
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
        $query .= "     T1.YEAR = '".$model->year."' AND ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
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
    function getKantenHyouka($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV1, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        if ($model->field["DIV"] == '3') {
            $query .= "     NAMECD1 = 'D028' ";
        } else {
            $query .= "     NAMECD1 = 'D029' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //管理者コントロール取得
    function getAdminContol($model){

        $query  = " SELECT ";
        $query .= "     CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '3' ";
        return $query;
    }

    //編集可能項目の判別
    function selectViewcdQuery($model){
    
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
        $query .= "    T1.YEAR = '".$model->year."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
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

    //全体のデータの取得
    function selectQuery($model, $cntflg=false)
    {
        $query  = " SELECT ";
        if ($cntflg) {
            $query .= "     COUNT(T1.*) AS CNT ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     ASSESS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ASSESSCD        = '".$model->field["DIV"]."' ";
        if (!$cntflg) {
            $query .= " ORDER BY ";
            $query .= "   T1.ASSESSLEVEL DESC ";
        }

        return $query;
    }

    function selectDetailQuery($model) {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        } else {
            $substr_subclasscd       = $model->field["SUBCLASSCD"];
        }
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_ASSESS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD = '".$substr_classcd."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".$substr_school_kind."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$substr_curriculum_cd."' ";
        }
        $query .= "     AND T1.SUBCLASSCD = '".$substr_subclasscd."' ";
        $query .= "     AND T1.JVIEW_DIV = '".$model->field["DIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "   ASSESSLEVEL DESC, ";
        $query .= "   JVIEW_CNT ";
        return $query;
    }

    //更新
    function update($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();


        //削除
        $query  = "DELETE FROM ";
        $query .= "    JVIEWNAME_ASSESS_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR      = '".$model->year."' AND ";
        $query .= "    GRADE     = '".$model->field["GRADE"]."' AND ";
        $query .= "    JVIEW_DIV = '".$model->field["DIV"]."' AND ";
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

        for($i=1; $i <= get_count($model->fields); $i++){
            foreach ($model->data["HYOUTEN"] as $code => $col) {
                if (!isset($model->fields["STATUS".$i][$code]) || $model->fields["STATUS".$i][$code] == "") {
                    continue;
                }
                //追加
                $data = array();
                $data["YEAR"][TEXT]      = $model->year;
                $data["GRADE"][TEXT]     = $model->field["GRADE"];
                $data["JVIEW_DIV"][TEXT] = $model->field["DIV"];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]              = $substr_classcd;
                    $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
                    $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                }
                $data["ASSESSLEVEL"][TEXT]       = $col;
                $data["JVIEW_CNT"][TEXT]         = $i;
                if (($model->Properties["useJviewStatus_NotHyoji_D029"] && $model->fields["STATUS".$i][$code] == "")) {
                    $status       = $model->nonVisualViewCd;
                } else {
                    $status       = $model->fields["STATUS".$i][$code];
                }
                if ($status == "A") $status = "11";
                if ($status == "B") $status = "22";
                if ($status == "C") $status = "33";
                $data["STATUS"][TEXT] = $status;
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWNAME_ASSESS_MST");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);

    }

}
?>
