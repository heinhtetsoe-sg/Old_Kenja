<?php

require_once('for_php7.php');

class knjp850form2
{
    public function main(&$model)
    {
        $objForm = new form();
        //DB接続
        $db = Query::dbCheckOut();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp850index.php", "", "edit");

        if (!isset($model->warning)) {
            $query = knjp850Query::getRowQuery($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //対応日付
        $Row["CONTACT_SDATE"] = $Row["CONTACT_SDATE"] ? $Row["CONTACT_SDATE"] : CTRL_DATE;
        $date_ymd = strtr($Row["CONTACT_SDATE"], "-", "/");
        $arg["data"]["CONTACT_SDATE"] = View::popUpCalendar($objForm, "CONTACT_SDATE", $date_ymd);

        //通番
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SEQ"] = knjCreateTextBox($objForm, $Row["SEQ"], "SEQ", 3, 3, $extra);

        //カテゴリ
        $extra = "";
        $query = knjp850Query::getNameMst($model, "P010");
        $arg["data"]["CONTACT_DIV"] = makeCmb($objForm, $arg, $db, $query, $Row["CONTACT_DIV"], "CONTACT_DIV", $extra, "1");
        
        //記録者
        $extra = "";
        if ($Row["STAFFCD"] == "") {
            $Row["STAFFCD"] = STAFFCD;
        }
        $query = knjp850Query::getStaffMst($model, $Row["STAFFCD"]);
        $arg["data"]["STAFFCD"] = makeCmb($objForm, $arg, $db, $query, $Row["STAFFCD"], "STAFFCD", $extra, "1");

        //記録内容
        $moji = $model->contactRemark_moji;
        $gyou = $model->contactRemark_gyou;
        $extra = "id=\"CONTACT_REMARK\"";
        $arg["data"]["CONTACT_REMARK"] = getTextOrArea($objForm, "CONTACT_REMARK", $moji, $gyou, $Row["CONTACT_REMARK"], $model, $extra);
        $arg["data"]["CONTACT_REMARK_COMMENT"] = getTextAreaComment($moji, $gyou);

        //対応完了日
        $date_ymd = strtr($Row["CONTACT_EDATE"], "-", "/");
        $arg["data"]["CONTACT_EDATE"] = View::popUpCalendar($objForm, "CONTACT_EDATE", $date_ymd);
        
        /**********/
        /* ラジオ */
        /**********/
        //設定区分
        $opt = array(1, 2, 3); //1:国家資格 2:公的資格 3:民間資格
        $Row["CONDITION_DIV"] = ($Row["CONDITION_DIV"] == "") ? "1" : $Row["CONDITION_DIV"];
        $extra = array("id=\"CONDITION_DIV1\" onClick=\"btn_submit('conditionDiv')\"", "id=\"CONDITION_DIV2\" onClick=\"btn_submit('conditionDiv')\"", "id=\"CONDITION_DIV3\" onClick=\"btn_submit('conditionDiv')\"");
        $radioArray = knjCreateRadio($objForm, "CONDITION_DIV", $Row["CONDITION_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /******************/
        /* コンボボックス */
        /******************/

        /********************/
        /* チェックボックス */
        /********************/
        //資格証書
        $extra = 'id="CERTIFICATE" '.(($Row["CERTIFICATE"] == "1") ? "checked" : "");
        $arg["data"]["CERTIFICATE"]  = knjCreateCheckBox($objForm, "CERTIFICATE", "1", $extra, "");
        /********************/
        /* テキストボックス */
        /********************/
        //得点
        $extra = "";
        $arg["data"]["HOBBY_SCORE"] = knjCreateTextBox($objForm, $Row["HOBBY_SCORE"], "HOBBY_SCORE", 3, 3, $extra);
        //資格内容
        $extra = "";
        $arg["data"]["CONTENTS"] = $Row["CONTENTS"];
        //備考
        $extra = "".$remarkDisabled;
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 30, $extra);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return Btn_reset('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //ＣＳＶ処理
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);
        
        //CSV一括処理
        $extra = "onclick=\"return btn_submit('csv2');\"";
        $arg["button"]["btn_csv2"] = knjCreateBtn($objForm, "btn_csv2", "ＣＳＶ一括処理", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "IKKATU_LIST"); //一括CSVの対象生徒

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjp850index.php?cmd=list&SCHREGNO={$model->schregno}','right_frame');";
        }

        View::toHTML5($model, "knjp850Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $all = "")
{
    $opt = array();
    if ($all == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "{$all}");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $ext_outstyle)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" ".$ext_outstyle;
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" ".$ext_outstyle;
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

function getTextAreaComment($moji, $gyo)
{
    $comment = "";

    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
