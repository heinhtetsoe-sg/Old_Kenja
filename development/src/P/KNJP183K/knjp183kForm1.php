<?php

require_once('for_php7.php');


class knjp183kForm1
{
    function main(&$model){

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //対象データ
        $opt[] = array('label' => "1:受給資格認定CSV", 'value' => "1");
        $opt[] = array('label' => "2:加算決定通知CSV", 'value' => "2");
        $extra = "";
        $arg["data"]["DATA_DIV"] = knjCreateCombo($objForm, "DATA_DIV", $model->field["DATA_DIV"], $opt, $extra, 1);

        //1:取込, 2:エラー出力
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"]   = $objForm->get_start("knjp183kForm1", "POST", "knjp183kindex.php", "", "knjp183kForm1");
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp183kForm1.html", $arg); 
    }

}
?>
