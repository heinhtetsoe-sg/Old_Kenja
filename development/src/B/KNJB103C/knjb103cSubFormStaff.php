<?php

require_once('for_php7.php');

class knjb103cSubFormStaff
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("substaff", "POST", "knjb103cindex.php", "", "substaff");

        if ($model->cmd != "substaff2") {
            $arg["isStaffMainSub"] = "1";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $arg["TITLE"] = CTRL_YEAR.'年度　職員一覧';

        makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, "");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb103cSubFormStaff.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    $i = 0;
    $query = knjb103cQuery::getStaffMstProctor($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");

        $checked = "";
        if ($row["STAFFCD"] == $model->staffStaffcd) {
            $checked = " checked";
        }

        //選択チェックボックス
        $check  = "onclick=\"checkboxSel(this)\"";
        $check .= $checked;
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["STAFFCD"], $check, "1");

        $row["STAFF_MAIN"] = $row["CHARGEDIV"] == "1" ? "○" : "";
        $row["STAFF_SUB"] = $row["CHARGEDIV"] == "0" ? "○" : "";

        $arg["data"][] = $row;
        //名称
        knjCreateHidden($objForm, "STAFFNAME".$i, $row["STAFFNAME"]);

        $i++;
    }
    $result->free();

    //選択ボタン
    $extra = "onclick=\"return btn_submit('".$i."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
    knjCreateHidden($objForm, "GET_COUNTER", $model->counter);
}
