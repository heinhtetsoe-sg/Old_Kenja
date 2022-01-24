<?php

require_once('for_php7.php');

class knjosearchForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"] = $objForm->get_start("knjosearchForm1", "POST", "index.php", "", "knjosearchForm1");

        $db     = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjosearchQuery::getSchkind($model);
            $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\" onChange=\"return btn_submit('');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1);
        }

        //年組リスト作成
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\" onchange=\"return btn_submit('search');\"";
        $query = knjosearchQuery::getGradeHrClass($model);
        $result = $db->query($query);
        $row1[] = array();
        $row1[0] = array("label" => "",
                        "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $arg["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->gradehrclass, $row1, $extra, 1);

        //検索ボタン
        $extra = "onclick=\"wopen('index.php?cmd=search','knjoSearch',0,0,450,250);\"";
        $arg["SEARCH_BTN"] = knjCreateBtn($objForm, "SEARCH_BTN", "検索条件入力", $extra);

        //検索結果表示
        if ($model->cmd == "search") {
            $result = $db->query(knjosearchQuery::SearchStudent($model->search_fields, $model));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            list($path, $cmd) = explode("?cmd=", $model->path);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                 array_walk($row, "htmlspecialchars_array");
                 $row["GRADUATE_CLASS"] = $row["HR_NAME"].$row["ATTENDNO"]."番";
                 $row["IMAGE"]    = $image[($row["SEXNUM"]-1)];
                 $row["BIRTHDAY"] = str_replace("-","/",$row["BIRTHDAY"]);
                 $arg["data"][]   = $row;
                 $i++;
            }
            $arg["RESULT"] = "結果　".$i."名";
            $result->free();
            if ($i == 0) {
                $arg["search_result"] = "SearchResult();";
            }
        }

        Query::dbCheckIn($db);

        //hidden(検索条件値を格納する)
        knjCreateHidden($objForm, "cmd");
        // knjCreateHidden($objForm, "GRADE_HR_CLASS");
        // knjCreateHidden($objForm, "GRADUATE_CLASS");
        // knjCreateHidden($objForm, "KANJI");
        // knjCreateHidden($objForm, "KANA");
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        knjCreateHidden($objForm, "SEMESTER", $model->control["学期"]);

        $arg["finish"]  = $objForm->get_finish();

        //当該画面で完結させる為、不要となる
        // if(VARS::post("cmd")==""){
        //     $arg["reload"] ="wopen('index.php?cmd=search_view','knjoSearch',0,0,450,250);";
        // }
        View::toHTML($model, "knjosearchForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
