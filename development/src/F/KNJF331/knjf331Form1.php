<?php

require_once('for_php7.php');

class knjf331Form1
{
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf331index.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();
        $db2    = Query::dbCheckOut2();

        //教育委員会判定
        $query = knjf331Query::z010Abbv1();
        $model->z010Abbv1 = $db->getOne($query);

        if ($model->z010Abbv1 == "1" || $model->z010Abbv1 == "2") {
            $arg["Z010ABBV1"] = "1";
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年度
        $arg["TITLE"] = "疾病等結果一覧";

        //更新する内容があった場合に日付を入力させるポップアップ
        if ($model->cmd == "fixed") {
            $arg["reload"] = " fixed('".REQUESTROOT."')";
        }

        //確定日付ありは入力不可
        $disabled = $model->fixedData ? " disabled " : "";

        //県への報告用登録日付(テーブルは報告履歴テーブルのみ)
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", $model->execute_date, "");

        //radio
        $opt = array(1, 2);
        $model->dataDiv = ($model->dataDiv == "") ? "1" : $model->dataDiv;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATA_DIV{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->dataDiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        if ($model->dataDiv == "1") {
            $arg["IPPAN"] = "1";
            $arg["maxPx"] = "1800";
        } else {
            $arg["HA_KOUKOU"] = "1";
            $arg["maxPx"] = "1500";
        }

        unset($model->fields["CODE"]);

        $count = 0;
        $bifKey = "";
        $query = knjf331Query::ReadQuery($model);
        $result = $db->query($query);
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->fields["CODE"][] = $Row["AGE_DATA"]."-".$Row["SEX"];

            //text
            $setSize = $Row["AGE_DATA"] == "99" ? "4" : "3";
            foreach ($model->dataField as $name) {
                $setName = "DATA".$name;
                $val = ($Row[$setName] == "")? "0" : $Row[$setName] ;

                $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ";
                $Row[$setName] = knjCreateTextBox($objForm, $val, $setName."_".$Row["AGE_DATA"]."_".$Row["SEX"], $setSize, $setSize, $disabled.$extra, "1");
            }

            if ($bifKey !== $Row["AGE_DATA"]) {
                $Row["ROWSPAN"] = $Row["AGE_DATA"] == "99" ? 3 : 2;
            }
            $bifKey = $Row["AGE_DATA"];

            $arg["data"][] = $Row;
            $count++;
        }

        //報告済み日付
        $query = knjf331Query::getReport($model);
        $setExeDate = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setExeDate .= $sep.str_replace("-", "/", $row["VALUE"]);
            $sep = ",";
        }
        $result->free();
        $arg["EXE_DATES"] = $setExeDate;

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra);

        //文書番号
        $query = knjf331Query::getTuutatu($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db2, $query, "DOC_NUMBER", $model->docNumber, $extra, 1, "BLANK");

        //確定データ
        $query = knjf331Query::getFixed($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "FIXED_DATA", $model->fixedData, $extra, 1, 1);

        //再計算ボタン
        $extra = $disabled."onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);
        //更新ボタン
        $extra = $disabled."onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = $disabled."onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷
        $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "FIXED_DATE");
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "PRGID", "KNJF331");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf331Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["LABEL"] = $name == "FIXED_DATA" ? str_replace("-", "/", $row["LABEL"]) : $row["LABEL"];
        $row["VALUE"] = $name == "FIXED_DATA" ? str_replace("-", "/", $row["VALUE"]) : $row["VALUE"];
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
