<?php

require_once('for_php7.php');

class knjf160SubForm2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjf160index.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf160Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //ALLチェック
        $extra = "onClick=\"check_all(this); OptionUse('this')\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //使用テーブル
        $table = "HEALTH_AFT_SICKREC_DAT";

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $query = knjf160Query::getSubQuery1($model, $table);
        $cnt = get_count($db->getcol($query));
        if($model->schregno && $cnt)
        {
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row["S_YEAR"] = ($row["S_YEAR"]) ? $row["S_YEAR"].'年' : "";
                $row["S_MONTH"] = ($row["S_MONTH"]) ? sprintf("%01d", $row["S_MONTH"]).'月' : "";

                $row["E_YEAR"] = ($row["E_YEAR"]) ? $row["E_YEAR"].'年' : "";
                $row["E_MONTH"] = ($row["E_MONTH"]) ? sprintf("%01d", $row["E_MONTH"]).'月' : "";

                $period = ($row["S_YEAR"] || $row["S_MONTH"] || $row["E_YEAR"] || $row["E_MONTH"]) ? ' ～ ' : "";
                $row["PERIOD"] = $row["S_YEAR"].$row["S_MONTH"].$period.$row["E_YEAR"].$row["E_MONTH"];

                $row["SUSPECT_SHOW"] = ($row["SUSPECT"]) ? '○' : "";

                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {

                    $check = ($setval["SEQ"] && $model->checked && in_array($setval["SEQ"], $model->checked)) ? "checked " : "";
                    $extra = $check." onclick=\"OptionUse('this');\"";
                    $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $setval["SEQ"], $extra, "1");
                    $arg["data"][] = $setval;
                    $setval = $row;
                }
            }

            $check = ($setval["SEQ"] && $model->checked && in_array($setval["SEQ"], $model->checked)) ? "checked " : "";
            $extra = $check." onclick=\"OptionUse('this');\"";
            $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED",  $setval["SEQ"], $extra, "1");

            $arg["data"][] = $setval;
        }

        //警告メッセージを表示しない場合
        if($model->cmd == "subform2A" || $model->cmd == "subform2_clear"){
            if (isset($model->schregno) && !isset($model->warning) && $model->seq){
                $Row = $db->getRow(knjf160Query::getSubQuery1($model, $table, $model->seq), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //SEQ
        $arg["data1"]["SEQ"] = $Row["SEQ"];

        //病名
        $arg["data1"]["DISEASE"] = knjCreateTextBox($objForm, $Row["DISEASE"], "DISEASE", 40, 40, "");

        //extra
        $extra_year = "STYLE=\"text-align: right\" onBlur=\"this.value=toInteger(this.value); YearCheck(this);\"";
        $extra_month = "STYLE=\"text-align: right\" onBlur=\"this.value=toInteger(this.value); MonthCheck(this);\"";
        $extra_telno = "onBlur=\"this.value=toTelNo(this.value);\"";

        //開始年
        $arg["data1"]["S_YEAR"] = knjCreateTextBox($objForm, $Row["S_YEAR"], "S_YEAR", 4, 4, $extra_year);

        //開始月
        $arg["data1"]["S_MONTH"] = knjCreateTextBox($objForm, $Row["S_MONTH"], "S_MONTH", 2, 2, $extra_month);

        //終了年
        $arg["data1"]["E_YEAR"] = knjCreateTextBox($objForm, $Row["E_YEAR"], "E_YEAR", 4, 4, $extra_year);

        //終了月
        $arg["data1"]["E_MONTH"] = knjCreateTextBox($objForm, $Row["E_MONTH"], "E_MONTH", 2, 2, $extra_month);

        //病院
        $arg["data1"]["HOSPITAL"] = knjCreateTextBox($objForm, $Row["HOSPITAL"], "HOSPITAL", 40, 40, "");

        //主治医
        $arg["data1"]["DOCTOR"] = knjCreateTextBox($objForm, $Row["DOCTOR"], "DOCTOR", 40, 40, "");

        //電話番号
        $arg["data1"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra_telno);

        //服用薬
        $arg["data1"]["MEDICINE"] = knjCreateTextBox($objForm, $Row["MEDICINE"], "MEDICINE", 40, 40, "");

        //経過
        $arg["data1"]["SITUATION"] = knjCreateTextBox($objForm, $Row["SITUATION"], "SITUATION", 80, 80, "");

        //出席停止チェックボックス
        $extra = ($Row["SUSPECT"] == "1") ? "checked" : "";
        $arg["data1"]["SUSPECT"] = knjCreateCheckBox($objForm, "SUSPECT", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf160SubForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタンを作成する
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", "onclick=\"return btn_submit('subform2_insert');\"");
    //更新ボタンを作成する
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('subform2_update');\"");
    //削除ボタンを作成する
    $disabled = ($model->checked) ? "" : "disabled";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $disabled." onclick=\"return btn_submit('subform2_delete');\"");
    //クリアボタンを作成する
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('subform2_clear');\"");
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);
    knjCreateHidden($objForm, "cmd");
}
?>
