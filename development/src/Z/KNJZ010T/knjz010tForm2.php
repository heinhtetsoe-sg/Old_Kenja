<?php

require_once('for_php7.php');

class knjz010tForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz010tindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjz010tQuery::getRow($model->year,$model->applicantdiv,$model->majorcd);
        } else {
            $Row =& $model->field;
        }

        //入試制度コンボ
        $db = Query::dbCheckOut();
        $result    = $db->query(knjz010tQuery::selectApplicantdiv($model->year));
        $opt_applicantdiv = array();
        $opt_applicantdiv[] = array("label" => "", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_applicantdiv[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                        "value" => $row["NAMECD2"]);
        }

        $result->free();
        Query::dbCheckIn($db);
        $objForm->ae( array("type"        => "select",
                            "name"        => "APPLICANTDIV",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->applicantdiv,
                            "options"     => $opt_applicantdiv));
        $arg["data"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //学科コード
        $db = Query::dbCheckOut();
        $result    = $db->query(knjz010tQuery::getMajorcd($model->year));
        $opt       = array();
        $opt[] = array("label" => "",
                       "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"        => "select",
                            "name"        => "MAJORCD",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->majorcd,
                            "options"     => $opt));
        $arg["data"]["MAJORCD"] = $objForm->ge("MAJORCD");

        //募集枠数
        $objForm->ae( array("type"        => "text",
                            "name"        => "CAPACITY",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["CAPACITY"] ));

        $arg["data"]["CAPACITY"] = $objForm->ge("CAPACITY");

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

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz010tindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz010tForm2.html", $arg);
    }
}
?>
