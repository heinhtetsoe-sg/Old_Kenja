<?php

require_once('for_php7.php');

class knjd126equery extends Query {

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
        $query .= "     NAMECD1 = '".$namecd1."' ";
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
        $query .= "     SCHREG_REGD_FI_HDAT T1, ";
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //選択教科取得
    function getClassMst($subclasscd, $grade_hr_class, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $subclasscd);
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND AS VALUE, ";
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || ':' || T2.CLASSNAME AS LABEL, ";
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     V_CLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class,0,2)."' AND ";
        $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        if($subclasscd){
            $query .= "     AND T2.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "     AND T2.SCHOOL_KIND      = '".$subclass_array[1]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス形態取得
    function getClassKeitai($model)
    {
        $query  = " SELECT ";
        $query .= "     RECORD_DIV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER  = '".$model->field["SEMESTER2"]."' ";
        $query .= "     AND GRADE || HR_CLASS  = '".$model->field["GRADE_HR_CLASS"]."' ";

        return $query;
    }

    //科目取得
    function getSubclassMst($grade_hr_class, $model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || '：' || T2.SUBCLASSNAME AS LABEL, ";
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class,0,2)."' AND ";
        $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";

        $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD NOT IN ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "          CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ";
        $query .= "      FROM ";
        $query .= "          SUBCLASS_GRADE_DAT ";
        $query .= "      WHERE ";
        $query .= "          YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND GRADE = '".substr($grade_hr_class,0,2)."' ";
        $query .= "          AND RECORD_DIV = '{$model->classKeitai}' ";
        $query .= "          AND VALUE(TEXT_HYOKA_FLG, '0') = '1') ";

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタの取得
    function getKantenHyouka($model)
    {
        $query  = " SELECT ";
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
    function getAdminContol($model){

        $query  = " SELECT ";
        $query .= "     CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '3' ";
        return $query;
    }

    //編集可能項目の判別
    function selectViewcdQuery($model){
    
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        $setViewseq = sprintf("%03d", $model->field["SEMESTER2"]);
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     L1.REMARK1 AS DETAIL_VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     JVIEWNAME_GRADE_MST T2 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_DETAIL_MST L1 ON L1.GRADE = T2.GRADE ";
        $query .= "                                            AND L1.CLASSCD = T2.CLASSCD ";
        $query .= "                                            AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "                                            AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "                                            AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "                                            AND L1.VIEWCD = T2.VIEWCD ";
        $query .= "                                            AND L1.VIEW_SEQ = '".$setViewseq."' ";
        $query .= "                                            AND L1.REMARK1 IS NOT NULL ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_DETAIL_MST L2 ON L1.GRADE = L2.GRADE ";
        $query .= "                                            AND L1.CLASSCD = L2.CLASSCD ";
        $query .= "                                            AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ";
        $query .= "                                            AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        $query .= "                                            AND L1.SUBCLASSCD = L2.SUBCLASSCD ";
        $query .= "                                            AND L1.VIEWCD = L2.VIEWCD ";
        $query .= "                                            AND L2.VIEW_SEQ = '004' ";
        $query .= "                                            AND L2.REMARK1 = '{$model->classKeitai}' ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"],0,2)."' AND ";
        $query .= "    T1.GRADE = T2.GRADE AND ";
        $query .= "    T1.CLASSCD        = '".$substr_classcd."' AND ";
        $query .= "    T1.SCHOOL_KIND    = '".$substr_school_kind."' AND ";
        $query .= "    T1.CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
        $query .= "    T1.SUBCLASSCD     = '".$substr_subclasscd."' AND ";
        $query .= "    T1.CLASSCD       = T2.CLASSCD AND ";
        $query .= "    T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
        $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "    T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$substr_classcd."' ";
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $view_key)
    {
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);

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
        $query .= "         JVIEWSTAT_RECORD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "        T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "        T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "        T2.CLASSCD        = '".$substr_classcd."' AND ";
        $query .= "        T2.SCHOOL_KIND    = '".$substr_school_kind."' AND ";
        $query .= "        T2.CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
        $query .= "        T2.SUBCLASSCD     = '".$substr_subclasscd."' AND ";
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    T2.VIEWCD IN ('".implode("','", $view_key)."') AND ";

        $query .= "        SUBSTR(T2.VIEWCD, 1, 2) = '".$substr_classcd."' ";
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
                $query .= ",T3.SCORE AS STATUS".$code;
            }
        }
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT T3 ";
        $query .= "              ON T3.YEAR         = '".CTRL_YEAR."' ";
        $query .= "             AND T3.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND T3.TESTKINDCD   = '99' ";
        $query .= "             AND T3.TESTITEMCD   = '00' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "             AND T3.SCORE_DIV    = '09' ";
        } else {
            $query .= "             AND T3.SCORE_DIV    = '08' ";
        }
        $query .= "             AND T3.CLASSCD        = '".$substr_classcd."' ";
        $query .= "             AND T3.SCHOOL_KIND    = '".$substr_school_kind."' ";
        $query .= "             AND T3.CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
        $query .= "             AND T3.SUBCLASSCD     = '".$substr_subclasscd."' ";
        $query .= "             AND T3.SCHREGNO     = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }

    //更新
    function update($model)
    {
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        $db = Query::dbCheckOut();

        for($i=0; $i < get_count($model->data["SCHREGNO"]); $i++){

            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_RECORD_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "    SCHREGNO = '".$model->data["SCHREGNO"][$i]."' AND ";
            $query .= "    CLASSCD        = '".$substr_classcd."' AND ";
            $query .= "    SCHOOL_KIND    = '".$substr_school_kind."' AND ";
            $query .= "    CURRICULUM_CD  = '".$substr_curriculum_cd."' AND ";
            $query .= "    SUBCLASSCD     = '".$substr_subclasscd."' ";
            $db->query($query);

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定は更新しない
                if (substr($col,2) == "99") continue;

                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                $data["CLASSCD"][TEXT]              = $substr_classcd;
                $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
                $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
                $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
                $data["VIEWCD"][TEXT]       = $col;
                $data["STATUS"][TEXT]       = $model->fields["STATUS".$code][$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /* 成績データ更新処理 */
    function updateRecord($model)
    {
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定のみ更新
                if (substr($col,2) != "99") continue;

                //RECORD_SCORE_DATの追加
                knjd126eQuery::insertRecord($db, $model, $i);

                //数字に変換
                $status = $model->fields["STATUS".$code][$i];
                if ($status == "A") $status = "11";
                if ($status == "B") $status = "22";
                if ($status == "C") $status = "33";

                //評定をセット
                $data = array();
                $data["SCORE"][NUMBER]  = $status;
                $data["VALUE_DI"][TEXT] = "";

                //条件
                $where  = "WHERE   YEAR         = '".CTRL_YEAR."' ";
                $where .= "  AND   SEMESTER     = '".$model->field["SEMESTER"]."' ";
                $where .= "  AND   TESTKINDCD   = '99' ";
                $where .= "  AND   TESTITEMCD   = '00' ";
                if ($model->field["SEMESTER"] == '9') {
                    $where .= "             AND SCORE_DIV    = '09' ";
                } else {
                    $where .= "             AND SCORE_DIV    = '08' ";
                }
                $where .= "  AND   CLASSCD       = '".$substr_classcd."' ";
                $where .= "  AND   SCHOOL_KIND   = '".$substr_school_kind."' ";
                $where .= "  AND   CURRICULUM_CD = '".$substr_curriculum_cd."' ";
                $where .= "  AND   SUBCLASSCD    = '".$substr_subclasscd."' ";
                $where .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";

                //更新
                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }

    // RECORD_SCORE_DATの追加
    function insertRecord($db, $model, $i) {
        $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
        $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
        $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
        $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        // RECORD_SCORE_DATの存在チェック
        $query  = "SELECT  COUNT(*) ";
        $query .= "FROM    RECORD_SCORE_DAT ";
        $query .= "WHERE   YEAR         = '".CTRL_YEAR."' ";
        $query .= "  AND   SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "  AND   TESTKINDCD   = '99' ";
        $query .= "  AND   TESTITEMCD   = '00' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "             AND SCORE_DIV    = '09' ";
        } else {
            $query .= "             AND SCORE_DIV    = '08' ";
        }
        $query .= "  AND   CLASSCD       = '".$substr_classcd."' ";
        $query .= "  AND   SCHOOL_KIND   = '".$substr_school_kind."' ";
        $query .= "  AND   CURRICULUM_CD = '".$substr_curriculum_cd."' ";
        $query .= "  AND   SUBCLASSCD    = '".$substr_subclasscd."' ";
        $query .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";
        $record_cnt = $db->getOne($query);
        // RECORD_SCORE_DATがなかったら追加する
        if ($record_cnt == 0) {
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]   = "99";
            $data["TESTITEMCD"][TEXT]   = "00";
            if ($model->field["SEMESTER"] == '9') {
                $data["SCORE_DIV"][TEXT]    = "09";
            } else {
                $data["SCORE_DIV"][TEXT]    = "08";
            }
            $data["CLASSCD"][TEXT]              = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
            $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }
    }
}
?>
