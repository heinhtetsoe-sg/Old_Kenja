<?php

require_once('for_php7.php');

class knjl271gForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl271gQuery::getNameMst("L003", $model->year);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl271gQuery::getNameMst("L004", $model->year);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //専併区分コンボ
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl271gQuery::getNameMst("L006", $model->year);
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->shdiv, $extra, 1);

        //第１志望コンボ
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl271gQuery::getExamcourse($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSE", $model->examcourse, $extra, 1);

        //特待コンボ
        $extra = "";
        $query = knjl271gQuery::getNameMst("L025", $model->year);
        makeCmb($objForm, $arg, $db, $query, "JUDGEKIND", $model->judgediv, $extra, 1, "BLANK");

        //更新ALLチェック
        $chk = ($model->judgekind == "on") ? " checked" : "";
        $extra = "onclick=\"chkDataALL(this);\"";
        $arg["TOP"]["CHK_DATA_ALL"] = knjCreateCheckBox($objForm, "CHK_DATA_ALL", "on", $extra.$chk);

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->shdiv != "" && $model->examcourse != "") {

            //データ取得
            $result = $db->query(knjl271gQuery::SelectQuery($model));

            //データなし
            if ($result->numRows() == 0) {
               $model->setMessage("MSG303");
            }

            //データ表示
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //更新チェック
                $extra = "onclick=\"bgcolorYellow(this, '{$row["EXAMNO"]}');\"";
                $row["CHK_DATA"] = knjCreateCheckBox($objForm, "CHK_DATA"."-".$row["EXAMNO"], "on", $extra);

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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl271gindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl271gForm1.html", $arg);
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
    knjCreateHidden($objForm, "HID_SHDIV");
    knjCreateHidden($objForm, "HID_EXAMCOURSE");
}
?>
