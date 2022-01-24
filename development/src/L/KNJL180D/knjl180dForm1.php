<?php

require_once('for_php7.php');

class knjl180dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $opt = array();
        $opt[] = array('value' => CTRL_YEAR,     'label' => CTRL_YEAR);
        $opt[] = array('value' => CTRL_YEAR + 1, 'label' => CTRL_YEAR + 1);
        $model->ObjYear = ($model->ObjYear == "") ? substr(CTRL_DATE, 0, 4): $model->ObjYear;
        $extra = "onChange=\" return btn_submit('main');\"";
        $arg["TOP"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->ObjYear, $opt, $extra, 1);

        //志望区分（入試区分）
        $maxTestDiv = $db->getOne(knjl180dQuery::getMaxTestDiv($model->ObjYear));
        $model->testdiv = ($model->testdiv) ? $model->testdiv: $maxTestDiv;
        $query = knjl180dQuery::getTestDivList($model->ObjYear);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //一覧表示
        $arr_examno = array();
        $examno = array();
        $dataflg = false;
        global $sess;
        if ($model->testdiv != "") {
            //データ取得
            $query = knjl180dQuery::SelectQuery($model, "list");
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                if (isset($model->warning)) {
                    $model->field[$row["EXAMNO"]]["EXAMNO"] = $row["EXAMNO"];
                    $model->field[$row["EXAMNO"]]["NAME"] = $row["NAME"];
                    $row = $model->field[$row["EXAMNO"]];
                }

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //入学金予定チェックボックス
                $extra  = ($row["ENTDIV"] == "1" || $row["ENTDIV"] == "2") ? "checked" : "";
                $extra .= " id=\"ENTRYFLG_".$row["EXAMNO"]."\" onChange=\"Setflg(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["ENTRYFLG"] = knjCreateCheckBox($objForm, "ENTRYFLG_".$row["EXAMNO"], "1", $extra, "");

                //$extra = " id=\"PROCEDUREDATE_{$row["EXAMNO"]}\" ";
                $extra = "";
                $row["PROCEDUREDATE"] = str_replace("-", "/", $row["PROCEDUREDATE"]);
                $row["PROCEDUREDATE"] = View::popUpCalendarAlp($objForm, "PROCEDUREDATE_".$row["EXAMNO"], $row["PROCEDUREDATE"], $extra, "");

                //入学キャンセルチェックボックス
                $extra  = ($row["ENTDIV"] == "2") ? "checked" : "";
                $extra .= ($row["ENTDIV"] == "") ? " disabled" : "";

                $extra .= " id=\"ENTRYCANCEL_".$row["EXAMNO"]."\" onChange=\"Setflg(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["ENTRYCANCEL"] = knjCreateCheckBox($objForm, "ENTRYCANCEL_".$row["EXAMNO"], "1", $extra, "");

                $dataflg = true;

                $arg["data"][] = $row;

            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg, $examno);

        //ヘッダ有
        $extra = ($model->field["HEADER"] == "on" || $model->cmd == "main") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl180dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl180dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "－全て－", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg, $examno) {

    $disable  = ($dataflg) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //CSV出力ボタン
    $extra = "onclick=\"return btn_submit('csv');\"".$disable;
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "CSV出力", $extra);

}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL180D");
}
?>
