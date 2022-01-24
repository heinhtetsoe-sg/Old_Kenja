<?php

require_once('for_php7.php');

class knjl050qForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl050qQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        if (SCHOOLKIND == "P") {
            $namecd1 = "LP24";
        } else if (SCHOOLKIND == "J") {
            $namecd1 = "L024";
        } else {
            $namecd1 = "L004";
        }
        $query = knjl050qQuery::getNameMst($namecd1, $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //受験科目コンボボックス
        if (SCHOOLKIND == "P") {
            $field = "NAME3";
        } else if (SCHOOLKIND == "J") {
            $field = "NAME2";
        } else {
            $field = "NAME1";
        }
        $query = knjl050qQuery::getTestSubclasscd($model, $field);
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //入力回数ラジオボタン
        $opt = array(1, 2);
        $model->field["NYURYOKU"] = ($model->field["NYURYOKU"] == "") ? "1" : $model->field["NYURYOKU"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"NYURYOKU{$val}\" onClick=\"btn_submit('main')\"");
        }
        $radioArray = knjCreateRadio($objForm, "NYURYOKU", $model->field["NYURYOKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        if (SCHOOLKIND == "H") {
            //一般入試選択で「基準テストを含める・含めない・のみ」ラジオボタン
            if ($model->testdiv == "5") {
                $arg["isIppan"] = ($model->testdiv == "5") ? 1 : "";
                $opt = array(1, 2, 3);
                $model->field["KIJUN_TEST_DIV"] = ($model->field["KIJUN_TEST_DIV"] == "") ? "1" : $model->field["KIJUN_TEST_DIV"];
                $extra = array();
                foreach($opt as $key => $val) array_push($extra, " id=\"KIJUN_TEST_DIV{$val}\" onClick=\"btn_submit('main')\"");
                $radioArray = knjCreateRadio($objForm, "KIJUN_TEST_DIV", $model->field["KIJUN_TEST_DIV"], $extra, $opt, get_count($opt));
                foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;
            }
        }

        //一覧表示
        $arr_receptno = array();
        $model->hidscore = array();
        $model->hidscore2 = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->testsubclasscd != "") {

            list ($coursecd, $majorcd, $examcourse) = preg_split("/-/", $model->examcourse);

            //データ取得
            $result = $db->query(knjl050qQuery::SelectQuery($model, ""));
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }
            $count = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $model->s_receptno = $count == 0 ? $row["RECEPTNO"] : $model->s_receptno;
                $model->e_receptno = $row["RECEPTNO"];

                if ($row["ATTEND_FLG"] === '0') $row["SCORE"] = $row["SCORE2"] = '*';

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                //満点チェック用
                $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => (int)$row["PERFECT"]);

                //背景色
                if (($row["SCORE"] != $row["SCORE2"]) && !($row["SCORE"] == "" || $row["SCORE2"] == "")) {
                    $row["BGCOLOR"] = "pink";
                } else {
                    $row["BGCOLOR"] = "#ffffff";
                }

                if ($model->field["NYURYOKU"] == "1") {
                    //得点テキストボックス
                    $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row["SCORE"];
                    if ($row["ATTEND_FLG"] == '0') $value = '*';    //*の表示
                    $extra = " OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;background-color:".$row["BGCOLOR"]."\" onblur=\"CheckScore(this);\" onKeyDown=\"goEnter(this)\"";
                    $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE[]", 3, 3, $extra);
                    $model->hidscore2[] = $row["SCORE2"];
                } else if ($model->field["NYURYOKU"] == "2") {
                    //得点テキストボックス２
                    $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row["SCORE2"];
                    if ($row["ATTEND_FLG"] == '0') $value = '*';    //*の表示
                    $extra = " OnChange=\"Setflg(this, 2);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;background-color:".$row["BGCOLOR"]."\" onblur=\"CheckScore(this);\" onKeyDown=\"goEnter(this)\"";
                    $row["SCORE2"] = knjCreateTextBox($objForm, $value, "SCORE2[]", 3, 3, $extra);
                    $model->hidscore[] = $row["SCORE"];
                }

                $arg["data"][] = $row;
                $count++;
            }
        }

        //受験番号セット
        knjCreateHidden($objForm, "s_receptno", $model->s_receptno);
        knjCreateHidden($objForm, "e_receptno", $model->e_receptno);

        //5000件以上の場合、切換ボタンで切換
        $getBacCount  = $db->getOne(knjl050qQuery::SelectQuery($model, "BAC_COUNT"));
        $getNextCount = $db->getOne(knjl050qQuery::SelectQuery($model, "NEXT_COUNT"));
        if ($getBacCount > 0) {
            $extra  = "onClick=\"btn_submit('back');\" tabindex=-1 ";
            $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        }
        if ($getNextCount > 0) {
            $extra = "onClick=\"btn_submit('next');\" tabindex=-1 ";
            $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);
        }

        //ボタン作成
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV0");
        knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
        knjCreateHidden($objForm, "HID_EXAMCOURSE");
        knjCreateHidden($objForm, "HID_RECEPTNO2");
        knjCreateHidden($objForm, "HID_SCORE");
        knjCreateHidden($objForm, "NEXT_ID");//更新後カーソルセット用
        knjCreateHidden($objForm, "ENTER_FLG");
        knjCreateHidden($objForm, "SET_SC_VAL");
        knjCreateHidden($objForm, "HID_SCHOOLKIND", SCHOOLKIND);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL050Q");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050qindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "read") {
            $arg["setTarget"] = " setCursor('$model->nextId', '$model->set_sc_val', '$model->enter_flg');";
            $model->enter_flg = "";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl050qForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
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
