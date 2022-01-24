<?php

require_once('for_php7.php');

class knjl071dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //事前チェック（合格類型）
        $valArray = $labelArray = array();
        $query = knjl071dQuery::getNameMst($model->ObjYear, "L013");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["VALUE"] == '4') continue;
            $valArray[]     = $row["VALUE"];
            $labelArray[]   = $row["NAME1"];
        }
        if (get_count($valArray) == 0) {
            $arg["pre_check"] = "errorPreCheck();";
        }
        knjCreateHidden($objForm, "VAL_LIST", implode(',', $valArray));
        knjCreateHidden($objForm, "LABEL_LIST", implode(',', $labelArray));

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //受験種別（入試区分）
        $query = knjl071dQuery::getNameMst($model->ObjYear, "L004");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //内部判定
        $query = knjl071dQuery::getEntexamInternalDecisionMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUB_ORDER", $model->sub_order, $extra, 1, "blank");

        //志望類型
        $query = knjl071dQuery::getNameMst($model->ObjYear, "L058");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->desirediv, $extra, 1, "blank");

        //一覧表示
        $arr_examno = array();
        $examno = array();
        $dataflg = false;
        if ($model->testdiv != "") {
            //データ取得
            $query = knjl071dQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //合格類型テキストボックス
                $value = ($model->isWarning()) ? $model->judge[$row["EXAMNO"]] : $row["JUDGEMENT"];
                $extra = " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckValue(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["JUDGEMENT"] = knjCreateTextBox($objForm, $value, "JUDGEMENT_".$row["EXAMNO"], 3, 1, $extra);

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $valArray, $dataflg, $examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl071dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl071dForm1.html", $arg);
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
function makeBtn(&$objForm, &$arg, $model, $valArray, $dataflg) {
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
    knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_SUB_ORDER");
    knjCreateHidden($objForm, "HID_DESIREDIV");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL071D");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
}
?>
