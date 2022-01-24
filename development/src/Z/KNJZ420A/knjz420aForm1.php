<?php

require_once('for_php7.php');

class knjz420aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz420aindex.php", "", "edit");

        $db = Query::dbCheckOut();
        $result = $db->query(knjz420aQuery::getList());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae(array("type"    => "hidden",
                            "name"    => "cmd"
                            ));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz420aForm1.html", $arg);
    }
}
