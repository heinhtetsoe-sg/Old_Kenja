<?php

require_once('for_php7.php');

class knjm240mForm2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjm240mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (isset($model->warning) || !$model->chaircd) {
            $row = $model->field;
        } else {
            //SQL文発行
            $query = knjm240mQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        $arg["data"] = $row;
        if (is_array($row)) {
            $model->AddorUp = "up";
        } else {
            $model->AddorUp = "add";
        }
        $arg["NAME"]            = $model->subclass_show;
        $arg["CHAIRCD"]         = $model->chaircd;
        $arg["SUBCLASSCD"]      = $model->subclasscd;
        $arg["data"]["KAMOKU"]  = $model->subclass_show;

        //回数
        if (isset($model->warning) || !$model->chaircd) {
            $row["REP_SEQ_ALL"] = $row["SCHCNT"];
        }
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);check(this)\"";
        $arg["data"]["SCHCNT"] = knjCreateTextBox($objForm, $row["REP_SEQ_ALL"], "SCHCNT", 2, 2, $extra);

        //提出日延長限度回数
        if (isset($model->warning) || !$model->chaircd) {
            $row["REP_LIMIT"] = $row["CHECKCNT"];
        }
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);check(this)\"";
        $arg["data"]["CHECKCNT"] = knjCreateTextBox($objForm, $row["REP_LIMIT"], "CHECKCNT", 2, 2, $extra);

        //開始回数
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["REP_START_SEQ"] = knjCreateTextBox($objForm, $row["REP_START_SEQ"], "REP_START_SEQ", 2, 2, $extra);

        $arg["SUBCLASSCD"]  = $model->subclasscd;

        //登録ボタン
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", $extra);

        //取消ボタン
        $extra = " onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "reset" && !isset($model->warning)) {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm240mForm2.html", $arg);
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
