<?php

require_once('for_php7.php');

class knjl532jForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試種別コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl532jQuery::getTestdiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "BLANK");

        //入試方式コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl532jQuery::getExamType($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMTYPE", $model->examtype, $extra, 1, "BLANK");

        //学校種別表示
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl532jQuery::getApplicantDivName($model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //一覧表示
        $counter = 0;
        $arr_receptno = array();
        if ($model->cmd == "main" || $model->cmd == "next" || $model->cmd == "back" || $model->cmd == "reset") {
            if ($model->testdiv != "" && $model->examtype != "") {
                //データ取得
                $query = knjl532jQuery::SelectQuery($model);
                $result = $db->query($query);

                if ($result->numRows() == 0 ) {
                    //0件だった場合、現状の表示を復元するため、入力した状態からの"次へ"検索を行う。
                    $model->chg_srchnoflg = true;
                    $query = knjl532jQuery::SelectQuery($model, true);
                    $result = $db->query($query);
                    //再検索してもデータが1件もなかったらメッセージを返す
                    if ($result->numRows() == 0 ) {
                        $model->setMessage("MSG303");
                    }
                }

                $getfstflg = true;
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    if ($row["ATTEND_FLG"] === '0') $row["SCORE"] = '*';

                    //HIDDENに保持する用
                    $arr_receptno[] = $row["RECEPTNO"];

                    //満点チェック用
                    $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                    //整理番号テキストボックス
                    $value = ($model->isWarning()) ? $model->orderno[$row["RECEPTNO"]] : $row["ORDERNO"];
                    $extra = " onPaste=\"return showPaste(this, {$counter});\" OnChange=\"Setflg(this);\" id=\"{$row["RECEPTNO"]}\" style=\"text-align:right;\" onblur=\"CheckScore(this);\"";
                    $row["ORDERNO"] = knjCreateTextBox($objForm, $value, "ORDERNO[]", 6, 6, $extra);

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
            } else {
                //初期化
                $model->s_receptno = "";
                $model->e_receptno = "";
            }
        } else {
            //初期化
            $model->s_receptno = "";
            $model->e_receptno = "";
        }

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
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_EXAMTYPE");
        knjCreateHidden($objForm, "HID_S_RECEPTNO", $model->s_receptno);
        knjCreateHidden($objForm, "HID_E_RECEPTNO", $model->e_receptno);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL532J");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl532jindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl532jForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
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
?>
