<?php

require_once('for_php7.php');

class knjz091_2Form1
{
    function main(&$model)
    {
    //権限チェック
    if (AUTHORITY != DEF_UPDATABLE){
        $arg["jscript"] = "OnAuthError();";
    }

    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjz091_2index.php", "", "edit");

    $db     = Query::dbCheckOut();
    $result = $db->query(knjz091_2Query::selectQuery($model));   
    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        array_walk($row, "htmlspecialchars_array");//レコードを連想配列のまま配列$arg[data]に追加していく。 
        if ($row["PRISCHOOLCD"] == $model->finschoolcd) {
            $row["PRISCHOOL_NAME"] = ($row["PRISCHOOL_NAME"]) ? $row["PRISCHOOL_NAME"] : "　";
            $row["PRISCHOOL_NAME"] = "<a name=\"target\">{$row["PRISCHOOL_NAME"]}</a><script>location.href='#target';</script>";
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
    View::toHTML($model, "knjz091_2Form1.html", $arg);
    }
} 
?>
