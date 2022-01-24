<?php

require_once('for_php7.php');

class knjz062_2aForm1
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
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz062_2aindex.php", "", "edit");

        $db     = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjz062_2aQuery::getData($model);
        //$query  = "select * from class_mst order by CLASSCD";
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
            //専門･その他
            if ($row["SPECIALDIV"] == "1") {
                $row["SPECIALDIV"] = "専";
            } else if ($row["SPECIALDIV"] == "2") {
                $row["SPECIALDIV"] = "他";
            } else {
                $row["SPECIALDIV"] = "";
            }

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
        View::toHTML($model, "knjz062_2aForm1.html", $arg); 
    }
}
?>
