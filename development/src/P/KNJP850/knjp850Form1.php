<?php

require_once('for_php7.php');

class knjp850form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp850index.php", "", "edit");

        $db = Query::dbCheckOut();

        //学籍番号・氏名
        $arg["TOP"]["DISP_SCHREGNO"] = $model->schregno;
        $arg["TOP"]["DISP_NAME"]     = $db->getOne(knjp850Query::getSchregName($model));

        //カテゴリ選択
        $extra = " onchange=\"return btn_submit('list')\" ";
        $query = knjp850Query::getNameMst($model, "P010");
        $arg["TOP"]["OPT_CONTACT_DIV"] = makeCmb($objForm, $arg, $db, $query, $model->optContactDiv, "OPT_CONTACT_DIV", $extra, "1", "ALL");

        //表示順
        $opt = array(1, 2, 3, 4);
        $model->optSort = ($model->optSort == "") ? "1" : $model->optSort;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OPT_SORT{$val}\" onClick=\"btn_submit('list')\"");
        }
        $radioArray = knjCreateRadio($objForm, "OPT_SORT", $model->optSort, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //メイン
        $query = knjp850Query::selectMainQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //表示名作成
            $row["DISP_NAME"] = makeDispName($row);

            //完了日
            $row["DISP_EDATE"] = str_replace("-", "/", $row["CONTACT_EDATE"]);
            $arg["data"][] = $row;
        }

        //Hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list") {
            $arg["reload"]  = "parent.edit_frame.location.href='knjp850index.php?cmd=edit&SCHREGNO={$model->schregno}'";
        }

        View::toHTML($model, "knjp850Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $all = "")
{
    $opt = array();
    if ($all == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "{$all}");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//表示名文字列作成
function makeDispName($row)
{
    //表示文字列作成
    $dispNameTop = str_replace("-", "/", $row["CONTACT_SDATE"])." - ".$row["SEQ"];
    $dispNameMid = $row["CONTACT_DIV"].":".$row["CONTACT_DIV_NAME"];
    $dispNameBtm = $row["STAFFNAME_SHOW"];

    //リンク作成
    $aHash["cmd"] = "edit";
    $aHash["CONTACT_SDATE"] = $row["CONTACT_SDATE"];
    $aHash["SEQ"] = $row["SEQ"];
    $dispNameTop = View::alink("knjp850index.php", $dispNameTop, "target=edit_frame", $aHash);
    
    return sprintf("%s<br>%s<br>%s", $dispNameTop, $dispNameMid, $dispNameBtm);
}
