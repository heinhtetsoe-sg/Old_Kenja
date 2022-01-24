<?php

require_once('for_php7.php');

class knjl412Form1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl412index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック
        authCheck($arg);

        //年度
        $opt = array();
        $opt[] = array('label' => (CTRL_YEAR+1), 'value' => (CTRL_YEAR+1));
        $opt[] = array('label' => CTRL_YEAR,     'value' => CTRL_YEAR);
        if ($model->leftYear == "") $model->leftYear = CTRL_YEAR + 1;
        $extra = "onchange=\"return btn_submit('chgYear');\"";
        $arg["LEFT_YEAR"] = knjCreateCombo($objForm, "LEFT_YEAR", $model->leftYear, $opt, $extra, 1);

        //分類
        $query = knjl412Query::getRecruitClass($model);
        $extra = "onchange=\"return btn_submit('chgYear');\"";
        makeCmb($objForm, $arg, $db, $query, $model->leftEventClassCd, "LEFT_EVENT_CLASS_CD", $extra, 1);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からデータをコピー", $extra);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $this_cnt = $db->getOne(knjl412Query::getRecruitEventYmstCopy($model->leftYear, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $this_cnt);

        $preYear = $model->leftYear-1;
        $pre_cnt = $db->getOne(knjl412Query::getRecruitEventYmstCopy($preYear, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $pre_cnt);

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "chgYear") {
            $model->leftEventCd = "";
            $arg["reload"] = "parent.right_frame.location.href='knjl412index.php?cmd=edit';";
        }

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl412Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $result = $db->query(knjl412Query::getList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        $row["SELECT_YEAR"] = $model->leftYear;
        $arg["data"][] = $row;
    }

    $result->free();
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
?>
