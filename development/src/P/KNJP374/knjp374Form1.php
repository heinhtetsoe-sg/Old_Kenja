<?php

require_once('for_php7.php');

class knjp374Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp374index.php", "", "right_list");

        $db = Query::dbCheckOut();

        //学年コンボボックス
        $query = knjp374Query::getGrade($model);
        $extra = "onchange=\"btn_submit('chngCmb')\"";
        $model->selectSchoolKind = makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "");

        //学年コンボボックス
        $query = knjp374Query::getHrClass($model);
        $extra = "onchange=\"btn_submit('chngCmb')\"";
        makeCmb($objForm, $arg, $db, $query, $model->hrClass, "HR_CLASS", $extra, 1, "BLANK");

        //振込区分コンボボックス
        $query = knjp374Query::getTransferDiv($model);
        $extra = "onchange=\"btn_submit('chngCmb')\"";
        makeCmb($objForm, $arg, $db, $query, $model->transferDiv, "TRANSFER_DIV", $extra, 1, "BLANK");

        if ($model->grade) {
            //振込区分マスタ
            $link = REQUESTROOT."/P/KNJP374_1/knjp374_1index.php?cmd=&SEND_PRGID=KNJP374&SEND_AUTH=".AUTHORITY."&SEND_GRADE=".$model->grade."&SEND_HR_CLASS=".$model->hrClass."&SEND_SCHOOL_KIND=".$model->selectSchoolKind[$model->grade];
            $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
            $arg["TRANSFER_DIV_MST"] = knjCreateBtn($objForm, "TRANSFER_DIV_MST", "振込区分登録", $extra);
        }

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //データ作成
        $this->makeData($objForm, $arg, $db, $model);

        //実行ボタンを作成する
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_execute"] = knjCreateBtn($objForm, "btn_execute", "実 行", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp374Form1.html", $arg);
    }

    function makeData(&$objForm, &$arg, $db, &$model) {
        $setval = array();  //出力データ配列
        $model->schregNoArray = array();
        $disabled = $model->transferDiv ? "" : " disabled ";
        $query = knjp374Query::getStd($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregNoArray[] = $row["SCHREGNO"];
            $setval = $row;
            $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED_{$row["SCHREGNO"]}", $row["SCHREGNO"], $disabled);
            //テキスト
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $setval["TRANSFER_MONEY"] = knjCreateTextBox($objForm, $row["TRANSFER_MONEY"], "TRANSFER_MONEY_{$row["SCHREGNO"]}", 6, 6, $extra);
            $arg["data"][] = $setval;
        }
        $result->free();
    }

}
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $retVal = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
        $retVal[$row["VALUE"]] = $row["SCHOOL_KIND"];
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
    return $retVal;
}
?>
