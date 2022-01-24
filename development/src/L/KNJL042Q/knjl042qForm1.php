<?php

require_once('for_php7.php');

class knjl042qForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        $name3Flg = SCHOOLKIND == "P";
        if (SCHOOLKIND == "P") {
            $arg["P_MENU"] = "1";
        } else {
            $arg["JH_MENU"] = "1";
        }

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl042qQuery::getNameMst($model, "L003", false);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $setNameCd = SCHOOLKIND == "P" ? "LP24" : "L024";
        $query = knjl042qQuery::getNameMst($model, $setNameCd, false);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //試験室コンボ
        $query = knjl042qQuery::getNameMst($model, "L050", $name3Flg);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TEST_ROOM", $model->test_room, $extra, 1, "BLANK");

        //面接集合時間コンボ
        $query = knjl042qQuery::getNameMst($model, "L051", $name3Flg);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "INTERVIEW_SETTIME", $model->interview_settime, $extra, 1, "BLANK");

        // //面接控室コンボ
        // $query = knjl042qQuery::getNameMst($model, "L052", $name3Flg);
        // $extra = "";
        // makeCmb($objForm, $arg, $db, $query, "INTERVIEW_WAITINGROOM", $model->interview_waitingroom, $extra, 1, "BLANK");

        //面接室コンボ
        $query = knjl042qQuery::getNameMst($model, ($name3Flg ? "L053" : "L052"), $name3Flg);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "INTERVIEW_ROOM", $model->interview_room, $extra, 1, "BLANK");

        //面談終了予定時間コンボ
        $ns2Flg = true;
        $query = knjl042qQuery::getNameMst($model, "L051", $name3Flg, $ns2Flg);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "INTERVIEW_ENDTIME", $model->interview_endtime, $extra, 1, "BLANK");

        //ゼッケン番号コンボ
        $query = knjl042qQuery::getNameMst($model, "L054", $name3Flg);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "INTERVIEW_GROUP", $model->interview_group, $extra, 1, "BLANK");

        //更新ALLチェック
        $extra = "onclick=\"chkDataALL(this);\"";
        $arg["TOP"]["CHK_DATA_ALL"] = knjCreateCheckBox($objForm, "CHK_DATA_ALL", "on", $extra);

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            $result = $db->query(knjl042qQuery::SelectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];
                $row["INTERVIEW_GROUP"] = $row["INTERVIEW_GROUP"].$row["GNO"];

                //更新チェック
                $extra = "id=\"CHK_DATA-{$row["EXAMNO"]}\" onclick=\"bgcolorYellow(this, '{$row["EXAMNO"]}');\"";
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl042qindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl042qForm1.html", $arg);
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
        if ($value === $row["VALUE"]) $value_flg = true;
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
