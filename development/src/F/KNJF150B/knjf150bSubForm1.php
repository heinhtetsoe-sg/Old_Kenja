<?php

require_once('for_php7.php');

class knjf150bSubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjf150bindex.php", "", "subform1");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //種別区分（内科）
        $model->type ='1';

        //警告メッセージを表示しない場合
        if(($model->cmd == "subform1A") || ($model->cmd == "subform1_clear")){
            if (isset($model->schregno) && !isset($model->warning)){
                $row = $db->getRow(knjf150bQuery::getRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
        } else {
            $row =& $model->field;
        }

        //extra
        $extra_int = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $extra_Decimal = "onblur=\"checkDecimal(this)\"";

        //生徒情報
        $hr_name = $db->getOne(knjf150bQuery::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //来室日付作成
        $value = ($row["VISIT_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["VISIT_DATE"]);
        $arg["data"]["VISIT_DATE"] = View::popUpCalendar($objForm, "VISIT_DATE", $value);

        //来室時間（時）
        $arg["data"]["VISIT_HOUR"] = knjCreateTextBox($objForm, $row["VISIT_HOUR"], "VISIT_HOUR", 2, 2, $extra_int);

        //来室時間（分）
        $arg["data"]["VISIT_MINUTE"] = knjCreateTextBox($objForm, $row["VISIT_MINUTE"], "VISIT_MINUTE", 2, 2, $extra_int);

        //来室校時
        $query = knjf150bQuery::getNameMstPeriod();
        makeCmb($objForm, $arg, $db, $query, "VISIT_PERIODCD", $row["VISIT_PERIODCD"], "", 1);

        //来室理由テキスト入力可コード格納
        $f200Text = array();
        $query = knjf150bQuery::getNameMst('F200');
        $result = $db->query($query);
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row1["NAMESPARE2"] == "1") {
                $f200Text[] = $row1["VALUE"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "f200Text", implode(',', $f200Text));

        //来室理由１コンボ作成
        $query = knjf150bQuery::getNameMst('F200');
        $extra = "onclick=\"OptionUse2(this, 'f200Text');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON1", $row["VISIT_REASON1"], $extra, 1);
        //来室理由１テキスト
        $extra = (in_array($row["VISIT_REASON1"], $f200Text)) ? "" : "disabled";
        $arg["data"]["VISIT_REASON1_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON1_TEXT"], "VISIT_REASON1_TEXT", 60, 90, $extra);

        //発生日付作成
        $value = ($row["OCCUR_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["OCCUR_DATE"]);
        $arg["data"]["OCCUR_DATE"] = View::popUpCalendar($objForm, "OCCUR_DATE", $value);

        //発生時間（時）
        $arg["data"]["OCCUR_HOUR"] = knjCreateTextBox($objForm, $row["OCCUR_HOUR"], "OCCUR_HOUR", 2, 2, $extra_int);

        //発生時間（分）
        $arg["data"]["OCCUR_MINUTE"] = knjCreateTextBox($objForm, $row["OCCUR_MINUTE"], "OCCUR_MINUTE", 2, 2, $extra_int);
        //発生場所コンボ作成
        $query = knjf150bQuery::getNameMst('F206');
        makeCmb($objForm, $arg, $db, $query, "OCCUR_PLACE", $row["OCCUR_PLACE"], "", 1);

        //処置テキスト入力可コード格納
        $treatmentText = array();
        $query = knjf150bQuery::getNameMst('F208');
        $result = $db->query($query);
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row1["NAMESPARE2"] == "1") {
                $treatmentText[] = $row1["VALUE"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "treatmentText", implode(',', $treatmentText));

        //処置１コンボ
        $extra = "onclick=\"OptionUse2(this, 'treatmentText');\"";
        makeCmb($objForm, $arg, $db, $query, "TREATMENT1", $row["TREATMENT1"], $extra, 1);
        //処置１テキスト
        $extra = (in_array($row["TREATMENT1"], $treatmentText)) ? "" : "disabled";
        $arg["data"]["TREATMENT1_TEXT"] = knjCreateTextBox($objForm, $row["TREATMENT1_TEXT"], "TREATMENT1_TEXT", 60, 90, $extra);

        //原因コンボ作成
        $query = knjf150bQuery::getNameMst('F204');
        makeCmb($objForm, $arg, $db, $query, "OCCUR_CAUSE", $row["OCCUR_CAUSE"], "", 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf150bSubForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //登録ボタン
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", "onclick=\"return btn_submit('add');\"");
    //更新ボタン
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
    //削除ボタン
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");
    //クリアボタン
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('edit');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "TYPE", $model->type);

    //印刷用
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) $ext = $extra[$i-1];
        else $ext = $extra;

        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));

        $arg["data"][$name.$multi[$i]]  = $objForm->ge($name, $multi[$i]);
    }
}
?>
