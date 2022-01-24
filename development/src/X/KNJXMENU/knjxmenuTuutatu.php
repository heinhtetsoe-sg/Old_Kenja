<?php

require_once('for_php7.php');


class knjxmenuTuutatu
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxmenuTuutatu", "POST", "index.php", "", "knjxmenuTuutatu");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //表示データ
        makeDispData($arg, $db2, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $row["STAFFCD"]);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxmenuTuutatu.html", $arg);
    }
}

//表示データ
function makeDispData(&$arg, $db2, &$model)
{
    $query = knjxmenuQuery::getTuutatu($model, "");
    $result = $db2->query($query);
    $dataCnt = 0;
    $model->TuutatuDocNumber = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $wday = array("(日)","(月)","(火)","(水)","(木)","(金)","(土)");
        $date = $row["SUBMISSION_DATE"] ? $row["SUBMISSION_DATE"] : "";
        $w = date("w", strtotime($date));
        $row["SUBMISSION_DATE"] = str_replace("-", "/", $date).$wday[$w];

        $arg["data"][] = $row;
        $model->TuutatuDocNumber[] = $row["DOC_NUMBER"];
        $dataCnt++;
    }
    $arg["DATA_CNT"] = $dataCnt;
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $staffcd)
{
    //更新
    $extra = "onClick=\"getUpd()\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "確認済", $extra);

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
