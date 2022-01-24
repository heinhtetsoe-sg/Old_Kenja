<?php

require_once('for_php7.php');

class knjl084jForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl084jindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $arg["CTRL_YEAR"] = $model->ObjYear ."年度";

        //開始番号
        $model->startnumber = $db->getOne(knjl084jQuery::getMaxSuccess($model->ObjYear));
        $model->startnumber = $model->startnumber == "" ? "000001" : sprintf("%06d", $model->startnumber + 1);

        $objForm->ae( array("type"       => "text",
                            "name"       => "STARTNUMBER",
                            "size"       => "6",
                            "maxlength"  => "6",
                            "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\";",
                            "value"      => $model->startnumber));

        $arg["STARTNUMBER"] = $objForm->ge("STARTNUMBER");

        //一覧表
        $result = $db->query(knjl084jQuery::GetListDt($model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["TRNCEDATE"] = str_replace("-","/",$row["TRNCEDATE"]);

            $arg["data"][] = $row;
        }

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl084jForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行ボタン作成
    $arg["btn_doit"] = createBtn($objForm, "btn_doit", "採番実行", " onClick=\"btn_submit('update');\"");
    //クリアボタン作成
    $arg["btn_clear"] = createBtn($objForm, "btn_clear", "番号クリア", " onClick=\"btn_submit('clear');\"");
    //終了ボタン作成
    $arg["btn_end"]  = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    $objForm->ae(createHiddenAe("cmd"));
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
