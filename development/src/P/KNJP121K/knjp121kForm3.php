<?php

require_once('for_php7.php');

class knjp121kForm3 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjp121kindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //更新対象費目
        $opt = array();
        $result = $db->query(knjp121kQuery::getMcd($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["TOTALCD"]."：".htmlspecialchars($row["EXPENSE_M_NAME"]),
                           "value" => $row["TOTALCD"]);

            $arg["data2"][] = array("idx"   => $row["TOTALCD"],
                                    "money" => $row["EXPENSE_M_MONEY"],
                                    "sFlg"  => $row["EXPENSE_S_EXIST_FLG"],
                                    "mday"  => str_replace("-", "/", $row["BANK_TRANS_SDATE"])); 
        }
        $extra = "onChange=\"SetVal(1);\"";
        $arg["data"]["TOTALCD"] = knjCreateCombo($objForm, "TOTALCD", $model->field["TOTALCD"], $opt, $extra, 1);

        //費目小分類有無フラグ取得
        $model->s_exist_flg_ikkatsu = $db->getOne(knjp121kQuery::getMcd($model->year, $model->field["TOTALCD"]));

        //小分類を有する中分類なら、入金・返金ともに編集不可
        if ($model->s_exist_flg_ikkatsu == "1") {
            $disabled1 = " disabled";
            $disabled2 = " disabled";
        } else {
            $disabled1 = "";
            $disabled2 = "";
        }

        //年組コンボ
        $query = knjp121kQuery::GetHrclass();
        $extra = " OnChange=\"btn_submit('change_class');\"";
        makeCombo($objForm, $arg, $db, $query, $model->hrclass, "HRCLASS", $extra, 1);

        //性別コンボ
        $query = knjp121kQuery::getNamecd($model->year, "Z002", true);
        $extra = " OnChange=\"btn_submit('change_sex');\"";
        makeCombo($objForm, $arg, $db, $query, $model->sex, "SEX", $extra, 1);

        //生徒一覧
        $opt_left = array();
        $opt_right = array();
        $selectdata = explode(",", $model->selectdata);

        $result = $db->query(knjp121kQuery::GetStudent($model->hrclass,$model->sex));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."  ".$row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"], 
                                                         "value" => $row["SCHREGNO"]);

            if (!in_array($row["SCHREGNO"], $selectdata)) {
                $opt_right[]  = array("label" => $row["HR_NAME"]."  ".$row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"], 
                                      "value" => $row["SCHREGNO"]);
            }
        }

        //左リストで選択されたものを再セット
        foreach ($model->select_opt as $key => $val) {
            if (in_array($key, $selectdata)) {
                $opt_left[] = $val;
            }
        }

        //更新対象データ
        if ($model->div == "2" || !strlen($model->div)) {
            $arg["data"]["checked2"] = "checked";
        } else {        
            $arg["data"]["checked1"] = "checked";
        }

        //対象チェックボックス
        for ($i = 1; $i < 4; $i++) {
            $chk = ($model->field["CHECKED".$i] == $i) ? "checked" : "";
            $extra = "$chk tabindex=\"-1\"";
            if ($i == 1) {
                $extra .= $disabled1;
            } else if ($i == 2) {
                $extra .= $disabled2;
            }
            $arg["data"]["CHECKED".$i] = knjCreateCheckBox($objForm, "CHECKED".$i, $i, $extra, "");
        }

        //入金必要額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MONEY_DUE"] = knjCreateTextBox($objForm, $model->field["MONEY_DUE"], "MONEY_DUE", 10, 8, $extra.$disabled1);

        //入金額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value), money_check1()\"";
        $arg["data"]["PAID_MONEY"] = knjCreateTextBox($objForm, $model->field["PAID_MONEY"], "PAID_MONEY", 10, 8, $extra.$disabled1);

        global $sess;
        //入金日
        $paid_money_date = str_replace("-","/",$model->field["PAID_MONEY_DATE"]);
        $extra = "onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $paidDate = knjCreateTextBox($objForm, $paid_money_date, "PAID_MONEY_DATE", 12, 12, $extra.$disabled1);

        //読込ボタンを作成する
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=PAID_MONEY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['PAID_MONEY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $baidBtn = knjCreateBtn($objForm, "btn_calen", "･･･", $extra.$disabled1);
        $arg["data"]["PAID_MONEY_DATE"] = $paidDate.$baidBtn;

        //入金区分
        $query = knjp121kQuery::getNamecd($model->year, "G205");
        $dummyVal = $model->field["PAID_MONEY_DIV"];
        makeCombo($objForm, $arg, $db, $query, $dummyVal, "PAID_MONEY_DIV", $disabled1, 1, "BLANK");

        //返金額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["REPAY_MONEY"] = knjCreateTextBox($objForm, $model->field["REPAY_MONEY"], "REPAY_MONEY", 10, 8, $extra.$disabled2);

        //返金日
        $repay_money_date = str_replace("-","/",$model->field["REPAY_DATE"]);
        $extra = "onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $rePayDate = knjCreateTextBox($objForm, $repay_money_date, "REPAY_DATE", 12, 12, $extra.$disabled2);

        //読込ボタンを作成する
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=REPAY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['REPAY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $rePayBtn = knjCreateBtn($objForm, "btn_calen2", "･･･", $extra.$disabled2);
        $arg["data"]["REPAY_DATE"] = $rePayDate.$rePayBtn;

        //返金区分
        $repayNmst = $model->s_exist_flg_ikkatsu == "1" ? "G209" : "G212";
        $query = knjp121kQuery::getNamecd($model->year, $repayNmst);
        $dummyVal = $model->field["REPAY_DEV"];
        makeCombo($objForm, $arg, $db, $query, $dummyVal, "REPAY_DEV", $disabled2, 1, "BLANK");

        //備考
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $model->field["REMARK"], "REMARK", 40, 40, "");

        //対象生徒
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, 20);
        //その他の生徒
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, 20);
        //≪ボタン
        $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //＜ボタン
        $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //＞ボタン
        $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //≫ボタン
        $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        $arg["info"] = array("LEFT_LIST"  => "対象者一覧",
                             "RIGHT_LIST" => "生徒一覧");

        //ボタン作成
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('all_update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //戻るボタンを作成する
        $link = "knjp121kindex.php?cmd=back";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "TMP_PAID_MONEY");
        knjCreateHidden($objForm, "TMP_PAID_MONEY_DATE");
        knjCreateHidden($objForm, "TMP_PAID_MONEY_DIV");
        knjCreateHidden($objForm, "TMP_REPAY_MONEY");
        knjCreateHidden($objForm, "selectdata");

        if (VARS::get("mode") == "ALL" || strlen($arg["data"]["checked1"])) {
            $arg["SetVal"]        = "SetVal(2);";
            $arg["Checkdisabled"] = "Checkdisabled(2);";
        }

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjp121kForm3.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    if ($name == "SEX") {
        $opt[] = array ("label" => "男女",
                        "value" => "99");
    }
    $result = $db->query($query);

    $dataFlg = false;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        $dataFlg = $value == $row["VALUE"] ? true : $dataFlg;
    }
    $result->free();

    $value = ($value) && $dataFlg ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
