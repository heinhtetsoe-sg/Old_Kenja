<?php

require_once('for_php7.php');

class knjl050uForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050uQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050uQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //試験科目コンボボックス
        $extra = "onchange=\"return btn_submit('testsub');\" tabindex=-1";
        $query = knjl050uQuery::getTestSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //一覧表示
        $counter = 0;
        $arr_receptno = array();
        if ($model->cmd == "next" || $model->cmd == "back" || $model->cmd == "reset") {
            if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "" && $model->s_receptno != "") {
                //データ取得
                $query = knjl050uQuery::selectQuery($model);
                $result = $db->query($query);

                if ($result->numRows() == 0) {
                    //0件だった場合、現状の表示を復元するため、入力した状態からの"次へ"検索を行う。
                    $model->chg_srchnoflg = true;
                    $query = knjl050uQuery::selectQuery($model, true);
                    $result = $db->query($query);
                    //再検索してもデータが1件もなかったらメッセージを返す
                    if ($result->numRows() == 0) {
                        $model->setMessage("MSG303");
                    }
                }

                $getfstflg = true;
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    if ($row["ATTEND_FLG"] === '0') {
                        $row["SCORE"] = '*';
                    }

                    //HIDDENに保持する用
                    $arr_receptno[] = $row["RECEPTNO"];

                    //満点チェック用
                    $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                    //得点テキストボックス
                    $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row["SCORE"];
                    $extra = " onPaste=\"return showPaste(this, {$counter});\" OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\"";
                    $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 3, 3, $extra);

                    $arg["data"][] = $row;
                    //検索開始受験番号(昇順にデータが並んでいるため、最初の値が検索開始受験番号)
                    if ($getfstflg) {
                        $model->s_receptno = $row["RECEPTNO"];
                        $getfstflg = false;
                    }
                    //検索終了受験番号(昇順にデータが並んでいるため、最終的には最後の値が検索終了受験番号)
                    $model->e_receptno = $row["RECEPTNO"];
                    $counter++;
                }
                //終了受験番号
                if ($result->numRows() == 0) {
                    $model->e_receptno = "";
                }
            }
        } else {
            //初期化
            $model->s_receptno = "";
            $model->e_receptno = "";
        }

        //開始受験番号テキストボックス
        $extra = " onChange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["S_RECEPTNO"] = knjCreateTextBox($objForm, $model->s_receptno, "S_RECEPTNO", 5, 5, $extra);

        //人数
        knjCreateHidden($objForm, "all_count", $counter);

        //ボタン作成
        $disable  = ($arr_receptno[0] != '') ? "" : " disabled";

        //次へボタン
        $extra = "onclick=\"return btn_submit('next');\"";
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", ">>", $extra);
        //前へボタン
        $extra = "onclick=\"return btn_submit('back');\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "<<", $extra);
        ////読込ボタン
        //$extra = "onclick=\"return btn_submit('read');\"";
        //$arg["btn_reload"] = knjCreateBtn($objForm, "btn_reload", "読 込", $extra);
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
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
        knjCreateHidden($objForm, "HID_S_RECEPTNO", $model->s_receptno);
        knjCreateHidden($objForm, "HID_E_RECEPTNO", $model->e_receptno);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL050U");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050uindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl050uForm1.html", $arg);
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
