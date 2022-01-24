<?php

require_once('for_php7.php');

class knjz140Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz140index.php", "", "edit");

        $db = Query::dbCheckOut();
        $result = $db->query(knjz140query::getList($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["MSG_CD"] == $model->messagecd) {
                $row["MSG_LEVEL"] = ($row["MSG_LEVEL"]) ? $row["MSG_LEVEL"] : "　";
                $row["MSG_LEVEL"] = "<a name=\"target\">{$row["MSG_LEVEL"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz140Form1.html", $arg);
    }
}
?>
