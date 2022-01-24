<?php

require_once('for_php7.php');


class knji091Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knji091Form1", "POST", "knji091index.php", "", "knji091Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //卒業年度
        $opt_year = array();
        $query = knji091Query::selectYear($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array('label' => $row["YEAR"]."年度",
                                'value' => $row["YEAR"]);
        }
        $result->free();

        if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;     //初期値：現在年度をセット。
        $extra = "onChange=\"return btn_submit('knji091');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, $extra, 1);

        if ($model->Properties["useSchool_KindField"] == "1") {
            //校種コンボ
            $query = knji091Query::getA023($model);
            $extra = "onchange=\"return btn_submit('knji091');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");
        }

        //学期コード・学年数上限
        $query = knji091Query::selectGradeSemesterDiv($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opt_Semester = isset($row["SEMESTERDIV"])?$row["SEMESTERDIV"]:"3";//データ無しはデフォルトで３学期を設定

        /* 学期コードをhiddenで送る。
         * 卒業年度が現在年度の場合：現在学期をセット。
         * 卒業年度が現在年度未満の場合：３学期をセット。
         */
        if ($model->field["YEAR"] == CTRL_YEAR) {
            $model->field["GAKKI"] = CTRL_SEMESTER;
        } else {
            $model->field["GAKKI"] = $opt_Semester;
        }

        knjCreateHidden($objForm, "GAKKI", $model->field["GAKKI"]);
        $arg["data"]["GAKKI"] = $model->control["学期名"][$model->field["GAKKI"]];

        //中高一貫
        $opt_Grade = $db->getOne(knji091Query::getNameMst()) == "1" ? "1" : "";

        //クラスコンボボックスを作成する
        $opt_grcl = array();
        $value_flg = false;
        $query = knji091Query::getAuth($model, $opt_Grade);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_grcl[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            if ($model->field["GR_CL"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["GR_CL"] = ($model->field["GR_CL"] != "" && $value_flg) ? $model->field["GR_CL"] : $opt_grcl[0]["value"];

        $extra = "onChange=\"return btn_submit('knji091');\"";
        $arg["data"]["GR_CL"] = knjCreateCombo($objForm, "GR_CL", $model->field["GR_CL"], $opt_grcl, $extra, 1);

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knji091Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($model->field["GR_CL"] || ($model->Properties["useSchool_KindField"] == "1"))) {
            //生徒項目名取得
            $sch_label = $db->getOne(knji091Query::getSchName($model));
        }
        $arg["data"]["SCH_LABEL"] = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        //生徒一覧リスト作成する
        $opt_left = $opt_right = array();
        $query = knji091Query::selectSchregno($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $name = $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"];
            //卒業生台帳設定データ無しは右、有りは左のリストに生徒を表示する。
            if ($row["SCHREGNO2"] == "") {
                $opt_right[] = array('label' => $name,
                                    'value' => $row["SCHREGNO"]);
            } else {
                $opt_left[] = array('label' => $name,
                                    'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();

        $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", $val, $opt_right, $extra, 20);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", $val, $opt_left, $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //生徒の学籍番号保管（更新用）
        knjCreateHidden($objForm, "left_list_schno");
        knjCreateHidden($objForm, "right_list_schno");
        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJI091");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knji091Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
