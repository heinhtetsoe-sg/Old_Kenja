<?php

require_once('for_php7.php');

class knje390nSubForm1_9
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_9", "POST", "knje390nindex.php", "", "subform1_9");

        //DB接続
        $db = Query::dbCheckOut();
        
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //表示日付をセット
        if ($model->record_date === 'NEW') {
            $setHyoujiDate = '';
        } else {
            $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
        }

        //生徒情報
        $info = $db->getRow(knje390nQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;

        //視覚・聴覚情報取得
        if ($model->cmd == "subform1_visionEar" || $model->cmd == "subform1_visionEar_set"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390nQuery::getSubQuery1HealthGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        if($Row["DATE"] == ""){
            $Row["DATE"] = CTRL_DATE;
        }
        $Row["DATE"] = str_replace("-","/",$Row["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE" ,$Row["DATE"]);

        //視力
        //裸眼(右)
        $extra = "";
        $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $Row["R_BAREVISION"], "R_BAREVISION", 4, 4, $extra);
        
        //裸眼(左)
        $extra = "";
        $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $Row["L_BAREVISION"], "L_BAREVISION", 4, 4, $extra);
        
        //矯正(右)
        $extra = "";
        $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $Row["R_VISION"], "R_VISION", 4, 4, $extra);
        
        //矯正(左)
        $extra = "";
        $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $Row["L_VISION"], "L_VISION", 4, 4, $extra);

        //聴力
        //聴力(右)
        $extra = "";
        $arg["data"]["R_EAR_DB"] = knjCreateTextBox($objForm, $Row["R_EAR_DB"], "R_EAR_DB", 3, 3, $extra);
        
        //聴力(左)
        $extra = "";
        $arg["data"]["L_EAR_DB"] = knjCreateTextBox($objForm, $Row["L_EAR_DB"], "L_EAR_DB", 3, 3, $extra);

        //補聴器
        //装着開始(年齢)
        $extra = "";
        $arg["data"]["DET_REMARK1"] = knjCreateTextBox($objForm, $Row["DET_REMARK1"], "DET_REMARK1", 2, 2, $extra);
        
        //装着開始(ヵ月)
        $extra = "";
        $arg["data"]["DET_REMARK2"] = knjCreateTextBox($objForm, $Row["DET_REMARK2"], "DET_REMARK2", 2, 2, $extra);

        //人工内耳施術(年齢)
        $extra = "";
        $arg["data"]["DET_REMARK3"] = knjCreateTextBox($objForm, $Row["DET_REMARK3"], "DET_REMARK3", 2, 2, $extra);
        
        //人工内耳施術(ヵ月)
        $extra = "";
        $arg["data"]["DET_REMARK4"] = knjCreateTextBox($objForm, $Row["DET_REMARK4"], "DET_REMARK4", 2, 2, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390nSubForm1_9.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
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

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "RECORD_DIV", $Row["RECORD_DIV"]);
}
?>

