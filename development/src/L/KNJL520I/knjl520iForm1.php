<?php
class knjl520iForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->examYear ."年度";

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

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl520iQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //初期化
        if ($model->cmd == "main") {
            $model->s_examno = "";
            $model->e_examno = "";
        }

        //開始受験番号テキストボックス
        $extra = " onchange=\"this.value=toIntegerCheck(this.value, 4);\"";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->s_examno, "S_EXAMNO", 4, 4, $extra);
        //終了受験番号テキストボックス
        $extra = " onchange=\"this.value=toIntegerCheck(this.value, 4);\"";
        $arg["TOP"]["E_EXAMNO"] = knjCreateTextBox($objForm, $model->e_examno, "E_EXAMNO", 4, 4, $extra);

        //教科名取得
        $headerKyoka = array();
        $keyKyoka = 0;
        $result = $db->query(knjl520iQuery::getSettingMst($model, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerKyoka[$keyKyoka]["TITLE"] = $row["NAME1"];
            $headerKyoka[$keyKyoka]["SEQ"] = $row["SEQ"];
            $keyKyoka++;
        }
        $arg["headerKyoka"] = $headerKyoka;
        knjCreateHidden($objForm, "kyouka_count", $keyKyoka);

        //テキスト名
        $text_name = array();
        $textCnt = 0;
        for ($i = 1; $i <= 3; $i++) {
            for ($j = 1; $j <= 9; $j++) {
                $textCnt++;
                $num = sprintf("%02d", $j);
                $text_name[$textCnt] = "KYOKA{$i}_{$num}";
            }
        }
        $text_name[] = "KESSEKI1";
        $text_name[] = "KESSEKI2";
        $text_name[] = "KESSEKI3";

        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        //一覧表示
        $arr_examno = array();
        $dataflg = false;
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $result = $db->query(knjl520iQuery::selectQuery($model));
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"].'-'.$count;

                $keyKyoka = 0;
                $textboxKyoka1 = array();
                $textboxKyoka2 = array();
                $textboxKyoka3 = array();
                for ($i = 1; $i <= 9; $i++) {
                    $num = sprintf("%02d", $i);
                    //教科名textbox 1年
                    $extra  = "id=\"KYOKA1_{$num}-$count\" ";
                    $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 1, this.id); Keisan({$count}, 1);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $textboxKyoka1[$keyKyoka]["FORM"] = knjCreateTextBox($objForm, $row["KYOKA1_{$num}"], "KYOKA1_{$num}-".$count, 1, 1, $extra);
                    //教科名textbox 2年
                    $extra  = "id=\"KYOKA2_{$num}-$count\" ";
                    $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 1, this.id); Keisan({$count}, 2);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $textboxKyoka2[$keyKyoka]["FORM"] = knjCreateTextBox($objForm, $row["KYOKA2_{$num}"], "KYOKA2_{$num}-".$count, 1, 1, $extra);
                    //教科名textbox 3年
                    $extra  = "id=\"KYOKA3_{$num}-$count\" ";
                    $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 1, this.id); Keisan({$count}, 3);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $textboxKyoka3[$keyKyoka]["FORM"] = knjCreateTextBox($objForm, $row["KYOKA3_{$num}"], "KYOKA3_{$num}-".$count, 1, 1, $extra);
                    $keyKyoka++;
                }
                $row["textboxKyoka1"] = $textboxKyoka1;
                $row["textboxKyoka2"] = $textboxKyoka2;
                $row["textboxKyoka3"] = $textboxKyoka3;

                //合計 1年
                $row["TOTAL_ALL1_ID"] = "TOTAL_ALL1-".$count;
                knjCreateHidden($objForm, "HID_TOTAL_ALL1-".$count, $row["TOTAL_ALL1"]);
                //合計 2年
                $row["TOTAL_ALL2_ID"] = "TOTAL_ALL2-".$count;
                knjCreateHidden($objForm, "HID_TOTAL_ALL2-".$count, $row["TOTAL_ALL2"]);
                //合計 3年
                $row["TOTAL_ALL3_ID"] = "TOTAL_ALL3-".$count;
                knjCreateHidden($objForm, "HID_TOTAL_ALL3-".$count, $row["TOTAL_ALL3"]);

                //平均 1年
                $row["AVERAGE_ALL1_ID"] = "AVERAGE_ALL1-".$count;
                knjCreateHidden($objForm, "HID_AVERAGE_ALL1-".$count, $row["AVERAGE_ALL1"]);
                //平均 2年
                $row["AVERAGE_ALL2_ID"] = "AVERAGE_ALL2-".$count;
                knjCreateHidden($objForm, "HID_AVERAGE_ALL2-".$count, $row["AVERAGE_ALL2"]);
                //平均 3年
                $row["AVERAGE_ALL3_ID"] = "AVERAGE_ALL3-".$count;
                knjCreateHidden($objForm, "HID_AVERAGE_ALL3-".$count, $row["AVERAGE_ALL3"]);

                //欠席 1年
                $extra  = "id=\"KESSEKI1-$count\"";
                $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 3, this.id);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["KESSEKI1"] = knjCreateTextBox($objForm, $row["KESSEKI1"], "KESSEKI1-".$count, 1, 3, $extra);
                //欠席 2年
                $extra  = "id=\"KESSEKI2-$count\"";
                $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 3, this.id);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["KESSEKI2"] = knjCreateTextBox($objForm, $row["KESSEKI2"], "KESSEKI2-".$count, 1, 3, $extra);
                //欠席 3年
                $extra  = "id=\"KESSEKI3-$count\"";
                $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 3, this.id);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["KESSEKI3"] = knjCreateTextBox($objForm, $row["KESSEKI3"], "KESSEKI3-".$count, 1, 3, $extra);

                $arg["data"][] = $row;
                $count++;
                $dataflg = true;
            }

            //データ件数が120件以上の場合エラー
            if ($count > 120) {
                if ($model->isMessage() && $model->cmd == "csvInputMain") {
                    $arg["jscript"] = "alert('".$model->isMessage()."')";
                }
                $model->setWarning("", "データ件数が120件以上あるため読み込みできません。\\n受験番号を絞って読み込みを行ってください。");
                $arg["data"] = array();
            }

            if ($count == 0) {
                $model->setMessage("MSG303");
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $dataflg);

        //CSVフォーム部品作成
        makeCsvForm($objForm, $arg, $model, $dataflg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COUNT", $count);
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_EXAM_TYPE");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL520I");
        knjCreateHidden($objForm, "TEXT_NAME", implode(",", $text_name));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl520iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl520iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
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
function makeBtn(&$objForm, &$arg, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //読込ボタン
    $extra = "onclick=\"return btn_submit('read');\" tabindex=-1";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "読 込", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\" tabindex=-1".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\" tabindex=-1".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\" tabindex=-1";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
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

    //CSV取込書出種別ラジオボタン 1:取込 2:書出
    $opt_shubetsu = array(1, 2);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
    $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["csv"][$key] = $val;
    }
}
