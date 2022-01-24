<?php

require_once('for_php7.php');

class knjl171dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $opt = array();
        $opt[] = array('value' => CTRL_YEAR,     'label' => CTRL_YEAR);
        $opt[] = array('value' => CTRL_YEAR + 1, 'label' => CTRL_YEAR + 1);
        $model->ObjYear = ($model->ObjYear == "") ? substr(CTRL_DATE, 0, 4): $model->ObjYear;
        $extra = "onChange=\" return btn_submit('main');\"";
        $arg["TOP"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->ObjYear, $opt, $extra, 1);

        //事前チェック（合否）
        $resultcdstr = array();
        $valArray = $labelArray = array();
        $query = knjl171dQuery::getNameMst($model->ObjYear, "L013");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $valArray[]     = $row["VALUE"];
            $labelArray[]   = $row["NAME1"];
            $resultcdstr[] = $row["VALUE"].":".$row["NAME1"];
        }
        if (get_count($valArray) == 0) {
            $arg["pre_check"] = "errorPreCheck();";
        }
        knjCreateHidden($objForm, "VAL_LIST", implode(',', $valArray));
        knjCreateHidden($objForm, "LABEL_LIST", implode(',', $labelArray));

        //合否コード
        $arg["TOP"]["RESULTCODE"] = implode('　', $resultcdstr);

        //入試区分
        $maxTestDiv = $db->getOne(knjl171dQuery::getMaxTestDiv($model->ObjYear));
        $model->testdiv = ($model->testdiv) ? $model->testdiv: $maxTestDiv;
        $query = knjl171dQuery::getTestDivList($model->ObjYear);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        if ($model->cmd == "main") {
            $model->e_examno = "";
            $model->s_examno = "";
        }

        //一覧表示
        $arr_examno = array();
        $examno = array();
        $dataflg = false;
        if ($model->testdiv != "") {
            //データ取得
            $query = knjl171dQuery::SelectQuery($model, "list");
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //合否テキストボックス
                $value = ($model->isWarning()) ? $model->judge[$row["EXAMNO"]] : $row["JUDGEMENT"];
                $extra = " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckValue(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["JUDGEMENT"] = knjCreateTextBox($objForm, $value, "JUDGEMENT_".$row["EXAMNO"], 3, 1, $extra);

                //開始・終了受験番号
                if ($s_examno == "") $s_examno = $row["EXAMNO"];
                $e_examno = $row["EXAMNO"];
                $dataflg = true;

                $arg["data"][] = $row;

            }

            //受験番号の最大値・最小値取得
            $exam_array = $db->getCol(knjl171dQuery::SelectQuery($model, "examno"));
            $examno["min"] = $exam_array[0];
            $examno["max"] = end($exam_array);

            //初期化
            if (in_array($model->cmd, array("next", "back")) && $dataflg) {
                $model->e_examno = "";
                $model->s_examno = "";
            }
        }

        //開始受験番号
        if ($s_examno) $model->s_examno = $s_examno;
        $extra="";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->s_examno, "S_EXAMNO", 5, 5, $extra);

        //終了受験番号
        if ($e_examno) $model->e_examno = $e_examno;
        $extra="";
        $arg["TOP"]["E_EXAMNO"] = knjCreateTextBox($objForm, $model->e_examno, "E_EXAMNO", 5, 5, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $valArray, $dataflg, $examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl171dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl171dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "－全て－", "value" => "");
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
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $valArray, $dataflg, $examno) {
    //読込ボタン
    $extra  = "style=\"width:64px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('read2');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", " 読込み ", $extra);
    //読込ボタン（前の受験番号検索）
    $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('back');\"";
    $extra .= ($examno["min"] != $model->s_examno) ? "" : " disabled";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
    //読込ボタン（後の受験番号検索）
    $extra  = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onclick=\"return btn_submit('next');\"";
    $extra .= ($examno["max"] != $model->e_examno) ? "" : " disabled";
    $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

    $disable  = ($dataflg &&get_count($valArray) > 0) ? "" : " disabled";

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

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EVALTYPE");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL171D");
}
?>
