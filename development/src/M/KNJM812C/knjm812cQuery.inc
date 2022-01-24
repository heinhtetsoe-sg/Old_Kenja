<?php

require_once('for_php7.php');

class knjm812cQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR='" .$year ."' ";
        $query .= "AND SEMESTER='".$semester ."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }
        return $query;
    }


    //生徒取得
    function getSchno($model,$year,$semester)
    {
        $query  = "WITH SUBCLASS_STD AS ( ";
        $query .= "  SELECT T1.SCHREGNO, T1.YEAR ";
        $query .= "  FROM SUBCLASS_STD_SELECT_DAT T1 ";
        $query .= "  INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
        $query .= "      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "      AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "  GROUP BY  T1.SCHREGNO, T1.YEAR ";
        $query .= ") ";

        $query .= "SELECT T1.SCHREGNO,T1.ATTENDNO,T1.GRADE,T1.HR_CLASS,T2.NAME_SHOW ";
        $query .= "FROM   SCHREG_REGD_DAT T1 ";
        $query .= "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "LEFT JOIN SUBCLASS_STD T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = '".(intval($year) - 1)."' ";
        $query .= "LEFT JOIN SUBCLASS_STD T4 ON T4.SCHREGNO = T1.SCHREGNO AND T4.YEAR = '".$year."' ";
        $query .= "LEFT JOIN SUBCLASS_STD T5 ON T5.SCHREGNO = T1.SCHREGNO AND T5.YEAR = '".(intval($year) + 1)."' ";
        $query .= "LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T6 ON T6.SCHREGNO = T1.SCHREGNO AND T5.YEAR = '".$year."' AND T6.BASE_SEQ = '001' ";
        $query .= "WHERE  T1.YEAR       = '".$year."' ";
        $query .= "       AND T1.SEMESTER   = '".$semester."' ";
        if ($model->field["REG"] == "1") { // 除籍候補者
            $query .= "    AND (T3.SCHREGNO IS NULL AND "; // 前年度なし
            $query .= "         T4.SCHREGNO IS NULL AND "; // 今年度なし
            $query .= "         T5.SCHREGNO IS NULL AND ";    // 次年度なし
            $query .= "         FISCALYEAR(T2.ENT_DATE) < '".$year."' ";
            $query .= "         ) ";
        } else if ($model->field["REG"] == "2") { // 履修登録未登録者
            $query .= "    AND (T5.SCHREGNO IS NULL AND  "; // 次年度なし AND NOT 除籍候補者
            $query .= "        NOT (T3.SCHREGNO IS NULL AND "; // 前年度なし
            $query .= "             T4.SCHREGNO IS NULL AND "; // 今年度なし
            $query .= "             T5.SCHREGNO IS NULL AND ";    // 次年度なし
            $query .= "             FISCALYEAR(T2.ENT_DATE) < '".$year."' ";
            $query .= "            )";
            $query .= "        )";
        } else if ($model->field["REG"] == "3") { // 卒業予定者
            $query .= "    AND T6.SCHREGNO IS NOT NULL AND T6.BASE_REMARK1 = '1' "; // 卒業予定者
        } else if ($model->field["REG"] == "4") { // 履修登録者
            $query .= "    AND T4.SCHREGNO IS NOT NULL "; // 履修登録者
        }
        $query .= "ORDER BY T1.SCHREGNO ";

        return $query;
    }

}
?>
