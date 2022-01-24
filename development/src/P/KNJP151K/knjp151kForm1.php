<?php

require_once('for_php7.php');

class knjp151kForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjp151kindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //更新対象費目
        $opt = array();
        $result = $db->query(knjp151kQuery::getScd());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["TOTALCD"]."：".htmlspecialchars($row["EXPENSE_S_NAME"]),
                           "value" => $row["TOTALCD"]);
 

            $arg["data2"][] = array("idx"   => $row["TOTALCD"],
                                    "money" => $row["EXPENSE_S_MONEY"],
                                    "mday"  => str_replace("-", "/", $row["BANK_TRANS_SDATE"])); 
        }
        $model->field["TOTALCD"] = $model->field["TOTALCD"] ? $model->field["TOTALCD"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('edit_clear');\"";
        $arg["data"]["TOTALCD"] = knjCreateCombo($objForm, "TOTALCD", $model->field["TOTALCD"], $opt, $extra, 1);

        //年組コンボ
        $query = knjp151kQuery::GetHrclass();
        $extra = " OnChange=\"btn_submit('edit');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["HRCLASS"], "HRCLASS", $extra, 1);

        //性別コンボ
        $query = knjp151kQuery::getNamecd(Z002, true);
        $extra = " OnChange=\"btn_submit('edit');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["SEX"], "SEX", $extra, 1);

        //返金データ選択
        $query = knjp151kQuery::getRepaySeq($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["REPAY_SEQ"], "REPAY_SEQ", $extra, 1, "NEW");

        //返金区分ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2);
        $model->field["REPAY_FLG"] = ($model->field["REPAY_FLG"] == "") ? "1" : $model->field["REPAY_FLG"];
        $extra = array("id=\"REPAY_FLG1\"", "id=\"REPAY_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "REPAY_FLG", $model->field["REPAY_FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //生徒一覧
        $opt_left = array();
        $opt_right = array();
        $selectdata = explode(",", $model->selectdata);

        $result = $db->query(knjp151kQuery::GetStudent($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."  ".$row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"], 
                                                         "value" => $row["SCHREGNO"]);

            if (!in_array($row["SCHREGNO"], $selectdata)) {
                $opt_right[]  = array("label" => $row["HR_NAME"]."  ".$row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"], 
                                      "value" => $row["SCHREGNO"]);
            }
        }

        //左リストで選択されたものを再セット
        if (is_array($model->select_opt)) {
            foreach ($model->select_opt as $key => $val) {
                if (in_array($key, $selectdata)) {
                    $opt_left[] = $val;
                }
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
            $arg["data"]["CHECKED".$i] = knjCreateCheckBox($objForm, "CHECKED".$i, $i, $extra, "");
        }

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
        makeBtn($objForm, $arg);
        //hidden
        makeHidden($objForm);

        if (VARS::get("mode") == "ALL" || strlen($arg["data"]["checked1"])) {
            $arg["SetVal"]        = "SetVal(2);";
            $arg["Checkdisabled"] = "Checkdisabled(2);";
        }

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjp151kForm1.html", $arg); 
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
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('all_update')\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}

?>
