<?php

require_once('for_php7.php');

class knjp912_mainForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjp912_mainForm1", "POST", "knjp912_mainindex.php", "", "knjp912_mainForm1");

        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit') {
            $Row = knjp912_mainQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjp912_mainQuery::getCloseFlgData($model));

        //表示項目
        //伝票番号
        $arg["data"]["REQUEST_NO"] = $model->getRequestNo;

        //年組番表示
        $extra  = "id=\"HR_CLASS_HYOUJI_FLG\"  onclick=\"return btn_submit('edit');\"";
        if ($model->field["HR_CLASS_HYOUJI_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["HR_CLASS_HYOUJI_FLG"] = knjCreateCheckBox($objForm, "HR_CLASS_HYOUJI_FLG", "1", $extra);
                
        //支出細目
        $extra = "";
        $query = knjp912_mainQuery::getLevySDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["HENKIN_L_M_S_CD"], "HENKIN_L_M_S_CD", $extra, 1, "BLANK", $model);
        
        //入金科目(細目)
        $extra = "";
        $query = knjp912_mainQuery::getCollectSDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["COLLECT_L_M_S_CD"], "COLLECT_L_M_S_CD", $extra, 1, "BLANK", $model);

        //返金額
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["HENKIN_GK"] = knjCreateTextBox($objForm, $Row["HENKIN_GK"], "HENKIN_GK", 6, 6, $extra);

        //人数
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["HENKIN_CNT"] = knjCreateTextBox($objForm, $Row["HENKIN_CNT"], "HENKIN_CNT", 6, 6, $extra);
        
        //返金総額
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_HENKIN_GK"] = knjCreateTextBox($objForm, $Row["TOTAL_HENKIN_GK"], "TOTAL_HENKIN_GK", 6, 6, $extra);
        
        //生徒一覧 OR 科目一覧リスト
        $opt_right = array();
        $opt_left  = array();

        //左側
        $query = knjp912_mainQuery::getSchno($model, "get");
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
        $result->free();
        //右側
        $query = knjp912_mainQuery::getSchno($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();

        //生徒一覧
        $extra = "multiple style=\"width:275px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjcreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:275px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjcreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjcreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjcreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjcreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjcreateBtn($objForm, "btn_left1", "＜", $extra);

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp912_mainForm1.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //更 新
    if ($model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('update');\"";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    
    //削 除
    if ($model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('delete');\"";
    }
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectStudent");
    knjCreateHidden($objForm, "selectStudentLabel");
}

?>
