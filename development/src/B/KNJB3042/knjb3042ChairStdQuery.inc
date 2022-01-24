<?php

require_once('for_php7.php');

class knjb3042ChairStdQuery extends Query
{

    /*
     * (f)
     * 重複する講座一覧を取得
     */
    //重複講座コンボ
    public function getDupChairDat($model)
    {
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRCD AS VALUE, ";
        $query .= "     CHAIR.CHAIRCD || ':' || CHAIR.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /*
     * (f)
     * 指定講座の科目を取得し、科目にぶら下がる講座一覧を取得
     * （指定した講座を含む）
     */
    //科目-講座
    public function getDupChairDat2($model)
    {
        return $query;
    }
}
