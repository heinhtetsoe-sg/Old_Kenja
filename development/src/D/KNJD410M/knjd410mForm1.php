<?php

require_once('for_php7.php');

class knjd410mForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjd410mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = $model->year;

        //校種コンボ
        $query = knjd410mQuery::getSchKind($model);
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind, $extra, 1);

        //ラジオボタン  1:法定クラス 2:実クラス
        $opt = array(1, 2);
        $model->hukusiki_radio = ($model->hukusiki_radio == "") ? "1" : $model->hukusiki_radio;
        $extra = array("id=\"HUKUSIKI_RADIO1\" onclick=\"return btn_submit('combo');\"", "id=\"HUKUSIKI_RADIO2\" onclick=\"return btn_submit('combo');\"");
        $radioArray = knjCreateRadio($objForm, "HUKUSIKI_RADIO", $model->hukusiki_radio, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //年組コンボ
        $query = knjd410mQuery::getHrClass($model);
        $extra = "onchange=\"btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->hr_class, $extra, 1, $model);
        $model->hr_class_list = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->hr_class_list[] = $row["VALUE"];
        }

        //年組コンボ（特別クラス選択時）
        if ($model->hukusiki_radio == "2") {
            $query = knjd410mQuery::getHrClass2($model);
            $extra = "onchange=\"btn_submit('combo');\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS2", $model->hr_class2, $extra, 1, $model);
            $model->hr_class2_list = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->hr_class2_list[] = $row["VALUE"];
            }
            $arg["ghr"] = 1;
        } else {
            $model->hr_class2 = "00-000";
        }

        //並び替えコンボ
        $opt = array(
            array("label" => "クラス順",       "value" => "hr_class_attendno"),
            array("label" => "科目グループ順", "value" => "groupcd")
        );
        $extra = "onchange=\"btn_submit('combo');\"";
        $arg["LIST_SORT"] = knjCreateCombo($objForm, "LIST_SORT", $model->list_sort, $opt, $extra, 1);

        //全チェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //一覧取得
        $schCount = 0;
        if (strlen($model->gakubu_school_kind) || strlen($model->hr_class)) {
            $query = knjd410mQuery::getList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //レコードを連想配列のまま配列$arg[data]に追加していく。
                array_walk($row, "htmlspecialchars_array");

                $array = (get_count($model->checked) > 0) ? $model->checked : array();
                $extra = (in_array($row["SCHREGNO"], $array) && isset($model->warning)) ? "checked" : "";
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["SCHREGNO"], $extra, "1");

                $row["GROUP"] = $row["GROUPCD"]."：".$row["GROUPNAME"];
                $row["HR_CLASS_ATTENDNO"] = $row["HR_NAME"]."　".$row["ATTENDNO"]."番";

                $arg["data"][] = $row;
                $schCount++;
            }
            $result->free();
        }

        //人数セット
        $arg["SCHREG_COUNT"] = $schCount.'人';

        //削除ボタン
        $extra  = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";
        $extra .= " onClick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            $param  = "&GAKUBU_SCHOOL_KIND={$model->gakubu_school_kind}";
            $param .= "&HUKUSIKI_RADIO={$model->hukusiki_radio}";
            $param .= "&HR_CLASS={$model->hr_class}";
            $param .= "&HR_CLASS2={$model->hr_class2}";
            $arg["jscript"] = "window.open('knjd410mindex.php?cmd=edit{$param}','right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd410mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
