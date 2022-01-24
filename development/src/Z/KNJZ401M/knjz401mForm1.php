<?php

require_once('for_php7.php');

class knjz401mForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz401mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //対象学年コンボ
        $query = knjz401mQuery::getGradeSemes($model);
        $extra = "onchange=\"return btn_submit('grade');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_SEMES", $model->grade_semes, $extra, 1);

        //参照学年コンボ
        $query = knjz401mQuery::getGradeSemes($model, $model->grade_semes);
        makeCmb($objForm, $arg, $db, $query, "R_GRADE_SEMES", $model->r_grade_semes, "", 1);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の学年学期のデータをコピー", $extra);

        //前年度コピーボタン
        $extra = "onclick=\"return btn_submit('pre_copy');\"";
        $arg["button"]["btn_pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //科目ごとの行数取得
        $rowCnt = array();
        $query = knjz401mQuery::getLeftList($model, $model->grade_semes, "cnt");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rowCnt[$row["SUBCLASSCD"]] = $row["CNT"];
        }

        //一覧取得
        $subclasscd = "";
        $query = knjz401mQuery::getLeftList($model, $model->grade_semes, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["ROWSPAN"] = ($rowCnt[$row["SUBCLASSCD"]] > 0) ? $rowCnt[$row["SUBCLASSCD"]] : 1;

            if ($subclasscd != $row["SUBCLASSCD"]) {
                $row["SUBCLASS_SHOW"] = 1;
            }

            $arg["data"][] = $row;
            $subclasscd = $row["SUBCLASSCD"];
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "grade") {
            $model->subclasscd = "";
            $arg["reload"] = "window.open('knjz401mindex.php?cmd=edit','right_frame')";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz401mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
