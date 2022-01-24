<?php

require_once('for_php7.php');

class knjl521hForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //Windowリサイズ用
        $arg["WindowWidth"]      = $model->windowWidth  - 36;
        $arg["titleWindowWidth"] = $model->windowWidth  - 847;
        $arg["valWindowWidth"]   = $model->windowWidth  - 830;
        $arg["valWindowHeight"]  = $model->windowHeight - 200;
        $arg["tcolWindowHeight"] = $model->windowHeight - 217;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;

        //対象年度コンボボックス
        $opt_year   = array();
        $opt_year[] = array("label" => (CTRL_YEAR),     "value" => CTRL_YEAR);
        $opt_year[] = array("label" => (CTRL_YEAR + 1), "value" => (CTRL_YEAR + 1));
        $extra = "onChange=\"return btn_submit('read');\"";
        $model->year = ($model->year == "") ? substr(CTRL_DATE, 0, 4): $model->year;
        $arg["TOP"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt_year, $extra, 1);

        //学校種別
        $query = knjl521hQuery::getNameMst($model->year, "L003");
        $extra = "onchange=\"return btn_submit('read');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試判別
        $query = knjl521hQuery::getDistinctId($model);
        $extra = "onchange=\"return btn_submit('read');\"";
        makeCmb($objForm, $arg, $db, $query, "DISTINCT_ID", $model->distinctId, $extra, 1, "");

        // 各項目セット
        $hidKoumoku = $sepHid = "";
        $setSubName = "";
        $setCnt = 6;
        $model->confSubclass = $model->subConf = array();
        $model->koumoku = array();
        $query = knjl521hQuery::getNameMst($model->year, "L008");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $name = ($model->applicantdiv == "2") ? $row["NAME1"]: $row["NAME2"];
            if ($name == "") {
                continue;
            }
            $model->koumoku["CONFIDENTIAL_RPT".$row["VALUE"]] = $name;
            $setCnt++;
            // 更新時に使用
            $model->subConf["CONFIDENTIAL_RPT".$row["VALUE"]] = $name;
        }
        $model->koumoku["DET001_1"] = "その他";
        if ($model->applicantdiv == "1") { // 中学
            $model->koumoku["DET001_6"]  = "４年";
            $model->koumoku["DET001_7"]  = "５年";
            $model->koumoku["DET001_8"]  = "６年";
            $setCnt + 3;
        }
        $model->koumoku["TOTAL_ALL"] = "合計";
        $model->koumoku["DET001_2"]  = "行動";
        $model->koumoku["DET001_3"]  = "特別";
        $model->koumoku["DET001_4"]  = "出席";
        $model->koumoku["DET001_5"]  = "合計";
        foreach ($model->koumoku as $fieldName => $nameVal) {
            if ($fieldName == "DET001_1") {
                $size = "55";
            } else {
                $size = "45";
            }
            $setSubName .= "<td width=\"{$size}\" nowrap>";
            $setSubName .= $nameVal;
            $setSubName .= "</td>";

            $hidKoumoku .= $sepHid.$fieldName;
            $sepHid = ",";
        }
        $arg["conf"]["SUB_NAME"] = $setSubName;
        $arg["setWidth"] = $setCnt * 60 + 40;

        //Enterキー移動で使用
        knjCreateHidden($objForm, "HID_KOUMOKU", $hidKoumoku);

        //初期化
        if ($model->cmd == "main") {
            $model->s_examno = "";
            $model->e_examno = "";
        }

        //一覧表示
        $arr_examno = array();
        $s_examno = $e_examno = "";
        $examno = array();
        $dataflg = false;
        if ($model->applicantdiv != "" && $model->distinctId != "") {
            //データ取得
            $query = knjl521hQuery::selectQuery($model, "list");

            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
                $model->e_examno = "";
            }

            $counter = 0;
            $examNos = $exsep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //各科目textbox
                $setTxt = "";
                $toJS_Name = $sepJs = "";
                foreach ($model->koumoku as $fieldName => $name) {
                    $setName = $fieldName."-".$row["EXAMNO"];
                    if ($fieldName == "DET001_1") {
                        $size = "55";
                        $extra  = " id = \"{$setName}\"";
                        $extra .= " OnChange=\"Setflg(this);\" onPaste=\"return showPaste(this, ".$counter.");\" onKeyDown=\"keyChangeEntToTab(this);\"";
                        $txtbox = knjCreateTextBox($objForm, $row[$fieldName], $setName, 6, 9, $extra);
                    } else {
                        $size = "45";
                        $extra  = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" id = \"{$setName}\"";
                        $extra .= " OnChange=\"Setflg(this);\" onPaste=\"return showPaste(this, ".$counter.");\" onKeyDown=\"keyChangeEntToTab(this);\"";
                        $txtbox = knjCreateTextBox($objForm, $row[$fieldName], $setName, 2, 2, $extra);
                    }

                    $setTxt .= "<td width=\"{$size}\" nowrap>";
                    $setTxt .= $txtbox;
                    $setTxt .= "</td>";

                    //jsに渡す用
                    $toJS_Name .= $sepJs.$fieldName;
                    $sepJs = ',';
                }
                $row["CONFIDENTIAL_RPT"] = $setTxt;

                //開始・終了受験番号
                if ($s_examno == "") {
                    $s_examno = $row["EXAMNO"];
                }
                $e_examno = $row["EXAMNO"];
                $dataflg = true;

                $arg["data"][] = $row;
    
                //貼り付けに使用する
                $examNos .= $exsep.$row["EXAMNO"];
                $exsep    = ',';
                $counter++;
            }

            //貼り付けに使用する
            knjCreateHidden($objForm, "TEXTBOX_NAMES", $toJS_Name);
            knjCreateHidden($objForm, "EXAMNO_REN", $examNos);

            //受験番号の最大値・最小値取得
            $exam_array = $db->getCol(knjl521hQuery::selectQuery($model, "examno"));
            $examno["min"] = $exam_array[0];
            $examno["max"] = end($exam_array);

            //初期化
            if (in_array($model->cmd, array("next", "back")) && $dataflg) {
                $model->e_examno = "";
                $model->s_examno = "";
            }
        }

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

        $disable  = ($dataflg &&get_count($arr_examno) > 0) ? "" : " disabled";

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
            $check_header = ($model->cmd == "cmdStart") ? " checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
        knjCreateHidden($objForm, "HID_YEAR");
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_DISTINCT_ID");
        knjCreateHidden($objForm, "HID_S_EXAMNO");
        knjCreateHidden($objForm, "HID_E_EXAMNO");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL521H");

        //DB切断
        Query::dbCheckIn($db);

        //Windowリサイズ用
        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl521hindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl521hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
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
