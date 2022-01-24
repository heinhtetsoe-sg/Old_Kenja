<?php

require_once('for_php7.php');

class knjd410Form1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjd410index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            //年度
            $query = knjd410Query::getYear();
            $extra = "onchange=\"return btn_submit('combo');\"";
            makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1, $model);
        } else {
            $arg["YEAR"] = CTRL_YEAR;
            knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
            $model->year = CTRL_YEAR;
        }

        //前年度・学期からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $title = ($model->Properties["useGradeKindCompGroupSemester"] == "1") ?  "前年度・学期からコピー" : "前年度からコピー"; 
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", $title, $extra);

        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            //学期コンボ作成
            $query = knjd410Query::getSemester($model);
            $extra = "onchange=\"return btn_submit('combo');\"";
            makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, $model);
        }

        //学部コンボ作成
        $query = knjd410Query::getSchoolKind();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind, $extra, 1, $model);

        if ($model->Properties["useSpecial_Support_School"] == '1') {
            //年組コンボ切替ラジオボタン 1:法定クラス 2:実クラス
            $opt = array(1, 2);
            $model->hukusiki_radio = ($model->hukusiki_radio == "") ? "1" : $model->hukusiki_radio;
            $extra = array("id=\"HUKUSIKI_RADIO1\" onclick=\"return btn_submit('combo');\"", "id=\"HUKUSIKI_RADIO2\" onclick=\"return btn_submit('combo');\"");
            $radioArray = knjCreateRadio($objForm, "HUKUSIKI_RADIO", $model->hukusiki_radio, $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg[$key] = $val;
            $arg["useSpecial_Support_School"] = 1;
        } else {
            knjCreateHidden($objForm, "HUKUSIKI_RADIO", "1");
        }

        //年組コンボ
        $query = knjd410Query::getHrClass($model);
        $extra = "onchange=\"btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->hr_class, $extra, 1, $model);

        //年組コンボ（特別クラス選択時）
        if ($model->hukusiki_radio == "2") {
            $query = knjd410Query::getHrClass2($model);
            $extra = "onchange=\"btn_submit('combo');\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS2", $model->hr_class2, $extra, 1, $model);
            $arg["ghr"] = 1;
        } else {
            $model->hr_class2 = "00-000";
        }

        //一覧表示
        $key = "";
        $query = knjd410Query::getList($model, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($key !== $row["GAKUBU_SCHOOL_KIND"].'-'.$row["CONDITION"]) {
                $cnt = $db->getOne(knjd410Query::getList($model, $row["CONDITION"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }

            $key = $row["GAKUBU_SCHOOL_KIND"].'-'.$row["CONDITION"];
            
            //状態区分
            $row["CONDITION_NAME"] = $db->getOne(knjd410Query::getCondition($row["CONDITION"]));
            //グループ名
            $row["GROUP_NAME"] = $db->getOne(knjd410Query::getGroupName($model, $row["CONDITION"], $row["GROUPCD"]));
            //件数
            $row["SCHREG_COUNT"] = $db->getOne(knjd410Query::getSchregCnt($model, $row["CONDITION"], $row["GROUPCD"]));
            
            $arg["data"][] = $row;
        }

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            $arg["jscript"] = "window.open('knjd410index.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd410Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, &$model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name === 'YEAR') {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else if ($name === 'SEMESTER') {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
