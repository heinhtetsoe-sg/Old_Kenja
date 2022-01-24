<?php

require_once('for_php7.php');

class knjl050pForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050pQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050pQuery::getNameMst(($model->applicantdiv == "2") ? "L004" : "L024", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //受験科目コンボボックス
        $extra = "onchange=\"return btn_submit('testsub');\" tabindex=-1";
        $query = knjl050pQuery::getTestSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //初期化
        if ($model->cmd == "main") {
            $model->s_receptno = "";
            $model->e_receptno = "";
        }

        //開始受験番号テキストボックス
        $extra = " onChange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["S_RECEPTNO"] = knjCreateTextBox($objForm, $model->s_receptno, "S_RECEPTNO", 6, 6, $extra);

        //終了受験番号テキストボックス
        $extra = " onChange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["E_RECEPTNO"] = knjCreateTextBox($objForm, $model->e_receptno, "E_RECEPTNO", 6, 6, $extra);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "" && $model->s_receptno != "" && $model->e_receptno != "" && ($model->cmd == "read" || $model->cmd == "reset")) {

            list ($coursecd, $majorcd, $examcourse) = preg_split("/-/", $model->examcourse);

            //データ取得
            $result = $db->query(knjl050pQuery::SelectQuery($model, ""));
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                if ($row["ATTEND_FLG"] === '0' || $row["JUDGEDIV"] == "4") $row["SCORE"] = '*';

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                //背景色
                $row["BGCOLOR"] = "#ffffff";
                $row["TEXT_BGCOLOR"] = ($row["JUDGEDIV"] == "4") ? "cccccc" : "#ffffff";

                //欠席者は表示する。但し、入力不可とする。
                $disJdg = ($row["JUDGEDIV"] == "4") ? " readOnly" : "";

                //得点テキストボックス
                $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row["SCORE"];
                $extra = " onPaste=\"return showPaste(this, {$count});\" OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;background-color:".$row["TEXT_BGCOLOR"]."\" onblur=\"CheckScore(this);\" onKeyDown=\"keyChangeEntToTab(this)\"" .$disJdg;
                $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 3, 3, $extra);

                $arg["data"][] = $row;
                $count++;
            }
        }

        //人数
        knjCreateHidden($objForm, "all_count", $count);

        //ボタン作成
        $disable = $count > 0 && ($model->cmd == "read" || $model->cmd == "reset") ? "" : " disabled";
        //読込ボタン
        $extra = "onclick=\"return btn_submit('read');\"";
        $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disable);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
        knjCreateHidden($objForm, "HID_S_RECEPTNO");
        knjCreateHidden($objForm, "HID_E_RECEPTNO");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL050P");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050pindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl050pForm1.html", $arg);
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
