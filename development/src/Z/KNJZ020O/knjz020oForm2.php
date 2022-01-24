<?php

require_once('for_php7.php');

class knjz020oForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz020oindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //マスタチェック
        $row = $db->getRow(knjz020oQuery::entexam_course(CTRL_YEAR+1));
        if (!is_array($row)) {
            $arg["Closing"] = " closing_window('受験コースマスタ'); " ;
        }
        $row = $db->getRow(knjz020oQuery::getTestdivMst(CTRL_YEAR+1));
        if (!is_array($row)) {
            $arg["Closing"] = " closing_window('入試区分マスタ'); " ;
        }
        for ($i=0; $i<1; $i++) {
            $mname = "L009";
            $row = $db->getRow(knjz020oQuery::get_name_mst(CTRL_YEAR+1, $mname));
            if (!is_array($row)) {
                $arg["Closing"] = " closing_window('名称マスタ'); " ;
            }
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjz020oQuery::getRow($db,$model);
        }else{
            $Row =& $model->field;
        }

        //名称マスタからデータを取得してコンボ用の配列にセット
        $result = $db->query(knjz020oQuery::getName($model->year, array("L009")));
        $opt = array();
        $opt["L009"] = array();     //試験科目コンボ

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                                            "value"  =>  $row["NAMECD2"]);
        }
        $result->free();

        //入試区分
        $query = knjz020oQuery::getTestdivMst($model->year);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        Query::dbCheckIn($db);

        //試験科目
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTSUBCLASSCD",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->testsubclasscd,
                            "options"     => $opt["L009"]));
        $arg["data"]["TESTSUBCLASSCD"] = $objForm->ge("TESTSUBCLASSCD");

        //満点
        $objForm->ae( array("type"        => "text",
                            "name"        => "PERFECT",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["PERFECT"] ));
        $arg["data"]["PERFECT"] = $objForm->ge("PERFECT");


        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COURSECD",
                            "value"     => $Row["COURSECD"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "MAJORCD",
                            "value"     => $Row["MAJORCD"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "EXAMCOURSECD",
                            "value"     => $Row["EXAMCOURSECD"]
                            ) );        

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz020oindex.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz020oForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
