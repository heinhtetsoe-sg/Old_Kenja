<?php

require_once('for_php7.php');

class knjl692iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->examyear;

        //学科コンボ ※固定
        $extra = " onchange=\"return btn_submit('knjl692i');\"";
        $opt = array();
        $opt[] = array('label' => "全て", 'value' => "ALL");
        $opt[] = array('label' => "1:普通科", 'value' => "1");
        $opt[] = array('label' => "2:工業科", 'value' => "2");
        $model->field["GAKKA"] = ($model->field["GAKKA"] == null) ? $model->field["GAKKA"] = "ALL" : $model->field["GAKKA"];
        $arg["data"]["GAKKA"] = knjCreateCombo($objForm, "GAKKA", $model->field["GAKKA"], $opt, $extra, 1);

        //入試区分コンボ
        $extra = "";
        $query = knjl692iQuery::getTestDivMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //類別コンボ
        $extra = "";
        $query = knjl692iQuery::getRuibetsu($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["RUIBETSU"], "RUIBETSU", $extra, 1, "ALL");

        //受験番号
        $extra = "id=\"EXAMNO_FROM\" onblur=\"this.value=toInteger(this.value, 'EXAMNO_FROM'); checkReceptRange('EXAMNO_FROM');\"";
        $arg["data"]["EXAMNO_FROM"] = knjCreateTextBox($objForm, $model->field["EXAMNO_FROM"], "EXAMNO_FROM", 4, 4, $extra);
        $extra = "id=\"EXAMNO_TO\" onblur=\"this.value=toInteger(this.value, 'EXAMNO_TO'); checkReceptRange('EXAMNO_TO');\"";
        $arg["data"]["EXAMNO_TO"] = knjCreateTextBox($objForm, $model->field["EXAMNO_TO"], "EXAMNO_TO", 4, 4, $extra);

        //男女別ラジオボタン（1:男、2:女、3:全て）
        $opt = array(1, 2, 3);
        $model->field["SEX"] = ($model->field["SEX"] == "") ? "3" : $model->field["SEX"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEX{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力ラジオボタン（1:願書リスト、2:出願中学校リスト）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力順ラジオボタン（1:受験番号順、2:氏名(50音順)）
        $opt = array(1, 2);
        $model->field["ORDER"] = ($model->field["ORDER"] == "") ? "1" : $model->field["ORDER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"ORDER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->field["ORDER"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //帳票種類ラジオボタン（1:願書リスト、2:出願中学校リスト）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //帳票種類 種別ラジオボタン（1:願書リスト、2:特待生・学業、3:特待生・部活）
        $opt = array(1, 2, 3);
        $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "1" : $model->field["OUTPUT_DIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

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
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantDiv);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJL692I");

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl692iForm1", "POST", "knjl692iindex.php", "", "knjl692iForm1");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl692iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
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
