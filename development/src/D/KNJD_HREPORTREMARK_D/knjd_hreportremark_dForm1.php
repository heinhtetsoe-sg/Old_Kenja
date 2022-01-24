<?php

require_once('for_php7.php');

class knjd_hreportremark_dForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjd_hreportremark_dindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $opt = array();
        $result = $db->query(knjd_hreportremark_dQuery::getSemester($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit');\"";
        $arg["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //生徒情報
        $schInfo = $db->getRow(knjd_hreportremark_dQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $arg["NAME_SHOW"] = $model->schregno."　".$schInfo["NAME"];

        //出力項目取得
        $query = knjd_hreportremark_dQuery::getNameMst($model, "D034");
        $result = $db->query($query);
        $model->itemArray = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArray[$setItem["NAMECD2"]] = $setItem;
        }

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knjd_hreportremark_dQuery::getHrepSpecial($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row[$row["CODE"]] = $row["REMARK1"];
        }
        $result->free();

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            foreach ($model->itemArray as $key => $val) {
                $Row[$key] = $model->field["REMARK".$key];
            }
        }

        foreach ($model->itemArray as $key => $val) {
            $setData = array();

            if ($model->Properties["reportSpecialSize01_".$key]) {
                list($moji, $gyou) = preg_split("/\*/", $model->Properties["reportSpecialSize01_".$key]);
                $model->getPro["reportSpecial".$key."_moji"] = (int)trim($moji);
                $model->getPro["reportSpecial".$key."_gyou"] = (int)trim($gyou);
            } else {
                $model->getPro["reportSpecial".$key."_moji"] = 33;
                $model->getPro["reportSpecial".$key."_gyou"] = 1;
            }

            if ($model->getPro["reportSpecial".$key."_gyou"] > 1) {
                //textArea
                $height = $model->getPro["reportSpecial".$key."_gyou"] * 13.5 + ($model->getPro["reportSpecial".$key."_gyou"] -1 ) * 3 + 5;
                $extra = "style=\"height:".$height."px;\" onPaste=\"return show(this);\""; 
                $setData["RECORD_VAL"] = knjCreateTextArea($objForm, "REMARK".$key, $model->getPro["reportSpecial".$key."_gyou"], ($moji * 2 + 1), "soft", $extra, $Row[$key]);
            } else {
                //textbox
                $moji = $model->getPro["reportSpecial".$key."_moji"];
                $extra = "onPaste=\"return show(this);\"";
                $setData["RECORD_VAL"] = knjCreateTextBox($objForm, $Row[$key], "REMARK".$key, ($moji * 2), $moji, $extra);
            }
            $setData["RECORD_COMMENT"] = "(全角".$model->getPro["reportSpecial".$key."_moji"]."文字X".$model->getPro["reportSpecial".$key."_gyou"]."行まで)";
            $setData["RECORD_NAME"] = $val["NAME1"];
            $arg["data"][] = $setData;
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "remark01_moji", $model->remark01_moji);
        knjCreateHidden($objForm, "remark01_gyou", $model->remark01_gyou);
        knjCreateHidden($objForm, "remark02_moji", $model->remark02_moji);
        knjCreateHidden($objForm, "remark02_gyou", $model->remark02_gyou);
        knjCreateHidden($objForm, "remark03_moji", $model->remark03_moji);
        knjCreateHidden($objForm, "remark03_gyou", $model->remark03_gyou);
        knjCreateHidden($objForm, "remark04_moji", $model->remark04_moji);
        knjCreateHidden($objForm, "remark04_gyou", $model->remark04_gyou);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd_hreportremark_dForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>