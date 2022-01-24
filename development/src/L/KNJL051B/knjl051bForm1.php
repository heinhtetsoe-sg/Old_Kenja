<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl051bForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl051bindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl051bQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl051bQuery::getNameMst("L004", $model->ObjYear, "1");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        $extra = "onClick=\"btn_submit('read');\" tabindex=-1";
        $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読込み", $extra);
        $extra = "onClick=\"btn_submit('back');\" tabindex=-1";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        $extra = "onClick=\"btn_submit('next');\" tabindex=-1";
        $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //特別措置者(インフルエンザ)
        $extra = "id=\"SPECIAL_REASON_DIV\" onclick=\"return btn_submit('read');\" tabindex=-1 ";
        $extra .= strlen($model->special_reason_div) ? "checked='checked' " : "";
        $arg["TOP"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->field["S_EXAMNO"] != "" && ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next")) {
            //データ取得
            $searchCnt = $db->getOne(knjl051bQuery::SelectQuery($model, "COUNT"));
            $checkCnt = 50;
            if ($searchCnt == 0) {
                $model->setMessage("MSG303");
            } else {
                $result = $db->query(knjl051bQuery::SelectQuery($model, ""));
                $count = 0;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");
                    $model->field["S_EXAMNO"] = $count == 0 ? $row["EXAMNO"] : $model->field["S_EXAMNO"];
                    $model->field["E_EXAMNO"] = $row["EXAMNO"];
                    //HIDDENに保持する用
                    $arr_receptno[] = $row["RECEPTNO"].'-'.$count;
                    //面接
                    $extra = " onPaste=\"return showPaste(this);\" onblur=\"this.value=toInterViewInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                    $row["A_CHECK"] = knjCreateTextBox($objForm, $row["A_CHECK"], "A_CHECK-".$count, 1, 1, $extra);
                    $row["B_CHECK"] = knjCreateTextBox($objForm, $row["B_CHECK"], "B_CHECK-".$count, 1, 1, $extra);
                    $row["C_CHECK"] = knjCreateTextBox($objForm, $row["C_CHECK"], "C_CHECK-".$count, 1, 1, $extra);

                    $arg["data"][] = $row;
                    $count++;
                }
            }
        }

        //受験番号範囲
        $extra = " onblur=\"this.value=toAlphaNumber(this.value);\" tabindex=-1";
        $model->field["S_EXAMNO"] = ($model->field["S_EXAMNO"]) ? sprintf("%05d",$model->field["S_EXAMNO"]) : "";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->field["S_EXAMNO"], "S_EXAMNO", 5, 5, $extra);

        //座席番号至
        $arg["TOP"]["E_EXAMNO"] = $model->field["E_EXAMNO"];
        knjCreateHidden($objForm, "E_EXAMNO", $model->field["E_EXAMNO"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl051bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
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

}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL051B");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
