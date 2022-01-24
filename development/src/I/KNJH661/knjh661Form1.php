<?php

require_once('for_php7.php');

class knjh661Form1
{
    function main(&$model)
    {
        $objForm = new form;

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"] = CTRL_YEAR ."年度";

        //処理名コンボボックス
        $opt = array();
        $opt[] = array("label" => "更新", "value" => "1");
        $model->field["SHORI_MEI"] = ($model->field["SHORI_MEI"] == "") ? "1" : $model->field["SHORI_MEI"];
        $extra = "";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt, $extra, 1);

        //ヘッダ有チェックボックス
        $extra  = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //出力取込種別ラジオボタン  1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjh661index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh661Form1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg) {
    //実行
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
