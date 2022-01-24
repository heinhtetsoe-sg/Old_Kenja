<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd124kForm1.php 66447 2019-03-22 04:33:54Z tawada $

class knjd124kForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd124kindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //学期コンボ(観点データ用)
        if (!$model->field["SEMESTER"]) $model->field["SEMESTER"] = (1 < CTRL_SEMESTER) ? 9 : 1;//初期値
        $opt_semes = array();
        //$opt_semes[] = array("label" => $model->control["学期名"][1],"value" => "1");
        //$opt_semes[] = array("label" => $model->control["学期名"][9],"value" => "9");
        $result = $db->query(knjd124kQuery::selectNamemstQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_semes[] = array("label" => $row["NAME1"],"value" => $row["NAMECD2"]);
        }
//echo get_count($opt_semes);
        $semesflg = false;
        if (get_count($opt_semes) == 0) {
            $opt_semes[] = array("label" => $model->control["学期名"][9],"value" => "9");
            $semesflg = true;
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEMESTER",
                            "size"        => "1",
                            "value"       => $model->field["SEMESTER"],
                            "options"     => $opt_semes,
                            "extrahtml"   => "onChange=\"btn_submit('semester')\";"));
        $arg["SEMESTER"] = $objForm->ge("SEMESTER");

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : 1;//初期値

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種一覧取得
            $schoolkind = $db->getCol(knjd124kQuery::getSchoolKindList($model, "cnt"));
            if (get_count($schoolkind) > 1) {
                //校種コンボ
                $query = knjd124kQuery::getSchoolKindList($model, "list");
                $extra = "onChange=\"btn_submit('main')\";";
                makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
                knjCreateHidden($objForm, "H_SCHOOL_KIND");
                $arg["useSchoolKindCmb"] = 1;
            } else {
                $model->field["SCHOOL_KIND"] = $schoolkind[0];
                knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind[0]);
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
            knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        } else {
            knjCreateHidden($objForm, "SCHOOL_KIND");
        }

        //教科コンボ
        $opt_sbuclass = $opt_electdiv = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd124kQuery::selectSubclassQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]." ".$row["CLASSNAME"],"value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
                //選択教科の保管
                $opt_electdiv[$row["CLASSCD"].'-'.$row["SCHOOL_KIND"]] = $row["ELECTDIV"];
            } else {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"]." ".$row["CLASSNAME"],"value" => $row["CLASSCD"]);
                //選択教科の保管
                $opt_electdiv[$row["CLASSCD"]] = $row["ELECTDIV"];
            }
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $electdiv = ($model->field["CLASSCD"]) ? $opt_electdiv[$class_array[0]] : "0";
        $objForm->ae( array("type"        => "select",
                            "name"        => "CLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["CLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('classcd')\";"));
        $arg["CLASSCD"] = $objForm->ge("CLASSCD");

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd124kQuery::selectChairQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('chaircd')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");

        //選択科目の判別
        //$electdiv = $db->getOne(knjd124kQuery::getElectdivQuery($model));

        //管理者コントロール
        $admin_key = array();
        $result = $db->query(knjd124kQuery::selectContolCodeQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $admin_key[] = $row["CONTROL_CODE"];
        }

        //観点コード(MAX5)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
        $result = $db->query(knjd124kQuery::selectViewcdQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $view_cnt++;
            if ($view_cnt > 5) break;//MAX5
            $view_key[$view_cnt] = $row["VIEWCD"];//1,2,3,4,5
            //チップヘルプ
            if ($view_cnt < 5) {
                $view_html .= "<th width=\"68\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
            } else {
                $view_html .= "<th width=\"*\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
            }
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "VIEWCD".$view_cnt,
                                "value"     => $row["VIEWNAME"] )  );
        }
        for ($i=1; $i<(5-get_count($view_key)); $i++) $view_html .= "<th width=\"68\">&nbsp;</th>";
        if (get_count($view_key) < 5) $view_html .= "<th width=\"*\">&nbsp;</th>";
        $arg["view_html"] = $view_html;
        //評定用観点コード
        if ($view_cnt > 0) $view_key[9] = $class_array[0]."99";//9

        //生徒を抽出する日付
        $edate = str_replace("/","-",$model->control["学期終了日付"][$model->field["SEMESTER"]]);
        $execute_date = ($edate < CTRL_DATE) ? $edate : CTRL_DATE;//初期値

        //初期化
        $model->data=array();
        $counter=0;
        $disable = "disabled";

        //一覧表示
        $result = $db->query(knjd124kQuery::selectQuery($model, $execute_date, $view_key));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //クラス-出席番(表示)
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"];

            //名前
            $row["NAME_SHOW"]   = $row["SCHREGNO"]." ".$row["NAME_SHOW"];

            //各項目を作成
            foreach ($view_key as $code => $col)
            {
                //管理者コントロール
//              if(in_array($code, $admin_key) && DEF_UPDATE_RESTRICT <= AUTHORITY) {
//              if(in_array($model->field["SEMESTER"], $admin_key) && DEF_UPDATE_RESTRICT <= AUTHORITY) {
                if((in_array($model->field["SEMESTER"], $admin_key) || $semesflg) && DEF_UPDATE_RESTRICT <= AUTHORITY && substr($col, 2) != "99") {//2006/03/24

                    //各観点コードを取得
                    $model->data["STATUS"][$code] = $col;

                    //テキストボックスを作成
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "STATUS".$code."-".$counter,
                                        "size"      => "3",
                                        "maxlength" => "1",
                                        "value"     => $row["STATUS".$code],
                                        "extrahtml" => "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this, '".$electdiv."');\" "));
                    $row["STATUS".$code] = $objForm->ge("STATUS".$code."-".$counter);

                    //更新ボタンのＯＮ／ＯＦＦ
                    $disable = "";

                //ラベルのみ
                } else {

                    $row["STATUS".$code] = "<font color=\"#000000\">".$row["STATUS".$code]."</font>";

                }
            }

            $row["COLOR"]="#ffffff";

            $counter++;
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update', '".$electdiv."');\"".$disable ) );
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "印 刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );
        $arg["btn_print"] = $objForm->ge("btn_print");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD124K" )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_Y",
                            "value"     => CTRL_YEAR )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_S",
                            "value"     => $model->field["SEMESTER2"] )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_D",
                            "value"     => $execute_date )  );
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");
        //hidden
        knjCreateHidden($objForm, "H_CLASSCD");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd124kForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if($name == "SCHOOL_KIND"){
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
