<?php

require_once('for_php7.php');

class knjb1100Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb1100index.php", "", "main");
        $db = Query::dbCheckOut();

        //処理年度
        $arg['YEAR'] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjb1100Query::getSemester(CTRL_YEAR);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);

        //リスト表示
        makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb1100Form1.html", $arg); 
    }
}
/***************************************    これ以下は関数    **************************************************/
////////////////////////
////////////////////////コンボ作成
////////////////////////
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["header"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
////////////////////////
////////////////////////リスト表示
////////////////////////
function makeList(&$objForm, &$arg, $db, &$model) {
    //合併先科目一覧：科目合併設定データより取得。単位加算のみ。
    $gyo = 0;
    $model->dataList = array();
    $query = knjb1100Query::getList($model, CTRL_YEAR);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //合併先科目コード保管
        $model->dataList[] = $row["COMBINED_SUBCLASSCD"];
        $paramSubclasscd = $row["COMBINED_SUBCLASSCD"];
        //講座・群コンボ作成
        $query = knjb1100Query::getChairDat($model, CTRL_YEAR, $model->semester, $paramSubclasscd);
        $name = "CHAIRCD" .$gyo;
        $extra = "onchange=\"return btn_submit('main')\"";
        $groupcd = getChairCmb($objForm, $row, $db, $query, $name, $model->field[$name], $extra, 1);
        //名簿作成状況を取得
        $query = knjb1100Query::getExistsChairStdDat(CTRL_YEAR, $model->semester, $model->field[$name]);
        if (0 < $db->getOne($query)) {
            $row["MAKING"] = "作成済み";
            $row["COLOR"] = "red";
        } else {
            $row["MAKING"] = "未作成";
            $row["COLOR"] = "black";
        }
        //「実行」欄チェックボックス
        $checkValue = $row["COMBINED_SUBCLASSCD"] .":" .$model->field[$name] .":" .$groupcd .":" .$row["SUBCLASSNAME"];
        $checked = "";
        if (get_count($model->check)) {
            foreach ($model->check as $val) {
                if ($checkValue == $val) $checked = "checked ";
            }
        }
        $extra = $checked;
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $checkValue, $extra, "1");
        //合併先科目一覧
        $arg['data'][] = $row;
        $gyo++;
    }
    $result->free();
}
//講座・群コンボ作成
function getChairCmb(&$objForm, &$rowSub, $db, $query, $name, &$value, $extra, $size) {
    $opt = $optGrp = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $groupName = ($row["GROUPCD"] == "0000") ? "なし(HR)" : $row["GROUPCD"] ."：" .$row["GROUPNAME"];
        $opt[] = array('label' => $row["CHAIRCD"] ."：" .$row["CHAIRNAME"] ."　" ."（" .$groupName ."）",
                       'value' => $row["CHAIRCD"]);
        if ($value == $row["CHAIRCD"]) $value_flg = true;
        $optGrp[$row["CHAIRCD"]] = $row["GROUPCD"];
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    if (1 < get_count($opt)) {
        $rowSub["CHAIRCD"] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $rowSub["CHAIRCD"] = $opt[0]["label"];
    }

    return $optGrp[$value];
}
////////////////////////
////////////////////////ボタン作成
////////////////////////
function makeButton(&$objForm, &$arg, &$model) {
    //更新ボタン
    $disExtra = (0 < get_count($model->dataList)) ? "" : "disabled ";
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "生 成", $disExtra ."onclick=\"return btn_submit('update');\"");

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return closeWin();\"");
}
////////////////////////
////////////////////////hidden作成
////////////////////////
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
