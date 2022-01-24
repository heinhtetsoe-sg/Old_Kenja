<?php

require_once('for_php7.php');

class knjh312form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh312index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //学籍基礎マスタより名前を取得
        $nameArray = $db->getRow(knjh312Query::getName($model), DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $model->year."年度　".$nameArray["HR_NAME"]."　".$nameArray["ATTENDNO"]."番　氏名：".$nameArray["NAME"];

        //オール選択チェックボックス作成
        makeAllCheck($objForm, $arg, "TEST_ALL_CHECK", "test");

        //明細ヘッダデータ作成
        $subclass = makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        $testAry = makeMeisai($objForm, $arg, $db, $model, $subclass, $nameArray["GRADE"]);

        //リンク作成
        makeLink($arg, $model->lastyear, $model->lastseme, "LINKLAST", $model->schregno);
        makeLink($arg, $model->nextyear, $model->nextseme, "LINKNEXT", $model->schregno);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model, $testAry["SCORE"], $testAry["DEVIATION"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh312Form1.html", $arg);
    }
}

//ALLチェックボックス作成
function makeAllCheck(&$objForm, &$arg, $name, $cmd)
{
    $arg[$name] = createCheckBox($objForm, $name, "ON", " onClick=\"allCheck('".$cmd."', this)\";", "1");
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model)
{
    $width = 20;

    $head  = "<th align=\"center\" width=* colspan=2 nowrap >科目（講座）</th> ";
    $subclass[0] = array("code"  => "*", "value" => "HEAD");
    $i = 1;
    $result = $db->query(knjh312Query::getTestSubclass($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $head .= "<th width=38 align=\"center\" nowrap >".$row["LABEL"]."</th> ";

        $subclass[$i] = array("code"  => $row["VALUE"], "value" => $row["LABEL"]);
        $i++;
        $width += 4;
    }
    $result->free();
    $arg["WIDTH"] = $width."%";
    $arg["HEAD"] = $head;
    $arg["COL"]  = $i;
    //スクロールバーの表示
    if ($i > 20) {
        $arg["over"] = $i;
    }
    return $subclass;
}

//明細
function makeMeisai(&$objForm, &$arg, $db, $model, $subclass, $grade)
{
    $i = 0;
    $lineH = 24;    //1行の高さ
    $overwith = $lineH * 2 + 25;
    $result = $db->query(knjh312Query::getSemester($model));
    while ($semeData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $testAry = setMeisai($objForm, $arg, $db, $model, $scoreArr, $deviationArr, $subclass, $semeData, $i, $grade);
        setAbsent($arg, $db, $model, $semeData, $subclass);
    }
    $overwith += ($lineH * (get_count($testAry) + (get_count($testAry["SCORE"]) * 3)));
    $arg["OVERWITH"] = $overwith;
    $result->free();
    return $testAry;
}

//明細設定
function setMeisai(&$objForm, &$arg, $db, $model, &$scoreArr, &$deviationArr, $subclass, $semeData, &$i, $grade)
{
    $result = $db->query(knjh312Query::getTestKind($model, $semeData["SEMESTER"]));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $checkBox = createCheckBox($objForm, "CHK_TEST".$i, "ON", "", "1");
        $head  = "<tr bgcolor=white> <th align=\"left\" width=* nowrap rowspan=3 bgcolor=white>".$checkBox.$row["TESTITEMNAME"]."</th> ";
        $head .= setMeisaiData($db, $model, $scoreArr,     $i, $row, "得点",   $subclass, "SCORE",     "black", $grade);
        $head .= setMeisaiData($db, $model, $deviationArr, $i, $row, "偏差値", $subclass, "DEVIATION", "blue", $grade);
        $head .= setMeisaiData($db, $model, $dummyAry,     0,  $row, "順位",   $subclass, "RANK",      "black", $grade);
        $arg["data"][]["MEISAI"] = $head;
        $i++;
    }
    $result->free();
    $testAry["SCORE"] = $scoreArr;
    $testAry["DEVIATION"] = $deviationArr;
    return $testAry;
}

