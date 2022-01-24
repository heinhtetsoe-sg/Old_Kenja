<?php

require_once('for_php7.php');

class knjd136Form2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd136index.php", "", "edit");

        $db        = Query::dbCheckOut();

        //初期値取得（学期、学年）
        $firstS = $db->getOne(knjd136Query::getSemester("shokichi"));
        $model->semester = (!$model->semester) ? $firstS : $model->semester;
        $firstG = $db->getOne(knjd136Query::getGrade($model, "shokichi", $firstS));
        $model->grade = (!$model->grade) ? $firstG : $model->grade;
        if (!$model->grade2 || $model->grade != $model->grade2) {
            $model->grade2 = $firstG;
        }

        //校種を取得
        $query = knjd136Query::getSchoolKind($model->grade2);
        $school_kind = $db->getOne($query);
        if (!$model->school_kind || $model->school_kind != $school_kind) {
            $model->school_kind = $school_kind;
        }
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;

        if ($model->Properties["RECORD_DOCUMENT_KIND_DAT_FOOTNOTE_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $model->Properties["RECORD_DOCUMENT_KIND_DAT_FOOTNOTE_SIZE_".$school_kind]);
            $model->getPro["FOOTNOTE"]["moji"] = (int)trim($moji);
            $model->getPro["FOOTNOTE"]["gyou"] = (int)trim($gyou);
        } else {
            $model->getPro["FOOTNOTE"]["moji"] = 50;
            $model->getPro["FOOTNOTE"]["gyou"] = 3;
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = $db->getRow(knjd136Query::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row = $model->field;
        }

        //全体評
        $extra = "";
        $arg["data"]["FOOTNOTE"] = knjCreateTextArea($objForm, "FOOTNOTE", $model->getPro["FOOTNOTE"]["gyou"], $model->getPro["FOOTNOTE"]["moji"] * 2 + 1, "soft", $extra, $Row["FOOTNOTE"]);

        //コメント
        $arg["COMMENT"] = "※全角".$model->getPro["FOOTNOTE"]["moji"]."文字X".$model->getPro["FOOTNOTE"]["gyou"]."行まで";

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRADE",
                            "value"     => $model->grade
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTCD",
                            "value"     => $model->testcd
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => $model->semester
                            ) );

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjd136index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd136Form2.html", $arg);
    }
}
?>
