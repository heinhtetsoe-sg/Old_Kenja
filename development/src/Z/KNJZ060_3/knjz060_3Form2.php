<?php

require_once('for_php7.php');

class knjz060_3Form2
{
    function main(&$model)
    {
        $arg["reload"] = "";

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz060_3index.php", "", "edit");
        if ($model->school_kind == "") {
            $model->school_kind = VARS::get("SCHOOL_KIND");
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz060_3Query::getRow($model);
        } else {
            $Row     =& $model->field;
        }
        $db = Query::dbCheckOut();
        
        //教科コード、学校校種
        $arg["data"]["CLASSCD_SET"] = $Row["CLASSCD_SET"];
        $arg["data"]["CLASSNAME"]   = $Row["CLASSNAME"];
        
        //hidden
        knjCreateHidden($objForm, "CLASSCD_SET", $Row["CLASSCD_SET"]);
        knjCreateHidden($objForm, "CLASSNAME", $Row["CLASSNAME"]);
        
        //各項目のチェックボックス、テキストボックス
        for ($i = 1; $i <= 7; $i++ ) {
            //頭0埋め3桁統一
            $seq = sprintf("%03d", $i);
            //SEQ
            $arg["data"][$seq] = $seq;
            //チェックボックス
            $extra_chk  = ($Row["SEQ_".$seq]) ? "checked" : "nocheck";
            $extra_chk .= " id=\"SEQ_".$seq."\"";
            $arg["data"]["SEQ_".$seq] = createCheckBox($objForm, "SEQ_".$seq, "1", $extra_chk, "");
            //テキストボックス
            if ($seq === '004' || $seq === '005' || $seq === '007') {
                $extra = "onblur=\"this.value=toInteger(this.value)\"";
                $arg["data"]["REMARK1_".$seq] = createTextBox($objForm, $Row["REMARK1_".$seq], "REMARK1_".$seq, 3, 2, $extra);
                if ($seq === '007') {
                    $extra = "";
                    $arg["data"]["REMARK2_".$seq] = createTextBox($objForm, $Row["REMARK2_".$seq], "REMARK2_".$seq, 4, 4, $extra);
                }
            }
        
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz060_3index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz060_3Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //修正ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_udpate", "更 新", $extra);

    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタンを作成する
    $link = REQUESTROOT."/Z/KNJZ060/knjz060index.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_back"] = createBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("UPDATED", $Row["UPDATED"]));
}

//テキスト作成
function createTextBox(&$objForm, $data, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => $extra) );
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
