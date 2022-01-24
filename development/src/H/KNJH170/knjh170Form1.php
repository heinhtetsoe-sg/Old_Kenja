<?php

require_once('for_php7.php');

class knjh170Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh170index.php", "", "edit");

        $db     = Query::dbCheckOut();
        $query  = "SELECT * FROM GO_HOME_GROUP_MST ORDER BY GO_HOME_GROUP_NO";
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            //更新後この行が画面の先頭に来るようにする
            if ($row["GO_HOME_GROUP_NO"] == $model->go_home_group_no) {
                $row["GO_HOME_GROUP_NAME"] = ($row["GO_HOME_GROUP_NAME"]) ? $row["GO_HOME_GROUP_NAME"] : "　";
                $row["GO_HOME_GROUP_NAME"] = "<a name=\"target\">{$row["GO_HOME_GROUP_NAME"]}</a><script>location.href='#target';</script>";
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
        View::toHTML($model, "knjh170Form1.html", $arg); 
    }
}
?>
