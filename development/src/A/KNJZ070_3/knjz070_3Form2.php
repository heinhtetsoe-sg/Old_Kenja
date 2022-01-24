<?php

require_once('for_php7.php');

class knjz070_3Form2
{
    function main($model)
    {
        $arg["reload"] = "";

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz070_3index.php", "", "edit");
        if ($model->school_kind == "") {
            $model->school_kind = VARS::get("SCHOOL_KIND");
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz070_3Query::getRow($model);
        } else {
            $Row     =& $model->field;
        }
        $db = Query::dbCheckOut();

        //科目情報表示
        $arg["data"]["SUBCLASSCD_SET"] = $Row["SUBCLASSCD_SET"];
        $arg["data"]["SUBCLASSNAME"]   = $Row["SUBCLASSNAME"];
        
        //hidden
        knjCreateHidden($objForm, "SUBCLASSCD_SET", $Row["SUBCLASSCD_SET"]);
        knjCreateHidden($objForm, "SUBCLASSNAME", $Row["SUBCLASSNAME"]);
        
        //学期数取得
        $model->semesterCount = $db->getOne(knjz070_3Query::getSemesterMst($model, "leftyear"));
        
        //宮城、常磐のSEQ=012のみを表示するプロパティ（見た目は逆）
        if ($model->Properties["kari_useMiyagiTokiwa"] !== '1') {
            $arg["kari_useMiyagiTokiwa"] = "1";
        }
        //各項目のチェックボックス、テキストボックス
        for ($i = 1; $i <= 12; $i++ ) {
            //頭0埋め3桁統一
            $seq = sprintf("%03d", $i);
            //SEQ
            $arg["data"][$seq] = $seq;
            //チェックボックス
            //$arg["data"]["SEQ_".$seq] = createCheckBox($objForm, "SEQ_".$seq, "1", ($Row["SEQ_".$seq]) ? "checked" : "nocheck", "");
            if ($seq === '007' || $seq === '008' || $seq === '012') {
                //テキストボックス
                $arg["data"]["SEQ_".$seq] = '*';
                $Row["SEQ_".$seq] = "1";
                $arg["data"]["SEMESTERNAME1"] = '前期';
                $arg["data"]["SEMESTERNAME2"] = '後期';
                $arg["data"]["REMARK1_".$seq] = createCheckBox($objForm, "REMARK1_".$seq, "1", (($Row["REMARK1_".$seq] == "1") ? "checked" : "nocheck")." id=\"REMARK1_".$seq."\"", "");
                $arg["data"]["REMARK2_".$seq] = createCheckBox($objForm, "REMARK2_".$seq, "1", (($Row["REMARK2_".$seq] == "1") ? "checked" : "nocheck")." id=\"REMARK2_".$seq."\"", "");
                if ($model->semesterCount != "2") {
                    $arg["semester3"] = "1";
                    $arg["data"]["SEMESTERNAME1"] = '1学期';
                    $arg["data"]["SEMESTERNAME2"] = '2学期';
                    $arg["data"]["SEMESTERNAME3"] = '3学期';
                    $arg["data"]["REMARK3_".$seq] = createCheckBox($objForm, "REMARK3_".$seq, "1", (($Row["REMARK3_".$seq] == "1") ? "checked" : "nocheck")." id=\"REMARK3_".$seq."\"", "");
                }
                if ($seq === '007' || $seq === '008') {
                    $arg["data"]["REMARK4_".$seq] = createCheckBox($objForm, "REMARK4_".$seq, "1", (($Row["REMARK4_".$seq] == "1") ? "checked" : "nocheck")." id=\"REMARK4_".$seq."\"", "");
                }
            } else {
                //チェックボックス
                $arg["data"]["SEQ_".$seq] = createCheckBox($objForm, "SEQ_".$seq, "1", (($Row["SEQ_".$seq]) ? "checked" : "nocheck")." id=\"SEQ_".$seq."\"", "");
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $Row);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz070_3index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz070_3Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn($objForm, $arg, $model)
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

    //戻るボタンを作成する
    $prgId = $model->getPrgId ."/" .strtolower($model->getPrgId);
    $subPrgId = substr($model->getPrgId, 3, 1);
    $link = REQUESTROOT."/{$subPrgId}/{$prgId}index.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_back"] = createBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden($objForm, $Row)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("UPDATED", $Row["UPDATED"]));
}

//テキスト作成
function createTextBox($objForm, $data, $name, $size, $maxlen, $extra)
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
function createCheckBox($objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//ボタン作成
function createBtn($objForm, $name, $value, $extra)
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
