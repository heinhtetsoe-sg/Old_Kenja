<?php

require_once('for_php7.php');

class knjl721hForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl721hindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->examYear;

        //学校種別コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl721hQuery::getNameMst($model->examYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl721hQuery::getTestdiv($model);
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
        $arr_kyokaseq = array();
        $arr_5kyokaseq = array();
        $arr_3kyokaseq = array();
        $keyKyoka = 0;
        $result = $db->query(knjl721hQuery::getEntexamSettingMst($model, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerKyoka[$keyKyoka]["TITLE"] = $row["NAME1"];
            $headerKyoka[$keyKyoka]["SEQ"] = $row["SEQ"];
            $arr_kyokaseq[] = $row["SEQ"];
            //5教科
            if ($row["NAMESPARE1"] == "1") {
                $arr_5kyokaseq[] = $row["SEQ"];
            }
            //3教科
            if ($row["NAMESPARE3"] == "1") {
                $arr_3kyokaseq[] = $row["SEQ"];
            }
            $keyKyoka++;
        }
        $arg["headerKyoka"] = $headerKyoka;

        //テキスト名
        $text_name = array("1"  => "KYOKA_01"
                          ,"2"  => "KYOKA_02"
                          ,"3"  => "KYOKA_03"
                          ,"4"  => "KYOKA_04"
                          ,"5"  => "KYOKA_05"
                          ,"6"  => "KYOKA_06"
                          ,"7"  => "KYOKA_07"
                          ,"8"  => "KYOKA_08"
                          ,"9"  => "KYOKA_09"
                          ,"10" => "KODO"
                          ,"11" => "KESSEKI"
                          ,"12" => "REMARK");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $result = $db->query(knjl721hQuery::selectQuery($model));
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"].'-'.$count;

                $keyKyoka = 0;
                $textboxKyoka = array();
                foreach ($headerKyoka as $val) {
                    //教科名textbox
                    $extra  = "id=\"KYOKA_{$val['SEQ']}-$count\" ";
                    $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 1, this.id);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $textboxKyoka[$keyKyoka]["FORM"] = knjCreateTextBox($objForm, $row["CONFIDENTIAL_RPT{$val['SEQ']}"], "KYOKA_{$val['SEQ']}-".$count, 1, 1, $extra);
                    $keyKyoka++;
                }
                $row["textboxKyoka"] = $textboxKyoka;

                //行動
                $extra  = "id=\"KODO-$count\"";
                $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 3, this.id);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["KODO"] = knjCreateTextBox($objForm, $row["KODO"], "KODO-".$count, 3, 3, $extra);
                //欠席
                $extra  = "id=\"KESSEKI-$count\"";
                $extra .= "style=\"text-align: center; width: 30px;\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toIntegerCheck(this.value, 3, this.id);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["KESSEKI"] = knjCreateTextBox($objForm, $row["KESSEKI"], "KESSEKI-".$count, 1, 3, $extra);
                //備考
                $extra = " onPaste=\"return showPaste(this);\" onblur=\"this.value=toStringCheck(this.value, 15);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                $row["REMARK"] = knjCreateTextBox($objForm, $row["REMARK"], "REMARK-".$count, 32, 30, $extra);

                $arg["data"][] = $row;
                $count++;
            }

            //データ件数が120件以上の場合エラー
            if ($count > 120) {
                $model->setMessage("", "データ件数が120件以上あるため読み込みできません。\\n受験番号を絞って読み込みを行ってください。");
                $arg["data"] = array();
            }

            if ($count == 0) {
                $model->setMessage("MSG303");
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COUNT", $count);
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
        knjCreateHidden($objForm, "HID_KYOKASEQ", implode(",", $arr_kyokaseq));
        knjCreateHidden($objForm, "HID_5KYOKASEQ", implode(",", $arr_5kyokaseq));
        knjCreateHidden($objForm, "HID_3KYOKASEQ", implode(",", $arr_3kyokaseq));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_S_EXAMNO");
        knjCreateHidden($objForm, "HID_E_EXAMNO");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL721H");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl721hForm1.html", $arg);
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

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //読込ボタン
    $extra = "onclick=\"return btn_submit('read');\" tabindex=-1";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "読 込", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\" tabindex=-1";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\" tabindex=-1";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\" tabindex=-1";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
