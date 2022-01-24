<?php

require_once('for_php7.php');

class knjm812bQuery extends Query {


    //地域・住所取得
    function getList($model)
    {
        $query  = " WITH T_ZIPCD AS (";
        $query .= "   SELECT NEW_ZIPCD AS ZIPCD, MIN (CITY || '-' || TOWN) AS ZIPNAME ";
        $query .= "   FROM ZIPCD_MST GROUP BY NEW_ZIPCD ";
        $query .= " ), SUBCLASS_STD AS ( ";
        $query .= "  SELECT T1.SCHREGNO, T1.YEAR ";
        $query .= "  FROM SUBCLASS_STD_SELECT_DAT T1 ";
        $query .= "  INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
        $query .= "      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "      AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "      AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "  GROUP BY  T1.SCHREGNO, T1.YEAR ";
        $query .= " ), SCHREG_ADDRESS AS ( ";
        $query .= "  SELECT T1.* ";
        $query .= "  FROM SCHREG_ADDRESS_DAT T1 ";
        $query .= "  INNER JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "      AND T2.ISSUEDATE = T1.ISSUEDATE ";
        $query .= " ) ";
        $query  .= "  SELECT DISTINCT ";
        if ($model->field["LIST_TYPE"] === '1') {
            if ($model->field["OUTPUT"] === '1' || $model->field["OUTPUT"] === '2' || $model->field["OUTPUT"] === '5') {
                $query .= "      T1.AREACD || '　' || L1.NAME1 AS LABEL, ";
                $query .= "      T1.AREACD AS VALUE ";
            } else {
                $query .= "      T1.SEND_AREACD || '　' || L1.NAME1 AS LABEL, ";
                $query .= "      T1.SEND_AREACD AS VALUE ";
            }
        } else {
            if ($model->field["OUTPUT"] === '1' || $model->field["OUTPUT"] === '2' || $model->field["OUTPUT"] === '5') {
                $query .= "      '〒' || T1.ZIPCD || ' | ' || VALUE(L1.ZIPNAME, '') AS LABEL, ";
                $query .= "      T1.ZIPCD AS VALUE ";
            } else {
                $query .= "      '〒' || T1.SEND_ZIPCD || ' | ' || VALUE(L1.ZIPNAME, '') AS LABEL, ";
                $query .= "      T1.SEND_ZIPCD AS VALUE ";
            }
        }
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT T0 ";
        if ($model->field["LIST_TYPE"] === '1') {
            //地区一覧
            if ($model->field["OUTPUT"] === '1' || $model->field["OUTPUT"] === '2' || $model->field["OUTPUT"] === '5') {
                $query .= "  INNER JOIN SCHREG_ADDRESS T1 ON T1.SCHREGNO = T0.SCHREGNO ";
                $query .= "  INNER JOIN NAME_MST L1 ON L1.NAMECD1 = 'A020' ";
                $query .= "                        AND L1.NAMECD2 = T1.AREACD ";
            } else {
                $query .= "  INNER JOIN SCHREG_SEND_ADDRESS_DAT T1 ON T1.SCHREGNO = T0.SCHREGNO ";
                $query .= "  INNER JOIN NAME_MST L1 ON L1.NAMECD1 = 'A020' ";
                $query .= "                        AND L1.NAMECD2 = T1.SEND_AREACD ";
            }
            $query .= "  INNER JOIN SUBCLASS_STD T4 ON T4.SCHREGNO = T0.SCHREGNO AND T4.YEAR = T0.YEAR ";
        } else {
            //住所一覧
            if ($model->field["OUTPUT"] === '1' || $model->field["OUTPUT"] === '2' || $model->field["OUTPUT"] === '5') {
                $query .= "  INNER JOIN SCHREG_ADDRESS T1 ON T1.SCHREGNO = T0.SCHREGNO ";
                $query .= "  INNER JOIN T_ZIPCD L1 ON L1.ZIPCD = T1.ZIPCD ";
            } else {
                $query .= "  INNER JOIN SCHREG_SEND_ADDRESS_DAT T1 ON T1.SCHREGNO = T0.SCHREGNO ";
                $query .= "  INNER JOIN T_ZIPCD L1 ON L1.ZIPCD = T1.SEND_ZIPCD ";
            }
        }
        $query .= "  WHERE ";
        $query .= "     T0.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T0.SEMESTER = '".$model->field["GAKKI"]."' ";
        if ($model->field["OUTPUT"] === '3') {
            $query .= "     AND T1.DIV = '1' ";
        } else if ($model->field["OUTPUT"] === '4') {
            $query .= "     AND T1.DIV = '2' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

}
?>
