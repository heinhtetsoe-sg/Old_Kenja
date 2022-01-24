<?php

require_once('for_php7.php');

class knjd297Form1
{
    function main(&$model)
    {
        $flg = "";
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd297Form1", "POST", "knjd297index.php", "", "knjd297Form1");
        $db             = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $opt = array();
        $result = $db->query(knjd297Query::getSchoolKind($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]    = array("label" => $row["LABEL"],
                                   "value" => $row["VALUE"]);
        }
        $model->field["SCHOOL_KIND"] = ($model->field["SCHOOL_KIND"]) ? $model->field["SCHOOL_KIND"] : $opt[0]["value"];
        $extra = " onchange=\"btn_submit('changeKind');\" ";
        $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $opt, $extra, 1);

        //クラスコンボ
        $opt = array();
        $result = $db->query(knjd297Query::getHrClass($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[]    = array("label" => $row["LABEL"],
                                   "value" => $row["VALUE"]);
        }
        $model->field["GRADE_HR_CLASS"] = ($model->cmd != "changeKind" && $model->field["GRADE_HR_CLASS"]) ? $model->field["GRADE_HR_CLASS"] : $opt[0]["value"];

        $extra = " onchange=\"btn_submit('main');\" ";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $opt, $extra, 1);


        //年度学科一覧取得
        $opt_left = array();
        $opt_right = array();

        //学科一覧取得
        $result = $db->query(knjd297Query::selectSchQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_right[]    = array("label" => $row["LABEL"],
                                       "value" => $row["VALUE"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt ));

        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => "" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));

        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

        //出力対象者
        $objForm->ae( array("type"        => "select",
                            "name"        => "category_selected",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','category_selected','category_name',1)\"",
                            "options"     => $opt_left));

        //生徒一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "category_name",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','category_selected','category_name',1)\"",
                            "options"     => $opt_right));

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','category_selected','category_name',1);\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','category_selected','category_name',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','category_selected','category_name',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','category_selected','category_name',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("category_selected"),
                                   "RIGHT_PART"  => $objForm->ge("category_name"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //ボタン作成
        $extra = " onClick=\"btn_submit('csv');\" ";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
        $extra = " onClick=\"closeWin();\" ";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["TITLE"]   = "マスタメンテナンス - 学科マスタ";
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd297Form1.html", $arg);
    }
}
?>
