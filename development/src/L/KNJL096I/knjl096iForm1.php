<?php

class knjl096iForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear."年度";

        //入試制度コンボボックス
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] == "") ? "1" : $model->field["APPLICANTDIV"];
        $extra = "onchange=\"return btn_submit('main');\" ";
        $query = knjl096iQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] == "") ? "1" : $model->field["TESTDIV"];
        $extra = "onchange=\"return btn_submit('main');\" ";
        $query = knjl096iQuery::getTestDiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //判定対象ラジオボタン 1:男子のみ 2:女子のみ
        $opt_sex = array(1, 2);
        $model->field["SEX"] = ($model->field["SEX"] == "") ? "1" : $model->field["SEX"];
        $extra = array("id=\"SEX1\" onclick=\"btn_submit('main');\"", "id=\"SEX2\" onclick=\"btn_submit('main');\"", "id=\"SEX3\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt_sex, count($opt_sex));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //合格点
        if ($model->cmd ="main") {
            $model->field["BORDER_SCORE"] = $db->getOne(knjl096iQuery::getBorderScore($model));
        }
        $model->field["BORDER_SCORE"] = ($model->field["BORDER_SCORE"] == "") ? "0" : $model->field["BORDER_SCORE"];
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["TOP"]["BORDER_SCORE"] = knjCreateTextBox($objForm, $model->field["BORDER_SCORE"], "BORDER_SCORE", 3, 3, $extra);

        //合格者数
        $arg["TOP"]["COUNT"] = $db->getOne(knjl096iQuery::getPassCount($model));

        //確定結果一覧
        $totalCount = 0;
        $query = knjl096iQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $hash = array("cmd"             => "edit",
                          "APPLICANTDIV"    => $row["APPLICANTDIV"],
                          "TESTDIV"         => $row["TESTDIV"],
                          "EXAM_TYPE"       => $row["EXAM_TYPE"],
                          "SEX"             => $row["SHDIV"],
                          "BORDER_SCORE"    => $row["BORDER_SCORE"]);

            $row["BORDER_SCORE"] = View::alink("knjl096iindex.php", $row["BORDER_SCORE"], "", $hash);

            //合格者数 合計
            $totalCount += $row["PASS_COUNT"];

            $arg["data"][] = $row;
        }

        //合格者数 合計
        $arg["data2"]["TOTAL_COUNT"] = $totalCount;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl096iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl096iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }
    $value_flg = false;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //確定ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "確 定", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
