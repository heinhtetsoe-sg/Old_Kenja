<?php

require_once('for_php7.php');

class knje063pform2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje063pindex.php", "", "edit");

        $db = Query::dbCheckOut();
        $Row = array();
        $Row = $this->makeSelectData($db, $model);

        //年度コンボボックス作成
        $opt_year = $this->makeYear($db, $model);
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $Row["YEAR"], $opt_year, "onChange=\"return btn_submit('add_year');\"", 1)
                       ."&nbsp;&nbsp;".knjCreateTextBox($objForm, "", "year_add", 5, 4, "onblur=\"this.value=toInteger(this.value);\"")
                       ."&nbsp;".knjCreateBtn($objForm, "btn_year_add", "年度追加", "onclick=\"return add('');\"");

        //教育課程
        $query = knje063pQuery::getCurriculum();
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CURRICULUM_CD"], "CURRICULUM_CD", $extra, 1, "BLANK");

        //教科取得
        $class_chk = $this->makeClass($objForm, $db, $arg, $model);

        //科目作成
        $this->makeSubclass($objForm, $db, $arg, $class_chk, $Row, $model);

        //学年(年次)
        $extra = "STYLE=\"text-align: right\" onblur=\"this.value = toInteger(this.value); return btn_submit('add_year');\"";
        $arg["ANNUAL"] = knjCreateTextBox($objForm, $Row["ANNUAL"], "ANNUAL", 2, 2, $extra);

        //備考
        $arg["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 80, 150, "");

        //評定、単位作成
        $this->makeSetData($objForm, $db, $arg, $model, $Row);

        //ボタン作成
        $this->makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        $this->makeHidden($objForm, $Row);

        $arg["finish"]  = $objForm->get_finish();
        if (isset($model->message)){
            $arg["reload"]  = "window.open('knje063pindex.php?cmd=list&SCHREGNO=$model->schregno','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje063pForm2.html", $arg);
    }

    //表示データ作成
    function makeSelectData($db, &$model) {
        $Row = array();
        $Row["YEAR"]                = "";
        $Row["SUBCLASSCD"]          = "";
        $Row["SUBCLASSNAME"]        = "";
        $Row["SUBCLASSABBV"]        = "";
        $Row["SUBCLASSNAME_ENG"]    = "";
        $Row["SUBCLASSABBV_ENG"]    = "";
        $subclasscd="";

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)){ 
            if ($model->cmd == "add_year" || $model->cmd == "subclasscd") {
                $Row =& $model->field;
            } else { 
                $query = knje063pQuery::selectQuery($model);
                $result = $db->query($query);
                while ($Mid = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($Mid["CHECKSUBCD"]) {
                        $checksubcd = "_M";
                    }
                    $Row["SCHREGNO"]          = $Mid["SCHREGNO"];
                    $Row["YEAR"]              = $Mid["YEAR"];
                    $Row["ANNUAL"]            = $Mid["ANNUAL"];
                    $Row["REMARK"]            = $Mid["REMARK"];
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $Row["CLASSCD"]       = $Mid["CLASSCD"]."-".$Mid["SCHOOL_KIND"]."-".$Mid["CURRICULUM_CD"]."-".substr($Mid["SUBCLASSCD"],0,2);
                    } else {
                        $Row["CLASSCD"]       = $Mid["CLASSCD"];
                    }
                    $Row["SUBCLASSCD"]        = substr($Mid["SUBCLASSCD"],2,6);
                    $Row["SUBCLASSNAME"]      = $Mid["SUBCLASSNAME".$checksubcd];
                    $Row["SUBCLASSABBV"]      = $Mid["SUBCLASSABBV".$checksubcd];
                    $Row["SUBCLASSNAME_ENG"]  = $Mid["SUBCLASSNAME_ENG".$checksubcd];
                    $Row["SUBCLASSABBV_ENG"]  = $Mid["SUBCLASSABBV_ENG".$checksubcd];
                    $Row[$Mid["SCHOOLCD"]]["VALUATION"]   = $Mid["VALUATION"];
                    $Row[$Mid["SCHOOLCD"]]["GET_CREDIT"]  = $Mid["GET_CREDIT"];
                    $Row[$Mid["SCHOOLCD"]]["ADD_CREDIT"]  = $Mid["ADD_CREDIT"];
                    $Row[$Mid["SCHOOLCD"]]["COMP_CREDIT"] = $Mid["COMP_CREDIT"];
                    if ($model->cmd == "class") {
                        $Row["YEAR"]              = $model->field["YEAR"];
                        $Row["ANNUAL"]            = $model->field["ANNUAL"];
                        $Row["REMARK"]            = "";
                        $Row["SUBCLASSCD"]        = "";
                        $Row["SUBCLASSNAME"]      = "";
                        $Row["SUBCLASSABBV"]      = "";
                        $Row["SUBCLASSNAME_ENG"]  = "";
                        $Row["SUBCLASSABBV_ENG"]  = "";
                        $Row[$Mid["SCHOOLCD"]]["VALUATION"]   = "";
                        $Row[$Mid["SCHOOLCD"]]["GET_CREDIT"]  = "";
                        $Row[$Mid["SCHOOLCD"]]["ADD_CREDIT"]  = "";
                        $Row[$Mid["SCHOOLCD"]]["COMP_CREDIT"] = "";
                    }
                }
            }
        }else{
            $Row =& $model->field;
        }
        return $Row;
    }

    //年度作成
    function makeYear($db, &$model) {
        //年度取得
        $query = knje063pQuery::selectQueryYear($model);
        $result = $db->query($query);
        $make_year = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $model->year[] = $row["YEAR"];
        }


        //年度追加された値を保持 
        $year_arr = array_unique($model->year);
        foreach ($year_arr as $val)
        {
            $make_year[] = array("label" => $val, "value" => $val);
        }
        rsort($make_year);
        return $make_year;
    }

    //教科作成
    function makeClass(&$objForm, $db, &$arg, &$model) {
        $query  = " SELECT * FROM class_mst ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= ",SCHOOL_KIND";
        }
        $result = $db->query($query);
        $opt_c = array();
        $i = 0;
        while($row_c = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $classcd = $row_c["CLASSCD"];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classcd .= "-".$row_c["SCHOOL_KIND"]."-".$row_c["CLASSCD"];
            }
            $opt_c[] = array("label" => htmlspecialchars($classcd."：".$row_c["CLASSNAME"]), 
                             "value" => $classcd);

            if ($model->class_select=="" && $i==0){

                $class_chk = $classcd;
                $show["CLASSNAME"]        = $row_c["CLASSNAME"];
                $show["CLASSABBV"]        = $row_c["CLASSABBV"];
                $show["CLASSNAME_ENG"] = $row_c["CLASSNAME_ENG"];
                $show["CLASSABBV_ENG"] = $row_c["CLASSABBV_ENG"];
                $i++;

            } else if ($model->class_select==$classcd){

                $class_chk = $classcd;
                $show["CLASSNAME"]        = $row_c["CLASSNAME"];
                $show["CLASSABBV"]        = $row_c["CLASSABBV"];
                $show["CLASSNAME_ENG"] = $row_c["CLASSNAME_ENG"];
                $show["CLASSABBV_ENG"] = $row_c["CLASSABBV_ENG"];

            }

        }

        //科目コンボ作成
        $arg["data"]["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", $class_chk, $opt_c, "onchange=\"btn_submit('class');\"", 1);
        //教科名称
        $arg["data"]["CLASSABBV"]     = $show["CLASSABBV"];
        $arg["data"]["CLASSNAME_ENG"] = $show["CLASSNAME_ENG"];
        $arg["data"]["CLASSABBV_ENG"] = $show["CLASSABBV_ENG"];

        return $class_chk;
    }

    //科目作成
    function makeSubclass(&$objForm, $db, &$arg, $class_chk, $Row, $model) {

        //科目取得
        $query = "SELECT * FROM SUBCLASS_MST WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " CURRICULUM_CD || '-' || ";
            $query .= " CLASSCD || '-' || ";
            $query .= " SCHOOL_KIND || '-' || ";
        }
        $query .= "      substr(SUBCLASSCD,1,2) = '".$model->field["CURRICULUM_CD"]."-".$class_chk."' ORDER BY SUBCLASSCD";
        $result = $db->query($query);
        $opt_s = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_s[] = array("label" => htmlspecialchars(substr($row["SUBCLASSCD"],2,6).":".$row["SUBCLASSNAME"]), 
                             "value" => $row["SUBCLASSCD"].":".$row["SUBCLASSABBV"].":".$row["SUBCLASSNAME_ENG"].":".$row["SUBCLASSABBV_ENG"]);

        }

        //科目コンボ
        $arg["data"]["SUBCLASS"] = knjCreateCombo($objForm, "SUBCLASS", "", $opt_s, "", 1);
        //科目の読込ボタンを作成する
        $arg["button"]["btn_sub"] = knjCreateBtn($objForm, "btn_sub", "＜", "onclick=\"return add2('');\"");
        //科目コード
        $arg["data"]["SUBCLASSCD"] = knjCreateTextBox($objForm, $Row["SUBCLASSCD"], "SUBCLASSCD", 8, 4, "onBlur=\"this.value = toInteger(this.value); return btn_submit('add_year');\"");
        //科目名
        $arg["data"]["SUBCLASSNAME"] = knjCreateTextBox($objForm, $Row["SUBCLASSNAME"], "SUBCLASSNAME", 40, 60, "");
        //科目略称
        $arg["data"]["SUBCLASSABBV"] = knjCreateTextBox($objForm, $Row["SUBCLASSABBV"], "SUBCLASSABBV", 5, 9, "");
        //英字科目名
        $extra = "onblur=\"return moji_hantei(this)\" STYLE=\"ime-mode:disabled\"";
        $arg["data"]["SUBCLASSNAME_ENG"] = knjCreateTextBox($objForm, $Row["SUBCLASSNAME_ENG"], "SUBCLASSNAME_ENG", 40, 40, $extra);
        //英字略称
        $arg["data"]["SUBCLASSABBV_ENG"] = knjCreateTextBox($objForm, $Row["SUBCLASSABBV_ENG"], "SUBCLASSABBV_ENG", 20, 20, $extra);

    }

    //評定、単位作成
    function makeSetData(&$objForm, $db, &$arg, &$model, $Row) {
        //設定種別にあわせてrowspan値を変化
        $arg["span"] = $model->grade_range + 1;
        //設定種別名称設定
        $schoolName[0] = "在籍中";
        $schoolName[1] = "在籍前";
        $setdisabled = "disabled";


        //観点コード(MAX5)
        $model->field["CLASSCD"] = $Row["CLASSCD"];
        $model->field["SUBCLASSCD"] = $Row["SUBCLASSCD"];
        $model->view_key = array();
        $view_cnt = 0;
        $view_html = "";
        $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
        $query = knje063pQuery::selectViewcdQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $view_cnt++;
            if ($view_cnt > 5) break;   //MAX5
            $model->view_key[$view_cnt] = $row["VIEWCD"];  //1,2,3,4,5
            //チップヘルプ
            $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
            knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this, '".$electdiv["ELECTDIV"]."');\" oncontextmenu=\"kirikae2(this, 'STATUS".$code."')\";";
            $RowKanten["STATUS".$view_cnt] = knjCreateTextBox($objForm, "", "STATUS".$view_cnt, 3, 1, $extra);
        }
        for ($i=0; $i<(5-get_count($model->view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        $arg["view_html"] = $view_html;

        //選択教科
        $electdiv = $db->getRow(knje063pQuery::getClassMst($model), DB_FETCHMODE_ASSOC);

        //初期化
        $model->data = array();
        $disable = "disabled";

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

        //一覧表示
        $statusData = false;
        $query = knje063pQuery::selectQuery($model, $model->view_key);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //観点項目を作成
            foreach ($model->view_key as $code => $col) {
                //権限
                if (DEF_UPDATE_RESTRICT <= AUTHORITY) {

                    //各観点コードを取得
                    $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this, '".$electdiv["ELECTDIV"]."');\" oncontextmenu=\"kirikae2(this, 'STATUS".$code."')\";";
                    $RowKanten["STATUS".$code] = knjCreateTextBox($objForm, $row["STATUS".$code], "STATUS".$code, 3, 1, $extra);

                    //更新ボタンのＯＮ／ＯＦＦ
                    $disable = "";

                //ラベルのみ
                } else {
                    $RowKanten["STATUS".$code] = "<font color=\"#000000\">".$row["STATUS".$code]."</font>";
                }
            }
            $statusData = true;
        }

        $list_d = array();
        for ($i = 0; $i < $model->grade_range; $i++) {
            //schoolcd
            $disabled = "";
            $school = $i;
            if(!in_array($i, array_keys($Row))){
                $Row[$i]["VALUATION"]   = "";
                $Row[$i]["GET_CREDIT"]  = "";
                $Row[$i]["ADD_CREDIT"]  = "";
                $Row[$i]["COMP_CREDIT"] = "";
            }

            //評定
            if ($i == 2) {
                $extra = "$setdisabled onChange=\"this.style.background='#ccffcc'\" onBlur=\"this.value = toInteger(this.value);\" STYLE=\"text-align: right\"";
            } else {
                $extra = "onChange=\"this.style.background='#ccffcc'\" onBlur=\"this.value = toInteger(this.value);\" STYLE=\"text-align: right\"";
            }
            if ($i == 0) {
                $Row_d = $RowKanten;
                if ($Row[$i]["VALUATION"] == "" && !$statusData) {
                    $disabled = "disabled";
                }
            } else {
                foreach ($model->view_key as $code => $col) {
                    $Row_d["STATUS".$code] = "";
                }
                if ($Row[$i]["VALUATION"] == "") {
                    $disabled = "disabled";
                }
            }
            //名称
            $Row_d["SCHOOLNAME"] = $schoolName[$i];
            $Row_d["viewhelp"] = "onMouseOver=\"ViewcdMousein(".$i.")\" onMouseOut=\"ViewcdMouseout()\"";
            $Row_d["VALUATION"] = knjCreateTextBox($objForm, $Row[$i]["VALUATION"], "VALUATION".$i, 2, 2, $extra);

            //削除用チェックボックス
            $Row_d["CHECKED2"] = knjCreateCheckBox($objForm, "CHECKED2", $i ."," .$Row["YEAR"] ."," .$Row["ANNUAL"] ."," .$Row["CLASSCD"].$Row["SUBCLASSCD"], $disabled, "1");

            $arg["list_d"][] = $Row_d;
        }
    }

    //ボタン作成
    function makeBtn(&$objForm, &$arg, $model) {

        //修正ボタンを作成する
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");

        //削除ボタンを作成する
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete2');\"");

        //観点入力
        $extra  = " onClick=\" wopen('".REQUESTROOT."/E/KNJE063P_2/knje063p_2index.php?";
        $extra .= "cmd=&SEND_PRGID=KNJE063P&SEND_AUTH=".AUTHORITY."&SCHREGNO={$model->schregno}";
        $extra .= "','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $extra .= $model->schregno ? "" : " disabled ";
        $arg["button"]["btn_063_2call"] = knjCreateBtn($objForm, "btn_063_2call", "観点参照", $extra);

        //クリアボタンを作成する
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return Btn_reset('edit');\"");

        //終了ボタンを作成する
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
    }

    //hidden作成
    function makeHidden(&$objForm, $Row) {
        //cmd
        knjCreateHidden($objForm, "cmd");
        //教科名
        knjCreateHidden($objForm, "CLASSNAME", $Row["CLASSNAME"]);
        knjCreateHidden($objForm, "SETVAL", "1,2,3");
        knjCreateHidden($objForm, "SETSHOW", "A,B,C");
    }

}
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
