<?php

require_once('for_php7.php');

class knjl054pForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl054pQuery::getNameMst("L003", $model->ObjYear, "2");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl054pQuery::getNameMst(($model->applicantdiv == "2") ? "L004" : "L024", $model->ObjYear, "1");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {

            list ($coursecd, $majorcd, $examcourse) = preg_split("/-/", $model->examcourse);

            //データ取得
            $result = $db->query(knjl054pQuery::SelectQuery($model, ""));
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $model->s_receptno = $count == 0 ? $row["RECEPTNO"] : $model->s_receptno;
                $model->e_receptno = $row["RECEPTNO"];

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                //背景色
                $row["BGCOLOR"] = "#ffffff";
                $row["TEXT_BGCOLOR"] = ($row["JUDGEDIV"] == "4") ? "cccccc" : "#ffffff";

                //欠席者は表示する。但し、入力不可とする。
                $disJdg = ($row["JUDGEDIV"] == "4") ? " readOnly" : "";

                //実績テキストボックス
                $value = ($model->isWarning()) ? $model->value[$row["RECEPTNO"]]["SCORE"] : $row["SCORE"];
                $extra = " onPaste=\"return showPaste(this, {$count});\" OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;background-color:".$row["TEXT_BGCOLOR"]."\" onblur=\"CheckScore(this);\" onKeyDown=\"keyChangeEntToTab(this)\"" .$disJdg;
                $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 3, 3, $extra);

                //実績状況テキストエリア
                $extra  = " onkeyup =\"charCount(this.value, 5, (40 * 2), true);\" oncontextmenu =\"charCount(this.value, 5, (40 * 2), true);\"";
                $extra .= " onPaste=\"return showPaste(this, {$count});\" OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"background-color:".$row["TEXT_BGCOLOR"]."\"" .$disJdg;
                $row["REMARK1"] = knjCreateTextArea($objForm, "REMARK1[]", "5", "80", "wrap", $extra, $row["REMARK1"]);

                $arg["data"][] = $row;
                $count++;
            }
        }

        //人数
        knjCreateHidden($objForm, "all_count", $count);

        //受験番号セット
        knjCreateHidden($objForm, "s_receptno", $model->s_receptno);
        knjCreateHidden($objForm, "e_receptno", $model->e_receptno);

        //500件以上の場合、切換ボタンで切換
        $getBacCount  = $db->getOne(knjl054pQuery::SelectQuery($model, "BAC_COUNT"));
        $getNextCount = $db->getOne(knjl054pQuery::SelectQuery($model, "NEXT_COUNT"));
        if ($getBacCount > 0) {
            $extra  = "onClick=\"btn_submit('back');\" tabindex=-1 ";
            $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        }
        if ($getNextCount > 0) {
            $extra = "onClick=\"btn_submit('next');\" tabindex=-1 ";
            $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);
        }

        //ボタン作成
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL054P");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl054pindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl054pForm1.html", $arg);
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
