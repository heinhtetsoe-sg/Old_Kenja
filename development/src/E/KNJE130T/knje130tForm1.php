<?php

require_once('for_php7.php');


class knje130tForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje130tForm1", "POST", "knje130tindex.php", "", "knje130tForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //hidden
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //学年リストボックスを作成する
        $query = knje130tQuery::getSelectGrade($model);
        $extra = "onchange=\"return btn_submit('knje130t')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //クラス一覧リスト作成する
        $query = knje130tQuery::getAuth($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        //対象ラジオボタン 1:学年評価 2:評定
        $model->field["OUT_DIV"] = $model->field["OUT_DIV"] ? $model->field["OUT_DIV"] : '1';
        $opt_outdiv = array(1, 2);
        $extra = array("id=\"OUT_DIV1\"", "id=\"OUT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "OUT_DIV", $model->field["OUT_DIV"], $extra, $opt_outdiv, get_count($opt_outdiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //未履修科目を含むチェックボックス
        $extra  = ($model->field["INCLUDE_MIRISYUU"] == "1") ? " checked " :  "" ;
        $extra .= " id=\"INCLUDE_MIRISYUU\"";
        $arg["data"]["INCLUDE_MIRISYUU"] = knjCreateCheckBox($objForm, "INCLUDE_MIRISYUU", "1", $extra, "");

        //評定読替するかしないかのフラグ 1:表示 1以外:非表示
        if ($model->Properties["hyoteiYomikae"] == '1') {
            $arg["HYOTEI_YOMIKAE_FLG"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["HYOTEI_YOMIKAE_FLG"]);
        }

        //評定読替チェックボックス
        if ($model->Properties["useProvFlg"] == "1") {
            $arg["data"]["KARI_MOJI"] = "仮";
        }
        $extra  = ($model->field["HYOTEI_YOMIKAE"] == "1") ? "checked" : "";
        $extra .= " id=\"HYOTEI_YOMIKAE\"";
        $arg["data"]["HYOTEI_YOMIKAE"] = knjCreateCheckBox($objForm, "HYOTEI_YOMIKAE", "1", $extra, "");

        //DB切断
        Query::dbCheckIn($db);

        //クラス一覧
        $extra = "multiple style=\"width:180px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, isset($row1)?$row1:array(), $extra, 15);

        //出力対象クラス
        $extra = "multiple style=\"width:180px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, array(), $extra, 15);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE130T");
        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "useCurriculumcd" , $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje130tForm1.html", $arg); 
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $gradeH3 = "";
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["SCHOOL_KIND"] == "H" && (int)$row["GRADE_CD"] == 3) $gradeH3 = $row["VALUE"];
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "GRADE" && strlen($gradeH3)) ? $gradeH3 : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
