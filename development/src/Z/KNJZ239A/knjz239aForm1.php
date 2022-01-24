<?php

require_once('for_php7.php');

class knjz239aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjz239aForm1", "POST", "knjz239aindex.php", "", "knjz239aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        //年度コンボ
        $query = knjz239aQuery::getYear($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        $model->field["YEAR_COMB"] = $model->field["YEAR_COMB"] ? $model->field["YEAR_COMB"] : CTRL_YEAR;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["YEAR_COMB"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["YEAR_COMB"] = ($model->field["YEAR_COMB"] && $value_flg) ? $model->field["YEAR_COMB"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["YEAR_COMB"] = knjCreateCombo($objForm, "YEAR_COMB", $model->field["YEAR_COMB"], $opt, $extra, 1);

        //学年コンボ作成
        $query = knjz239aQuery::getGrade($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE"] == $row["VALUE"]) $value_flg = true;
        }
        $opt[] = array('label' => '全て', 'value' => '99');
        $model->field["GRADE"] = ($model->field["GRADE"] && $value_flg) ? $model->field["GRADE"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt, $extra, 1);

        //対象日作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz239aForm1.html", $arg);
    }
}


//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJZ239A");
    knjCreateHidden($objForm, "STAFFCD", STAFFCD);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

    //日付チェック用
    knjCreateHidden($objForm, "CHK_LDATE", str_replace("-", "/", CTRL_DATE));
    knjCreateHidden($objForm, "CHK_SDATE", $model->control['学期開始日付'][9]);
    knjCreateHidden($objForm, "CHK_EDATE", $model->control['学期終了日付'][9]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
?>
