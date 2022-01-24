<?php

require_once('for_php7.php');

class knje370iForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje370iForm1", "POST", "knje370iindex.php", "", "knje370iForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //出力対象ラジオ 1:合格者のみ 2:全て
        $opt = array(1, 2);
        $model->field["TARGET_DIV"] = ($model->field["TARGET_DIV"] == "") ? "2" : $model->field["TARGET_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TARGET_DIV{$val}\"");
        }
        $targetDivValue = ($model->field["TARGET_DIV"] == "") ? 2 : $model->field["TARGET_DIV"];
        $radioArray = knjCreateRadio($objForm, "TARGET_DIV", $targetDivValue, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje370iForm1.html", $arg); 
    }
}
?>
