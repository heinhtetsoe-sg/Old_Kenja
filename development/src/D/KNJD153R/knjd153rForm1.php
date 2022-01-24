<?php

require_once('for_php7.php');


class knjd153rForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd153rindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd153rQuery::getSemesterList();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ作成
        $query = knjd153rQuery::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd153rQuery::selectQuery($model));
        $arg["REMARK1_TYUI"] = "(全角{$model->getPro["REMARK1"]["moji"]}文字{$model->getPro["REMARK1"]["gyou"]}行まで)";
        $arg["REMARK2_TYUI"] = "(全角{$model->getPro["REMARK2"]["moji"]}文字{$model->getPro["REMARK2"]["gyou"]}行まで)";

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if ($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //担任所見
            $value = (!isset($model->warning)) ? $row["REMARK1"] : $model->fields["REMARK1"][$counter];
            $height = $model->getPro["REMARK1"]["gyou"] * 13.5 + ($model->getPro["REMARK1"]["gyou"] -1 ) * 3 + 5;
            $row["REMARK1"] = KnjCreateTextArea($objForm, "REMARK1-".$counter, $model->getPro["REMARK1"]["gyou"], ($model->getPro["REMARK1"]["moji"] * 2 + 1), "soft", "style=\"height:{$height}px;\" onPaste=\"return showPaste(this);\"", $value);

            //その他
            $value = (!isset($model->warning)) ? $row["REMARK2"] : $model->fields["REMARK2"][$counter];
            $height = $model->getPro["REMARK2"]["gyou"] * 13.5 + ($model->getPro["REMARK2"]["gyou"] -1 ) * 3 + 5;
            $row["REMARK2"] = KnjCreateTextArea($objForm, "REMARK2-".$counter, $model->getPro["REMARK2"]["gyou"], ($model->getPro["REMARK2"]["moji"] * 2 + 1), "soft", "style=\"height:{$height}px;\" onPaste=\"return showPaste(this);\"", $value);

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "remark1_gyou", $model->getPro["REMARK1"]["gyou"]);
        knjCreateHidden($objForm, "remark1_moji", $model->getPro["REMARK1"]["moji"]);
        knjCreateHidden($objForm, "remark2_gyou", $model->getPro["REMARK2"]["gyou"]);
        knjCreateHidden($objForm, "remark2_moji", $model->getPro["REMARK2"]["moji"]);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd153rForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //データCSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX155CSV/knjx155csvindex.php?','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "データCSV", $extra);
}
?>
