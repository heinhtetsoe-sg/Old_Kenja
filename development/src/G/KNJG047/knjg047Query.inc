<?php

require_once('for_php7.php');

class knjg047Query extends Query {

    //学期開始日、終了日を取得
    function getSemedayQuery($flg="") {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     SDATE ";
        } else {
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '9' ";

        return $query;
    }
        
    //日付を取得
    function getdayHeaderQuery($model) {
    
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.DIARY_DATE ";
        $query .= " FROM  ";
        $query .= "     V_STAFF_MST T1  ";
        $query .= "     LEFT JOIN SCHOOL_DIARY_DETAIL_DAT L1 ON L1.STAFFCD = T1.STAFFCD  ";
        $query .= "                              AND L1.STAFF_DIV IN ('5', '6') ";
        $query .= "                              AND L1.DIARY_DATE BETWEEN '". str_replace("/", "-", $model->field["DATE_FROM"])."' AND '".str_replace("/", "-", $model->field["DATE_TO"])."' ";
        $query .= "  WHERE  ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "  AND T1.STAFFCD IN ( ";
        $query .= "                 SELECT  ";
        $query .= "                     K1.STAFFCD  ";
        $query .= "                 FROM  ";
        $query .= "                     V_STAFF_MST K1  ";
        $query .= "                     LEFT JOIN SCHOOL_DIARY_DETAIL_DAT D1 ON D1.STAFFCD = K1.STAFFCD  ";
        $query .= "                                                         AND D1.STAFF_DIV IN ('5', '6') ";
        $query .= "                  WHERE  ";
        $query .= "                      K1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "                  AND D1.DIARY_DATE BETWEEN '".str_replace("/", "-", $model->field["DATE_FROM"])."' AND '".str_replace("/", "-", $model->field["DATE_TO"])."' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     L1.DIARY_DATE ";

        return $query;
    }
        
    //CSV出力データ
    function getCsvQuery($model, $dayHeader) {

        $query  = " WITH GET_SUM AS ( ";
        $query .= "     SELECT  ";
        $query .= "        T1.STAFFCD,  ";
        $query .= "        T1.STAFFNAME_SHOW, ";
        $query .= "        SUM(L1.COUNT) AS SUM_CNT ";
        $query .= "     FROM  ";
        $query .= "        V_STAFF_MST T1  ";
        $query .= "        LEFT JOIN SCHOOL_DIARY_DETAIL_DAT L1 ON L1.STAFFCD = T1.STAFFCD  ";
        $query .= "                                 AND L1.STAFF_DIV IN ('5', '6') ";
        $query .= "     WHERE  ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "     AND L1.DIARY_DATE BETWEEN '".str_replace("/", "-", $model->field["DATE_FROM"])."' AND '".str_replace("/", "-", $model->field["DATE_TO"])."' ";
        $query .= "     GROUP BY ";
        $query .= "        T1.STAFFCD,  ";
        $query .= "        T1.STAFFNAME_SHOW ";
        $query .= "     ORDER BY ";
        $query .= "         T1.STAFFCD ";
        for ($i = 0; $i < get_count($dayHeader); $i++) {
            $query .= " ), GET_DATE$i AS ( ";
            $query .= " SELECT  ";
            $query .= "     T1.STAFFCD,  ";
            $query .= "     T1.STAFFNAME_SHOW, ";
            $query .= "     L1.DIARY_DATE, ";
            $query .= "     SUM(L1.COUNT) AS COUNT ";
            $query .= " FROM  ";
            $query .= "     V_STAFF_MST T1  ";
            $query .= "     LEFT JOIN SCHOOL_DIARY_DETAIL_DAT L1 ON L1.STAFFCD = T1.STAFFCD  ";
            $query .= "                              AND L1.STAFF_DIV IN ('5', '6') ";
            $query .= "                              AND L1.DIARY_DATE = '".str_replace('/', '-', $dayHeader["DIARY_DATE".$i])."' ";
            $query .= " WHERE  ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."'  ";
            $query .= " AND T1.STAFFCD IN ( ";
            $query .= "                     SELECT  ";
            $query .= "                         K1.STAFFCD  ";
            $query .= "                     FROM  ";
            $query .= "                         V_STAFF_MST K1  ";
            $query .= "                         LEFT JOIN SCHOOL_DIARY_DETAIL_DAT D1 ON D1.STAFFCD = K1.STAFFCD  ";
            $query .= "                                                             AND D1.STAFF_DIV IN ('5', '6') ";
            $query .= "                      WHERE  ";
            $query .= "                          K1.YEAR = '".CTRL_YEAR."'  ";
            $query .= "                      AND D1.DIARY_DATE BETWEEN '".str_replace("/", "-", $model->field["DATE_FROM"])."' AND '".str_replace("/", "-", $model->field["DATE_TO"])."' ";
            $query .= "                     ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.STAFFCD,  ";
            $query .= "     T1.STAFFNAME_SHOW, ";
            $query .= "     L1.DIARY_DATE     ";
            $query .= "  ORDER BY  ";
            $query .= "     T1.STAFFCD ";
            if ($i == get_count($dayHeader) - 1) {
                $query .= " ) ";
            }
        }
        $query .= " SELECT ";
        $query .= "     T1.STAFFCD  ";
        $query .= "    ,T1.STAFFNAME_SHOW ";
        $query .= "    ,T1.SUM_CNT ";
        for ($i = 0; $i < get_count($dayHeader); $i++) {
            $query .= "    ,L$i.COUNT AS DIARY_DATE$i ";
        }
        $query .= " FROM ";
        $query .= "     GET_SUM T1 ";
        for ($i = 0; $i < get_count($dayHeader); $i++) {
            $query .= "     LEFT JOIN GET_DATE$i L$i ON L$i.STAFFCD = T1.STAFFCD ";
        }
        
        return $query;
    }
}
?>
