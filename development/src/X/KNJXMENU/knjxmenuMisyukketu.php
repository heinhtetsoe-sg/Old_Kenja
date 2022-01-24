<?php

require_once('for_php7.php');


class knjxmenuMisyukketu
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxmenuMisyukketu", "POST", "index.php", "", "knjxmenuMisyukketu");

        //DB接続
        $db = Query::dbCheckOut();

        //表示データ
        makeDispData($arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $row["STAFFCD"]);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxmenuMisyukketu.html", $arg);
    }
}

//表示データ
function makeDispData(&$arg, $db, $model)
{
    $Z026 = $db->getRow(knjxmenuQuery::getDateCtrl(), DB_FETCHMODE_ASSOC);

    list($year, $month, $day) = preg_split("/-/", CTRL_DATE);
    $lastDay = date("Y-m-d", mktime(0, 0, 0, $month, intval($day) - 1, $year));

    $toDate = $Z026["NAMESPARE1"] == "1" ? CTRL_DATE : $lastDay;

    if ($model->syukketuDiv == 0) {
        $arg["DATA_CNT"] = $db->getOne(knjxmenuQuery::getMisyukketuPrt1($model, $toDate, "CNT"));
        $query = knjxmenuQuery::getMisyukketuPrt1($model, $toDate);
    } else {
        $arg["DATA_CNT"] = $db->getOne(knjxmenuQuery::getMisyukketuPrt2($model, $toDate, "CNT"));
        $query = knjxmenuQuery::getMisyukketuPrt2($model, $toDate);
    }

    $jumping = REQUESTROOT."/C/KNJC010/knjc010index.php";
    $sendParam = "";
    if ($model->properties["useCheckAttendInputPrg"]) {
        $upperPrg = mb_strtoupper($model->properties["useCheckAttendInputPrg"]);
        $lowerPrg = mb_strtolower($model->properties["useCheckAttendInputPrg"]);
        $jumping = REQUESTROOT."/C/{$upperPrg}/{$lowerPrg}index.php";
        $sendParam = "&SEND_AUTH=0&SEND_PRG=KNJC020A&SEND_XMENU=1";
    }

    $result = $db->query($query);

    list($semeSdate, $semeEdate) = $db->getRow(knjxmenuQuery::getSemeDate(CTRL_SEMESTER));

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $date = $row["EXECUTEDATE"];
        if ($date >= $semeSdate && $date <= $semeEdate) {
            $linkFlg = true;
        } else {
            $linkFlg = false;
        }
        $wday = array("(日)","(月)","(火)","(水)","(木)","(金)","(土)");
        $w = date("w", strtotime($date));
        $row["EXECUTEDATE"] = str_replace("-", "/", $date).$wday[$w];

        if ($linkFlg) {
            if ($model->syukketuDiv == 0) {
                $subdata = "wopen('".$jumping."?&syoribi={$date}&periodcd={$row["PERIODCD"]}&chaircd={$row["CHAIRCD"]}&staffcd=".STAFFCD."&STAFFCD=".STAFFCD."{$sendParam}&SUBWIN=SUBWIN2','name',0,0,screen.availWidth,screen.availHeight)";
                $row["EXECUTEDATE"] = View::alink("#", htmlspecialchars($row["EXECUTEDATE"]), "onclick=\"$subdata\"");
            } else {
                $subdata = "wopen('".$jumping."?&syoribi={$date}&periodcd={$row["PERIODCD"]}&grade={$row["GRADE"]}&hrclass={$row["HR_CLASS"]}&tr_cd1=".STAFFCD."&staffcd=".STAFFCD."&STAFFCD=".STAFFCD."&SUBWIN=SUBWIN2','name',0,0,screen.availWidth,screen.availHeight)";
                $row["EXECUTEDATE"] = View::alink("#", htmlspecialchars($row["EXECUTEDATE"]), "onclick=\"$subdata\"");
            }
        }

        $arg["data"][] = $row;
    }
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $staffcd)
{
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"top.right_frame.closeit();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
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

//テキスト作成
function createTextBox(&$objForm, $data, $name, $size, $maxlen, $extra)
{
    $objForm->ae(array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//テキストエリア作成
function createTextArea(&$objForm, $name, $rows, $cols, $wrap, $extra, $value)
{
    $objForm->ae(array("type"        => "textarea",
                        "name"        => $name,
                        "rows"        => $rows,
                        "cols"        => $cols,
                        "wrap"        => $wrap,
                        "extrahtml"   => $extra,
                        "value"       => $value));
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
