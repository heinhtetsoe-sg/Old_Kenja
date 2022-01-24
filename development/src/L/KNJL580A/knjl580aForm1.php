<?php

require_once('for_php7.php');

class knjl580aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $arg["TOP"]["TITLE"] = "合格者・不合格者一覧";
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //合格コース
        $query = knjl580aQuery::getPassCourseCd($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PASS_COURSE", $model->passCourse, $extra, 1, "SELALL");

        //表示順 (1:受験番号順 2:名前順)
        $opt = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl580aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl580aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $default_flg = true;
    $value_flg = false;
    $force_setflg = false;
    $i = $default = 0;
    $selallval = "99999";    //引数を増やすとなぜか動作不安定となるようなので、固定値を設定

    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    } else if ($blank == "SELALL") {
        //リストに全てを設定
        $opt[] = array("label" => "全て", "value" => $selallval);
        //先に"全て"が設定されていたら、下記のSQLデータ取得ループでは"見つかったもの"としてフラグ設定する
        $value_flg = ($value == $selallval ? true : $value_flg);
        //$default_flgも不要。
        $default_flg = ($value_flg ? false : $default_flg);
        //先に"全て"が選択されていたら、SQLには"全て"が無いので、強制フラグをセットする。
        $force_setflg = ($value_flg ? true : false);
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }

    $result->free();
    $value = ($force_setflg || (!$force_setflg && $value && $value_flg)) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL580A");
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
}
?>
