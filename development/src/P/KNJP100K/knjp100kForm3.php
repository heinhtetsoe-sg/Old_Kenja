<?php

require_once('for_php7.php');

class knjp100kForm3
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjp100kindex.php", "", "sel");
        $arg["jscript"] = "";

        $db = Query::dbCheckOut();

        //対象ラジオボタン 1:個人指定 2:クラス指定
        $optSelectDiv = array(1, 2);
        $model->selectDiv = ($model->selectDiv) ? $model->selectDiv : "1";
        $extra = array(" OnClick=\"btn_submit('selectChange');\"", " OnClick=\"btn_submit('selectChange');\"");
        $radioArray = knjCreateRadio($objForm, "SELECT_DIV", $model->selectDiv, $extra, $optSelectDiv, get_count($optSelectDiv));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年組コンボ
        if ($model->selectDiv == "1") {
            $query = knjp100kQuery::GetHrclass();
            $leftTitle = "対象者一覧";
            $rightTitle = "生徒一覧";
        } else {
            $query = knjp100kQuery::GetGrade();
            $leftTitle = "対象クラス一覧";
            $rightTitle = "クラス一覧";
        }
        $extra = " OnChange=\"btn_submit('change_class');\"";
        makeCombo($objForm, $arg, $db, $query, $model->hrclass, "HRCLASS", $extra, 1);

        //生徒一覧
        $opt_left = array();
        $opt_right = array();
        $selectdata = explode(",", $model->selectdata);
        $result = $db->query(knjp100kQuery::GetStudent($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["VALUE"]] = array("label" => $row["LABEL"], 
                                                      "value" => $row["VALUE"]);

            if (!in_array($row["VALUE"], $selectdata)) {
                $opt_right[]  = array("label" => $row["LABEL"], 
                                      "value" => $row["VALUE"]);
            }
        }

        //左リストで選択されたものを再セット
        foreach ($model->select_opt as $key => $val) {
            if (in_array($key, $selectdata)) {
                $opt_left[] = $val;
            }
        }

        //申込コード
        $opt = array();
        $result = $db->query(knjp100kQuery::getApplicd($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["APPLICATIONCD"]."：".htmlspecialchars($row["APPLICATIONNAME"]),
                           "value" => $row["APPLICATIONCD"]);

            //申込コードごとの金額を設定
            $arg["data2"][] = array("idx" => $row["APPLICATIONCD"],  "money" => $row["APPLICATIONMONEY"]); 
        }
        $extra = "onChange=\"SetMoney();\"";
        $arg["data"]["APPLICATIONCD"] = knjCreateCombo($objForm, "APPLICATIONCD", $model->field["APPLICATIONCD"], $opt, $extra, 1);

        //納入必要金額
        $extraInt = " onblur=\"this.value=toInteger(this.value)\"";
        $extraRight = " style=\"text-align:right\"";
        $arg["data"]["APPLI_MONEY_DUE"] = knjCreateTextBox($objForm, $model->field["APPLI_MONEY_DUE"], "APPLI_MONEY_DUE", 10, 8, $extraInt.$extraRight);

        //申込日
        $applied_date = str_replace("-", "/", $model->field["APPLIED_DATE"]);
        $arg["data"]["APPLIED_DATE"] = View::popUpCalendar($objForm, "APPLIED_DATE", $applied_date);

        //入金額
        $arg["data"]["APPLI_PAID_MONEY"] = knjCreateTextBox($objForm, $model->field["APPLI_PAID_MONEY"], "APPLI_PAID_MONEY", 10, 8, $extraInt.$extraRight);

        //入金区分
        $query = knjp100kQuery::nameGet();
        makeCombo($objForm, $arg, $db, $query, $model->field["APPLI_PAID_DIV"], "APPLI_PAID_DIV", "", 1, "BLANK");

        //納期日
        $inst_due_date = str_replace("-", "/", $model->field["APPLI_PAID_DATE"]);
        $arg["data"]["APPLI_PAID_DATE"] = View::popUpCalendar($objForm, "APPLI_PAID_DATE", $inst_due_date);

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

        $arg["info"] = array("LEFT_LIST"  => $leftTitle,
                             "RIGHT_LIST" => $rightTitle);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $arg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp100kForm3.html", $arg); 
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('all_add')\"";
    $arg["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('all_update')\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('all_delete')\"";
    $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $link = "knjp100kindex.php?cmd=back";
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

function makeHidden(&$objForm, &$arg) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}

?>
