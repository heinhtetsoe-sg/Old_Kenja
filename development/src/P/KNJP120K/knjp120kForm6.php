<?php

require_once('for_php7.php');

require_once('knjp120kQuery2.inc');
class knjp120kForm6
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp120kindex.php", "", "edit");
        $arg["data"] = array();

        $result = $db->query(knjp120kQuery2::getList3($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            //リンク設定
            $row["APPLICATIONCD"] = $row["APPLICATIONCD"];
            //金額フォーマット
            $row["APPLI_MONEY_DUE"] = (strlen($row["APPLI_MONEY_DUE"])) ? number_format($row["APPLI_MONEY_DUE"]): "";
            $row["APPLI_PAID_MONEY"] = (strlen($row["APPLI_PAID_MONEY"])) ? number_format($row["APPLI_PAID_MONEY"]): "";

            //日付変換
            $row["APPLIED_DATE"] = str_replace("-", "/", $row["APPLIED_DATE"]);
            $row["APPLI_PAID_DATE"] = str_replace("-", "/", $row["APPLI_PAID_DATE"]);

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
    
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp120kForm6.html", $arg);
    }
}
?>
