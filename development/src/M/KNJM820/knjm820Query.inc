<?php

require_once('for_php7.php');


class knjm820Query extends Query {

    //出欠の記録データ
    function getCsvQuery($model)
    {
        $query  = "  WITH SUBCLASS_STD_SELECT AS (  ";
        $query .= "      SELECT DISTINCT T1.YEAR, T1.SCHREGNO  ";
        $query .= "      FROM SUBCLASS_STD_SELECT_DAT T1  ";
        $query .= "      INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD  ";
        $query .= "          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ";
        $query .= "          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD   ";
        $query .= "          AND T2.SUBCLASSCD = T1.SUBCLASSCD  ";
        $query .= "      WHERE  ";
        $query .= "          (  ";
        if (CTRL_YEAR != $model->field["YEAR"]) {
            $query .= "           T1.YEAR = '".$model->field["YEAR"]."' OR  ";
        }
        $query .= "           T1.YEAR = '".CTRL_YEAR."' OR ";
        $query .= "           T1.YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= "          )  ";
        $query .= "          AND T1.CLASSCD < '90'  ";
        $query .= "  )  ";
        $query .= "  SELECT  ";
        $query .= "      T1.SCHREGNO,  ";
        $query .= "      T5.NAME,  ";
        $query .= "      T6.NAME2 AS SEX_NAME,  ";
        $query .= "      T7.SEND_ZIPCD,  ";
        $query .= "      T7.SEND_ADDR1,  ";
        $query .= "      T7.SEND_ADDR2,  ";
        $query .= "      T7.SEND_TELNO  ";
        $query .= "  FROM  ";
        $query .= "      SCHREG_REGD_DAT T1  ";
        if (CTRL_YEAR != $model->field["YEAR"]) {
            $query .= "  LEFT JOIN SUBCLASS_STD_SELECT T2 ON T2.YEAR = '".$model->field["YEAR"]."' AND T2.SCHREGNO = T1.SCHREGNO  ";
        }
        $query .= "  LEFT JOIN SUBCLASS_STD_SELECT T3 ON T3.YEAR = '".CTRL_YEAR."' AND T3.SCHREGNO = T1.SCHREGNO  ";
        $query .= "  LEFT JOIN SUBCLASS_STD_SELECT T4 ON T4.YEAR = '".(CTRL_YEAR - 1)."' AND T4.SCHREGNO = T1.SCHREGNO  ";
        $query .= "  INNER JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO  ";
        $query .= "  LEFT JOIN NAME_MST T6 ON T6.NAMECD1 = 'Z002'  ";
        $query .= "      AND T6.NAMECD2 = T5.SEX  ";
        $query .= "  LEFT JOIN SCHREG_SEND_ADDRESS_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO  ";
        $query .= "      AND T7.DIV = '1'  ";
        $query .= "  WHERE  ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "      AND T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= "      AND FISCALYEAR(T5.ENT_DATE) < '".CTRL_YEAR."'  ";
        $query .= "      AND (T5.GRD_DIV IS NULL OR T5.GRD_DIV = '4')  ";
        if (CTRL_YEAR != $model->field["YEAR"]) {
            $query .= "      AND T2.SCHREGNO IS NULL  ";
        }
        $query .= "      AND T3.SCHREGNO IS NULL  ";
        $query .= "      AND T4.SCHREGNO IS NULL  ";
        $query .= "  ORDER BY  ";
        $query .= "      T1.SCHREGNO  ";
        $query .= "  ";
        return $query;
    }

}
?>
