<?php

require_once('for_php7.php');

class knjd219tForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjd219tindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        //処理年度
        $arg["YEAR"] = CTRL_YEAR;
        
        //更新後に各フィールドにセット
        if ($model->cmd == "up_list") {
            $model->field["SEMESTER"] = $model->semester;
            $model->field["TESTKIND_ITEMCD"] = $model->testkind_itemcd;
            $model->field["RUISEKI_DIV"] = $model->ruiseki_div;
            $model->field["GRADE"]  = $model->grade;
            if ($model->schoolkind === 'H') {
                $model->field["GROUP_CD"] = $model->groupcd;
            } else {
                $model->field["COURSE_MAJOR_CODE"] = $model->coursecd.'-'.$model->groupcd.'-'.$model->coursecode;
            }
        }

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knjd219t');\"";
        $query = knjd219tQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //累積種別(1, 3のみ使用する)
        $opt = array(1, 2, 3);
        $model->field["RUISEKI_DIV"] = ($model->field["RUISEKI_DIV"] == "") ? "3" : $model->field["RUISEKI_DIV"];
        $extra = array("id=\"RUISEKI_DIV1\" onClick=\"return btn_submit('knjd219t');\"", "id=\"RUISEKI_DIV2\" onClick=\"return btn_submit('knjd219t');\"", "id=\"RUISEKI_DIV3\" onClick=\"return btn_submit('knjd219t');\"");
        $radioArray = knjCreateRadio($objForm, "RUISEKI_DIV", $model->field["RUISEKI_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
//        if ($model->field["SEMESTER"] == "9") {
//            $arg["useRuiseki3"] = "1";
//        } else {
//            $arg["useRuiseki2"] = "1";
//        }
        $arg["useRuiseki3"] = "1";
        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjd219t');\"";
        $query = knjd219tQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "blank");

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd219tQuery::getSchoolKind($model));

        //コース、コースグループ区分 (実際に値はDIV='3'または'5'をセット)
        $opt = array(1);
        $model->field["SET_DIV"] = ($model->field["SET_DIV"] == "") ? "1" : $model->field["SET_DIV"];
        $extra = array("id=\"DIV1\"");
        $radioArray = knjCreateRadio($objForm, "SET_DIV", $model->field["SET_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        if ($model->schoolkind === 'H') {
            //コースグループコンボ
            $arg["CourseGroup"] = "1";
            $arg["SET_DIV_NAME"] = 'コースグループ';
            $extra = "onChange=\"return btn_submit('knjd219t');\"";
            $query = knjd219tQuery::getCourseGroup($model);
            makeCmb($objForm, $arg, $db, $query, "GROUP_CD", $model->field["GROUP_CD"], $extra, 1, "blank");
        } else if ($model->schoolkind === 'J') {
            //コースコンボ
            $arg["CourseMajor"] = "1";
            $arg["SET_DIV_NAME"] = 'コース';
            $extra = "onChange=\"return btn_submit('knjd219t');\"";
            $query = knjd219tQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR_CODE", $model->field["COURSE_MAJOR_CODE"], $extra, 1, "blank");
        }
        
        //テスト種別コンボ
        $extra = "onChange=\"return btn_submit('knjd219t');\"";
        $query = knjd219tQuery::getTestitem($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD", $model->field["TESTKIND_ITEMCD"], $extra, 1, "blank");

        //評定項目の表示
        $koumokuArray = array();
        $count = $model->setAssesslevelCount;//評定値の段階
        while ($count > 0) {
            $koumokuArray["ASSESSLEVEL_NAME"] = '評定'.$count;
            $arg["KOUMOKU"][] = $koumokuArray;
            $count--;
        }

        //データ取得
        //初期化
        $model->data = array();
        $result = $db->query(knjd219tQuery::selectQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
                if ($row["ASSESSLOW_".$i] != "" && $row["ASSESSHIGH_".$i] != "") {
                    $row["SET_ASSESSLEVEL_".$i] = $row["ASSESSLOW_".$i].' ～ '.$row["ASSESSHIGH_".$i];
                }
            }
            $arg["data"][] = $row;
        }
        $result->free();

        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        //右画面に渡す
        if ($model->cmd === 'knjd219t') {
            $model->semester        = $model->field["SEMESTER"];
            $model->testkind_itemcd = $model->field["TESTKIND_ITEMCD"];
            $model->ruiseki_div     = $model->field["RUISEKI_DIV"];;
            if ($model->schoolkind === 'H') {
                $model->div             = '5';//コースグループ用の値
                $model->coursecd        = '0';
                $model->groupcd         = $model->field["GROUP_CD"];
                $model->coursecode      = '0000';
            } else {
                $model->div             = '3';//コース用の値
                list($coursecd, $majorcd, $coursecode) = explode("-", $model->field["COURSE_MAJOR_CODE"]);
                $model->coursecd        = $coursecd;
                $model->groupcd         = $majorcd;
                $model->coursecode      = $coursecode;
            }
            $model->hr_class        = '000';
            $model->grade           = $model->field["GRADE"];
        }

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "add") {
            $arg["reload"] = "window.open('knjd219tindex.php?cmd=edit','right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd219tForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
