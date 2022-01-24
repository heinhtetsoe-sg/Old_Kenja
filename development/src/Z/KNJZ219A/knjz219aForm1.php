<?php

require_once('for_php7.php');

class knjz219aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz219aindex.php", "", "main");
        $db = Query::dbCheckOut();

        //処理年度
        $arg['YEAR'] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjz219aQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            //学校名称2表示
            $info = $db->getRow(knjz219aQuery::getSchoolMst($model, CTRL_YEAR), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        }

        //基準の計算方法
        $query = knjz219aQuery::getStandard($model, CTRL_YEAR, "Z017");
        $optStandard = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg['STANDARD'] = $optStandard["LABEL"];

        //リスト表示
        makeList($objForm, $arg, $db, $optStandard["GVAL_CALC"], $model);

        //割合表示
        makeListWeight($objForm, $arg, $db, $optStandard["GVAL_CALC"], $model);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz219aForm1.html", $arg); 
    }
}

function makeList(&$objForm, &$arg, $db, $standard, &$model) {
    //「評定計算方法」欄のコンボボックス：名称マスタより取得
    $optNotH = array();
    $opt = array();
    $query = knjz219aQuery::getNameMst(CTRL_YEAR, "Z017");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        //高校以外は、計算方法「単位による重み付け」を表示しない。
        if ($row["VALUE"] == "1") continue;
        $optNotH[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
    }
    $result->free();

    //合併先科目一覧：科目合併設定データより取得。単位加算のみ。
    $cnt = 0;
    $model->combSubclass = array();
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        $model->combClass = array();
        $model->combSchoolkind = array();
        $model->combCurriculumcd = array();
    }
    $query = knjz219aQuery::getList(CTRL_YEAR, $model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //合併先科目コード保管
        $model->combSubclass[] = $row["COMBINED_SUBCLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $model->combClass[]         = $row["COMBINED_CLASSCD"];
            $model->combSchoolkind[]    = $row["COMBINED_SCHOOL_KIND"];
            $model->combCurriculumcd[]  = $row["COMBINED_CURRICULUM_CD"];
        }
        //「評定計算方法」欄のコンボボックス
        $name = "GVAL_CALC" .$cnt;
        $value = strlen($row["GVAL_CALC"]) ? $row["GVAL_CALC"] : $standard;//デフォルト値
        $row["GVAL_CALC"] = knjCreateCombo($objForm, $name, $value, $row["COMBINED_SCHOOL_KIND"] == "H" ? $opt : $optNotH, "", 1);
        //合併先科目一覧
        $arg['data'][] = $row;
        $cnt++;
    }
    $result->free();
    //基準の計算方法チェック用
    knjCreateHidden($objForm, "HID_STANDARD", $standard);
    //評定計算方法チェック用
    $hidZ017 = (0 < get_count($opt)) ? "OK" : "NG";
    knjCreateHidden($objForm, "HID_Z017", $hidZ017);
}

function makeListWeight(&$objForm, &$arg, $db, $standard, &$model) {

    //合併先科目一覧：科目合併設定データより取得。単位加算のみ。
    $cnt = 0;
    $model->attendSubclass = array();
    $model->attendClass = array();
    $model->attendSchoolkind = array();
    $model->attendCurriculumcd = array();
    $query = knjz219aQuery::getListWeight(CTRL_YEAR, $model);
    $result = $db->query($query);
    //extraセット
    $extraWeight = "onblur=\"calc(this);\"";

    $befCombinedSubclassCd = "";
    $setData = "";
    $setCnt = 1;
    $model->weight = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $combSubCd = $row["COMBINED_CLASSCD"].$row["COMBINED_SCHOOL_KIND"].$row["COMBINED_CURRICULUM_CD"].$row["COMBINED_SUBCLASSCD"];
        $attendSubCd = $row["ATTEND_CLASSCD"].$row["ATTEND_SCHOOL_KIND"].$row["ATTEND_CURRICULUM_CD"].$row["ATTEND_SUBCLASSCD"];
        $setName = $combSubCd."_".$attendSubCd;
        if (isset($model->warning)) {
            $row["WEIGHTING"] = $model->warnAtte[$setName];
        }

        $row["WEIGHTING"] = knjCreateTextBox($objForm, $row["WEIGHTING"], "WEIGHTING_{$setName}", 4, 4, $extraWeight);
        if ($befCombinedSubclassCd != $combSubCd) {
            $setData  = "<tr class=\"no_search_line\" >";
            $setData .= "<td bgcolor='#ffffff' align=\"left\" width=\"250px\" rowspan=\"{$row["CNT"]}\">&nbsp;&nbsp;{$row["COMBINED_SUBCLASS_NAME"]}</td>";
            $setData .= "<td bgcolor='#ffffff' align=\"reft\" width=\"250px\">{$row["ATTEND_SUBCLASS_NAME"]}</td>";
            $setData .= "<td bgcolor='#ffffff' align=\"center\" width=\"*px\">{$row["WEIGHTING"]}</td>";
        } else {
            $setData .= "</tr>";
            $setData .= "<tr class=\"no_search_line\" >";
            $setData .= "<td bgcolor='#ffffff' align=\"reft\" width=\"250px\">{$row["ATTEND_SUBCLASS_NAME"]}</td>";
            $setData .= "<td bgcolor='#ffffff' align=\"center\" width=\"*px\">{$row["WEIGHTING"]}</td>";
            $setCnt++;
        }

        $model->weight[$combSubCd][$setCnt] = $row;

        $befCombinedSubclassCd = $row["COMBINED_CLASSCD"].$row["COMBINED_SCHOOL_KIND"].$row["COMBINED_CURRICULUM_CD"].$row["COMBINED_SUBCLASSCD"];
        //合併先科目一覧
        if ($setCnt == $row["CNT"]) {
            $setData .= "</tr>";
            $arg['data2'][] = $setData;
            $setCnt = 1;
        }
    }
    //合併先科目一覧
    if ($setCnt > 1) {
        $setData .= "</tr>";
        $arg['data2'][] = $setData;
    }

    $result->free();
}

function makeButton(&$objForm, &$arg, &$model) {
    //更新ボタン
    $disExtra = (0 < get_count($model->combSubclass)) ? "" : "disabled ";
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "保 存", $disExtra ."onclick=\"return btn_submit('update');\"");

    //更新ボタン
    $arg["button"]["btn_updte2"] = knjCreateBtn($objForm, "btn_updte2", "保 存", "onclick=\"return btn_submit('updateAttend');\"");

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return closeWin();\"");
}

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
