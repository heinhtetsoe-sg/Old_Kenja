<?php

require_once('for_php7.php');


class knjl352cForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl352cForm1", "POST", "knjl352cindex.php", "", "knjl352cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボの設定
        $query = knjl352cQuery::getApctDiv("L003", $model->ObjYear);
        $extra = " onChange=\"return btn_submit('knjl352c');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, $model);

        //入試区分コンボの設定
        $query = knjl352cQuery::getTestDiv("L004", $model->ObjYear, $model);
        $extra = " onChange=\"return btn_submit('knjl352c');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, $model);

        //学校・塾選択ラジオボタン 1:学校 2:塾 3:塾グループ
        $opt_print = array(1, 2, 3);
        $model->field["PRINT_TYPE"] = ($model->field["PRINT_TYPE"] == "") ? "1" : $model->field["PRINT_TYPE"];
        $click = " onClick=\"return btn_submit('knjl352c');\"";
        $extra  = array("id=\"PRINT_TYPE1\"".$click, "id=\"PRINT_TYPE2\"".$click, "id=\"PRINT_TYPE3\"".$click);
        $radioArray = knjCreateRadio($objForm, "PRINT_TYPE", $model->field["PRINT_TYPE"], $extra, $opt_print, get_count($opt_print));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
		if ($model->isCollege) {    // カレッジ
			$arg["USE_GRP_PRISCHOOLCD"] = '1';
		}

        //成績あり/成績なしラジオボタン 1:成績あり 2:成績なし
        $opt_score = array(1, 2);
        $model->field["PRINT_SCORE"] = ($model->field["PRINT_SCORE"] == "") ? "2" : $model->field["PRINT_SCORE"];
        $extra  = array("id=\"PRINT_SCORE1\"", "id=\"PRINT_SCORE2\"");
        $radioArray = knjCreateRadio($objForm, "PRINT_SCORE", $model->field["PRINT_SCORE"], $extra, $opt_score, get_count($opt_score));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl352cForm1.html", $arg); 
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $query = ($model->field["PRINT_TYPE"] == "1") ? knjl352cQuery::getFinchoolName($model) : knjl352cQuery::getPrischoolName($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //対象一覧リスト名
    $arg["data"]["NAME_LIST"] = ($model->field["PRINT_TYPE"] == "1") ? '学校一覧' : '塾一覧';

    //教育委員会一覧を作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象一覧を作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();

    if($name == "TESTDIV"){
        if (!$model->isGojou && !$model->isCollege) {   //和歌山
            $opt[]= array("label" => "前期・後期", "value" => "X");
            $opt[]= array("label" => "編入・スポーツコース", "value" => "B");
        }
        $opt[]= array("label" => "-- 全て --", "value" => "9");
    }

    $value = (($value && $value_flg) || $value == "X" || $value == "B" || $value == "9") ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJL352C");
    knjCreateHidden($objForm, "cmd");
}

?>
