<?php

require_once('for_php7.php');

class knjz233mForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz233mindex.php", "", "list");

        //権限チェック
        authCheck($arg);
        //DB接続
        $db = Query::dbCheckOut();

        //教科コンボボックス
        makeClassCmb($objForm, $arg, $db, $model);

        //科目リスト
        makeSubclassList($arg, $db, $model);

        //対象年度
        $arg["year"] = CTRL_YEAR;

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz233mForm1.html", $arg); 
    }
}
//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//教科コンボボックス
function makeClassCmb(&$objForm, &$arg, $db, &$model)
{
    $opt = array();
    $result = $db->query(knjz233mQuery::GetClass($model));

    //空で全教科表示とする
    $opt[] = array("label" => "", "value" => "00");
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."：".$row["CLASSNAME"],
                           "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
        }
        if($model->classcd == ""){
            $model->classcd = ($row["CLASSCD"] != "")? $row["CLASSCD"].'-'.$row["SCHOOL_KIND"] : $opt[0]["value"];
        }
    } else {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["CLASSCD"]."：".$row["CLASSNAME"],
                           "value" => $row["CLASSCD"]);
        }
        if ($model->classcd == "") {
            $model->classcd = ($row["CLASSCD"] != "") ? $row["CLASSCD"] : $opt[0]["value"];
        }
    }
    $arg["classcd"] = createCombo($objForm, "classcd", $model->classcd, $opt, "onChange=\"btn_submit('list')\"", 1);
}

//科目リスト
function makeSubclassList(&$arg, $db, $model)
{

    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        $arg["useCurriculumcd"] = "1";
    } else {
        $arg["NoCurriculumcd"] = "1";
    }
    $result = $db->query(knjz233mQuery::GetSubClass($model, $model->classcd, "MAIN"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mainData = "<tr> ";

        //更新後この行にスクロールバーを移動させる
        if ($row["VALUE"] == $model->subclasscd) {
            $row["LABEL"] = ($row["LABEL"]) ? $row["LABEL"] : "　";
            $row["LABEL"] = "<a name=\"target\">{$row["LABEL"]}</a><script>location.href='#target';</script>";
        }

        $link = View::alink("knjz233mindex.php",
                            $row["VALUE"],
                            "target=right_frame",
                            array("cmd"        => "sel",
                                  "VALUE" => $row["VALUE"]));
        //合併先
        $mainData .= "<td width=\"13%\" bgcolor=\"#ffffff\" nowrap align=\"center\" rowspan=".$row["CNT"].">".$link."</td> ";
        $mainData .= "<td width=\"30%\" bgcolor=\"#ffffff\" nowrap rowspan=".$row["CNT"].">".$row["LABEL"]."</td> ";

        //合併元
        $cnt = 0;
        $attendData = "";
        $attendsub = $db->query(knjz233mQuery::GetSubClass($model, $row["VALUE"]));
        while ($arow = $attendsub->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attendData .= ($cnt == 0) ? "" : "<tr> ";
            $attendData .= "<td width=\"*%\" bgcolor=\"#ffffff\" nowrap>".$arow["ATTEND_NAME"]."</td> </tr>";
            $cnt++;
        }
        $attendsub->free();
        $arg["data"][]["MAINLIST"] = $mainData.$attendData;
    }
    $result->free();
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
