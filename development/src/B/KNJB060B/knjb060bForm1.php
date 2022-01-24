<?php

require_once('for_php7.php');

class knjb060bForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjb060bForm1", "POST", "knjb060bindex.php", "", "knjb060bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //対象日作成(開始日)
        $model->field["START_DATE"] = $model->field["START_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["START_DATE"];
        $arg["data"]["START_DATE"]  = View::popUpCalendar($objForm, "START_DATE", $model->field["START_DATE"]);

        //対象日作成(終了日)
        $model->field["END_DATE"] = $model->field["END_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["END_DATE"];
        $arg["data"]["END_DATE"]  = View::popUpCalendar($objForm, "END_DATE", $model->field["END_DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb060bForm1.html", $arg);
    }
}

/******************************************* 以下関数 *****************************************************/

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //CSVボタンを作成する
    $extra = "onclick =\" return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $db)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", str_replace("-", "/", CTRL_DATE));
    knjCreateHidden($objForm, "LAST_DATE", str_replace("-", "/", $db->getOne(knjb060bQuery::getSemesterEdate())));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJB060B");
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
