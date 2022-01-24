<?php

require_once('for_php7.php');


class knjl053fForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl053fQuery::getNameMst("L003", $model->year, "1");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl053fQuery::getNameMst("L024", $model->year);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //特別入試対象者は除くチェックボックス
        //第５回の時、表示する。
        if ($model->testdiv == "5") {
            $chkUnShow = ($model->chkUnShow == "on") ? " checked" : "";
            $extra  = " id=\"CHK_UN_SHOW\"" .$chkUnShow;
            $extra .= " onchange=\"return btn_submit('main');\"";
            $arg["TOP"]["CHK_UN_SHOW"] = knjCreateCheckBox($objForm, "CHK_UN_SHOW", "on", $extra);
        }

        //特別措置者(インフルエンザ)
        $extra = "id=\"SPECIAL_REASON_DIV\" onchange=\"return btn_submit('main');\" tabindex=-1 ";
        $extra .= strlen($model->special_reason_div) ? "checked='checked' " : "";
        $arg["TOP"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //初期化
        if ($model->cmd == "app") {
            $model->s_receptno = "";
            $model->e_receptno = "";
        }

        //開始受験番号テキストボックス
        $extra = " onChange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["S_RECEPTNO"] = knjCreateTextBox($objForm, $model->s_receptno, "S_RECEPTNO", 4, 4, $extra);

        //終了受験番号テキストボックス
        $extra = " onChange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["E_RECEPTNO"] = knjCreateTextBox($objForm, $model->e_receptno, "E_RECEPTNO", 4, 4, $extra);

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {

            //データ取得
            $result = $db->query(knjl053fQuery::SelectQuery($model));

            //データなし
            if ($result->numRows() == 0) {
               $model->setMessage("MSG303");
            }

            //データ表示
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["RECEPTNO"] . "-" . $row["EXAMNO"];

                //第１志望チェック
                $chkFlg = ($row["SH_FLG"] == "1") ? " checked" : "";
                $extra = "" .$chkFlg;
                $row["SH_FLG"] = knjCreateCheckBox($objForm, "SH_FLG"."-".$row["RECEPTNO"], "1", $extra);

                for ($i = 1; $i <= 6; $i++) {
                    //受験校テキスト
                    $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
                    $name = "SH_SCHOOLNAME{$i}";
                    $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$row["RECEPTNO"], 40, 60, $extra);
                    //合否コンボ
                    //$extra = "";
                    $name = "SH_JUDGEMENT{$i}";
                    $query = knjl053fQuery::getNameMstL035("L035", $model->year);
                    $row[$name] = makeCmbReturn($objForm, $arg, $db, $query, $name."-".$row["RECEPTNO"], $row[$name], $extra, 1, "BLANK");
                }

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl053findex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl053fForm1.html", $arg);
    }
}

//コンボ作成2
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
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
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
    //読込ボタン
    $extra = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
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
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
}
?>
