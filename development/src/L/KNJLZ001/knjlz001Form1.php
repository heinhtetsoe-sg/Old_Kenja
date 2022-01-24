<?php

require_once('for_php7.php');
class knjlz001Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjlz001index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度コンボ
        if ($model->field["ENTEXAMYEAR"] == "") {
            $model->field["ENTEXAMYEAR"] = CTRL_YEAR + 1;
        }
        $query = knjlz001Query::getYear();
        $extra = "onChange=\"btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["ENTEXAMYEAR"], "ENTEXAMYEAR", $extra, 1, "", "");

        //制度コンボ
        $query = knjlz001Query::getNameMst($model->field["ENTEXAMYEAR"], "L003", $model->fixApplicantDiv);
        $extra = "onChange=\"btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "", "");

        $opt = array();
        $query = knjlz001Query::getCombo($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            $opt[] = array("label" => $row["SETTING_CD"]."　".htmlspecialchars($row["CDMEMO"]),
                           "value" => $row["SETTING_CD"]);
            
            if (!isset($model->field["SETTING_CD"])) {
                $model->field["SETTING_CD"] = $row["SETTING_CD"];
            }
        }
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["top"]["SETTING_CD"] = knjCreateCombo($objForm, "SETTING_CD", $model->field["SETTING_CD"], $opt, $extra, 1);

        $query = knjlz001Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row;
        }
        $result->free();

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からデータをコピー", $extra);

        $this_cnt = $db->getOne(knjlz001Query::getCopyCnt($model->field["ENTEXAMYEAR"], "cnt"));
        knjCreateHidden($objForm, "THIS_ENTEXAMYEAR_CNT", $this_cnt);

        $preYear = $model->field["ENTEXAMYEAR"] -1;
        $pre_cnt = $db->getOne(knjlz001Query::getCopyCnt($preYear, "cnt"));
        knjCreateHidden($objForm, "PRE_ENTEXAMYEAR_CNT", $pre_cnt);

        Query::dbCheckIn($db);

        if ($model->cmd == "change") {
            $arg["reload"]  = "window.open('knjlz001index.php?cmd=edit&ENTEXAMYEAR=".$model->field["ENTEXAMYEAR"]."&APPLICANTDIV=".$model->field["APPLICANTDIV"]."','right_frame');";
        }

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjlz001Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
