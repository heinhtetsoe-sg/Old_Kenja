<?php
class knje372oForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knje372oindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //参照年度
        $query = knje372oQuery::getYear("RYEAR");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->ryear, "RYEAR", $extra, 1);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["COPYBTN"] = knjCreateBtn($objForm, "COPYBTN", "左の年度データをコピー", $extra);

        //対象年度
        $query = knje372oQuery::getYear("OYEAR");
        $extra = "onchange=\" return btn_submit('changeOyear')\"";
        makeCmb($objForm, $arg, $db, $query, $model->oyear, "OYEAR", $extra, 1);

        //一覧表示
        $query = knje372oQuery::getSelectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $array = $db->getAll(knje372oQuery::getSubclass($model->oyear,$row));
            $subclass = "";
            $sep = "";
            foreach($array as $value){
                $subclass .= $sep.implode($value);
                $sep = ",";
            }
            $row["SUBCLASSNAME"] = $subclass;
            $row["RECOMMENDATION_CD"] = View::alink("knje372oindex.php", $row["RECOMMENDATION_CD"], "target=\"right_frame\"",
                                         array("cmd"               => "select",
                                               "RECOMMENDATION_CD" => $row["RECOMMENDATION_CD"],
                                               "COURSEMAJOR"       => $row["COURSEMAJOR"],
                                               "COURSECODE"        => $row["COURSECODE"],
                                               "CLASSCD"           => $row["CLASSCD"]));
            $row["RECOMMENDATION_NAME"] = $row["RECOMMENDATION_CD"].':専願'.sprintf("%02d", $row["DEPARTMENT_S"]).',併願'.sprintf("%02d", $row["DEPARTMENT_H"]);
            $arg["data"][] = $row;
        }

        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        if ($model->cmd == "changeOyear") {
            $arg["reload"] = "window.open('knje372oindex.php?cmd=edit', 'right_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje372oForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : CTRL_YEAR;
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
