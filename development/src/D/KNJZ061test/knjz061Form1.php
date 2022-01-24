<?php

require_once('for_php7.php');

class knjz061Form1
{
    function main(&$model)
    {
         $arg["jscript"] = "";
        //権限チェック
        if ($model->auth != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz061index.php", "", "edit");

        $db     = Query::dbCheckOut();
        //データ取得
        $query = knjz061Query::getData($model);
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["ELECTDIV"]=="1"){ 
                $row["ELECTDIV"] = "選";
            } elseif ($row["ELECTDIV"]=="0"){
                $row["ELECTDIV"] = "";
            }
            //専門
            $row["SPECIALDIV"] = ($row["SPECIALDIV"] == "1") ? "専" : "";

            //更新後この行が画面の先頭に来るようにする
            if ($row["CLASSCD"] == $model->classcd) {
                $row["CLASSNAME"] = ($row["CLASSNAME"]) ? $row["CLASSNAME"] : "　";
                $row["CLASSNAME"] = "<a name=\"target\">{$row["CLASSNAME"]}</a><script>location.href='#target';</script>";
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
        View::toHTML($model, "knjz061Form1.html", $arg); 
    }
}
?>
