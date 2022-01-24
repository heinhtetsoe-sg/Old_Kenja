<?php

require_once('for_php7.php');

class knjd219aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd219aindex.php", "", "main");
        $db = Query::dbCheckOut();

        //処理年度
        $arg['YEAR'] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjd219aQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            //学校名称2表示
            $info = $db->getRow(knjd219aQuery::getSchoolMst($model, CTRL_YEAR), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        }

        //基準の計算方法
        $query = knjd219aQuery::getStandard($model, CTRL_YEAR, "Z017");
        $optStandard = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg['STANDARD'] = $optStandard["LABEL"];

        //リスト表示
        makeList($objForm, $arg, $db, $optStandard["GVAL_CALC"], $model);

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd219aForm1.html", $arg); 
    }
}
/***************************************    これ以下は関数    **************************************************/
////////////////////////
////////////////////////リスト表示
////////////////////////
function makeList(&$objForm, &$arg, $db, $standard, &$model) {
    //「評定計算方法」欄のコンボボックス：名称マスタより取得
    $optGval = array();
    $query = knjd219aQuery::getNameMst(CTRL_YEAR, "Z017");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optGval[$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();

    //合併先科目一覧：科目合併設定データより取得。単位加算のみ。
    $model->combSubclass = array();
    $query = knjd219aQuery::getList(CTRL_YEAR, $model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //合併先科目コード保管
        $model->combSubclass[] = $row["COMBINED_SUBCLASSCD"];
        //「評定計算方法」欄
        $value = strlen($row["GVAL_CALC"]) ? $row["GVAL_CALC"] : $standard;//デフォルト値
        $row["GVAL_CALC"] = $optGval[$value];
        //「実行」欄チェックボックス
        $checkValue = $row["COMBINED_SUBCLASSCD"] ."-" .$value ."-" .$row["SUBCLASSNAME"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $checkValue .= "-" .$row["COMBINED_CLASSCD"];
            $checkValue .= "-" .$row["COMBINED_SCHOOL_KIND"];
            $checkValue .= "-" .$row["COMBINED_CURRICULUM_CD"];
        }
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
    }
    $result->free();
    //基準の計算方法チェック用
    knjCreateHidden($objForm, "HID_STANDARD", $standard);
    //評定計算方法チェック用
    $hidZ017 = (0 < get_count($optGval)) ? "OK" : "NG";
    knjCreateHidden($objForm, "HID_Z017", $hidZ017);
}
////////////////////////
////////////////////////履歴表示
////////////////////////
function makeListRireki(&$objForm, &$arg, $db, $model) {
    //履歴一覧
    $query = knjd219aQuery::getListRireki(CTRL_YEAR, $model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-","/",$row["CALC_DATE"]);
        $arg['data2'][] = $row;
    }
    $result->free();
}
////////////////////////
////////////////////////ボタン作成
////////////////////////
function makeButton(&$objForm, &$arg, &$model) {
    //更新ボタン
    $disExtra = (0 < get_count($model->combSubclass)) ? "" : "disabled ";
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "実 行", $disExtra ."onclick=\"return btn_submit('update');\"");

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return closeWin();\"");
}
////////////////////////
////////////////////////hidden作成
////////////////////////
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
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
