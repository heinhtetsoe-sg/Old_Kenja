<?php

require_once('for_php7.php');

class knjd425_5Query extends Query {

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
    function getHreportRemarkCount($model, $flg="1", $seq="")
    {
        //テーブル取得
        $table = "HREPORT_GUIDANCE_SCHREG_REMARK_DAT";
        
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
        if ($seq != "") {
            $query .= "  AND TB.SEQ = {$seq} ";
        }

        return $query;
    }


    //UPDATE処理
    function &getUpdateQuery($model, $remarks, $getcolcnt) {
        $db = Query::dbCheckOut();

        //登録件数取得
        //UPDATE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
        $dstr = str_replace("/", "-", $model->upddate);
        foreach ($remarks as $key => $val) {
            $seqval = $key + 1;
            $data = array();
            $data["REMARK"][TEXT]          = $val;
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "sysdate()";

            $dataCheck1 = $db->getOne(knjd425_5Query::getHreportRemarkCount($model, "1", $seqval));
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

    function getEvalView($model) {
        $query .= " SELECT distinct ";
        $query .= "  T4.GUIDANCE_PATTERN, ";
        $query .= "  T5.ITEM_REMARK1, ";
        $query .= "  T5.ITEM_REMARK2, ";
        $query .= "  T5.ITEM_REMARK3, ";
        $query .= "  T5.ITEM_REMARK4, ";
        $query .= "  T5.ITEM_REMARK5, ";
        $query .= "  T5.ITEM_REMARK6, ";
        $query .= "  T5.ITEM_REMARK7, ";
        $query .= "  T5.ITEM_REMARK8 ";
        $query .= " FROM ";
        $query .= "  GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= "  LEFT JOIN GRADE_KIND_COMP_GROUP_DAT T2 ";
        $query .= "    ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "   AND T2.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ";
        $query .= "   AND T2.GHR_CD = T1.GHR_CD ";
        $query .= "   AND T2.GRADE = T1.GRADE ";
        $query .= "   AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "   AND T2.CONDITION = T1.CONDITION ";
        $query .= "   AND T2.GROUPCD = T1.GROUPCD ";
        $query .= "  LEFT JOIN GRADE_KIND_COMP_GROUP_YMST T4 ";
        $query .= "    ON T4.YEAR = T2.YEAR ";
        $query .= "   AND T4.SEMESTER = T2.SEMESTER ";
        $query .= "   AND T4.GAKUBU_SCHOOL_KIND = T2.GAKUBU_SCHOOL_KIND ";
        $query .= "   AND T4.GHR_CD = T2.GHR_CD ";
        $query .= "   AND T4.GRADE = T2.GRADE ";
        $query .= "   AND T4.HR_CLASS = T2.HR_CLASS ";
        $query .= "   AND T4.CONDITION = T2.CONDITION ";
        $query .= "   AND T4.GROUPCD = T2.GROUPCD ";
        $query .= "  LEFT JOIN HREPORT_GUIDANCE_ITEM_NAME_DAT T5 ";
        $query .= "    ON T5.YEAR = T4.YEAR ";
        $query .= "   AND T5.SEMESTER = T4.SEMESTER ";
        $query .= "   AND T5.GAKUBU_SCHOOL_KIND = T4.GAKUBU_SCHOOL_KIND ";
        $query .= "   AND T5.CONDITION = T4.CONDITION ";
        $query .= "   AND T5.GUIDANCE_PATTERN = T4.GUIDANCE_PATTERN ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '{$model->exp_year}' ";
        $query .= "  AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->grade}' ";
        $query .= "  AND T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    function getEvalSubject($model, $cntflg=false) {
        $query .= " SELECT ";
        if ($cntflg) {
            $query .= "  count(T3.SUBCLASSNAME) ";
        } else {
            $query .= "  T3.SUBCLASSNAME ";
        }
        $query .= " FROM ";
        $query .= "  GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= "  LEFT JOIN GRADE_KIND_COMP_GROUP_DAT T2 ";
        $query .= "    ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "   AND T2.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ";
        $query .= "   AND T2.GHR_CD = T1.GHR_CD ";
        $query .= "   AND T2.GRADE = T1.GRADE ";
        $query .= "   AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "   AND T2.CONDITION = T1.CONDITION ";
        $query .= "   AND T2.GROUPCD = T1.GROUPCD ";
        $query .= "  LEFT JOIN V_SUBCLASS_MST T3 ";
        $query .= "    ON T3.YEAR = T2.YEAR ";
        $query .= "   AND T3.CLASSCD = T2.CLASSCD ";
        $query .= "   AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "   AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "   AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '{$model->exp_year}' ";
        $query .= "  AND T1.SEMESTER = '9' ";
        $query .= "  AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->grade}' ";
        $query .= "  AND T1.SCHREGNO = '{$model->schregno}' ";
        if (!$cntflg) {
            $query .= " ORDER BY ";
            $query .= "   T3.CLASSCD, ";
            $query .= "   T3.SCHOOL_KIND, ";
            $query .= "   T3.CURRICULUM_CD, ";
            $query .= "   T3.SUBCLASSCD ";
        }
        return $query;
    }

}
?>
