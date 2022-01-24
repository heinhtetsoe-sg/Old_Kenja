<?php

require_once('for_php7.php');

class knjh330aForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh330aindex.php", "", "edit");

        $model->mockyear = ($model->mockyear) ? $model->mockyear : CTRL_YEAR;

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjh330aQuery::getRow($model->mockdiv.$model->mockyear.$model->company.$model->grade.$model->mockcd);
            $Row["MOCK_DIV"] = substr($Row["MOCKCD"], 0, 1);
            $Row["GRADE"] = "0".substr($Row["MOCKCD"], 6,1);
            $Row["MOCKCD"] = substr($Row["MOCKCD"], 7);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->mockyear;

        //模試種別
        $query = knjh330aQuery::getMockDiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->mockdiv, "MOCK_DIV", $extra);
        $arg["data"]["MOCK_DIV"] = "外部模試";
        knjCreateHidden($objForm, "MOCK_DIV","1");

        //模試コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MOCKCD"] = createTextBox($objForm, $Row["MOCKCD"], "MOCKCD", 2, 2, $extra);

        //学年
        $query = knjh330aQuery::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra);

        //模試名称
        $arg["data"]["MOCKNAME1"] = createTextBox($objForm, $Row["MOCKNAME1"], "MOCKNAME1", 40, 40, "");

        //模試略称１
        $arg["data"]["MOCKNAME2"] = createTextBox($objForm, $Row["MOCKNAME2"], "MOCKNAME2", 40, 40, "");

        //模試略称２
        $arg["data"]["MOCKNAME3"] = createTextBox($objForm, $Row["MOCKNAME3"], "MOCKNAME3", 40, 40, "");

        //業者コード
        $query = knjh330aQuery::getCompanycd($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["COMPANYCD"], "COMPANYCD", $extra, "BLANK");

        //試験コード
        $extra = "";
        $arg["data"]["COMPANYMOSI_CD"] = createTextBox($objForm, $Row["COMPANYMOSI_CD"], "COMPANYMOSI_CD", 8, 8, $extra);

        //通知表試験名
        $arg["data"]["TUUCHIHYOU_MOSI_NAME"] = createTextBox($objForm, $Row["TUUCHIHYOU_MOSI_NAME"], "TUUCHIHYOU_MOSI_NAME", 40, 40, "");

        //進路指導試験名
        $arg["data"]["SINROSIDOU_MOSI_NAME"] = createTextBox($objForm, $Row["SINROSIDOU_MOSI_NAME"], "SINROSIDOU_MOSI_NAME", 40, 40, "");

        //試験種別
        $query = knjh330aQuery::getMosiDiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["MOSI_DIV"], "MOSI_DIV", $extra, "BLANK");

        //試験日
        $arg["data"]["MOSI_DATE"] = View::popUpCalendar($objForm,"MOSI_DATE",str_replace("-","/",$Row["MOSI_DATE"]));

        //ファイル名
        $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $arg["data"]["FILE_NAME"] = createTextBox($objForm, $Row["FILE_NAME"], "FILE_NAME", 100, 100, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "edit" && $model->cmd != "reset") {
            $arg["reload"]  = "parent.left_frame.location.href='knjh330aindex.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh330aForm2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, 1);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = createBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row)
{
    $objForm->ae(createHiddenAe("cmd"));
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

//テキスト作成
function createTextBox(&$objForm, $value, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $value,
                        "extrahtml" => $extra) );
    return $objForm->ge($name);
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
