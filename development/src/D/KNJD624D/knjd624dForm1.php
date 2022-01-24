<?php

require_once('for_php7.php');


class knjd624dForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd624dForm1", "POST", "knjd624dindex.php", "", "knjd624dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;


        //学期コンボ作成
        $query = knjd624dQuery::getSemester($model, 0);
        $extra = "onchange=\"return btn_submit('knjd624d')\"";
        
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        if ($model->Properties["KNJD129V_SUBCLASS_RADIO"] == '1') {
            $arg["KNJD129V_SUBCLASS_RADIO"] = 1;

            // 実施科目ラジオ作成
            $query = knjd624dQuery::getSemester($model, 1);
            $result = $db->query($query);
            $semes = array();
            $opt_div = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $semes[] = $row;
                $opt_div[] = $row["VALUE"];
            }
            $opt_div[] = "9";
            $result->free();
            $extra = "";
            $value = $model->field["SUBSEL"] ? $model->field["SUBSEL"] : "9";
            foreach ($semes as $row) {
                $seme = array();
                $seme["SEME"] = ($row["SEMESTERDIV"] == $row["VALUE"] ? ($row["LABEL"]."・通年") : $row["LABEL"])."科目のみ";
                $seme["ID"] = "SUBSEL".$row["VALUE"];
                $objForm->ae( array("type"      => "radio",
                                    "name"      => "SUBSEL",
                                    "value"     => $value,
                                    "extrahtml" => " id=SUBSEL".$row["VALUE"]." onclick=\"return btn_submit('knjd624d')\"",
                                    "multiple"  => get_count($opt_div)));

                $seme["SUBSEL"] = $objForm->ge("SUBSEL", $row["VALUE"]);
                $arg["seme"][] = $seme;
                $model->field["SEMESTERDIV"] = $row["SEMESTERDIV"];
            }
            $seme = array();
            $seme["SEME"] = "全て";
            $seme["ID"] = "SUBSEL9";
            $objForm->ae( array("type"      => "radio",
                                "name"      => "SUBSEL",
                                "value"     => $value,
                                "extrahtml" => " id=SUBSEL9"." onclick=\"return btn_submit('knjd624d')\"",
                                "multiple"  => get_count($opt_div)));

            $seme["SUBSEL"] = $objForm->ge("SUBSEL", "9");
            $arg["seme"][] = $seme;
        }

        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["USE_MAJOR"] = '1';
            //学科名コンボ
            $query = knjd624dQuery::getCourseMajor($model);
            $extra = "onchange=\"return btn_submit('knjd624d');\"";
            makeCmb($objForm, $arg, $db, $query, "MAJOR", $model->field["MAJOR"], $extra, 1);
        }

        //テストコンボ作成
        $query = knjd624dQuery::getTest($model, $model->field["SEMESTER"]);
        $extra = "onchange=\"return btn_submit('knjd624d')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);

        //学年コンボ作成
        $query = knjd624dQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('knjd624d')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        $model->schoolKind = $db->getOne(knjd624dQuery::getSchoolKind($model));

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd624dForm1.html", $arg); 
    }
}

function makeListToList(&$objForm, &$arg, $db, $model) {

    //対象一覧リストを作成する
    $query = knjd624dQuery::getTestSubclass($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($row["VALUE"] !== $row["COMBINED_CLASSCD"].'-'.$row["COMBINED_SCHOOL_KIND"].'-'.$row["COMBINED_CURRICULUM_CD"].'-'.$row["COMBINED_SUBCLASSCD"]) {
                $opt[] = array('label' => "　".$row["LABEL"],
                               'value' => $row["VALUE"]);
            } else {
                $opt[] = array('label' => "●".$row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        } else {
            if ($row["VALUE"] !== $row["COMBINED_SUBCLASSCD"]) {
                $opt[] = array('label' => "　".$row["LABEL"],
                               'value' => $row["VALUE"]);
            } else {
                $opt[] = array('label' => "●".$row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        }
    }
    $result->free();
    $arg["SELECT_SUBCLASS"] = "1";

    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

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
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "CHANGE", $model->field["SELECT_DIV"]);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD624D");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "cmd");
    //教育課程コード
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}

?>
