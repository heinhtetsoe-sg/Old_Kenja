<?php

require_once('for_php7.php');

class knjz291Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz291index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz291Query::getStaffYear();
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

        //職員リスト
        $query = knjz291Query::getList($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["CHARGECLASSCD"] != "") {
                if ($row["CHARGECLASSCD"] == 0) {
                   $row["CHARGECLASSCD"] = "0  無し";
                } else if ($row["CHARGECLASSCD"] == 1) {
                   $row["CHARGECLASSCD"] = "1  有り";
                }
            }
            //各名称を取得
            if ($row["POSITIONCD1"] === '0200') {
                $row["POSITIONCD1_MANAGER_NAME"] = $db->getOne(knjz291Query::getGrade($model, $row["POSITIONCD1_MANAGER"]));
            } else if ($row["POSITIONCD1"] === '1050') {
                $row["POSITIONCD1_MANAGER_NAME"] = $db->getOne(knjz291Query::getClass($model, $row["POSITIONCD1_MANAGER"]));
            }
            if ($row["POSITIONCD2"] === '0200') {
                $row["POSITIONCD2_MANAGER_NAME"] = $db->getOne(knjz291Query::getGrade($model, $row["POSITIONCD2_MANAGER"]));
            } else if ($row["POSITIONCD2"] === '1050') {
                $row["POSITIONCD2_MANAGER_NAME"] = $db->getOne(knjz291Query::getClass($model, $row["POSITIONCD2_MANAGER"]));
            }
            if ($row["POSITIONCD3"] === '0200') {
                $row["POSITIONCD3_MANAGER_NAME"] = $db->getOne(knjz291Query::getGrade($model, $row["POSITIONCD3_MANAGER"]));
            } else if ($row["POSITIONCD3"] === '1050') {
                $row["POSITIONCD3_MANAGER_NAME"] = $db->getOne(knjz291Query::getClass($model, $row["POSITIONCD3_MANAGER"]));
            }
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
            $arg["reload"] = "parent.right_frame.location.href='knjz291index.php?cmd=new';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz291Form1.html", $arg);
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
