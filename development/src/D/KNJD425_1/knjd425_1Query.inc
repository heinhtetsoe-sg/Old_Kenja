<?php

require_once('for_php7.php');

class knjd425_1Query extends Query {
    function chkDataExist($model) {
        $dstr = str_replace("/", "-", $model->upddate);
        $query  = " SELECT DISTINCT ";
        $query .= "  COUNT(*)  ";
        $query .= " FROM ";
        $query .= "  HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '{$model->exp_year}' ";
        $query .= "  AND SEMESTER = '9' ";
        $query .= "  AND RECORD_DATE = '{$dstr}' ";
        $query .= "  AND SCHREGNO = '{$model->schregno}' ";
        $query .= "  AND DIV = '{$model->selKindNo}' ";
        return $query;
    }

    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "   NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "   NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "   V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->exp_year}' ";
        $query .= "   AND NAMECD1 = '{$namecd1}' ";
        return $query;
    }

    function getDataList($model, $cnt) {
        $dstr = str_replace("/", "-", $model->upddate);
        $query  = " SELECT ";
        $query .= " REMARK AS REMARK ";
        $query .= " FROM ";
        $query .= "   HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->exp_year}' ";
        $query .= "   AND SEMESTER = '9' ";
        $query .= "   AND RECORD_DATE = '{$dstr}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND DIV = '{$model->selKindNo}' ";
        $query .= "   AND SEQ = {$cnt} ";
        return $query;
    }

    //データチェック
    function getHreportRemarkCount($model, $flg="1")
    {
        //テーブル取得
        $table = "HREPORT_GUIDANCE_SCHREG_REMARK_DAT";
        // if ($flg == "2") {
        //     $table = "HREPORT_GUIDANCE_SCHREG_REMARK_HDAT";
        // }
        
        $query .= " SELECT ";
        $query .= "  COUNT(*) ";
        $query .= " FROM ";
        $query .= "  {$table} TB ";
        $query .= " WHERE ";
        $query .= "  TB.YEAR = '{$model->exp_year}' ";
        $query .= "  AND TB.SEMESTER = '9' ";
        $dstr = str_replace("/", "-", $model->upddate);
        $query .= "  AND TB.RECORD_DATE = '{$dstr}' ";
        $query .= "  AND TB.SCHREGNO = '{$model->schregno}' ";
        if ($flg != "2") {
            $query .= "  AND TB.DIV = '{$model->selKindNo}' ";
        }

        return $query;
    }


    //UPDATE処理
    function &getUpdateQuery($model, $remarks, $getcolcnt) {
        $db = Query::dbCheckOut();

        //登録件数取得
        $dataCheck1 = $db->getOne(knjd425_1Query::getHreportRemarkCount($model, "1"));
        //UPDATE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
        $dstr = str_replace("/", "-", $model->upddate);
        for ($cnt=0;$cnt < $getcolcnt;$cnt++) {
            $seqval = $cnt+1;
            $data = array();
            $data["REMARK"][TEXT]       = $remarks[$cnt];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            if ($dataCheck1 == 0) {
                $data["YEAR"][TEXT]        = $model->exp_year;
                $data["SEMESTER"][TEXT]    = "9";
                $data["RECORD_DATE"][TEXT] = $dstr;
                $data["SCHREGNO"][TEXT]    = $model->schregno;
                $data["DIV"][TEXT]         = $model->selKindNo;
                $data["SEQ"][NUMBER]       = $seqval;
                
                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "  YEAR = '{$model->exp_year}' ";
                $where .= "  AND SEMESTER = '9' ";
                $where .= "  AND RECORD_DATE = '{$dstr}' ";
                $where .= "  AND SCHREGNO = '{$model->schregno}' ";
                $where .= "  AND DIV = '{$model->selKindNo}' ";
                $where .= "  AND SEQ = {$seqval} ";

                $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT", $where);
                $db->query($query);
            }
        }
        return;
    }

    function getGuidanceKindName($model, $specifyschregflg="", $kno = "") {
        $query  = " SELECT ";
        $query .= "  T1.KIND_NO, ";
        $query .= "  T2.KIND_NAME AS BTN_SUBFORMCALL, ";
        $query .= "  CASE WHEN T1.KIND_NO = '01' THEN '1' ";
        $query .= "       WHEN T1.KIND_NO IN ('04', '09') THEN '2' ";
        $query .= "       WHEN T1.KIND_NO IN ('02', '03', '05', '06') THEN '3' ";
        $query .= "       WHEN T1.KIND_NO = '10' THEN '4' ";
        $query .= "       WHEN T1.KIND_NO IN ('07', '08') THEN '5' ";
        $query .= "       ELSE '' END AS KINDCD ";
        $query .= " FROM ";
        $query .= "  HREPORT_GUIDANCE_KIND_DAT T1 ";
        $query .= "  LEFT JOIN HREPORT_GUIDANCE_KIND_NAME_HDAT T2 ";
        $query .= "    ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.KIND_NO = T1.KIND_NO";
        $query .= "  LEFT JOIN V_NAME_MST VD090 ";
        $query .= "    ON VD090.YEAR = T1.YEAR ";
        $query .= "   AND VD090.NAMECD1 = 'D090' ";
        $query .= "   AND T1.KIND_NO = VD090.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '{$model->exp_year}' ";
        if ($specifyschregflg === "" || $specifyschregflg == "1") {
            $query .= "  AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->grade}' ";
        } else {
            $query .= "  AND T1.GRADE || '-' || T1.HR_CLASS = '00-000' ";
        }
        if ($specifyschregflg === "") {
            $query .= "  AND T1.SCHREGNO = '{$model->schregno}' ";
        } else {
            $query .= "  AND T1.SCHREGNO = '00000000' ";
        }
        if ($kno != "") {
            $query .= "  AND T1.KIND_NO = '{$kno}' ";
        }
        $query .= " ORDER BY T1.SHOWORDER ";

        return $query;
    }

    function getStaffList($model) {
        $query  = " SELECT ";
        $query .= "   T1.STAFFCD AS VALUE, ";
        $query .= "   T2.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "   STAFF_YDAT T1 ";
        $query .= "   LEFT JOIN STAFF_MST T2 ";
        $query .= "     ON T2.STAFFCD = T1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= " ORDER BY ";
        $query .= "   T1.STAFFCD ";

        return $query;
    }
}
?>
