<?php

require_once('for_php7.php');

class knjz251Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz251index.php", "", "edit");

        $arg["top"]["YEAR"] = CTRL_YEAR;

        //「前年度コピー」ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_copy",
                            "value"       => "前年度からコピー",
                            "extrahtml"   => "onclick=\"return btn_submit('copy');\"" ) );

        $arg["button"]["btn_copy"] = $objForm->ge("btn_copy");

        $db = Query::dbCheckOut();
        $query = knjz251Query::getDataAll(CTRL_YEAR);
        $result = $db->query($query);
        $i=0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][$i]["CERTIF_KINDCD"] = $row["CERTIF_KINDCD"];
            $arg["data"][$i]["KINDNAME"]      = $row["KINDNAME"];
            $arg["data"][$i]["COLOR"]         = $row["KINDCOLOR"];
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz251Form1.html", $arg);
    }
}
?>
