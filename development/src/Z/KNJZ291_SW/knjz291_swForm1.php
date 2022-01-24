<?php

require_once('for_php7.php');

class knjz291_swForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz291_swindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz291_swQuery::getStaffYear();
        $extra = "onchange=\"return btn_submit('chg_year');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1, $model);

        //学校側から呼び出された時
        if ($model->sendPrgid === 'KNJZ291A') {
            //年度詳細データコピー
            $disflg = $model->year === "ALL" ? " disabled" : "";
            $btnSize = " style=\"font-size:10px;\"";
            $hyouji = "職名等前年度からコピー";
            $extra = "onclick=\"return btn_submit('copy');\"";
            $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", $hyouji, $extra.$btnSize.$disflg);
        }

        //学年取得
        $gradeArray = array();
        $query = knjz291_swQuery::getGrade($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gradeArray[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        //教科取得
        $classArray = array();
        $query = knjz291_swQuery::getClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $classArray[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        //職員リスト
        $query = knjz291_swQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg["data"]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["CHARGECLASSCD"] != "") {
                if ($row["CHARGECLASSCD"] == 0) {
                   $row["CHARGECLASSCD"] = "0  無し";
                } else if ($row["CHARGECLASSCD"] == 1) {
                   $row["CHARGECLASSCD"] = "1  有り";
                }
            }

            //肩書き名称取得
            for ($i = 0; $i <= 3; $i++) {
                $field = "POSITIONCD".$i;
                if ($row[$field] === '0200') {
                    $row[$field."_MANAGER_NAME"] = $gradeArray[$row[$field."_MANAGER"]];
                } else if ($row[$field] === '1050') {
                    $row[$field."_MANAGER_NAME"] = $classArray[$row[$field."_MANAGER"]];
                }
            }

            //職員名取得
            if ($row["STAFFCD"] == $model->staffcd && $model->cmd != "chg_year") {
                $row["STAFFNAME"] = ($row["STAFFNAME"]) ? $row["STAFFNAME"] : "　";
                $row["STAFFNAME"] = "<a name=\"target\">{$row["STAFFNAME"]}</a><script>location.href='#target';</script>";
            }

            $row["STAFFBIRTHDAY"] = str_replace("-","/",$row["STAFFBIRTHDAY"]);

            $arg["data"][] = $row; 
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "list"){
            $arg["reload"] = "parent.right_frame.location.href='knjz291_swindex.php?cmd=edit';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz291_swForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    if ($name == "YEAR") {
        $opt[] = array('label' => ' －全て－ ', 'value' => "ALL");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        if ($name == "YEAR" && $model->sendPrgid == "") break;

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $default = ($model->sendPrgid == "") ? "ALL" : CTRL_YEAR;
        $value = ($value && $value_flg) ? $value : (($value == "ALL") ? $opt[0]["value"] : $default);
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
