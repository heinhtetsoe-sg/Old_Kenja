<?php

require_once('for_php7.php');

class knjl571aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["TITLE"] = "合否判定";
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試区分
        $query = knjl571aQuery::getNameMst($model->ObjYear, "L004");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //志望区分
        $query = knjl571aQuery::getHopeCourse($model->ObjYear);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->field["DESIREDIV"], $extra, 1, "blank");

        //絞り込み(1:得点、2:内申)
        $opt = array(1, 2);
        $model->field["SIBORI"] = ($model->field["SIBORI"] == "") ? "1" : $model->field["SIBORI"];
        $extra = array();
        for ($idx = 1; $idx <= 2; $idx++) {
            array_push($extra , "id=\"SIBORI".$idx."\" onchange=\"return btn_submit('reload');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "SIBORI", $model->field["SIBORI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //内申合計(1:5科合計、2:9科目合計)
        $opt = array(1, 2);
        $model->field["SEL_TOTAL"] = ($model->field["SEL_TOTAL"] == "") ? "1" : $model->field["SEL_TOTAL"];
        $extra = array();
        for ($idx = 1; $idx <= 2; $idx++) {
            array_push($extra , "id=\"SEL_TOTAL".$idx."\" onchange=\"return btn_submit('reload');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "SEL_TOTAL", $model->field["SEL_TOTAL"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //得点絞り込み入力
        $extra = "";
        if($model->field["SIBORI"] != "1"){
            $extra .= "disabled";
            $model->field["POINT_FROM"] = "";
            $model->field["POINT_TO"] = "";
        }
        $arg["TOP"]["POINT_FROM"] = knjCreateTextBox($objForm, $model->field["POINT_FROM"], "POINT_FROM", 3, 3, $extra);
        $arg["TOP"]["POINT_TO"] = knjCreateTextBox($objForm, $model->field["POINT_TO"], "POINT_TO", 3, 3, $extra);

        //内申絞り込み入力(5科合計)
        $extra = "";
        if($model->field["SIBORI"] != "2" || $model->field["SEL_TOTAL"] != "1"){
            $extra .= "disabled";
            $model->field["TOTAL5_FROM"] = "";
            $model->field["TOTAL5_TO"] = "";
        }
        $arg["TOP"]["TOTAL5_FROM"] = knjCreateTextBox($objForm, $model->field["TOTAL5_FROM"], "TOTAL5_FROM", 3, 3, $extra);
        $arg["TOP"]["TOTAL5_TO"] = knjCreateTextBox($objForm, $model->field["TOTAL5_TO"], "TOTAL5_TO", 3, 3, $extra);

        //内申絞り込み入力(9科合計)
        $extra = "";
        if($model->field["SIBORI"] != "2" || $model->field["SEL_TOTAL"] != "2"){
            $extra .= "disabled";
            $model->field["TOTAL9_FROM"] = "";
            $model->field["TOTAL9_TO"] = "";
        }
        $arg["TOP"]["TOTAL9_FROM"] = knjCreateTextBox($objForm, $model->field["TOTAL9_FROM"], "TOTAL9_FROM", 3, 3, $extra);
        $arg["TOP"]["TOTAL9_TO"]   = knjCreateTextBox($objForm, $model->field["TOTAL9_TO"], "TOTAL9_TO", 3, 3, $extra);

        //合否入力済みを除く(左側)
        $extra  = "id=\"IGNORE_GOUHI_LEFT\"";
        $extra .= ($model->field["IGNORE_GOUHI_LEFT"] == "1") ? " checked": "";
        if($model->field["SIBORI"] != "1") $extra .= " disabled";
        $arg["TOP"]["IGNORE_GOUHI_LEFT"] = knjCreateCheckBox($objForm, "IGNORE_GOUHI_LEFT", "1", $extra);

        //合否入力済みを除く(右側)
        $extra  = "id=\"IGNORE_GOUHI_RIGHT\"";
        $extra .= ($model->field["IGNORE_GOUHI_RIGHT"] == "1") ? " checked": "";
        if($model->field["SIBORI"] != "2") $extra .= " disabled";
        $arg["TOP"]["IGNORE_GOUHI_RIGHT"] = knjCreateCheckBox($objForm, "IGNORE_GOUHI_RIGHT", "1", $extra);

        //入力項目 (1:合否 2:奨学生 3:特進勧誘)
        $opt = array(1, 2, 3);
        $model->field["INPUT"] = ($model->field["INPUT"] == "") ? "1" : $model->field["INPUT"];
        $extra = array();
        for ($idx = 1; $idx <= 3; $idx++) {
            array_push($extra , "id=\"INPUT".$idx."\" onchange=\"return btn_submit('reload');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "INPUT", $model->field["INPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //更新チェックボックス(ヘッダ)
        $disabled = "";
        $checked = ($model->field["CHECKALL"] == "1") ? " checked": "";
        $extra = " id=\"CHECKALL\" onchange=\"return btn_submit('reload');\" ".$checked.$disabled;
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra);

        //ソート表示文字作成
        $order[1] = "▲";
        $order[2] = "▼";
        $model->getSort = $model->getSort ? $model->getSort : "";
        $item = "TESTDIV=".$model->field["TESTDIV"]."&DESIREDIV=".$model->field["DESIREDIV"]."&SIBORI=".$model->field["SIBORI"]."&POINT_FROM=".$model->field["POINT_FROM"]."&POINT_TO=".$model->field["POINT_TO"]."&IGNORE_GOUHI_LEFT=".$model->field["IGNORE_GOUHI_LEFT"]."&SEL_TOTAL=".$model->field["SEL_TOTAL"]."&TOTAL5_FROM=".$model->field["TOTAL5_FROM"]."&TOTAL5_TO=".$model->field["TOTAL5_TO"]."&TOTAL9_FROM=".$model->field["TOTAL9_FROM"]."&TOTAL9_TO=".$model->field["TOTAL9_TO"]."&IGNORE_GOUHI_RIGHT=".$model->field["IGNORE_GOUHI_RIGHT"]."&INPUT=".$model->field["INPUT"]."&CHECKALL=".$model->field["CHECKALL"];

        //リストヘッダーソート作成 得点合計
        $model->sort["SRT_POINT"] = $model->sort["SRT_POINT"] ? $model->sort["SRT_POINT"] : 1;
        $setOrder = ($model->getSort == "SRT_POINT") ? $order[$model->sort["SRT_POINT"]] : "";
        $POINT_SORT = "<a href=\"knjl571aindex.php?cmd=sort&sort=SRT_POINT&".$item."\" target=\"_self\" STYLE=\"color:white\">得点合計{$setOrder}</a>";
        $arg["POINT_SORT"] = $POINT_SORT;

        //リストヘッダーソート作成 5科内申合計
        $model->sort["SRT_NAISIN5"] = $model->sort["SRT_NAISIN5"] ? $model->sort["SRT_NAISIN5"] : 1;
        $setOrder = ($model->getSort == "SRT_NAISIN5") ? $order[$model->sort["SRT_NAISIN5"]] : "";
        $NAISIN5_SORT = "<a href=\"knjl571aindex.php?cmd=sort&sort=SRT_NAISIN5&".$item."\" target=\"_self\" STYLE=\"color:white\">5科内申合計{$setOrder}</a>";
        $arg["NAISIN5_SORT"] = $NAISIN5_SORT;

        //リストヘッダーソート作成 9科内申合計
        $model->sort["SRT_NAISIN9"] = $model->sort["SRT_NAISIN9"] ? $model->sort["SRT_NAISIN9"] : 1;
        $setOrder = ($model->getSort == "SRT_NAISIN9") ? $order[$model->sort["SRT_NAISIN9"]] : "";
        $NAISIN9_SORT = "<a href=\"knjl571aindex.php?cmd=sort&sort=SRT_NAISIN9&".$item."\" target=\"_self\" STYLE=\"color:white\">9科内申合計{$setOrder}</a>";
        $arg["NAISIN9_SORT"] = $NAISIN9_SORT;

        //リストヘッダーソート作成 確約者
        $model->sort["SRT_PROMISED"] = $model->sort["SRT_PROMISED"] ? $model->sort["SRT_PROMISED"] : 1;
        $setOrder = ($model->getSort == "SRT_PROMISED") ? $order[$model->sort["SRT_PROMISED"]] : "";
        $PROMISED_SORT = "<a href=\"knjl571aindex.php?cmd=sort&sort=SRT_PROMISED&".$item."\" target=\"_self\" STYLE=\"color:white\">確約者{$setOrder}</a>";
        $arg["PROMISED_SORT"] = $PROMISED_SORT;

        $arr_examno = array();
        if($model->cmd == "search" || ($model->hid_examno != "" && $model->cmd == "reload") || $model->cmd == "reset" || $model->cmd == "sort"){

            //データ一覧取得
            $query = knjl571aQuery::SelectQuery($model);
            $result = $db->query($query);
            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }
            //一覧表示
            $arr_examno = array();
            $examno = array();
            $dataflg = false;
            $updflg = false;
            $valueFlg = false;
            if($model->cmd == "search" || $model->cmd == "reset" || $model->cmd == "sort") $valueFlg = true;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //更新チェックボックス
                $disabled = "";
                $checked = ($model->field["CHECKALL"] == "1" || $model->data["CHECKED"][$row["EXAMNO"]] == "1") ? " checked": "";
                $extra  = "id=\"CHECKED_{$row["EXAMNO"]}\" onclick=\"updChk('{$row["EXAMNO"]}');\" ".$checked.$disabled;
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED_".$row["EXAMNO"], "1", $extra);

                $val = ($valueFlg) ? $row["PASS_UNPASS"] : $model->data["PASS_UNPASS"][$row["EXAMNO"]];
                //合否ラベル(DB取得値と差異がある場合のみ)
                if (isset($model->data["PASS_UNPASS"][$row["EXAMNO"]])
                    && $row["PASS_UNPASS"] != $model->data["PASS_UNPASS"][$row["EXAMNO"]]) {
                    $row["PU_LABEL"] = $db->getOne(knjl571aQuery::getPULabel($model, $row["EXAMNO"], $val));
                }
                //合否コンボ
                $extra = "onchange=\"return btn_submit('reload');\"";
                if($model->field["INPUT"] != "1"){
                    $extra .= "disabled";
                    knjCreateHidden($objForm, "HID_PASS_UNPASS_".$row["EXAMNO"], $val);
                }
                $val = ($model->cmd != "reload" && $row["JUDGEMENT"] == "2") ? "UNPASS": $val;
                $query = knjl571aQuery::getHopeCoursePass($model->ObjYear);
                makeCmbData($objForm, $row, $db, $query, "PASS_UNPASS", "PASS_UNPASS_".$row["EXAMNO"], $val, $extra, 1, "blank");

                //奨学生コンボ
                $val = ($valueFlg) ? $row["SCHOLARSHIP_STUDENT"] : $model->data["SCHOLARSHIP_STUDENT"][$row["EXAMNO"]];
                $extra = "";
                if($model->field["INPUT"] != "2"){
                    $extra .= "disabled";
                    knjCreateHidden($objForm, "HID_SCHOLARSHIP_STUDENT_".$row["EXAMNO"], $val);
                }
                $query = knjl571aQuery::getNameMst($model->ObjYear, "L025");
                makeCmbData($objForm, $row, $db, $query, "SCHOLARSHIP_STUDENT", "SCHOLARSHIP_STUDENT_".$row["EXAMNO"], $val, $extra, 1);

                //特進勧誘コンボ
                $val = ($valueFlg) ? $row["SP_SOLICIT"] : $model->data["SP_SOLICIT"][$row["EXAMNO"]];
                $extra = "";
                if($model->field["INPUT"] != "3"){
                    $extra .= "disabled";
                    knjCreateHidden($objForm, "HID_SP_SOLICIT_".$row["EXAMNO"], $val);
                }
                $query = knjl571aQuery::getNameMst($model->ObjYear, "L066");
                makeCmbData($objForm, $row, $db, $query, "SP_SOLICIT", "SP_SOLICIT_".$row["EXAMNO"], $val, $extra, 1);

                $dataflg = true;
                if($model->field["CHECKALL"] == "1" || $model->data["CHECKED"][$row["EXAMNO"]] == "1") $updflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg, $updflg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl571aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl571aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "－全て－", "value" => "");
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
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeCmbData(&$objForm, &$dataRow, $db, $query, $name, $name2, &$value, $extra, $size, $blank="") {
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

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    if($name == "PASS_UNPASS") {
        $opt[] = array("label" => "不合格", "value" => "UNPASS");
        if($value != "UNPASS"){
            $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
        }
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    }

    $dataRow[$name] = knjCreateCombo($objForm, $name2, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg, $updflg) {
    $disable  = ($dataflg) ? "" : " disabled";

    //読込ボタン
    $extra = "onclick=\"return btn_submit('search');\"";
    $arg["btn_search_left"] = knjCreateBtn($objForm, "btn_search_left", "読 込", $extra);
    $extra = "onclick=\"return btn_submit('search');\"";
    $arg["btn_search_right"] = knjCreateBtn($objForm, "btn_search_right", "読 込", $extra);
    //更新ボタン
    if($updflg){
        $extra = "onclick=\"return btn_submit('update');\"";
    } else {
        $extra = "onclick=\"return btn_submit('update');\"  disabled";
    }
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
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL571A");
}
?>
