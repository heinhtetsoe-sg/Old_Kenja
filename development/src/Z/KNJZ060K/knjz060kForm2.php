<?php

require_once('for_php7.php');

class knjz060kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz060kindex.php", "", "edit");

        

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjz060kQuery::getRow($model,1);
        }else{
            $Row =& $model->field;
            //費目小分類が存在して更新が中止になった場合は小分類有無を有りに戻す
            if($model->exist_flg != "") {
                $Row["EXPENSE_S_EXIST_FLG"] = "1";
            }
            //小分類有無が有りなら小分類金額合計を取得
            if($Row["EXPENSE_S_EXIST_FLG"] == "1") {
                $Row["EXPENSE_S_MONEY"] = knjz060kQuery::getSumSmoney($model->year,$Row["EXPENSE_M_CD"]);
            }
        }

        $db = Query::dbCheckOut();

        //費目大分類コードコンボ
        $result      = $db->query(knjz060kQuery::getName($model->year,"G201"));
        $opt["G201"] = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt["G201"][] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                   "value" => $row["NAMECD2"]);
        }
        $result->free();

        $objForm->ae( array("type"        => "select",
                            "name"        => "EXPENSE_L_CD",
                            "size"        => "1",
                            "value"       => $model->exp_lcd,
                            "options"     => $opt["G201"]));
        $arg["data"]["EXPENSE_L_CD"] = $objForm->ge("EXPENSE_L_CD");

        //費目中分類コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXPENSE_M_CD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["EXPENSE_M_CD"] ));
        $arg["data"]["EXPENSE_M_CD"] = $objForm->ge("EXPENSE_M_CD");

        //費目中分類名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXPENSE_M_NAME",
                            "size"        => 20,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["EXPENSE_M_NAME"] ));
        $arg["data"]["EXPENSE_M_NAME"] = $objForm->ge("EXPENSE_M_NAME");

        if (strlen($Row["EXPENSE_M_MONEY"]) && !strlen($model->exist_flg)) {
            $m_money = $Row["EXPENSE_M_MONEY"];
        } else {
            $m_money = "false";
        }
        //費目小分類有無が有りの場合、金額編集不可
        if($Row["EXPENSE_S_EXIST_FLG"] == "1"){
            $model->s_exist_flg = "1";
            $Row["EXPENSE_M_MONEY"] = $Row["EXPENSE_S_MONEY"];
            $disabled = "disabled";
            $s_money = (strlen($Row["EXPENSE_S_MONEY"])) ? $Row["EXPENSE_S_MONEY"]: "false";
        }else{
            $model->s_exist_flg = "2";
            $disabled = "";
            $s_money = "false";
        }

        //費用小分類有無    
        $objForm->ae( array("type"        => "radio",
                            "name"        => "EXPENSE_S_EXIST_FLG",
                            "extrahtml"   => "onclick=\"changetext(this,$s_money,$m_money)\"",
                            "value"       => $model->s_exist_flg ));
        $arg["data"]["EXPENSE_S_EXIST_FLG1"] = $objForm->ge("EXPENSE_S_EXIST_FLG","1");
        $arg["data"]["EXPENSE_S_EXIST_FLG2"] = $objForm->ge("EXPENSE_S_EXIST_FLG","2");

        //金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXPENSE_M_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "$disabled style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["EXPENSE_M_MONEY"] ));
        $arg["data"]["EXPENSE_M_MONEY"] = $objForm->ge("EXPENSE_M_MONEY");

        //納入期限
        $due_date = str_replace("-", "/", $Row["DUE_DATE"]);
        $arg["data"]["DUE_DATE"] = View::popUpCalendar($objForm, "DUE_DATE", $due_date);

        //自動振替日
        $bank_trans_sdate = str_replace("-", "/", $Row["BANK_TRANS_SDATE"]);
        $arg["data"]["BANK_TRANS_SDATE"] = View::popUpCalendar($objForm, "BANK_TRANS_SDATE", $bank_trans_sdate);

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

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz060kindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz060kForm2.html", $arg);
    }
}
?>
