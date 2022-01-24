<?php

require_once('for_php7.php');

class knjl052eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl052eQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分
        $query = knjl052eQuery::getNameMst($model->ObjYear, "L004", "", "2");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //会場コンボボックス
        $extra = " onChange=\"return btn_submit('main');\"";
        $query = knjl052eQuery::getHallData($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "BLANK");

        //入試区分
        $query = knjl052eQuery::getNameMst($model->ObjYear, "L027");
        $extra = "onChange=\"return btn_submit('main')\"";
        $setL027 = makeCmb($objForm, $arg, $db, $query, "L027", $val, $extra, 1, "BLANK", "retOpt");

        //一覧表示
        $model->examnoArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->examhallcd != "") {
            //データ取得
            $query = knjl052eQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examNo = $row["EXAMNO"];

                //HIDDENに保持する用
                $model->examnoArray[] = $row["EXAMNO"];

                //面接点1
                $row["INTERVIEW_A"] = $setL027[$row["INTERVIEW_A"]] ? $setL027[$row["INTERVIEW_A"]] : "　";
                //面接点2
                $row["INTERVIEW_B"] = $setL027[$row["INTERVIEW_B"]] ? $setL027[$row["INTERVIEW_B"]] : "　";

                //textbox
                for ($orCnt = 1; $orCnt <= 6; $orCnt++) {
                    $setOnKeyAct = " onkeydown=\"keyChangeEntToTab(this);\"";
                    $setAlign    = "right";
                    $setLength   = "3";
                    $setOnblur   = " onblur=\"this.value=toInteger(this.value);\"";
                    $setOnChange = '';
                    if ($orCnt != 3 && $orCnt != 6) {
                        $setOnKeyAct = " onkeyup=\"henkan(this, '1');\"";
                        $setAlign    = "center";
                        $setLength   = "1";
                        $setOnblur   = '';
                        $setOnChange = " henkan(this, '2');";
                    }
                    $extra = "style=\"text-align:{$setAlign}\" {$setOnKeyAct} onchange=\"changeScore();{$setOnChange}\" {$setOnblur}";
                    $row["OTHER_REMARK{$orCnt}"] = knjCreateTextBox($objForm, $row["OTHER_REMARK{$orCnt}"], "OTHER_REMARK{$orCnt}_{$examNo}", 3, $setLength, $extra);
                }

                $dataflg = true;

                $arg["data"][] = $row;
            }

        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg, $examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl052eindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjl052eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $retDiv="") {
    $opt = array();
    $retOpt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "L027") {
            $row["LABEL"] = $row["NAME1"];
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        $retOpt[$row["VALUE"]] = $row["LABEL"];

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $retOpt;
    }
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
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CHANGE_SCORE");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL052E");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
}
?>
