<?php
class knjz070kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz070kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjz070kQuery::getRow($model,1);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //費目小分類コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXPENSE_S_CD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["EXPENSE_S_CD"] ));
        $arg["data"]["EXPENSE_S_CD"] = $objForm->ge("EXPENSE_S_CD");

        //費目小分類名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXPENSE_S_NAME",
                            "size"        => 30,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["EXPENSE_S_NAME"] ));
        $arg["data"]["EXPENSE_S_NAME"] = $objForm->ge("EXPENSE_S_NAME");

        //金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXPENSE_S_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["EXPENSE_S_MONEY"] ));
        $arg["data"]["EXPENSE_S_MONEY"] = $objForm->ge("EXPENSE_S_MONEY");

        //性別
        $opt["Z002"]   = array();
        $opt["Z002"][] = array("label" => "", "value" => "");     //空リスト設定

        $result      = $db->query(knjz070kQuery::getName($model->year,Z002));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt["Z002"][] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                   "value" => $row["NAMECD2"]);
        }
        $result->free();

        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "value"       => $Row["SEX"],
                            "options"     => $opt["Z002"]));
        $arg["data"]["SEX"] = $objForm->ge("SEX");

        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリア
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        if ($model->calledId == "KNJZ071K") {
            //中小分類マスタへのリンクを作成する
            $link = REQUESTROOT."/Z/KNJZ071K/knjz071kindex.php";
            //戻る
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_back",
                                "value"       => "戻 る",
                                "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
            $arg["button"]["btn_back"] = $objForm->ge("btn_back");
        } else {
            //終了
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_back",
                                "value"       => "終 了",
                                "extrahtml"   => "onclick=\"closeWin();\"" ) );
            $arg["button"]["btn_back"] = $objForm->ge("btn_back");
        }
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" || VARS::get("cmd") != "change2"){
            $arg["reload"]  = "window.open('knjz070kindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz070kForm2.html", $arg);
    }
}
?>
