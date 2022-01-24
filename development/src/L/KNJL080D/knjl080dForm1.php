<?php

require_once('for_php7.php');

class knjl080dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //事前チェック
        $check = array();
        $check["ENTDIV"]        = array("入学類型", "L012");
        $check["PROCEDUREDIV"]  = array("手続区分", "L011");
        foreach ($check as $key => $val) {
            $valArray = $labelArray = array();
            $query = knjl080dQuery::getNameMst($model->ObjYear, $val[1]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $valArray[]     = $row["VALUE"];
                $labelArray[]   = $row["NAME1"];
            }
            if (get_count($valArray) == 0) {
                $arg["pre_check"] = "errorPreCheck('{$val[0]}');";
            }
            knjCreateHidden($objForm, $key."_LIST", implode(',', $valArray));
            knjCreateHidden($objForm, $key."_LABEL_LIST", implode(',', $labelArray));
        }

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //受験種別（入試区分）
        $query = knjl080dQuery::getNameMst($model->ObjYear, "L004");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //内部判定
        $query = knjl080dQuery::getEntexamInternalDecisionMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUB_ORDER", $model->sub_order, $extra, 1, "blank");

        //志望類型
        $query = knjl080dQuery::getNameMst($model->ObjYear, "L058");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->desirediv, $extra, 1, "blank");

        //合格類型
        $query = knjl080dQuery::getNameMst($model->ObjYear, "L013", "1");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "JUDGEMENT", $model->judgement, $extra, 1, "blank");

        //一覧表示
        $arr_examno = array();
        $examno = array();
        $dataflg = false;
        if ($model->testdiv != "") {
            //データ取得
            $query = knjl080dQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //入学類型テキストボックス
                $value = ($model->isWarning()) ? $model->data["ENTDIV"][$row["EXAMNO"]] : $row["ENTDIV"];
                $extra = " onChange=\"Setflg(this);\" id=\"ENTDIV_".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckValue(this, '');\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["ENTDIV"] = knjCreateTextBox($objForm, $value, "ENTDIV_".$row["EXAMNO"], 1, 1, $extra);

                //手続区分テキストボックス
                $value = ($model->isWarning()) ? $model->data["PROCEDUREDIV"][$row["EXAMNO"]] : $row["PROCEDUREDIV"];
                $extra = " onChange=\"Setflg(this);\" id=\"PROCEDUREDIV_".$row["EXAMNO"]."\" style=\"text-align:center;\" onblur=\"CheckValue(this, '{$row["JUDGE_KIND"]}');\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["PROCEDUREDIV"] = knjCreateTextBox($objForm, $value, "PROCEDUREDIV_".$row["EXAMNO"], 1, 1, $extra);

                //辞退チェックボックス
                $extra  = ($row["JITAI"] == "1") ? "checked" : "";
                $extra .= " id=\"JITAI_".$row["EXAMNO"]."\" onChange=\"Setflg(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                $row["JITAI"] = knjCreateCheckBox($objForm, "JITAI_".$row["EXAMNO"], "1", $extra, "");

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
        $arg["start"] = $objForm->get_start("main", "POST", "knjl080dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl080dForm1.html", $arg);
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
    knjCreateHidden($objForm, "HID_JUDGEMENT");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL080D");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
}
?>
