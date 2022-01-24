<?php

require_once('for_php7.php');

class knjtx009Query extends Query {

    //マスタのデータを削除
    function getDeleteQuery($citycd)
    {
        $query = "DELETE FROM ZIPCD_MST WHERE CITYCD ='".$citycd."'";
        return $query;
    }
    //ワークテーブルにCSVデータを挿入
    function insertQueryCsv($file)
    {
        $data["ZIPNO"][TEXT]        = $file["ZIPNO"];
        $data["CITYCD"][TEXT]       = $file["CITYCD"];
        $data["OLD_ZIPCD"][TEXT]    = $file["OLD_ZIPCD"];
        $data["NEW_ZIPCD"][TEXT]    = $file["NEW_ZIPCD"];
        $data["PREF_KANA"][TEXT]    = $file["PREF_KANA"];
        $data["CITY_KANA"][TEXT]    = $file["CITY_KANA"];
        $data["TOWN_KANA"][TEXT]    = $file["TOWN_KANA"];
        $data["PREF"][TEXT]         = $file["PREF"];
        $data["CITY"][TEXT]         = $file["CITY"];
        $data["TOWN"][TEXT]         = $file["TOWN"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ZIPCD_MST");
        return $query;
    }
    //ZIPNOのMAX値を取得
    function maxZipNo()
    {
        $db = Query::dbCheckOut();
        $query  = "SELECT MAX(ZIPNO) as ZIPNO FROM ZIPCD_MST ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["ZIPNO"];
    }
}
?>
