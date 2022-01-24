<?php
class knjl389iForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl389iForm1", "POST", "knjl389iindex.php", "", "knjl389iForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボ
        $extra = " onchange=\"return btn_submit('knjl389i');\"";
        $query = knjl389iQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボ
        $extra = " onchange=\"return btn_submit('knjl389i');\"";
        $query = knjl389iQuery::getEntexamTestDivMst($model->ObjYear, $model->field["APPLICANTDIV"]);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //出力対象
        $extra = "";
        $query = knjl389iQuery::getEntexamTestSubcls($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["OUTPUT_SUBJECT"], "OUTPUT_SUBJECT", $extra, 1);

        //抽出区分ラジオボタン 3:全員 1:男子のみ 2:女子のみ
        $opt = array(1, 2, 3);
        if (!$model->field["SEX"]) $model->field["SEX"] = 3;
        $extra = array("id=\"SEX1\"", "id=\"SEX2\"", "id=\"SEX3\"");
        $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //並び順ラジオボタン 1:受験番号順 2:氏名カナ順 3:出身校コード順
        $opt = array(1, 2);
        if (!$model->field["ORDER"]) $model->field["ORDER"] = 1;
        $extra = array("id=\"ORDER1\"", "id=\"ORDER2\"");
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->field["ORDER"], $extra, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL389I");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl389iForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $alStr = $name == "OUTPUT_SUBJECT" ? "総合計" : "-- 全て --";
    if ($blank == "ALL") $opt[] = array("label" => $alStr, "value" => "ALL");
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

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
?>
