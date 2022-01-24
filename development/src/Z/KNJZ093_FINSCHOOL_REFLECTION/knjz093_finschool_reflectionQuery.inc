<?php

require_once('for_php7.php');

class knjz093_finschool_reflectionQuery extends Query
{

    //名称取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校一覧取得
    public function selectQuery($model, $flg, $finschoolcd = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     CASE WHEN LENGTH(T2.NAME1) > 0 THEN T2.NAME1 ELSE '　' END AS NAME1 ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= "     LEFT  JOIN NAME_MST T2 ON T2.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= "                           AND T2.NAMECD1 = 'L019' "; 
        if ($flg == "list") {
            if ($model->selectFinschoolType != "99") {
                $query .= " WHERE ";
                $query .= "     T1.FINSCHOOL_TYPE = '".$model->selectFinschoolType."' ";
            }
        } else {
            if ($finschoolcd) {
                $query .= " WHERE ";
                $query .= "     T1.FINSCHOOLCD = '".$finschoolcd."' ";
            }
        }

        $query .= " ORDER BY ";
        $query .= "     T1.FINSCHOOLCD ";

        return $query;
    }

    //教育委員会用の学校コード取得
    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //出身学校コード配列に格納
        $array = explode (",", $model->selectdata);
        $db->autoCommit(false);
        $db2->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                //学校出身学校コードチェック
                $query = knjz093_finschool_reflectionQuery::selectQuery($model, "check", $array[$i]);
                $finschool = $db->getOne($query);

                if (!$finschool) {
                    //教育委員会の出身学校マスタを参照
                    $query = knjz093_finschool_reflectionQuery::selectQuery($model, "check", $array[$i]);
                    $edboard = $db2->getRow($query, DB_FETCHMODE_ASSOC);

                    //追加
                    $data = array();
                    $data["FINSCHOOLCD"][TEXT]          = $edboard["FINSCHOOLCD"];
                    $data["FINSCHOOL_TYPE"][TEXT]       = $edboard["FINSCHOOL_TYPE"];
                    $data["FINSCHOOL_DISTCD"][TEXT]     = $edboard["FINSCHOOL_DISTCD"];
                    $data["FINSCHOOL_DISTCD2"][TEXT]    = $edboard["FINSCHOOL_DISTCD2"];
                    $data["FINSCHOOL_DIV"][TEXT]        = $edboard["FINSCHOOL_DIV"];
                    $data["FINSCHOOL_NAME"][TEXT]       = $edboard["FINSCHOOL_NAME"];
                    $data["FINSCHOOL_KANA"][TEXT]       = $edboard["FINSCHOOL_KANA"];
                    $data["FINSCHOOL_NAME_ABBV"][TEXT]  = $edboard["FINSCHOOL_NAME_ABBV"];
                    $data["FINSCHOOL_KANA_ABBV"][TEXT]  = $edboard["FINSCHOOL_KANA_ABBV"];
                    $data["PRINCNAME"][TEXT]            = $edboard["PRINCNAME"];
                    $data["PRINCNAME_SHOW"][TEXT]       = $edboard["PRINCNAME_SHOW"];
                    $data["PRINCKANA"][TEXT]            = $edboard["PRINCKANA"];
                    $data["DISTRICTCD"][TEXT]           = $edboard["DISTRICTCD"];
                    $data["FINSCHOOL_PREF_CD"][TEXT]    = $edboard["FINSCHOOL_PREF_CD"];
                    $data["FINSCHOOL_ZIPCD"][TEXT]      = $edboard["FINSCHOOL_ZIPCD"];
                    $data["FINSCHOOL_ADDR1"][TEXT]      = $edboard["FINSCHOOL_ADDR1"];
                    $data["FINSCHOOL_ADDR2"][TEXT]      = $edboard["FINSCHOOL_ADDR2"];
                    $data["FINSCHOOL_TELNO"][TEXT]      = $edboard["FINSCHOOL_TELNO"];
                    $data["FINSCHOOL_FAXNO"][TEXT]      = $edboard["FINSCHOOL_FAXNO"];
                    $data["EDBOARDCD"][TEXT]            = $edboard["EDBOARDCD"];
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][NUMBER]            = "sysdate()";

                    $db->query(Query::insertSQL($data, "FINSCHOOL_MST"));

                    //学校コード取得
                    $schoolcd = $db->getOne(knjz093_finschool_reflectionQuery::getSchoolCd());

                    //学校別教育委員会テーブルへのデータ追加
                    $data2 = array();
                    $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
                    $data2["FINSCHOOLCD"][TEXT]         = $edboard["FINSCHOOLCD"];
                    $data2["EDBOARD_FLG"][TEXT]         = "1";
                    $data2["REGISTERCD"][TEXT]          = STAFFCD;
                    $data2["UPDATED"][NUMBER]           = "sysdate()";

                    $query = Query::insertSQL($data2, "EDBOARD_FINSCHOOL_DAT");
                    $db2->query($query);
                }
            }
        }
        $db->commit();
        $db2->commit();

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }
}
?>
