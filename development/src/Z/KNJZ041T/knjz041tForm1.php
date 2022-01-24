<?php

require_once('for_php7.php');

class knjz041tForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz041tindex.php", "", "edit");

        $db = Query::dbCheckOut();

        $result = $db->query(knjz041tQuery::selectQuery());
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
             array_walk($row, "htmlspecialchars_array");

             $arg["data"][] = $row;
        }
        $result->free();

        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd"
                            ));

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjz041tindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz041tForm1.html", $arg);
    }
}
?>
