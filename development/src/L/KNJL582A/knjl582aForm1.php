<?php

require_once('for_php7.php');

class knjl582aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試区分
        $query = knjl582aQuery::getNameMst($model->ObjYear, "L004");
        $extra = "onchange=\"return btn_submit('main');\" tabindex=1";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //志望区分
        $query = knjl582aQuery::getHopeCourse($model->ObjYear);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=2";
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->field["DESIREDIV"], $extra, 1, "blank");

        //表示順 (1:受験番号順 2:名前順)
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //入力項目 (1:入学一時金 2:入学金 3:入学決定)
        $opt = array(1, 2, 3);
        $extra = array();
        $model->field["DIV"] = ($model->field["DIV"] == "") ? "1" : $model->field["DIV"];
        for ($idx = 1; $idx <= 3; $idx++) {
            array_push($extra , "id=\"DIV".$idx."\" onchange=\"return btn_submit('reload');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        if($model->cmd == "main"){
            $model->field["S_EXAMNO"] = "";
            $model->field["E_EXAMNO"] = "";
        }

        //受験番号範囲
        $query = knjl582aQuery::SelectFstExamno($model);
        $fstExamno =  $db->getOne($query);
        $query = knjl582aQuery::SelectLstExamno($model);
        $lstExamno =  $db->getOne($query);
        $model->field["S_EXAMNO"] = ($model->field["S_EXAMNO"]) ? sprintf("%05d",$model->field["S_EXAMNO"]) : "";
        $model->field["E_EXAMNO"] = ($model->field["E_EXAMNO"]) ? sprintf("%05d",$model->field["E_EXAMNO"]) : "";

        if($model->cmd != "reload"){
            if ($model->field["E_EXAMNO"] != "" && $model->field["S_EXAMNO"] > $model->field["E_EXAMNO"]) {
                $chgwk = $model->field["E_EXAMNO"];
                $model->field["E_EXAMNO"] = $model->field["S_EXAMNO"];
                $model->field["S_EXAMNO"] = $chgwk;
            }
        }

        $arr_examno = array();
        if($model->cmd == "search" || $model->cmd == "reload" || $model->cmd == "reset" || $model->cmd == "back" || $model->cmd == "next"){

            //データ一覧取得
            $query = knjl582aQuery::SelectQuery($model);
            $result = $db->query($query);
            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }
            //一覧表示
            $arr_examno = array();
            $dataflg = false;
            $counter = 0;
            $valueFlg = false;
            if($model->cmd == "search" || $model->cmd == "reset" || $model->cmd == "back" || $model->cmd == "next") $valueFlg = true;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                if($model->cmd != "reload"){
                    $model->field["S_EXAMNO"] = $counter == 0 ? $row["EXAMNO"] : $model->field["S_EXAMNO"];
                    $model->field["E_EXAMNO"] = $row["EXAMNO"];
                }

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //背景色の設定
                $row["BGCOLOR"] = "white";

                $dataName = "REMARK1_".$row["EXAMNO"];
                if ($model->field["DIV"] == "2"){
                    $dataName = "PROCEDUREDIV_".$row["EXAMNO"];
                } else if ($model->field["DIV"] == "3"){
                    $dataName = "ENTDIV_".$row["EXAMNO"];
                }

                $dname = " data-name=\"".$dataName."\" ";
                //$dbefore = "data-befcolor=\"".$row["BGCOLOR"]."\" ";
                $dbefore = "";

                // 背景色設定
                if ($model->cmd == "reload") {
                    if ($model->field["DIV"] == "1" && $model->data["REMARK1"][$row["EXAMNO"]] == "1"
                    || $model->field["DIV"] == "2" && $model->data["PROCEDUREDIV"][$row["EXAMNO"]] == "1"
                    || $model->field["DIV"] == "3" && $model->data["ENTDIV"][$row["EXAMNO"]] == "2") {
                        $row["BGCOLOR"] = "yellow";
                    }
                } else {
                    if ($model->field["DIV"] == "1" && $row["REMARK1"] == "1"
                    || $model->field["DIV"] == "2" && $row["PROCEDUREDIV"] == "1"
                    || $model->field["DIV"] == "3" && $row["ENTDIV"] == "2") {
                        $row["BGCOLOR"] = "yellow";
                    }
                }

                //入学一時金
                $val = ($valueFlg) ? $row["REMARK1"] : $model->data["REMARK1"][$row["EXAMNO"]];
                $disabled = "";
                if($model->field["DIV"] != "1"){
                    $disabled = " disabled";
                    knjCreateHidden($objForm, "HID_REMARK1_".$row["EXAMNO"], $val);
                }
                $checked = "";
                if ($row["REMARK1"] == "1") {
                    $checked = " checked ";
                }
                if($model->cmd == "reload") $checked = ($model->data["REMARK1"][$row["EXAMNO"]] == "1") ? " checked": "";
                $extra = "id=\"REMARK1_{$row["EXAMNO"]}\" ".$checked.$disabled.$dname.$dbefore;
                $row["REMARK1"] = knjCreateCheckBox($objForm, "REMARK1_".$row["EXAMNO"], "1", $extra);

                //入学金
                $val = ($valueFlg) ? $row["PROCEDUREDIV"] : $model->data["PROCEDUREDIV"][$row["EXAMNO"]];
                $disabled = "";
                if($model->field["DIV"] != "2"){
                    $disabled = " disabled";
                    knjCreateHidden($objForm, "HID_PROCEDUREDIV_".$row["EXAMNO"], $val);
                }
                $checked = ($row["PROCEDUREDIV"] == "1") ? " checked": "";
                if($model->cmd == "reload") $checked = ($model->data["PROCEDUREDIV"][$row["EXAMNO"]] == "1") ? " checked": "";
                $extra = "id=\"PROCEDUREDIV_{$row["EXAMNO"]}\" onchange=\"return procedureChk('{$model->field["DIV"]}','{$row["EXAMNO"]}');\" ".$checked.$disabled.$dname.$dbefore;
                $row["PROCEDUREDIV"] = knjCreateCheckBox($objForm, "PROCEDUREDIV_".$row["EXAMNO"], "1", $extra);

                //辞退
                $val = ($valueFlg) ? $row["ENTDIV"] : $model->data["ENTDIV"][$row["EXAMNO"]];
                $disabled = "";
                if($model->field["DIV"] != "3"){
                    $disabled = " disabled";
                    knjCreateHidden($objForm, "HID_ENTDIV_".$row["EXAMNO"], $val);
                }
                $checked = ($row["ENTDIV"] == "2") ? " checked": "";
                if($model->cmd == "reload") $checked = ($model->data["ENTDIV"][$row["EXAMNO"]] == "2") ? " checked": "";
                $extra = "id=\"ENTDIV_{$row["EXAMNO"]}\" ".$checked.$disabled.$dname.$dbefore;
                $row["ENTDIV"] = knjCreateCheckBox($objForm, "ENTDIV_".$row["EXAMNO"], "2", $extra);

                //レコードの設定
                $row["CHECK_NUM"] = $dataName;
                $row["backcolor"] = $row["BGCOLOR"];

                $dataflg = true;

                $arg["data"][] = $row;
                $counter++;
            }
        }

        //受験番号
        $extra = " onchange=\"this.value=toAlphaNumber(this.value);\" tabindex=3";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->field["S_EXAMNO"], "S_EXAMNO", 5, 5, $extra);
        $extra = " onchange=\"this.value=toAlphaNumber(this.value);\" tabindex=4";
        $model->field["E_EXAMNO"] = ($model->field["E_EXAMNO"]) ? sprintf("%05d",$model->field["E_EXAMNO"]) : "";
        $arg["TOP"]["E_EXAMNO"] = $model->field["E_EXAMNO"];
        knjCreateHidden($objForm, "E_EXAMNO", $model->field["E_EXAMNO"]);

        $fsthidden = $fstExamno == $model->field["S_EXAMNO"] ? " disabled " : "";
        $extra = "onClick=\"btn_submit('back');\" tabindex=6".$fsthidden;
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        $lsthidden = $lstExamno == $model->field["E_EXAMNO"] ? " disabled " : "";
        $extra = "onClick=\"btn_submit('next');\" tabindex=7".$lsthidden;
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl582aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl582aForm1.html", $arg);
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg) {
    $disable  = ($dataflg) ? "" : " disabled";
    
    //読込ボタン
    $extra = "onclick=\"return btn_submit('search');\" tabindex=5";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "読 込", $extra);
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
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL582A");
}
?>
