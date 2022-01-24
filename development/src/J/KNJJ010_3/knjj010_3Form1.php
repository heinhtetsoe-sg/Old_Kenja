<?php

require_once('for_php7.php');


class knjj010_3Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjj010_3index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjj010_3Query::getYear($model);
        $extra = "onchange=\"return btn_submit('changeYear');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        $arg["YEAR_DISP"] = $model->year;

        //コピー元年度コンボ
        $query = knjj010_3Query::getCopyYear($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "COPY_YEAR", $model->copyYear, $extra, 1);

        //コピー
        $extra = "onclick=\"return btn_submit('updateCopy');\"";
        $arg["COPY_BTN"] = knjCreateBtn($objForm, "COPY_BTN", "コピー", $extra);

        $query = knjj010_3Query::getSchkind();
        $result = $db->query($query);
        $a023 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["kindData"][]["TITLE_KIND"] = $row["LABEL"];
            $a023[] = $row["VALUE"];
        }

        $query = knjj010_3Query::getClubMstYear($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["YEAR"] = $model->year;
            $row["SDATE"] = str_replace("-", "/", $row["SDATE"]);
            //更新後この行にスクロールバーを移動させる
            if ($row["CLUBCD"] == $model->clubcd) {
                $row["CLUBNAME"] = ($row["CLUBNAME"]) ? $row["CLUBNAME"] : "　";
                $row["CLUBNAME"] = "<a name=\"target\">{$row["CLUBNAME"]}</a><script>location.href='#target';</script>";
            }

            $remark1Array = explode(":", $row["REMARK1"]);
            foreach ($a023 as $a023Key => $a023Val) {
                $setMark = "";
                if (false !== array_search($a023Val, $remark1Array)) {
                    $setMark = "●";
                }
                $row["kindData"][]["KIND_MARK"] = $setMark;
            }

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if ($model->Properties["use_prg_schoolkind"] == "1" && $model->Properties["not_classify_schoolkind"] == "1") {
            knjCreateHidden($objForm, "SCHKIND", SCHOOLKIND);
        }

        $arg["finish"]  = $objForm->get_finish();
        if ($model->cmd == "changeYear") {
            $arg["reload"] = "parent.right_frame.location.href='knjj010_3index.php?cmd=edit';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj010_3Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
