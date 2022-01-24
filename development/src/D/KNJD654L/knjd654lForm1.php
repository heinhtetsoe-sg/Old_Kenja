<?php

require_once('for_php7.php');

class knjd654lForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd654lForm1", "POST", "knjd654lindex.php", "", "knjd654lForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd654lQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd654l')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, $model);
        //学年末は最新の学期とする
        $query = knjd654lQuery::getMaxSemester();
        $seme = $model->field["SEMESTER"] == "9" ? $db->getOne($query) : $model->field["SEMESTER"];

        //学年コンボ作成
        $query = knjd654lQuery::getGrade($seme);
        $extra = "onchange=\"return btn_submit('knjd654l')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, $model);

        //テスト種別コンボ
        $query = knjd654lQuery::getTestKind($model->field["SEMESTER"], $model->field["GRADE"]);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1, $model);
        
        //クラス別、コース別
        $opt = array(1, 2);
        $model->field["GROUP"] = ($model->field["GROUP"] == "") ? "1" : $model->field["GROUP"];
        $extra = array("id=\"GROUP1\"", "id=\"GROUP2\"", "id=\"GROUP3\"");
        $radioArray = knjCreateRadio($objForm, "GROUP", $model->field["GROUP"], $extra, $opt, get_count($opt));
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
        View::toHTML($model, "knjd654lForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    $value_flg = false;

    if ($name == "GRADE" && $model->field["FORM"] == "1"){
        $schoolkind = $db->getCol(knjd654lQuery::getSchregRegdGdat());
        if(in_array('H', $schoolkind)) $opt[] = array('label' =>'高校全て', 'value' => "99");
        if(in_array('J', $schoolkind)) $opt[] = array('label' =>'中学全て', 'value' => "98");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        if ($name == "GRADE"){
            $opt[] = array('label' => sprintf("%d",$row["LABEL"]).'学年',
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

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
    //ＣＳＶボタンを作成する
    $extra = "style='width: 220px; font:0.85em;' onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "クラス別平均点一覧表ＣＳＶ出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD654L");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}
?>
