<?php

require_once('for_php7.php');
//ビュー作成用クラス
class knjl100oForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl100oindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度学期表示
        $arg["YEAR"] = $model->examyear;

        //出力対象ラジオボタンを作成する
        $opt[0]=1;
        $opt[1]=2;
        createRadio($objForm, $arg, "OUTPUTDIV", $model->outputdiv, "", $opt, get_count($opt));

        //合格コース
        $opt = array();
        $result = $db->query(knjl100oQuery::getExamCourse($model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"],
                           "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
        }
        $arg["EXAMCOURSE"] = createCombo($objForm, "EXAMCOURSE", $model->examcourse, $opt, "", 1);

        //入学クラス
        $opt_class = array();
        $result = $db->query(knjl100oQuery::getNameMst($model->examyear, 'L017'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_class[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
        $arg["ENTCLASS"] = createCombo($objForm, "ENTCLASS", $model->entclass, $opt_class, "", 1);


        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl100oForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = createBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
    //終了
    $arg["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, &$value, $extra, $multi, $count)
{
    $value = isset($value) ? $value : "1";

    $objForm->ae(array("type"      => "radio",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));
    for ($i = 1; $i <= $count; $i++) {
        $arg[$name.$i] = $objForm->ge($name, $i);
    }
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae(array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae(array("type"      => "button",
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
