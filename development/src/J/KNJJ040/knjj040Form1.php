<?php

require_once('for_php7.php');

class knjj040Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjj040index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["useClubMultiSchoolKind"] != '1' && $model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjj040Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('clubchange');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1);
        }

        //部クラブ取得
        $optN = array();
        $value_flg = false;
        $queryN = knjj040Query::getClubNameList($model);
        $resultN = $db->query($queryN);
        while ($rowN = $resultN->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optN[]= array('label' => $rowN["CLUBNAME"],
                           'value' => $rowN["CLUBCD"]);
            if ($model->SelectClub === $rowN["CLUBCD"]) $value_flg = true;
        }
        $resultN->free();
        $selectclub = ($model->SelectClub && $value_flg) ? $model->SelectClub : $optN[0]["value"];
        //部クラブリスト
        $extra = "onChange=\"return btn_submit('clubchange')\"";
        $arg["CLUB_YEAR_LIST"] = knjCreateCombo($objForm, "CLUB_YEAR_LIST", $selectclub, $optN, $extra, $size);

        //役職区分取得
        $queryY = knjj040Query::getExecutivecd($model);
        $resultY = $db->query($queryY);
        while ($rowY = $resultY->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optY[$rowY["EXECUTIVECD"]] = $rowY["ROLENAME"];
        }
        $resultY->free();

        //学籍部クラブ履歴データ取得
        $query = knjj040Query::getStudentList($model, $selectclub);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //更新後この行にスクロールバーを移動させる
            if ($row["SCHREGNO"] == $model->gakusekino2) {
                $model->gakusekino2 = null;
                $row["REMARK"] = ($row["REMARK"]) ? $row["REMARK"] : "　";
                $row["REMARK"] = "<a name=\"target\">{$row["REMARK"]}</a><script>location.href='#target';</script>";
            }

            $row["STUDENTINFO"] = $row["HR_NAME"]."-".$row["ATTENDNO"]."番　".$row["SCHREGNO"]."　".$row["NAME_SHOW"];
            $row["EDITSDATE"]   = str_replace("-","/",$row["SDATE"]);
            $row["EDATE"]       = str_replace("-","/",$row["EDATE"]);
            $row["EXECUTIVECD"] = $optY[$row["EXECUTIVECD"]];

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CLUBFLG", "");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "clubchange") {
            $arg["reload"] = "window.open('knjj040index.php?cmd=edit&CLUBFLG=ON&SCHKIND={$model->schkind}','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj040Form1.html", $arg); 
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
