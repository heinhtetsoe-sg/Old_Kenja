<?php

require_once('for_php7.php');

class knjl050aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //受験校種
        $query = knjl050aQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onChange=\"return btn_submit('clr')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //試験区分
        $query = knjl050aQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('clr')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //試験科目ラジオボタン
        $extra = " onClick=\"return btn_submit('set');\" tabindex=-1";
        $query = knjl050aQuery::getName2ForMst($model->ObjYear, 'L009');
        makeRjBtn($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK");

        //初期化
        if (in_array($model->cmd, array("main", "clr", "set"))) {
            $model->s_receptno = "";
            $model->e_receptno = "";
        }

        //一覧表示
        $arr_receptno = array();
        $s_receptno = $e_receptno = "";
        $receptno = array();
        $dataflg = false;
        if (in_array($model->cmd, array("read", "back", "next", "reset"))) {
            if ($model->testdiv != "" && $model->testsubclasscd != "") {
                //データ取得
                $query = knjl050aQuery::SelectQuery($model, "list");
                $result = $db->query($query);

                //データが1件もなかったらメッセージを返す
                if ($result->numRows() == 0 ) {
                    $model->setWarning("MSG303");
                }

                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    //HIDDENに保持するための処理
                    $arr_receptno[] = $row["RECEPTNO"];

                    //満点チェック用
                    $arg["data2"][] = array("key" => $row["RECEPTNO"], "perf" => 100);

                    //得点テキストボックス
                    $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]] : $row["SCORE"];
                    $extra  = "";
                    $extra .= " OnChange=\"Setflg(this);\" id=\"".$row["RECEPTNO"]."\" style=\"text-align:right;\" onblur=\"CheckScore(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                    $row["SCORE"] = knjCreateTextBox($objForm, $value, "SCORE_".$row["RECEPTNO"], 3, 3, $extra);

                    //得点更新フラグ　Setflg()で更新フラグ"1"を立てる
                    knjCreateHidden($objForm, "UPD_FLG_".$row["RECEPTNO"], "");

                    //開始受験番号
                    if ($s_receptno == "") $s_receptno = $row["RECEPTNO"];
                    $e_receptno = $row["RECEPTNO"];
                    $dataflg = true;

                    $arg["data"][] = $row;
                }

                //受験番号の最大値・最小値取得
                $recept_array = $db->getCol(knjl050aQuery::SelectQuery($model, "receptno"));
                $receptno["min"] = $recept_array[0];
                $receptno["max"] = end($recept_array);

                //初期化
                if (in_array($model->cmd, array("next", "back")) && $dataflg) {
                    $model->e_receptno = "";
                    $model->s_receptno = "";
                }
            }
        }

        //開始受験番号
        if ($s_receptno) $model->s_receptno = $s_receptno;
        $extra="";
        $arg["TOP"]["S_RECEPTNO"] = knjCreateTextBox($objForm, $model->s_receptno, "S_RECEPTNO", 10, 10, $extra);

        //終了受験番号
        if ($e_receptno) $model->e_receptno = $e_receptno;
        $extra="";
        $arg["TOP"]["E_RECEPTNO"] = knjCreateTextBox($objForm, $model->e_receptno, "E_RECEPTNO", 10, 10, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg, $receptno);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl050aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl050aForm1.html", $arg);
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg, $receptno) {
    //読込みボタン
    $extra = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    //読込ボタン（前の受験番号検索）
    $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('back');\"";
    $extra .= ($receptno["min"] != $model->s_receptno) ? "" : " disabled";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
    //読込ボタン（後の受験番号検索）
    $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('next');\"";
    $extra .= ($receptno["max"] != $model->e_receptno) ? "" : " disabled";
    $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

    $disable  = (in_array($model->cmd, array("read", "back", "next", "reset")) && $dataflg) ? "" : " disabled";

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

//ラジオボタン作成
function makeRjBtn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank=""){
    $radioValue = array(1, 2, 3, 4, 5, 6);
    if ($value == "") $value = "1";
    $click = $extra;
    $extrawk = array("id=\"TESTSUBCLASSCD1\"".$click, "id=\"TESTSUBCLASSCD2\"".$click, "id=\"TESTSUBCLASSCD3\"".$click, "id=\"TESTSUBCLASSCD4\"".$click, "id=\"TESTSUBCLASSCD5\"".$click, "id=\"TESTSUBCLASSCD6\"".$click);
    $radioArray = knjCreateRadio($objForm, $name, $value, $extrawk, $radioValue, get_count($radioValue));
    foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;
    
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TOP"]["TESTSUBCLASSLABEL".$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",",$arr_receptno));
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL050A");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
}
?>
