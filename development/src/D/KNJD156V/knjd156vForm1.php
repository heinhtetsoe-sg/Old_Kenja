<?php

require_once('for_php7.php');

class knjd156vForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjd156vindex.php", "", "edit");
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //プログラムＩＤ
        $arg["PROGRAMID"] = PROGRAMID;

        //学期コンボボックスを作成する
        $query = knjd156vQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, "onchange=\"return btn_submit('coursename');\"", 1);

        //学年コンボボックスを作成する
        $query = knjd156vQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, "onchange=\"return btn_submit('coursename');\"", 1);

        //学校種別取得（教育課程）
        $query = knjd156vQuery::getSchoolKind($model->grade);
        $model->school_kind = $db->getOne($query);
        $model->classcd = "00";
        $model->curriculum_cd = "00";

        //コース一覧取得
        $result = $db->query(knjd156vQuery::getList($model));
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

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $arg["reload"]  = "parent.right_frame.location.href='knjd156vindex.php?cmd=edit';";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd156vForm1.html", $arg);
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
