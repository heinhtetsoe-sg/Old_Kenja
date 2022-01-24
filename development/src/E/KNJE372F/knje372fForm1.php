<?php

require_once('for_php7.php');


class knje372fForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組リストを作成する
        $query = knje372fQuery::getHrClass($model, CTRL_YEAR, CTRL_SEMESTER);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1, "BLANK");

        //ソート順ラジオ
        $opt = array(1, 2);
        $model->field["SORT_ITEM"] = $model->field["SORT_ITEM"] ? $model->field["SORT_ITEM"] : 1;
        $extra = array("id=\"SORT_ITEM1\" aria-label=\"換算値順\" onclick=\"current_cursor('SORT_ITEM1');btn_submit('edit');\""
                     , "id=\"SORT_ITEM2\" aria-label=\"クラス順\" onclick=\"current_cursor('SORT_ITEM2');btn_submit('edit');\"");
        $radioArray =  knjCreateRadio($objForm, "SORT_ITEM", $model->field["SORT_ITEM"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //実力科目名称取得
        $query = knje372fQuery::getNameMst("E072", "01");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $subclassCd1 = $row["NAMESPARE1"];
        $subclassCd2 = $row["NAMESPARE2"];
        $query = knje372fQuery::getProficiencySubclassMst($subclassCd1);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["PROFICIENCY1_SUBCLASS_NAME1"] = $row["LABEL"];
        $query = knje372fQuery::getProficiencySubclassMst($subclassCd2);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["PROFICIENCY1_SUBCLASS_NAME2"] = $row["LABEL"];

        $query = knje372fQuery::getNameMst("E072", "02");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $subclassCd1 = $row["NAMESPARE1"];
        $subclassCd2 = $row["NAMESPARE2"];
        $query = knje372fQuery::getProficiencySubclassMst($subclassCd1);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["PROFICIENCY2_SUBCLASS_NAME1"] = $row["LABEL"];
        $query = knje372fQuery::getProficiencySubclassMst($subclassCd2);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["PROFICIENCY2_SUBCLASS_NAME2"] = $row["LABEL"];

        //換算値順位の重複取得
        $convRankDupList = array();
        $query = knje372fQuery::selectDuplicatRank();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $convRankDupList[] = $row["CONVERT_RANK"];
        }
        //クラス選択時は順位の変更不可
        $disabled = "";
        if ($model->field["HR_CLASS"] != "") {
            $disabled = " disabled ";
        }
        $schregNoList = array();
        $counter = 0;
        $colorFlg = false;
        //データ取得
        $query = knje372fQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            $row["ROW_COLOR"] = $colorFlg ? "#FFFFFF" : "#CCCCCC";
            $row["COLOR"] = "";
            //順位が重複している場合は背景色を変更する。
            if (in_array($row["CONVERT_RANK"], $convRankDupList)) {
                $row["COLOR"] = "#FFFF00";
            }
            //少数点以下1位まで表示
            $row["CONVERT_SCORE"] = sprintf("%.1f", $row["CONVERT_SCORE"]);
            $row["CONVERT_TOTAL"] = sprintf("%.1f", $row["CONVERT_TOTAL"]);
            $row["PROFICIENCY1_AVG"] = sprintf("%.1f", $row["PROFICIENCY1_AVG"]);
            $row["PROFICIENCY2_AVG"] = sprintf("%.1f", $row["PROFICIENCY2_AVG"]);
            $row["TOTAL_AVG"] = sprintf("%.1f", $row["TOTAL_AVG"]);
            
            //換算値順位
            $extra = "style=\"text-align:right;\"". $disabled;
            $extra .= "onChange=\"rankChange('{$row["SCHREGNO"]}', this); \" ";
            $extra .= "onblur=\"this.value=toInteger(this.value)\" ";

            $convertRank = $row["CONVERT_RANK"];
            //入力エラー時は画面で入力された値を設定
            if ($model->cmd == "check") {
                $convertRank = $model->convertRank[$row["SCHREGNO"]];
                //値が異なる場合、背景色を変更
                if ($row["CONVERT_RANK"] != $model->convertRank[$row["SCHREGNO"]]) {
                    $row["COLOR"] = "#9DCCE0";
                }
            }
            $row["CONVERT_RANK"] = knjCreateTextBox($objForm, $convertRank, "CONVERT_RANK_".$row["SCHREGNO"], 3, 3, $extra, "");

            //改行を変更
            $row["ACTIVITY_CONTENT"] = str_replace(array("\n", "\r\n"), "<br>", $row["ACTIVITY_CONTENT"]);

            $schregNoList[] = $row["SCHREGNO"];
            $arg["data"][] = $row;
            $counter++;
        }

        //対象の学籍番号を出力
        knjCreateHidden($objForm, "SCHREGNO_LIST", implode(",", $schregNoList));

        Query::dbCheckIn($db);

        // ボタンの作成
        makeBtn($objForm, $arg, $model);
        // hidden作成
        makeHidden($objForm, $model);

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $arg["IFRAME"] = VIEW::setIframeJs();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje372findex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knje372fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    //CSV処理ボタン
    // $extra = " onclick=\"if (csvConfirm()) loadwindow('knje372findex.php?";
    // $extra .= "cmd=csv&HR_CLASS={$model->field["HR_CLASS"]}' ";
    // $extra .= ", (event.clientX - 480) + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), (event.clientY - (event.clientY > 400 ? 400 : 0)) + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 480, 350);\" ";
    // $arg["btn_csv"] = knjCreateBtn($objForm, "btn_input", "ＣＳＶ処理", $extra);
    $extra  = "id= \"btn_csv\" onClick=\" wopen('".REQUESTROOT."/X/KNJX_E372F/knjx_e372findex.php";
    $extra .= "?SEND_PRGID=KNJE372F&SEND_AUTH={$model->auth}&SEND_GRADE_HR_CLASS={$model->field["HR_CLASS"]}'";
    $extra .= ", 'SUBWIN3', 0, 0, screen.availWidth, screen.availHeight);\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV処理", $extra);

    $disabled = "";
    if ($model->field["HR_CLASS"] != "") {
        $disabled = " disabled ";
    }
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disabled;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

}

//hidden作成
function makeHidden(&$objForm, $model) {

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "RANK_CHANGED");

}

?>
