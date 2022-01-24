<?php

require_once('for_php7.php');


class knjz055_2Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz055_2Form1", "POST", "knjz055_2index.php", "", "knjz055_2Form1");

        $db     = Query::dbCheckOut();
        $query  = "select * from coursecode_mst order by COURSECODE";
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg[data]に追加していく。 
             array_walk($row, "htmlspecialchars_array");
             $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz055_2Form1.html", $arg); 
    }
}
?>
