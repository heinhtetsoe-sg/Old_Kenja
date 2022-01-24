<?php

require_once('for_php7.php');

class knjg045bForm2
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjg045bindex.php", "", "edit");

        if (VARS::get("cmd") == "from_right"){
            unset($model->field);
            unset($model->transfer_sdate);

            $arg["reload"] = "window.open('knjg045bindex.php?cmd=edit','edit_frame');";
        }
        //DB接続
        $db = Query::dbCheckOut();
        if($model->cmd == 'updEdit2' && (str_replace('/','-',$model->field['DIARY_DATE']) != $model->field['DIARY_DATE_DEF'])){
            $Row =array();
            $Row["DIARY_DATE"] = $model->field['DIARY_DATE'];
            $Row["DIARY_DATE_DEF"] = $model->field['DIARY_DATE'];
            $model->field["DIARY_DATE_DEF"] = $model->field['DIARY_DATE'];
        } else if($model->seq &&!$model->isWarning()) {
            $Row = $db->getRow(knjg045bQuery::getSEQDatDetail($model), DB_FETCHMODE_ASSOC);
            $Row["DIARY_DATE_DEF"] = $Row["DIARY_DATE"];
        } else {
            $Row =& $model->field;
        }
        if(!isset($Row["DIARY_DATE"])){
            $Row["DIARY_DATE"] = str_replace('-','/',$model->diarydate);
            $Row["DIARY_DATE_DEF"] = str_replace('-','/',$model->diarydate);
        }

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolkind2) {
            $sk = $db->getOne(knjg045bQuery::getSchkind($model));
            $model->schoolkind2 = (SCHOOLKIND) ? SCHOOLKIND : $sk;
        }

        $arg['data']['SEQ'] = isset($Row['SEQ'])?('('.$Row['SEQ'].')'):'';
        
        $Row["DIARY_DATE"] = str_replace("-", "/", $Row["DIARY_DATE"]);
        $arg["data"]["DIARY_DATE"] = popUpCalendarCustom($objForm, "DIARY_DATE", $Row["DIARY_DATE"]);
        
        $extra = "";
        $arg["data"]["DUMMY"] = knjCreateTextBox($objForm, $setNumerator, "DUMMY", 3, 3, $extra);
        
        $extra = '';
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->remark1_moji, $model->remark1_gyou, $Row["REMARK1"], $model);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->remark1_moji."文字X".$model->remark1_gyou."行まで)";
        $arg["data"]["REMARK2"] = getTextOrArea($objForm, "REMARK2", $model->remark2_moji, $model->remark2_gyou, $Row["REMARK2"], $model);
        $arg["data"]["REMARK2_COMMENT"] = "(全角".$model->remark2_moji."文字X".$model->remark2_gyou."行まで)";

        $arg['data']['SCHOOLCD'] = $model->schoolcd;
        $arg['data']['SCHOOL_KIND'] = $model->schoolkind2;
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEQ", $Row['SEQ']);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolkind2);
        knjCreateHidden($objForm, "DIARY_DATE_DEF", $Row["DIARY_DATE_DEF"]);
        knjCreateHidden($objForm, "SEME_SDATE", $model->sDate);
        knjCreateHidden($objForm, "SEME_EDATE", $model->eDate);

        //DB切断
        Query::dbCheckIn($db);
        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //画面のリロード

        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');parent.right_frame.btn_submit('right_list');";
        }

        if ($model->cmd == "edit2" && !$model->isWarning()){
            $arg["reload"] = "parent.left_frame.btn_submit('list');window.open('knjg045bindex.php?cmd=right_list&amp;SCHOOLCD=".$model->schoolcd."&amp;SCHOOL_KIND=".$model->schoolkind2."&amp;DIARY_DATE=".str_replace('/','-',$model->field['DIARY_DATE'])."','right_frame');";
            $model->cmd = "edit";
        }
        if ($model->cmd == "updEdit2") {
            $arg["reload"] = "window.open('knjg045bindex.php?cmd=right_list&amp;SCHOOLCD=".$model->schoolcd."&amp;SCHOOL_KIND=".$model->schoolkind2."&amp;DIARY_DATE=".str_replace('/','-',$model->field['DIARY_DATE'])."','right_frame');";
            $model->cmd = "edit";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg045bForm2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"return btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
    //カレンダーコントロール
function popUpCalendarCustom(&$form, $name, $value="",$param="")
{

    //DBからgk.cssの種類を変更したい
    $db = Query::dbCheckOut();
    $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
    $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

    $cssNo = $staffRow["FIELD1"];

    if($staffRow["FIELD2"] != "2"){
        $size = "";
    }else{
        $size = "big";
    }

    if($cssNo == ""){
        $cssNo = 1;
    }

    global $sess;
    //テキストエリア
    $form->ae( array("type"        => "text",
                    "name"        => $name,
                    "size"        => 12,
                    "maxlength"   => 12,
                    "value"       => $value));

    //読込ボタンを作成する
    $form->ae( array("type" => "button",
                    "name"        => "btn_calen",
                    "value"       => "･･･",
                    "extrahtml"   => "onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&CAL_SESSID=$sess->id&$param' + '&CSSNO=$cssNo', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"") );

    return View::setIframeJs() .$form->ge($name) .$form->ge("btn_calen");
}
?>
