<?php

require_once('for_php7.php');

class knjz066form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz066index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度コピーボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('copy');\"" : "disabled";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //学部コンボ
        $query = knjz066Query::getSchoolKind();
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->school_kind, $extra, 1);

        //学年コンボ
        $opt = array();
        $value_flg = false;
        $query = knjz066Query::getSchoolKind($model->school_kind);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        for ($i = intval($row["GRADE_FROM"]); $i <= intval($row["GRADE_TO"]); $i++) {
        	$label = $db->getOne(knjz066Query::getGradeName1(sprintf("%02d", $i)));
        	if ($label) {
            	$opt[] = array('label' => $label, 'value' => sprintf("%02d", $i));
            	if ($model->grade == sprintf("%02d", $i)) $value_flg = true;
            }
        }
        $model->grade = ($model->grade && $value_flg) ? $model->grade : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('change');\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $opt, $extra, 1);

        //状態区分コンボ
        $query = knjz066Query::getCondition();
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "CONDITION", $model->condition, $extra, 1);

        //コンボを変更したら段階数をリセット
        if ($model->cmd == "change") unset($model->levelcnt);

        //データ件数
        $cnt = $db->getOne(knjz066Query::getGradeKindAssessDat($model, "cnt"));
        $cnt = ($cnt > 0) ? $cnt : "";

        //段階数テキスト
        $model->levelcnt = (($model->cmd != "level" && $model->cmd != "check") || $model->levelcnt == "") ? $cnt : $model->levelcnt;
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["LEVELCNT"] = knjCreateTextBox($objForm, $model->levelcnt, "LEVELCNT", 3, 3, $extra);

        //確定ボタン
        $extra = "onclick=\"return level(".$cnt.");\"";
        $arg["button"]["btn_level"] = knjCreateBtn($objForm, "btn_level", "確 定", $extra);

        //一覧表示
        $extra = "style=\"text-align: center\"";
        for ($i = 1; $i <= $model->levelcnt; $i++) {
            $query = knjz066Query::getGradeKindAssessDat($model, $i);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["ASSESSLEVEL"] = $i;
            if ($model->cmd == "check") $Row["ASSESSMARK"] = $model->field["ASSESSMARK_".$i];
            $Row["ASSESSMARK"] = knjCreateTextBox($objForm, $Row["ASSESSMARK"], "ASSESSMARK_".$i, 6, 6, $extra);
            $arg["data"][] = $Row;
        }

        //更新ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->levelcnt > 0) ? "onclick=\"return btn_submit('update');\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->levelcnt > 0 && $cnt > 0) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //Hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LEVEL", $model->levelcnt);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz066Form1.html", $arg);
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
