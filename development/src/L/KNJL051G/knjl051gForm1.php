<?php

require_once('for_php7.php');

class knjl051gForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051gQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051gQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //専併区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051gQuery::getNameMst($model->ObjYear, "L006");
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->shdiv, $extra, 1);

        //志望コースコンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051gQuery::getEntexamCourseMst($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->examcoursecd, $extra, 1);

        //会場コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051gQuery::getEntexamHallYdat($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1);

        //特別措置者(インフルエンザ)
        $extra  = "id=\"SPECIAL_REASON_DIV\" onClick=\"return btn_submit('main');\" ";
        $extra .= strlen($model->special_reason_div) ? "checked" : "";
        $arg["TOP"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //一覧表示
        $arr_receptno = $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->shdiv != "" && $model->examcoursecd != "" && $model->examhallcd != "") {

            //データ取得
            $query = knjl051gQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];
                $arr_examno[]   = $row["EXAMNO"];

                //評価コンボボックス
                $extra = "onchange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\"";
                $query = knjl051gQuery::getNameMst($model->ObjYear, "L027");
                $value = ($model->isWarning()) ? $model->interview_value[$row["RECEPTNO"]] : $row["INTERVIEW_VALUE"];
                $row["INTERVIEW_VALUE"] = makeCmbReturn($objForm, $arg, $db, $query, "INTERVIEW_VALUE[]", $value, $extra, 1, "blank");

                //面接情報テキストボックス
                $value = ($model->isWarning()) ? $model->interview_remark[$row["RECEPTNO"]] : $row["INTERVIEW_REMARK"];
                $extra = "onchange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"width:100%\" onblur=\"CheckLength(this);\"";
                $row["INTERVIEW_REMARK"] = knjCreateTextBox($objForm, $value, "INTERVIEW_REMARK[]", 60, 90, $extra);

                //監督者情報テキストボックス
                $value = ($model->isWarning()) ? $model->remark1[$row["RECEPTNO"]] : $row["REMARK1"];
                $extra = "onchange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"width:100%\" onblur=\"CheckLength(this);\"";
                $row["REMARK1"] = knjCreateTextBox($objForm, $value, "REMARK1[]", 60, 90, $extra);

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl051gindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl051gForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
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

//コンボ作成（表内用）
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_SHDIV");
    knjCreateHidden($objForm, "HID_EXAMCOURSECD");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL051G");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
}
?>
