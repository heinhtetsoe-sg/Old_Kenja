<?php

require_once('for_php7.php');

class knje014Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knje014index.php", "", "main");

        //設定チェック
        if ($model->Properties["useMaruA_avg"] > 0) {
            $arg["close_win"] = "closeWindow();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //課程学科コンボ
            $query = knje014Query::getCourseMajor($model);
            $extra = "onChange=\"btn_submit('change_course')\";";
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1);
        }

        //学級コンボ
        $query = knje014Query::getGradeHrClass($model);
        $extra = "onChange=\"btn_submit('change_hrclass')\";";
        makeCmb($objForm, $arg, $db, $query, "GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"], $extra, 1);

        //校種取得
        $schoolkind = $db->getOne(knje014Query::getSchoolKind($model));
        knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind);
        if (!$model->field["SCHOOL_KIND"]) {
            $model->field["SCHOOL_KIND"] = $schoolkind;
        }

        //学校区分取得
        $check_schKind = $db->getOne(knje014Query::checkSchoolMst());
        $schooldiv = $db->getOne(knje014Query::getSchoolDiv($model, $check_schKind, $schoolkind));
        knjCreateHidden($objForm, "SCHOOLDIV", $schooldiv);

        //ALLチェック
        $extra  = ($model->field["CHECKALL"] == "1") ? "checked" : "";
        $extra .= " onClick=\"return check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $query = knje014Query::selectQuery($model, $check_schKind);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //マルＡチェックボックス
            if (isset($model->warning)) {
                $extra = ($model->fields["COMMENTEX_A_CD"][$counter] == "1") ? "checked" : "";
            } else if ($row["COMMENTEX_A_CD"] == "1") {
                $extra = "checked";
            } else {
                $extra = "";
            }
            $row["COMMENTEX_A_CD"] = knjCreateCheckBox($objForm, "COMMENTEX_A_CD-".$counter, "1", $extra);

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "AUTH", AUTHORITY);
        knjCreateHidden($objForm, "PASS_AUTH", DEF_UPDATE_RESTRICT);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje014Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
