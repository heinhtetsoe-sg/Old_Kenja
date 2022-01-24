<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd124kQuery.inc 66447 2019-03-22 04:33:54Z tawada $

class knjd124kquery extends Query {
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
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->selectSchoolKind) {
            $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタの取得
    function selectNamemstQuery($model)
    {
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."09";
        }
        $query  = "SELECT namecd2, name1 ";
        $query .= "  FROM V_NAME_MST name ";
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.SCHOOL_KIND, ";
        }
        $query .= "    T3.classname, ";
        $query .= "    value(T3.electdiv,'0') as electdiv ";
        $query .= "FROM ";
        $query .= "    chair_dat T1, ";
        $query .= "    chair_std_dat T2, ";
        $query .= "    class_mst T3 ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "   ,schreg_regd_hdat T4 ";
            $query .= "   ,schreg_regd_dat T5 ";
        }
        $query .= "WHERE ";
        $query .= "    T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "    T1.semester   = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "    T2.year       = T1.year AND ";
        $query .= "    T2.semester   = T1.semester AND ";
        $query .= "    T2.chaircd    = T1.chaircd AND ";
        $query .= "    T3.classcd NOT LIKE '9%' AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "    T1.year      = T4.year AND ";
            $query .= "    T1.semester  = T4.semester AND ";
            $query .= "    T4.year      = T5.year AND ";
            $query .= "    T4.semester  = T5.semester AND ";
            $query .= "    T4.grade     = T5.grade AND ";
            $query .= "    T4.hr_class  = T5.hr_class AND ";
            $query .= "    T5.schregno  = T2.schregno AND ";
            $query .= "    T5.year      = T2.year AND ";
            $query .= "    T5.semester  = T2.semester AND ";
            $query .= "   (T4.TR_CD1    = '".STAFFCD."' OR ";
            $query .= "    T4.TR_CD2    = '".STAFFCD."' OR ";
            $query .= "    T4.TR_CD3    = '".STAFFCD."' OR ";
            $query .= "    T4.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "    T4.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "    T4.SUBTR_CD3 = '".STAFFCD."') AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD       = T1.CLASSCD  AND ";
            $query .= "    T3.SCHOOL_KIND   = T1.SCHOOL_KIND  ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "    AND T3.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
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
    function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.chaircd, ";
        $query .= "     T1.chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2 ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "    ,schreg_regd_hdat T4 ";
            $query .= "    ,schreg_regd_dat T5 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "    T1.year      = T4.year AND ";
            $query .= "    T1.semester  = T4.semester AND ";
            $query .= "    T4.year      = T5.year AND ";
            $query .= "    T4.semester  = T5.semester AND ";
            $query .= "    T4.grade     = T5.grade AND ";
            $query .= "    T4.hr_class  = T5.hr_class AND ";
            $query .= "    T5.schregno  = T2.schregno AND ";
            $query .= "    T5.year      = T2.year AND ";
            $query .= "    T5.semester  = T2.semester AND ";
            $query .= "   (T4.TR_CD1    = '".STAFFCD."' OR ";
            $query .= "    T4.TR_CD2    = '".STAFFCD."' OR ";
            $query .= "    T4.TR_CD3    = '".STAFFCD."' OR ";
            $query .= "    T4.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "    T4.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "    T4.SUBTR_CD3 = '".STAFFCD."') AND ";
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

    //選択科目の判別
    function getElectdivQuery($model){

        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    chair_dat t1, ";
        $query .= "    subclass_mst t2 ";
        $query .= "WHERE ";
        $query .= "    year = '".CTRL_YEAR."' AND ";
        $query .= "    semester = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "    chaircd = '".$model->field["CHAIRCD"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    t1.CLASSCD       = t2.CLASSCD AND ";
            $query .= "    t1.SCHOOL_KIND   = t2.SCHOOL_KIND AND ";
            $query .= "    t1.CURRICULUM_CD = t2.CURRICULUM_CD AND ";
        }
        $query .= "    t1.subclasscd = t2.subclasscd AND ";
        $query .= "    electdiv = '1' ";
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
            $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        }
        $query .= "     CONTROL_FLG = '3' ";
      return $query;
    }

    //編集可能項目の判別2
    function selectViewcdQuery($model){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }

        $query  = "SELECT ";
        $query .= "    t1.viewcd, t2.viewname ";
        $query .= "FROM ";
        $query .= "    jviewname_ydat t1, jviewname_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year = '".CTRL_YEAR."' AND ";
        $query .= "    t1.viewcd = t2.viewcd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    substr(t1.viewcd,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    substr(t1.viewcd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    t1.viewcd ";
      return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date, $view_key)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }

        $query  = "WITH VIEWSTAT AS ( ";
        $query .= "    SELECT ";
        $query .= "        schregno ";
        // "1" => "1101"
        foreach ($view_key as $code => $col)
        {
            $query .= "    ,max(case when viewcd = '".$col."' then status else null end) as status".$code;
        }
        $query .= "    FROM ";
        $query .= "        jviewstat_dat ";
        $query .= "    WHERE ";
        $query .= "        year = '".CTRL_YEAR."' AND ";
        $query .= "        semester = '".$model->field["SEMESTER"]."' AND ";
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    viewcd in('".implode("','", $view_key)."') AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        substr(viewcd,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "        substr(viewcd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "    GROUP BY schregno ";
        $query .= "    ) ";

        //-----メイン-----//
        $query .= "SELECT ";
        $query .= "    tbl1.hr_name, tbl1.grade, tbl1.hr_class, tbl1.attendno, tbl1.schregno, tbl1.name_show ";
        // "1" => "1101"
        foreach ($view_key as $code => $col)
        {
            $query .= ",tbl2.status".$code;
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
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "   (t1.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "    t1.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "    t1.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "    t1.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "    t1.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "    t1.SUBTR_CD3 = '".STAFFCD."') AND ";
        }
        $query .= "        t2.year      = '".CTRL_YEAR."' AND ";
        $query .= "        t2.semester  = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "        t2.schregno  = t3.schregno AND ";
        $query .= "        t4.year      = t2.year AND ";
        //$query .= "        t4.semester  = t2.semester AND ";
        $query .= "        t4.chaircd   = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "        t4.schregno  = t2.schregno AND ";
        $query .= "        '".$execute_date."' BETWEEN t4.appdate AND t4.appenddate ";
        $query .= "    ) tbl1 ";
        $query .= "    left join VIEWSTAT tbl2 on tbl2.schregno = tbl1.schregno ";
        $query .= "ORDER BY ";
        $query .= "    tbl1.grade, tbl1.hr_class, tbl1.attendno ";
        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();

        for($i=0;$i<get_count($model->data["SCHREGNO"]);$i++){

            foreach ($model->data["STATUS"] as $code => $col) {

                //削除
                $query  = "DELETE FROM ";
                $query .= "    jviewstat_dat ";
                $query .= "WHERE ";
                $query .= "    year     = '".CTRL_YEAR."' AND ";
                $query .= "    semester = '".$model->field["SEMESTER"]."' AND ";
                $query .= "    schregno = '".$model->data["SCHREGNO"][$i]."' AND ";
                $query .= "    viewcd   = '".$col."' ";
                $db->query($query);

                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                $data["VIEWCD"][TEXT]       = $col;
                $data["STATUS"][TEXT]       = $model->fields["STATUS".$code][$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "jviewstat_dat");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }

}
?>
