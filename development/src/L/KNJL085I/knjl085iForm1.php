<?php
class knjl085iForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl085iQuery::getNameMst($model->ObjYear, "L003", "1"); // 中学で固定
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "", "", "");

        //入試区分
        $query = knjl085iQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "", "", "");

        //面接班
        $query = knjl085iQuery::getHallYdat($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "", "ALL", "");

        //面接評価comboboxのリストを取得
        $query = knjl085iQuery::getInterview($model->ObjYear, "1", "L027"); // 中学用を取得
        $interviewOpt = makeCmb($objForm, $arg, $db, $query, "INTERVIEW", $model->interview, "", 1, "BLANK", "", "1");

        //一覧表示
        $examnoArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl085iQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examNo = $row["EXAMNO"];

                //HIDDENに保持する用
                $examnoArray[] = $examNo;

                //面接評価combobox
                $extra = "id=\"INTERVIEW_A-{$examNo}\" onchange=\"changeFlg(this);\" ";
                $row["INTERVIEW_A"] = knjCreateCombo($objForm, "INTERVIEW_A-{$examNo}", $row["INTERVIEW_A"], $interviewOpt, $extra, 1);

                //面接欠席checkbox
                $checked = ($row["ATTEND_FLG"] == "1") ? " checked": "";
                $extra = "id=\"ATTEND_FLG-{$examNo}\" onclick=\"changeFlg(this);\" ".$checked;
                $row["ATTEND_FLG"] = knjCreateCheckBox($objForm, "ATTEND_FLG-{$examNo}", "1", $extra);

                $dataflg = true;

                $arg["data"][] = $row;
            }

        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $examnoArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl085iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl085iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $all="", $retDiv="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    if ($all) $opt[] = array("label" => "全て", "value" => "ALL");
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
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $opt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg) {
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
function makeHidden(&$objForm, $model, $examnoArray) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $examnoArray));
    knjCreateHidden($objForm, "CHANGE_FLG");
}
?>
