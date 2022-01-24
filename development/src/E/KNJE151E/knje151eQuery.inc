<?php

require_once('for_php7.php');

class knje151equery extends Query {

    //名称マスタの取得
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER2"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "   AND ( T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "      OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "      OR T1.TR_CD3 = '".STAFFCD."' ";
            $query .= "      OR T1.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "      OR T1.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "      OR T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科取得
    function getClassMst($model, $classcd="")
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || ' ' || T2.CLASSNAME AS LABEL, ";
        } else {
            $query .= "     T2.CLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || ' ' || T2.CLASSNAME AS LABEL, ";
        }
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     V_CLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       = T2.CLASSCD  AND ";
            $query .= "     T1.SCHOOL_KIND   = T2.SCHOOL_KIND  ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        }
        if($classcd){
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD          = '".$class_array[0]."' ";
                $query .= "     AND T1.SCHOOL_KIND      = '".$class_array[1]."' ";
            } else {
                $query .= "     AND T2.CLASSCD = '".$classcd."' ";
            }
        }
        if($model->field["GRADE_HR_CLASS"]){
            $query .= "     AND EXISTS (SELECT 'X' ";
            $query .= "             FROM ";
            $query .= "                 JVIEWNAME_SUB_MST T3, ";
            $query .= "                 SCHREG_REGD_GDAT T4 ";
            $query .= "             WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                 T1.CLASSCD = T3.CLASSCD AND ";
                $query .= "                 T1.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
                $query .= "                 T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
            }
            $query .= "                 T1.SUBCLASSCD = T3.SUBCLASSCD AND ";
            $query .= "                 T1.VIEWCD = T3.VIEWCD AND ";
            $query .= "                 T1.YEAR = T4.YEAR AND ";
            $query .= "                 T3.SCHOOL_KIND = T4.SCHOOL_KIND AND ";
            $query .= "                 T4.GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2)) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //科目取得
    function getSubclassMst($model, $classcd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
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
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD = T2.CLASSCD  AND ";
            $query .= "    T1.SCHOOL_KIND = T2.SCHOOL_KIND  AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD  AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //編集可能項目の判別
    function selectViewcdQuery($model){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind   = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd    = substr($model->field["SUBCLASSCD"], 7, 6);
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }

        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     JVIEWNAME_SUB_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$substr_classcd."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$substr_school_kind."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$substr_curriculum_cd."' AND ";
            $query .= "    T1.SUBCLASSCD    = '".$substr_subclasscd."' AND ";
            $query .= "    T1.CLASSCD = T2.CLASSCD  AND ";
            $query .= "    T1.SCHOOL_KIND = T2.SCHOOL_KIND  AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD  AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        if($model->field["GRADE_HR_CLASS"]){
            $query .= "   AND T2.SCHOOL_KIND IN(SELECT ";
            $query .= "                             SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GDAT ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '".CTRL_YEAR."'  AND ";
            $query .= "                             GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2)) ";
        }
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $view_key)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind   = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd    = substr($model->field["SUBCLASSCD"], 7, 6);
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
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
        $query .= "        SCHREG_REGD_FI_HDAT T1, ";
        $query .= "        SCHREG_REGD_FI_DAT T2, ";
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
        foreach ($view_key as $code => $col)
        {
            $query .= "    ,MAX(CASE WHEN T2.VIEWCD = '".$col."' THEN T2.STATUS ELSE NULL END) AS STATUS".$code;
        }
        $query .= "     FROM ";
        $query .= "         SCHINFO T1, ";
        $query .= "         JVIEWSTAT_SUB_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "        T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "        T1.SCHREGNO = T2.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T2.CLASSCD       = '".$substr_classcd."' AND ";
            $query .= "        T2.SCHOOL_KIND   = '".$substr_school_kind."' AND ";
            $query .= "        T2.CURRICULUM_CD = '".$substr_curriculum_cd."' AND ";
            $query .= "        T2.SUBCLASSCD    = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "        T2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    T2.VIEWCD IN ('".implode("','", $view_key)."') AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T2.VIEWCD, 1, 2) = '".$class_array[0]."' ";
        } else {
            $query .= "    SUBSTR(T2.VIEWCD, 1, 2) = '".$model->field["CLASSCD"]."' ";
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
        foreach ($view_key as $code => $col)
        {
            if (substr($col,2) != "99") {
                $query .= ",T2.STATUS".$code;
            } else {
                $query .= ",T3.VALUATION AS STATUS".$code;
            }
        }
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_STUDYREC_DAT T3 ";
        $query .= "              ON T3.SCHOOLCD     = '0' ";
        $query .= "             AND T3.YEAR         = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T3.CLASSCD       = '".$substr_classcd."' ";
            $query .= "             AND T3.SCHOOL_KIND   = '".$substr_school_kind."' ";
            $query .= "             AND T3.CURRICULUM_CD = '".$substr_curriculum_cd."' ";
            $query .= "             AND T3.SUBCLASSCD    = '".$substr_subclasscd."' ";
        } else {
            $query .= "             AND T3.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "             AND T3.SCHREGNO     = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }

    //更新
    function update($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind   = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd    = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        for($i=0; $i < get_count($model->data["SCHREGNO"]); $i++){

            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_SUB_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "    SCHREGNO = '".$model->data["SCHREGNO"][$i]."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD       = '".$substr_classcd."' AND ";
                $query .= "    SCHOOL_KIND   = '".$substr_school_kind."' AND ";
                $query .= "    CURRICULUM_CD = '".$substr_curriculum_cd."' AND ";
                $query .= "    SUBCLASSCD    = '".$substr_subclasscd."' ";
            } else {
                $query .= "    SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $db->query($query);

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定は更新しない
                if (substr($col,2) == "99") continue;

                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = $substr_classcd;
                    $data["SCHOOL_KIND"][TEXT]      = $substr_school_kind;
                    $data["CURRICULUM_CD"][TEXT]    = $substr_curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]       = $substr_subclasscd;
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                }
                $data["VIEWCD"][TEXT]       = $col;
                $data["STATUS"][TEXT]       = $model->fields["STATUS".$code][$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWSTAT_SUB_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /* 成績データ更新処理 */
    function updateRecord($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind   = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd    = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            //年次の取得
            $query  = " SELECT ";
            $query .= "     ANNUAL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_FI_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$model->data["SCHREGNO"][$i]."' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER    = '".CTRL_SEMESTER."' ";
            $annual = $db->getOne($query);

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定のみ更新
                if (substr($col,2) != "99") continue;

                //数字に変換
                $status = $model->fields["STATUS".$code][$i];
                if ($status == "A") $status = "11";
                if ($status == "B") $status = "22";
                if ($status == "C") $status = "33";

                //削除
                $query  = "DELETE FROM SCHREG_STUDYREC_DAT ";
                $query .= "WHERE   SCHOOLCD     = '0' ";
                $query .= "  AND   YEAR         = '".CTRL_YEAR."' ";
                $query .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "  AND   CLASSCD       = '".$substr_classcd."' ";
                    $query .= "  AND   SCHOOL_KIND   = '".$substr_school_kind."' ";
                    $query .= "  AND   CURRICULUM_CD = '".$substr_curriculum_cd."' ";
                    $query .= "  AND   SUBCLASSCD    = '".$substr_subclasscd."' ";
                } else {
                    $query .= "  AND   SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
                }
                $db->query($query);

                //追加
                $data = array();
                $data["SCHOOLCD"][TEXT]     = "0";
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                $data["ANNUAL"][TEXT]       = $annual;
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = $substr_classcd;
                    $data["SCHOOL_KIND"][TEXT]      = $substr_school_kind;
                    $data["CURRICULUM_CD"][TEXT]    = $substr_curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]       = $substr_subclasscd;
                } else {
                    $data["CLASSCD"][TEXT]      = $model->field["CLASSCD"];
                    $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                }
                $data["VALUATION"][NUMBER]  = $status;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "SCHREG_STUDYREC_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }
}
?>
