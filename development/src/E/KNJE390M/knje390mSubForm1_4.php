<?php

require_once('for_php7.php');
class knje390mSubForm1_4
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_4", "POST", "knje390mindex.php", "", "subform1_4");

        //DB接続
        $db = Query::dbCheckOut();
        
        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt  = makeList($arg, $db, $model, "1");
        $rirekiCnt2 = makeList($arg, $db, $model, "2");
        $rirekiCnt3 = makeList($arg, $db, $model, "4");

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform1_healthcare1") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }

        //健康管理情報取得
        //服薬情報
        if ($model->cmd == "subform1_healthcare_set") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery1HealthcareGetData($model, "1", $model->getRecordSeq), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        //服薬状況
        //薬品名
        $extra = "";
        // $arg["data"]["MEDICINE_NAME"] = knjCreateTextBox($objForm, $Row["MEDICINE_NAME"], "MEDICINE_NAME", 40, 40, $extra);
        $arg["data"]["MEDICINE_NAME"] = getTextOrArea($objForm, "MEDICINE_NAME", 16, 1, $Row["MEDICINE_NAME"]);
        $arg["data"]["MEDICINE_NAME_SIZE"] = '<font size="2" color="red">(全角16文字まで)</font>';
        //病名・症状
        $extra = "style=\"height:50px; overflow:auto;\"";
        // $arg["data"]["DISEASE_CONDITION_NAME"] = knjCreateTextArea($objForm, "DISEASE_CONDITION_NAME", 3, 31, "soft", $extra, $Row["DISEASE_CONDITION_NAME"]);
        $arg["data"]["DISEASE_CONDITION_NAME"] = getTextOrArea($objForm, "DISEASE_CONDITION_NAME", 15, 3, $Row["DISEASE_CONDITION_NAME"]);
        $arg["data"]["DISEASE_CONDITION_NAME_SIZE"] = '<font size="2" color="red">(全角15文字X3行まで)</font>';
        //飲ませ方・配慮事項
        $extra = "style=\"height:80px; overflow:auto;\"";
        // $arg["data"]["CARE_WAY"] = knjCreateTextArea($objForm, "CARE_WAY", 6, 31, "soft", $extra, $Row["CARE_WAY"]);
        $arg["data"]["CARE_WAY"] = getTextOrArea($objForm, "CARE_WAY", 15, 6, $Row["CARE_WAY"]);
        $arg["data"]["CARE_WAY_SIZE"] = '<font size="2" color="red">(全角15文字X6行まで)</font>';

        //医療的ケア--------------------------
        if ($model->cmd == "subform1_healthcare_set2") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row2 = $db->getRow(knje390mQuery::getSubQuery1HealthcareGetData($model, "2", $model->getRecordSeq), DB_FETCHMODE_ASSOC);
            } else {
                $Row2 =& $model->field;
            }
        } else {
            $Row2 =& $model->field;
        }
        //名称
        $query = knje390mQuery::getMedicalCareNameMst("");
        makeCmb($objForm, $arg, $db, $query, "MEDICAL_NAMECD", $Row2["MEDICAL_NAMECD"], "", 1, 1);
        //学校
        $extra  = "id=\"SCHOOL_CARE\"";
        $extra .= ($Row2["SCHOOL_CARE"] == "1") ? "checked='checked' " : "";
        $arg["data"]["SCHOOL_CARE"] = knjCreateCheckBox($objForm, "SCHOOL_CARE", "1", $extra);
        //家庭・病院
        $extra  = "id=\"HOUSE_CARE\"";
        $extra .= ($Row2["HOUSE_CARE"] == "1") ? "checked='checked' " : "";
        $arg["data"]["HOUSE_CARE"] = knjCreateCheckBox($objForm, "HOUSE_CARE", "1", $extra);
        //事業所
        $extra  = "id=\"CENTER_CARE\"";
        $extra .= ($Row2["CENTER_CARE"] == "1") ? "checked='checked' " : "";
        $arg["data"]["CENTER_CARE"] = knjCreateCheckBox($objForm, "CENTER_CARE", "1", $extra);

        //食物--------------------------
        if (isset($model->schregno) && !isset($model->warning)) {
            $Row3 = $db->getRow(knje390mQuery::getSubQuery1HealthcareGetData($model, "3"), DB_FETCHMODE_ASSOC);
        } else {
            $Row3 =& $model->field;
        }
        //除去する食品
        // $arg["data"]["ALLERGIA_FOOD_CAT"] = knjCreateTextBox($objForm, $Row3["ALLERGIA_FOOD_CAT"], "ALLERGIA_FOOD_CAT", 70, 70, "");
        $arg["data"]["ALLERGIA_FOOD_CAT"] = getTextOrArea($objForm, "ALLERGIA_FOOD_CAT", 35, 1, $Row3["ALLERGIA_FOOD_CAT"]);
        $arg["data"]["ALLERGIA_FOOD_CAT_SIZE"] = '<font size="1" color="red">(全角35文字まで)</font>';
        //取組プラン有無
        $extra  = "id=\"ALLERGIA_PLAN\"";
        $extra .= ($Row3["ALLERGIA_PLAN"] == "1") ? "checked='checked' " : "";
        $arg["data"]["ALLERGIA_PLAN"] = knjCreateCheckBox($objForm, "ALLERGIA_PLAN", "1", $extra);
        //特記事項
        // $arg["data"]["ALLERGIA_SPECIAL_REPORT"] = knjCreateTextBox($objForm, $Row3["ALLERGIA_SPECIAL_REPORT"], "ALLERGIA_SPECIAL_REPORT", 80, 80, "");
        $arg["data"]["ALLERGIA_SPECIAL_REPORT"] = getTextOrArea($objForm, "ALLERGIA_SPECIAL_REPORT", 40, 1, $Row3["ALLERGIA_SPECIAL_REPORT"]);
        $arg["data"]["ALLERGIA_SPECIAL_REPORT_SIZE"] = '<font size="1" color="red">(全角40文字まで)</font>';
        //食形態
        // $arg["data"]["ALLERGIA_FOOD_STYLE"] = knjCreateTextBox($objForm, $Row3["ALLERGIA_FOOD_STYLE"], "ALLERGIA_FOOD_STYLE", 80, 80, "");
        $arg["data"]["ALLERGIA_FOOD_STYLE"] = getTextOrArea($objForm, "ALLERGIA_FOOD_STYLE", 40, 1, $Row3["ALLERGIA_FOOD_STYLE"]);
        $arg["data"]["ALLERGIA_FOOD_STYLE_SIZE"] = '<font size="1" color="red">(全角40文字まで)</font>';
        //その他のアレルギー
        // $arg["data"]["ALLERGIA_REMARK"] = knjCreateTextBox($objForm, $Row3["ALLERGIA_REMARK"], "ALLERGIA_REMARK", 80, 80, "");
        $arg["data"]["ALLERGIA_REMARK"] = getTextOrArea($objForm, "ALLERGIA_REMARK", 40, 1, $Row3["ALLERGIA_REMARK"]);
        $arg["data"]["ALLERGIA_REMARK_SIZE"] = '<font size="1" color="red">(全角40文字まで)</font>';

        //発作
        if ($model->cmd == "subform1_healthcare_set3") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row4 = $db->getRow(knje390mQuery::getSubQuery1HealthcareGetData($model, "4", $model->getRecordSeq), DB_FETCHMODE_ASSOC);
            } else {
                $Row4 =& $model->field;
            }
        } else {
            $Row4 =& $model->field;
        }
        //頻度
        $extra = "id=\"REMARK1\"";
        // $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row4["REMARK1"], "REMARK1", 30, 15, $extra);
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", 15, 1, $Row4["REMARK1"]);
        $arg["data"]["REMARK1_SIZE"] = '<font size="1" color="red">(全角15文字まで)</font>';
        //症状
        $extra = "id=\"REMARK2\"";
        // $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $Row4["REMARK2"], "REMARK2", 30, 15, $extra);
        $arg["data"]["REMARK2"] = getTextOrArea($objForm, "REMARK2", 15, 1, $Row4["REMARK2"]);
        $arg["data"]["REMARK2_SIZE"] = '<font size="1" color="red">(全角15文字まで)</font>';
        //発作時の対応
        $extra = "id=\"REMARK3\"";
        // $arg["data"]["REMARK3"] = knjCreateTextArea($objForm, "REMARK3", 4, 30, "soft", $extra, $Row4["REMARK3"]);
        $arg["data"]["REMARK3"] = getTextOrArea($objForm, "REMARK3", 15, 4, $Row4["REMARK3"]);
        $arg["data"]["REMARK3_SIZE"] = '<font size="1" color="red">(全角15文字X4行まで)</font>';
        
        //配慮事項
        if (isset($model->schregno) && !isset($model->warning)) {
            $Row5 = $db->getRow(knje390mQuery::getSubQuery1HealthcareGetData($model, "5"), DB_FETCHMODE_ASSOC);
        } else {
            $Row5 =& $model->field;
        }

        //配慮事項
        $extra = "id=\"REMARK4\"";
        // $arg["data"]["REMARK4"] = knjCreateTextArea($objForm, "REMARK4", 4, 80, "soft", $extra, $Row5["REMARK4"]);
        $arg["data"]["REMARK4"] = getTextOrArea($objForm, "REMARK4", 42, 10, $Row5["REMARK4"]);
        $arg["data"]["REMARK4_SIZE"] = '<font size="1" color="red">(全角42文字X10行まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm1_4.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model, $recordDiv)
{
    $retCnt = 0;
    $query = knje390mQuery::getSubQuery1HealthcareRecordList($model, $recordDiv);
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($recordDiv === '1') {
            $rowlist["CONTENTS_NAME"] = '服薬状況'.($retCnt+1);
            $arg["data2"][] = $rowlist;
        } elseif ($recordDiv === '2') {
            $rowlist["CONTENTS_NAME"] = '医療的ケア'.($retCnt+1);
            $rtnRow = $db->getRow(knje390mQuery::getMedicalCareNameMst($rowlist["MEDICAL_NAMECD"]), DB_FETCHMODE_ASSOC);
            $rowlist["MEDICAL_NAMECD"] = $rtnRow["NAME"];
            $rowlist["SCHOOL_CARE"] = ($rowlist["SCHOOL_CARE"] == "1") ? "レ" : "";
            ;
            $rowlist["HOUSE_CARE"] = ($rowlist["HOUSE_CARE"] == "1") ? "レ" : "";
            ;
            $rowlist["CENTER_CARE"] = ($rowlist["CENTER_CARE"] == "1") ? "レ" : "";
            ;
            $arg["data3"][] = $rowlist;
        } elseif ($recordDiv === '4') {
            $rowlist["CONTENTS_NAME"] = '発作'.($retCnt+1);
            $rowlist["VSPASM_FREQUENCY"] = $rowlist["REMARK1"];
            $rowlist["VSPASM_SYMPTOM"] = $rowlist["REMARK2"];
            $rowlist["VSPASM_SUPPORT"] = $rowlist["REMARK3"];
            $arg["data4"][] = $rowlist;
        }
        $retCnt++;
    }
    $result->free();
    return $retCnt;
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
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{

    //医療的ケアマスタ参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=medical_care_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_healthcare"] = knjCreateBtn($objForm, "btn_healthcare", "医療的ケアマスタ参照", $extra.$disabled);

    //服薬状況（有）
    //追加ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disabled);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disabled);
    
    //医療的ケア
    //追加ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_insert_care');\"";
    $arg["button"]["btn_insertC"] = knjCreateBtn($objForm, "btn_insertC", "追 加", $extra.$disabled);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_update_care');\"";
    $arg["button"]["btn_updateC"] = knjCreateBtn($objForm, "btn_updateC", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_delete_care');\"";
    $arg["button"]["btn_deleteC"] = knjCreateBtn($objForm, "btn_deleteC", "削 除", $extra.$disabled);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('subform1A');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra.$disabled);

    //食物
    //更新ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_update_allergen');\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra.$disabled);

    //発作
    //追加ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_insert_spasm');\"";
    $arg["button"]["btn_insertD"] = knjCreateBtn($objForm, "btn_insertD", "追 加", $extra.$disabled);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_update_spasm');\"";
    $arg["button"]["btn_updateD"] = knjCreateBtn($objForm, "btn_updateD", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_delete_spasm');\"";
    $arg["button"]["btn_deleteD"] = knjCreateBtn($objForm, "btn_deleteD", "削 除", $extra.$disabled);

    //配慮事項
    //更新ボタン
    $extra = "onclick=\"return btn_submit('healthcare1_update_consid');\"";
    $arg["button"]["btn_updateE"] = knjCreateBtn($objForm, "btn_updateE", "更 新", $extra.$disabled);
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
    knjCreateHidden($objForm, "RECORD_DIV", $model->getRecordDiv);
    knjCreateHidden($objForm, "RECORD_SEQ", $model->getRecordSeq);
}
?>

