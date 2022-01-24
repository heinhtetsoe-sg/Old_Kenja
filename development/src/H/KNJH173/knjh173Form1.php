<?php

require_once('for_php7.php');

class knjh173Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh173index.php", "", "edit");

        $db     = Query::dbCheckOut();
        $query  = "SELECT * FROM HOUSE_GROUP_MST ORDER BY HOUSE_GROUP_CD";
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            //更新後この行が画面の先頭に来るようにする
            if ($row["HOUSE_GROUP_CD"] == $model->HOUSE_GROUP_CD) {
                $row["HOUSE_GROUP_NAME"] = ($row["HOUSE_GROUP_NAME"]) ? $row["HOUSE_GROUP_NAME"] : "　";
                $row["HOUSE_GROUP_NAME"] = "<a name=\"target\">{$row["HOUSE_GROUP_NAME"]}</a><script>location.href='#target';</script>";
            }

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
        View::toHTML($model, "knjh173Form1.html", $arg); 
    }
}
?>
