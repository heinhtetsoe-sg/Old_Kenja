<?php

require_once('for_php7.php');


class knjl009fForm1 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl009findex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["valWindowHeight"]  = $model->windowHeight - 270;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;
        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        //年度
        $arg["top"]["YEAR"] = $model->year;

        //入試制度コンボ
        $query = knjl009fQuery::getNameMst($model->year, "L003", "2");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分
        $query = knjl009fQuery::getNameMst($model->year, ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        /**********/
        /* 検索用 */
        /**********/
        //氏名
        $extra = " STYLE=\"ime-mode: active;\" ";
        $arg["top"]["S_NAME"] = knjCreateTextBox($objForm, $model->field["S_NAME"], "S_NAME", 31, 40, $extra);

        //ふりがな
        $extra = " STYLE=\"ime-mode: active;\" ";
        $arg["top"]["S_NAME_KANA"] = knjCreateTextBox($objForm, $model->field['S_NAME_KANA'], "S_NAME_KANA", 31, 40, $extra);

        //受験番号
        $extra = "style=\"text-align:right; ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["top"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->field['S_EXAMNO'], "S_EXAMNO", 4, 4, $extra);

        //検索
        $extra = "onclick=\"return btn_submit('main');\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/

        //一覧データ出力
        $schcnt = 0;
        $result = $db->query(knjl009fQuery::getSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $link = REQUESTROOT."/L/KNJL011F/knjl011findex.php?cmd=reference&SEND_PRGID=KNJL009F&SEND_AUTH={$model->auth}&SEND_APPLICANTDIV={$model->field["APPLICANTDIV"]}&SEND_EXAMNO={$row["EXAMNO"]}";
            $row["EXAMNO"] = "<a href=\"#\" onClick=\"wopen('{$link}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);return false;\">".$row["EXAMNO"]."</a>";

            $arg["data2"][] = $row;

            $schcnt++;
        }
        $result->free();

        $arg["TOTALCNT"] = $schcnt."件";

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl009fForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != '' && $value_flg) ? $value : $opt[0]["value"];
    $arg["top"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
