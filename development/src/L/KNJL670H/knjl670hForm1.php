<?php

require_once('for_php7.php');

class knjl670hForm1
{
    public function main(&$model)
    {
        define("LINE_MAX", 12); // １頁に表示する最大人数

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl670hForm1", "POST", "knjl670hindex.php", "", "knjl670hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試日程コンボ
        $extra = "onchange=\"btn_submit('knjl670h');\"";
        $query = knjl670hQuery::getEntexamTestDivMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //類別コンボ
        $extra = "";
        $query = knjl670hQuery::getEntexamClassifyMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV1"], "TESTDIV1", $extra, 1, "ALL");

        //合格点
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["PASS_POINT"] = knjCreateTextBox($objForm, $model->field["PASS_POINT"], "PASS_POINT", 4, 4, $extra);

        //シミュレーション最低点（開始）
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SIM_START"] = knjCreateTextBox($objForm, $model->field["SIM_START"], "SIM_START", 4, 4, $extra);

        //シミュレーション最低点（終了）
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SIM_END"] = knjCreateTextBox($objForm, $model->field["SIM_END"], "SIM_END", 4, 4, $extra);

        //併願(東京)確率
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["TOKIO_PROBABILITY"] = knjCreateTextBox($objForm, $model->field["TOKIO_PROBABILITY"], "TOKIO_PROBABILITY", 4, 4, $extra);

        //併願(東京以外)確率
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["OTHER_PROBABILITY"] = knjCreateTextBox($objForm, $model->field["OTHER_PROBABILITY"], "OTHER_PROBABILITY", 4, 4, $extra);

        //一般確率1
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["GENERAL_PROBABILITY1"] = knjCreateTextBox($objForm, $model->field["GENERAL_PROBABILITY1"], "GENERAL_PROBABILITY1", 4, 4, $extra);

        //一般確率1
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["GENERAL_PROBABILITY1"] = knjCreateTextBox($objForm, $model->field["GENERAL_PROBABILITY1"], "GENERAL_PROBABILITY1", 4, 4, $extra);

        //一般確率2
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["GENERAL_PROBABILITY2"] = knjCreateTextBox($objForm, $model->field["GENERAL_PROBABILITY2"], "GENERAL_PROBABILITY2", 4, 4, $extra);

        //一般延納確率1
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["POSTPONE_PROBABILITY1"] = knjCreateTextBox($objForm, $model->field["POSTPONE_PROBABILITY1"], "POSTPONE_PROBABILITY1", 4, 4, $extra);

        //一般延納確率2
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["POSTPONE_PROBABILITY2"] = knjCreateTextBox($objForm, $model->field["POSTPONE_PROBABILITY2"], "POSTPONE_PROBABILITY2", 4, 4, $extra);

        //一般延納確率3
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["POSTPONE_PROBABILITY3"] = knjCreateTextBox($objForm, $model->field["POSTPONE_PROBABILITY3"], "POSTPONE_PROBABILITY3", 4, 4, $extra);

        //履歴の出力
        $query = knjl670hQuery::getRireki($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $arg["data2"][] = $row;
        }

        //合格基準点種別合格者数一覧表ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "合格基準点種別合格者数一覧表", $extra);

        //合格確定ボタン
        $extra = "onclick=\"return btn_submit('fix');\"";
        $arg["button"]["btn_fix"] = knjCreateBtn($objForm, "btn_fix", "合格確定", $extra);

        //合格取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "合格取消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL670H");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl670hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        if ($name == "TESTDIV1") {
            $opt[] = array("label" => "00:全て", "value" => "00");
        } else {
            $opt[] = array("label" => "-- 全て --", "value" => "ALL");
        }
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
