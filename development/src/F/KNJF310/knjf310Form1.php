<?php

require_once('for_php7.php');

class knjf310Form1
{
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf310index.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //更新する内容があった場合に日付を入力させるポップアップ
        if ($model->cmd == "fixed") {
            $arg["reload"] = " fixed('".REQUESTROOT."')";
        }

        //確定日付ありは入力不可
        $disabled = $model->fixedData ? " disabled " : "";

        //年度
        $arg["TITLE"] = "歯科検診結果一覧";

        //県への報告用登録日付(テーブルは報告履歴テーブルのみ)
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", $model->execute_date, "");

        //項目名(列)
        $nameArray = array("REMARK101"
                          ,"REMARK102"
                          ,"REMARK103"
                          ,"REMARK104"
                          ,"REMARK105"
                          ,"REMARK106"
                          ,"REMARK107"
                          ,"REMARK108"
                          ,"REMARK201"
                          ,"REMARK202"
                          ,"REMARK203"
                          ,"REMARK204"
                          ,"REMARK205"
                          ,"REMARK206"
                          ,"REMARK207"
                          ,"REMARK208"
                          ,"REMARK209"
                          ,"REMARK210"
                          ,"REMARK211"
                          ,"REMARK212");

        unset($model->fields["CODE"]);

        $count = 0;
        $bifKey = "";
        $result = $db->query(knjf310Query::ReadQuery($model));
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->fields["CODE"][] = $Row["GRADE"]."-".$Row["SEX"];

            //text
            foreach ($nameArray as $name) {
                $val = ($Row[$name] == "")? "0" : $Row[$name] ;
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name,
                                    "size"        => "3",
                                    "maxlength"   => "3",
                                    "multiple"    => "1",
                                    "extrahtml"   => $disabled."style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ",
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

        //報告済み日付
        $query = knjf310Query::getReport($model);
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
        //確定データ
        $query = knjf310Query::getFixed($model);
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
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);
        knjCreateHidden($objForm, "FIXED_DATE");
        //印刷パラメータ
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJF310");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf310Form1.html", $arg); 
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
