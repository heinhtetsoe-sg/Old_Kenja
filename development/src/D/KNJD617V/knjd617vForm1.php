<?php

require_once('for_php7.php');
class knjd617vForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd617vForm1", "POST", "knjd617vindex.php", "", "knjd617vForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //Z010取得
        $query = knjd617vQuery::getNameMstZ010($model);
        $model->z010 = $db->getOne($query);

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd617vQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd617v')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["USE_MAJOR"] = '1';
            //学科名コンボ
            $query = knjd617vQuery::getCourseMajor($model);
            $extra = "onchange=\"return btn_submit('knjd617v');\"";
            makeCmb($objForm, $arg, $db, $query, "MAJOR", $model->field["MAJOR"], $extra, 1);
        }

        //学年コンボ作成
        $query = knjd617vQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('knjd617v')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //テストコンボ作成
        $query = knjd617vQuery::getTest($model, $model->field["SEMESTER"], $model->field["GRADE"]);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], "", 1);
        knjCreateHidden($objForm, "TESTCD_NAME");

        //平均点・順位ラジオボタン 1:クラス 2:学年
        $opt_sort = array(1, 2);
        $model->field["PRINT_DIV"] = ($model->field["PRINT_DIV"] == "") ? ($model->Properties["knjd617vDefaultPrintDiv"] == "" ? "1" : $model->Properties["knjd617vDefaultPrintDiv"]) : $model->field["PRINT_DIV"];
        $printSubmit = "onclick=\"return btn_submit('knjd617v')\"";
        $extra = array("id=\"PRINT_DIV1\" {$printSubmit}", "id=\"PRINT_DIV2\" {$printSubmit}");
        $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //総合点科目コードラジオボタン 3:3科目 5:5科目 9:全科目
        if ($model->Properties["useSubclassGroup"] && $model->Properties["knjd617vSelectSubclassGroup"]) {
            $query = knjd617vQuery::getSubclassGroup($model);
            $rows = rows($db, $query);
            if (!empty($rows)) {
                $rows[] = array("GROUP_DIV" => "9", "GROUP_NAME" => "全教科計");
                if (!$model->field["SUBCLASS_GROUP_DIV"]) $model->field["SUBCLASS_GROUP_DIV"] = $model->Properties["knjd617vDefaultSubclassGroupDiv"] ? $model->Properties["knjd617vDefaultSubclassGroupDiv"] : "9";
                foreach ($rows  as $row) {
                    $d = $row["GROUP_DIV"];
                    $k = "SUBCLASS_GROUP_DIV".$row["GROUP_DIV"];
                    $arr[] = "<input type='radio' name='SUBCLASS_GROUP_DIV' id='{$k}' value='{$d}' ".($d == $model->field["SUBCLASS_GROUP_DIV"] ? " checked " : "")." ><label for='{$k}'>{$row["GROUP_NAME"]}</label>";
                }
                $arg["data"]["SUBCLASS_GROUP_DIV"] = $arr;
            }
        }

        makeListToList($objForm, $arg, $db, $model);

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : ($model->Properties["knjd617vDefaultOutputKijun"] ? $model->Properties["knjd617vDefaultOutputKijun"] : '1');
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //平均点・順位ラジオボタン 1:学年 2:コース
        $opt_addr = array(1, 2);
        $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? ($model->Properties["knjd617vDefaultGroupDiv"] ? $model->Properties["knjd617vDefaultGroupDiv"] : "1") : $model->field["GROUP_DIV"];
        $extra = array("id=\"GROUP_DIV1\" onclick=\"chkGroupDiv();\" ", "id=\"GROUP_DIV2\" onclick=\"chkGroupDiv();\" ");
        $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_addr, get_count($opt_addr));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力順ラジオボタン 1:年組番号順 2:成績順
        $opt_sort = array(1, 2);
        $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "2" : $model->field["SORT_DIV"];
        $extra = array("id=\"SORT_DIV1\"", "id=\"SORT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        if ($model->z010 == 'yamamura') {
            //CSVボタンを作成する
            $extra = "onclick=\"return btn_submit('csv');\"";
            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        }
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD617V");
        knjCreateHidden($objForm, "IMAGE_PATH", "/usr/local/development/src/image");
        knjCreateHidden($objForm, "SUBCLASS_GROUP", $model->subclassGroup);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useLc_Hrclass", $model->Properties["useLc_Hrclass"]);

        knjCreateHidden($objForm, "selectGradeHrClass");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd617vForm1.html", $arg); 
    }
}
//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    if ($model->field["PRINT_DIV"] == 1) {
        //対象クラスリストを作成する
        $query = knjd617vQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS_LIST");
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

        //出力対象一覧リストを作成する//
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

        //extra
        $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";

    }

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
}
//コンボ作成
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//
function rows($db, $query) {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = $row;
    }
    $result->free();
    return $opt;
}
?>
