<?php

require_once('for_php7.php');

class knje370kForm1
{

    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje370kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度コンボ
        $arg["TAISYOU_YEAR"] = CTRL_YEAR;

        //コピー用データチェック 登録
        $query = knje370kQuery::selectYearQuery(CTRL_YEAR);
        $yearDataCnt = $db->getOne($query);
        $query = knje370kQuery::selectYearQuery((CTRL_YEAR - 1));
        $lastYearDataCnt = $db->getOne($query);

        //前年度からコピーボタン
        $extra = "onclick=\"btn_submit('copy');\"";
        $arg["copy_btn"] = knjCreateBtn($objForm, "copy_btn", "前年度からコピー", $extra);

        //学校グループリスト取得
        $query = knje370kQuery::SelectQuery($model);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $setName = $row["COLLEGE_GRP_CD"].":".$row["COLLEGE_GRP_NAME"];
            $row["COLLEGE_GRP_CD"] = View::alink("knje370kindex.php", $setName, "target=\"right_frame\"",
                                         array("cmd"            => "edit",
                                               "COLLEGE_GRP_CD" => $row["COLLEGE_GRP_CD"]));

            $arg["data"][] = $row;
        }
        $result->free();


        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "yearDataCnt", $yearDataCnt);
        knjCreateHidden($objForm, "lastYearDataCnt", $lastYearDataCnt);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeKind") {
            $arg["jscript"] = "window.open('knje370kindex.php?cmd=edit','right_frame');";
        }

        View::toHTML($model, "knje370kForm1.html", $arg);
    }

}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else if ($name == "TAISYOU_YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
