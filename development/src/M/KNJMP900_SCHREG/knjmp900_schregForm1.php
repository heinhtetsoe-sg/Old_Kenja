<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knjmp900_schregForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjmp900_schregForm1", "POST", "knjmp900_schregindex.php", "", "knjmp900_schregForm1");

        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit') {
            $Row = knjmp900_schregQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjmp900_schregQuery::getCloseFlgData($model));

        //入金科目と決済の状況を取得(比較時に利用)
        if ($model->getIncomeLMcd) {
            $model->getApproval = $db->getOne(knjmp900_schregQuery::getLevyData($model, "APPROVAL"));
            $model->getCancel   = $db->getOne(knjmp900_schregQuery::getLevyData($model, "CANCEL"));
        } else {
            $model->getApproval = "";
            $model->getCancel   = "";
        }
        
        //収入科目
        $query = knjmp900_schregQuery::getLevyMDiv($model);
        $arg["data"]["INCOME_M_NAME"] = $db->getOne($query);
        
        //生徒、生徒以外切替
        $opt = array(1, 2);
        //初期値(データなしの場合)
        if (!$Row["WARIHURI_DIV"]) {
            $Row["WARIHURI_DIV"] = "1";
        }
        $model->field["WARIHURI_DIV"] = ($model->field["WARIHURI_DIV"] == "") ? $Row["WARIHURI_DIV"] : $model->field["WARIHURI_DIV"];
        $extra = array("id=\"WARIHURI_DIV1\" onclick=\"return btn_submit('knjmp900_schreg');\"", "id=\"WARIHURI_DIV2\" onclick=\"return btn_submit('knjmp900_schreg');\"");
        $radioArray = knjCreateRadio($objForm, "WARIHURI_DIV", $model->field["WARIHURI_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年組番表示
        $extra  = "id=\"HR_CLASS_HYOUJI_FLG\"  onclick=\"return btn_submit('edit');\"";
        if ($model->field["HR_CLASS_HYOUJI_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["HR_CLASS_HYOUJI_FLG"] = knjCreateCheckBox($objForm, "HR_CLASS_HYOUJI_FLG", "1", $extra);

        //NO
        $arg["data"]["NO"] = $model->getLineNo;
        
        //会費名等
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["COMMODITY_NAME"] = knjCreateTextBox($objForm, $Row["COMMODITY_NAME"], "COMMODITY_NAME", 30, 120, $extra);
        
        //単価
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["COMMODITY_PRICE"] = knjCreateTextBox($objForm, $Row["COMMODITY_PRICE"], "COMMODITY_PRICE", 6, 6, $extra);

        //数量
        if ($model->field["WARIHURI_DIV"] == "1") {
            $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        } else {
            $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        }
        $arg["data"]["COMMODITY_CNT"] = knjCreateTextBox($objForm, $Row["COMMODITY_CNT"], "COMMODITY_CNT", 6, 6, $extra);
        
        //合計金額
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_PRICE"] = knjCreateTextBox($objForm, $Row["TOTAL_PRICE"], "TOTAL_PRICE", 6, 6, $extra);

        //備考
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 120, $extra);

        //返金可の場合
        if ($model->field["WARIHURI_DIV"] == "1") {
            $arg["WARIHURI_OK"] = "1";
            //生徒一覧 OR 科目一覧リスト
            $opt_right = array();
            $opt_left  = array();

            //左側
            $query = knjmp900_schregQuery::getSchno($model, "get");
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_left[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            }
            $result->free();
            //右側
            $query = knjmp900_schregQuery::getSchno($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
            $result->free();

            //生徒一覧
            $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left')\"";
            $arg["data"]["CATEGORY_NAME"] = knjcreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

            //出力対象一覧リスト
            $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right')\"";
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
        }

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjmp900_schregForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //更 新
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        if ($model->field["WARIHURI_DIV"] == "1") {
            $extra = " onclick=\"return btn_submit('update');\"";
        } else {
            $extra = " onclick=\"return btn_submit('not_warihuri_update');\"";
        }
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    
    //削 除
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('delete');\"";
    }
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    
    //戻る
    $extra = "";
    $subdata  = "wopen('".REQUESTROOT."/M/KNJMP900_MAIN/knjmp900_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
    $subdata .= "&SEND_PRGID=KNJMP900_SCHREG&SEND_INCOME_L_CD={$model->getIncomeLcd}&SEND_INCOME_M_CD={$model->getIncomeMcd}";
    $subdata .= "&SEND_REQUEST_NO={$model->getRequestNo}";
    $subdata .= "&SEND_INCOME_L_M_CD={$model->getIncomeLMcd}&SEND_YEAR={$model->getYear}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $extra = "onclick=\"$subdata\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectStudent");
    knjCreateHidden($objForm, "selectStudentLabel");
}

?>
