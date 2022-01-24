<?php

require_once('for_php7.php');

class knjp121kForm2 
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp121kindex.php", "", "edit");
        $arg["reload"] = "";

        //DB接続
        $db = Query::dbCheckOut();

        //小分類を有する中分類なら、入金・返金ともに編集不可
        if ($model->s_exist_flg == "1") {
            $disabled1 = " disabled";
            $disabled2 = " disabled";
        } else {
            $disabled1 = "";
            $disabled2 = "";
        }

        if (!$model->isWarning()) {
            $query = knjp121kQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            //編集不可になっているフォームは値が参照できないので、データを取得し直す
            if (strlen($disabled1) || strlen($disabled2)) {
                $query = knjp121kQuery::getRow($model);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

                if (strlen($disabled2)) {
                    $Row["REPAY_MONEY"]     = $row["REPAY_MONEY"];
                    $Row["REPAY_DATE"]      = $row["REPAY_DATE"];
                    $Row["REPAY_DEV"]       = $row["REPAY_DEV"];
                } else if (strlen($disabled1)) {
                    $Row["MONEY_DUE"]       = $row["MONEY_DUE"];
                    $Row["PAID_MONEY"]      = $row["PAID_MONEY"];
                    $Row["PAID_MONEY_DATE"] = $row["PAID_MONEY_DATE"];
                    $Row["PAID_MONEY_DIV"]  = $row["PAID_MONEY_DIV"];
                }
            }
        }

        $arg["TARGET_EXPENSE"] = $model->exp_lcd.$model->exp_mcd."　".$model->exp_mname;

        $extraRight = " style=\"text-align:right\"";
        //入金必要額
        $extra = " onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MONEY_DUE"] = knjCreateTextBox($objForm, $Row["MONEY_DUE"], "MONEY_DUE", 10, 8, $disabled1.$extraRight.$extra);

        //入金額
        $extra = " onblur=\"this.value=toInteger(this.value), money_check1()\"";
        $arg["data"]["PAID_MONEY"] = knjCreateTextBox($objForm, $Row["PAID_MONEY"], "PAID_MONEY", 10, 8, $disabled1.$extraRight.$extraInt.$extra);

        global $sess;
        //入金日
        $paid_money_date = str_replace("-", "/", $Row["PAID_MONEY_DATE"]);
        $arg["data"]["PAID_MONEY_DATE"] = View::popUpCalendar2($objForm, "PAID_MONEY_DATE", $paid_money_date, "", "", $disabled1);

        //入金区分
        $query = knjp121kQuery::getNamecd($model->year, "G205");
        $extra = $disabled1;
        makeCombo($objForm, $arg, $db, $query, $Row["PAID_MONEY_DIV"], "PAID_MONEY_DIV", $extra, 1, "BLANK");

        //返金額
        $extra = " onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["REPAY_MONEY"] = knjCreateTextBox($objForm, $Row["REPAY_MONEY"], "REPAY_MONEY", 10, 8, $disabled2.$extraRight.$extraInt.$extra);

        //返金日
        $repay_date = str_replace("-", "/", $Row["REPAY_DATE"]);
        $extra = " onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $argRepay = knjCreateTextBox($objForm, $repay_date, "REPAY_DATE", 12, 12, $disabled2.$extra);

        //読込ボタンを作成する
        $extra = " onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=REPAY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['REPAY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $argRepayBtn = knjCreateBtn($objForm, "btn_calen2", "･･･", $disabled2.$extra);
        $arg["data"]["REPAY_DATE"] = $argRepay .$argRepayBtn;

        //返金区分
        $repayNmst = $model->s_exist_flg == "1" ? "G209" : "G212";
        $query = knjp121kQuery::getNamecd($model->year, $repayNmst);
        $extra = $disabled2;
        makeCombo($objForm, $arg, $db, $query, $Row["REPAY_DEV"], "REPAY_DEV", $extra, 1, "BLANK");

        //備考
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 40, 40, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knjp121kindex.php?cmd=list';";
        }

        View::toHTML($model, "knjp121kForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタンを作成
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);
    //一括更新ボタンを作成
    $extra = "onclick=\"return btn_submit('all_edit');\"";
    $arg["button"]["btn_allupdate"] = knjCreateBtn($objForm, "btn_allupdate", "一括更新", $extra);
}

function makeHidden(&$objForm, &$arg, $model) {

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "TMP_PAID_MONEY");
    knjCreateHidden($objForm, "TMP_REPAY_MONEY");
}

?>
