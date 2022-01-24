<?php

require_once('for_php7.php');
class knje390mSubForm1_9
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_9", "POST", "knje390mindex.php", "", "subform1_9");

        //DB接続
        $db = Query::dbCheckOut();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //視覚・聴覚情報取得
        if ($model->cmd == "subform1_visionEar" || $model->cmd == "subform1_visionEar_set") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery1HealthGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        if ($Row["DATE"] == "") {
            $Row["DATE"] = CTRL_DATE;
        }
        $Row["DATE"] = str_replace("-", "/", $Row["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $Row["DATE"]);

        //視力
        //裸眼(右)
        $disabled = ($Row["R_VISION_CANTMEASURE"] == "1") ? " disabled " : "";
        $extra = "";
        $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $Row["R_BAREVISION"], "R_BAREVISION", 4, 4, $extra.$disabled);
        //矯正(右)
        $extra = "";
        $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $Row["R_VISION"], "R_VISION", 4, 4, $extra.$disabled);
        //測定困難(右)
        $checked = ($Row["R_VISION_CANTMEASURE"] == "1") ? "checked" : "";
        $extra = "id=\"R_VISION_CANTMEASURE\" onClick=\"disabledCantmeasure(this, 'R_VISION');\"";
        $arg["data"]["R_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_VISION_CANTMEASURE", "1", $extra.$checked, "");

        //裸眼(左)
        $disabled = ($Row["L_VISION_CANTMEASURE"] == "1") ? " disabled " : "";
        $extra = "";
        $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $Row["L_BAREVISION"], "L_BAREVISION", 4, 4, $extra.$disabled);
        //矯正(左)
        $extra = "";
        $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $Row["L_VISION"], "L_VISION", 4, 4, $extra.$disabled);
        //測定困難(左)
        $checked = ($Row["L_VISION_CANTMEASURE"] == "1") ? "checked" : "";
        $extra = "id=\"L_VISION_CANTMEASURE\" onClick=\"disabledCantmeasure(this, 'L_VISION');\"";
        $arg["data"]["L_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_VISION_CANTMEASURE", "1", $extra.$checked, "");

        //裸眼(両眼)
        $disabled = ($Row["RL_VISION_CANTMEASURE"] == "1") ? " disabled " : "";
        $extra = "";
        $arg["data"]["RL_BAREVISION"] = knjCreateTextBox($objForm, $Row["RL_BAREVISION"], "RL_BAREVISION", 4, 4, $extra.$disabled);
        //矯正(両眼)
        $extra = "";
        $arg["data"]["RL_VISION"] = knjCreateTextBox($objForm, $Row["RL_VISION"], "RL_VISION", 4, 4, $extra.$disabled);
        //測定困難(両眼)
        $checked = ($Row["RL_VISION_CANTMEASURE"] == "1") ? "checked" : "";
        $extra = "id=\"RL_VISION_CANTMEASURE\"  onClick=\"disabledCantmeasure(this, 'RL_VISION');\"";
        $arg["data"]["RL_VISION_CANTMEASURE"] = knjCreateCheckBox($objForm, "RL_VISION_CANTMEASURE", "1", $extra.$checked, "");

        //聴力
        //聴力状態コンボ
        $query = knje390mQuery::getNameMst("F010");
        //聴力(右)
        $disabled = ($Row["R_EAR_CANTMEASURE"] == "1") ? " disabled " : "";
        $extra = "";
        $arg["data"]["R_EAR_DB"] = knjCreateTextBox($objForm, $Row["R_EAR_DB"], "R_EAR_DB", 3, 3, $extra.$disabled);
        //状態(右)
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "R_EAR", $Row["R_EAR"], $extra.$disabled, 1, 1);
        //測定困難(右)
        $checked = ($Row["R_EAR_CANTMEASURE"] == "1") ? "checked" : "";
        $extra = "id=\"R_EAR_CANTMEASURE\" onClick=\"disabledCantmeasure(this, 'R_EAR');\"";
        $arg["data"]["R_EAR_CANTMEASURE"] = knjCreateCheckBox($objForm, "R_EAR_CANTMEASURE", "1", $extra.$checked, "");

        //聴力(左)
        $disabled = ($Row["L_EAR_CANTMEASURE"] == "1") ? " disabled " : "";
        $extra = "";
        $arg["data"]["L_EAR_DB"] = knjCreateTextBox($objForm, $Row["L_EAR_DB"], "L_EAR_DB", 3, 3, $extra.$disabled);
        //状態(左)
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "L_EAR", $Row["L_EAR"], $extra.$disabled, 1, 1);
        //測定困難(左)
        $checked = ($Row["L_EAR_CANTMEASURE"] == "1") ? "checked" : "";
        $extra = "id=\"L_EAR_CANTMEASURE\" onClick=\"disabledCantmeasure(this, 'L_EAR');\"";
        $arg["data"]["L_EAR_CANTMEASURE"] = knjCreateCheckBox($objForm, "L_EAR_CANTMEASURE", "1", $extra.$checked, "");

        //聴力(両耳)
        $extra = "";
        $arg["data"]["DET_REMARK3"] = knjCreateTextBox($objForm, $Row["DET_REMARK3"], "DET_REMARK3", 3, 3, $extra);
        //閾値(補聴器装用時)
        $extra = "";
        $arg["data"]["DET_REMARK4"] = knjCreateTextBox($objForm, $Row["DET_REMARK4"], "DET_REMARK4", 3, 3, $extra);
        //閾値(人口内耳)
        $extra = "";
        $arg["data"]["DET_REMARK5"] = knjCreateTextBox($objForm, $Row["DET_REMARK5"], "DET_REMARK5", 3, 3, $extra);
        //メーカー・機種・シリアル番号
        $extra = "";
        $moji = 25;
        $gyo = 2;
        // $arg["data"]["DET_REMARK6"] = KnjCreateTextArea($objForm, "DET_REMARK6", $gyo, ($moji * 2 + 1), "soft", "", $Row["DET_REMARK6"]);
        $arg["data"]["DET_REMARK6"] = getTextOrArea($objForm, "DET_REMARK6", $moji, $gyo, $Row["DET_REMARK6"]);
        $arg["data"]["DET_REMARK6_SIZE"] = '<font size="2" color="red">(全角'.$moji.'文字X'.$gyo.'行まで)</font>';
        //主なコミュニケーション手段
        $extra = "";
        // $arg["data"]["DET_REMARK7"] = knjCreateTextBox($objForm, $Row["DET_REMARK7"], "DET_REMARK7", 52, 50, $extra);
        $arg["data"]["DET_REMARK7"] = getTextOrArea($objForm, "DET_REMARK7", 25, 1, $Row["DET_REMARK7"]);
        $arg["data"]["DET_REMARK7_SIZE"] = '<font size="2" color="red">(全角25文字まで)</font>';


        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm1_9.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"], 'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('visionEar1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('subform1_visionEar');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform1A');\"");
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "RECORD_DIV", $Row["RECORD_DIV"]);
}
?>

