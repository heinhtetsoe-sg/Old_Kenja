<?php

require_once('for_php7.php');

class knjj144cForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjj144cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "checkAttendCd") {
            if (0 < $db->getOne(knjj144cQuery::checkAttendCd($model, $model->ajaxAttendCd))) {
                $checkResult = true;
            } else {
                $checkResult = false;
            }
            $response = array("checkResult" => $checkResult);
            echo json_encode($response);
            die();
        }

        $arg["TOP"]["CTRL_YEAR"] = $model->year;

        //学年コンボ
        $extra = "onchange=\"btn_submit('changeCmb');\"";
        $query = knjj144cQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //学級コンボ
        $extra = "onchange=\"btn_submit('changeCmb');\"";
        $query = knjj144cQuery::getHrClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, 1);

        //性別コンボ
        $extra = "onchange=\"btn_submit('changeCmb');\"";
        $query = knjj144cQuery::getGender($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GENDER"], "GENDER", $extra, 1, "BLANK");

        //タイム登録者は除く
        $extra  = "onclick=\"btn_submit('changeCmb');\"";
        $extra .= " id=\"IGNORE_TIME_REGISTRATION\"";
        if ($model->field["IGNORE_TIME_REGISTRATION"]) $extra .= " checked";
        $arg["IGNORE_TIME_REGISTRATION"] = knjCreateCheckBox($objForm, "IGNORE_TIME_REGISTRATION", "1", $extra, "");

        //欠席理由名称(赤文字)
        $query = knjj144cQuery::getNameMstJ010($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["NAMEJ010"] .= $row["NAME"]." ";
        }

        //一覧
        $model->schregnoList = array();
        $query = knjj144cQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $extra = "onblur=\"this.value=toInteger(this.value); checkAttendCd(this);\"";
            $row["ATTEND_CD"] = knjCreateTextBox($objForm, $row["ATTEND_CD"], "ATTEND_CD_".$row["SCHREGNO"], 2, 2, $extra);

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
        View::toHTML5($model, "knjj144cForm1.html", $arg);
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
