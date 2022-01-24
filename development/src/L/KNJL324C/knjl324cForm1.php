<?php

require_once('for_php7.php');


class knjl324cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl324cForm1", "POST", "knjl324cindex.php", "", "knjl324cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = " onChange=\"return btn_submit('knjl324c');\"";

        //入試制度コンボの設定
        $query = knjl324cQuery::getApctDiv("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボの設定
        $query = knjl324cQuery::getTestDiv("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //五條中学は、「専願」コンボをグレーアウトにする
        $disShdiv = ($model->isGojou && $model->field["APPLICANTDIV"] == "1" || $model->isCollege && $model->field["APPLICANTDIV"] == "2") ? " disabled" : "";

        //専併区分コンボの設定
        $div = (($model->field["APPLICANTDIV"] == "2" && $model->field["TESTDIV"] == "3") || $model->isGojou) ? "" : "1";
        $query = knjl324cQuery::getSHDiv("L006", $model->ObjYear, $div);
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->field["SHDIV"], $disShdiv, 1);

        //合格者区分ラジオボタン 1:合格者 2:補欠合格者 3:移行合格者
        $opt_judge = array(1, 2, 3);
        $model->field["JUDGEMENT"] = ($model->field["JUDGEMENT"] == "") ? "1" : $model->field["JUDGEMENT"];
        $extra = array("id=\"JUDGEMENT1\"", "id=\"JUDGEMENT2\"", "id=\"JUDGEMENT3\"");
        $radioArray = knjCreateRadio($objForm, "JUDGEMENT", $model->field["JUDGEMENT"], $extra, $opt_judge, get_count($opt_judge));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl324cForm1.html", $arg); 
	}
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
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
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL324C");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
