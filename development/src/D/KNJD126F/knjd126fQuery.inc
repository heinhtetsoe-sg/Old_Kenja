<?php

require_once('for_php7.php');

class knjd126fquery extends Query {

    //名称マスタの取得
    function selectNamemstQuery($model)
    {
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query  = "SELECT namecd2, name1 ";
        $query .= "  FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND namecd1='{$setNameCd}' ";
        $query .= "ORDER BY ";
        $query .= "    namecd2 ";
        return $query;
    }
    
    //教科データの取得
    function selectSubclassQuery($model)
    {
        $query  = "SELECT ";
        $query .= "    T3.classcd, ";
        $query .= "    T3.SCHOOL_KIND, ";
        $query .= "    T3.classname, ";
        $query .= "    value(T3.electdiv,'0') as electdiv ";
        $query .= "FROM ";
        $query .= "    chair_dat T1, ";
        $query .= "    chair_std_dat T2, ";
        $query .= "    class_mst T3, ";
        $query .= "    chair_stf_dat T4 ";
        $query .= "WHERE ";
        $query .= "    T1.year       = '".CTRL_YEAR."' ";
        $query .= "    AND T1.semester   = '".$model->field["SEMESTER2"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "    AND T2.year       = T1.year ";
        $query .= "    AND T1.year       = T4.year ";
        $query .= "    AND T1.semester   = T4.semester ";
        $query .= "    AND T1.chaircd    = T4.chaircd ";
        $query .= "    AND T2.semester   = T1.semester ";
        $query .= "    AND T2.chaircd    = T1.chaircd ";
        $query .= "    AND T3.classcd NOT LIKE '9%' ";
        $query .= "    AND T3.classcd IN (SELECT substr(J1.viewcd,1,2) FROM jviewname_grade_ydat J1 WHERE J1.year = '".CTRL_YEAR."' GROUP BY substr(J1.viewcd,1,2)) ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND t4.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "    AND T3.CLASSCD       = T1.CLASSCD ";
        $query .= "    AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND  ";
        $query .= "GROUP BY ";
        $query .= "    T3.classcd, ";
        $query .= "    T3.SCHOOL_KIND, ";
        $query .= "    T3.classname, ";
        $query .= "    value(T3.electdiv,'0') ";
        $query .= "ORDER BY ";
        $query .= "    T3.CLASSCD, ";
        $query .= "    T3.SCHOOL_KIND ";
        return $query;
    }

    //科目取得
    function getSubclassMst($model)
    {
        $class_array = array();
        $class_array = explode("-", $model->field["CLASSCD"]);
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
        $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "     T1.CLASSCD          = '".$class_array[0]."' AND ";
        $query .= "     T1.SCHOOL_KIND      = '".$class_array[1]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
        
    //講座データの取得
    function selectChairQuery($model)
    {
        $class_array = array();
        $class_array = explode("-", $model->field["CLASSCD"]);
        $query  = " SELECT DISTINCT ";
        $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASS_VALUE, ";
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
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        $query .= "    substr(T1.subclasscd,1,2) = '".$class_array[0]."' AND ";
        $query .= "    T1.SCHOOL_KIND            = '".$class_array[1]."'  ";
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //専門FLG取得
    function getSenmonFlg($subclassCd)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SENMON_FLG ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_GRADE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclassCd}' ";
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

    //編集可能項目の判別1
    function selectContolCodeQuery($model){

        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
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

    //編集可能項目の判別2
    function selectViewcdQuery($model, $execute_date, $checkKeitai){
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教科対応
        $class_array = array();
        $class_array = explode("-", $model->field["CLASSCD"]);
        //科目対応
        $subclass_array = array();
        $subclass_array = explode("-", $chaircd_subclass_array[1]);
        $query  = "WITH MAX_GRADE AS ( ";
        $query .= "    SELECT ";
        $query .= "        max(t2.grade) as grade ";
        $query .= "    FROM ";
        $query .= "        SCHREG_REGD_FI_HDAT t1, ";
        $query .= "        SCHREG_REGD_FI_DAT t2, ";
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
        $query .= "    t1.viewcd, ";
        $query .= "    t2.viewname, ";
        $query .= "    L1.REMARK1 ";
        $query .= "FROM ";
        $query .= "    jviewname_grade_ydat t1, ";
        $query .= "    jviewname_grade_mst t2 ";
        $query .= "    INNER JOIN JVIEWNAME_GRADE_DETAIL_MST L1 ON T2.GRADE = L1.GRADE ";
        $query .= "          AND T2.CLASSCD = L1.CLASSCD ";
        $query .= "          AND T2.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND T2.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "          AND T2.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= "          AND T2.VIEWCD = L1.VIEWCD ";
        $query .= "          AND L1.VIEW_SEQ = '00{$model->field["SEMESTER2"]}' ";
        $query .= "          AND L1.REMARK1 IS NOT NULL ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_DETAIL_MST L2 ON L1.GRADE = L2.GRADE ";
        $query .= "                                            AND L1.CLASSCD = L2.CLASSCD ";
        $query .= "                                            AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ";
        $query .= "                                            AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        $query .= "                                            AND L1.SUBCLASSCD = L2.SUBCLASSCD ";
        $query .= "                                            AND L1.VIEWCD = L2.VIEWCD ";
        $query .= "                                            AND L2.VIEW_SEQ = '004' ";
        $query .= "                                            AND L2.REMARK1 = '{$checkKeitai["RECORD_DIV"]}' ";
        $query .= "WHERE ";
        $query .= "    t1.year = '".CTRL_YEAR."' AND ";
        $query .= "    t1.grade = t2.grade AND ";
        $query .= "    t1.CLASSCD        = '".$subclass_array[0]."' AND ";
        $query .= "    t1.SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
        $query .= "    t1.CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
        $query .= "    t1.SUBCLASSCD     = '".$subclass_array[3]."' AND ";
        $query .= "    t1.CLASSCD       = t2.CLASSCD AND ";
        $query .= "    t1.SCHOOL_KIND   = t2.SCHOOL_KIND AND ";
        $query .= "    t1.CURRICULUM_CD = t2.CURRICULUM_CD AND ";
        $query .= "    t1.SUBCLASSCD    = t2.SUBCLASSCD AND ";
        $query .= "    t1.viewcd = t2.viewcd AND ";
        $query .= "    SUBSTR(t1.VIEWCD,1,2) = '".$class_array[0]."' ";
        $query .= "    AND t1.grade in (SELECT w1.grade FROM MAX_GRADE w1) ";
        $query .= "ORDER BY ";
        $query .= "    t1.viewcd ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date, $view_key, $selectDiv = "")
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教科対応
        $class_array = array();
        $class_array = explode("-", $model->field["CLASSCD"]);
        //科目対応
        $subclass_array = array();
        $subclass_array = explode("-", $chaircd_subclass_array[1]);
        $query  = "WITH VIEWSTAT AS ( ";
        $query .= "    SELECT ";
        $query .= "        schregno ";
        // "1" => "1101"
        foreach ($view_key as $code => $col)
        {
            $query .= "    ,max(case when viewcd = '".$col."' then status else null end) as status".$code;
        }
        $query .= "    FROM ";
        $query .= "        JVIEWSTAT_RECORD_DAT ";
        $query .= "    WHERE ";
        $query .= "        year = '".CTRL_YEAR."' AND ";
        $query .= "        semester = '".$model->field["SEMESTER"]."' AND ";
        $query .= "        CLASSCD        = '".$subclass_array[0]."' AND ";
        $query .= "        SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
        $query .= "        CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
        $query .= "        SUBCLASSCD     = '".$subclass_array[3]."' AND ";
        //観点コード
        if (get_count($view_key) > 0) {
            $query .= "    viewcd in('".implode("','", $view_key)."') AND ";
        }
        $query .= "        SUBSTR(VIEWCD,1,2) = '".$class_array[0]."' ";
        $query .= "    GROUP BY schregno ";
        $query .= "    ) ";

        if ($selectDiv == "CNT") {
            $query .= " , SELECT_T AS( ";
        }

        //-----メイン-----//
        $query .= "SELECT ";
        $query .= "    tbl1.RECORD_DIV, tbl1.hr_name, tbl1.grade, tbl1.hr_class, tbl1.attendno, tbl1.schregno, tbl1.name_show ";
        foreach ($view_key as $code => $col) {
            if (substr($col,2) != "99") {
                $query .= ",tbl2.status".$code;
            } else {
                $query .= ",T3.SCORE AS STATUS".$code;
            }
        }
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        t1.RECORD_DIV, t1.hr_name, t2.grade, t2.hr_class, t2.attendno, t4.schregno, t3.name_show, ";
        $query .= "        L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "    FROM ";
        $query .= "        SCHREG_REGD_FI_HDAT t1, ";
        $query .= "        SCHREG_REGD_FI_DAT t2, ";
        $query .= "        schreg_base_mst t3, ";
        $query .= "        chair_std_dat t4 ";
        $query .= "        LEFT JOIN CHAIR_DAT L1 ON T4.YEAR = L1.YEAR ";
        $query .= "             AND T4.SEMESTER = L1.SEMESTER ";
        $query .= "             AND T4.CHAIRCD = L1.CHAIRCD ";
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
        $query .= "    ) tbl1 ";
        $query .= "    left join VIEWSTAT tbl2 on tbl2.schregno = tbl1.schregno ";
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
        $query .= "             AND T3.CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "             AND T3.SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "             AND T3.CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "             AND T3.SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= "             AND T3.SCHREGNO     = tbl1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     tbl1.SUBCLASSCD NOT IN ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "          W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ";
        $query .= "      FROM ";
        $query .= "          SUBCLASS_GRADE_DAT W1 ";
        $query .= "      WHERE ";
        $query .= "          W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND W1.GRADE = tbl1.GRADE ";
        $query .= "          AND W1.RECORD_DIV = tbl1.RECORD_DIV ";
        $query .= "          AND VALUE(W1.TEXT_HYOKA_FLG, '0') = '1') ";
        $query .= " ORDER BY ";
        $query .= "    tbl1.grade, tbl1.hr_class, tbl1.attendno ";
        if ($selectDiv == "CNT") {
            $query .= " ), MAIN_T AS ( ";
            $query .= " SELECT ";
            $query .= "    RECORD_DIV ";
            $query .= " FROM ";
            $query .= "    SELECT_T ";
            $query .= " GROUP BY ";
            $query .= "    RECORD_DIV ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "    MAX(RECORD_DIV) AS RECORD_DIV, ";
            $query .= "    COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "    MAIN_T ";
        }
        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //科目対応
        $subclass_array = array();
        $subclass_array = explode("-", $chaircd_subclass_array[1]);

        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_RECORD_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "    SCHREGNO = '".$model->data["SCHREGNO"][$i]."' AND ";
            $query .= "    CLASSCD        = '".$subclass_array[0]."' AND ";
            $query .= "    SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
            $query .= "    CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
            $query .= "    SUBCLASSCD     = '".$subclass_array[3]."' ";
            $db->query($query);

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定は更新しない
                if (substr($col,2) == "99") continue;

                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                $data["CLASSCD"][TEXT]              = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]        = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]           = $subclass_array[3];
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
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //科目対応
        $subclass_array = array();
        $subclass_array = explode("-", $chaircd_subclass_array[1]);
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定のみ更新
                if (substr($col,2) != "99") continue;

                //RECORD_SCORE_DATの追加
                knjd126fQuery::insertRecord($db, $model, $i);

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
                $where .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
                $where .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
                $where .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
                $where .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
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
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //科目対応
        $subclass_array = array();
        $subclass_array = explode("-", $chaircd_subclass_array[1]);
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
        $query .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
        $query .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
        $query .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
        $query .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
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
            $data["CLASSCD"][TEXT]              = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]        = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]           = $subclass_array[3];
            $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
            $data["CHAIRCD"][TEXT]      = $chaircd_subclass_array[0];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }
    }

}
?>
