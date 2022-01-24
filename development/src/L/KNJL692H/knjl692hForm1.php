<?php
class knjl692hForm1
{
    public function main(&$model)
    {
        define("LINE_MAX", 12); // １頁に表示する最大人数

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl692hForm1", "POST", "knjl692hindex.php", "", "knjl692hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試日程コンボ
        $extra = "";
        $query = knjl692hQuery::getEntexamTestDivMst($model->ObjYear, "2");
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //類別
        $query = knjl692hQuery::getEntexamClassifyMst($model->ObjYear, "2");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV1"], "TESTDIV1", $extra, 1, "ALL");

        //受験番号（開始）
        $extra = " onblur=\"this.value=toInteger(this.value);checkReceptRange();\"";
        $arg["data"]["RECEPTNO_START"] = knjCreateTextBox($objForm, $model->field["RECEPTNO_START"], "RECEPTNO_START", 4, 4, $extra);

        //受験番号（終了）
        $extra = " onblur=\"this.value=toInteger(this.value);checkReceptRange();\"";
        $arg["data"]["RECEPTNO_END"] = knjCreateTextBox($objForm, $model->field["RECEPTNO_END"], "RECEPTNO_END", 4, 4, $extra);

        //男女別ラジオボタン 3:全員 1:男子のみ 2:女子のみ
        $opt = array(1, 2, 3);
        makeRdo($objForm, $arg, $model->field["SEX"], "SEX", $opt, "", 3);

        //出力順ラジオボタン 1:受験番号順 2:氏名順(50音順)
        $opt = array(1, 2);
        makeRdo($objForm, $arg, $model->field["ORDER"], "ORDER", $opt);

        //帳票種類ラジオボタン（1:受験者名簿、2:入試成績一覧表、3:合格者一覧表、4:合格通知書）
        $opt = array(1, 2, 3, 4);
        makeRdo($objForm, $arg, $model->field["OUTPUT"], "OUTPUT", $opt);

        //受験者名簿(1)出力条件
        $opt = array(1, 2, 3, 4, 5);
        makeRdo($objForm, $arg, $model->field["FILTER1"], "FILTER1", $opt);

        //入試成績一覧表(2)出力条件
        $opt = array(1, 2, 3, 4, 5);
        makeRdo($objForm, $arg, $model->field["FILTER2"], "FILTER2", $opt);

        //csv出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJL692H");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl692hForm1.html", $arg);
    }
}

function makeRdo(&$objForm, &$arg, &$value, $name, $opt, $extra = "", $defval = "1")
{
    //受験者名簿(1)出力条件
    $model->field[$name] = ($model->field[$name] == "") ? $defval : $model->field[$name];
    $extraObj = array();
    foreach ($opt as $vl) {
        $extraObj[] = "id=\"{$name}$vl\"";
    }
    if ($extra != "") {
        $extraObj[] = $extra;
    }
    $radioArray = knjCreateRadio($objForm, $name, $model->field[$name], $extraObj, $opt, count($opt));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "00:全て", "value" => "00");
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

        if ($row["NAMESPARE2"] && $default_flg && $value != "00") {
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
