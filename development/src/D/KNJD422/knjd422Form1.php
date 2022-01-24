<?php

require_once('for_php7.php');

class knjd422Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd422index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('gakki');\"";
        $query = knjd422Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $Row  = $db->getRow(knjd422Query::getRow($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row  =& $model->field;
        }

        //活動内容
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->totalstudytime_moji, $model->totalstudytime_gyou, $Row["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->totalstudytime_moji."文字X".$model->totalstudytime_gyou."行まで)";

        //評価
        $arg["data"]["REMARK01"] = getTextOrArea($objForm, "REMARK01", $model->remark01_moji, $model->remark01_gyou, $Row["REMARK01"], $model);
        $arg["data"]["REMARK01_COMMENT"] = "(全角".$model->remark01_moji."文字X".$model->remark01_gyou."行まで)";

        //特別活動の記録
        $arg["data"]["SPECIALACTREMARK"] = getTextOrArea($objForm, "SPECIALACTREMARK", $model->specialactremark_moji, $model->specialactremark_gyou, $Row["SPECIALACTREMARK"], $model);
        $arg["data"]["SPECIALACTREMARK_COMMENT"] = "(全角".$model->specialactremark_moji."文字X".$model->specialactremark_gyou."行まで)";

        //総合所見
        $arg["data"]["REMARK02"] = getTextOrArea($objForm, "REMARK02", $model->remark02_moji, $model->remark02_gyou, $Row["REMARK02"], $model);
        $arg["data"]["REMARK02_COMMENT"] = "(全角".$model->remark02_moji."文字X".$model->remark02_gyou."行まで)";

        //部活動参照ボタン
        $extra = "onclick=\"return btn_submit('subform2');\"";
        $arg["button"]["btn_club"] = KnjCreateBtn($objForm, "btn_club", "部活動参照", $extra);
        
        //資格参照ボタン
        $extra = "onclick=\"return btn_submit('subform3');\"";
        $arg["button"]["btn_committee"] = KnjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $Row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJD422");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd422Form1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    $defValue = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = $row["VALUE"];
        }
    }

    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "" && $defValue) ? $defValue : ($value ? $value : $opt[0]["value"]);
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
