<?php

require_once('for_php7.php');

class knjz080kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz080kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(isset($model->warning) || $model->cmd == 'knjz080k'){
            $Row =& $model->field;
        }else{
            $Row = knjz080kQuery::getRow($model,1);
        }

        $db = Query::dbCheckOut();

        //申込コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "APPLICATIONCD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["APPLICATIONCD"] ));
        $arg["data"]["APPLICATIONCD"] = $objForm->ge("APPLICATIONCD");

        //申込名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "APPLICATIONNAME",
                            "size"        => 20,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["APPLICATIONNAME"] ));
        $arg["data"]["APPLICATIONNAME"] = $objForm->ge("APPLICATIONNAME");

        //金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "APPLICATIONMONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["APPLICATIONMONEY"] ));
        $arg["data"]["APPLICATIONMONEY"] = $objForm->ge("APPLICATIONMONEY");


		//mk 2005.06.17 以下追加（銀行情報）

        //銀行コード
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        $result = $db->query(knjz080kQuery::getBankcd());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["BANKCD"]."：".$row["BANKNAME"],
                                           "value" => $row["BANKCD"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "BANKCD",
                            "size"        => 1,
                            "value"       => $Row["BANKCD"],
                            "extrahtml"   => "onchange=\"btn_submit('knjz080k');\"",
                            "options"     => $opt ));

        $arg["data"]["BANKCD"] = $objForm->ge("BANKCD");

        //支店コード
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        $result = $db->query(knjz080kQuery::getBranchcd($Row));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["BRANCHCD"]."：".$row["BRANCHNAME"],
                                           "value" => $row["BANKCD"]."-".$row["BRANCHCD"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "BANK_BRANCHCD",
                            "size"        => 1,
                            "value"       => $Row["BANK_BRANCHCD"] ,
                            //"extrahtml"   => "OnChange=\"SetBranch();\"",
                            "options"     => $opt ));

        $arg["data"]["BANK_BRANCHCD"] = $objForm->ge("BANK_BRANCHCD");

        //預金種目
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        $result = $db->query(knjz080kQuery::nameGet("G203"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
               $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                              "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "DEPOSIT_ITEM",
                            "size"        => 1,
                            "value"       => $Row["DEPOSIT_ITEM"] ,
                            "options"     => $opt ));

        $arg["data"]["DEPOSIT_ITEM"] = $objForm->ge("DEPOSIT_ITEM");

        //口座番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "ACCOUNTNO",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => "style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["ACCOUNTNO"] ));

        $arg["data"]["ACCOUNTNO"] = $objForm->ge("ACCOUNTNO");

        //口座名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "ACCOUNTNAME",
                            "size"        => 32,
                            "maxlength"   => 48,
                            "value"       => $Row["ACCOUNTNAME"] ));
                            //"value"       => ($model->name ? mb_convert_kana($model->name, "KVC") : $Row["ACCOUNTNAME"])));

        $arg["data"]["ACCOUNTNAME"] = $objForm->ge("ACCOUNTNAME");

		//ここまで

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

        //終了
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year ) );

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz080kindex.php?cmd=list','left_frame');";
        }
        
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz080kForm2.html", $arg);
    }
}
?>
