<?php

require_once('for_php7.php');

class knjl041qForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl041qQuery::getNameMst("L003", $model->year);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        if (SCHOOLKIND == "P") {
            $query = knjl041qQuery::getNameMst("LP24", $model->year);
        } else if (SCHOOLKIND == "J") {
            $query = knjl041qQuery::getNameMst("L024", $model->year);
        } else {
            $query = knjl041qQuery::getNameMst("L004", $model->year);
        }
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //学校名称変更
        if (SCHOOLKIND == "P") {
            $arg["FINSCHOOL_LABEL"] = "園名";
        } else if (SCHOOLKIND == "J") {
            $arg["FINSCHOOL_LABEL"] = "小学校名";
        } else {
            $arg["FINSCHOOL_LABEL"] = "中学校名";
        }

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {

            //データ取得
            $result = $db->query(knjl041qQuery::SelectQuery($model));

            //データなし
            if ($result->numRows() == 0) {
               $model->setMessage("MSG303");
            }

            //データ表示
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["RECEPTNO"] . "-" . $row["EXAMNO"];

                //欠席チェックボックス
                $disJdg = ($row["JUDGEDIV"] == "" || $row["JUDGEDIV"] == "4") ? "" : " disabled";
                $chkJdg = ($row["JUDGEDIV"] == "4") ? " checked" : "";
                $extra = "onclick=\"bgcolorYellow('{$row["RECEPTNO"]}', '{$row["EXAMNO"]}', this);\"" .$disJdg.$chkJdg;
                $row["CHK_DATA"] = knjCreateCheckBox($objForm, "CHK_DATA"."-".$row["RECEPTNO"], "4", $extra);

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_RECEPTNO");
        knjCreateHidden($objForm, "HID_EXAMNO2");

        knjCreateHidden($objForm, "NEXT_ID");//更新後カーソルセット用
        knjCreateHidden($objForm, "SET_SC_VAL");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl041qindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //カーソルセット用
        if ($model->cmd == "read") {
            $arg["setTarget"] = " setCursor('$model->nextId', '$model->set_sc_val');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl041qForm1.html", $arg);
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
        if ($row["NAMESPARE2"] && $default_flg) {
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
