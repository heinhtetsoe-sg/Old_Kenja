<?php

require_once('for_php7.php');

class knjz030eQuery extends Query
{
    /**********/
    /* 左画面 */
    /**********/
    //入試年度一覧取得
    public function selectYearQuery()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SLIDE_GROUP_YMST ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    public function DeleteAtExist($model)
    {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SLIDE_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '{$model->year}' ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $default="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        if ($default) {
            $query .= "    ,NAMESPARE2 AS DEFAULT ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //左画面 一覧表示
    public function Listdata($model, $flg="")
    {
        $nameCd1 = ($model->slide_div == '1') ? 'L058': 'L045';

        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     SLID.DESIREDIV, ";
            $query .= "     SLID.TESTDIV1, ";
            $query .= "     SLID.SUC_DESIREDIV, ";
            $query .= "     COUNT(SLID.DESIREDIV) AS CNT ";
        } else {
            $query .= "     SLID.DESIREDIV, ";
            $query .= "     SLID.DESIREDIV || ':' || L058_1.NAME1 as DESIREDIV_NAME, ";
            $query .= "     SLID.TESTDIV1, ";
            $query .= "     SLID.TESTDIV1|| ':' || L045_1.NAME1 as TESTDIV1_NAME, ";
            $query .= "     SLID.SUC_DESIREDIV, ";
            $query .= "     SLID.SUC_DESIREDIV || ':' || L058_2.NAME1 as SUC_DESIREDIV_NAME, ";
            $query .= "     SLID.SUC_TESTDIV1, ";
            $query .= "     SLID.SUC_TESTDIV1|| ':' || L045_2.NAME1 as SUC_TESTDIV1_NAME ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_SLIDE_GROUP_YMST SLID ";
        $query .= "     LEFT JOIN V_NAME_MST L058_1 ON L058_1.YEAR    = SLID.ENTEXAMYEAR ";
        $query .= "                                      AND L058_1.NAMECD1 = 'L058' ";
        $query .= "                                      AND L058_1.NAMECD2 = SLID.DESIREDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L058_2 ON L058_2.YEAR    = SLID.ENTEXAMYEAR ";
        $query .= "                                      AND L058_2.NAMECD1 = 'L058' ";
        $query .= "                                      AND L058_2.NAMECD2 = SLID.SUC_DESIREDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L045_1 ON L045_1.YEAR    = SLID.ENTEXAMYEAR ";
        $query .= "                                      AND L045_1.NAMECD1 = 'L045' ";
        $query .= "                                      AND L045_1.NAMECD2 = SLID.TESTDIV1 ";
        $query .= "     LEFT JOIN V_NAME_MST L045_2 ON L045_2.YEAR    = SLID.ENTEXAMYEAR ";
        $query .= "                                      AND L045_2.NAMECD1 = 'L045' ";
        $query .= "                                      AND L045_2.NAMECD2 = SLID.SUC_TESTDIV1 ";
        $query .= " WHERE ";
        $query .= "         SLID.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND SLID.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($flg == "cnt") {
            $query .= " GROUP BY ";
            $query .= "     SLID.DESIREDIV, ";
            $query .= "     SLID.TESTDIV1, ";
            $query .= "     SLID.SUC_DESIREDIV ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     SLID.DESIREDIV ";
            $query .= "   , SLID.TESTDIV1 ";
            $query .= "   , SLID.SUC_DESIREDIV ";
            $query .= "   , SLID.SUC_TESTDIV1 ";
        }

        return $query;
    }

    /**********/
    /* 右画面 */
    /**********/
    //名称マスタよりNAMESPARE2取得(初期値を取得できていない場合)
    public function getNameSpare2($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMESPARE2  = '1' ";

        return $query;
    }

