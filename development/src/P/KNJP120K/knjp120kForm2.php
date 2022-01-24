<?php

require_once('for_php7.php');

class knjp120kForm2 
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp120kindex.php", "", "edit");
        $arg["reload"] = "";


        //小分類を有する中分類なら、入金・返金ともに編集不可
        if ($model->s_exist_flg == "1") {
            $disabled1 = "disabled";
            $disabled2 = "disabled";
        } elseif (strlen($model->inst_cd) || strlen($model->paid_flg)) {
            $disabled1 = "disabled";
            $disabled2 = "";
        } else {
            $disabled1 = "";
            $disabled2 = "";
        }

        //振替停止が編集可能か判断
        if (strlen($model->bank_date)) {
            $disabled3 = "disabled";
        } else {
            $disabled3 = "";
        }

        if(!$model->isWarning()) {
            $Row = knjp120kQuery::getRow($model);
        }else{
            $Row =& $model->field;
            //編集不可になっているフォームは値が参照できないので、データを取得し直す
            if (strlen($disabled1) || strlen($disabled2) || strlen($disabled3)) {
                $row = knjp120kQuery::getRow($model);

                if (strlen($disabled2)) {
                    $Row["PAID_MONEY"]      = $row["PAID_MONEY"];
                    $Row["PAID_MONEY_DATE"] = $row["PAID_MONEY_DATE"];
                    $Row["PAID_MONEY_DIV"]  = $row["PAID_MONEY_DIV"];
                    $Row["REPAY_MONEY"]     = $row["REPAY_MONEY"];
                    $Row["REPAY_DATE"]      = $row["REPAY_DATE"];
                    $Row["REPAY_DEV"]       = $row["REPAY_DEV"];
                } elseif (strlen($disabled1)) {
                    $Row["PAID_MONEY"]      = $row["PAID_MONEY"];
                    $Row["PAID_MONEY_DATE"] = $row["PAID_MONEY_DATE"];
                    $Row["PAID_MONEY_DIV"]  = $row["PAID_MONEY_DIV"];
                }
                if (strlen($disabled3)) {
                    $Row["BANK_TRANS_STOP_RESON"] = $row["BANK_TRANS_STOP_RESON"];
                }
            }
        }

        $db = Query::dbCheckOut();

        $arg["TARGET_EXPENSE"] = $model->exp_lcd.$model->exp_mcd."　".$model->exp_mname;


        //入金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "PAID_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "$disabled1 style=\"text-align:right\" onblur=\"this.value=toInteger(this.value), money_check1()\"",
                            "value"       => $Row["PAID_MONEY"] ));
        $arg["data"]["PAID_MONEY"] = $objForm->ge("PAID_MONEY");


        global $sess;
        //入金日
        $paid_money_date = str_replace("-","/",$Row["PAID_MONEY_DATE"]);
        $objForm->ae( array("type"        => "text",
                            "name"        => "PAID_MONEY_DATE",
                            "size"        => 12,
                            "maxlength"   => 12,
                            "extrahtml"   => "$disabled1 onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"",
                            "value"       => $paid_money_date ));

        //読込ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_calen",
                            "value"       => "･･･",
                            "extrahtml"   => "$disabled1 onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=PAID_MONEY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['PAID_MONEY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"") );

        $arg["data"]["PAID_MONEY_DATE"] = $objForm->ge("PAID_MONEY_DATE") .$objForm->ge("btn_calen");


        //入金区分
        $opt = array();
        $opt[] = array("label" => "", "value" => "");         //空リストをセット
        $result = $db->query(knjp120kQuery::getNamecd($model->year,"G205"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "PAID_MONEY_DIV",
                            "size"        => 1,
                            "value"       => $Row["PAID_MONEY_DIV"],
                            "extrahtml"   => "$disabled1",
                            "options"     => $opt ));
        $arg["data"]["PAID_MONEY_DIV"] = $objForm->ge("PAID_MONEY_DIV");


        //返金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "REPAY_MONEY",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"   => "$disabled2 style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["REPAY_MONEY"] ));
        $arg["data"]["REPAY_MONEY"] = $objForm->ge("REPAY_MONEY");


        //返金日
        $repay_date = str_replace("-","/",$Row["REPAY_DATE"]);
        $objForm->ae( array("type"        => "text",
                            "name"        => "REPAY_DATE",
                            "size"        => 12,
                            "maxlength"   => 12,
                            "extrahtml"   => "$disabled2 onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"",
                            "value"       => $repay_date ));

        //読込ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_calen2",
                            "value"       => "･･･",
                            "extrahtml"   => "$disabled2 onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=REPAY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['REPAY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"") );

        $arg["data"]["REPAY_DATE"] = $objForm->ge("REPAY_DATE") .$objForm->ge("btn_calen2");


        //返金区分
        $opt   = array();
        $opt[] = array("label" => "", "value" => "");
        $namecd2 = $model->s_exist_flg == "1" ? "G209" : "G212";
        $result = $db->query(knjp120kQuery::getNamecd($model->year, $namecd2));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "REPAY_DEV",
                            "size"        => 1,
                            "value"       => $Row["REPAY_DEV"],
#                            "extrahtml"   => "$disabled2", # 2005/12/13
                            "extrahtml"   => "$disabled2",	//NO007
                            "options"     => $opt ));
        $arg["data"]["REPAY_DEV"] = $objForm->ge("REPAY_DEV");


        //振替停止区分
        $opt   = array();
        $opt[] = array("label" => "", "value" => "");         //空リストをセット
        $result = $db->query(knjp120kQuery::getNamecd($model->year,"G208"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "BANK_TRANS_STOP_RESON",
                            "size"        => 1,
                            "value"       => $Row["BANK_TRANS_STOP_RESON"],
                            "extrahtml"   => "$disabled3",
                            "options"     => $opt ));
        $arg["data"]["BANK_TRANS_STOP_RESON"] = $objForm->ge("BANK_TRANS_STOP_RESON");

        $result->free();


        //異動情報出力 NO001
        $result = $db->query(knjp120kQuery::getList2($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arg["data"]["TRANSFERNAME"]   = $row["TRANSFERNAME"];
            $arg["data"]["TRANSFER_SDATE"] = str_replace("-","/",$row["TRANSFER_SDATE"]);
            $arg["data"]["KARA"]           = "～";
            $arg["data"]["TRANSFER_EDATE"] = str_replace("-","/",$row["TRANSFER_EDATE"]);
        }
        $result->free();

        //異動情報出力 NO001
        $result = $db->query(knjp120kQuery::getList3($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arg["data"]["TRANSFERNAME"]   = $row["GRDNAME"];
            $arg["data"]["TRANSFER_SDATE"] = str_replace("-","/",$row["GRD_DATE"]);
            $arg["data"]["KARA"]           = "";
            $arg["data"]["TRANSFER_EDATE"] = "";
        }
        $result->free();


        Query::dbCheckIn($db);


        //更新ボタンを作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

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

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "MONEY_DUE",
                            "value"     => $model->money_due) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_PAID_MONEY") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TMP_REPAY_MONEY") );


        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knjp120kindex.php?cmd=list';";
        }

        View::toHTML($model, "knjp120kForm2.html", $arg);
    }
}
?>
