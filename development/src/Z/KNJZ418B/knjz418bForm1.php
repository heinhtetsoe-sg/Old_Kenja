<?php

require_once('for_php7.php');

class knjz418bForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz418bindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();
        if (!$model->field["JOBTYPE_LCD"]) {
            $model->field["JOBTYPE_LCD"] = $model->jobtype_lcd;
        }
        //大分類コンボ
        $opt = array();
        $query = knjz418bQuery::getJobtypeLcd($model);
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["JOBTYPE_LCD"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $model->field["JOBTYPE_LCD"] = ($model->field["JOBTYPE_LCD"] && $value_flg) ? $model->field["JOBTYPE_LCD"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('list')\"";
        $arg["JOBTYPE_LCD"] = knjCreateCombo($objForm, "JOBTYPE_LCD", $model->field["JOBTYPE_LCD"], $opt, $extra, 1);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz418bForm1.html", $arg);
    }
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $bifKey = "";
    $query = knjz418bQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        if ($bifKey !== $row["JOBTYPE_MCD"]) {
            $cnt = $db->getOne(knjz418bQuery::getJobtypeSCnt($row["JOBTYPE_LCD"], $row["JOBTYPE_MCD"]));
            $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
        }
        $bifKey = $row["JOBTYPE_MCD"];
        $arg["data"][] = $row;
    }
    $result->free();
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
