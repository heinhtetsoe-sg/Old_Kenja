<?php

require_once('for_php7.php');

class knjd425Query extends Query {

    function getGuidanceKindName($model, $specifyschregflg="", $kno = "") {
        $query  = " SELECT ";
        $query .= "  T1.KIND_NO, ";
        $query .= "  T2.KIND_NAME AS BTN_SUBFORMCALL, ";
        $query .= "  CASE WHEN T1.KIND_NO = '01' THEN '1' ";
        $query .= "       WHEN T1.KIND_NO IN ('04', '09') THEN '2' ";
        $query .= "       WHEN T1.KIND_NO IN ('02', '03', '05', '06') THEN '3' ";
        $query .= "       WHEN T1.KIND_NO = '10' THEN '4' ";
        $query .= "       WHEN T1.KIND_NO = '07' THEN '5' ";
        $query .= "       WHEN T1.KIND_NO = '08' THEN '6' ";
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

    //更新日取得(※最新を最後にするために、上位ではLIFO形式で処理する。そのため、降順で取得している。)
    function getUpdatedDateList($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "  RECORD_DATE AS LABEL, ";
        $query .= "  RECORD_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "  HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "  YEAR = '{$model->exp_year}' ";
        $query .= "  AND SEMESTER = '9' ";
        $query .= "  AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "  RECORD_DATE DESC";
        return $query;
    }

    //生徒詳細情報(氏名、生年月日等)取得
    function getSchInfoShousai($model)
    {
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO,  ";
        $query .= "      T3.SCHOOL_KIND,  ";
        $query .= "      N1.ABBV1 AS GAKUBU_NAME, ";
        $query .= "      T4.HR_NAME, ";
        $query .= "      G2.GHR_NAME, ";
        $query .= "      T1.NAME_SHOW,  ";
        $query .= "      T1.NAME_KANA,  ";
        $query .= "      T1.BIRTHDAY,  ";
        $query .= "      N2.NAME2 AS SEX_NAME ";
        $query .= "  FROM ";
        $query .= "      V_SCHREG_BASE_MST T1 ";
        $query .= "      LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "      LEFT JOIN SCHREG_REGD_GDAT T3 ON T2.YEAR = T3.YEAR ";
        $query .= "                                   AND T2.GRADE = T3.GRADE ";
        $query .= "      LEFT JOIN SCHREG_REGD_HDAT T4 ON T2.YEAR = T4.YEAR ";
        $query .= "                                   AND T2.SEMESTER = T4.SEMESTER  ";
        $query .= "                                   AND T2.GRADE || T2.HR_CLASS = T4.GRADE || T4.HR_CLASS  ";
        $query .= "      LEFT JOIN SCHREG_REGD_GHR_DAT G1 ON G1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                                      AND G1.YEAR =T2.YEAR ";
        $query .= "                                      AND G1.SEMESTER = T2.SEMESTER ";
        $query .= "      LEFT JOIN SCHREG_REGD_GHR_HDAT G2 ON G2.YEAR = G1.YEAR ";
        $query .= "                                       AND G2.SEMESTER = G1.SEMESTER ";
        $query .= "                                       AND G2.GHR_CD = G1.GHR_CD ";
        $query .= "      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T2.YEAR ";
        $query .= "                             AND N1.NAMECD1 = 'A023' ";
        $query .= "                             AND N1.NAME1 = T3.SCHOOL_KIND ";
        $query .= "      LEFT JOIN V_NAME_MST N2 ON T2.YEAR = N2.YEAR ";
        $query .= "                             AND N2.NAMECD1 = 'Z002' ";
        $query .= "                             AND T1.SEX = N2.NAMECD2 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  AND T2.YEAR     = '".$model->exp_year."' ";
        $query .= "  AND T2.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //KNJE390を元に作成。
    //障害名、概要を取得。
    function getSubQuery1($model, $setSanshouData="")
    {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."'";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";

        return $query;
    }

    //KNJE390を元に作成。
    //レコードの日付取得 (全て表示)
    function getTorikomiRecordDate($model)
    {
        $settableName = "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT";

        $query  = " SELECT ";
        $query .= "      RECORD_DATE AS VALUE, ";
        $query .= "      CASE WHEN RECORD_DATE = 'NEW' THEN '最新' ELSE REPLACE(RECORD_DATE,'-','/') END AS LABEL ";
        $query .= "  FROM ";
        $query .= "      $settableName ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$model->exp_year."' ";
        $query .= "  AND SCHREGNO = '".$model->schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //障害種別、作成日、作成者を取得
    function getSubQuery2CheckRecordList($model, $setSanshouData="")
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //障害種別、作成日、作成者(DIV='01'の情報)を取得
    function getRemarkDiv01Data($model)
    {
        $dstr = str_replace("/", "-", $model->upddate);
        $query .= " SELECT ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.SEQ, ";
        $query .= "     CASE WHEN T1.SEQ = 3 THEN T2.STAFFNAME_SHOW ";
        $query .= "          WHEN T1.SEQ = 1 THEN ";
        $query .= "               CASE WHEN D091.NAME1 IS NULL THEN T1.REMARK ELSE D091.NAME1 END ";
        $query .= "          ELSE T1.REMARK END AS REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST T2 ";
        $query .= "       ON T2.STAFFCD = T1.REMARK ";
        $query .= "     LEFT JOIN V_NAME_MST D091 ";
        $query .= "       ON D091.YEAR = '{$model->exp_year}' ";
        $query .= "      AND D091.NAMECD1 = 'D091' ";
        $query .= "      AND D091.NAMECD2 = T1.REMARK ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.SEMESTER = '9' ";
        $query .= "   AND T1.RECORD_DATE = '{$dstr}' ";
        $query .= "   AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND T1.DIV = '01' ";
        return $query;
    }

}
?>
