<?php

require_once('for_php7.php');

class knjd132cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //学期コンボ
        $query = knjd132cQuery::getSemester($model);
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        if ($model->Properties["knjdBehaviorsd_UseText"] == "1") {
            //出力項目取得
            $setTextTitle = $sep = "";
            $setCheckVal = "";
            $checkSep = "/";
            $query = knjd132cQuery::getNameMst($model, "D036");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setCheckVal .= $checkSep.$row["NAME1"];
                $checkSep = "|";
                $setTextTitle .= $sep.$row["NAME1"].":".$row["NAME2"];
                $sep = "　";

                $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$row["NAME1"]."')\"",
                                     "NAME" => $row["NAME1"].":".$row["NAME2"]);
            }
            $setCheckVal .= "/";
            $arg["TEXT_TITLE"] = $setTextTitle ? "(".$setTextTitle.")" : "";
            knjCreateHidden($objForm, "CHECK_VAL", $setCheckVal);
            knjCreateHidden($objForm, "CHECK_ERR_MSG", $setTextTitle);

            //ドロップダウンリスト
            $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
            $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
            if (is_array($dataArray)) {
                foreach ($dataArray as $key => $val) {
                    $setData["CLICK_NAME"] = $val["NAME"];
                    $setData["CLICK_VAL"] = $val["VAL"];
                    $arg["menu"][] = $setData;
                }
            }

            $result->free();
        }

        //観点名称取得
        $maxlen = 0;
        $model->itemArray = array();
        $query = knjd132cQuery::getBehaviorSemesMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArray[$row["VALUE"]] = $row["LABEL"];
            //MAX文字数
            if ($maxlen < mb_strwidth($row["LABEL"])) $maxlen = mb_strwidth($row["LABEL"]);
        }
        $result->free();

        //サイズ
        $width = ($maxlen * 8 < 400) ? 400 : $maxlen * 8;
        $arg["RECORD_LABEL_WIDTH"] = $width;
        $arg["RECORD_VALUE_WIDTH"] = 150;
        $arg["MAIN_WIDTH"] = $width + 150;

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        } else {
            //記録データの取得
            $result = $db->query(knjd132cQuery::getBehaviorDat($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["RECORD"][$row["CODE"]] = $row["RECORD"];
            }
            $result->free();
            //所見取得
            $row = $db->getRow(knjd132cQuery::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);
        }

        if (is_array($model->itemArray)) {
            $setTextField = "";
            $setTextFieldComma = "";
            foreach ($model->itemArray as $key => $val) {
                $setTextField .= $setTextFieldComma."RECORD".$key;
                $setTextFieldComma = ",";
            }
            foreach ($model->itemArray as $key => $val) {
                $setData = array();
                //データ
                if ($model->Properties["knjdBehaviorsd_UseText"] == "1") {
                    //項目名
                    $setData["RECORD_LABEL"] = $val;
                    //テキスト
                    $extra = "STYLE=\"text-align: center;\" onblur=\"calc(this);\" oncontextmenu=\"kirikae2(this, '".$key."');\" onkeydown=\"keyChangeEntToTab2(this, '".$setTextField."');\" ";
                    $setData["RECORD_VALUE"] = knjCreateTextBox($objForm, $Row["RECORD"][$key], "RECORD".$key, 3, 1, $extra);
                } else {
                    $id = "RECORD".$key;
                    //項目名
                    $setData["RECORD_LABEL"] = "<LABEL for={$id}>".$val."</LABEL>";
                    //チェックボックス
                    $check1 = ($Row["RECORD"][$key] == "1") ? "checked" : "";
                    $extra = $check1." id={$id}";
                    $setData["RECORD_VALUE"] = knjCreateCheckBox($objForm, "RECORD".$key, "1", $extra, "");
                }
                $arg["data"][] = $setData;
            }
        }

        //道徳
        if ($model->Properties["un_DoutokuHyouji_KNJD132C"] != "1") {
            $arg["data1"]["MORAL"] = getTextOrArea($objForm, "MORAL", $model->getPro["MORAL"]["moji"], $model->getPro["MORAL"]["gyou"], $row["REMARK2"], $model);
            setInputChkHidden($objForm, "MORAL", $model->getPro["MORAL"]["moji"], $model->getPro["MORAL"]["gyou"], $arg);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model);

        //hidden
        makeHidden($objForm, $model);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent2(0);";
        } else if (get_count($model->warning) != 0 || $model->cmd == "reset") {
            $arg["next"] = "NextStudent2(1);";
        }
        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd132cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array("label" => "", "value" => "");
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $db, $model) {
    //更新ボタン
    $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update')\"" : "disabled";
    $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//入力チェック設定
function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    $arg["data1"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
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

//テキストボックス or テキストエリア作成
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
        $extra = "style=\"height:".$height."px;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

?>
