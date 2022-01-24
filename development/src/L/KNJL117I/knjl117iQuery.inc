<?php
class knjl117iQuery extends Query {

    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "    AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分
    function getTestDiv($model) {
        $query  = "     SELECT ";
        $query .= "       TESTDIV AS VALUE, ";
        $query .= "       TESTDIV || ':' || TESTDIV_ABBV AS LABEL";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_TESTDIV_MST ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV IN ('1', '2') "; //試験区分は「1:A方式」「2:B方式」のみ対象
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //評価区分
    function getPointCd($model) {
        $query  = "     SELECT ";
        $query .= "       SEQ AS VALUE, ";
        $query .= "       SEQ || ':' || VALUE (NAME1, '') AS LABEL ";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_SETTING_MST ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND SETTING_CD = 'L072' ";
        $query .= "       AND (NAMESPARE1 IS NULL OR NAMESPARE1 = '{$model->testdiv}') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メイン
    function selectMainQuery($model, $div = "") {

        $query  = "     SELECT ";
        if ($div == "cnt") {
            $query .= "       COUNT(*) ";
        } else {
            $query .= "       POINTLEVEL, ";
            $query .= "       POINTLOW, ";
            $query .= "       POINTHIGH, ";
            $query .= "       PLUS_POINT, ";
            $query .= "       MINUS_POINT ";
        }
        $query .= "     FROM ";
        $query .= "       ENTEXAM_POINT_MST ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV = '{$model->testdiv}' ";
        $query .= "       AND POINTCD = '{$model->pointcd}' ";
        if ($div != "cnt") {
            $query .= "     ORDER BY POINTLEVEL DESC ";
        }

        return $query;
    }

    //コース名コンボ取得
   function getCouseName() {
        $query  = "  SELECT DISTINCT ";
        $query .= "      T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "      T2.GRADE_NAME1 || ' ' || T3.COURSENAME || ' ' || T4.MAJORNAME || ' ' || T5.COURSECODENAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT T1, ";
        $query .= "      SCHREG_REGD_GDAT T2, ";
        $query .= "      COURSE_MST T3, ";
        $query .= "      MAJOR_MST T4, ";
        $query .= "      COURSECODE_MST T5 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "  AND T1.YEAR = T2.YEAR ";
        $query .= "  AND T1.GRADE = T2.GRADE ";
        $query .= "  AND T1.COURSECD = T3.COURSECD ";
        $query .= "  AND T1.MAJORCD = T4.MAJORCD ";
        $query .= "  AND T1.COURSECODE = T5.COURSECODE ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //教科取得
    function getClassMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND  AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || ':' || T1.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclassMst($classcd, $model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".CTRL_YEAR."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND = '".$classcd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目別評定マスタ情報抽出
    function selectQuery($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        
        $query  = "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      ASSESS_SUBCLASS_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "  AND GRADE          = '".$model->grade."' ";
        $query .= "  AND COURSECD       = '".$model->coursecd."' ";
        $query .= "  AND MAJORCD        = '".$model->majorcd."' ";
        $query .= "  AND COURSECODE     = '".$model->coursecode."' ";
        $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        
        return $query;
    }
    
    //Insert作成
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $query  =  " DELETE FROM ";
        $query .= "      ENTEXAM_POINT_MST ";
        $query .= "  WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND POINTCD       = '{$model->pointcd}' ";
        $db->query($query);
        
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $model->examyear;
        $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
        $data["TESTDIV"][TEXT]              = $model->testdiv;
        $data["POINTCD"][TEXT]              = $model->pointcd;
        $data["REGISTERCD"][TEXT]           = STAFFCD ;
        $data["UPDATED"][FUNC]              = "sysdate()";
        for ($i = $model->maxLevel; $i > 0 ; $i--) {            
            $data["POINTLEVEL"][NUMBER]     = $i;
            $data["POINTLOW"][TEXT]         = $model->dataField[$i]["POINTLOW"];
            $data["POINTHIGH"][TEXT]        = $model->dataField[$i]["POINTHIGH"];
            $data["PLUS_POINT"][TEXT]       = $model->dataField[$i]["PLUS_POINT"];
            $data["MINUS_POINT"][TEXT]      = $model->dataField[$i]["MINUS_POINT"];

            $query = Query::insertSQL($data, "ENTEXAM_POINT_MST");
            $db->query($query);
        }
        
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //コピー先年度データカウント
    function cntToYearOrder($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_POINT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examyear}' ";

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $data_check;
    }
    
    //コピー元年度データカウント
    function cntFromYearOrder($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_POINT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".($model->examyear - 1)."' ";

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $data_check;
    }
    
    //指定年度から対象年度へコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ENTEXAM_POINT_MST ";
        $query .= "       ( ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         POINTCD, ";
        $query .= "         POINTLEVEL, ";
        $query .= "         POINTLOW, ";
        $query .= "         POINTHIGH, ";
        $query .= "         PLUS_POINT, ";
        $query .= "         MINUS_POINT, ";
        $query .= "         REGISTERCD, ";
        $query .= "         UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '{$model->examyear}', ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         POINTCD, ";
        $query .= "         POINTLEVEL, ";
        $query .= "         POINTLOW, ";
        $query .= "         POINTHIGH, ";
        $query .= "         PLUS_POINT, ";
        $query .= "         MINUS_POINT, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_POINT_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".($model->examyear - 1)."' ";
        $query .= "     ) ";
        
        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        
        return true;
    }

}
?>
