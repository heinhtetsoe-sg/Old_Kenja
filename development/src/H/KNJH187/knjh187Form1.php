<?php

require_once('for_php7.php');

class knjh187Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh187Form1", "POST", "knjh187index.php", "", "knjh187Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //対象幼稚園コンボ
        $query = knjh187Query::getKindergarten();
        $extra = "onchange=\"return btn_submit('knjh187');\"";
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->field["COURSECODE"], $extra, 1, "ALL");

        //年組コンボ
        $query = knjh187Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('knjh187');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "ALL");

        //初期化
        $model->data = array();
        $counter = 0;
        //一覧表示
        $colorFlg = false;
        $query = knjh187Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //バスコース
            $query = knjh187Query::getBusCourse();
            $value = (!isset($model->warning)) ? $row["ROSEN_2"] : $model->fields["ROSEN_2"][$counter];
            $extra = "";
            $row["ROSEN_2"] = makeCmb2($objForm, $arg, $db, $query, "ROSEN_2-".$counter, $value, $extra, 1, "BLANK");

            //乗車名
            $model->data["JOSYA_2"."-".$counter] = $row["JOSYA_2"];
            $value = (!isset($model->warning)) ? $row["JOSYA_2"] : $model->fields["JOSYA_2"][$counter];
            $extra = "onPaste=\"return showPaste(this);\"";
            $row["JOSYA_2"] = knjCreateTextBox($objForm, $value, "JOSYA_2-".$counter, 30, 30, $extra);

            //降車名
            $model->data["GESYA_2"."-".$counter] = $row["GESYA_2"];
            $value = (!isset($model->warning)) ? $row["GESYA_2"] : $model->fields["GESYA_2"][$counter];
            $extra = "onPaste=\"return showPaste(this);\"";
            $row["GESYA_2"] = knjCreateTextBox($objForm, $value, "GESYA_2-".$counter, 30, 30, $extra);

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh187Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    if ($blank == "ALL")   $opt[] = array('label' => "-- 全て --", 'value' => "ALL");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    if ($blank == "ALL")   $opt[] = array('label' => "-- 全て --", 'value' => "ALL");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
