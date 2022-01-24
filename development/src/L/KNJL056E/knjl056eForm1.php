<?php

require_once('for_php7.php');

class knjl056eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl056eQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分
        $query = knjl056eQuery::getNameMst($model->ObjYear, "L004");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //会場コンボボックス
        $extra = " onChange=\"return btn_submit('main');\"";
        $query = knjl056eQuery::getHallData($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "BLANK");

        //科目コード
        $query = knjl056eQuery::getNameMst($model->ObjYear, "L009");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->subclasscd, $extra, 1, "BLANK");

        //入試区分
        $query = knjl056eQuery::getNameMst($model->ObjYear, "L027");
        $extra = "onChange=\"return btn_submit('main')\"";
        $setL027 = makeCmb($objForm, $arg, $db, $query, "L027", $val, $extra, 1, "BLANK", "retOpt");

        //一覧表示
        $model->examnoArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->examhallcd != "" && $model->subclasscd != "") {
            //データ取得
            $query = knjl056eQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examNo = $row["EXAMNO"];

                $model->examnoArray[] = $row["EXAMNO"];

                //得点
                $extra = "style=\"text-align:right\" onkeydown=\"keyChangeEntToTab(this);\" onchange=\"changeScore();\" onblur=\"this.value=toInteger(this.value);\"";
                $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], "SCORE_{$examNo}", 3, 3, $extra);

                $dataflg = true;

                $arg["data"][] = $row;
            }

        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg, $examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl056eindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjl056eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $retDiv="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "L027") {
            $row["LABEL"] = $row["NAME1"];
        }
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

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $opt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg, $examno) {
    $disable  = ($dataflg) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷
    $extra = "onclick=\"return newwin('".SERVLET_URL."');\"".$disable;
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CHANGE_SCORE");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL056E");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
}
?>
