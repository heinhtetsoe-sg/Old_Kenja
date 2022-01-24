<?php

require_once('for_php7.php');

class knjh311form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh311index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //学籍基礎マスタより名前を取得
        $arg["SCHREGNO"] = $model->schregno;
        $nameArray = $db->getRow(knjh311Query::getName($model), DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $model->year."年度　".$nameArray["HR_NAME"]."　".$nameArray["ATTENDNO"]."番　氏名：".$nameArray["NAME"];

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
        View::toHTML($model, "knjh311Form1.html", $arg);
    }
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
    $objForm->ae(createHiddenAe("year", $model->year));
    $objForm->ae(createHiddenAe("semester", $model->semester));
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
