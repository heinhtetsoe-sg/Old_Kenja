<?php

require_once('for_php7.php');

class knjd_hreportremark_d_2Form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjd_hreportremark_d_2index.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $opt = array();
        $result = $db->query(knjd_hreportremark_d_2Query::getSemester($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit');\"";
        $arg["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //生徒情報
        $schInfo = $db->getRow(knjd_hreportremark_d_2Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $arg["NAME_SHOW"] = $model->schregno."　".$schInfo["NAME"];

        //出力項目取得
        $query = knjd_hreportremark_d_2Query::getNameMst($model, "D034");
        $result = $db->query($query);
        $model->itemArray = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArray[$setItem["NAMECD2"]] = $setItem;
        }

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knjd_hreportremark_d_2Query::getHrepSpecial($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row[$row["CODE"]] = $row["REMARK1"];
        }
        $result->free();

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        $recordrow = array();
        $iswarning = false;
        if (isset($model->warning)) {
            foreach ($model->itemArray as $key => $val) {
                $Row[$key] = $model->field["REMARK".$key];
            }
            $recordrow["01"] = $model->field["RECORD_VAL01"];
            $recordrow["02"] = $model->field["RECORD_VAL02"];
            $iswarning = true;
        }
        
        //行動の状況および部活動の取得
        $setRecordVal["01"] = array();
        $setRecordVal["02"] = array();
        foreach ($setRecordVal as $recKey => $recVal) {
            $setgyou1 = "";
            if ($model->Properties["reportSpecialSize02_".$recKey]) {
                list($moji, $gyou) = preg_split("/\*/", $model->Properties["reportSpecialSize02_".$recKey]);
                $model->getPro["reportSpecialSize02_".$recKey."_moji"] = (int)trim($moji);
                $model->getPro["reportSpecialSize02_".$recKey."_gyou"] = (int)trim($gyou);
                $setgyou1 = (int)trim($gyou);
            } else {
                $model->getPro["reportSpecialSize02_".$recKey."_moji"] = 16;
                $model->getPro["reportSpecialSize02_".$recKey."_gyou"] = 6;
                $setgyou1 = 6;
            }
            $moji = $model->getPro["reportSpecialSize02_".$recKey."_moji"];
            $height = $model->getPro["reportSpecialSize02_".$recKey."_gyou"] * 13.5 + ($model->getPro["reportSpecialSize02_".$recKey."_gyou"] -1 ) * 3 + 5;
            $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $setgyou1, ($moji * 2), true);\" onPaste=\"return showPaste(this);\""; 
            $setRecordVal[$recKey]["RECORD_VAL"] = knjCreateTextArea($objForm, "RECORD_VAL".$recKey, $model->getPro["reportSpecial02_".$recKey."_gyou"], ($moji * 2 + 1), "soft", $extra,  $recordrow[$recKey]);
            $setRecordVal[$recKey]["RECORD_COMMENT"] = "(全角".$model->getPro["reportSpecialSize02_".$recKey."_moji"]."文字X".$model->getPro["reportSpecialSize02_".$recKey."_gyou"]."行まで)";
        }

        $row = array();
        $result = $db->query(knjd_hreportremark_d_2Query::getActivities($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setgyou2 = "";
            if ($model->Properties["reportSpecialSize02_".$row["CODE"]]) {
                list($moji, $gyou) = preg_split("/\*/", $model->Properties["reportSpecialSize02_".$row["CODE"]]);
                $model->getPro["reportSpecialSize02_".$row["CODE"]."_moji"] = (int)trim($moji);
                $model->getPro["reportSpecialSize02_".$row["CODE"]."_gyou"] = (int)trim($gyou);
                $setgyou2 = (int)trim($gyou);
            } else {
                $model->getPro["reportSpecialSize02_".$row["CODE"]."_moji"] = 16;
                $model->getPro["reportSpecialSize02_".$row["CODE"]."_gyou"] = 6;
                $setgyou2 = 6;
            }
            $moji = $model->getPro["reportSpecialSize02_".$row["CODE"]."_moji"];
            
            $setvalue = $iswarning ? $recordrow[$row["CODE"]] : $row["REMARK1"];
            $height = $model->getPro["reportSpecialSize02_".$row["CODE"]."_gyou"] * 13.5 + ($model->getPro["reportSpecialSize02_".$row["CODE"]."_gyou"] -1 ) * 3 + 5;
            $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $setgyou2, ($moji * 2), true);\" onPaste=\"return showPaste(this);\""; 
            $setRecordVal[$row["CODE"]]["RECORD_VAL"] = knjCreateTextArea($objForm, "RECORD_VAL".$row["CODE"], $model->getPro["reportSpecial02_".$row["CODE"]."_gyou"], ($moji * 2 + 1), "soft", $extra, $setvalue);
            $setRecordVal[$row["CODE"]]["RECORD_COMMENT"] = "(全角".$model->getPro["reportSpecialSize02_".$row["CODE"]."_moji"]."文字X".$model->getPro["reportSpecialSize02_".$row["CODE"]."_gyou"]."行まで)";
        }
        $result->free();
        
        foreach ($setRecordVal as $key => $val) {
            $arg["data2"]["RECORD_VAL".$key] = $val["RECORD_VAL"];
            $arg["data2"]["RECORD_COMMENT".$key] = $val["RECORD_COMMENT"];
        }

        foreach ($model->itemArray as $key => $val) {
            $setData = array();
            $setgyou3 = "";
            if ($model->Properties["reportSpecialSize01_".$key]) {
                list($moji, $gyou) = preg_split("/\*/", $model->Properties["reportSpecialSize01_".$key]);
                $model->getPro["reportSpecial".$key."_moji"] = (int)trim($moji);
                $model->getPro["reportSpecial".$key."_gyou"] = (int)trim($gyou);
                $setgyou3 = (int)trim($gyou);
            } else {
                $model->getPro["reportSpecial".$key."_moji"] = 33;
                $model->getPro["reportSpecial".$key."_gyou"] = 1;
                $setgyou3 = 1;
            }

            if ($model->getPro["knjdHreportRemark_d2_UseText"] == "1") {
                if ($model->getPro["reportSpecial".$key."_gyou"] > 1) {
                    //textArea
                    $height = $model->getPro["reportSpecial".$key."_gyou"] * 13.5 + ($model->getPro["reportSpecial".$key."_gyou"] -1 ) * 3 + 5;
                    $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $setgyou3, ($moji * 2), true);\" onPaste=\"return showPaste(this);\""; 
                    $setData["RECORD_VAL"] = knjCreateTextArea($objForm, "REMARK".$key, $model->getPro["reportSpecial".$key."_gyou"], ($moji * 2 + 1), "soft", $extra, $Row[$key]); 
                } else {
                    //textbox
                    $moji = $model->getPro["reportSpecial".$key."_moji"];
                    $extra = "onPaste=\"return showPaste(this);\"";
                    $setData["RECORD_VAL"] = knjCreateTextBox($objForm, $Row[$key], "REMARK".$key, ($moji * 2), $moji, $extra);
                }
                $setData["RECORD_COMMENT"] = "(全角".$model->getPro["reportSpecial".$key."_moji"]."文字X".$model->getPro["reportSpecial".$key."_gyou"]."行まで)";
                $setData["RECORD_ALIGN"] = "left";
            } else {
                $check1 = ($Row[$key] == "1") ? "checked" : "";
                $extra = $check1." id=\"REMARK".$key."\"";
                $setData["RECORD_VAL"] = knjCreateCheckBox($objForm, "REMARK".$key, "1", $extra, "");
                $setData["RECORD_ALIGN"] = "center";
            }

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
        knjCreateHidden($objForm, "remark05_moji", $model->remark05_moji);
        knjCreateHidden($objForm, "remark05_gyou", $model->remark05_gyou);
        knjCreateHidden($objForm, "remark06_moji", $model->remark06_moji);
        knjCreateHidden($objForm, "remark06_gyou", $model->remark06_gyou);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //画面のリロード
        if ($model->cmd == "updEdit2") {
            $arg["reload"] = "parent.parent.left_frame.btn_submit('list');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd_hreportremark_d_2Form1.html", $arg);
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