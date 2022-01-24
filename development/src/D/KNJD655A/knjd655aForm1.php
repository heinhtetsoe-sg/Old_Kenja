<?php

require_once('for_php7.php');

class knjd655aForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjd655aindex.php", "", "edit");
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //プログラムＩＤ
        $arg["PROGRAMID"] = PROGRAMID;

        //学期コンボボックスを作成する
        $query = knjd655aQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, "onchange=\"return btn_submit('coursename');\"", 1);

        //学年コンボボックスを作成する
        $query = knjd655aQuery::getGradeHrclass($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->grade_hr_class, "onchange=\"return btn_submit('coursename');\"", 1);
        $model->grade    = substr($model->grade_hr_class, 0, 2);
        $model->hr_class = substr($model->grade_hr_class, 2);

        //学校種別取得（教育課程）
        $query = knjd655aQuery::getSchoolKind($model->grade);
        $model->school_kind = $db->getOne($query);
        $model->classcd = "00";
        $model->curriculum_cd = "0";

        //コース一覧取得
        $result = $db->query(knjd655aQuery::getList($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["FOOTNOTE"] = str_replace("\r\n", "<BR>", $row["FOOTNOTE"]);
            $row["FOOTNOTE"] = str_replace("\r", "<BR>", $row["FOOTNOTE"]);
            $row["FOOTNOTE"] = str_replace("\n", "<BR>", $row["FOOTNOTE"]);
            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $arg["reload"]  = "parent.right_frame.location.href='knjd655aindex.php?cmd=edit';";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd655aForm1.html", $arg);
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

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
