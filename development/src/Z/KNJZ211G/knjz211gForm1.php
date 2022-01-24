<?php

require_once('for_php7.php');

class knjz211gForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjz211gindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種一覧取得
            $schoolkind = $db->getCol(knjz211gQuery::getSchoolKindList($model, "cnt"));
            if (get_count($schoolkind) > 1) {
                //校種コンボ
                $query = knjz211gQuery::getSchoolKindList($model, "list");
                $extra = "onChange=\"btn_submit('form1')\";";
                $model->field["SCHOOL_KIND"] = !$model->field["SCHOOL_KIND"] ? "H" : $model->field["SCHOOL_KIND"];
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

        //区分
        $query = "";
        $extra = "onchange=\"return btn_submit('form1')\"";
        $opt=array(array("label"=>"2:学期末用評価", "value"=>"2"), array("label"=>"3:学年末用評価", "value"=>"3"));
        makeCmb($objForm, $arg, $db, $query, "DIV", $model->field["DIV"], $extra, 1, "", $opt);


        //学年コンボ作成
        $query = knjz211gQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('form1')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");
        //hidden
        knjCreateHidden($objForm, "H_GRADE");

        //科目コンボ作成
        $query = knjz211gQuery::getSubclassMst($model->field["GRADE"], $model);
        $extra = "onchange=\"return btn_submit('form1')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //入力値選択ラジオボタン
        $query = knjz211gQuery::getKantenHyouka($model);
        $result = $db->query($query);
        $kantenArray = array();
        $kantenCnt = 1;
        $komoji = 0;
        $oomoji = 0;
        $model->nonVisualViewCd = "";
        while ($kanten = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //プロパティにセットされているコードは表示しない
            if (($model->Properties["useJviewStatus_NotHyoji_D029"] == $kanten["NAMECD2"])) {
                $model->nonVisualViewCd = $kanten["ABBV1"];
            } else {
                $kantenArray[$kantenCnt]["VAL"] = $kanten["ABBV1"];
                if (preg_match("/^[a-z]+$/", $kanten["ABBV1"])) {
                    $komoji++;
                } else {
                    $oomoji++;
                }
                $kantenArray[$kantenCnt]["SHOW"] = $kanten["NAME1"];
                $kantenCnt++;
            }
        }
        $result->free();
        if ($model->Properties["useJviewStatus_NotHyoji_D029"]) {
            $oomoji++;
            $kantenArray[$kantenCnt]["VAL"] = "F";
            $kantenArray[$kantenCnt]["SHOW"] = "F";
        }

        if (get_count($kantenArray) == 0) {
            if ($model->field["DIV"] === '2') {
                $arg["close_win"] = "close_window1();";
            } else {
                $arg["close_win"] = "close_window2();";
            }
        }

        //管理者コントロール
        $admin_key = array();
        $result = $db->query(knjz211gQuery::getAdminContol($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
        }
        $result->free();

        //観点コード(MAX5または6)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        if ($model->Properties["kantenHyouji"] !== '6') {
            $arg["kantenHyouji_5"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
            $result = $db->query(knjz211gQuery::selectViewcdQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 5) break;   //MAX5
                $view_key[$view_cnt] = $row["VIEWCD"];  //1,2,3,4,5
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            }
            $result->free();

            for ($i=0; $i<(5-get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        } else {
            $arg["kantenHyouji_6"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤","6" => "⑥");
            $result = $db->query(knjz211gQuery::selectViewcdQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 6) break;   //MAX6
                $view_key[$view_cnt] = $row["VIEWCD"];  //1,2,3,4,5,6
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            }
            $result->free();

            for ($i=0; $i<(6-get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        }
        $arg["view_html"] = $view_html;
        knjCreateHidden($objForm, "VIEWCNT", $view_cnt);
        //評定用観点コード
        if ($view_cnt > 0) $view_key[9] = substr($model->field["SUBCLASSCD"], 0, 2)."99";

        //選択教科
        $electdiv = $db->getrow(knjz211gQuery::getClassMst($model->field["SUBCLASSCD"], $model->field["GRADE"], $model), DB_FETCHMODE_ASSOC);

        //選択教科と同様に英字で入力する科目コード取得
        if ($db->getOne(knjz211gQuery::getNameMstD065($model)) > 0) {
            $electdiv["ELECTDIV"] = 1;
        }

        $arrJviewAssMst = array();
        $result = $db->query(knjz211gQuery::selectDetailQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrJviewAssMst[$row["ASSESSLEVEL"]][$row["JVIEW_CNT"]] = $row["STATUS"];
        }
        //初期化
        $model->data = array();
        $counter = 0;
        $disable = "disabled";

        //一覧表示
        $result = $db->query(knjz211gQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // //学籍番号を配列で取得
            $model->data["HYOUTEN"][] = $row["ASSESSLEVEL"];

            // //各項目を作成
            $row["HYOUTEN"] = "<font color=\"#000000\">".$row["ASSESSLEVEL"]."</font>";
            foreach ($view_key as $code => $col) {
                $extra = "STYLE=\"text-align: center\" onPaste=\"return showPaste(this);\" onChange=\"this.style.background='#FFFFFF'\"";
                $len = $model->Properties["useHyoukaHyouteiFlg"] == '1' ? 3 : strlen($model->maxValue);
                $setWk = get_count($arrJviewAssMst) > 0 ? $arrJviewAssMst[$row["ASSESSLEVEL"]][$code] : "";
                if ($setWk == "11") $setWk = "A";
                if ($setWk == "22") $setWk = "B";
                if ($setWk == "33") $setWk = "C";
                $row["STATUS".$code] = knjCreateTextBox($objForm, $setWk, "STATUS".$code."-".$counter, 3, $len, $extra);
                $disable = "";
            }

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

        // 評価・評定の文言
        $arg["HyoukaHyouteiWord"] = "評点(評価)";

        //更新ボタン
        $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? $disable : "disabled";
        $extra = "onclick=\"return btn_submit('update', '".$electdiv["ELECTDIV"]."');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJZ211G");
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "MAXVALUE", $model->maxValue);

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
        //教科コード
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass = explode("-", $model->field["SUBCLASSCD"]);
            knjCreateHidden($objForm, "CLASSCD", $subclass[0].'-'.$subclass[1]);
        } else {
            knjCreateHidden($objForm, "CLASSCD", substr($model->field["SUBCLASSCD"], 0, 2));
        }
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useHyoukaHyouteiFlg", $model->Properties["useHyoukaHyouteiFlg"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz211gForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $opt=array())
{
    //$opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
    if ($query != "") {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
    } else {
      if ($value) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
