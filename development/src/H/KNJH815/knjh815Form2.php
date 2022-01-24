<?php
class knjh815Form2
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh815index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        
        //年度と学期
        if (!isset($model->exp_year)) $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;
        
        //年組番氏名
        if($model->knjid != ""){
            $query = knjh815Query::getName($model->knjid, $model->year);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $arg["GRADE"] = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
            $arg["NAME"] = $row["NAME"];
        }
        
        
        graph::CreateDataHidden($objForm, $model->graph,"0");
        
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        //View::toHTML($model, "knjh815Form2.html", $arg);
        $cssplugin = "";
        $jsplugin = "chart.js|Chart.min.js|graph.js|jquery-1.11.0.min.js";
        View::toHTML6($model, "knjh815Form2.html", $arg, $jsplugin, $cssplugin);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "戻 る", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "type", "radar");
    knjCreateHidden($objForm, "maxTicksLimit", 11);
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

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $cnt = 0;
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    if ($blank == "blank") $cnt++;
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($name == "CHAIRCD") {
            knjCreateHidden($objForm, "LIST_CHAIRCD" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
