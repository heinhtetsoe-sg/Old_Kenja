<?php

require_once('for_php7.php');

class knjp070kForm3
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp070kindex.php", "", "edit");
        $arg["reload"] = "";

        if(!$model->isWarning()){
            $Row = knjp070kQuery::getRow1($model->expense_m_cd, $model->schregno);
        }else{
            $Row =& $model->field;
        }
        $db = Query::dbCheckOut();

        //中分類
        $opt = array();
        $result = $db->query(knjp070kQuery::GetMcd());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"]."：".$row["EXPENSE_M_NAME"],
                           "value" => $row["EXPENSE_M_CD"]);

            //小分類存在フラグをセットし、金額テキストの有無効を変更
            $arg["data2"][] = array("idx" => $row["EXPENSE_M_CD"], "flg" => $row["EXPENSE_S_EXIST_FLG"]); 

            if ($model->schregno != "") {
                //中分類コードごとのデータを設定
                $row2 = $db->getRow(knjp070kQuery::ExistMcd($row["EXPENSE_M_CD"], $model->schregno),DB_FETCHMODE_ASSOC);
                if ($row2["EXPENSE_M_CD"] != "") {
                    $arg["data3"][] = array("idx" => $row2["EXPENSE_M_CD"], "money" => $row2["MONEY_DUE"], "reason" => $row2["REDUCTION_REASON"]); 
                } else {
                    $arg["data3"][] = array("idx" => $row["EXPENSE_M_CD"],  "money" => $row["EXPENSE_M_MONEY"], "reason" => ''); 
                }
            }
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "EXPENSE_M_CD",
                            "size"        => 1,
                            "extrahtml"   => "onChange=\"EnableMoney(), SetMoney();\"",
                            "value"       => $Row["EXPENSE_M_CD"],
                            "options"     => $opt ));
        $arg["data"]["EXPENSE_M_CD"] = $objForm->ge("EXPENSE_M_CD");

        //1年生自動入金なしチェックボックス
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "UN_AUTO_PAYFLG",
                            "value"       =>  1,
                            "extrahtml"   => ($Row["UN_AUTO_PAYFLG"] == 1) ? "checked" : "" ));

        $arg["data"]["UN_AUTO_PAYFLG"] = $objForm->ge("UN_AUTO_PAYFLG");

        //納入必要金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "MONEY_DUE",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => " style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["MONEY_DUE"] ));

        $arg["data"]["MONEY_DUE"] = $objForm->ge("MONEY_DUE");

        //減免事由
        $opt = array();
        $opt[0] = array("label" => "", "value" => "");
        $result = $db->query(knjp070kQuery::nameGet());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
               $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                              "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "REDUCTION_REASON",
                            "size"        => 1,
                            "value"       => $Row["REDUCTION_REASON"],
                            "options"     => $opt ));
        $arg["data"]["REDUCTION_REASON"] = $objForm->ge("REDUCTION_REASON");

        Query::dbCheckIn($db);


        if ($model->div == "M") {
            $arg["data"]["checked1"] = "checked";
        } else {
            $arg["data"]["checked2"] = "checked";
        }

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成
        $objForm->ae( array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit(document.forms[0].cmd.value);\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_all_edit",
                            "value"       => "一括更新",
                            "extrahtml"   => "onclick=\"return btn_submit('all_edit');\"" ) );

        $arg["button"]["btn_all_edit"] = $objForm->ge("btn_all_edit");

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd) );

        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit1"){
            $arg["reload"]  = "parent.top_frame.location.href='knjp070kindex.php?cmd=list1';parent.mid_frame.location.href='knjp070kindex.php?cmd=list2'; ";
        }

        View::toHTML($model, "knjp070kForm3.html", $arg);
    }
}
?>
