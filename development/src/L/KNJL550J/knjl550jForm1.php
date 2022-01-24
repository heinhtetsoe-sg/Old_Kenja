<?php

require_once('for_php7.php');

class knjl550jForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->exp_year;

        //学校種別表示
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl550jQuery::getNameMst($model->exp_year, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        ////事前チェック（評価）
        $valArray = array("");

        //入試種別
        $query = knjl550jQuery::getTestdiv($model);
        $extra = "onChange=\"return btn_submit('read')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //入試方式
        $query = knjl550jQuery::getExamType($model);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $blank = ($model->applicantdiv == "2") ? "ALL" : "BLANK";
        makeCmb($objForm, $arg, $db, $query, "EXAMTYPE", $model->examtype, $extra, 1, $blank);

        //科目選択
        $query = knjl550jQuery::getSubjectList($model);
        $extra = "onChange=\"return btn_submit('read')\"";
        makeCmb($objForm, $arg, $db, $query, "EVALTYPE", $model->evaltype, $extra, 1);

        //検索範囲タイトル
        if ($model->applicantdiv == "1") {
            $arg["TOP"]["FINDNOTTL"] = "整理番号";
        } else {
            $arg["TOP"]["FINDNOTTL"] = "受験番号";
        }

        //入力項目タイトル
        $arg["TOP"]["VALUE_TITLE"]  = "得点";

        //初期化
        if ($model->cmd == "main") {
            $model->s_findno = "";
            $model->e_findno = "";
        }

        //一覧表示
        $arr_examno = array();
        $s_findno = $model->s_findno;
        $e_findno = $model->e_findno;
        $examno = array();
        $dataflg = false;
        if ($model->testdiv != "") {
            //データ取得
            $query = knjl550jQuery::SelectQuery($model, "list");
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 && ($model->testdiv != "" && $model->examtype != "")) {
                $model->setWarning("MSG303");
                //$model->e_findno = "";
            }
            $counter = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => $row["PERFECT"]);

                $value = $row["SCORE"];
                if ($model->examtype == "ALL" && !$row["EXAMTYPE_SUBCLASS"]) {
                    $extra = " readOnly id=\"".$row["RECEPTNO"]."\" style=\"text-align:right; background-color:#999999;\" onKeyDown=\"keyChangeEntToTab(this);\" ";
                } else {
                    $extra = " onPaste=\"return showPaste(this, {$counter});\" OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                }
                $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 3, 3, $extra);

                //開始・終了受験番号
                if ($model->applicantdiv == "1") {
                    if ($s_findno == "") $s_findno = $row["ORDERNO"];
                    if ($model->e_findno == "") $e_findno = $row["ORDERNO"];
                } else {
                    if ($s_findno == "") $s_findno = $row["RECEPTNO"];
                    if ($model->e_findno == "") $e_findno = $row["RECEPTNO"];
                }
                $dataflg = true;
                $counter++;
                $arg["data"][] = $row;
            }
            //人数
            knjCreateHidden($objForm, "all_count", $counter);

            //受験番号の最大値・最小値取得
            $exam_array = $db->getCol(knjl550jQuery::SelectQuery($model, "examno"));
            $examno["min"] = $exam_array[0];
            $examno["max"] = end($exam_array);
        }

        //開始受験番号
        $extra="";
        $arg["TOP"]["S_FINDNO"] = knjCreateTextBox($objForm, $model->s_findno, "S_FINDNO", 5, 5, $extra);

        //終了受験番号
        $extra="";
        $arg["TOP"]["E_FINDNO"] = knjCreateTextBox($objForm, $model->e_findno, "E_FINDNO", 5, 5, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $valArray, $dataflg, $examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl550jindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl550jForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    if ($blank == "ALL") $opt[] = array("label" => "-- 全て --", "value" => "ALL");
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

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $valArray, $dataflg, $examno) {
    //読込ボタン
    $extra  = "style=\"width:64px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('read2');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", " 読込み ", $extra);
    //読込ボタン（前の受験番号検索）
    $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('back');\"";
    $extra .= ($examno["min"] != $model->s_findno) ? "" : " disabled";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
    //読込ボタン（後の受験番号検索）
    $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('next');\"";
    $extra .= ($examno["max"] != $model->e_findno) ? "" : " disabled";
    $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

    $disable  = ($dataflg &&get_count($valArray) > 0) ? "" : " disabled";

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
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EVALTYPE");
    knjCreateHidden($objForm, "HID_S_FINDNO");
    knjCreateHidden($objForm, "HID_E_FINDNO");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL550J");
}
?>
