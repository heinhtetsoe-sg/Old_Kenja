<?php

require_once('for_php7.php');

class knjm700dForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjm700dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHINFO"] = $model->schregno.'　'.$model->name;

        if ($model->field["SEMESTER"] == "") $model->field["SEMESTER"] = $model->expSemester;

        $query = knjm700dQuery::getSemester($model);
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //更新時に使用する管理番号を初期化
        $model->specialcd = array();

        //データを取得
        $query = knjm700dQuery::selectQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $extra = ($row["SPECIAL_FLG"] == "1") ? "checked " : "";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED",  $row["SPECIALCD"], $extra, "1");
            $model->specialcd[] = $row["SPECIALCD"];

            $row["SPECIAL_SDATE"] = str_replace("-", "/", $row["SPECIAL_SDATE"]);
            $row["SPECIAL_EDATE"] = ($row["SPECIAL_EDATE"]) ? " - ".str_replace("-", "/", $row["SPECIAL_EDATE"]) : "";

            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm700dForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタンを作成する
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
    //終了ボタンを作成する
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
            'value' => $row["VALUE"]);
    }
    $result->free();

    $value = ($value == "") ? $opt[0]["value"] : $value;

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
