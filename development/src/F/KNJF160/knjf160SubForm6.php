<?php

require_once('for_php7.php');

class knjf160SubForm6
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform6", "POST", "knjf160index.php", "", "subform6");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf160Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)){
            $Row = $db->getRow(knjf160Query::getNurseEnt($model), DB_FETCHMODE_ASSOC);
            $Hoken = $db->getRow(knjf160Query::getNurseEnt($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            $Hoken["REMARK"] = $model->field["INFIARMARY_REMARK"];
        }
        
        //被保険者氏名
        $arg["data"]["INSURED_NAME"] = knjCreateTextBox($objForm, $Row["INSURED_NAME"], "INSURED_NAME", 40, 40, "");

        //加入者記号
        $arg["data"]["INSURED_MARK"] = knjCreateTextBox($objForm, $Row["INSURED_MARK"], "INSURED_MARK", 40, 40, "");

        //加入者番号
        $arg["data"]["INSURED_NO"] = knjCreateTextBox($objForm, $Row["INSURED_NO"], "INSURED_NO", 20, 20, "");

        //保険者名称
        $arg["data"]["INSURANCE_NAME"] = knjCreateTextBox($objForm, $Row["INSURANCE_NAME"], "INSURANCE_NAME", 40, 40, "");

        //保険者番号
        $arg["data"]["INSURANCE_NO"] = knjCreateTextBox($objForm, $Row["INSURANCE_NO"], "INSURANCE_NO", 20, 20, "");

        //有効期限
        $arg["data"]["VALID_DATE"] = View::popUpCalendar($objForm, "VALID_DATE", str_replace("-", "/",$Row["VALID_DATE"]));

        //被扶養者認定年月
        $arg["data"]["AUTHORIZE_DATE"] = View::popUpCalendar($objForm, "AUTHORIZE_DATE", str_replace("-", "/",$Row["AUTHORIZE_DATE"]));

        //続柄
        $query = knjf160Query::getNameMst2('F240');
        makeCmb($objForm, $arg, $db, $query, "RELATIONSHIP", $Row["RELATIONSHIP"], "", 1);

        //保健室記入用
        $extra = "style=\"height:118px;\"";
        $arg["data"]["INFIARMARY_REMARK"] = knjCreateTextArea($objForm, "INFIARMARY_REMARK", 8, 101, "", $extra, $Hoken["REMARK"]);

        //注意点（項目名）
        $label = $db->getOne(knjf160Query::getNameMst('F241', '01'));
        $arg["data"]["ATTENTION_LABEL"] = (strlen($label) > 0) ? $label : "山上・海浜学校のための注意点";

        //注意点
        $arg["data"]["ATTENTION"] = knjCreateTextBox($objForm, $Row["ATTENTION"], "ATTENTION", 60, 60, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf160SubForm6.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform6_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform6_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = " onclick=\"return btn_submit('subform6_delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform6_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
