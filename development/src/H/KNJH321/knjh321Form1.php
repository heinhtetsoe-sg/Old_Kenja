<?php

require_once('for_php7.php');

class knjh321form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh321index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //学籍基礎マスタより名前を取得
        $arg["SCHREGNO"] = $model->schregno;
        $nameArray = $db->getRow(knjh321Query::getName($model->schregno), DB_FETCHMODE_ASSOC);
        $arg["NAME"] = CTRL_YEAR."年度　".$nameArray["HR_NAME"]."　".$nameArray["ATTENDNO"]."番　氏名：".$nameArray["NAME"];

        //目標値コンボ1
        makeTargetCmb($objForm, $arg, $db, $model, $model->mock_target[0], "MOCK_TARGET_CD1", 1, 2);
        setTargetScore($objForm, $db, $model, $model->mock_target[0], "目標値１", "MOCKTARGET1", "SUBTARGET1");
        //目標値コンボ2
        makeTargetCmb($objForm, $arg, $db, $model, $model->mock_target[1], "MOCK_TARGET_CD2", 0, 2);
        setTargetScore($objForm, $db, $model, $model->mock_target[1], "目標値２", "MOCKTARGET2", "SUBTARGET2");
        //目標値コンボ3
        makeTargetCmb($objForm, $arg, $db, $model, $model->mock_target[2], "MOCK_TARGET_CD3", 0, 1);
        setTargetScore($objForm, $db, $model, $model->mock_target[2], "目標値３", "MOCKTARGET3", "SUBTARGET3");

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model);

        //グラフ表示
        if ("" != $model->adpara) {
            $arg["jscript"] = "addDataToApplet();";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh321Form1.html", $arg);
    }
}

//目標値コンボ作成
function makeTargetCmb(&$objForm, &$arg, $db, $model, &$value, $name, $tar1, $tar2)
{
    //他の目標値コンボで選択されている、データはセットしない。
    $target1 = ($model->mock_target[$tar1]) ? preg_split("/,/", $model->mock_target[$tar1]) : array("","","");
    $target2 = ($model->mock_target[$tar2]) ? preg_split("/,/", $model->mock_target[$tar2]) : array("","","");
    $select1Cd1 = $target1[1];
    $select1Cd2 = $target2[1];

    $query = knjh321Query::getMockTarget($model, $select1Cd1, $select1Cd2);
    $extra = "onChange=\"btn_submit('changetarget')\";";
    makeCmb($objForm, $arg, $db, $query, $value, $name, $extra, "BLANK");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];

    $arg[$name] = createCombo($objForm, $name, $value, $opt, $extra, 1);
}

//目標値
function setTargetScore(&$objForm, $db, $model, $value, $testName, $mockName, $subName)
{
    $result = $db->query(knjh321Query::getMockData($model));
    $mockNameAr = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mockNameAr[] = $row["LABEL"];
    }
    $result->free();

    $targetmain = ($value) ? preg_split("/,/", $value) : array("","","");
    $subDiv = preg_split("/,/", $model->paraSubcd);

    $result = $db->query(knjh321Query::getTargetScore($targetmain[0], $targetmain[2], $targetmain[1]));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mockData .= $testName."-".$row["SUBCLASS_ABBV"]."-".$row["DEVIATION"].",";
        if (in_array($row["MOCK_SUBCLASS_CD"], $subDiv)) {
            for ($i = 0; $i < get_count($mockNameAr); $i++) {
                $subData .= $testName.$row["SUBCLASS_ABBV"]."-".$mockNameAr[$i]."-".$row["DEVIATION"].",";
            }
        }
    }
    $result->free();

    $objForm->ae(createHiddenAe($mockName, $mockData));
    $objForm->ae(createHiddenAe($subName, $subData));
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //終了ボタン
    $arg["BTN_END"] = createBtn($objForm, "BTN_END", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("schregno", $model->schregno));
    $objForm->ae(createHiddenAe("adpara", $model->adpara));
    $objForm->ae(createHiddenAe("cmbIndex", $model->cmbIndex));
    $objForm->ae(createHiddenAe("paraSubcd", $model->paraSubcd));
    $objForm->ae(createHiddenAe("mock_group_cd", $model->mock_group_cd));
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

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
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
