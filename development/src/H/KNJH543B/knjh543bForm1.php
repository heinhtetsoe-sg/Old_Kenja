<?php

require_once('for_php7.php');

class knjh543bForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh543bindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //基準の計算方法 4:合算
        $query = knjh543bQuery::getNameMstZ027("4");
        $optStandard = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["STANDARD"] = $optStandard["LABEL"];

        /**********/
        /* ラジオ */
        /**********/
        //区分ラジオ 01:02:ダミー 03:コース 04:コースグループ
        //プロパティ(usePerfectCourseGroup = 1)の場合、コースグループを初期値にする。
        $defDiv = ($model->Properties["usePerfectCourseGroup"] == '1') ? "04" : "03";
        $model->field["DIV"] = ($model->field["DIV"] == "" || $model->field["DIV"] == "00") ? $defDiv : $model->field["DIV"];
        $opt_div = array(1, 2, 3, 4);
        $extra = "onClick=\"return btn_submit('change')\"";
        $label = array($extra." id=\"DIV1\"", $extra." id=\"DIV2\"", $extra." id=\"DIV3\"", $extra." id=\"DIV4\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $label, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["sepa"][$key] = $val;

        //表示切替
        if ($model->field["DIV"] == "03") {
            $arg["show3"] = $model->field["DIV"];
        } else if ($model->field["DIV"] == "04") {
            $arg["show4"] = $model->field["DIV"];
        } else {
        }

        /**********/
        /* コンボ */
        /**********/
        $extra = "onChange=\"return btn_submit('change');\" ";
        //学期コンボ
        $query = knjh543bQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);
        //実力区分コンボ
        $query = knjh543bQuery::getProficiencyDiv($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYDIV"], "PROFICIENCYDIV", $extra, 1);
        //実力コードコンボ
        $query = knjh543bQuery::getProficiencyCd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYCD"], "PROFICIENCYCD", $extra, 1);
        //学年コンボ
        $query = knjh543bQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);
        //課程学科コースコンボ
        $query = knjh543bQuery::getCourse($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSE"], "COURSE", $extra, 1);
        //コースグループコンボ
        $query = knjh543bQuery::getGroupCd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GROUP_CD"], "GROUP_CD", $extra, 1);

        /**********/
        /* リスト */
        /**********/
        //リスト表示
        makeList($objForm, $arg, $db, $optStandard["VALUE"], $model);
        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh543bForm1.html", $arg); 
    }
}
/***************************************    これ以下は関数    **************************************************/
////////////////////////
////////////////////////リスト表示
////////////////////////
function makeList(&$objForm, &$arg, $db, $standard, &$model) {
    //「計算方法」欄のコンボボックス：名称マスタより取得
    $opt = array();
    $query = knjh543bQuery::getNameMstZ027();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();

    //合併先科目一覧
    $cnt = 0;
    $model->combSubclass = array();
    $query = knjh543bQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //合併先科目コード保管
        $model->combSubclass[] = $row["COMBINED_SUBCLASSCD"];
        //「計算方法」欄のコンボボックス
        $name = "GVAL_CALC" .$cnt;
        $value = strlen($row["GVAL_CALC"]) ? $row["GVAL_CALC"] : $standard;//デフォルト値
        $row["GVAL_CALC"] = $opt[$value];
        knjCreateHidden($objForm, $name, $value);
        //「実行」欄チェックボックス
        $name = "CHECK" .$cnt;
        $extra = strlen($model->field[$name]) ? "checked" : "";
        $row["CHECK"] = knjCreateCheckBox($objForm, $name, "1", $extra);
        //合併先科目一覧
        $arg["data"][] = $row;
        $cnt++;
    }
    $result->free();
    //基準の計算方法チェック用
    knjCreateHidden($objForm, "HID_STANDARD", $standard);
    //計算方法チェック用
    $hidZ027 = (0 < get_count($opt)) ? "OK" : "NG";
    knjCreateHidden($objForm, "HID_Z027", $hidZ027);
}
////////////////////////
////////////////////////履歴表示
////////////////////////
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    //履歴一覧
    $query = knjh543bQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-","/",$row["CALC_DATE"]);
        $arg['data2'][] = $row;
    }
    $result->free();
}
////////////////////////
////////////////////////コンボ作成
////////////////////////
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["sepa"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
?>
