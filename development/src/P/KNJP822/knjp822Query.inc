<?php

require_once('for_php7.php');

class knjp822query extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/
    //年度取得
    public function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["SELYEAR"]."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["SELYEAR"]}' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SELSCHKIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //CSVデータ出力
    public function selectQuery($model)
    {
        $query  = " WITH SCHLIST AS ( ";
        $query .= " SELECT ";
        $query .= "  DISTINCT ";
        $query .= "  T1.SCHREGNO ";
        $query .= " from ";
        $query .= "  SCHREG_REGD_DAT T1 ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "      ON T2.YEAR  = T1.YEAR ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '".$model->field["SELYEAR"]."' ";
        if ($model->field["SELGRADE"] != "ALL") {
            $query .= "  AND T1.GRADE = '".$model->field["SELGRADE"]."' ";
        }
        $query .= "  AND T2.SCHOOL_KIND = '".$model->field["SELSCHKIND"]."' ";
        $query .= " ), NEW_ZIPCD_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         NEW_ZIPCD, ";
        $query .= "         PREF ";
        $query .= "     FROM ";
        $query .= "         ZIPCD_MST ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= " T1.SCHREGNO, ";
        $query .= " (CASE WHEN T2.GUARANTOR_NAME IS NULL THEN T2.GUARD_NAME ELSE T2.GUARANTOR_NAME END) AS GNAME, ";
        $query .= " (CASE WHEN T2.GUARANTOR_KANA IS NULL THEN T2.GUARD_KANA ELSE T2.GUARANTOR_KANA END) AS GKANA, ";
        $query .= " PREFM.PREF_CD AS PREF_CD, ";
        $query .= " PREFM.PREF_NAME AS PREF_NAME, ";
        $query .= " (CASE WHEN T2.GUARANTOR_NAME IS NULL THEN T2.GUARD_ZIPCD ELSE T2.GUARANTOR_ZIPCD END) AS ZIP_NO, ";
        $query .= " (CASE WHEN T2.GUARANTOR_NAME IS NULL THEN T2.GUARD_ADDR1 ELSE T2.GUARANTOR_ADDR1 END) AS ADDRESS1, ";
        $query .= " (CASE WHEN T2.GUARANTOR_NAME IS NULL THEN T2.GUARD_ADDR2 ELSE T2.GUARANTOR_ADDR2 END) AS ADDRESS2, ";
        $query .= " (CASE WHEN T2.GUARANTOR_NAME IS NULL THEN T2.GUARD_TELNO ELSE T2.GUARANTOR_TELNO END) AS TELNO, ";
        if ($model->field["SELSCHKIND"] == "J") {
            $query .= " T3.SIGEL_NO_J AS SIGEL_NO, ";
            $query .= " T3.SCHREGNO_J AS SCHREGNO_SUB, ";
        } elseif ($model->field["SELSCHKIND"] == "H") {
            $query .= " T3.SIGEL_NO_H AS SIGEL_NO, ";
            $query .= " T3.SCHREGNO_H AS SCHREGNO_SUB, ";
        } else {
            $query .= " '' AS SIGEL_NO, ";
            $query .= " '' AS SCHREGNO_SUB, ";
        }
        $query .= " '1' AS GUARANTOR_TYPE, ";
        $query .= " '0' AS VER_NO ";
        $query .= " FROM ";
        $query .= "  SCHLIST T1 ";
        $query .= "  INNER JOIN GUARDIAN_DAT T2 ";
        $query .= "    ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "  LEFT JOIN NEW_ZIPCD_T ZIPM ";
        $query .= "    ON ZIPM.NEW_ZIPCD = CASE WHEN T2.GUARANTOR_NAME IS NULL THEN T2.GUARD_ZIPCD ELSE T2.GUARANTOR_ZIPCD END ";
        $query .= "  LEFT JOIN PREF_MST PREFM ";
        $query .= "    ON PREFM.PREF_NAME = ZIPM.PREF ";
        $query .= "  LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT T3 ";
        $query .= "    ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= " T1.SCHREGNO ";

        return $query;
    }
}
