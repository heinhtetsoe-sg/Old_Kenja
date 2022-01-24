<?php

require_once('for_php7.php');

class knjf301Form1
{
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf301index.php", "", "edit");

        $db     = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年度
        $arg["TITLE"] = "疾病等結果一覧";

        //県への報告用登録日付(テーブルは報告履歴テーブルのみ)
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", $model->execute_date, "");

        //項目名(列)
        $nameArray = array("NUTRITIONCD01"
                          ,"NUTRITIONCD02"
                          ,"NUTRITIONCD03"
                          ,"SPINERIBCD01"
                          ,"SPINERIBCD02"
                          ,"SPINERIBCD03"
                          ,"SPINERIBCD99"
                          ,"SKINDISEASECD01"
                          ,"SKINDISEASECD02"
                          ,"SKINDISEASECD03"
                          ,"SKINDISEASECD99"
                          ,"OTHERDISEASECD01"
                          ,"OTHERDISEASECD02"
                          ,"OTHERDISEASECD03"
                          ,"OTHERDISEASECD04"
                          ,"OTHERDISEASECD05"
                          ,"OTHERDISEASECD99");

        unset($model->fields["CODE"]);

        $count = 0;
        $bifKey = "";
        $result = $db->query(knjf301Query::ReadQuery($model));
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->fields["CODE"][] = $Row["GRADE"]."-".$Row["SEX"];

            //text
            $setSize = $Row["GRADE"] == "99" ? "4" : "3";
            foreach ($nameArray as $name) {
                $val = ($Row[$name] == "")? "0" : $Row[$name] ;
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "size"        => $setSize,
                                    "maxlength"   => $setSize,
                                    "multiple"    => "1",
                                    "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ",
                                    "value"       => $val ));
                $Row[$name] = $objForm->ge($name);
            }

            if ($bifKey !== $Row["GRADE"]) {
                $Row["ROWSPAN"] = $Row["GRADE"] == "99" ? 3 : 2;
            }
            $bifKey = $Row["GRADE"];

            $arg["data"][] = $Row;
            $count++;
        }

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告／PDFプレビュー", $extra);
        //報告履歴
        $query = knjf301Query::getReport($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "REPORT", $model->report, $extra, 1, 1);

        //再計算ボタン
        $extra = "onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷
        $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRGID", "KNJF301");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf301Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
