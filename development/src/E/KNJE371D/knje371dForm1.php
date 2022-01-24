<?php

require_once('for_php7.php');


class knje371dForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje371dForm1", "POST", "knje371dindex.php", "", "knje371dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;


        //学期コンボ作成
        $query = knje371dQuery::getSchool($model);
        $extra = "onchange=\"return btn_submit('knje371d')\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_CD", $model->field["SCHOOL_CD"], $extra, 1);

        //出力種別ラジオボタンを作成
        $opt = array(1, 2);
        $model->field["RADIO"] = ($model->field["RADIO"] == "") ? "1" : $model->field["RADIO"];
        $extra = array("id=\"RADIO1\"", "id=\"RADIO2\"");
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->field["RADIO"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //学年コンボ作成
        $query = knje371dQuery::getFaculty($model, $model->field["SCHOOL_CD"]);
        $extra = "onchange=\"return btn_submit('knje371d')\"";
        makeCmb($objForm, $arg, $db, $query, "FACULTY_CD", $model->field["FACULTY_CD"], $extra, 1);

        //テストコンボ作成
        $query = knje371dQuery::getDepartment($model, $model->field["SCHOOL_CD"], $model->field["FACULTY_CD"]);
        $extra = "onchange=\"return btn_submit('knje371d')\"";
        makeCmb($objForm, $arg, $db, $query, "DEPARTMENT_CD", $model->field["DEPARTMENT_CD"], $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje371dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $opt[] = array('label' => "",
                       'value' => "");
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($value && $value_flg) {
    } else {
        $value = $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $opt;
}

function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE371D");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
}
