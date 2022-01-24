<?php

require_once('for_php7.php');

class knjh565Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh565Form1", "POST", "knjh565index.php", "", "knjh565Form1");
        //DB接続
        $db = Query::dbCheckOut();


        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjh565Query::getSemester();
        $extra = "onchange=\"return btn_submit('knjh565')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ作成
        $query = knjh565Query::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('knjh565')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //データ種別コンボ作成
        $query = knjh565Query::getDataDiv();
        $extra = "onchange=\"return btn_submit('knjh565')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->field["PROFICIENCYDIV"], $extra, 1);

        //テスト名称コンボ作成
        $query = knjh565Query::getProName($model);
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->field["PROFICIENCYCD"], "", 1);

        //平均点・順位ラジオボタン 各値 1:学年　2:クラス　3:コース　4:学科　5:コースグループ
        $set_group_div = "";
        $opt_group = array();
        $rankdiv_array = array();
        $check_array = array();
        //プロパティの値チェック
        $rankdiv_array = explode("-", $model->Properties["useRadioPattern"]);
        foreach($rankdiv_array as $key => $val) {
            if ($val != "1" && $val != "2" && $val != "3" && $val != "4" && $val != "5") {
                //値が不正の場合は下記をセット
                $model->Properties["useRadioPattern"] = "1-2";
            }
        }
        //プロパティの値をセット
        $rankdiv_array = explode("-", $model->Properties["useRadioPattern"]);
        foreach($rankdiv_array as $key => $val) {
            $opt_group[(int)$key + 1] = $val;
            //5:コースグループの初期値をセット（プロパティusePerfectCourseGroup用）
            if ($val == "5") {
                $set_group_div = (int)$key + 1;
            }
        }
        $set_name_array = array();
        $set_name_array[1] = '学年';
        $set_name_array[2] = 'クラス';
        $set_name_array[3] = 'コース';
        $set_name_array[4] = '学科';
        $set_name_array[5] = 'コースグループ';

        //ラジオ作成
        if ($model->Properties["usePerfectCourseGroup"] === '1') {
            if ($set_group_div) {
                $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? $set_group_div : $model->field["GROUP_DIV"];
            } else {
                $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
            }
        } else {
            $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
        }
        $radioArray = array();
        $ret = array();
        for ($count = 1; $count <= get_count($opt_group); $count++) {
            $objForm->ae( array("type"      => "radio",
                                "name"      => "GROUP_DIV",
                                "value"     => $model->field["GROUP_DIV"],
                                "extrahtml" => "id=\"GROUP_DIV{$count}\"",
                                "multiple"  => $opt_group));
            $ret["GROUP_DIV".$count] = $objForm->ge("GROUP_DIV", $count);
            $arg["data"]["GROUP_DIV_NAME".$count] = $set_name_array[$opt_group[$count]].'　';
        }
        $radioArray = $ret;
        foreach($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //順位ラジオボタン 1:総合点 2:平均点 3:偏差値 4:傾斜総合点
        $opt_sort = array(1, 2, 3, 4);
        $model->field["JUNI"] = ($model->field["JUNI"] == "") ? "1" : $model->field["JUNI"];
        $extra = array("id=\"JUNI1\"", "id=\"JUNI2\"", "id=\"JUNI3\"", "id=\"JUNI4\"");
        $radioArray = knjCreateRadio($objForm, "JUNI", $model->field["JUNI"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力順ラジオボタン 1:年組番号順 2:成績順
        $opt_sort = array(1, 2);
        $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "2" : $model->field["SORT_DIV"];
        $extra = array("id=\"SORT_DIV1\"", "id=\"SORT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh565Form1.html", $arg); 
    }
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

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
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJH565");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useKnjd106cJuni1", $model->useKnjd106cJuni1);
    knjCreateHidden($objForm, "useKnjd106cJuni2", $model->useKnjd106cJuni2);
    knjCreateHidden($objForm, "useKnjd106cJuni3", $model->useKnjd106cJuni3);
    knjCreateHidden($objForm, "FORM_GROUP_DIV");
    knjCreateHidden($objForm, "useRadioPattern", $model->Properties["useRadioPattern"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
}

?>
