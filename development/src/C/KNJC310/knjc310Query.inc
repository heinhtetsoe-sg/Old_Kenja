<?php

require_once('for_php7.php');
class knjc310Query extends Query {
    
    //発信者取得
    function getStaff()
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    
    //一覧作成
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.MESSAGENO, ";
        $query .= "     T1.SENDDATE, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.MESSAGE1, ";
        $query .= "     T1.MESSAGE2, ";
        $query .= "     T1.TERMDATE, ";
        $query .= "     T1.READDATE, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     DENGON T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     V_STAFF_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.STAFFCD  = T3.STAFFCD ";
        $query .= "     AND T3.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["C_STAFF"] != ""){
            $query .= " AND T1.STAFFCD  = '".$model->field["C_STAFF"]."' ";
        }
        if ($model->joken == "2"){
            $query .= " AND T1.READDATE IS NOT NULL ";
        }
        if ($model->joken == "3"){
            $query .= " AND T1.READDATE IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SENDDATE, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.SCHREGNO ";
        
        return $query;
    }
    
    //削除
    function getDeleteQuery($MESSAGENO)
    {
        $query  = " DELETE FROM ";
        $query .= "     DENGON ";
        $query .= " WHERE ";
        $query .= "     MESSAGENO = $MESSAGENO ";
        
        return $query;
    }
    
}
?>