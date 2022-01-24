<?php

require_once('for_php7.php');
class knjf150dQuery extends Query {

    //年度の取得
    function getVisitYear() {
        $query .= " SELECT DISTINCT ";
        $query .= "     CASE WHEN MONTH(VISIT_DATE) > 3 THEN YEAR(VISIT_DATE) ELSE CHAR(INTEGER(YEAR(VISIT_DATE))-1) END AS VALUE, ";
        $query .= "     CASE WHEN MONTH(VISIT_DATE) > 3 THEN YEAR(VISIT_DATE) ELSE CHAR(INTEGER(YEAR(VISIT_DATE))-1) END AS LABEL ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TYPE IN ('1', '2', '3', '4', '5') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //保健室来室記録データよりデータを取得
    function selectQuery($model)
    {

        $selectDet = array();
        $selectDet["01"] = array(1, 2, 3);
        $selectDet["02"] = array(1);
        $selectDet["06"] = array(3, 4);
        $selectDet["08"] = array(1, 2, 3, 4, 5, 6, 7, 8);
        $selectDet["09"] = array(1, 3, 6, 8);
        $selectDet["98"] = array(1, 2);
        $selectDet["99"] = array("_L1");

        $query  = " WITH SRCHTYPE AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        foreach ($selectDet as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     DET{$seq}.REMARK{$remarkName} AS SEQ{$seq}_REMARK{$remarkName}, ";
            }
        }
        $query .= "     N1.NAME1 AS TYPE_REASON1 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        foreach ($selectDet as $seq => $remarkArray) {
            $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT DET{$seq} ON T1.SCHREGNO = DET{$seq}.SCHREGNO ";
            $query .= "          AND T1.VISIT_DATE = DET{$seq}.VISIT_DATE ";
            $query .= "          AND T1.VISIT_HOUR = DET{$seq}.VISIT_HOUR ";
            $query .= "          AND T1.VISIT_MINUTE = DET{$seq}.VISIT_MINUTE ";
            $query .= "          AND T1.TYPE = DET{$seq}.TYPE ";
            $query .= "          AND DET{$seq}.SEQ = '{$seq}' ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON DET02.REMARK1 = N1.NAMECD2 AND N1.NAMECD1 =  ";
        $query .= "         CASE WHEN T1.TYPE = '1' THEN 'F200' ";
        $query .= "              WHEN T1.TYPE = '2' THEN 'F201' ";
        $query .= "              WHEN T1.TYPE = '3' THEN 'F203' ";
        $query .= "              WHEN T1.TYPE = '4' THEN 'F202' ";
        $query .= "              WHEN T1.TYPE = '5' THEN 'F219' ";
        $query .= "              ELSE '' END ";
        $query .= " WHERE ";
        $query .= "     T1.TYPE IN ('1', '2', '3', '4', '5') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        $query .= "     T1.TYPE_REASON1, ";
        $query .= "     T3.HR_NAME || T2.ATTENDNO || '番' AS HR_NAME_ATTEND, ";
        $query .= "     T4.NAME, ";
        foreach ($selectDet as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     T1.SEQ{$seq}_REMARK{$remarkName}, ";
            }
        }
        $query .= "     CASE T1.TYPE WHEN '1' THEN '内科' WHEN '2' THEN '外科' WHEN '3' THEN 'その他' WHEN '4' THEN '生徒以外' ELSE '健康相談' END TYPE_NAME ";
        $query .= " FROM ";
        $query .= "     SRCHTYPE T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST M1 ";
        $query .= "       ON M1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND M1.SEMESTER <> '9' ";
        $query .= "      AND T1.VISIT_DATE BETWEEN M1.SDATE AND M1.EDATE ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "       ON T2.YEAR = M1.YEAR ";
        $query .= "      AND T2.SEMESTER = M1.SEMESTER ";
        $query .= "      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "       ON T3.YEAR = T2.YEAR ";
        $query .= "      AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "      AND T3.GRADE = T2.GRADE ";
        $query .= "      AND T3.HR_CLASS = T2.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T4 ";
        $query .= "       ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   VISIT_DATE = '".$model->cntl_dt_key."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.VISIT_DATE ASC, ";
        $query .= "     T1.VISIT_HOUR ASC, ";
        $query .= "     T1.VISIT_MINUTE ASC, ";
        $query .= "     T1.TYPE ";

        return $query;
    }

    function &getDeleteQuery(&$model)
    {
        $ary = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $array = explode (",", implode(',',$model->checked));

        for ($i = 0; $i < get_count($array); $i++){
            $schregno = "";
            $date = "";
            $hour = "";
            $minute = "";
            $type = "";
            list($schregno, $date, $hour, $minute, $type) = explode(":", $array[$i]);

            //情報が足りていない場合はskip
            if ($schregno == "" || $type == "") continue;

            $query  = " DELETE FROM ";
            $query .= "     NURSEOFF_VISITREC_DAT ";
            $query .= " WHERE ";
            $query .= "     VISIT_DATE = '".$date."' AND ";
            $query .= "     VISIT_HOUR = '".$hour."' AND ";
            $query .= "     VISIT_MINUTE = '".$minute."' AND ";
            $query .= "     TYPE = '".$type."' AND ";
            $query .= "     SCHREGNO = '".$schregno."' ";
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //スタッフの確認
    function getStaffCount($model)
    {
        $query  = " SELECT COUNT(*) FROM V_STAFF_MST T1 ";
        $query .= " WHERE T1.YEAR ='".$model->cntl_dt_year."' ";

        $db = Query::dbCheckOut();
        $row = $db->getOne($query);
        Query::dbCheckIn($db);

        return $row;
    }

    //データを移動
    function keyMoverQuery($model, $key, $move_key = "")
    {
        $handle_end = "";

        switch ($move_key) {
            case "next":
                $max_min = " MIN";
                $handle = " > ";
                break;
            case "before":
                $max_min = " MAX";
                $handle = " < ";
                break;
            default:
                $max_min = " MIN";
                $handle = " BETWEEN ";
                $handle_end = " AND DATE('".($model->cntl_dt_year + 1)."-3-31') ";
                break;
        }

        $query  = "SELECT ";
        $query  .= $max_min."(VISIT_DATE) AS VISIT_DATE ";
        $query  .= "FROM ";
        $query  .= " NURSEOFF_VISITREC_DAT ";
        $query  .= "WHERE ";
        $query  .= " VISIT_DATE ".$handle." DATE('".$key."') ";
        $query  .= $handle_end ;
        $query  .= " AND TYPE IN ('1', '2', '3', '4', '5') ";
        $db  = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row["VISIT_DATE"];
    }

}
?>
