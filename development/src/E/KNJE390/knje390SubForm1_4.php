<?php

require_once('for_php7.php');

class knje390SubForm1_4
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_4", "POST", "knje390index.php", "", "subform1_4");

        //DB接続
        $db = Query::dbCheckOut();
        
        //表示日付をセット
        if ($model->record_date === 'NEW') {
            $setHyoujiDate = '';
        } else {
            $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
        }

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;
        // Add by PP for Title 2020-02-03 start
        if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = "B プロフィールの健康管理画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // for 915 error
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubFrom1_4_CurrentCursor915\");</script>";
        } else {
          echo "<script>var error195= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubFrom1_4_CurrentCursor915\", error195);
              sessionStorage.removeItem(\"KNJE390SubFrom1_4_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model, "1");
        $rirekiCnt2 = makeList($arg, $db, $model, "2");

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
        if ($model->cmd == "subform1_healthcare_set"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getSubQuery1HealthcareGetData($model, "1"), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        //服薬状況
        //薬品名
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label=\"薬品名\" id=\"MEDICINE_NAME\"";
        $arg["data"]["MEDICINE_NAME"] = knjCreateTextBox($objForm, $Row["MEDICINE_NAME"], "MEDICINE_NAME", 40, 40, $extra);
        // Add by PP for PC-Talker 2020-02-20 end
        //病名・症状
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:50px; overflow:auto;\" aria-label=\"病名・症状全角15文字3行まで\"";
        $arg["data"]["DISEASE_CONDITION_NAME"] = knjCreateTextArea($objForm, "DISEASE_CONDITION_NAME", 3, 31, "soft", $extra, $Row["DISEASE_CONDITION_NAME"]);
        $arg["data"]["DISEASE_CONDITION_NAME_SIZE"] = '<font size="1" color="red">(全角15文字3行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end
        //飲ませ方・配慮事項
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:80px; overflow:auto;\" aria-label=\"飲ませ方・配慮事項全角15文字6行まで\"";
        $arg["data"]["CARE_WAY"] = knjCreateTextArea($objForm, "CARE_WAY", 6, 31, "soft", $extra, $Row["CARE_WAY"]);
        $arg["data"]["CARE_WAY_SIZE"] = '<font size="1" color="red">(全角15文字6行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end

        //医療的ケア--------------------------
        if ($model->cmd == "subform1_healthcare_set2"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row2 = $db->getRow(knje390Query::getSubQuery1HealthcareGetData($model, "2"), DB_FETCHMODE_ASSOC);
            } else {
                $Row2 =& $model->field;
            }
        } else {
            $Row2 =& $model->field;
        }
        //名称
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getMedicalCareNameMst("");
        makeCmb($objForm, $arg, $db, $query, "MEDICAL_NAMECD", $Row2["MEDICAL_NAMECD"], "aria-label=\"ケアの種類\" id=\"MEDICAL_NAMECD\"", 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end
        //学校
        // Add by PP for PC-Talker 2020-02-03 start
        $extra  = "id=\"SCHOOL_CARE\" aria-label=\"学校\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $extra .= ($Row2["SCHOOL_CARE"] == "1") ? "checked='checked' " : "";
        $arg["data"]["SCHOOL_CARE"] = knjCreateCheckBox($objForm, "SCHOOL_CARE", "1", $extra);
        //家庭・病院
        // Add by PP for PC-Talker 2020-02-03 start
        $extra  = "id=\"HOUSE_CARE\" aria-label=\"家庭病院\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $extra .= ($Row2["HOUSE_CARE"] == "1") ? "checked='checked' " : "";
        $arg["data"]["HOUSE_CARE"] = knjCreateCheckBox($objForm, "HOUSE_CARE", "1", $extra);
        //事業所
        // Add by PP for PC-Talker 2020-02-03 start
        $extra  = "id=\"CENTER_CARE\" aria-label=\"事業所\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $extra .= ($Row2["CENTER_CARE"] == "1") ? "checked='checked' " : "";
        $arg["data"]["CENTER_CARE"] = knjCreateCheckBox($objForm, "CENTER_CARE", "1", $extra);

        //食物--------------------------
        if (isset($model->schregno) && !isset($model->warning)){
            $Row3 = $db->getRow(knje390Query::getSubQuery1HealthcareGetData($model, "3"), DB_FETCHMODE_ASSOC);
        } else {
            $Row3 =& $model->field;
        }
        //除去する食品
        // Add by PP for PC-Talker 2020-02-03 start
        $arg["data"]["ALLERGIA_FOOD_CAT"] = knjCreateTextBox($objForm, $Row3["ALLERGIA_FOOD_CAT"], "ALLERGIA_FOOD_CAT", 70, 70, "aria-label=\"食物アレルギーの除去する食品\"");
        // Add by PP for PC-Talker 2020-02-20 end
        //取組プラン有無
        // Add by PP for PC-Talker 2020-02-03 start
        $extra  = "id=\"ALLERGIA_PLAN\" aria-label=\"食物アレルギーの取組プランの有\"";
        $extra .= ($Row3["ALLERGIA_PLAN"] == "1") ? "checked='checked' " : "";
        $arg["data"]["ALLERGIA_PLAN"] = knjCreateCheckBox($objForm, "ALLERGIA_PLAN", "1", $extra);
        // Add by PP for PC-Talker 2020-02-20 end
        //特記事項
        // Add by PP for PC-Talker 2020-02-03 start
        $arg["data"]["ALLERGIA_SPECIAL_REPORT"] = knjCreateTextBox($objForm, $Row3["ALLERGIA_SPECIAL_REPORT"], "ALLERGIA_SPECIAL_REPORT", 80, 80, "aria-label=\"食物アレルギーの取組プランの特記事項\"");
        // Add by PP for PC-Talker 2020-02-20 end
        //食形態
        // Add by PP for PC-Talker 2020-02-03 start
        $arg["data"]["ALLERGIA_FOOD_STYLE"] = knjCreateTextBox($objForm, $Row3["ALLERGIA_FOOD_STYLE"], "ALLERGIA_FOOD_STYLE", 80, 80, "aria-label=\"食形態\"");
        // Add by PP for PC-Talker 2020-02-20 end
        //その他のアレルギー
        // Add by PP for PC-Talker 2020-02-03 start
        $arg["data"]["ALLERGIA_REMARK"] = knjCreateTextBox($objForm, $Row3["ALLERGIA_REMARK"], "ALLERGIA_REMARK", 80, 80, "aria-label=\"その他のアレルギー\"");
        // Add by PP for PC-Talker 2020-02-20 end

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
        View::toHTML($model, "knje390SubForm1_4.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model, $recordDiv) {
    $retCnt = 0;
    $query = knje390Query::getSubQuery1HealthcareRecordList($model, $recordDiv);
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($recordDiv === '1') {
            $rowlist["CONTENTS_NAME"] = '服薬状況'.($retCnt+1);
            $arg["data2"][] = $rowlist;
        } else if ($recordDiv === '2') {
            $rowlist["CONTENTS_NAME"] = '医療的ケア'.($retCnt+1);
            $rtnRow = $db->getRow(knje390Query::getMedicalCareNameMst($rowlist["MEDICAL_NAMECD"]), DB_FETCHMODE_ASSOC);
            $rowlist["MEDICAL_NAMECD"] = $rtnRow["NAME"];
            $rowlist["SCHOOL_CARE"] = ($rowlist["SCHOOL_CARE"] == "1") ? "レ" : "";;
            $rowlist["HOUSE_CARE"] = ($rowlist["HOUSE_CARE"] == "1") ? "レ" : "";;
            $rowlist["CENTER_CARE"] = ($rowlist["CENTER_CARE"] == "1") ? "レ" : "";;
            $arg["data3"][] = $rowlist;
        }
        $retCnt++;
    }
    $result->free();
    return $retCnt;
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
function makeBtn(&$objForm, &$arg)
{

    //医療的ケアマスタ参照
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=medical_care_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_healthcare"] = knjCreateBtn($objForm, "btn_healthcare", "医療的ケアマスタ参照", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end

    //服薬状況（有）
    //追加ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_insert\" onclick=\"current_cursor('btn_insert'); return btn_submit('healthcare1_insert');\" aria-label=\"追加\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    //更新ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('healthcare1_update');\" aria-label=\"更新\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    //削除ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_delete\" onclick=\"current_cursor('btn_delete'); return btn_submit('healthcare1_delete');\" aria-label=\"削除\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    
    //医療的ケア
    //追加ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_insertC\" onclick=\"current_cursor('btn_insertC'); return btn_submit('healthcare1_insert_care');\" aria-label=\"追加\"";
    $arg["button"]["btn_insertC"] = knjCreateBtn($objForm, "btn_insertC", "追 加", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    //更新ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_updateC\" onclick=\"current_cursor('btn_updateC'); return btn_submit('healthcare1_update_care');\" aria-label=\"更新\"";
    $arg["button"]["btn_updateC"] = knjCreateBtn($objForm, "btn_updateC", "更 新", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    //削除ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_deleteC\" onclick=\"current_cursor('btn_deleteC'); return btn_submit('healthcare1_delete_care');\" aria-label=\"削除\"";
    $arg["button"]["btn_deleteC"] = knjCreateBtn($objForm, "btn_deleteC", "削 除", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    //戻るボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "onclick=\"return btn_submit('subform1A');\" aria-label=\"戻る\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end

    //食物
    //更新ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_update2\" onclick=\"current_cursor('btn_update2'); return btn_submit('healthcare1_update2');\" aria-label=\"更新\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

