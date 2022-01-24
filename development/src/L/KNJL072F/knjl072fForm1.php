<?php

require_once('for_php7.php');


class knjl072fForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl072fQuery::getNameMst("L003", $model->year, "1");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl072fQuery::getNameMst("L024", $model->year);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //受験型コンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl072fQuery::getExamType($model->year, $model->testdiv);
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->exam_type, $extra, 1, "BLANK");

        //抽出合計点FROM
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["AVG_FROM"] = knjCreateTextBox($objForm, $model->avg_from, "AVG_FROM", 3, 3, $extra);

        //抽出合計点TO
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["AVG_TO"] = knjCreateTextBox($objForm, $model->avg_to, "AVG_TO", 3, 3, $extra);

        //表示順序ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('main');\"", "id=\"SORT2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //特別措置者(インフルエンザ)
        $extra = "id=\"SPECIAL_REASON_DIV\" onchange=\"return btn_submit('main');\" tabindex=-1 ";
        $extra .= strlen($model->special_reason_div) ? "checked='checked' " : "";
        $arg["TOP"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //合否コンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl072fQuery::getNameMst("L013", $model->year);
        makeCmb($objForm, $arg, $db, $query, "JUDGEDIV", $model->judgediv, $extra, 1, "BLANK");

        //合格コースHidden
        $query = knjl072fQuery::getSucCourse($model);
        $sucRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SUC_COURSE", $sucRow["VALUE"]);

        //更新ALLチェック
        $chk = ($model->judgediv == "on") ? " checked" : "";
        $extra = "onclick=\"chkDataALL(this);\"";
        $arg["TOP"]["CHK_DATA_ALL"] = knjCreateCheckBox($objForm, "CHK_DATA_ALL", "on", $extra.$chk);

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {

            //データ取得
            $result = $db->query(knjl072fQuery::SelectQuery($model));

            //データ表示
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["RECEPTNO"] . "-" . $row["EXAMNO"];

                //更新チェック
                $disJdg = ($row["PROCEDUREDIV1"] == "1") ? " disabled" : "";
                $extra = "onclick=\"bgcolorYellow(this, '{$row["RECEPTNO"]}');\"";
                $row["CHK_DATA"] = knjCreateCheckBox($objForm, "CHK_DATA"."-".$row["RECEPTNO"], "on", $extra.$disJdg);

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $arr_examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl072findex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl072fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
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
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $arr_examno) {
    //抽出ボタン
    $extra = "onclick=\"return btn_submit('search');\"";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "抽 出", $extra);
    //更新ボタン
    $disBtn = (0 < get_count($arr_examno)) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disBtn);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
}
?>
