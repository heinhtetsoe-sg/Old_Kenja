<?php

require_once('for_php7.php');

class knjc161Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjc161Form1", "POST", "knjc161index.php", "", "knjc161Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        //makeCmb2
        $query = knjc161Query::getA023($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");

        //対象日作成(開始日)
        $sdRef = strtotime(CTRL_DATE);
        $prtsDate = date("Y/m/01", $sdRef);
        $model->field["STRT_DATE"] = $model->field["STRT_DATE"] == "" ? str_replace("-", "/", $prtsDate) : $model->field["STRT_DATE"];
        $arg["data"]["STRT_DATE"]  = View::popUpCalendar($objForm, "STRT_DATE", $model->field["STRT_DATE"]);

        //対象日作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"]  = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //1:全校指定月 2:クラス指定月 3:全校月毎
        $opt = array(1, 2, 3);
        $model->field["OUTPUT_SELECT"] = ($model->field["OUTPUT_SELECT"] == "") ? "1" : $model->field["OUTPUT_SELECT"];
        $extra = array("id=\"OUTPUT_SELECT1\" onclick=\"return btn_submit('knjc161')\"", "id=\"OUTPUT_SELECT2\" onclick=\"return btn_submit('knjc161')\"", "id=\"OUTPUT_SELECT3\" onclick=\"return btn_submit('knjc161')\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_SELECT", $model->field["OUTPUT_SELECT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc161Form1.html", $arg);
    }
}
/******************************************* 以下関数 *****************************************************/
//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}


//hidden作成
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", str_replace("-","/",CTRL_DATE));
    knjCreateHidden($objForm, "LAST_DATE", str_replace("-","/",$db->getOne(knjc161Query::getSemesterEdate($model))));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJC161");
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
?>