    //名称マスタより取得(初期値を取得できていない場合)
    public function getNameMstSet($model, $namecd1, $namespare2)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namespare2) {
            $query .= " AND NAMESPARE2 = '".$namespare2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //右画面のリストtoリスト設定
    public function getListCourse($model, $list)
    {

        //左リスト用
        if ($list == 'Llist') {
            $query  = " SELECT ";
            $query .= "     SLID.SUC_TESTDIV1 , ";
            $query .= "     SLID.SUC_TESTDIV1 || ':' || L045_1.NAME1 AS SUC_TESTDIV1_NAME ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_SLIDE_GROUP_YMST SLID ";
            $query .= "     LEFT JOIN V_NAME_MST L045_1 ON L045_1.YEAR    = SLID.ENTEXAMYEAR ";
            $query .= "                                      AND L045_1.NAMECD1 = 'L045' ";
            $query .= "                                      AND L045_1.NAMECD2 = SLID.SUC_TESTDIV1 ";
            $query .= " WHERE ";
            $query .= "         SLID.ENTEXAMYEAR   = '{$model->year}' ";
            $query .= "     AND SLID.APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "     AND SLID.DESIREDIV     = '{$model->desirediv}' ";
            $query .= "     AND SLID.TESTDIV1      = '{$model->testdiv1}' ";
            $query .= "     AND SLID.SUC_DESIREDIV = '{$model->sucDesirediv}' ";
            $query .= " ORDER BY ";
            $query .= "     SLID.SUC_TESTDIV1 ";

        //右リスト用
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     L045_1.NAMECD2 as SUC_TESTDIV1, ";
            $query .= "     L045_1.NAMECD2 || ':' || L045_1.NAME1 AS SUC_TESTDIV1_NAME ";
            $query .= " FROM ";
            $query .= "     V_NAME_MST L045_1 ";
            $query .= " WHERE ";
            $query .= "         L045_1.YEAR    = '{$model->year}' ";
            $query .= "     AND L045_1.NAMECD1 = 'L045' ";
            $query .= "     AND L045_1.NAMECD2 NOT IN (SELECT ";
            $query .= "                                          SUC_TESTDIV1 ";
            $query .= "                                      FROM ";
            $query .= "                                          ENTEXAM_SLIDE_GROUP_YMST ";
            $query .= "                                      WHERE ";
            $query .= "                                             ENTEXAMYEAR   = '{$model->year}' ";
            $query .= "                                         AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "                                         AND DESIREDIV     = '{$model->desirediv}' ";
            $query .= "                                         AND TESTDIV1      = '{$model->testdiv1}' ";
            $query .= "                                         AND SUC_DESIREDIV = '{$model->sucDesirediv}' ";
            $query .= "                                     )";
            $query .= " ORDER BY ";
            $query .= "     L045_1.NAMECD2 ";
        }

        return $query;
    }

    /**************/
    /* コピー処理 */
    /**************/
    //ある年度にデータが存在するのかチェック
    public function getExists2($table, $year, $namecd1="")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        if ($table == "V_NAME_MST") {
            $query .= "     YEAR        = '".$year."' AND ";
            $query .= "     NAMECD1     = '".$namecd1."' ";
        } else {
            $query .= "     ENTEXAMYEAR = '".$year."' ";
        }

        return $query;
    }

    //COPY
    public function getCopyQuery($db, $thisYear, $nextYear)
    {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_SLIDE_GROUP_YMST ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     DESIREDIV, ";
        $query .= "     TESTDIV1, ";
        $query .= "     SUC_DESIREDIV, ";
        $query .= "     SUC_TESTDIV1, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_SLIDE_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        $db->query($query);

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_SLIDE_GROUP_YMST ";
        $query .= "       WHERE ENTEXAMYEAR   = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "         AND DESIREDIV     = '{$model->desirediv}' ";
        $query .= "         AND TESTDIV1      = '{$model->testdiv1}' ";
        $query .= "         AND SUC_DESIREDIV = '{$model->sucDesirediv}' ";

        $db->query($query);

        if ($model->selectdata) {
            $selectarr = array();
            $selectarr = explode(",", $model->selectdata);

            for ($i = 0; $i < get_count($selectarr); $i++) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["DESIREDIV"][TEXT]        = $model->desirediv;
                $data["TESTDIV1"][TEXT]         = $model->testdiv1;
                $data["SUC_DESIREDIV"][TEXT]    = $model->sucDesirediv;
                $data["SUC_TESTDIV1"][TEXT]     = $selectarr[$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_SLIDE_GROUP_YMST");
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
