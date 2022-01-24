<?php

require_once('for_php7.php');

class knjj144dForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjj144dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["TOP"]["CTRL_YEAR"] = $model->year;

        //学級コンボ
        $extra = "onchange=\"btn_submit('changeCmb');\"";
        $query = knjj144dQuery::getGradeHrClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //性別コンボ
        $extra = "onchange=\"btn_submit('changeCmb');\"";
        $query = knjj144dQuery::getGender($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GENDER"], "GENDER", $extra, 1, "BLANK");

        //一覧
        $model->schregnoList = array();
        $query = knjj144dQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $extra = "onblur=\"this.value=toInteger(this.value); checkTime(24, this);\" tabindex=\"-1\"";
            $row["TIME_H"] = knjCreateTextBox($objForm, $row["TIME_H"], "TIME_H_".$row["SCHREGNO"], 2, 2, $extra);

            $extra = "onblur=\"this.value=toInteger(this.value); checkTime(60, this);\"";
            $row["TIME_M"] = knjCreateTextBox($objForm, $row["TIME_M"], "TIME_M_".$row["SCHREGNO"], 2, 2, $extra);

            $extra = "onblur=\"this.value=toInteger(this.value); checkTime(60, this);\"";
            $row["TIME_S"] = knjCreateTextBox($objForm, $row["TIME_S"], "TIME_S_".$row["SCHREGNO"], 2, 2, $extra);

            $extra = "";
            $row["REMARK"] = knjCreateTextBox($objForm, $row["REMARK"], "REMARK_".$row["SCHREGNO"], 40, 20, $extra);

            $model->schregnoList[] = $row["SCHREGNO"];

            $arg["data"][] = $row;
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjj144dForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);

    //終了ボタン
    $extra = " onclick=\"return closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}
?>
