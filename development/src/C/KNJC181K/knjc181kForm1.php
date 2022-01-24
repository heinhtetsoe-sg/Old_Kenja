<?php

require_once('for_php7.php');

class knjc181kForm1
{
    function main(&$model){

        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc181kForm1", "POST", "knjc181kindex.php", "", "knjc181kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //印刷範囲
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", str_replace("-", "/", CTRL_DATE));
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", str_replace("-", "/", CTRL_DATE));

        //集計範囲選択ラジオボタン 1:日別 2:集計結果
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "2" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $arg, $db, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc181kForm1.html", $arg); 
    }

}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //発行ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //閉じるボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, &$arg, $db, $model, $seme, $semeflg)
{
    createHidden($objForm, "DBNAME", DB_DATABASE);
    createHidden($objForm, "PRGID", "KNJC181K");
    createHidden($objForm, "cmd");
    createHidden($objForm, "SSEMESTER");
    createHidden($objForm, "ESEMESTER");
    createHidden($objForm, "STARTDAY", $model->control["学期開始日付"][9]);
    createHidden($objForm, "ENDDAY", $model->control["学期終了日付"][9]);
    createHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

    $result = $db->query(knjc181kQuery::getSemester());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["CHECK_SEM"][] = $row["SEMESTER"].":".str_replace("-", "", $row["SDATE"]).":".str_replace("-", "", $row["EDATE"]);
    }
    $result->free();
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra, $type = "button")
{
    $objForm->ae( array("type"      => $type,
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHidden(&$objForm, $name, $value = "")
{
    $objForm->ae( array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value));

    return $objForm->ge($name);
}

?>
