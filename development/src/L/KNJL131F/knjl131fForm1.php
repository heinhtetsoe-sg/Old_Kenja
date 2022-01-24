<?php

require_once('for_php7.php');


class knjl131fForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        if ($model->cmd == 'print') {
            $arg["print"] = "newwin('" . SERVLET_URL . "', '{$model->applicantdiv}', '{$model->finschoolcd}', '{$model->printExamNo}')";
        }

        //入試制度コンボ
        $extra = "onChange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl131fQuery::getNameMst("L003", $model->year);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //出身学校コンボ
        $extra = "onChange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl131fQuery::getFinSchool($model);
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOLCD", $model->finschoolcd, $extra, 1, "", '0000000');

        //印刷状態チェック
        $chk = ($model->printZumi == "1") ? " checked" : "";
        $extra = "id=\"PRINT_ZUMI\" onClick=\"btn_submit('');\"";
        $arg["TOP"]["PRINT_ZUMI"] = knjCreateCheckBox($objForm, "PRINT_ZUMI", "1", $extra.$chk);

        //印刷ALLチェック
        $extra = "onClick=\"chkDataALL(this);\"";
        $arg["TOP"]["PRINT_ALL"] = knjCreateCheckBox($objForm, "PRINT_ALL", "1", $extra);

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->finschoolcd != "") {

            //データ取得
            $result = $db->query(knjl131fQuery::SelectQuery($model));

            //データ表示
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["RECEPTNO"] . "-" . $row["EXAMNO"];

                //指導要録受領チェック
                $checked = $row["GET_YOUROKU"] == "1" ? " checked " : "";
                $extra = "onClick=\"bgcolorYellow(this, '{$row["RECEPTNO"]}');\"";
                $row["YOUROKU_DATA"] = knjCreateCheckBox($objForm, "YOUROKU_DATA"."-".$row["RECEPTNO"], "1", $extra.$checked);

                //健康診断受領チェック
                $checked = $row["GET_MEDEXAM"] == "1" ? " checked " : "";
                $extra = "onClick=\"bgcolorYellow(this, '{$row["RECEPTNO"]}');\"";
                $row["MEDEXAM_DATA"] = knjCreateCheckBox($objForm, "MEDEXAM_DATA"."-".$row["RECEPTNO"], "1", $extra.$checked);

                //印刷チェック
                $disabled = $row["GET_YOUROKU"] == "1" && $row["GET_MEDEXAM"] == "1" ? "" : " disabled ";
                $extra = "onClick=\"printBtnColor();\"";
                $row["PRINT_DATA"] = knjCreateCheckBox($objForm, "PRINT_DATA"."-".$row["RECEPTNO"], "1", $extra.$disabled);

                //印刷状態
                $row["PRINT_STATE"] = $row["PRINTFLG"] == "1" ? "済" : "";

                //hidden
                knjCreateHidden($objForm, "DEF_YOUROKU-".$row["RECEPTNO"], $row["GET_YOUROKU"]);
                knjCreateHidden($objForm, "DEF_MEDEXAM-".$row["RECEPTNO"], $row["GET_MEDEXAM"]);

                $arg["data"][] = $row;
            }
        }

        //印刷日
        $model->printDate = $model->printDate ? $model->printDate : CTRL_DATE;
        $arg["PRINT_DATE"] = View::popUpCalendar($objForm, "PRINT_DATE", str_replace("-","/",$model->printDate),"");

        //ボタン作成
        makeBtn($objForm, $arg, $arr_examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl131findex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl131fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $subeteCd = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($subeteCd != "") {
        $opt[] = array("label" => "-- 全て --", "value" => $subeteCd);
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
function makeBtn(&$objForm, &$arg, $arr_examno) {
    //更新ボタン
    $disBtn = (0 < get_count($arr_examno)) ? "" : " disabled";
    $extra = "onClick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disBtn);
    //取消ボタン
    $extra = "onClick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //更新/印刷ボタン
    $disBtn = (0 < get_count($arr_examno)) ? "" : " disabled";
    $extra = "onClick=\"return btn_submit('updatePrint');\"";
    $arg["btn_updatePrint"] = knjCreateBtn($objForm, "btn_updatePrint", "更 新／プレビュー／印刷", $extra.$disBtn);
    //終了ボタン
    $extra = "onClick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "changeCnt", 0);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_FINSCHOOLCD");
    knjCreateHidden($objForm, "HID_PRINT_ZUMI");
    knjCreateHidden($objForm, "PRINT_EXAMNO");
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->year);
    knjCreateHidden($objForm, "PRGID", "KNJL131F");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
}
?>
