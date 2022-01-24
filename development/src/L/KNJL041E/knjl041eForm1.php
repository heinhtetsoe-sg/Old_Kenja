<?php

require_once('for_php7.php');

class knjl041eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl041eQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl041eQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //推薦フラグセット
        $model->suisenFlg = false;
        $nameCdL004Row = $db->getRow(knjl041eQuery::getNameMst($model->ObjYear, "L004", $model->testdiv), DB_FETCHMODE_ASSOC);

        //推薦フラグ
        if ($nameCdL004Row["NAMESPARE1"] == "1") $model->suisenFlg = true;

        //会場区分セット
        $model->examType = ($model->suisenFlg) ? '1': '2';//1:面接会場 2:試験会場

        //会場コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl041eQuery::getExamHallCd($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallCd, $extra, 1, "");

        //一覧表示
        $arr_ExamNo = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->examhallCd != "") {
            //データ取得
            $query = knjl041eQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_ExamNo[] = $row["EXAMNO"];

                //試験欠席checkbox
                $disJdg = ($row["JUDGEMENT"] == "" || $row["JUDGEMENT"] == "3" || $row["JUDGEMENT"] == "0") ? "" : " disabled";
                $checked1 = ($row["JUDGEMENT"] == "3") ? " checked": "";
                $extra = "id=\"TEST_ABSENCE-{$row["EXAMNO"]}\" onClick=\"CheckOn(this, '1')\"".$checked1.$disJdg;
                $row["TEST_ABSENCE"] = knjCreateCheckBox($objForm, "TEST_ABSENCE-".$row["EXAMNO"], "3", $extra);

                //出欠値(JUDGEMENT)内部保持
                knjCreateHidden($objForm, "BAK_ABSENCE-".$row["EXAMNO"], $row["JUDGEMENT"]);

                //背景色
                $row["BGCOLOR"] = "#ffffff";

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'pdf');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //CSVボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_ExamNo));
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_EXAMHALLCD");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL041E");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl041eindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl041eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    if ($name == "EXAMHALLCD") $opt[] = array("label" => "-- 全て --", "value" => "9999");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;

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
