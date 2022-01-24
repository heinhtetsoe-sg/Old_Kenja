<?php

require_once('for_php7.php');

class knjj092Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjj092index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjj092Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('committeechange');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1);
        }

        //学期
        $query = knjj092Query::getSemester();
        $extra = "onchange=\"return btn_submit('committeechange');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");

        //委員会取得
        $optN = array();
        $value_flg = false;
        $queryN = knjj092Query::getCommitteeNameList($model);
        $resultN = $db->query($queryN);
        while ($rowN = $resultN->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optN[]= array('label' => $rowN["COMMITTEENAME"],
                           'value' => $rowN["COMMITTEECD"]);
            if ($model->SelectCommittee === $rowN["COMMITTEECD"]) $value_flg = true;
        }
        $resultN->free();
        $selectcommittee = ($model->SelectCommittee && $value_flg) ? $model->SelectCommittee : $optN[0]["value"];
        //委員会リスト
        $extra = "onChange=\"return btn_submit('committeechange')\"";
        $arg["COMMITTEE_YEAR_LIST"] = knjCreateCombo($objForm, "COMMITTEE_YEAR_LIST", $selectcommittee, $optN, $extra, $size);

        //学籍委員会履歴データ取得
        $query = knjj092Query::getStudentList($model, $selectcommittee);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //更新後この行にスクロールバーを移動させる
            if ($row["SCHREGNO"] == $model->schregNo2) {
                $model->schregNo2 = null;
                $row["EXECUTIVENAME"] = ($row["EXECUTIVENAME"]) ? $row["EXECUTIVENAME"] : "　";
                $row["EXECUTIVENAME"] = "<a name=\"target\">{$row["EXECUTIVENAME"]}</a><script>location.href='#target';</script>";
            }

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COMMITTEEFLG", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "committeechange") {
            $arg["reload"] = "window.open('knjj092index.php?cmd=edit&COMMITTEEFLG=ON&SCHKIND={$model->schkind}','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj092Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
