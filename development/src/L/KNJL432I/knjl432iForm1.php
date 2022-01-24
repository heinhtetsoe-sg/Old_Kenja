<?php
class knjl432iForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl432iindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = $model->examyear;

        //入試制度コンボボックス
        $extra = "onChange=\"return btn_submit('main')\"";
        $query = knjl432iQuery::getNameMst($model->examyear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "";
        $query = knjl432iQuery::getEntexamTestDivMst($model->examyear, $model->applicantdiv);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);
        
        //並び区分
        $opt = array(1, 2);
        $model->sortdiv = ($model->sortdiv == "") ? "1" : $model->sortdiv;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->sortdiv, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //抽出区分
        $opt = array(1, 2, 3);
        $model->outputdiv = ($model->outputdiv == "") ? "1" : $model->outputdiv;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->outputdiv, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }


        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl432iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJL432I");
}
