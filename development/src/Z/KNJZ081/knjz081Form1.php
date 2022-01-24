<?php

require_once('for_php7.php');

class knjz081Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz081index.php", "", "edit");

        $db = Query::dbCheckOut();
        
        $query = knjz081Query::getList($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
             array_walk($row, "htmlspecialchars_array");
             $row["LINK_ADDR"] = $row["LINK_ADDR"] != "" ? "******************" : "";
             $row["DBNAME"] = $row["DBNAME"] != "" ? "*****" : "";
             $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz081Form1.html", $arg);
    }
}
?>
