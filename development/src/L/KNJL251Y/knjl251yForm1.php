<?php

require_once('for_php7.php');


class knjl251yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db           = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl251yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl251yQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        $extra = "onClick=\"btn_submit('read');\" tabindex=-1";
        $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読込み", $extra);
        $extra = "onClick=\"btn_submit('back');\" tabindex=-1";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        $extra = "onClick=\"btn_submit('next');\" tabindex=-1";
        $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //受験番号範囲
        $extra = " onblur=\"this.value=toAlphaNumber(this.value);\" tabindex=-1";
        $model->field["S_EXAMNO"] = ($model->field["S_EXAMNO"]) ? sprintf("%05d",$model->field["S_EXAMNO"]) : "";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->field["S_EXAMNO"], "S_EXAMNO", 5, 5, $extra);

        //本人、保護者ラジオボタン
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onclick=\"return btn_submit('kirikae');\"", "id=\"OUTPUT2\" onclick=\"return btn_submit('kirikae');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;
        
        //色を切替
        //本人用
        if ($model->field["OUTPUT"] == "1") {
            $arg["HONNIN"] = "1";
            $arg["BGCOLOR1"] = "#ffffff";
            $arg["BGCOLOR2"] = "darkgray";
        //保護者用
        } else {
            $arg["BGCOLOR1"] = "darkgray";
            $arg["BGCOLOR2"] = "#ffffff";
        }
        
        //一覧表示
        $model->data = array();
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->field["S_EXAMNO"] != "" && ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next" || $model->cmd == "kirikae")) {
            //データ取得
            $searchCnt = $db->getOne(knjl251yQuery::SelectQuery($model, "COUNT"));
            $checkCnt = 50;
            if ($searchCnt == 0) {
                $model->setMessage("MSG303");
            } else {
                $counter = 0;
                $result    = $db->query(knjl251yQuery::SelectQuery($model, ""));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");
                    $model->field["S_EXAMNO"] = $counter == 0 ? $row["EXAMNO"] : $model->field["S_EXAMNO"];
                    $model->field["E_EXAMNO"] = $row["EXAMNO"];
                    //HIDDENに保持する用
                    $arr_receptno[] = $row["EXAMNO"];
                    //受験番号を配列で取得
                    $model->data["EXAMNO"][] = $row["EXAMNO"];
                    //対象データをセット
                    $model->fields["INTERVIEW_REMARK"][$counter] = $row["INTERVIEW_REMARK"];
                    //本人用のみ評価を表示
                    if ($model->field["OUTPUT"] == "1") {
                        //対象データをセット
                        $model->fields["INTERVIEW_VALUE"][$counter] = $row["INTERVIEW_VALUE"];
                        //評価
                        $opt = array();
                        $opt[] = array('label' => "", 'value' => "");
                        $query = knjl251yQuery::getHyouteiData();
                        $resultCombo = $db->query($query);
                        while ($comboRow = $resultCombo->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $opt[] = array('label' => $comboRow["LABEL"],
                                           'value' => $comboRow["VALUE"]);
                        }
                        $extra = "OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\"";
                        $row["INTERVIEW_VALUE"] = knjCreateCombo($objForm, "INTERVIEW_VALUE-".$counter, $model->fields["INTERVIEW_VALUE"][$counter], $opt, $extra, 1);
                    }
                    //行動の観察
                    $extra = "onChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\"";
                    $row["INTERVIEW_REMARK"] = knjCreateTextBox($objForm, $model->fields["INTERVIEW_REMARK"][$counter], "INTERVIEW_REMARK-".$counter, 100, 150, $extra);
                    
                    $arg["data"][] = $row;
                    $counter++;
                }
            }
        }
        
        //座席番号至
        $arg["TOP"]["E_EXAMNO"] = $model->field["E_EXAMNO"];
        knjCreateHidden($objForm, "E_EXAMNO", $model->field["E_EXAMNO"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl251yindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl251yForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL251Y");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
