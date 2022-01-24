<?php

require_once('for_php7.php');

class knjd132jForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //IBコースコンボ
        $query = knjd132jQuery::getIBPrgCourse($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "IBPRG_COURSE", $model->field["IBPRG_COURSE"], $extra, 1);

        //学期コンボ
        $query = knjd132jQuery::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knjd132jQuery::getIBGrade($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //年組コンボ
        $query = knjd132jQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //D045情報取得
        $model->nameCd2 = "1";//"1"固定
        $model->infoD045 = array();
        $model->infoD045 = $db->getRow(knjd132jQuery::getD045($model->nameCd2), DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "FROM_VAL", $model->infoD045["FROM_VAL"]);
        knjCreateHidden($objForm, "TO_VAL", $model->infoD045["TO_VAL"]);

        //一覧表示
        $model->arrSchregNo = array();
        $result = $db->query(knjd132jQuery::getStudent($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //更新用
            $model->arrSchregNo[] = $row["SCHREGNO"];

            //警告メッセージがある時と、更新の際はモデルの値を参照する
            $row["SCORE"] = (isset($model->warning)) ? $model->field["SCORE_".$row["SCHREGNO"]]: $row["SCORE"];

            //textbox
            $extra = "style=\"text-align:right\" onblur=\"calcCheck(this);\" onkeydown=\"keyChangeEntToTab(this)\"";
            $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], "SCORE_".$row["SCHREGNO"], 2, 1, $extra);

            $arg["data"][] = $row;
        }

        //ボタン作成
        //更新ボタン
        $disabled = ($model->arrSchregNo[0] == "") ? " disabled": "";
        $extra  = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update')\"" : "disabled";
        $extra .= $disabled;
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd132jForm1.html", $arg);
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
