<?php

require_once('for_php7.php');

class knjl550hForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度コンボボックス
        $opt_year   = array();
        $opt_year[] = array("label" => (CTRL_YEAR),     "value" => CTRL_YEAR);
        $opt_year[] = array("label" => (CTRL_YEAR + 1), "value" => (CTRL_YEAR + 1));
        $extra = "onChange=\"return btn_submit('read');\"";
        $model->year = ($model->year == "") ? substr(CTRL_DATE, 0, 4): $model->year;
        $arg["TOP"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt_year, $extra, 1);

        //学校種別
        $query = knjl550hQuery::getNameMst($model->year, "L003");
        $extra = "onchange=\"return btn_submit('read');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試判別
        $query = knjl550hQuery::getDistinctId($model);
        $extra = "onchange=\"return btn_submit('read');\"";
        makeCmb($objForm, $arg, $db, $query, "DISTINCT_ID", $model->distinctId, $extra, 1, "");

        //試験科目
        $query = knjl550hQuery::getExamTypeTestSubclass($model);
        $extra = "onchange=\"return btn_submit('read');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testSubclassCd, $extra, 1, "");

        //入試種別、試験方式を取得
        $query = knjl550hQuery::getDistinctionMst($model->year, $model->applicantdiv, $model->distinctId);
        $distinction = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->testDiv  = $distinction["TESTDIV"];
        $model->examType = $distinction["EXAM_TYPE"];

        //初期化
        if ($model->cmd == "main") {
            $model->s_examno = "";
            $model->e_examno = "";
        }

        //一覧表示
        $arr_examno = $arr_receptno = array();
        $s_examno = $e_examno = "";
        $examno = array();
        $dataflg = false;
        if ($model->applicantdiv != "" && $model->distinctId != "") {
            //データ取得
            $query = knjl550hQuery::selectQuery($model, "list");

            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
                $model->e_examno = "";
            }

            $counter = 0;
            $receptNos = $exsep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[]   = $row["EXAMNO"];
                $arr_receptno[] = $row["RECEPTNO"];

                //内申合計点textbox
                $setName = "SCORE-{$row["RECEPTNO"]}";
                $extra  = " OnChange=\"Setflg(this);\" id=\"{$row["EXAMNO"]}\" style=\"text-align:center\" onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $extra .= " onPaste=\"return showPaste(this, ".$counter.");\"";
                $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], $setName, 3, 3, $extra);

                //開始・終了受験番号
                if ($s_examno == "") {
                    $s_examno = $row["EXAMNO"];
                }
                $e_examno = $row["EXAMNO"];
                $dataflg = true;

                $arg["data"][] = $row;
    
                //貼り付けに使用する
                $receptNos .= $exsep.$row["RECEPTNO"];
                $exsep    = ',';
                $counter++;
            }

            //受験番号の最大値・最小値取得
            $exam_array = $db->getCol(knjl550hQuery::selectQuery($model, "examno"));
            $examno["min"] = $exam_array[0];
            $examno["max"] = end($exam_array);

            //初期化
            if (in_array($model->cmd, array("next", "back")) && $dataflg) {
                $model->e_examno = "";
                $model->s_examno = "";
            }
        }

        //貼り付けに使用する
        knjCreateHidden($objForm, "RECEPTNO_REN", $receptNos);

        //開始受験番号
        if ($s_examno) {
            $model->s_examno = $s_examno;
        }
        $extra="";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->s_examno, "S_EXAMNO", 10, 10, $extra);

        //終了受験番号
        if ($e_examno) {
            $model->e_examno = $e_examno;
        }
        $extra="";
        $arg["TOP"]["E_EXAMNO"] = knjCreateTextBox($objForm, $model->e_examno, "E_EXAMNO", 10, 10, $extra);

        /**************/
        /* ボタン作成 */
        /**************/
        //読込ボタン
        $extra  = "style=\"width:64px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('read2');\"";
        $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", " 読込み ", $extra);
        //読込ボタン（前の受験番号検索）
        $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('back');\"";
        $extra .= ($examno["min"] != $model->s_examno) ? "" : " disabled";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        //読込ボタン（後の受験番号検索）
        $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('next');\"";
        $extra .= ($examno["max"] != $model->e_examno) ? "" : " disabled";
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        $disable  = ($dataflg &&get_count($arr_receptno) > 0) ? "" : " disabled";

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**************/
        /* ＣＳＶ作成 */
        /**************/
        //ファイル
        $extra = "".$disable;
        $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);
        //取込ボタン
        $extra = "onclick=\"return btn_submit('csvInput');\"".$disable;
        $arg["csv"]["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra);
        //出力ボタン
        $extra = "onclick=\"return btn_submit('csvOutput');\"".$disable;
        $arg["csv"]["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra);
        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = " checked";
        } else {
            $check_header = ($model->cmd == "main") ? " checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
        knjCreateHidden($objForm, "HID_YEAR");
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_DISTINCT_ID");
        knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
        knjCreateHidden($objForm, "HID_S_EXAMNO");
        knjCreateHidden($objForm, "HID_E_EXAMNO");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL550H");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl550hindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl550hForm1.html", $arg);
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
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
