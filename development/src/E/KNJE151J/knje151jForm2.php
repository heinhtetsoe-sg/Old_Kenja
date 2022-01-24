<?php

require_once('for_php7.php');

class knje151jForm2
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form2", "POST", "knje151jindex.php", "", "form2");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ作成
        $query = knje151jQuery::getNameMstA023($model);
        $extra = "onchange=\"return btn_submit('form2')\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //教科コンボ作成
        $query = knje151jQuery::getClassMst($model);
        $extra = "onchange=\"return btn_submit('form2')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        //科目コンボ作成
        $query = knje151jQuery::getSubclassMst($model->field["CLASSCD"], $model);
        $extra = "onchange=\"return btn_submit('form2')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        
        //講座コンボ
        $query = knje151jQuery::selectChairQuery($model);
        $extra = "onchange=\"return btn_submit('form2')\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");

        //入力選択ラジオボタン 1:マウス入力 2:手入力
        $opt_select = array(1, 2);
        $model->select = ($model->select == "") ? "1" : $model->select;
        $extra = array("id=\"SELECT1\" onclick =\" return btn_submit('select1');\"", "id=\"SELECT2\" onclick =\" return btn_submit('select2');\"");
        $radioArray = knjCreateRadio($objForm, "SELECT", $model->select, $extra, $opt_select, get_count($opt_select));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //生徒を抽出する日付(追加)
        $sdate = str_replace("/","-",$model->control["学期開始日付"]["9"]);
        $edate = str_replace("/","-",$model->control["学期終了日付"]["9"]);
        $execute_date = ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) ? CTRL_DATE : $edate;//初期値
        
        //観点コード(MAX5)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
        $result = $db->query(knje151jQuery::selectViewcdQuery($model));
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
        $electdiv = $db->getrow(knje151jQuery::getClassMst($model, $model->field["CLASSCD"]), DB_FETCHMODE_ASSOC);

        //名称マスタ「D065」設定科目の時、選択教科と同様に処理する。
        $cntD065 = $db->getOne(knje151jQuery::getCntNameMstD065($model));
        if (0 < $cntD065) $electdiv["ELECTDIV"] = "1";

        //初期化
        $model->data = array();
        $counter = 0;
        $disable = "disabled";

        //追加元に戻すための配列(追加)
        $setdata = array();
        $kuuhaku_frg = false;
        //一覧表示
        $result = $db->query(knje151jQuery::selectQuery($model, $execute_date, $view_key));
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
                $row["STATUS_LABEL".$code] = "<font color=\"#000000\">".$row["STATUS".$code]."</font>";

                //権限
                if (DEF_UPDATE_RESTRICT <= AUTHORITY) {

                    $query = knje151jQuery::getJviewName_Viewcd($model);
                    $viewcd_count = $db->getOne($query);
                    //学年の確認(追加)
                    if ($row["GRADE"]) {
                        $query = knje151jQuery::getSchool_kind($row["GRADE"]);
                        $School_kind = $db->getOne($query);
                        $query = knje151jQuery::getJviewName_School_kind($model, $School_kind);
                        $school_kind_count = $db->getOne($query);
                        if ($school_kind_count != $viewcd_count) {
                            $kuuhaku_frg = true;
                        }
                    }
                    //各観点コードを取得
                    $model->data["STATUS"][$code] = $col;

                    $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this, '".$electdiv["ELECTDIV"]."');\" onPaste=\"return showPaste(this);\"";
                    //rollback確認用(追加)
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
            $setdata[] = $row;
        }

        foreach ($setdata as $key => $val) {
            if ($kuuhaku_frg) {
                foreach ($view_key as $code => $col) {
                    $val["STATUS".$code] = $val["STATUS_LABEL".$code];
                }
            }
            $arg["data"][] =  $val;
        }

        //更新ボタン
        $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? $disable : "disabled";
        $extra = "onclick=\"return btn_submit('form2_update', '".$electdiv["ELECTDIV"]."');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('form2_reset');\"".$disable;
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
        knjCreateHidden($objForm, "PRGID", "KNJE151J");
        knjCreateHidden($objForm, "CTRL_Y", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_S", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_D", CTRL_DATE);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        //学期（観点データ用）
        knjCreateHidden($objForm, "SEMESTER", "9");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje151jForm2.html", $arg);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
