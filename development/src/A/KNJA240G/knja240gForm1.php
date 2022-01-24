<?php

class knja240gForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja240gForm1", "POST", "knja240gindex.php", "", "knja240gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //処理日
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE));

        //ボタン作成
        makeBtn($objForm, $arg);

        //学期期間日付取得
        $semester = $sep = "";
        $query = knja240gQuery::getSemesterMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->semeSDate = str_replace("-", "/", $row["SDATE"]);
            $model->semeEDate = str_replace("-", "/", $row["EDATE"]);
        }
        $result->free;

        //hidden作成
        makeHidden($objForm, $model, $semester);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja240gForm1.html", $arg);
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
function makeHidden(&$objForm, $model, $semester)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJA240G");
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SDATE", $model->semeSDate);
    knjCreateHidden($objForm, "EDATE", $model->semeEDate);
    knjCreateHidden($objForm, "cmd");
}
