<?php

require_once('for_php7.php');

class knjp100kForm2 
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp100kindex.php", "", "edit");
        $arg["reload"] = "";

        $db = Query::dbCheckOut();

        //申込コード
        $opt = array();
        $result = $db->query(knjp100kQuery::getApplicd($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["APPLICATIONCD"]."：".htmlspecialchars($row["APPLICATIONNAME"]),
                           "value" => $row["APPLICATIONCD"]);
        }

        //起動時に設定
        if (!strlen($model->appli_cd)) {
            $model->appli_cd = $opt[0]["value"];
        }

        $result->free();

        if(!$model->isWarning()){
            $Row = knjp100kQuery::getRow($db, $model, 1);

            if (!strlen($Row["SCHREGNO"])) {
                $Row["APPLI_MONEY_DUE"] = $Row["APPLICATIONMONEY"];
            }
        }else{
            $Row =& $model->field;
        }

        $arg["TARGET_APPLI"] = $model->appli_cd."　".$model->appli_name;


        $objForm->ae( array("type"        => "select",
                            "name"        => "APPLICATIONCD",
                            "size"        => 1,
                            "value"       => $Row["APPLICATIONCD"],
                            "extrahtml"   => "OnChange=\"return btn_submit('edit');\"",
                            "options"     => $opt ));
        $arg["data"]["APPLICATIONCD"] = $objForm->ge("APPLICATIONCD");

        //納入必要金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "APPLI_MONEY_DUE",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["APPLI_MONEY_DUE"] ));
        $arg["data"]["APPLI_MONEY_DUE"] = $objForm->ge("APPLI_MONEY_DUE");

        //申込日
        $applied_date = str_replace("-", "/", $Row["APPLIED_DATE"]);
        $arg["data"]["APPLIED_DATE"] = View::popUpCalendar($objForm, "APPLIED_DATE", $applied_date);

        //入金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "APPLI_PAID_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["APPLI_PAID_MONEY"] ));
        $arg["data"]["APPLI_PAID_MONEY"] = $objForm->ge("APPLI_PAID_MONEY");

        //入金区分
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $result = $db->query(knjp100kQuery::nameGet());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
               $opt[] = array("label" => $row["LABEL"],
                              "value" => $row["VALUE"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "APPLI_PAID_DIV",
                            "size"        => 1,
                            "value"       => $Row["APPLI_PAID_DIV"],
                            "options"     => $opt ));
        $arg["data"]["APPLI_PAID_DIV"] = $objForm->ge("APPLI_PAID_DIV");

        Query::dbCheckIn($db);

        //納期日
        $appli_paid_date = str_replace("-", "/", $Row["APPLI_PAID_DATE"]);
        $arg["data"]["APPLI_PAID_DATE"] = View::popUpCalendar($objForm, "APPLI_PAID_DATE", $appli_paid_date);

        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタンを作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成
        $objForm->ae( array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //一括更新ボタンを作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_allupdate",
                            "value"       => "一括更新",
                            "extrahtml"   => "onclick=\"return btn_submit('all_edit');\"" ) );

        $arg["button"]["btn_allupdate"] = $objForm->ge("btn_allupdate");


        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno) );

        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knjp100kindex.php?cmd=list';";
        }

        View::toHTML($model, "knjp100kForm2.html", $arg);
    }
}
?>
