<?php

require_once('for_php7.php');

class knjd658aForm1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd658aindex.php", "", "edit");
        $db = Query::dbCheckOut();

        //テスト種別コンボボックスを作成する
        $query = knjd658aQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, "onchange=\"return btn_submit('edit');\"", 1);

        //学校種別取得（教育課程）
        $query = knjd658aQuery::getSchoolKind($model->grade);
        $model->school_kind = $db->getOne($query);
        $model->classcd = "00";
        $model->curriculum_cd = "00";

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $query = knjd658aQuery::getRecordDocumentKindDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /******************/
        /* テキストエリア */
        /******************/
        //注釈
        $extra = "style=\"height:55px;\"";
        $arg["data"]["FOOTNOTE"] = KnjCreateTextArea($objForm, "FOOTNOTE", $model->gyou, 101, "soft", $extra, $Row["FOOTNOTE"]);

        //コメント
        $arg["COMMENT"] = "※全角{$model->moji}文字×{$model->gyou}行まで";
        $arg["YEAR"] = CTRL_YEAR;

        /**********/
        /* ボタン */
        /**********/
        //コピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = KnjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEMESTER", $model->semester);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd658aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "GRADE"){
            $opt[] = array('label' => sprintf("%d",$row["LABEL"]).'学年',
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
