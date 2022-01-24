<?php

require_once('for_php7.php');

class knjd419Form1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjd419index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $query = knjd419Query::getYear();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //前年度・学期からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度・学期からコピー", $extra);

        //学期コンボ
        $query = knjd419Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);

        //学部コンボ
        $query = knjd419Query::getSchoolKind();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind, $extra, 1);

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
        
        //年組コンボ作成
        $query = knjd419Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->grade_hr_class, $extra, 1, $model);

        //年組コンボ（特別クラス選択時）
        if ($model->hukusiki_radio == "2") {
            $query = knjd419Query::getHrClass2($model);
            $extra = "onchange=\"return btn_submit('combo');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS2", $model->grade_hr_class2, $extra, 1, $model);
            $arg["ghr"] = 1;
        } else {
            $model->grade_hr_class2 = "00-000";
        }

        //状態区分コンボ
        $query = knjd419Query::getCondition($model);
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "CONDITION", $model->condition, $extra, 1);

        //グループコードコンボ
        $query = knjd419Query::getGroupcd($model);
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "GROUPCD", $model->groupcd, $extra, 1);

        //指導計画帳票パターン
        $gp = $db->getRow(knjd419Query::getGuidancePattern($model), DB_FETCHMODE_ASSOC);
        $arg["GUIDANCE_PATTERN"] = $gp["LABEL"];
        $model->guidance_pattern = $gp["VALUE"];

        if ($model->cmd == "combo") {
            unset($model->classcd);
            unset($model->school_kind);
            unset($model->curriculum_cd);
            unset($model->subclasscd);
            unset($model->unit_aim_div);
        }

        //単元項目名
        $item = $db->getRow(knjd419Query::getGuidanceItemName($model), DB_FETCHMODE_ASSOC);
        $arg["ITEM_NAME"] = ($item["ITEM_REMARK1"]) ? $item["ITEM_REMARK1"] : '　　';

        //一覧表示
        $key = "";
        $query = knjd419Query::getList($model, "", "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //列結合
            if ($key != $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]) {
                $cnt = $db->getOne(knjd419Query::getList($model, $row, "cnt"));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            //列結合（所見登録）
            if ($row["UNIT_AIM_DIV"] == "1") {
                $row["ROWSPAN2"] = 1;
            } else if ($key != $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]) {
                $row["ROWSPAN2"] = ($cnt > 0) ? $cnt : 1;

            }

            $row["SUBCLASS"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"].' '.$row["SUBCLASSNAME"];

            $sep = ($row["UNITCD"] && $row["UNITNAME"]) ? ':' : '';
            $row["UNIT_SHOW"] = $row["UNITCD"].$sep.$row["UNITNAME"];

            $row["SEND_UNIT_AIM_DIV"] = ($row["UNIT_AIM_DIV"] == "1") ? "1" : "0";

            $arg["data"][] = $row;

            $key = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
        }

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            $arg["jscript"] = "window.open('knjd419index.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd419Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == 'YEAR') {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else if ($name == 'SEMESTER') {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
