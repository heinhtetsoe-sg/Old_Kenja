<?php

require_once('for_php7.php');


class knjm391mForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm391mForm1", "POST", "knjm391mindex.php", "", "knjm391mForm1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //処理日
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE));

        //DB接続
        $semester_sdate = '';
        $semester_edate = '';
        $db = Query::dbCheckOut();
        $query = knjm391mQuery::getSemester($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semester_sdate = $row["SDATE"];
            $semester_edate = $row["EDATE"];
        }
        Query::dbCheckIn($db);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $semester_sdate, $semester_edate);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm391mForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"btn_submit('csv');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "ＣＳＶ出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $semester_sdate, $semester_edate){

    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJM391M");
    knjCreateHidden($objForm, "SDATE", $semester_sdate);
    knjCreateHidden($objForm, "EDATE", $semester_edate);
    knjCreateHidden($objForm, "cmd");
}
?>
