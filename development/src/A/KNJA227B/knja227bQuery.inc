<?php

require_once('for_php7.php');


class knja227bQuery extends Query {

    //塾一覧取得
    function selectPriSchoolQuery($model) {
        //塾名を全角スペースで分割
        $prischoolName = explode('　', $model->field["SEARCH_PRISCHOOL_NAME"]);
        $prischoolClassName = explode('　', $model->field["SEARCH_PRISCHOOL_CLASS_NAME"]);

        $query  = " SELECT ";
        $query .= "     P1.PRISCHOOLCD || '-' || VALUE(P2.PRISCHOOL_CLASS_CD,'0000000') AS VALUE, ";
        $query .= "     P1.PRISCHOOL_NAME || '：' || VALUE(P2.PRISCHOOL_NAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST P1 ";
        $query .= "     LEFT JOIN PRISCHOOL_CLASS_MST P2 ON P2.PRISCHOOLCD = P1.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     P1.PRISCHOOL_NAME IS NOT NULL ";
        $query .= "     AND VALUE(P2.DIRECT_MAIL_FLG,'0') != '1' ";
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

    //説明会(午前、午後)
    function getBriefingAmPm() {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '午前') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '午後') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //説明会(時)
    function getBriefingHour() {
        $query  = " WITH T_COMMON(SORT, VALUE, LABEL) AS ( ";
        for ($i = 0; $i <= 12; $i++) {
            if ($i > 0) $query .= "     UNION ALL ";
            $hour = $i;
            $sort = sprintf("%02d", $i);
            $query .= "     VALUES('{$sort}', '{$hour}', '{$hour}時') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";
        return $query;
    }

    //説明会(分)
    function getBriefingMinute() {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        for ($i = 0; $i <= 59; $i++) {
            if ($i > 0) $query .= "     UNION ALL ";
            $minute = sprintf("%02d", $i);
            $query .= "     VALUES('{$minute}', '{$minute}分') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
?>
