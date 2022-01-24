<?php

require_once('for_php7.php');

class knjd680Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd680Query::getSemester();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knjd680Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        $comment = "";
        //初期化
        $model->data = array();
        $counter = 0;
        $grade = array();
        //一覧表示
        $colorFlg = false;
        $query = knjd680Query::selectQuery($model);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["GRADE_HR_CLASS"][] = $row["GRADE"]."-".$row["HR_CLASS"];

            //学年ごとの連番取得
            $grade[$row["GRADE"]][] = $counter;

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            /*** テキストエリア ***/
            foreach ($model->getPro as $key => $val) {
                $model->data[$key."-".$counter] = $row[$key];

                if ($val["gyou"] == 1) {
                    $extra = "onPaste=\"return showPaste(this);\"";
                    $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];

                    $row[$key] = knjCreateTextBox($objForm, $value, $key."-".$counter, ($val["moji"] * 2), ($val["moji"] * 2), $extra);
                    $comment = "(全角{$val["moji"]}文字まで)";
                } else {
                    $height = $val["gyou"] * 13.5 + ($val["gyou"] - 1) * 3 + 5;
                    $extra = "style=\"height:{$height}px;\" onPaste=\"return showPaste(this);\"";
                    $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                    $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];

                    $row[$key] = KnjCreateTextArea($objForm, $key."-".$counter, $val["gyou"], ($val["moji"] * 2 + 1), "soft", $extra, $value);
                    $comment = "(全角{$val["moji"]}文字X{$val["gyou"]}行まで)";
                }
            }

            $arg["TITLE_COMMENT"] = $comment;

            //背景色
            //$row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            $row["COLOR"] = "#ffffff";

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $counter);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //貼付機能の文字数チェック用
        knjCreateHidden($objForm, "TITLE_moji", "20");
        knjCreateHidden($objForm, "TITLE_gyou", "1");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd680index.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd680Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $counter) {
    //更新ボタン
    $extra = (AUTHORITY == DEF_UPDATABLE) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
