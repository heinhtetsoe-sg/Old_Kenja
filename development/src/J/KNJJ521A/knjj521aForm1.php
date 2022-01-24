<?php
class knjj521aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]    = $objForm->get_start("form1", "POST", "knjj521aindex.php", "", "form1");

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //年度
        $query = knjj521aQuery::getYear();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1, "");

        //タイトル
        $arg["TITLE"] = "新体力テスト測定結果の県への報告画面";

        //県への報告用作成日(テーブルは報告履歴テーブルのみ)
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", str_replace("-", "/", $model->field["EXECUTE_DATE"]), "");

        //V_SCHOOL_MSTから学校コードを取得
        $query = knjj521aQuery::getSchoolMst($model);
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolcd = $rtnRow["KYOUIKU_IINKAI_SCHOOLCD"];
        $model->prefcd = $rtnRow["PREF_CD"];

        //文書番号
        $query = knjj521aQuery::getTuutatu($model);
        $extra = "";
        makeCmb($objForm, $arg, $db2, $query, "DOC_NUMBER", $model->field["DOC_NUMBER"], $extra, 1, "BLANK");

        //学年コンボ
        $query = knjj521aQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "blank");

        //学科コンボ
        $query = knjj521aQuery::getCourseMajor($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "blank");

        //性別コンボ
        $query = knjj521aQuery::getGender($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEX", $model->field["SEX"], $extra, 1, "blank");

        //ヘッダ作成
        $item_abbv = $item_unit = "";

        //問診コード
        $inquiryWidth = 50;
        $arg["INQUIRY_WIDTH"] = $inquiryWidth;
        $inquiryAllWidth = $model->maxInquiryNum * $inquiryWidth;
        $item_abbv .= "<th width=\"{$inquiryAllWidth}\" colspan=\"{$model->maxInquiryNum}\"><font size=\"2\">問診項目</font></th>";
        $inquiryCdArray = range(1, $model->maxInquiryNum);
        foreach ($inquiryCdArray as $inquiryCd) {
            //単位
            $item_unit .= "<th width=\"{$inquiryWidth}\"><font size=\"2\">{$inquiryCd}</font></th>";
        }

        //種目コード(MAX9)
        $recordWidth = 70;
        $arg["RECORD_WIDTH"] = $recordWidth;
        $item_key = array();
        $item_cnt = 0;
        $result = $db->query(knjj521aQuery::getSportsItemMst());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $item_cnt++;
            if ($item_cnt > 9) {
                break;
            }
            $item_key[$item_cnt] = $row["ITEMCD"];
            //略称名
            $item_abbv .= "<th width=\"{$recordWidth}\"><font size=\"2\">{$row["ITEMABBV"]}</font></th>";
            //単位
            $item_unit .= "<th width=\"{$recordWidth}\"><font size=\"2\">{$row["UNIT"]}</font></th>";
        }
        for ($i = 0; $i < (9 - count($item_key)); $i++) {
            $item_abbv .= "<th width=\"{$recordWidth}\">&nbsp;</th>";
            $item_unit .= "<th width=\"{$recordWidth}\">&nbsp;</th>";
        }
        $arg["ITEMABBV"] = $item_abbv;
        $arg["UNIT"] = $item_unit;

        //初期化
        $model->data=array();
        $counter = 0;
        $colorFlg = false;
        $disable = "disabled";

        //一覧表示
        if ($model->cmd == "recalc") {
            $query = knjj521aQuery::getRecalcList($model, $item_key);
        } else {
            $query = knjj521aQuery::getList($model, $item_key);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["ROWNO"] = sprintf("%03d", $row["ROWNO"]);
            $model->data["ROWNO"][] = $row["ROWNO"];


            $targetArray = array("RECORD1","RECORD2","RECORD3","RECORD4","RECORD5","RECORD6","RECORD7","RECORD8","RECORD9", "HEIGHT", "WEIGHT", "SITHEIGHT");
            foreach ($targetArray as $targetName) {
                if ($row[$targetName] != "") {
                    $row[$targetName] = sprintf("%.1f", $row[$targetName]);
                }
            }

            //５行毎に背景色を変える
            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            //各問診項目を作成
            foreach ($inquiryCdArray as $inquiryCd) {
                //テキストボックスを作成
                $name = "INQUIRY".$inquiryCd;
                $extra = "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"this.value=toInteger(this.value);\" onPaste=\"return showPaste(this);\"";
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 2, 1, $extra);
            }

            //各項目を作成
            foreach ($item_key as $lenNo => $itemCd) {
                //各コードを取得
                $model->data["RECORD"][$lenNo] = $itemCd;
                //テキストボックスを作成
                $name = "RECORD".$lenNo;
                $extra = "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onPaste=\"return showPaste(this);\"";
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 5, 5, $extra);
            }

            //総合計
            $name = "TOTAL";
            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"this.value=toInteger(this.value);\" onPaste=\"return showPaste(this);\"";
            $row[$name] = $row[$name];
            knjCreateHidden($objForm, $name."-".$counter, $row[$name]);

            //総合判定
            $name = "VALUE";
            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onPaste=\"return showPaste(this);\"";
            $row[$name] = $row[$name];
            knjCreateHidden($objForm, $name."-".$counter, $row[$name]);

            //身長
            $name = "HEIGHT";
            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onPaste=\"return showPaste(this);\"";
            $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 5, 5, $extra);
            //体重
            $name = "WEIGHT";
            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onPaste=\"return showPaste(this);\"";
            $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 5, 5, $extra);
            //座高
            $name = "SITHEIGHT";
            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onPaste=\"return showPaste(this);\"";
            $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 5, 5, $extra);

            //実施日
            $sokuteiDate = str_replace("-", "/", $row["SOKUTEI_DATE"]);
            //天候
            $weather = $row["WEATHER"];
            //気温
            $temperature = $row["TEMPERATURE"];

            //更新ボタンのＯＮ／ＯＦＦ
            $disable = "";
            $counter++;
            $arg["data"][] = $row;
            $arg["data2"][] = $row;
        }

        //実施日
        //$date = $db->getOne(knjj521aQuery::getScoreDate($model));
        if (!isset($model->warning)) {
            $model->field["SOKUTEI_DATE"] = ($sokuteiDate) ? $sokuteiDate : str_replace("-", "/", CTRL_DATE);
        }
        $arg["SOKUTEI_DATE"] = View::popUpCalendar($objForm, "SOKUTEI_DATE", $model->field["SOKUTEI_DATE"]);

        //天気
        if (!isset($model->warning)) {
            $model->field["WEATHER"] = $weather;
        }
        $extra = "";
        $arg["WEATHER"] = knjCreateTextBox($objForm, $model->field["WEATHER"], "WEATHER", 11, 5, $extra);

        //気温テキストボックス
        if (!isset($model->warning)) {
            $model->field["TEMPERATURE"] = $temperature;
        }
        $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toFloat(this.value)\"";
        $arg["TEMPERATURE"] = knjCreateTextBox($objForm, $model->field["TEMPERATURE"], "TEMPERATURE", 5, 5, $extra);

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告／PDFプレビュー", $extra);
        //報告履歴
        $query = knjj521aQuery::getReport($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "REPORT", $model->field["REPORT"], $extra, 1, 1);

        //再計算ボタン
        $extra = "onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷
        $extra = "onclick=\"newwin('".SERVLET_URL."');\" disabled ";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        $model->field["OUTPUT"] = "2"; // この画面では書き出しのみなので2固定

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "main") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('downloadCsv');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJJ521A");
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "SEMESTER2", $model->field["SEMESTER2"]);
        knjCreateHidden($objForm, "CTRL_D", $execute_date);
        knjCreateHidden($objForm, "ELECTDIV", $electdiv);
       
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjj521aForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } elseif ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model, $db)
{
    //ヘッダ有チェックボックス
    if ($model->field["HEADER"] == "on") {
        $check_header = "checked";
    } else {
        $check_header = ($model->cmd == "main") ? "checked" : "";
    }
    $extra = "id=\"HEADER\"".$check_header;
    $arg["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー出力
    $opt_shubetsu = array(1, 2, 3);
    // $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";

    $model->field["OUTPUT"] = "2"; // この画面では書き出しのみなので2固定
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click."disabled", "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click."disabled");
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg[$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
