<?php

require_once('for_php7.php');


class knjl318rForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl318rForm1", "POST", "knjl318rindex.php", "", "knjl318rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = " onChange=\"return btn_submit('knjl318r');\"";

        //入試制度
        $query = knjl318rQuery::getNameMst($model, $model->ObjYear, "L003"); //中学のみ
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], "", 1, "");

        //入試区分コンボボックス
        $model->field["TESTDIV"] = "";
        $query = knjl318rQuery::getNameMst($model, $model->ObjYear, "L024");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

        //内諾
        $model->field["CONSENT"] = "";
        $query = knjl318rQuery::getNameMst($model, $model->ObjYear, "L064");
        $ext = "onclick=\"checkConsent(this)\"";
        makeCmb($objForm, $arg, $db, $query, "CONSENT", $model->field["CONSENT"], $ext, 1);

        //傾斜配点出力
        $opt = array(1, 2);
        $model->field["OUTKEISYA"] = ($model->field["OUTKEISYA"] == "") ? "2" : $model->field["OUTKEISYA"];
        $extra = array("id=\"OUTKEISYA1\"", "id=\"OUTKEISYA2\"");
        $radioArray = knjCreateRadio($objForm, "OUTKEISYA", $model->field["OUTKEISYA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ラジオボタンを作成 1:受験者対象 2:合格者対象
        $opt = array(1, 2);
        if (!$model->field["TARGET"]) $model->field["TARGET"] = "2";
        $onclick = "onclick =\" return btn_submit('knj');\"";
        $extra = array("id=\"TARGET1\" ","id=\"TARGET2\" ");
        $radioArray = knjCreateRadio($objForm, "TARGET", $model->field["TARGET"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //加算点含む
        $extra = ($model->field["INC_KASAN"] == "on" && $model->cmd != "") ? "checked" : "";
        $extra .= " id=\"INC_KASAN\"";
        $arg["data"]["INC_KASAN"] = knjCreateCheckBox($objForm, "INC_KASAN", "on", $extra, "");

        //内諾別改頁
        $extra = $model->field["NAIDAKU_PAGE"] == "1" ? "checked" : "";
        $extra .= " id=\"NAIDAKU_PAGE\"";
        if ($model->field["CONSENT"] != "ALL") {
            $extra .= " disabled=\"disabled\"";
        }
        $arg["data"]["NAIDAKU_PAGE"] = knjCreateCheckBox($objForm, "NAIDAKU_PAGE", "on", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl318rForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    if ($name == "CONSENT") $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
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
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL318R");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
