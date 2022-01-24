<?php

require_once('for_php7.php');

class knjp150kForm2 
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp150kindex.php", "", "edit");
        $arg["reload"] = "";

        $db = Query::dbCheckOut();

        $err_flg = "";
        if(!$model->isWarning()) {
            $Row = knjp150kQuery::getRow($db, $model);
        }else{
            $Row =& $model->field;
            $err_flg = true;
        }
        //銀行処理結果CSVデータの内容で更新されている場合
        if ($model->paid_input_flg == "1") {
            $model->disabled1 = "disabled";
        } else {
            $model->disabled1 = "";
        }

        //分納の設定がされている場合
        if (strlen($model->inst_cd)) {
            $model->disabled1 = "disabled";

            //分納の金額を取得
            $money_due = "";
            $result = $db->query(knjp150kQuery::getMoneyDue($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($row["EXPENSE_S_EXIST_FLG"] == "1" ) {
                    $row["MONEY_DUE"] = $db->getOne(knjp150kQuery::getMoneyDue2($model, $row["EXPENSE_M_CD"]));
                }
                $money_due += $row["MONEY_DUE"];
            }

            $row = $db->getRow(knjp150kQuery::getInstMoney($model), DB_FETCHMODE_ASSOC);

            //分納の設定がされていて、分納の支払いが完了していない場合
            if ($money_due != ((int)$row["PAID_MONEY"] - (int)$row["REPAY_MONEY"])) {
                $model->disabled2 = "disabled";
                $model->disabled3 = "disabled";
            } else {
                $model->disabled3 = "";
            }
        } else {
            $model->disabled3 = "";
        }


        //編集不可になっているフォームは値が参照できないので、データを取得し直す
        if (strlen($err_flg) && 
           (strlen($model->disabled1) || strlen($model->disabled2) || strlen($model->disabled3))) {
            $row = knjp150kQuery::getRow($db, $model);

            if (strlen($model->disabled1)) {
                $Row["PAID_MONEY"]          = $row["PAID_MONEY"];
                $Row["PAID_MONEY_DATE"]     = $row["PAID_MONEY_DATE"];
                $Row["PAID_MONEY_DIV"]      = $row["PAID_MONEY_DIV"];
            } 
            if (strlen($model->disabled2)) {
                $Row["REPAY_MONEY"]         = $row["REPAY_MONEY"];
                $Row["REPAY_MONEY_DATE"]    = $row["REPAY_MONEY_DATE"];
                $Row["REPAY_MONEY_DIV"]     = $row["REPAY_MONEY_DIV"];
                $Row["REMARK"]              = $row["REMARK"];
            }
            if (strlen($model->disabled3)) {
                $Row["REPAY_FLG"]           = $row["REPAY_FLG"];
            }
        }

        $arg["TARGET_EXPENSE"] = $model->titlecd."　".$model->exp_sname;

        //入金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "PAID_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "$model->disabled1 style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["PAID_MONEY"] ));
        $arg["data"]["PAID_MONEY"] = $objForm->ge("PAID_MONEY");


        global $sess;
        //入金日
        $paid_money_date = str_replace("-","/",$Row["PAID_MONEY_DATE"]);
        $objForm->ae( array("type"        => "text",
                            "name"        => "PAID_MONEY_DATE",
                            "size"        => 12,
                            "maxlength"   => 12,
                            "extrahtml"   => "$model->disabled1 onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"",
                            "value"       => $paid_money_date ));

        //読込ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_calen",
                            "value"       => "･･･",
                            "extrahtml"   => "$model->disabled1 onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=PAID_MONEY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['PAID_MONEY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"") );

        $arg["data"]["PAID_MONEY_DATE"] = $objForm->ge("PAID_MONEY_DATE") .$objForm->ge("btn_calen");


        //入金区分
        $opt = array();
        $opt[] = array("label" => "", "value" => "");         //空リストをセット
        $result = $db->query(knjp150kQuery::getNamecd($model->year, "G205"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "PAID_MONEY_DIV",
                            "size"        => 1,
                            "value"       => $Row["PAID_MONEY_DIV"],
                            "extrahtml"   => "$model->disabled1",
                            "options"     => $opt ));
        $arg["data"]["PAID_MONEY_DIV"] = $objForm->ge("PAID_MONEY_DIV");

        //返金データ選択
        $query = knjp150kQuery::getRepaySeq($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["REPAY_SEQ"], "REPAY_SEQ", $extra, 1, "NEW");

        //返金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "REPAY_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "$model->disabled2 style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["REPAY_MONEY"] ));
        $arg["data"]["REPAY_MONEY"] = $objForm->ge("REPAY_MONEY");


        //返金日
        $repay_money_date = str_replace("-","/",$Row["REPAY_MONEY_DATE"]);
        $objForm->ae( array("type"        => "text",
                            "name"        => "REPAY_MONEY_DATE",
                            "size"        => 12,
                            "maxlength"   => 12,
                            "extrahtml"   => "$model->disabled2 onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"",
                            "value"       => $repay_money_date ));

        //読込ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_calen2",
                            "value"       => "･･･",
                            "extrahtml"   => "$model->disabled2 onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=REPAY_MONEY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['REPAY_MONEY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"") );

        $arg["data"]["REPAY_MONEY_DATE"] = $objForm->ge("REPAY_MONEY_DATE") .$objForm->ge("btn_calen2");

        //返金済
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "REPAY_FLG",
                            "value"       =>  1,
                            "extrahtml"   => ($Row["REPAY_FLG"] == "1") ? "$model->disabled3 checked" : "$model->disabled3" ));
        $arg["data"]["REPAY_FLG"] = $objForm->ge("REPAY_FLG");

        //返金区分
        $opt   = array();
        $opt[] = array("label" => "", "value" => "");         //空リストをセット
        $result = $db->query(knjp150kQuery::"getNamecd($model->year, "G209"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "REPAY_MONEY_DIV",
                            "size"        => 1,
                            "value"       => $Row["REPAY_MONEY_DIV"],
                            "extrahtml"   => "$model->disabled2",
                            "options"     => $opt ));
        $arg["data"]["REPAY_MONEY_DIV"] = $objForm->ge("REPAY_MONEY_DIV");

        //備考
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "value"       => $Row["REMARK"],
                            "extrahtml"   => "$model->disabled2" ));
        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        Query::dbCheckIn($db);


        //更新ボタンを作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //クリアボタンを作成
        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成
        $objForm->ae( array("type"        => "button",
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

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_PAID_MONEY",
                            "value"     => $Row["PAID_MONEY"]) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_PAID_MONEY_DATE",
                            "value"     => $Row["PAID_MONEY_DATE"]) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_PAID_MONEY_DIV",
                            "value"     => $Row["PAID_MONEY_DIV"]) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_REPAY_MONEY",
                            "value"     => $Row["REPAY_MONEY"]) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_REPAY_MONEY_DATE",
                            "value"     => $Row["REPAY_MONEY_DATE"]) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_REPAY_FLG",
                            "value"     => $Row["REPAY_FLG"]) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_REPAY_MONEY_DIV",
                            "value"     => $Row["REPAY_MONEY_DIV"]) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_REMARK",
                            "value"     => $Row["REMARK"]) );

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knjp150kindex.php?cmd=list';";
        }

        View::toHTML($model, "knjp150kForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "NEW") {
        $opt[] = array("label" => "新規作成", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
