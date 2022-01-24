<?php

require_once('for_php7.php');

class knjp986query extends Query {
    /****************/
    /** ＣＳＶ出力 **/
    /****************/
    //CSVデータ出力
    function selectMainQuery($model) {
        $fromDate = str_replace('/', '-', $model->field["FROM_DATE"]);
        $toDate   = str_replace('/', '-', $model->field["TO_DATE"]);

        $query  = " SELECT ";
        $query .= "     case ";
        $query .= "         when SCHD.OUTGO_L_CD = '99' then '1' ";
        $query .= "         else '0' ";
        $query .= "     end AS DIV, ";
        $query .= "     SCHD.SCHOOL_KIND, ";
        $query .= "     SCHD.REQUEST_NO || char(SCHD.LINE_NO) as REQUEST_NO, ";
        $query .= "     case ";
        $query .= "         when SCHD.SCHOOL_KIND = 'J' and MAPP.SCHREGNO_J is     null then SCHD.SCHREGNO ";
        $query .= "         when SCHD.SCHOOL_KIND = 'J' and MAPP.SCHREGNO_J is not null then MAPP.SCHREGNO_J ";
        $query .= "         when SCHD.SCHOOL_KIND = 'H' and MAPP.SCHREGNO_H is     null then SCHD.SCHREGNO ";
        $query .= "         when SCHD.SCHOOL_KIND = 'H' and MAPP.SCHREGNO_H is not null then MAPP.SCHREGNO_H ";
        $query .= "         else '' ";
        $query .= "     end AS SCHREGNO, ";
        $query .= "     SCHD.YEAR, ";
        $query .= "     SGLM.KIND_CD, ";
        $query .= "     SGLM.DEPOSIT_CD, ";
        $query .= "     case ";
        $query .= "         when SCHD.OUTGO_L_CD = '99' then OUTG.HENKIN_DATE ";
        $query .= "         else OUTG.OUTGO_DATE ";
        $query .= "     end AS OUTGO_DATE, ";
        $query .= "     SCHD.OUTGO_MONEY, ";
        $query .= "     MEIS.REMARK, ";
        $query .= "     value(MEIS.HASUU, 0) as HASUU ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT SCHD ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = SCHD.SCHOOLCD ";
        $query .= "                                          AND OUTG.SCHOOL_KIND = SCHD.SCHOOL_KIND ";
        $query .= "                                          AND OUTG.YEAR        = SCHD.YEAR ";
        $query .= "                                          AND OUTG.OUTGO_L_CD  = SCHD.OUTGO_L_CD ";
        $query .= "                                          AND OUTG.OUTGO_M_CD  = SCHD.OUTGO_M_CD ";
        $query .= "                                          AND OUTG.REQUEST_NO  = SCHD.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT MEIS ON MEIS.SCHOOLCD    = SCHD.SCHOOLCD ";
        $query .= "                                                 AND MEIS.SCHOOL_KIND = SCHD.SCHOOL_KIND ";
        $query .= "                                                 AND MEIS.YEAR        = SCHD.YEAR ";
        $query .= "                                                 AND MEIS.REQUEST_NO  = SCHD.REQUEST_NO ";
        $query .= "                                                 AND MEIS.LINE_NO     = SCHD.LINE_NO ";
        $query .= "                                                 AND MEIS.OUTGO_L_CD  = SCHD.OUTGO_L_CD ";
        $query .= "                                                 AND MEIS.OUTGO_M_CD  = SCHD.OUTGO_M_CD ";
        $query .= "                                                 AND MEIS.OUTGO_S_CD  = SCHD.OUTGO_S_CD ";
        $query .= "     LEFT JOIN LEVY_SGL_LMS_MAPPING_MST SGLM ON SGLM.SCHOOLCD    = SCHD.SCHOOLCD ";
        $query .= "                                            AND SGLM.SCHOOL_KIND = SCHD.SCHOOL_KIND ";
        $query .= "                                            AND SGLM.YEAR        = SCHD.YEAR ";
        $query .= "                                            AND SGLM.LEVY_L_CD   = SCHD.OUTGO_L_CD ";
        $query .= "                                            AND SGLM.LEVY_M_CD   = SCHD.OUTGO_M_CD ";
        $query .= "                                            AND SGLM.LEVY_S_CD   = SCHD.OUTGO_S_CD ";
        $query .= "     LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT MAPP ON MAPP.SCHREGNO = SCHD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         SCHD.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHD.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND (   OUTG.OUTGO_DATE  between '{$fromDate}' AND '{$toDate}' ";
        $query .= "          OR OUTG.HENKIN_DATE between '{$fromDate}' AND '{$toDate}') ";
        $query .= "     AND OUTG.OUTGO_CANCEL is null "; // キャンセル伝票除く
        $query .= "     AND (   value(OUTG.OUTGO_APPROVAL,  '') = '1' ";  // 決済済み
        $query .= "          OR value(OUTG.HENKIN_APPROVAL, '') = '1') "; // 決済済み(返金)
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     SCHD.SCHOOL_KIND, ";
        $query .= "     SCHD.REQUEST_NO, ";
        $query .= "     SCHD.LINE_NO, ";
        $query .= "     SCHD.SCHREGNO ";

        return $query;
    }

    //CSVデータ出力
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

}
?>
