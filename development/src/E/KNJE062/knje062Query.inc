<?php

require_once('for_php7.php');

class knje062Query extends Query {
    //指導要録データよりデータを取得
    function selectQuery($model)
    {
        if ($model->cmd == "add"){
            $scd = 0;
        }else if ($model->cmd == "update"){
            $scd = $model->schoolcd;
        }
        $query = " SELECT ";
        $query .= "   T1.SCHOOLCD, ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.ANNUAL, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T2.CLASSNAME, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T3.SUBCLASSNAME, ";
        $query .= "   T1.VALUATION, ";
        $query .= "   T1.GET_CREDIT, ";
        $query .= "   T1.ADD_CREDIT, ";
        $query .= "   T4.REMARK ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT T1 LEFT OUTER JOIN STUDYRECREMARK_DAT T4  ";
        $query .= "   ON T1.YEAR = T4.YEAR AND  ";
        $query .= "   T1.SCHREGNO = T4.SCHREGNO AND  ";
        $query .= "   T1.CLASSCD = T4.CLASSCD AND  ";
        $query .= "   T1.SUBCLASSCD = T4.SUBCLASSCD, ";
        $query .= "   CLASS_MST T2, ";
        $query .= "   SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        if ($model->cmd == "edit"){
            $query .= "   T1.SCHOOLCD = '" .$model->schoolcd ."' AND ";
            $query .= "   T1.ANNUAL   = '" .$model->annual ."' AND ";
            $query .= "   T1.SUBCLASSCD   = '" .$model->subclasscd ."' AND ";
        }else if ($model->cmd == "add" || $model->cmd == "update"){
            $query .= "   T1.SCHOOLCD = '" .$scd ."' AND ";
            $query .= "   T1.ANNUAL   = '" .$model->field["ANNUAL"] ."' AND ";
            $query .= "   T1.SUBCLASSCD   = '" .$model->field["SUBCLASSCD"] ."' AND ";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' AND ";
        $query .= "   ((T1.SCHOOLCD = '1' AND T1.ANNUAL = '00') OR T1.SCHOOLCD != '1') AND ";
        $query .= "   T1.CLASSCD = T2.CLASSCD  AND ";
        $query .= "   T1.SUBCLASSCD = T3.SUBCLASSCD ";
        if (is_array($model->sort)){
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order){
                switch($field){
                    case "YEAR":    //年度順
                        $query .= " T1.YEAR";
                        $query .= ($order > 0)? " ASC " : " DESC ";
                        $query .= ",T1.SUBCLASSCD";
                        break 2;
                    case "SUBCLASSCD":    //科目順
                        $query .= " T1.YEAR,T1.SUBCLASSCD";
                        $query .= ($order > 0)? " ASC " : " DESC ";
                        break 2;
                }
            }
        }else{
            $query .= "   T1.YEAR, ";
            $query .= "   T1.SUBCLASSCD ";
        }
        return $query;
    }
    
    //対応者を取得
    function selectSubclassQuery($year)
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '" .$year."' ";
        $query .= " ORDER BY ";
        $query .= "   SUBCLASSCD ";
        return $query;
    }
    
    // INSERT
    function &getInsertQuery($model)
    {
        $query = " INSERT INTO SCHREG_STUDYREC_DAT( ";
        $query .= " SCHOOLCD, ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " ANNUAL, ";
        $query .= " CLASSCD, ";
        $query .= " SUBCLASSCD, ";
        $query .= " CLASSNAME, ";
        $query .= " CLASSABBV, ";
        $query .= " CLASSNAME_ENG, ";
        $query .= " CLASSABBV_ENG, ";
        $query .= " SUBCLASSES, ";
        $query .= " SUBCLASSNAME, ";
        $query .= " SUBCLASSABBV, ";
        $query .= " SUBCLASSNAME_ENG, ";
        $query .= " SUBCLASSABBV_ENG, ";
        if (is_numeric($model->field["VALUATION"])){
            $query .= " VALUATION, ";
        }
        if (is_numeric($model->field["GET_CREDIT"])){
            $query .= " GET_CREDIT, ";
        }
        if (is_numeric($model->field["ADD_CREDIT"])){
            $query .= " ADD_CREDIT, ";
        }
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= "   (SELECT ";
        $query .= "     '0', ";
        $query .= "     '". $model->field["YEAR"] ."', ";
        $query .= "     '". $model->schregno ."', ";
        $query .= "     '". sprintf("%02d", $model->field["ANNUAL"]) ."', ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T1.CLASSNAME, ";
        $query .= "     T1.CLASSABBV, ";
        $query .= "     T1.CLASSNAME_ENG, ";
        $query .= "     T1.CLASSABBV_ENG, ";
        $query .= "     T1.SUBCLASSES, ";
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     T2.SUBCLASSABBV, ";
        $query .= "     T2.SUBCLASSNAME_ENG, ";
        $query .= "     T2.SUBCLASSABBV_ENG, ";
        if (is_numeric($model->field["VALUATION"])){
            $query .= $model->field["VALUATION"] .",";
        }
        if (is_numeric($model->field["GET_CREDIT"])){
            $query .= $model->field["GET_CREDIT"] .",";
        }
        if (is_numeric($model->field["ADD_CREDIT"])){
            $query .= $model->field["ADD_CREDIT"] .",";
        }
        $query .= "     '" .STAFFCD ."' ";
        $query .= "   FROM ";
        $query .= "     CLASS_MST T1, ";
        $query .= "     SUBCLASS_MST T2 ";
        $query .= "   WHERE ";
        $query .= "     T1.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2) AND ";
        $query .= "     T2.SUBCLASSCD = '". $model->field["SUBCLASSCD"] ."' ";
        $query .= "   )  ";

        return $query;
    }
    // DELETE
    function &getDeleteQuery($model)
    {
        $query = " DELETE FROM ";
        $query .= "   SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "update" || $model->cmd == "delete2"){
            $query .= "   SCHOOLCD = '" .$model->schoolcd ."' AND ";
            $query .= "   ANNUAL   = '" .$model->annual ."' AND ";
            $query .= "   SUBCLASSCD   = '" .$model->subclasscd ."' AND ";
        }else if ($model->cmd == "delete"){
            $query .= "   SCHOOLCD || ',' || ANNUAL || ',' || SUBCLASSCD IN ('" .implode($model->checked,"','") ."') AND ";
        }
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";

        return $query;
    }
    //備考登録
    function &getInsertQueryRemark($model)
    {
        $query = " INSERT INTO STUDYRECREMARK_DAT( ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " CLASSCD, ";
        $query .= " SUBCLASSCD, ";
        $query .= " REMARK, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= "  ( ";
        $query .= " '".$model->field["YEAR"]."', ";
        $query .= " '".$model->schregno."', ";
        $query .= " '".substr($model->field["SUBCLASSCD"],0,2)."', ";
        $query .= " '".$model->field["SUBCLASSCD"]."', ";
        $query .= " '".Query::addquote($model->field["REMARK"])."', ";
        $query .= " '".STAFFCD."' ";
        $query .= " ) ";

        return $query;
    }
    function &getDeleteQueryRemark($model)
    {
        $query = " DELETE FROM ";
        $query .= "   STUDYRECREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "   SCHREGNO = '".$model->schregno."' AND ";
        $query .= "   SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        return $query;
    }
    //年度取得
    function &selectQueryYear($model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "   YEAR ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '".$model->schregno."' ";
        return $query;
    }
    
    //教科名
    function SelectClassName($model)
    {
        $query = "SELECT classname FROM v_class_mst WHERE year = '".$model->field["YEAR"]."' AND classcd = '".substr($model->field["SUBCLASSCD"],0,2)."'";
        return $query;
    
    }
    
    //年度重複エラーチェック(同じ年度に複数の年次を登録しない）
    function SelectAnnualQuery($model)
    {
        if ($model->cmd == "add"){
            $scd = 0;
        }else if ($model->cmd == "update"){
            $scd = $model->schoolcd;
        }
        $query = "SELECT * FROM schreg_studyrec_dat
                   WHERE year       = '".$model->field["YEAR"]."'
                     AND schregno   = '".$model->schregno."'
                     AND schoolcd   = '".$scd."'
                     AND annual    <> '".$model->field["ANNUAL"]."'";
        return $query;    
    }
}
?>
