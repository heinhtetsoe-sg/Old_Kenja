<?php

require_once('for_php7.php');

class knjd126iForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("form1", "POST", "knjd126iindex.php", "", "form1");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種一覧取得
            $schoolkind = $db->getCol(knjd126iQuery::getSchoolKindList($model, "cnt"));
            if (get_count($schoolkind) > 1) {
                //校種コンボ
                $query = knjd126iQuery::getSchoolKindList($model, "list");
                $extra = "onChange=\"btn_submit('form1')\";";
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

        //学期コンボ(観点データ用)
        $model->field["SEMESTER"] = (!$model->field["SEMESTER"]) ? CTRL_SEMESTER : $model->field["SEMESTER"];//初期値
        $opt_semes = array();
        $result = $db->query(knjd126iQuery::selectNamemstQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_semes[] = array("label" => $row["NAME1"],"value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEMESTER",
                            "size"        => "1",
                            "value"       => $model->field["SEMESTER"],
                            "options"     => $opt_semes,
                            "extrahtml"   => "onChange=\"btn_submit('form1')\";"));
        $arg["SEMESTER"] = $objForm->ge("SEMESTER");
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];//初期値

        //教科コンボ
        $opt_sbuclass = $opt_electdiv = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd126iQuery::selectSubclassQuery($model));
        //教育課程対応
        $classCdExists = false;
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]." ".$row["CLASSNAME"],"value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
                if ($model->field["CLASSCD"] == $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]) {
                    $classCdExists = true;
                }
                //選択教科の保管
                $opt_electdiv[$row["CLASSCD"].'-'.$row["SCHOOL_KIND"]] = $row["ELECTDIV"];
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"]." ".$row["CLASSNAME"],"value" => $row["CLASSCD"]);
                if ($model->field["CLASSCD"] == $row["CLASSCD"]) {
                    $classCdExists = true;
                }
                //選択教科の保管
                $opt_electdiv[$row["CLASSCD"]] = $row["ELECTDIV"];
            }
        }
        $result->free();
        $electdiv = ($model->field["CLASSCD"]) ? $opt_electdiv[$model->field["CLASSCD"]] : "0";
        $model->field["CLASSCD"] = $classCdExists ? $model->field["CLASSCD"] : "";
        $objForm->ae( array("type"        => "select",
                            "name"        => "CLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["CLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('form1')\";"));
        $arg["CLASSCD"] = $objForm->ge("CLASSCD");
        //hidden
        knjCreateHidden($objForm, "H_CLASSCD");

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd126iQuery::selectChairQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"].':'.$row["SUBCLASS_VALUE"]);
        }
        $result->free();
        $model->field["CHAIRCD_SUBCLASS"] = $model->field["CLASSCD"] ? $model->field["CHAIRCD_SUBCLASS"] : "";
        $objForm->ae( array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD_SUBCLASS"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('form1')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //選択教科と同様に英字で入力する科目コード取得
        if ($db->getOne(knjd126iQuery::getNameMstD065($model)) > 0) {
            $electdiv = 1;
        }

        //入力選択ラジオボタン 1:マウス入力 2:手入力
        $opt_select = array(1, 2);
        $model->select = ($model->select == "") ? "1" : $model->select;
        $extra = array("id=\"SELECT1\" onclick =\" return btn_submit('form1');\"", "id=\"SELECT2\" onclick =\" return btn_submit('form2');\"");
        $radioArray = knjCreateRadio($objForm, "SELECT", $model->select, $extra, $opt_select, get_count($opt_select));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //入力選択ラジオボタン 1:値選択 2:データクリア
        $opt_nyuryoku = array(1, 2);
        $model->nyuryoku = ($model->nyuryoku == "") ? "1" : $model->nyuryoku;
        $extra = array("id=\"NYURYOKU1\" onClick=\"myHidden()\"", "id=\"NYURYOKU2\" onClick=\"myHidden()\"");
        $radioArray = knjCreateRadio($objForm, "NYURYOKU", $model->nyuryoku, $extra, $opt_nyuryoku, get_count($opt_nyuryoku));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //入力値選択ラジオボタン
        $query = knjd126iQuery::getKantenHyouka($model);
        $result = $db->query($query);
        $kantenArray = array();
        $opt_data = array();
        $kantenCnt = 1;
        $extra = array();
        $komoji = 0;
        $oomoji = 0;
        $model->nonVisualViewCd = "";
        while ($kanten = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //プロパティにセットされているコードは表示しない
            if (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] == $kanten["NAMECD2"]) ||
                ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] == $kanten["NAMECD2"])) {
                $model->nonVisualViewCd = $kanten["ABBV1"];
            } else {
                $kantenArray[$kantenCnt]["VAL"] = $kanten["ABBV1"];
                if (preg_match("/^[a-z]+$/", $kanten["ABBV1"])) {
                    $komoji++;
                } else {
                    $oomoji++;
                }
                $kantenArray[$kantenCnt]["SHOW"] = $kanten["NAME1"];
                $opt_data[] = $kantenCnt;
                $extra[] = "id=\"TYPE_DIV{$kantenCnt}\"";
                $arg["TYPE_SHOW{$kantenCnt}"] = $kanten["NAME1"];
                $kantenCnt++;
            }
        }
        $result->free();
        if (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"]) ||
            ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"])) {
            $oomoji++;
            $kantenArray[$kantenCnt]["VAL"] = "F";
            $kantenArray[$kantenCnt]["SHOW"] = "F";
            $opt_data[] = $kantenCnt;
            $extra[] = "id=\"TYPE_DIV{$kantenCnt}\"";
            $arg["TYPE_SHOW{$kantenCnt}"] = "F";
        }
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        if (get_count($kantenArray) == 0) {
            if ($model->field["SEMESTER"] === '9') {
                $arg["close_win"] = "close_window1();";
            } else {
                $arg["close_win"] = "close_window2();";
            }
        }

        //管理者コントロール
        $admin_key = array();
        $result = $db->query(knjd126iQuery::selectContolCodeQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
        }
        $result->free();

        //生徒を抽出する日付
        $sdate = str_replace("/","-",$model->control["学期開始日付"][$model->field["SEMESTER"]]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][$model->field["SEMESTER"]]);
        $execute_date = ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) ? CTRL_DATE : $edate;//初期値

        //観点コード(MAX5または6)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        if ($model->Properties["kantenHyouji"] !== '6') {
            $arg["kantenHyouji_5"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
            $result = $db->query(knjd126iQuery::selectViewcdQuery($model, $execute_date));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 5) break;//MAX5
                $view_key[$view_cnt] = $row["VIEWCD"];//1,2,3,4,5
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                $objForm->ae( array("type"      => "hidden",
                                    "name"      => "VIEWCD".$view_cnt,
                                    "value"     => $row["VIEWNAME"] )  );
            }
            $result->free();
            for ($i=0; $i<(5-get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        } else {
            $arg["kantenHyouji_6"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤","6" => "⑥");
            $result = $db->query(knjd126iQuery::selectViewcdQuery($model, $execute_date));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 6) break;//MAX6
                $view_key[$view_cnt] = $row["VIEWCD"];//1,2,3,4,5,6
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                $objForm->ae( array("type"      => "hidden",
                                    "name"      => "VIEWCD".$view_cnt,
                                    "value"     => $row["VIEWNAME"] )  );
            }
            $result->free();
            for ($i=0; $i<(6-get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        }
        $arg["view_html"] = $view_html;
        //評定用観点コード
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($view_cnt > 0) $view_key[9] = substr($model->field["CLASSCD"], 0, 2)."99";
        } else {
            if ($view_cnt > 0) $view_key[9] = $model->field["CLASSCD"]."99";//9
        }

        //データ取得(JVIEWSTAT_RECORD_DAT)
        $arrJviewSchNo = array();
        $result = $db->query(knjd126iQuery::getJviewstatRecordDat($model, $view_key));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrJviewSchNo[] = $row["SCHREGNO"];
        }
        $result->free();

        //各項目の合算を作成
        if ($model->field["CHAIRCD_SUBCLASS"]) {
            $model->getTotalData($arg, $db, $kantenArray, $execute_date, $view_key, $model);
        }

        //初期化
        $model->data=array();
        $counter=0;
        $disable = "disabled";

        //一覧表示
        $result = $db->query(knjd126iQuery::selectQuery($model, $execute_date, $view_key));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //クラス-出席番(表示)
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"];

            //名前
            $row["NAME_SHOW"]   = $row["SCHREGNO"]." ".$row["NAME_SHOW"];

            //異動情報
            $query = knjd126iQuery::getTransfer($model, $row["SCHREGNO"]);
            $transCnt = $db->getOne($query);
            $setColor = "#ffffff";
            if ($transCnt > 0) {
                $setColor = "#ffff00";
            }

            //各項目を作成
            foreach ($view_key as $code => $col) {
                if ($code != "9") {
                    if (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"]) || 
                        ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"])) {
                        if (in_array($row["SCHREGNO"], $arrJviewSchNo)) {
                            //JVIEWSTAT_RECORD_DATにデータがある人のみブランクを"F"に切り替え
                            $row["STATUS".$code] = ($row["STATUS".$code] == "") ? "F": $row["STATUS".$code];
                        }
                        //プロパティにセットされているコードは表示しない
                        $row["STATUS".$code] = ($row["STATUS".$code] == $model->nonVisualViewCd) ? "": $row["STATUS".$code];
                    }
                }

                //選択教科の評定は、A,B,Cに変換
                if ($code == "9" && $electdiv != "0") {
                    $status = $row["STATUS".$code];
                    if ($status == "11") $status = "A";
                    if ($status == "22") $status = "B";
                    if ($status == "33") $status = "C";
                    $row["STATUS".$code] = $status;
                }

                //管理者コントロール
                if(in_array($model->field["SEMESTER"], $admin_key) && DEF_UPDATE_RESTRICT <= AUTHORITY) {

                    //各観点コードを取得
                    $model->data["STATUS"][$code] = $col;

                    if($code == "9") {
                        //評定はプロパティが1以外のときは評定は入力可
                        if ($model->Properties["displayHyoutei"] != "1") {
                            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this, '".$electdiv."');\"";
                            $row["STATUS".$code] = knjCreateTextBox($objForm, $row["STATUS".$code], "STATUS".$code."-".$counter, 3, 1, $extra);
                        } else {
                            $row["STATUS".$code] = $row["STATUS".$code];
                            //hidden
                            knjCreateHidden($objForm, "STATUS".$code."-".$counter, $row["STATUS".$code]);
                        }
                    } else {
                        $extra = "STYLE=\"text-align: center\" readonly=\"readonly\" onClick=\"kirikae(this, 'STATUS".$code."-".$counter."')\" oncontextmenu=\"kirikae2(this, 'STATUS".$code."-".$counter."')\"; ";
                        $row["STATUS".$code] = knjCreateTextBox($objForm, $row["STATUS".$code], "STATUS".$code."-".$counter, 3, 1, $extra);
                    }

                    //更新ボタンのＯＮ／ＯＦＦ
                    $disable = "";

                //ラベルのみ
                } else {

                    $row["STATUS".$code] = "<font color=\"#000000\">".$row["STATUS".$code]."</font>";

                }
            }

            $row["COLOR"] = $setColor;

            $counter++;
            $arg["data"][] = $row;
        }
        $result->free();

        $dataArray = array();
        foreach ($kantenArray as $key => $val) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$key."')\"",
                                 "NAME" => $val["VAL"]);
        }

        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"] = $val["VAL"];
            $arg["menu"][] = $setData;
        }

        Query::dbCheckIn($db);

        //ボタン
        $extra = "onclick=\"return btn_submit('update', '".$electdiv."');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('updateRecordSeme', '".$electdiv."');\"".$disable;
        $arg["btn_sansyutsu"] = knjCreateBtn($objForm, "btn_sansyutsu", "算 出", $extra);

        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJD126I");
        knjCreateHidden($objForm, "CTRL_Y", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER2", $model->field["SEMESTER2"]);
        knjCreateHidden($objForm, "CTRL_D", $execute_date);
        knjCreateHidden($objForm, "ELECTDIV", $electdiv);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);

        $hiddenSetVal = "";
        $hiddenSetShow = "";
        $hiddenSetCheck = "";
        $sep = "";
        foreach ($kantenArray as $key => $val) {
            $hiddenSetVal .= $sep.$key;
            $hiddenSetShow .= $sep.$val["VAL"];
            $sep = ",";
        }
        knjCreateHidden($objForm, "SETVAL", $hiddenSetVal);
        knjCreateHidden($objForm, "SETSHOW", $hiddenSetShow);
        if ($komoji > 0 && $oomoji > 0) {
            $setHenkan = "3";
        } else if ($komoji > 0) {
            $setHenkan = "2";
        } else {
            $setHenkan = "1";
        }
        knjCreateHidden($objForm, "HENKAN_TYPE", $setHenkan);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //観点数設定
        knjCreateHidden($objForm, "kantenHyouji", $model->Properties["kantenHyouji"]);
        knjCreateHidden($objForm, "kantenHyouji_5", $arg["kantenHyouji_5"]);
        knjCreateHidden($objForm, "kantenHyouji_6", $arg["kantenHyouji_6"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        $setNameCd = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."09";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        knjCreateHidden($objForm, "Z009", $setNameCd);

        if ($model->field["SEMESTER"] == "9") {
            knjCreateHidden($objForm, "useJviewStatus_NotHyoji", $model->Properties["useJviewStatus_NotHyoji_D028"]);
        } else {
            knjCreateHidden($objForm, "useJviewStatus_NotHyoji", $model->Properties["useJviewStatus_NotHyoji_D029"]);
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd126iForm1.html", $arg);
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
    if($name == "SEMESTER"){
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
