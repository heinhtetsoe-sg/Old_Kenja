<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");
class knjh080eForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjh080eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjh080eQuery::getSemesterList();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ作成
        $query = knjh080eQuery::getGradeHrclass($model->field["SEMESTER"], $model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //チェックボックスALL
        $extra = "onClick=\"check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //初期化
        $model->data     = array();

        $arraycheckSch = array();
        if (isset($model->warning)) $arraycheckSch = explode(',', $model->checkSch);
        //一覧表示
        $result = $db->query(knjh080eQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //チェックボックス
            $extra  = "id=\"CHECKED{$row["SCHREGNO"]}\" onclick=\"chkClick(this)\" tabindex=\"-1\"";
            $extra .= (isset($model->warning) && in_array($row["SCHREGNO"], $arraycheckSch)) ? " checked" : "";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED".$row["SCHREGNO"], $row["SCHREGNO"], $extra, "");

            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if($row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            //担任報告
            $value = (!isset($model->warning)) ? $row["BASE_REMARK1"] : $model->field["BASE_REMARK1{$row["SCHREGNO"]}"];
            $extra = "onchange=\"checkSelf('".$row["SCHREGNO"]."');\"";
            $row["BASE_REMARK1"] = knjCreateTextArea($objForm, "BASE_REMARK1".$row["SCHREGNO"], "7", "43", "soft", $extra, $value);

            //背景色
            $row["COLOR"] = (isset($model->warning) && in_array($row["SCHREGNO"], $arraycheckSch)) ? "#ccffcc" : "#ffffff";

            $arg["data"][] = $row;
        }

        //ボタン作成
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
        //一括更新ボタンを作成する
        $link = REQUESTROOT."/H/KNJH080E/knjh080eindex.php?cmd=replace&SEMESTER=".$model->field["SEMESTER"]."&GRADE_HR_CLASS=".$model->field["GRADE_HR_CLASS"];
        $extra = ($model->field["SEMESTER"] && $model->field["GRADE_HR_CLASS"]) ? "onclick=\"Page_jumper('$link');\"" : "disabled";
        $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "checkSch");

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh080eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
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
?>
