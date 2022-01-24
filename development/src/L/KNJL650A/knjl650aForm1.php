<?php

require_once('for_php7.php');
class knjl650aForm1
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
        $query = knjl650aQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $namecd1 = $model->applicantdiv == "1" ? "L024" : "L004";
        $query = knjl650aQuery::getNameMst($model->ObjYear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //試験科目コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl650aQuery::getTestSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //会場コンボボックス
        $extra = "tabindex=-1";
        $query = knjl650aQuery::getHallcd($model);
        makeCmb($objForm, $arg, $db, $query, "HALLCD", $model->hallcd, $extra, 1, "BLANK", "ALL");

        //検索ボタン
        $extra = "onclick=\"return btn_submit('read');\"";
        $arg["TOP"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "検 索", $extra);

        //表示順 (1:座席番号順 2:受験番号順)
        $opt = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }
        
        //一覧表示
        $counter = 0;
        $start = 1;
        $maxcnt = 1;
        $arr_receptno = array();
        if ($model->cmd == "read" || $model->cmd == "reset" || $model->cmd == "next" || $model->cmd == "back") {
            if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "" && $model->hallcd != "") {
                //データ取得
                if ($model->cmd == "read") {
                    $start = 1;
                }
                if ($model->cmd == "reset" || $model->cmd == "next" || $model->cmd == "back") {
                    $start = $model->statno;
                }
                $query = knjl650aQuery::selectQuery($model, $start);
                $result = $db->query($query);

                //データが1件もなかったらメッセージを返す
                if ($result->numRows() == 0) {
                    $model->setMessage("MSG303");
                }
                $getfstflg = true;
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    //HIDDENに保持する用
                    $arr_receptno[] = $row["RECEPTNO"];
                    knjCreateHidden($objForm, "MAGNIFYING_".$row["RECEPTNO"], $row["MAGNIFYING"]); //倍率

                    //満点チェック用
                    $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                    if ($row["ATTEND_FLG"] == "0") {
                        $row["SCORE"] = "*";
                    }
    
                    //他入試区分の面接得点を取得
                    $interview = $db->getOne(knjl650aQuery::getInterviewScore($model, $row["EXAMNO"], $row["RECEPTNO"]));

                    //得点テキストボックス
                    $disable = "";
                    if ($model->testsubclasscd == 'A') {
                        //科目「A:面接」
                        if ($interview != "") {
                            //他入試区分に得点が存在する場合、入力不可とする
                            $disable = " disabled";
                            $row["SCORE"] = $interview;
                        }
                    }
                    $value = ($model->isWarning()) ? $model->arr_score[$row["RECEPTNO"]] : $row["SCORE"];
                    $extra = " onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this, {$counter});\" OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this, {$row["PERFECT"]});\"".$disable;
                    $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE-".$row["RECEPTNO"], 3, 3, $extra);

                    $arg["data"][] = $row;
                    $counter++;
                }
                $query = knjl650aQuery::selectQueryCsv($model);
                $maxcnt  = $db->getOne($query);
            }
        } else {
            $model->statno = 1;
        }

        //人数
        knjCreateHidden($objForm, "all_count", $counter);

        //ボタン作成
        $disable  = ($arr_receptno[0] != '') ? "" : " disabled";
        $disable2 = ($start != 1) ? "" : " disabled";
        $disable3 = ($counter <= 50 && $maxcnt < ($start + $counter)) ? " disabled" : "";
        if ($counter == 0) {
            $disable3 = " disabled";
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //戻るボタン
        $extra = "onclick=\"return btn_submit('back');\"".$disable2;
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        //進むボタン
        $extra = "onclick=\"return btn_submit('next');\"".$disable3;
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        /**************/
        /* ＣＳＶ作成 */
        /**************/
        //ファイル
        $extra = "".$disable;
        $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);
        //取込ボタン
        $extra = "onclick=\"return btn_submit('csvInput');\"";
        $arg["csv"]["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra);
        //出力ボタン
        $extra = "onclick=\"return btn_submit('csvOutput');\"";
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
        knjCreateHidden($objForm, "HID_HALLCD");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL650A");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "STARTNO", $start);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl650aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl650aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $all = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    
    if ($all == "ALL") {
        $opt[] = array('label' => "全て", 'value' => "ALL");
    }
    if ($value == "ALL") {
        $value_flg = true;
    }

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
