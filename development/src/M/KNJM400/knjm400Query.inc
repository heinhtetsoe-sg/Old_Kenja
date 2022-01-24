<?php

require_once('for_php7.php');


class knjm400Query extends Query {

    //学期取得
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //名称
    function GetName($namecd1)
    {
        $query  = " SELECT NAME1,NAMECD2 FROM v_name_mst ";
        $query .= "  WHERE year    = '".CTRL_YEAR."' ";
        $query .= "     AND namecd1 = '".$namecd1."' ";

        return $query;
    }

    //Z010
    function getZ010()
    {
        $query  = " SELECT NAME1 FROM v_name_mst ";
        $query .= "  WHERE year    = '".CTRL_YEAR."' ";
        $query .= "     AND namecd1 = 'Z010' ";
        $query .= "     AND namecd2 = '00' ";

        return $query;
    }

    //日付
    function GetDate($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT T1";
        if($model->Properties["useTsushinSemesKonboHyoji"] == '1') {
            $query .= " INNER JOIN ";
            $query .= "     SEMESTER_MST S1 ";
            $query .= "     ON S1.SDATE<=T1.EXECUTEDATE AND S1.EDATE>=T1.EXECUTEDATE ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if($model->Properties["useTsushinSemesKonboHyoji"] == '1') {
            $query .= "     AND S1.SEMESTER='".$model->field['SELECT_SEMESTER']."' ";
        }
        
        return $query;
    }

    //講座
    function GetChr($model)
    {
        $query  = " with atable(CCD) as (SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CHAIRNAME, ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT, ";
        $query .= "     atable w2 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     CHAIRCD = CCD ";
        $query .= " ORDER BY CHAIRCD ";
        return $query;
    }

}
?>
