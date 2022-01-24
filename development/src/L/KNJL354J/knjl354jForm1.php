<?php

class knjl354jForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl354jForm1", "POST", "knjl354jindex.php", "", "knjl354jForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試区分コンボ作成
        $query = knjl354jQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

        //出力順ラジオボタン 1:合計得点 2:受験番号
        $opt_sort = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"]) ? $model->field["SORT"] : "1";
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], "", $opt_sort, count($opt_sort));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //科目ごとの得点テキストボックス
        $value = ($model->field["EACH_SCORE"] == "") ? "80" : $model->field["EACH_SCORE"];
        $extra = "onblur=\"score_check(this),btn_submit('knjl354j')\" STYLE=\"text-align: right\"";
        $arg["data"]["EACH_SCORE"] = knjCreateTextBox($objForm, $value, "EACH_SCORE", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl354jForm1.html", $arg); 
	}
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $default = $i = 0 ;
    $default_flg = false;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] == 1 && !$default_flg){
            $default = $i;
            $default_flg = true;
        } else {
            $i++;
        }
    }
    $result->free();

    if ($name == "TESTDIV") {
        //データが存在しない場合
        if (count($db->getcol($query)) == 0) {
            $opt[] = array("label" => "　　　", "value" => "");
        }
        $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

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
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJL354J");
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}

?>
