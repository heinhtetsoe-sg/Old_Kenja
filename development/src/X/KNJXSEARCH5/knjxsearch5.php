<?php

require_once('for_php7.php');

class knjxsearch5
{
    function main(&$model){
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("search", "POST", "index.php", "right_frame");

        //DB接続
        $db = Query::dbCheckOut();

        //年度と学期
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["CTRL_SEMESTER"] = CTRL_SEMESTERNAME;

        //在籍生徒
        if ($model->mode == "ungrd"){
            //学年コンボボックス
            $query = knjxsearch5Query::GetHr_Class($model);
            makeCombo($objForm, $arg, $db, $query, "GRADE", $model->grade, "", 1);
        } else {
            //卒業年度コンボボックス
            $query = knjxsearch5Query::GetGrdYear($model);
            $extra = "onChange=\"return btn_submit('right')\"";
            makeCombo($objForm, $arg, $db, $query, "GRD_YEAR", $model->search["GRD_YEAR"], $extra, 1);

            //卒業時組
            $query = knjxsearch5Query::GetGrdHrClass($model);
            makeCombo($objForm, $arg, $db, $query, "HR_CLASS", $model->search["HR_CLASS"], "", 1);
        }
        
        //コースコンボボックス
        $query = knjxsearch5Query::GetCourseCode();
        makeCombo($objForm, $arg, $db, $query, "COURSECODE", "", "", 1);

        //クラブコンボボックス
        $query = knjxsearch5Query::GetClub($model);
        makeCombo($objForm, $arg, $db, $query, "CLUBCD", "", "", 1);

        //講座コンボボックス
        $query = knjxsearch5Query::GetChair($model);
        makeCombo($objForm, $arg, $db, $query, "CHAIRCD", "", "", 1);

        //漢字姓
        $arg["NAME"] = createTextBox($objForm, "", "NAME", 40, 40, "");
        //漢字姓
        $arg["NAME_SHOW"] = createTextBox($objForm, "", "NAME_SHOW", 40, 40, "");
        //かな姓
        $arg["NAME_KANA"] = createTextBox($objForm, "", "NAME_KANA", 40, 40, "");
        //英字氏名
        $arg["NAME_ENG"] = createTextBox($objForm, "", "NAME_ENG", 40, 40, "");
        //キーワード
        $arg["KEYWORD"] = createTextBox($objForm, "", "KEYWORD", 30, 30, "");

        //行動日付
        $arg["ACTIONDATE"] = View::popUpCalendar($objForm, "ACTIONDATE", "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxsearch5.html", $arg);
    }
}

//各コンボボックス
function makeCombo(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size)
{
    $opt = array();
    $opt[] = array("label"  => '',
                   "value" => '');

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $arg[$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["btn_search"] = createBtn($objForm, "btn_search", "検索", "onclick=\"return search_submit('".$model->mode."');\"");
    //閉じるボタン
    $arg["btn_end"] = createBtn($objForm, "btn_end", "終了", "onclick=\"closeWin();\"");
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//テキスト作成
function createTextBox(&$objForm, $data, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => $extra) );
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>