<?php
class knjl122iForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl122iQuery::getNameMst($model->ObjYear, "L003", $model->applicantdiv); // 高校で固定
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "", "", "");

        //入試区分
        $query = knjl122iQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "", "", "");

        //入試区分B方式か？
        $isTestdivB = ($model->testdiv == "2") ? true : false;
        $arg["isTestdivA"] = ($isTestdivB) ? "" : "1";
        $arg["isTestdivB"] = ($isTestdivB) ? "1" : "";
        knjCreateHidden($objForm, "IS_TESTDIV_B", $isTestdivB);

        //面接班
        $query = knjl122iQuery::getHallYdat($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "", "", "");

        //面接評価comboboxのリストを取得
        $interviewArray = array();
        $opt_interview = array();
        $query = knjl122iQuery::getInterview($model, "LH27"); // 高校用を取得
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $interviewArray[] = array("VALUE" => $row["VALUE"], "LABEL" => $row["LABEL"]);
            $opt_interview[] = $row["VALUE"];
        }
        $result->free();
        knjCreateHidden($objForm, "HID_INTERVIEW", implode(",", $opt_interview));

        //B方式は3つの面接評価
        if ($isTestdivB) {
            $nameArray = array("INTERVIEW_A", "INTERVIEW_B", "INTERVIEW_C");
        } else {
            $nameArray = array("INTERVIEW_A", "INTERVIEW_B");
        }

        //一覧表示
        $examnoArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl122iQuery::selectQuery($model);
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

                //面接評価ラジオボタン
                $setRow = array();
                $disRadio = ($row["ATTEND_FLG"] == "1") ? " disabled" : "";
                foreach($nameArray as $nameKey => $name) {
                    if ($row[$name] == "" && count($interviewArray) > 0) $row[$name] = $interviewArray[0]["VALUE"]; //初期値
                    foreach($interviewArray as $key => $val) {
                        $id = $name."-".$examNo."-".$val["VALUE"];
                        $objForm->ae( array("type"       => "radio",
                                            "name"       => $name."-".$examNo,
                                            "value"      => $row[$name],
                                            "extrahtml"  => "id=\"{$id}\" onclick=\"changeFlg();\" ".$disRadio,
                                            "multiple"   => $opt_interview ) );
                        $setRow[$name] .= $objForm->ge($name."-".$examNo, $val["VALUE"]) . "<LABEL for=\"{$id}\">" . $val["LABEL"] . "</LABEL>" . " ";
                    }
                    $row[$name] = $setRow[$name];
                }

                //面接欠席checkbox
                $checked = ($row["ATTEND_FLG"] == "1") ? " checked": "";
                $extra = "id=\"ATTEND_FLG-{$examNo}\" onclick=\"changeFlg(); disRadio(this);\" ".$checked;
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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl122iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl122iForm1.html", $arg);
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

    //一括更新ボタン
    $extra = "onclick=\"return btn_submit('replace');\"".$disable;
    $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
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
