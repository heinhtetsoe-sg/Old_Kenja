<?php

require_once('for_php7.php');


class knja227Query extends Query {

    //塾一覧取得
    function selectPriSchoolQuery($model) {
        //塾名を全角スペースで分割
        $prischoolName = explode('　', $model->field["SEARCH_PRISCHOOL_NAME"]);
        $prischoolClassName = explode('　', $model->field["SEARCH_PRISCHOOL_CLASS_NAME"]);

        $query  = " SELECT DISTINCT ";
        $query .= "     T2.PRISCHOOLCD || '-' || VALUE(B1.BASE_REMARK1,'0000000') AS VALUE, ";
        $query .= "     P1.PRISCHOOL_NAME || '：' || VALUE(P2.PRISCHOOL_NAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = T2.PRISCHOOLCD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
        $query .= "         AND T3.SCHOOL_KIND IN ('J','H') ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND B1.BASE_SEQ = '010' ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST P2 ON P2.PRISCHOOLCD = T2.PRISCHOOLCD ";
        $query .= "         AND P2.PRISCHOOL_CLASS_CD = B1.BASE_REMARK1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //塾コード
        if (strlen($model->field["SEARCH_PRISCHOOLCD"])) {
            $query .= "     AND P1.PRISCHOOLCD LIKE '%".$model->field["SEARCH_PRISCHOOLCD"]."%' ";
        }
        //塾名
        if (strlen($model->field["SEARCH_PRISCHOOL_NAME"])) {
            $query .= "     AND ( ";
            $or = "";
            for ($i = 0; $i < get_count($prischoolName); $i++) {
                if (!strlen($prischoolName[$i])) {
                    continue;
                }
                $query .= $or." P1.PRISCHOOL_NAME LIKE '%".$prischoolName[$i]."%' ";
                $or = "OR";
            }
            $query .= "     ) ";
        }
        //教室名
        if (strlen($model->field["SEARCH_PRISCHOOL_CLASS_NAME"])) {
            $query .= "     AND ( ";
            $or = "";
            for ($i = 0; $i < get_count($prischoolClassName); $i++) {
                if (!strlen($prischoolClassName[$i])) {
                    continue;
                }
                $query .= $or." P2.PRISCHOOL_NAME LIKE '%".$prischoolClassName[$i]."%' ";
                $or = "OR";
            }
            $query .= "     ) ";
        }
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     R1.PRISCHOOLCD || '-' || VALUE(R1.PRISCHOOL_CLASS_CD,'0000000') AS VALUE, ";
        $query .= "     P1.PRISCHOOL_NAME || '：' || VALUE(P2.PRISCHOOL_NAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT R1 ";
        $query .= "     LEFT JOIN RECRUIT_EVENT_DAT E1 ON E1.YEAR = R1.YEAR ";
        $query .= "         AND E1.RECRUIT_NO = R1.RECRUIT_NO ";
        $query .= "     LEFT JOIN RECRUIT_EVENT_YMST E2 ON E2.YEAR = E1.YEAR ";
        $query .= "         AND E2.EVENT_CLASS_CD = E1.EVENT_CLASS_CD ";
        $query .= "         AND E2.EVENT_CD = E1.EVENT_CD ";
        $query .= "     INNER JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = R1.PRISCHOOLCD ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST P2 ON P2.PRISCHOOLCD = R1.PRISCHOOLCD ";
        $query .= "         AND P2.PRISCHOOL_CLASS_CD = R1.PRISCHOOL_CLASS_CD ";
        $query .= " WHERE ";
        $query .= "     R1.YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND R1.SCHOOL_KIND IN ('J','H') ";
        //塾コード
        if (strlen($model->field["SEARCH_PRISCHOOLCD"])) {
            $query .= "     AND P1.PRISCHOOLCD LIKE '%".$model->field["SEARCH_PRISCHOOLCD"]."%' ";
        }
        //塾名
        if (strlen($model->field["SEARCH_PRISCHOOL_NAME"])) {
            $query .= "     AND ( ";
            $or = "";
            for ($i = 0; $i < get_count($prischoolName); $i++) {
                if (!strlen($prischoolName[$i])) {
                    continue;
                }
                $query .= $or." P1.PRISCHOOL_NAME LIKE '%".$prischoolName[$i]."%' ";
                $or = "OR";
            }
            $query .= "     ) ";
        }
        //教室名
        if (strlen($model->field["SEARCH_PRISCHOOL_CLASS_NAME"])) {
            $query .= "     AND ( ";
            $or = "";
            for ($i = 0; $i < get_count($prischoolClassName); $i++) {
                if (!strlen($prischoolClassName[$i])) {
                    continue;
                }
                $query .= $or." P2.PRISCHOOL_NAME LIKE '%".$prischoolClassName[$i]."%' ";
                $or = "OR";
            }
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

}
?>
