<?php

require_once('for_php7.php');

class knjmp984Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjmp984index.php", "", "edit");

        $db = Query::dbCheckOut();

        //マスタ一覧取得
        $result = $db->query(knjmp984Query::selectQuery());
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
             array_walk($row, "htmlspecialchars_array");
             $row["link"] = View::alink(REQUESTROOT."/M/KNJMP984_2/knjmp984_2index.php", "設定" , "target=\"_parent\" ",
                                        array("SEND_LEVY_GROUP_CD" => $row["LEVY_GROUP_CD"],
                                              "SEND_LEVY_GROUP_NAME" => $row["LEVY_GROUP_NAME"],
                                              "SEND_YEAR" => CTRL_YEAR));
             $arg["data"][] = $row; 
        }

        $result->free();
        Query::dbCheckIn($db);
        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp984Form1.html", $arg);
    }
} 
?>
