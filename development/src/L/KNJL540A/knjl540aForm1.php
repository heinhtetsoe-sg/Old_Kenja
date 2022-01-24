<?php

require_once('for_php7.php');

class knjl540aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl540aQuery::getNameMst($model->ObjYear, 'L004');
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //志望区分コンボボックス
        $model->hopeCourseCode = (($model->hopeCourseCode != "") ? $model->hopeCourseCode : $model->CONST_SELALL);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl540aQuery::getHopeCourseCd($model);
        makeCmb($objForm, $arg, $db, $query, "HOPE_COURSECODE", $model->hopeCourseCode, $extra, 1, "SELALL");

        //検索する受験番号
        $extra = " tabindex=-1";
        $arg["TOP"]["SEARCH_EXAMNO"] = knjCreateTextBox($objForm, $model->searchExamNo, "SEARCH_EXAMNO", 5, 5, $extra);
        //検索ボタン
        $extra = "onclick=\"return btn_submit('main');\"";
        $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);

        //一覧表示
        $arr_ReceptNo = array();
        if ($model->testdiv != "" && $model->hopeCourseCode != "") {
            //データ取得
            $query = knjl540aQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_ReceptNo[] = $row["RECEPTNO"];

                if ($row["JUDGEDIV"] == "4") {
                    $row["BGCOLOR"] = "yellow";
                } else {
                    $row["BGCOLOR"] = "white";
                }
                //試験欠席checkbox
                $disJdg = ($row["JUDGEDIV"] == "" || $row["JUDGEDIV"] == "4") ? "" : " disabled";
                $checked1 = ($row["JUDGEDIV"] == "4") ? " checked": "";
                $dname = " data-name=\"TEST_ABSENCE-".$row["RECEPTNO"]."\" ";
                $dbefore = "data-befcolor=\"".$row["BGCOLOR"]."\" ";
                $extra = "id=\"TEST_ABSENCE-{$row["RECEPTNO"]}\" ".$checked1.$disJdg.$dname.$dbefore;
                $row["TEST_ABSENCE"] = knjCreateCheckBox($objForm, "TEST_ABSENCE-".$row["RECEPTNO"], "4", $extra);
                $row["CHECK_NUM"] = "TEST_ABSENCE-".$row["RECEPTNO"];
                $row["backcolor"] = $row["BGCOLOR"];

                $arg["data"][] = $row;
            }
        }

        //出身中学校
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl540aQuery::SelectQuery($model, "FISCH_ONLY");
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOLCD", $model->finSchoolCd, $extra, 1, "SELALL");

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

        //プレビュー/印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_ReceptNo));
        knjCreateHidden($objForm, "HID_HOPE_COURSECODE");
        knjCreateHidden($objForm, "HID_TESTDIV");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL540A");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl540aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl540aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $default_flg = true;
    $value_flg = false;
    $force_setflg = false;
    $i = $default = 0;
    $selallval = "99999";    //引数を増やすとなぜか動作不安定となるようなので、固定値を設定
    
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    } else if ($blank == "SELALL") {
        //リストに全てを設定
        $opt[] = array("label" => "全て", "value" => $selallval);
        //先に"全て"が設定されていたら、下記のSQLデータ取得ループでは"見つかったもの"としてフラグ設定する
        $value_flg = ($value == $selallval ? true : $value_flg);
        //$default_flgも不要。
        $default_flg = ($value_flg ? false : $default_flg);
        //先に"全て"が選択されていたら、SQLには"全て"が無いので、強制フラグをセットする。
        $force_setflg = ($value_flg ? true : false);
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }

    $result->free();
    $value = ($force_setflg || (!$force_setflg && $value && $value_flg)) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
