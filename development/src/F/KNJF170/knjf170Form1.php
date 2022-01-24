<?php

require_once('for_php7.php');

class knjf170Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjf170index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //特別支援学校では校種コンボを表示しない
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $arg["schkind"] = "1";
                $query = knjf170Query::getSchkind($model);
                $extra = "onchange=\"return btn_submit('year');\"";
                makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schoolkind, $extra, 1);
            }
        }

        //年度コンボ作成
        $query = knjf170Query::getYear($model);
        $extra = "onchange=\"return btn_submit('year');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            //学校名称2表示
            $info = $db->getRow(knjf170Query::getYear($model, $model->year), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        }

        $order[$model->sort["SRT_D"]]="";
        //ソート表示文字作成
        $order[1] = "▲";
        $order[-1] = "▼";

        //リストヘッダーソート作成
        $DATE_SORT = "<a href=\"knjf170index.php?cmd=year&sort=SRT_D\" target=\"left_frame\" STYLE=\"color:white\">日付".$order[$model->sort["SRT_D"]]."</a>";

        $arg["DATE_SORT"] = $DATE_SORT;

        //生徒項目名切替処理
        $schName = "";
        //テーブルの有無チェック
        $query = knjf170Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "")) {
            //生徒項目名取得
            $schName = $db->getOne(knjf170Query::getSchName($model));
        }
        $model->sch_label = (strlen($schName) > 0) ? $schName : '生徒';

        //生徒項目名
        $arg["SCH_LABEL"] = $model->sch_label;

        //プログラムＩＤ
        $arg["PROGRAMID"] = PROGRAMID;

        //日付一覧取得
        $result = $db->query(knjf170Query::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["DATE"] = str_replace("-", "/", $row["DATE"]);
            $row["DIARY"] = ($row["DIARY"])? "●" : "";

            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "year") {
            $arg["reload"] = "window.open('knjf170index.php?cmd=edit&YEAR={$model->year}&SENDSCHOOLKIND={$model->schoolkind}&changeFlg=1','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf170Form1.html", $arg);
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

        if ($value == $row["VALUE"]) {
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
