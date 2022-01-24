<?php

require_once('for_php7.php');
class knjc043aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjc043aForm1", "POST", "knjc043aindex.php", "", "knjc043aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        //印刷範囲（開始日付）作成
        $model->field["DATE1"] = $model->field["DATE1"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE1"];
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $model->field["DATE1"]);

        //印刷範囲（終了日付）作成
        $model->field["DATE2"] = $model->field["DATE2"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE2"];
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $model->field["DATE2"]);

        //校時範囲ラジオボタン 1:コアタイム出力 2:全て出力
        $opt_period = array(1, 2);
        $model->field["PERIOD"] = ($model->field["PERIOD"] == "") ? "1" : $model->field["PERIOD"];
        $extra = array("id=\"PERIOD1\"", "id=\"PERIOD2\"");
        $radioArray = knjCreateRadio($objForm, "PERIOD", $model->field["PERIOD"], $extra, $opt_period, get_count($opt_period));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラスコンボ作成
        $query = knjc043aQuery::getHrClass();
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], "", 1);

        //集計票出力チェックボックス
        $extra  = ($model->field["OUTPUT1"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"OUTPUT1\"";
        $arg["data"]["OUTPUT1"] = knjCreateCheckBox($objForm, "OUTPUT1", "1", $extra, "");

        //明細票出力チェックボックス
        $extra  = ($model->field["OUTPUT2"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"OUTPUT2\"";
        $arg["data"]["OUTPUT2"] = knjCreateCheckBox($objForm, "OUTPUT2", "1", $extra, "");

        //空行をつめて印字チェックボックス
        $extra  = ($model->field["OUTPUT4"] || $model->cmd == "") ? "" : "checked";
        $extra .= " id=\"OUTPUT4\"";
        $arg["data"]["OUTPUT4"] = knjCreateCheckBox($objForm, "OUTPUT4", "1", $extra, "");

        //「未」出力チェックボックス
        $extra  = ($model->field["OUTPUT5"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"OUTPUT5\"";
        $arg["data"]["OUTPUT5"] = knjCreateCheckBox($objForm, "OUTPUT5", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc043aForm1.html", $arg); 
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJC043A");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJC043A");
    knjCreateHidden($objForm, "DATE3", CTRL_DATE);
    knjCreateHidden($objForm, "CHK_SDATE", $model->control['学期開始日付'][9]);
    knjCreateHidden($objForm, "CHK_EDATE", $model->control['学期終了日付'][9]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

    //各学期の開始日、終了日
    $seme_date  = $model->control['学期開始日付'][1].",".$model->control['学期終了日付'][1];
    $seme_date .= ",".$model->control['学期開始日付'][2].",".$model->control['学期終了日付'][2];
    $seme_date .= ",".$model->control['学期開始日付'][3].",".$model->control['学期終了日付'][3];
    knjCreateHidden($objForm, "SEME_DATE", $seme_date);
}
?>
