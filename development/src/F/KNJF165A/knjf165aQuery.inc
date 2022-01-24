<?php

require_once('for_php7.php');

class knjf165aQuery extends Query {

    function getNameMst($nameCd1) {
        $query .= " SELECT ";
        $query .= "   NAMECD2 AS VALUE, ";
        $query .= "   NAME1   AS LABEL ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = '{$nameCd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return($query);
    }

    function getRemark($model) {
        $query .= " SELECT ";
        $query .= "   T1.TYPE, ";
        $query .= "   CASE WHEN T1.TYPE = '1' THEN '内科' ";
        $query .= "                           ELSE '外科' ";
        $query .= "   END AS TYPE_NAME, ";
        $query .= "   T2.GRADE, ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T2.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T4.NAME, ";
        $query .= "   T1.VISIT_DATE, ";
        $query .= "   T1.VISIT_HOUR, ";
        $query .= "   T1.VISIT_MINUTE, ";
        $query .= "   T1.REMARK2, ";
        $query .= "   T1.REMARK3, ";
        $query .= "   T1.REMARK4, ";
        $query .= "   T1.REMARK5, ";
        $query .= "   T1.REMARK6, ";
        $query .= "   T1.REMARK7, ";
        $query .= "   T1.REMARK8 ";
        $query .= " FROM ";
        $query .= "   NURSEOFF_VISITREC_DETAIL_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "           ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "          AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "          ON T3.YEAR     = T2.YEAR ";
        $query .= "         AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T3.GRADE    = T2.GRADE ";
        $query .= "         AND T3.HR_CLASS = T2.HR_CLASS ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T4 ";
        $query .= "          ON T4.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.VISIT_DATE BETWEEN '".str_replace("/", "-", $model->sDate)."' AND '".str_replace("/", "-", $model->eDate)."' ";
        $query .= "   AND T1.TYPE IN ('1','2') ";
        $query .= "   AND T1.SEQ     = '97' ";
        $query .= "   AND T1.REMARK1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TYPE ";
        return($query);
    }

    //UPDATE処理
    function &getUpdateQuery($model, $rowcnt) {
        $db = Query::dbCheckOut();

        //INSERT
        for ($cnt=0;$cnt < $rowcnt;$cnt++) {
            if($model->chekArry[$cnt] != ""){
                //DELETE
                $query  = " DELETE FROM  ";
                $query .= "   NURSEOFF_VISITREC_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "   SCHREGNO         = '{$model->schregnoArry[$cnt]}' ";
                $query .= "   AND VISIT_DATE   = '{$model->dateArry[$cnt]}' ";
                $query .= "   AND VISIT_HOUR   = '{$model->hourArry[$cnt]}' ";
                $query .= "   AND VISIT_MINUTE = '{$model->minuteArry[$cnt]}' ";
                $query .= "   AND TYPE         = '{$model->typeArry[$cnt]}' ";
                $query .= "   AND SEQ          = '97' ";
                $query .= "   AND REMARK1      = '1' ";
                $db->query($query);

                $remark3 = $model->remark3Arry[$cnt];
                if($remark3 != "")$remark3 = str_replace("/", "-", $remark3);
                $remark4 = $model->remark3Arry[$cnt];
                if($remark4 != "")$remark4 = str_replace("/", "-", $remark4);

                //INSERT
                $data = array();
                $data["SCHREGNO"][TEXT]     = $model->schregnoArry[$cnt];
                $data["VISIT_DATE"][TEXT]   = $model->dateArry[$cnt];
                $data["VISIT_HOUR"][TEXT]   = $model->hourArry[$cnt];
                $data["VISIT_MINUTE"][TEXT] = $model->minuteArry[$cnt];
                $data["TYPE"][TEXT]         = $model->typeArry[$cnt];
                $data["SEQ"][TEXT]          = 97;
                $data["REMARK1"][TEXT]      = 1;
                $data["REMARK2"][TEXT]      = $model->remark2Arry[$cnt];
                $data["REMARK3"][TEXT]      = $remark3;
                $data["REMARK4"][TEXT]      = $remark4;
                $data["REMARK5"][TEXT]      = $model->remark5Arry[$cnt];
                $data["REMARK6"][TEXT]      = $model->remark6Arry[$cnt];
                $data["REMARK7"][TEXT]      = $model->remark7Arry[$cnt];
                $data["REMARK8"][TEXT]      = $model->remark8Arry[$cnt];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "NURSEOFF_VISITREC_DETAIL_DAT");
                $db->query($query);
            }
        }
        return;
    }
}
?>
