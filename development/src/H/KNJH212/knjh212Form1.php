<?php

require_once('for_php7.php');

class knjh212Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjh212index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjh212Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('domitorychange');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1);
        }

        //寮取得
        $optN = array();
        $value_flg = false;
        $queryN = knjh212Query::getDomitoryNameList($model);
        $resultN = $db->query($queryN);
        while ($rowN = $resultN->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optN[]= array('label' => $rowN["DOMI_NAME"],
                           'value' => $rowN["DOMI_CD"]);
            if ($model->SelectDomi === $rowN["DOMI_CD"]) $value_flg = true;
        }
        $resultN->free();
        $selectdomi = ($model->SelectDomi && $value_flg) ? $model->SelectDomi : $optN[0]["value"];
        //寮リスト
        $extra = "onChange=\"return btn_submit('domitorychange')\"";
        $arg["DOMI_YEAR_LIST"] = knjCreateCombo($objForm, "DOMI_YEAR_LIST", $selectdomi, $optN, $extra, $size);

        //学籍寮履歴データ取得
        $query = knjh212Query::getStudentList($model, $selectdomi);
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
            $row["EDITSDATE"]   = str_replace("-","/",$row["DOMI_ENTDAY"]);
            $row["DOMI_OUTDAY"]       = str_replace("-","/",$row["DOMI_OUTDAY"]);

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DOMI_FLG", "");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "domitorychange") {
            $arg["reload"] = "window.open('knjh212index.php?cmd=edit&DOMI_FLG=ON&SCHKIND={$model->schkind}','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh212Form1.html", $arg); 
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
