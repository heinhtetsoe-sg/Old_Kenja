<?php

require_once('for_php7.php');


class knjc165bForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc165bForm1", "POST", "knjc165bindex.php", "", "knjc165bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["CTRL_YEAR"] = CTRL_YEAR;

        //タイトル
        $arg["data"]["TITLE"] = "出欠席状況報告印刷画面";

        //年度コンボボックス
        $query = knjc165bQuery::getYear($model);
        $extra = "onchange=\"return btn_submit('knjc165b');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //学期コンボボックス
        //※SQLで取っているが、後でSQLだけ変えれば良いように、SQL内で固定値を設定しているので注意。
        $query = knjc165bQuery::getSemester($model);
        $extra = "onchange=\"return btn_submit('knjc165b');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMES_ID", $model->field["SEMES_ID"], $extra, 1);
        
        //校種コンボボックス
        $query = knjc165bQuery::getNameMstA023($model);
        $extra = "onchange=\"return btn_submit('knjc165b');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
        
        //課程コンボボックス
        $query = knjc165bQuery::getCourse($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "COURSECD", $model->field["COURSECD"], $extra, 1);
        
        if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $arg["dispRadio"] = "1";
            //クラス方式選択    1:法定クラス 2:実クラス
            $opt = array(1, 2);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"return btn_submit('knjc165b');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"return btn_submit('knjc165b');\"");
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        } else {
            knjCreateHidden($objForm, "HR_CLASS_TYPE", "1");
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc165bForm1.html", $arg);
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

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //保存ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'upload');\"";
    $arg["button"]["btn_upload"] = knjCreateBtn($objForm, "btn_upload", "ファイル保存", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJC165B");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

    $schoolName = explode("/", REQUESTROOT);
    knjCreateHidden($objForm, "schoolName", $schoolName[1]);
}
