<?php

require_once('for_php7.php');


class knji100Form1
{
    function main(&$model)
    {
        $objForm      = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knji100index.php", "", "edit");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knji100Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //出力設定ラジオボタン作成
        $output = array(OUT_CODE_NAME, OUT_CODE_ONLY, OUT_NAME_ONLY);
        foreach($output as $key => $val){
            $name = "RADIO".($key+1);
            $objForm->ae( array("type"       => "radio",
                                "name"       => "OUTPUT",
                                "extrahtml"  => "id=".$name ) );

            $arg[$name] = $objForm->ge("OUTPUT", $val);
        }

        $opt_left = $opt_right = $item = array();
        if (isset($model->selectdata)){
            $item = explode(",", $model->selectdata);
        }
        foreach($model->item as $key => $val){
            if (in_array($key, $item)){
                $opt_left[] = array("label" => $val,"value" => $key);
            }else{
                $opt_right[] = array("label" => $val,"value" => $key);
            }
        }

        //書き出し項目一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:180\" WIDTH=\"180\" ondblclick=\"move('right','left_select','right_select')\" ",
                            "options"     => $opt_left));
        //項目一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:180\" WIDTH=\"180\" ondblclick=\"move('left','left_select','right_select')\" ",
                            "options"     => $opt_right));
        //全て追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select');\"" ) );
        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select');\"" ) );
        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select');\"" ) );
        //全て削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select');\"" ) ); 

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));
        //実行ボタン
        $btnName = "ＣＳＶ書出し";
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knji100Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $btnName = "エクセル書出し";
        } else {
            $extra = "onclick=\"return doSubmit();\"";
        }
        $arg["button"]["BTN_CSV"] = knjCreateBtn($objForm, "btn_csv", $btnName, $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJI100");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "mode");
        knjCreateHidden($objForm, "SCHREGNO");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji100Form1.html", $arg);
    }
}
?>