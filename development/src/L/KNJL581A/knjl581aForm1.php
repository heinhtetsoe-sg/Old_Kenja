<?php

require_once('for_php7.php');

class knjl581aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $arg["TOP"]["TITLE"] = "出身中学校別試験結果通知出力";
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //通知出力 (1:全て 2:学校指定)
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onchange=submit('main');", "id=\"OUTPUT2\" onchange=submit('main');");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //入試区分
        //コンボ
        $opt = array();
        $default = "";
        $query = knjl581aQuery::getTestdiv($model->ObjYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"=>$row["LABEL"],
                           "value"=>$row["VALUE"]);
            if ($row["DEFAULT"] == "1") $default = $row["VALUE"];
        }
        if ($model->field["TESTDIV"] == "" && $default != "") $model->field["TESTDIV"] = $default;
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, "1");

        //通知日付
        $model->field["SET_DATE"] = $model->field["SET_DATE"] == "" ? CTRL_DATE : $model->field["SET_DATE"];
        $setdate = str_replace("-","/",$model->field["SET_DATE"]);
        $arg["data"]["SET_DATE"] = View::popUpCalendar($objForm, "SET_DATE", $setdate);

        //実力試験の実施日付
        $model->field["TEST_DATE"] = $model->field["TEST_DATE"] == "" ? CTRL_DATE : $model->field["TEST_DATE"];
        $setdate = str_replace("-","/",$model->field["TEST_DATE"]);
        $arg["data"]["TEST_DATE"] = View::popUpCalendar($objForm, "TEST_DATE", $setdate);

        $disFlg = "";
        if ($model->field["OUTPUT"] == "1") {
            $disFlg = " disabled";
        }
        //検索ボタン
        $extra = "style=\"width:70px\"".$disFlg." onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_ENTEXAM_FINSCHOOL/knjwexam_fin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=&entexamyear={$model->ObjYear}', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["btn_search_fs"] = knjCreateBtn($objForm, "btn_search_fs", "検 索", $extra);
        
        //指定学校名称
        $finschoolname = "";
        if ($model->field["FS_CD"]) {
            $finschoolname = $db->getOne(knjl581aQuery::getFinschoolName($model->field["FS_CD"]));
            $arg["data"]["FINSCHOOLNAME"] = $finschoolname;
        }

        //学校コード
        $extra = "onchange=\"submit('main')\";".$disFlg;
        $arg["data"]["FS_CD"] = knjCreateTextBox($objForm, $model->field["FS_CD"], "FS_CD", 4, 4, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl581aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl581aForm1.html", $arg);
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
    knjCreateHidden($objForm, "PRGID", "KNJL581A");
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
}
?>
