<?php

require_once('for_php7.php');

class knjd131wForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd131windex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->cmd != "edit2" && (isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd131wQuery::getTrainRow($model->schregno, $model),DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //extra
        $extra_commu = "style=\"height:145px;\"";
        $extra_spe   = "style=\"height:145px;\"";

        //特別活動の記録
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $row["REMARK1"], $model);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->getPro["REMARK1"]["moji"]."文字X".$model->getPro["REMARK1"]["gyou"]."行まで)";

        //学期コンボ
        $query = knjd131wQuery::getSemester();
        $extra = "onChange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["DETAIL_SEME"], "DETAIL_SEME", $extra, 1, "");

        $query = knjd131wQuery::getHreportDetailDat($model->schregno, $model);
        $result = $db->query($query);
        $detailData = array();
        $setHanei = array();
        while ($rowHanei = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $detailData[$rowHanei["SEMESTER"]][$rowHanei["CODE"]] = $rowHanei;
            if ($rowHanei["SEMESTER"] != "9") {
                $setHanei[$rowHanei["CODE"]] .= $setHanei[$rowHanei["CODE"]] ? "、".$rowHanei["REMARK1"] : $rowHanei["REMARK1"];
            }
        }
        $result->free();

        $query = knjd131wQuery::getAttendSemes($model->schregno, $model->field["DETAIL_SEME"]);
        $semeAttend = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $query = knjd131wQuery::getAttendSemes($model->schregno, "");
        $semeAttendAll = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $model->koumoku = array("01" => "忌引数",
                                "02" => "出席停止数",
                                "03" => "欠席数",
                                "04" => "遅刻数",
                                "05" => "早退数",
                                );
        foreach ($model->koumoku as $key => $val) {
            $extra = "";
            $setVal["DETAIL_CODE"] = $val;

            $setVal["DETAIL_ATTEND"] = $semeAttend["ATTEND{$key}"]."&nbsp;";
            $extra = $detailData[$model->field["DETAIL_SEME"]][$key]["REMARK2"] == "1" ? " checked " : "";
            $setVal["DETAIL_CHECK"] = knjCreateCheckBox($objForm, "DETAIL_CHECK{$key}", "1", $extra);
            $extra = "";
            $setRemark1 = $detailData[$model->field["DETAIL_SEME"]][$key]["REMARK1"];
            if (isset($model->warning)) {
                $setRemark1 = $model->detailField["DETAIL_REMARK1{$key}"];
            }
            $setVal["DETAIL_REMARK1"] = knjCreateTextBox($objForm, $setRemark1, "DETAIL_REMARK1{$key}", 30, 30, $extra);

            $setVal["DETAIL_ATTEND_ALL"] = $semeAttendAll["ATTEND{$key}"]."&nbsp;";
            $extra = $detailData["9"][$key]["REMARK2"] == "1" ? " checked " : "";
            $setVal["DETAIL_CHECK_ALL"] = knjCreateCheckBox($objForm, "DETAIL_CHECK_ALL{$key}", "1", $extra);
            $extra = "";
            $setAllRemark = $detailData["9"][$key]["REMARK1"];
            if ($model->cmd == "hanei") {
                $extra = $setAllRemark ? "" : "style=\"background-color:hotpink;\"";
                $setAllRemark = $setAllRemark ? $setAllRemark : $setHanei[$key];
            } else if (isset($model->warning)) {
                $setAllRemark = $model->detailField["DETAIL_REMARK1_ALL{$key}"];
            }
            $setVal["DETAIL_REMARK1_ALL"] = knjCreateTextBox($objForm, $setAllRemark, "DETAIL_REMARK1_ALL{$key}", 30, 30, $extra);
            $arg["data2"][] = $setVal;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjd131wForm1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SCHREGNOS");
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {

    //反映
    $extra = " onclick=\"return btn_submit('hanei');\"";
    $arg["button"]["HANEI"] = knjCreateBtn($objForm, "HANEI", "反 映", $extra);

    //更新
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //出欠備考参照
    $extra = "onclick=\"return btn_submit('reference_first');\"";
    $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_refernce", "出欠備考参照", $extra);

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
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}


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
        $extra = "style=\"height:".$height."px;\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

?>