//結果設定
function setAbsent(&$arg, $db, $model, $semeData, $subclass)
{
    $head  = "<tr bgcolor=white> <th align=\"center\" width=* nowrap bgcolor=white>".$semeData["SEMESTERNAME"]."</th> ";
    $head .= setMeisaiData($db, $model, $dummyAry, 0, $semeData, "欠課", $subclass, "ABSENT", "black");
    $arg["data"][]["MEISAI"] = $head;
}

//明細データ作成
function setMeisaiData($db, $model, &$testAry, $i, $row, $subTitle, $subclass, $scoreDiv, $color, $grade = "")
{
    $head .= "<th align=\"left\" width=* nowrap bgcolor=white>".$subTitle."</th> ";
    for ($subcnt = 1; $subcnt < get_count($subclass); $subcnt++) {
        $query = makeScoreSql($row, $subclass[$subcnt]["code"], $model, $scoreDiv, $grade);
        $score = $db->getOne($query);
        $head .= "<th width=30 align=\"center\" bgcolor=\"white\" nowrap ><font color=\"".$color."\">".$score."</font></th> ";

        $testCom = ($subcnt == get_count($subclass) - 1) ? "" : ",";
        $testAry[$i] .= $row["TESTITEMNAME"]."-".$subclass[$subcnt]["value"]."-".$score.$testCom;
    }
    $head .= ($scoreDiv == "RANK" || $scoreDiv == "ABSENT") ? "</tr>" : "</tr> <tr>";
    return $head;
}

//各項目のSQL作成
function makeScoreSql($row, $subclassCd, $model, $scoreDiv, $grade)
{
    if ($scoreDiv == "SCORE") {
        $query = knjh312Query::getScore($row["SEMESTER"], $row["TESTKINDCD"], $subclassCd, $model);
    } else if ($scoreDiv == "ABSENT") {
        $query = knjh312Query::getAbsent($row["SEMESTER"], $subclassCd, $model);
    } else {
        $query = knjh312Query::getRank($row["SEMESTER"], $row["TESTKINDCD"], $subclassCd, $model, $grade, $scoreDiv);
    }
    return $query;
}

//リンク作成
function makeLink(&$arg, $year, $semester, $name, $schregno)
{
    if ($year != "") {
        $hash = "knjh312index.php?&cmd=yearChange&YEAR=".$year."&SEMESTER=".$semester."&SCHREGNO=".$schregno;
        $arg["HEADER_".$name] = "<a href=\"" .$hash. "\" target=\"_self\">";
        $arg["FOOTER_".$name] = "</a>";
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //折れ線グラフボタン
    $url = REQUESTROOT. "/H/KNJH311/knjh311index.php";
    $arg["RADAR"] = createBtn($objForm, "RADAR", "得点(折れ線)", "onclick=\" return addDataToAppletCheck('".$url."', 'line', $model->year, $model->semester);\"");
    //レーダーチャートボタン
    $url = REQUESTROOT. "/H/KNJH311/knjh311index.php";
    $arg["LINE"] = createBtn($objForm, "LINE", "偏差値(レーダー)", "onclick=\" return addDataToAppletCheck('".$url."', 'radar', $model->year, $model->semester);\"");
    //終了ボタン
    $arg["BTN_END"] = createBtn($objForm, "BTN_END", "戻 る", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model, $score, $deviation)
{
    for ($i = 0; $i < get_count($score); $i++) {
        $objForm->ae(createHiddenAe("SCORE".$i, $score[$i]));
    }
    $objForm->ae(createHiddenAe("CHK_TEST_CNT", get_count($score)));

    for ($i = 0; $i < get_count($deviation); $i++) {
        $objForm->ae(createHiddenAe("DEVIATION".$i, $deviation[$i]));
    }
    $objForm->ae(createHiddenAe("CHK_DEVIATION_CNT", get_count($deviation)));

    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
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

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
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
