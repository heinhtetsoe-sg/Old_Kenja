<?php

require_once('for_php7.php');

class knjd136Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjd136index.php", "", "edit");
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //プログラムＩＤ
        $arg["PROGRAMID"] = PROGRAMID;

        //学期コンボボックスを作成する
        $query = knjd136Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, "onchange=\"return btn_submit('coursename');\"", 1);
//        //テスト種別コンボボックスを作成する
//        $query = knjd136Query::getTest($model);
//        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->testcd, "onchange=\"return btn_submit('coursename');\"", 1);

        //学年コンボボックスを作成する
        $query = knjd136Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, "onchange=\"return btn_submit('coursename');\"", 1);
        $model->grade2 = $model->grade;

        //学校種別取得
        $model->school_kind = $db->getOne(knjd136Query::getSchoolKind($model->grade));

        //コース一覧取得
        $result = $db->query(knjd136Query::getList($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["FOOTNOTE"] = str_replace("\r\n", "<BR>", $row["FOOTNOTE"]);
            $row["FOOTNOTE"] = str_replace("\r", "<BR>", $row["FOOTNOTE"]);
            $row["FOOTNOTE"] = str_replace("\n", "<BR>", $row["FOOTNOTE"]);
            $arg["data"][] = $row;
        }
        $result->free();

        //コピーボタンを作成
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy",
                            "value"     => "前年度からコピー",
                            "extrahtml" => " onclick=\"return btn_submit('copy');\""
                            ) );
        $arg["btn_copy"] = $objForm->ge("btn_copy");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "coursename"){
            $arg["reload"] = "window.open('knjd136index.php?cmd=edit','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd136Form1.html", $arg);
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size) {
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

?>
