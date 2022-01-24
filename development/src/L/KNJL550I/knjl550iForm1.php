<?php
class knjl550iForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //学科コンボ
        $listExamType = array();
        foreach ($model->examTypeList as $key => $val) {
            $listExamType[] = array(
                "LABEL" => $key.":".$val,
                "VALUE" => $key
            );
        }
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmbList($objForm, $arg, $listExamType, $model->exam_type, "EXAM_TYPE", $extra, 1, "TOP", "");

        //入試区分
        $query = knjl550iQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //科目
        $query = knjl550iQuery::getSettingMst($model, "L009");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "");

        //満点を取得
        $query = knjl550iQuery::getPerfectExamtypeMst($model);
        $perfectRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "PERFECT", strlen($perfectRow["PERFECT"]) ? $perfectRow["PERFECT"] : 0);

        //会場コンボ
        $query = knjl550iQuery::getExamHall($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "ALL");

        if (in_array($model->cmd, array("main"))) {
            $s_receptno = "";
            $e_receptno = "";
            if ($model->examhallcd == "ALL") {
                //開始受験番号、終了受験番号を取得（会場が全ての場合）
                $query = knjl550iQuery::getExamHallAllDetail($model);
                $hallRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $s_receptno = $hallRow["S_RECEPTNO"];
                if ($hallRow["SUM_CAPA"] <= 100) {
                    $e_receptno = $hallRow["E_RECEPTNO"];
                }
            } else {
                //開始受験番号、終了受験番号を取得
                $query = knjl550iQuery::getExamHall($model, $model->examhallcd);
                $hallRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $s_receptno = $hallRow["S_RECEPTNO"];
                $e_receptno = $hallRow["E_RECEPTNO"];
            }
            $model->s_receptno = $s_receptno;
            $model->e_receptno = $e_receptno;
        }

        //開始受験番号
        $extra = "";
        $arg["TOP"]["S_RECEPTNO"] = knjCreateTextBox($objForm, $model->s_receptno, "S_RECEPTNO", 4, 4, $extra);

        //終了受験番号
        $extra="";
        $arg["TOP"]["E_RECEPTNO"] = knjCreateTextBox($objForm, $model->e_receptno, "E_RECEPTNO", 4, 4, $extra);

        //エンター押下時の移動対象一覧
        $setField = array("SCORE");
        knjCreateHidden($objForm, "setField", implode(',', $setField));

        //一覧表示
        $examArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "" && $model->examhallcd != "") {
            //データ取得
            $query = knjl550iQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examno = $row["EXAMNO"];

                //HIDDENに保持する用
                $examArray[] = $examno;

                //試験欠席「JUDGEMENT = 4」の時、得点欄は入力不可とする
                $disScore = ($row["JUDGEMENT"] == "4") ? " disabled" : "";
                knjCreateHidden($objForm, "JUDGEMENT-{$examno}", $row["JUDGEMENT"]);

                //得点
                $extra = " id=\"SCORE-{$examno}\" style=\"text-align:right;\" onkeydown=\"keyChangeEntToTab(this);\" onchange=\"changeFlg(this);\" onblur=\"checkScore(this);\" onPaste=\"return showPaste(this);\" ".$disScore;
                $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], "SCORE-{$examno}", 3, 3, $extra);

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //CSVフォーム部品作成
        makeCsvForm($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $examArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl550iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl550iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
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

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
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

//makeCmbList
function makeCmbList(&$objForm, &$arg, $orgOpt, &$value, $name, $extra, $size, $argName = "", $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    foreach ($orgOpt as $row) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    //読込ボタン
    $extra  = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);

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
function makeHidden(&$objForm, $model, $examArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAM_TYPE");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $examArray));
    knjCreateHidden($objForm, "CHANGE_FLG");
}

//CSVフォーム部品作成
function makeCsvForm(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //ファイル
    $extra = "".$disable;
    $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

    //実行
    $extra = "onclick=\"return btn_submit('exec');\"".$disable;
    $arg["csv"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //ヘッダ有チェックボックス
    if ($model->field["HEADER"] == "on") {
        $check_header = " checked";
    } else {
        $check_header = ($model->cmd == "main") ? " checked" : "";
    }
    $extra = "id=\"HEADER\"".$check_header;
    $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

    //CSV取込書出種別ラジオボタン 1:取込 2:書出 3:ヘッダー
    $opt_shubetsu = array(1, 2, 3);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
    $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["csv"][$key] = $val;
    }
}
