<?php
class knjl321aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl321aForm1", "POST", "knjl321aindex.php", "", "knjl321aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //受験校種コンボ
        $extra = " onchange=\"return btn_submit('knjl321a');\"";
        $query = knjl321aQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //表示切替
        if ($model->field["APPLICANTDIV"] == "2") {
            $arg["isH"] = 1;
        }

        //試験回コンボ
        $extra = "";
        $query = knjl321aQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //専併区分コンボ
        $extra = "";
        $query = knjl321aQuery::getNameMst($model->ObjYear, "L006");
        makeCmb($objForm, $arg, $db, $query, $model->field["SHDIV"], "SHDIV", $extra, 1, "ALL");

        //志望コースコンボ
        $extra = "";
        $query = knjl321aQuery::getNameMst($model->ObjYear, ($model->field["APPLICANTDIV"] == "1") ? "LJ58" : "LH58");
        makeCmb($objForm, $arg, $db, $query, $model->field["EXAMCOURSE"], "EXAMCOURSE", $extra, 1, "ALL");

        //出力順ラジオボタン 1:成績順 2:受験番号順
        $opt = array(1, 2);
        if (!$model->field["SORT"]) {
            $model->field["SORT"] = 1;
        }
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //志願者情報有無チェック
        $extra = " id=\"APPINFO_FLG\" ";
        $checked = $model->field["APPINFO_FLG"] == '1' ? " checked " : "";
        $arg["data"]["APPINFO_FLG"] = knjCreateCheckBox($objForm, "APPINFO_FLG", "1", $checked.$extra);

        //CSVボタン
        $extra = "onclick=\"btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL321A");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl321aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
