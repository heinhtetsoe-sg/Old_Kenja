<?php

require_once('for_php7.php');

class knjm704dForm2
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm704dindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjm704dQuery::getRow($model->year, $model->specialcd);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //年度
        $extra = "";
        $arg["data"]["YEAR"] = knjCreateTextBox($objForm, $Row["YEAR"], "YEAR", 4, 4, $extra);

        //管理番号
        $extra = "";
        $arg["data"]["SPECIALCD"] = knjCreateTextBox($objForm, $Row["SPECIALCD"], "SPECIALCD", 3, 3, $extra);

        //日付
        //開始日付
        $arg["data"]["SPECIAL_SDATE"] = View::popUpCalendar($objForm, "SPECIAL_SDATE", str_replace("-", "/", $Row["SPECIAL_SDATE"]));
        //終了日付
        $arg["data"]["SPECIAL_EDATE"] = View::popUpCalendar($objForm, "SPECIAL_EDATE", str_replace("-", "/", $Row["SPECIAL_EDATE"]));

        //特別活動内容
        $extra = "";
        $arg["data"]["SPECIALACTIVITYNAME"] = knjCreateTextBox($objForm, $Row["SPECIALACTIVITYNAME"], "SPECIALACTIVITYNAME", 60, 90, $extra);

        //活動時間数
        $extra = "";
        $arg["data"]["SPECIALACTIVITYTIME"] = knjCreateTextBox($objForm, $Row["SPECIALACTIVITYTIME"], "SPECIALACTIVITYTIME", 3, 3, $extra);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjm704dindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm704dForm2.html", $arg);
    }
}

//CSV作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
