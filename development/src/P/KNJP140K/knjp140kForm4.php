<?php

require_once('for_php7.php');

class knjp140kForm4
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp140kindex.php", "", "edit");
        $arg["reload"] = "";

        if(!$model->isWarning() && strlen($model->inst_seq)){
            $Row = knjp140kQuery::getRow($model);
        }else{
            $Row =& $model->field;
        }
        $db = Query::dbCheckOut();

        $arg["INST_CD"] = $model->inst_cd."-".$model->inst_seq;

        //分納期限
        $arg["data"]["INST_DUE_DATE"] = View::popUpCalendar($objForm, "INST_DUE_DATE", str_replace("-","/",$Row["INST_DUE_DATE"]));

        //納入必要金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "INST_MONEY_DUE",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => " style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value), money_check(this)\"",
                            "value"       => $Row["INST_MONEY_DUE"] ));

        $arg["data"]["INST_MONEY_DUE"] = $objForm->ge("INST_MONEY_DUE");

        //入金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "PAID_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => " style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["PAID_MONEY"] ));

        $arg["data"]["PAID_MONEY"] = $objForm->ge("PAID_MONEY");

        //入金区分
        $opt = array();
        $opt[0] = array("label" => "", "value" => "");
        $result = $db->query(knjp140kQuery::nameGet());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
               $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                              "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "PAID_MONEY_DIV",
                            "size"        => 1,
                            "value"       => $Row["PAID_MONEY_DIV"],
                            "options"     => $opt ));
        $arg["data"]["PAID_MONEY_DIV"] = $objForm->ge("PAID_MONEY_DIV");

        //入金日
        $arg["data"]["PAID_MONEY_DATE"] = View::popUpCalendar($objForm, "PAID_MONEY_DATE", str_replace("-","/",$Row["PAID_MONEY_DATE"]));

        //入金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "REPAY_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => " style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["REPAY_MONEY"] ));

        $arg["data"]["REPAY_MONEY"] = $objForm->ge("REPAY_MONEY");

        //入金日
        $arg["data"]["REPAY_DATE"] = View::popUpCalendar($objForm, "REPAY_DATE", str_replace("-","/",$Row["REPAY_DATE"]));

        //備考
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 50,
                            "maxlength"   => 150,
                            "extrahtml"   => "",
                            "value"       => $Row["REMARK"] ));

        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        Query::dbCheckIn($db);

        if ($model->div == "M") {
            $arg["data"]["checked1"] = "checked";
        } else {
            $arg["data"]["checked2"] = "checked";
        }

		//NO003
		$model->money[$model->inst_cd]["MONEY_DUE"] = $model->money2;

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        $objForm->ae( array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit(document.forms[0].cmd.value);\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit2" && !$model->isWarning()){
            $arg["reload"]  = "parent.top_frame.location.href='knjp140kindex.php?cmd=list1';parent.mid_frame.location.href='knjp140kindex.php?cmd=list2'; ";
        }

        View::toHTML($model, "knjp140kForm4.html", $arg);
    }
}
?>
