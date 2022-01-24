<?php

require_once('for_php7.php');


class knja240aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja240aForm1", "POST", "knja240aindex.php", "", "knja240aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //処理日
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE));

        //フォーム選択ラジオ
        $opt = array(1, 2, 3); //1:4年生 2:6年生 3:3年生
        $model->field["FORM"] = ($model->field["FORM"] == "") ? "1" : $model->field["FORM"];
        $extra = array("id=\"FORM1\"", "id=\"FORM2\"", "id=\"FORM3\"");
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //性別欄無し
        $extra = $model->field["NO_SEIBETSU"] == "1" ? "checked='checked' " : "";
        $extra .= " id=\"NO_SEIBETSU\"";
        $arg["data"]["NO_SEIBETSU"] = knjCreateCheckBox($objForm, "NO_SEIBETSU", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //学期期間日付取得
        $semester = $sep = "";
        $query = knja240aQuery::getSemesterMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semester .= $sep.$row["SDATE"].','.$row["EDATE"];
            $sep = ':';
        }

        //hidden作成
        makeHidden($objForm, $model, $semester);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja240aForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $semester){

    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA240A");
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "GRADE_HVAL", $model->control["学年数"]);
    knjCreateHidden($objForm, "SEME_DATE", $semester);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
?>
