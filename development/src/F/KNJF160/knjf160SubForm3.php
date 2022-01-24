<?php

require_once('for_php7.php');

class knjf160SubForm3
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knjf160index.php", "", "subform3");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf160Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $query = knjf160Query::getRelaInfo($model);
        $cnt = get_count($db->getcol($query));
        if($model->schregno && $cnt)
        {
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row["RELABIRTHDAY"] = str_replace("/", "-", $row["RELABIRTHDAY"]);
                $row["RELATIONSHIP"] = ($row["RELATIONSHIP"]) ? $db->getOne(knjf160Query::getNameMst('H201', $row["RELATIONSHIP"])) : "";

                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $arg["data"][] = $setval;
                    $setval = $row;
                }
            }
            $arg["data"][] = $setval;
        }

        //警告メッセージを表示しない場合
        if($model->cmd == "subform3A" || $model->cmd == "subform3_clear"){
            if (isset($model->schregno) && !isset($model->warning) && $model->relano){
                $Row = $db->getRow(knjf160Query::getRelaInfo($model, $model->relano), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //親族番号
        $arg["data1"]["RELANO"] = $Row["RELANO"];

        //氏名
        $arg["data1"]["RELANAME"] = knjCreateTextBox($objForm, $Row["RELANAME"], "RELANAME", 40, 40, "");

        //氏名かな
        $arg["data1"]["RELAKANA"] = knjCreateTextBox($objForm, $Row["RELAKANA"], "RELAKANA", 80, 80, "");

        //続柄
        $query = knjf160Query::getNameMst2('H201');
        makeCmb($objForm, $arg, $db, $query, "RELATIONSHIP", $Row["RELATIONSHIP"], "", 1);

        //生年月日
        $value = ($Row["RELABIRTHDAY"] == "") ? "" : str_replace("-", "/", $Row["RELABIRTHDAY"]);
        $arg["data1"]["RELABIRTHDAY"] = View::popUpCalendar($objForm, "RELABIRTHDAY", $value);

        //健康状態
        $arg["data1"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 60, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf160SubForm3.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform3_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform3_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = " onclick=\"return btn_submit('subform3_delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform3_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "RELANO", $model->relano);
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

    $arg["data1"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
