<?php

require_once('for_php7.php');

class knjtx005Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjtx005index.php", "", "edit");

        $db = Query::dbCheckOut();
        $query = "select * from KOJIN_FURIKOMI_BANK_DAT ";
        $query .= "order by KOJIN_NO, S_DATE";
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
             array_walk($row, "htmlspecialchars_array");
             $row["S_DATE"]     = str_replace("-","/",$row["S_DATE"]);
             $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjtx005Form1.html", $arg);
    }
}
?>
