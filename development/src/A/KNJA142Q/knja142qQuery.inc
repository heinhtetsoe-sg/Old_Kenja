<?php

require_once('for_php7.php');

class knja142qQuery extends Query
{

    //校種取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '".CTRL_YEAR."' ";
        $query .= "   AND NAMECD1 = 'A023' ";
        $query .= "   AND NAME1   IN ('J','H') "; //固定 中学・高校
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //和暦変換
    public function getWarekiCd($setDay)
    {
        $setDay = str_replace("-", "/", $setDay);
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "       NAMECD1 = 'L007' ";
        $query .= "   AND '{$setDay}' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //CSVデータ
    public function getCsvData($model)
    {
        $r2Namecd1 = ($model->field["SCHOOL_KIND"] == "J") ? "L024" : "L004"; //入試区分
        $query .= " SELECT ";
        $query .= "   BD003.BASE_REMARK1 AS BD003_R1, ";
        $query .= "   R2NAME.NAME2 AS BD003_R2, ";
        $query .= "   BASE.INOUTCD, ";
        if ($model->field["SCHOOL_KIND"] == "J") {
            // 中学の場合 ⇒ 2：内進, 2：外進, それ以外：空白
            $query .= "   CASE WHEN BASE.INOUTCD = '2' THEN '内進' ";
            $query .= "        ELSE CASE WHEN BASE.INOUTCD = '3' THEN '外進' ELSE '' END ";
            $query .= "   END AS INOUT, ";
        } else {
            // 高校の場合 ⇒ 2/3：内進, 1：外進, それ以外：空白
            $query .= "   CASE WHEN BASE.INOUTCD IN ('2', '3') THEN '内進' ";
            $query .= "        ELSE CASE WHEN BASE.INOUTCD = '1' THEN '外進' ELSE '' END ";
            $query .= "   END AS INOUT, ";
        }
        $query .= "   REGD.SCHREGNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.NAME_KANA, ";
        $query .= "   BASE.BIRTHDAY, ";
        $query .= "   L007.NAME1 AS L007_NAME1, ";
        $query .= "   L007.NAMESPARE1 AS L007_NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT REGD ";
        $query .= "   INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "           ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_BASE_DETAIL_MST BD003 ";
        $query .= "          ON BD003.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         AND BD003.BASE_SEQ = '003' ";
        $query .= "   LEFT JOIN NAME_MST L007 ";
        $query .= "          ON L007.NAMECD1 = 'L007' ";
        $query .= "         AND BASE.BIRTHDAY BETWEEN DATE(REPLACE(L007.NAMESPARE2, '/', '-')) AND DATE(REPLACE(L007.NAMESPARE3, '/', '-')) ";
        $query .= "   LEFT JOIN V_NAME_MST R2NAME ";
        $query .= "          ON R2NAME.NAMECD1 = '".$r2Namecd1."' ";
        $query .= "         AND R2NAME.NAMECD2 = BD003.BASE_REMARK2 ";
        $query .= "         AND R2NAME.YEAR    = REGD.YEAR ";
        $query .= "   LEFT JOIN V_NAME_MST L024 ";
        $query .= "          ON L024.NAMECD1 = 'L024' ";
        $query .= "         AND L024.NAMECD2 = BD003.BASE_REMARK2 ";
        $query .= "         AND L024.YEAR    = REGD.YEAR ";
        $query .= "   INNER JOIN V_NAME_MST A023 ";
        $query .= "           ON A023.NAMECD1 = 'A023' ";
        $query .= "          AND A023.NAME1   = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "          AND A023.NAME2   = REGD.GRADE ";
        $query .= "          AND A023.YEAR    = REGD.YEAR ";
        $query .= " WHERE ";
        $query .= "       REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "   AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }
}
