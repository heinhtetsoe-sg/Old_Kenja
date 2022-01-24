<?php

require_once('for_php7.php');

class knjd139sIkkatsu {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjd139sindex.php", "", "sel");

        $arg["jscript"] = "";

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["PRJ1_TITLE"] = $model->prjTitle;

        //学期コンボ
        $query = knjd139sQuery::getSemester($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->ikkatsuSeme, "IKKATU_SEMESTER", $extra, 1, "");

        for($cnt = 1; $cnt <= 3; $cnt++){
            $code = "0{$cnt}";
            $query = knjd139sQuery::getHreportremarkDetailDatIkkatsu($model, "9", $div, $code);
            $setwk = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["REMARK{$div}_{$code}"] = $setwk["REMARK1"];
        }
        $div = "02";
        for($cnt = 2; $cnt <= 3; $cnt++){
            $code = "0{$cnt}";
            $query = knjd139sQuery::getHreportremarkDetailDatIkkatsu($model, ($cnt == 2 ? "9" : $model->ikkatsuSeme), $div, $code);
            $setwk = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["REMARK{$div}_{$code}"] = $setwk["REMARK1"];
        }

        //チェックボックス
        for ($i = 0; $i < 5; $i++) {
            $name   = "CHECK1_".$i;
            $value  = "1";
            $extra  = "";
            $cheked = ($model->ikkatsu_data["check1"][$i] == "1") ? " checked" : "";
            $extra  = "id=\"CHECK1_{$i}\"".$cheked;
            if ($i == 0) {
                $name   = "CHECK_ALL1";
                $value  = "ALL";
                $chkAll = ($model->ikkatsu_data["check1"][$i] == "ALL") ? " checked" : "";
                $extra  = "id=\"CHECK_ALL1\" onClick=\"return check_all(this, '1');\"".$chkAll;
            }
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, $value, $extra);
        }

        for ($i = 0; $i < 2; $i++) {
            $name   = "CHECK2_".$i;
            $value  = "1";
            $extra  = "";
            $cheked = ($model->ikkatsu_data["check2"][$i] == "1") ? " checked" : "";
            $extra  = "id=\"CHECK2_{$i}\"".$cheked;
            if ($i == 0) {
                $name   = "CHECK_ALL2";
                $value  = "ALL";
                $chkAll = ($model->ikkatsu_data["check2"][$i] == "ALL") ? " checked" : "";
                $extra  = "id=\"CHECK_ALL2\" onClick=\"return check_all(this, '2');\"".$chkAll;
            }
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, $value, $extra);
        }

        //対象HRクラス取得
        $model->hr_class = $db->getOne(knjd139sQuery::getHR_Class($model));


        //テキストボックス
        //通年データ
        $moji = 25;
        $gyou = 1;
        $arg["data"]["REMARK01_01"] = getTextOrArea($objForm, "REMARK01_01", $moji, $gyou, $Row["REMARK01_01"], $model);
        setInputChkHidden($objForm, "REMARK01_01", $moji, $gyou);
        $arg["data"]["REMARK01_01_COMMENT"] = getTextAreaComment($moji, $gyou);

        $moji = 10;
        $gyou = 1;
        $arg["data"]["REMARK01_02"] = getTextOrArea($objForm, "REMARK01_02", $moji, $gyou, $Row["REMARK01_02"], $model);
        setInputChkHidden($objForm, "REMARK01_02", $moji, $gyou);
        $arg["data"]["REMARK01_02_COMMENT"] = getTextAreaComment($moji, $gyou);

        $moji = 10;
        $gyou = 1;
        $arg["data"]["REMARK01_03"] = getTextOrArea($objForm, "REMARK01_03", $moji, $gyou, $Row["REMARK01_03"], $model);
        setInputChkHidden($objForm, "REMARK01_03", $moji, $gyou);
        $arg["data"]["REMARK01_03_COMMENT"] = getTextAreaComment($moji, $gyou);

        $moji = 25;
        $gyou = 1;
        $arg["data"]["REMARK02_02"] = getTextOrArea($objForm, "REMARK02_02", $moji, $gyou, $Row["REMARK02_02"], $model);
        setInputChkHidden($objForm, "REMARK02_02", $moji, $gyou);
        $arg["data"]["REMARK02_02_COMMENT"] = getTextAreaComment($moji, $gyou);

        //各学期データ
        $moji = 10;
        $gyou = 1;
        //内容
        $extra = " id=\"REMARK02_03\" onkeyup=\"charCount(this.value, {$gyou}, ({$moji} * 2), true);\"";
        $arg["data"]["REMARK02_03"] = knjCreateTextArea($objForm, "REMARK02_03", $gyou, ($moji * 2), "soft", $extra, $Row["REMARK02_03"]);
        $arg["data"]["REMARK02_03_COMMENT"] = "(全角".$moji."文字X".$gyou."行まで)";
        setInputChkHidden($objForm, "REMARK02_03", $moji, $gyou);

        //ボタン作成
        //更新ボタン
        $extra = "onclick=\"return doSubmit('ikkatsu_update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $link = REQUESTROOT."/D/KNJD139S/knjd139sindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //リスト作成
        //生徒一覧
        $opt_left = $opt_right = array();
        $array = explode(",", $model->ikkatsu_data["selectdata"]);
        //リストが空であれば置換処理選択時の生徒を加える
        if ($array[0]=="") $array[0] = $model->schregno;

        //生徒情報
        $result = $db->query(knjd139sQuery::GetStudent($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["SCHREGNO"], $array)){
                $opt_right[] = array("label" => $row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"],
                                     "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[]  = array("label" => $row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"],
                                     "value" => $row["SCHREGNO"]);
            }
        }

        $result->free();

        //対象生徒
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, 20);

        //その他の生徒
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "right", $opt_right, $extra, 20);

        //全追加
        $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //追加
        $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //削除
        $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //全削除
        $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        $hr_name = $semeName = "";
        //年組名
        $hr_name = $db->getOne(knjd139sQuery::getHR_Name($model));
        //学期名
        $semeName = $db->getOne(knjd139sQuery::getSemeName($model->ikkatsuSeme));
        $arg["info"] = array("LEFTTOP"    =>  CTRL_YEAR."年度 ".$semeName,
                             "RIGHTTOP"   =>  sprintf(" 対象クラス  %s", $hr_name),
                             "LEFT_LIST"  => "対象者一覧",
                             "RIGHT_LIST" => "生徒一覧");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SCHREGNO", $model->ikkatsu_data["selectdata"]);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd139sIkkatsu.html", $arg);
    }

}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo) {
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea".$setHiddenStr);
}

//コンボボックス作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($name == "IKKATU_SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
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
        $extra = "id=\"".$name."\" style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
