<?php

require_once('for_php7.php');

class knjz412aForm2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;

        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz412aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->school_cd) && !isset($model->warning)) {
            $query = knjz412aQuery::getCollegeDepartmentMst($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /********************/
        /* テキストボックス */
        /********************/
        //学科コード
        $extra = "";
        $arg["data"]["DEPARTMENTCD"] = knjCreateTextBox($objForm, $Row["DEPARTMENTCD"], "DEPARTMENTCD", 3, 3, $extra);
        //学科名
        $extra = "";
        $arg["data"]["DEPARTMENTNAME"] = knjCreateTextBox($objForm, $Row["DEPARTMENTNAME"], "DEPARTMENTNAME", 40, 30, $extra);
        //学科略称1
        $extra = "";
        $arg["data"]["DEPARTMENTNAME_SHOW1"] = knjCreateTextBox($objForm, $Row["DEPARTMENTNAME_SHOW1"], "DEPARTMENTNAME_SHOW1", 40, 30, $extra);
        //学科略称2
        $extra = "";
        $arg["data"]["DEPARTMENTNAME_SHOW2"] = knjCreateTextBox($objForm, $Row["DEPARTMENTNAME_SHOW2"], "DEPARTMENTNAME_SHOW2", 40, 30, $extra);
        //校内推薦用学科コード
        if ($model->Properties["Internal_Recommendation"] == "1") {
            $extra = " onblur=\"this.value=toInteger(this.value);\" ";
            $arg["data"]["CAMPUS_DEPARTMENTCD"] = knjCreateTextBox($objForm, $Row["CAMPUS_DEPARTMENTCD"], "CAMPUS_DEPARTMENTCD", 2, 2, $extra);
        }

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
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
        knjCreateHidden($objForm, "tmp_SCHOOL_CD", $model->school_cd);
        knjCreateHidden($objForm, "tmp_FACULTYCD", $model->facultycd);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz412aindex.php?cmd=list';";
        }

        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz412aForm2.html", $arg);
    }
}
