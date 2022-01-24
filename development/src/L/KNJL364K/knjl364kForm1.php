<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjl364kForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl364kindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度学期表示
        $arg["YEAR"] = $model->examyear;

        //合格コース
        $opt = array();
        $model->examcourseall = array();
        $result = $db->query(knjl364kQuery::getExamCourse($model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"],
                           "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
            $model->examcourseall[] = $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"];
        }

        $opt[] = array("label" => "全部","value" => "99999999");

        $arg["EXAMCOURSE"] = createCombo($objForm, "EXAMCOURSE", $model->examcourse, $opt, "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl364kForm1.html", $arg);
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
