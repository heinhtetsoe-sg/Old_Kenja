<?php

require_once('for_php7.php');

class knjxattendForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "knjxattendindex.php", "", "detail");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍基礎マスタより名前を取得
        $nameArray = $db->getRow(knjxattendQuery::getName($model), DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $model->year."年度　".$nameArray["HR_NAME"]."　".$nameArray["ATTENDNO"]."番　氏名：".$nameArray["NAME"];

        //ヘッダー作成
        $semeArray = makeHead($arg, $db, $model);

        //明細データ作成
        makeMeisai($arg, $db, $model, $semeArray);

        //リンク作成
        makeLink($arg, $model->lastyear, $model->lastseme, "LINKLAST", $model->schregno);
        makeLink($arg, $model->nextyear, $model->nextseme, "LINKNEXT", $model->schregno);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxattendForm1.html", $arg);
    }
}

//ヘッダー作成
function makeHead(&$arg, $db, $model)
{
    $rtnArray = array();
    $result = $db->query(knjxattendQuery::getSemester($model));
    $head  = "<th width=\"*%\" colspan=\"2\" >出欠の記録</th> ";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $head .= "<th width=\"15%\">".$row["SEMESTERNAME"]."</th>";
        $rtnArray[] = $row["SEMESTER"];
    }
    $result->free();
    $arg["HEAD"] = $head;
    $arg["COL"]  = get_count($rtnArray);

    return $rtnArray;
}

//明細データ作成
function makeMeisai(&$arg, $db, $model, $semeArray)
{

    //学校マスタ
    $knjSchoolMst = array();
    $query = knjxattendQuery::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $knjSchoolMst[$key] = $val;
        }
    }
    $result->free();

    $setArg = setTitle($db, $model);
    for ($semCnt = 0; $semCnt < get_count($semeArray); $semCnt++) {
        $meisaiData = $db->getRow(knjxattendQuery::getMeisaiData($model, $semeArray[$semCnt], $knjSchoolMst), DB_FETCHMODE_ASSOC);
        setMeisaiData($meisaiData, $setArg);
    }

    foreach ($setArg as $key => $val) {
        $arg["MEISAI"][] = $val;
    }
}

//明細タイトル設定
function setTitle($db, $model)
{
    $thfrt = "<th align=\"center\" class=\"no_search\" nowrap ";
    $thend = "</th>";
    $rtnArray["LESSON"]     = $thfrt."colspan=2>授&nbsp;&nbsp;業&nbsp;&nbsp;日&nbsp;&nbsp;数".$thend;
    $rtnArray["OFFDAYS"]    = $thfrt."colspan=2>休&nbsp;&nbsp;学&nbsp;&nbsp;日&nbsp;&nbsp;数".$thend;
    $rtnArray["SUSPEND"]    = $thfrt."colspan=2>出席停止の日数".$thend;
    $rtnArray["MOURNING"]   = $thfrt."colspan=2>忌&nbsp;&nbsp;引&nbsp;&nbsp;の&nbsp;&nbsp;日&nbsp;&nbsp;数".$thend;
    $rtnArray["ABROAD"]     = $thfrt."colspan=2>留学中の授業日数".$thend;
    $rtnArray["CLASSDAYS2"] = $thfrt."colspan=2>出席しなければ<br>ならない日数".$thend;
    $rtnArray["SICK"]       = $thfrt."rowspan=3>欠<br>席<br>日<br>数".$thend;
    $rtnArray["SICK"]      .= $thfrt.">".$db->getOne(knjxattendQuery::getNameMst($model->year, "4")).$thend;
    $rtnArray["NOTICE"]     = $thfrt.">".$db->getOne(knjxattendQuery::getNameMst($model->year, "5")).$thend;
    $rtnArray["NONOTICE"]   = $thfrt.">".$db->getOne(knjxattendQuery::getNameMst($model->year, "6")).$thend;
    $rtnArray["CLASSDAYS3"] = $thfrt."colspan=2>出&nbsp;&nbsp;席&nbsp;&nbsp;日&nbsp;&nbsp;数".$thend;

    return $rtnArray;
}

//明細データ設定
function setMeisaiData($meisaiData, &$setArg)
{
    $tdfrt = "<td bgcolor=\"#ffffff\" height=\"30\">";
    $tdend = "</td>";
    foreach ($meisaiData as $key => $val) {
        $setArg[$key] .= $tdfrt.$val.$tdend;
    }
}

//リンク作成
function makeLink(&$arg, $year, $semester, $name, $schregno)
{
    if ($year != "") {
        $hash = "knjxattendindex.php?&cmd=yearChange&YEAR=".$year."&SEMESTER=".$semester."&SCHREGNO=".$schregno;
        $arg["HEADER_".$name] = "<a href=\"" .$hash. "\" target=\"_self\">";
        $arg["FOOTER_".$name] = "</a>";
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    $arg["button"]["BTN_END"] = createBtn($objForm, "BTN_END", "戻 る", "onclick=\"return closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
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