<?php

require_once('for_php7.php');

class knjh530_2Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh530_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;

        //リスト作成
        makeList($arg, $db);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh530_2Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db)
{
    $query = knjh530_2Query::getList();
    $result = $db->query($query);
    $mainData = "";
    $proficiencyData = "";
    $gradeData = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $link = View::alink("knjh530_2index.php",
                            $row["PROFICIENCYCD"].":".$row["PROFICIENCYNAME1"],
                            "target=right_frame",
                            array("cmd"             => "edit",
                                  "PROFICIENCYDIV"  => $row["PROFICIENCYDIV"],
                                  "PROFICIENCYCD"   => $row["PROFICIENCYCD"]));
        $row["PROFICIENCYNAME1"] = $link;
        $row["PROFICIENCYDIV"] = $row["PROFICIENCYDIV"].":".$row["PROFICIENCYDIV_NAME"];
        $arg["data"][] = $row;
    }
    $result->free();
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
