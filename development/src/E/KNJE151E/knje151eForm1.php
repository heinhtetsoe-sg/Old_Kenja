<?php

require_once('for_php7.php');

class knje151eForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knje151eindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query = knje151eQuery::getNameMst($setNameCd);
        $extra = "onchange=\"return btn_submit('form1')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ作成
        $query = knje151eQuery::getHrClass($model);
        $extra = "onchange=\"return btn_submit('form1')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //教科コンボ作成
        $query = knje151eQuery::getClassMst($model);
        $extra = "onchange=\"return btn_submit('form1')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        //科目コンボ作成
        $query = knje151eQuery::getSubclassMst($model, $model->field["CLASSCD"]);
        $extra = "onchange=\"return btn_submit('form1')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //入力選択ラジオボタン 1:マウス入力 2:手入力
        $opt_select = array(1, 2);
        $model->select = ($model->select == "") ? "1" : $model->select;
        $extra = array("id=\"SELECT1\" onclick =\" return btn_submit('select1');\"", "id=\"SELECT2\" onclick =\" return btn_submit('select2');\"");
        $radioArray = knjCreateRadio($objForm, "SELECT", $model->select, $extra, $opt_select, get_count($opt_select));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //入力選択ラジオボタン 1:値選択 2:データクリア
        $opt_nyuryoku = array(1, 2);
        $model->nyuryoku = ($model->nyuryoku == "") ? "1" : $model->nyuryoku;
        $extra = array("id=\"NYURYOKU1\" onClick=\"myHidden()\"", "id=\"NYURYOKU2\" onClick=\"myHidden()\"");
        $radioArray = knjCreateRadio($objForm, "NYURYOKU", $model->nyuryoku, $extra, $opt_nyuryoku, get_count($opt_nyuryoku));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //入力値選択ラジオボタン 1:Ａ 2:Ｂ 3:Ｃ
        $opt_data = array(1, 2, 3);
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $extra = array("id=\"TYPE_DIV1\"", "id=\"TYPE_DIV2\"", "id=\"TYPE_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //観点コード(MAX5)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
        $result = $db->query(knje151eQuery::selectViewcdQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $view_cnt++;
            if ($view_cnt > 5) break;   //MAX5
            $view_key[$view_cnt] = $row["VIEWCD"];  //1,2,3,4,5
            //チップヘルプ
            $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
            knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
        }
        for ($i=0; $i<(5-get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        $arg["view_html"] = $view_html;
        //評定用観点コード
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        if ($view_cnt > 0) $view_key[9] = substr($model->field["CLASSCD"], 0, 2)."99";
        } else {
	        if ($view_cnt > 0) $view_key[9] = $model->field["CLASSCD"]."99";
        }

        //選択教科
        $electdiv = $db->getrow(knje151eQuery::getClassMst($model, $model->field["CLASSCD"]), DB_FETCHMODE_ASSOC);

        //初期化
        $model->data = array();
        $counter = 0;
        $disable = "disabled";

        //一覧表示
        $result = $db->query(knje151eQuery::selectQuery($model, $view_key));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //クラス-出席番(表示)
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"]."番";

            //名前
            $row["NAME_SHOW"] = $row["SCHREGNO"]." ".$row["NAME_SHOW"];

            //各項目を作成
            foreach ($view_key as $code => $col)
            {
                //選択教科の評定は、A,B,Cに変換
                if ($code == "9" && $electdiv["ELECTDIV"] != "0") {
                    $status = $row["STATUS".$code];
                    if ($status == "11") $status = "A";
                    if ($status == "22") $status = "B";
                    if ($status == "33") $status = "C";
                    $row["STATUS".$code] = $status;
                }

                //権限
                if (DEF_UPDATE_RESTRICT <= AUTHORITY) {

                    //各観点コードを取得
                    $model->data["STATUS"][$code] = $col;

                    if($code == "9") {
                        $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this, '".$electdiv["ELECTDIV"]."');\"";
                    } else {
                        $extra = "STYLE=\"text-align: center\" readonly=\"readonly\" onClick=\"kirikae(this, 'STATUS".$code."-".$counter."')\" oncontextmenu=\"kirikae2(this, 'STATUS".$code."-".$counter."')\"; ";
                    }
                    $row["STATUS".$code] = knjCreateTextBox($objForm, $row["STATUS".$code], "STATUS".$code."-".$counter, 3, 1, $extra);

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

        $type_div = array("1" => "A", "2" => "B", "3" => "C");
        foreach ($type_div as $key => $val) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$key."')\"",
                                 "NAME" => $val);
        }

        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"] = $val["VAL"];
            $arg["menu"][] = $setData;
        }
        $result->free();

        //更新ボタン
        $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? $disable : "disabled";
        $extra = "onclick=\"return btn_submit('update', '".$electdiv["ELECTDIV"]."');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJE151E");
        knjCreateHidden($objForm, "CTRL_Y", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_S", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_D", CTRL_DATE);

        knjCreateHidden($objForm, "SETVAL", "1,2,3");
        knjCreateHidden($objForm, "SETSHOW", "A,B,C");
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje151eForm1.html", $arg);
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
