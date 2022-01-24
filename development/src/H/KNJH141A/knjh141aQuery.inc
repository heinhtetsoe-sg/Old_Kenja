<?php

require_once('for_php7.php');

class knjh141aQuery extends Query {
    //エリア名取得
    function getArea() {
        $query  = " SELECT ";
        $query .= "     AREA_NAME AS LABEL, ";
        $query .= "     AREA_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     AREA_MST ";
        $query .= " ORDER BY ";
        $query .= "     INT(AREA_CD) ";

        return $query;
    }
    //とあるエリアの各路線を取得
    //getAreaのAREA_CDを渡す
    function getRosen($area_cd) {
        if ($area_cd == '3') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '21') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '22') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '23') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '24') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '25') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '26') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '27') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '28') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '29') ";
        } elseif ($area_cd == '5') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '30') ";
        } elseif ($area_cd == '6') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '31') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '32') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '33') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '34') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '35') ";
        } elseif ($area_cd == '9') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '36') ";
        } else {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
        }
            $query .= " ORDER BY ";
            $query .= "     RR_CD, ";
            $query .= "     LINE_SORT ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $semester) {
    
        $query .= " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER='".CTRL_SEMESTER."' ";

        return $query;
    }
    
    //CSVデータ
    function selectQuery($model){
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     M1.MAJORNAME, ";
        $query .= "     M2.COURSECODENAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     M3.NAME, ";
        $query .= "     CASE WHEN D1.FLG_1 = '1' THEN D1.JOSYA_1 || ':' || J1.STATION_NAME ELSE D1.JOSYA_1 END AS JOSYA_1, ";
        $query .= "     CASE WHEN D1.FLG_1 = '1' THEN D1.ROSEN_1 || ':' || J1.STATION_NAME ELSE D1.ROSEN_1 END AS ROSEN_1, ";
        $query .= "     CASE WHEN D1.FLG_1 = '1' THEN D1.GESYA_1 || ':' || G1.STATION_NAME ELSE D1.GESYA_1 END AS GESYA_1, ";
        $query .= "     D1.FLG_1, ";
        $query .= "     CASE WHEN D1.FLG_2 = '1' THEN D1.JOSYA_2 || ':' || J2.STATION_NAME ELSE D1.JOSYA_2 END AS JOSYA_2, ";
        $query .= "     CASE WHEN D1.FLG_2 = '1' THEN D1.ROSEN_2 || ':' || J2.STATION_NAME ELSE D1.ROSEN_2 END AS ROSEN_2, ";
        $query .= "     CASE WHEN D1.FLG_2 = '1' THEN D1.GESYA_2 || ':' || G2.STATION_NAME ELSE D1.GESYA_2 END AS GESYA_2, ";
        $query .= "     D1.FLG_2, ";
        $query .= "     CASE WHEN D1.FLG_3 = '1' THEN D1.JOSYA_3 || ':' || J3.STATION_NAME ELSE D1.JOSYA_3 END AS JOSYA_3, ";
        $query .= "     CASE WHEN D1.FLG_3 = '1' THEN D1.ROSEN_3 || ':' || J3.STATION_NAME ELSE D1.ROSEN_3 END AS ROSEN_3, ";
        $query .= "     CASE WHEN D1.FLG_3 = '1' THEN D1.GESYA_3 || ':' || G3.STATION_NAME ELSE D1.GESYA_3 END AS GESYA_3, ";
        $query .= "     D1.FLG_3, ";
        $query .= "     CASE WHEN D1.FLG_4 = '1' THEN D1.JOSYA_4 || ':' || J4.STATION_NAME ELSE D1.JOSYA_4 END AS JOSYA_4, ";
        $query .= "     CASE WHEN D1.FLG_4 = '1' THEN D1.ROSEN_4 || ':' || J4.STATION_NAME ELSE D1.ROSEN_4 END AS ROSEN_4, ";
        $query .= "     CASE WHEN D1.FLG_4 = '1' THEN D1.GESYA_4 || ':' || G4.STATION_NAME ELSE D1.GESYA_4 END AS GESYA_4, ";
        $query .= "     D1.FLG_4, ";
        $query .= "     CASE WHEN D1.FLG_5 = '1' THEN D1.JOSYA_5 || ':' || J5.STATION_NAME ELSE D1.JOSYA_5 END AS JOSYA_5, ";
        $query .= "     CASE WHEN D1.FLG_5 = '1' THEN D1.ROSEN_5 || ':' || J5.STATION_NAME ELSE D1.ROSEN_5 END AS ROSEN_5, ";
        $query .= "     CASE WHEN D1.FLG_5 = '1' THEN D1.GESYA_5 || ':' || G5.STATION_NAME ELSE D1.GESYA_5 END AS GESYA_5, ";
        $query .= "     D1.FLG_5, ";
        $query .= "     CASE WHEN D1.FLG_6 = '1' THEN D1.JOSYA_6 || ':' || J6.STATION_NAME ELSE D1.JOSYA_6 END AS JOSYA_6, ";
        $query .= "     CASE WHEN D1.FLG_6 = '1' THEN D1.ROSEN_6 || ':' || J6.STATION_NAME ELSE D1.ROSEN_6 END AS ROSEN_6, ";
        $query .= "     CASE WHEN D1.FLG_6 = '1' THEN D1.GESYA_6 || ':' || G6.STATION_NAME ELSE D1.GESYA_6 END AS GESYA_6, ";
        $query .= "     D1.FLG_6, ";
        $query .= "     CASE WHEN D1.FLG_7 = '1' THEN D1.JOSYA_7 || ':' || J7.STATION_NAME ELSE D1.JOSYA_7 END AS JOSYA_7, ";
        $query .= "     CASE WHEN D1.FLG_7 = '1' THEN D1.ROSEN_7 || ':' || J7.STATION_NAME ELSE D1.ROSEN_7 END AS ROSEN_7, ";
        $query .= "     CASE WHEN D1.FLG_7 = '1' THEN D1.GESYA_7 || ':' || G7.STATION_NAME ELSE D1.GESYA_7 END AS GESYA_7, ";
        $query .= "     D1.FLG_7 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_ENVIR_DAT D1 ";
        $query .= "     ON T1.SCHREGNO = D1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST M1 ";
        $query .= "     ON T1.COURSECD = M1.COURSECD ";
        $query .= "     AND T1.MAJORCD = M1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST M2 ";
        $query .= "     ON T1.COURSECODE = M2.COURSECODE ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST M3 ";
        $query .= "     ON T1.SCHREGNO = M3.SCHREGNO ";
        $query .= " LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = D1.JOSYA_1 ";
        $query .= " LEFT JOIN STATION_NETMST J2 ON J2.STATION_CD = D1.JOSYA_2 ";
        $query .= " LEFT JOIN STATION_NETMST J3 ON J3.STATION_CD = D1.JOSYA_3 ";
        $query .= " LEFT JOIN STATION_NETMST J4 ON J4.STATION_CD = D1.JOSYA_4 ";
        $query .= " LEFT JOIN STATION_NETMST J5 ON J5.STATION_CD = D1.JOSYA_5 ";
        $query .= " LEFT JOIN STATION_NETMST J6 ON J6.STATION_CD = D1.JOSYA_6 ";
        $query .= " LEFT JOIN STATION_NETMST J7 ON J7.STATION_CD = D1.JOSYA_7 ";
        $query .= " LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = D1.GESYA_1 ";
        $query .= " LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = D1.GESYA_2 ";
        $query .= " LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = D1.GESYA_3 ";
        $query .= " LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = D1.GESYA_4 ";
        $query .= " LEFT JOIN STATION_NETMST G5 ON G5.STATION_CD = D1.GESYA_5 ";
        $query .= " LEFT JOIN STATION_NETMST G6 ON G6.STATION_CD = D1.GESYA_6 ";
        $query .= " LEFT JOIN STATION_NETMST G7 ON G7.STATION_CD = D1.GESYA_7 ";
        $query .= "  ";
        $query .= " WHERE ";
        $query .= "     (YEAR='".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER='".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE || HR_CLASS IN('".implode("','",explode(',',$model->selectdata))."')) ";
        $query .= "     AND ((D1.FLG_1='1' AND D1.ROSEN_1='".$model->field['ROSEN']."') ";
        $query .= "     OR (D1.FLG_2='1' AND D1.ROSEN_2='".$model->field['ROSEN']."') ";
        $query .= "     OR (D1.FLG_3='1' AND D1.ROSEN_3='".$model->field['ROSEN']."') ";
        $query .= "     OR (D1.FLG_4='1' AND D1.ROSEN_4='".$model->field['ROSEN']."') ";
        $query .= "     OR (D1.FLG_5='1' AND D1.ROSEN_5='".$model->field['ROSEN']."') ";
        $query .= "     OR (D1.FLG_6='1' AND D1.ROSEN_6='".$model->field['ROSEN']."') ";
        $query .= "     OR (D1.FLG_7='1' AND D1.ROSEN_7='".$model->field['ROSEN']."')) ";
        
        return $query;
    }
}
?>
