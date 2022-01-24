<?php

require_once('for_php7.php');

class knjl621aform {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl621aindex.php", "", "right_list");

        //DB接続
        $db = Query::dbCheckOut();

        //ajax
        //受験者名取得
        if ($model->cmd == "ajaxGetName") {
            $query = knjl621aQuery::getEntexamApplicantbaseDat($model->entexamyear, $model->ajaxParam["APPLICANTDIV"], $model->ajaxParam["EXAMNO"], $model->ajaxParam["TESTDIV"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            echo json_encode($row["NAME"]);
            die();
        }
        //座席番号チェック
        if ($model->cmd == "ajaxGetSeatno") {
            $cnt = $db->getOne(knjl621aQuery::checkSeatno($model));
            $cnt2 = $db->getOne(knjl621aQuery::checkExamno($model));
            echo json_encode($cnt.",".$cnt2);
            die();
        }

        // 年度
        $arg["ENTEXAMYEAR"] = $model->entexamyear;

        //入試制度コンボ
        $query = knjl621aQuery::getNameMst($model, "L003", "1");
        $extra = "onChange=\"btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボ
        $query = knjl621aQuery::getNameMst($model, "L024");
        $extra = "onChange=\"btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //受験番号
        $extra = " onkeyup=\"nextfeild(this)\" style=\"ime-mode:disabled;\" onChange=\"showName()\"";
        $examno = (isset($model->warning)) ? $model->field["EXAMNO"] : "";
        $arg["EXAMNO"] = knjCreateTelBox($objForm, $examno, "EXAMNO", 4, 4, $extra);

        //座席番号
        $extra = " onkeyup=\"nextfeild(this)\" style=\"ime-mode:disabled;\"";
        $seatno = (isset($model->warning)) ? $model->field["SEATNO"] : "";
        $arg["SEATNO"] = knjCreateTelBox($objForm, $seatno, "SEATNO", 4, 4, $extra);

        //履歴リスト
        $query = knjl621aQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //削除ボタン
            $extra  = "onClick=\"deleteSubmit('".$row["ENTEXAMYEAR"]."', '".$row["APPLICANTDIV"]."', '".$row["TESTDIV"]."', '".$row["EXAMNO"]."', '".$row["EXEC_TIME"]."')\"";
            $row["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "取 消", $extra);

            //日時
            $row["EXEC_TIME"] = date("m/d H:i", strtotime($row["EXEC_TIME"]));

            $arg["data"][] = $row;
        }

        //登録ボタン
        $extra = "onclick=\"return btn_submit('insert');\"";
        $arg["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "登 録", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LIST_ENTEXAMYEAR");
        knjCreateHidden($objForm, "LIST_APPLICANTDIV");
        knjCreateHidden($objForm, "LIST_TESTDIV");
        knjCreateHidden($objForm, "LIST_EXAMNO");
        knjCreateHidden($objForm, "LIST_EXEC_TIME");
        knjCreateHidden($objForm, "DUPL_SEATNO_FLG");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjl621aForm.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
            'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
