
<?php
class knjl413iQuery extends Query {
    //名称マスタより取得
    function getNameMst($model, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        } else if ($namecd1 == "L003") {
            $query .= " AND NAMECD2 = '1' "; //入試制度 1:中学 固定
        } else if ($namecd1 == "Z057") {
            $query .= " AND NAMECD2    = 'L3' ";
            $query .= " AND NAMESPARE2 = '{$model->field["APPLICANTDIV"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
