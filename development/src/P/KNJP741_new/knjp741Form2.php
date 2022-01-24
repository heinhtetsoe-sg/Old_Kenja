<?php

require_once('for_php7.php');

class knjp741form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp741index.php", "", "edit");

        $db = Query::dbCheckOut();
        $Row = array();
        $Row = $this->makeSelectData($db, $model);

        //年度コンボボックス作成
        $opt_year = $this->makeYear($db, $model);
        $arg["YEAR"] = $this->createCombo($objForm, "YEAR", $Row["YEAR"], $opt_year, "onChange=\"return btn_submit('add_year');\"", 1)
                       ."&nbsp;&nbsp;".$this->createText($objForm, "year_add", "", "onblur=\"this.value=toInteger(this.value);\"", 5, 4)
                       ."&nbsp;".$this->createBtn($objForm, "btn_year_add", "年度追加", "onclick=\"return add('');\"");

        if ($model->Properties["useCurriculumcd"] == '1') {
            //教育課程
            $query = knjp741Query::getCurriculum();
            $extra = "onChange=\"return btn_submit('curriculum');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["CURRICULUM_CD"], "CURRICULUM_CD", $extra, 1, "BLANK");
        }

        $longCol = "7";
        $shortCol = "3";
        $dataWidth = "75";
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
            $longCol = "8";
            $shortCol = "4";
            $dataWidth = "60";
        }
        $arg["LONG_COL"] = $longCol;
        $arg["SHORT_COL"] = $shortCol;
        $arg["DATA_WIDTH"] = $dataWidth;

        //教科取得
        $class_chk = $this->makeClass($objForm, $db, $arg, $model);

        //科目作成
        $this->makeSubclass($objForm, $db, $arg, $class_chk, $Row, $model);

        //学年(年次)
        $extra = "STYLE=\"text-align: right\" onblur=\"this.value = toInteger(this.value); setDisabled(this)\"";
        $arg["ANNUAL"] = $this->createText($objForm, "ANNUAL", $Row["ANNUAL"], $extra, 2, 2);

        //備考
        $arg["REMARK"] = $this->createText($objForm, "REMARK", $Row["REMARK"], "", 80, 150);

        //checkbox
        $query = knjp741Query::getMaxRegdYear($model);
        $maxYear = $db->getOne($query);
        $yuukouDis = $maxYear == $Row["YEAR"] ? " disabled " : "";
        $extra = $Row["YUUKOU_FLG"] == "1" ? " checked " : "";
        $arg["YUUKOU_FLG"] = knjCreateCheckBox($objForm, "YUUKOU_FLG", "1", $extra.$yuukouDis);

        //評定、単位作成
        $this->makeSetData($objForm, $db, $arg, $model, $Row);

        //ボタン作成
        $this->makeBtn($objForm, $arg);

        //hiddenを作成する
        $this->makeHidden($objForm, $Row);

        $arg["finish"]  = $objForm->get_finish();
        if (isset($model->message)){
            $arg["reload"]  = "window.open('knjp741index.php?cmd=list&SCHREGNO=$model->schregno','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp741Form2.html", $arg);
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
                $query = knjp741Query::selectQuery($model);
                $result = $db->query($query);
                while ($Mid = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($Mid["CHECKSUBCD"]) {
                        $checksubcd = "_M";
                    }
                    $Row["SCHREGNO"]          = $Mid["SCHREGNO"];
                    $Row["YEAR"]              = $Mid["YEAR"];
                    $Row["ANNUAL"]            = $Mid["ANNUAL"];
                    $Row["REMARK"]            = $Mid["REMARK"];
                    $Row["YUUKOU_FLG"]        = $Mid["YUUKOU_FLG"];
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
                    $Row[$Mid["SCHOOLCD"]]["PROV_FLG"]    = $Mid["PROV_FLG"];
                    $Row[$Mid["SCHOOLCD"]]["VALUATION"]   = $Mid["VALUATION"];
                    $Row[$Mid["SCHOOLCD"]]["GET_CREDIT"]  = $Mid["GET_CREDIT"];
                    $Row[$Mid["SCHOOLCD"]]["ADD_CREDIT"]  = $Mid["ADD_CREDIT"];
                    $Row[$Mid["SCHOOLCD"]]["COMP_CREDIT"] = $Mid["COMP_CREDIT"];
                    if ($model->cmd == "class" || $model->cmd == "curriculum") {
                        $Row["YEAR"]              = $model->field["YEAR"];
                        $Row["ANNUAL"]            = $model->field["ANNUAL"];
                        $Row["REMARK"]            = "";
                        $Row["YUUKOU_FLG"]        = "";
                        $Row["SUBCLASSCD"]        = "";
                        $Row["SUBCLASSNAME"]      = "";
                        $Row["SUBCLASSABBV"]      = "";
                        $Row["SUBCLASSNAME_ENG"]  = "";
                        $Row["SUBCLASSABBV_ENG"]  = "";
                        $Row[$Mid["SCHOOLCD"]]["PROV_FLG"]   = "";
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
        $query = knjp741Query::selectQueryYear($model);
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

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     class_mst ";
        if ($model->Properties["useSchool_KindField"] == "1" || $model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = (SELECT ";
            $query .= "                        SCHOOL_KIND ";
            $query .= "                    FROM ";
            $query .= "                        SCHREG_REGD_GDAT ";
            $query .= "                    WHERE ";
            $query .= "                            YEAR  = '".CTRL_YEAR."' ";
            $query .= "                        AND GRADE = '".$model->grade."' ";
            $query .= "                    ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLASSCD ";
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
        $arg["data"]["CLASSCD"] = $this->createCombo($objForm, "CLASSCD", $class_chk, $opt_c, "onchange=\"btn_submit('class');\"", 1);
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
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      substr(SUBCLASSCD,1,2) = '".$model->field["CURRICULUM_CD"]."-".$class_chk."' ORDER BY SUBCLASSCD";
        } else {
            $query .= "      substr(SUBCLASSCD,1,2) = '".$class_chk."' ORDER BY SUBCLASSCD";
        }
        $result = $db->query($query);
        $opt_s = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_s[] = array("label" => htmlspecialchars(substr($row["SUBCLASSCD"],2,6).":".$row["SUBCLASSNAME"]), 
                             "value" => $row["SUBCLASSCD"].":".$row["SUBCLASSABBV"].":".$row["SUBCLASSNAME_ENG"].":".$row["SUBCLASSABBV_ENG"]);

        }

        //科目コンボ
        $arg["data"]["SUBCLASS"] = $this->createCombo($objForm, "SUBCLASS", "", $opt_s, "", 1);
        //科目の読込ボタンを作成する
        $arg["button"]["btn_sub"] = $this->createBtn($objForm, "btn_sub", "＜", "onclick=\"return add2('');\"");
        //科目コード
        $arg["data"]["SUBCLASSCD"] = $this->createText($objForm, "SUBCLASSCD", $Row["SUBCLASSCD"], "onBlur=\"this.value = toInteger(this.value);\"", 8, 4);
        //科目名
        $arg["data"]["SUBCLASSNAME"] = $this->createText($objForm, "SUBCLASSNAME", $Row["SUBCLASSNAME"], "", 40, 60);
        //科目略称
        $arg["data"]["SUBCLASSABBV"] = $this->createText($objForm, "SUBCLASSABBV", $Row["SUBCLASSABBV"], "", 5, 9);
        //英字科目名
        $extra = "onblur=\"return moji_hantei(this)\" STYLE=\"ime-mode:disabled\"";
        $arg["data"]["SUBCLASSNAME_ENG"] = $this->createText($objForm, "SUBCLASSNAME_ENG", $Row["SUBCLASSNAME_ENG"], $extra, 40, 40);
        //英字略称
        $arg["data"]["SUBCLASSABBV_ENG"] = $this->createText($objForm, "SUBCLASSABBV_ENG", $Row["SUBCLASSABBV_ENG"], $extra, 20, 20);

    }

    //評定、単位作成
    function makeSetData(&$objForm, $db, &$arg, $model, $Row) {
        //設定種別にあわせてrowspan値を変化
        $arg["span"] = $model->grade_range + 1;
        //設定種別名称設定
        $schoolName[0] = "在籍中";
        $schoolName[1] = "在籍前";
        $schoolName[2] = "高認試験";
        $setdisabled = "disabled";

        $list_d = array();
        for($i = 0; $i < $model->grade_range; $i++){
            //schoolcd
            $disabled = "";
            $school = $i;
            if(!in_array($i, array_keys($Row))){
                $Row[$i]["PROV_FLG"]   = "";
                $Row[$i]["VALUATION"]   = "";
                $Row[$i]["GET_CREDIT"]  = "";
                $Row[$i]["ADD_CREDIT"]  = "";
                $Row[$i]["COMP_CREDIT"] = "";
            }
            $query = knjp741Query::getStudyRecCnt($Row, $i, $model);
            $studyRecCnt = $db->getOne($query);
            if ($studyRecCnt == 0) {
                $disabled = "disabled";
            }
            //名称
            $Row_d["SCHOOLNAME"] = $schoolName[$i];
            $Row_d["viewhelp"] = "onMouseOver=\"ViewcdMousein(".$i.")\" onMouseOut=\"ViewcdMouseout()\"";

            //仮評定(在籍中のみ)
            if ($i == 0) {
                if ($Row[$i]["PROV_FLG"]== "1") {
                    $extra = "checked='checked' ";
                } else {
                    $extra = "";
                }
                $Row_d["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG".$i, "1", $extra);
            } else {
                $Row_d["PROV_FLG"] = "";
            }

            //評定
            if ($i == 2) {
                $extra = "$setdisabled onBlur=\"this.value = toInteger(this.value);\" STYLE=\"text-align: right\"";
            } else {
                $extra = "onBlur=\"this.value = toInteger(this.value);\" STYLE=\"text-align: right\"";
            }
            $Row_d["VALUATION"] = $this->createText($objForm, "VALUATION".$i, $Row[$i]["VALUATION"], $extra, 2, 2);

            //修得単位
            $extra = "onBlur=\"this.value = toInteger(this.value);\" STYLE=\"text-align: right\"";
            $Row_d["GET_CREDIT"] = $this->createText($objForm, "GET_CREDIT".$i, $Row[$i]["GET_CREDIT"], $extra, 2, 2);

            //増加単位
            if ($i > 0) {
                $extra = "$setdisabled onBlur=\"this.value = toInteger(this.value);\" STYLE=\"text-align: right\"";
            } else {
                $extra = "onBlur=\"this.value = toInteger(this.value);\" STYLE=\"text-align: right\"";
            }
            $Row_d["ADD_CREDIT"] = $this->createText($objForm, "ADD_CREDIT".$i, $Row[$i]["ADD_CREDIT"], $extra, 2, 2);

            //履修単位
            $extra = "onBlur=\"this.value = toInteger(this.value);\" STYLE=\"text-align: right\"";
            $Row_d["COMP_CREDIT"] = $this->createText($objForm, "COMP_CREDIT".$i, $Row[$i]["COMP_CREDIT"], $extra, 2, 2);

            //更新用チェックボックス
            $extra = $i != "1" && $Row["YEAR"] == "0" && $Row["ANNUAL"] == "0" ? " disabled " : "";
            $Row_d["CHECKED1"] = knjCreateCheckBox($objForm, "CHECKED1".$i, "1", $extra);

            //削除用チェックボックス
            $Row_d["CHECKED2"] = knjCreateCheckBox($objForm, "CHECKED2", $i ."," .$Row["YEAR"] ."," .$Row["ANNUAL"] ."," .$Row["CLASSCD"].$Row["SUBCLASSCD"], $disabled, "1");

            $arg["list_d"][] = $Row_d;
        }
    }

    //ボタン作成
    function makeBtn(&$objForm, &$arg) {

        //修正ボタンを作成する
        $arg["button"]["btn_update"] = $this->createBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");

        //削除ボタンを作成する
        $arg["button"]["btn_del"] = $this->createBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete2');\"");

        //クリアボタンを作成する
        $arg["button"]["btn_reset"] = $this->createBtn($objForm, "btn_reset", "取 消", "onclick=\"return Btn_reset('edit');\"");

        //終了ボタンを作成する
        $arg["button"]["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
    }

    //hidden作成
    function makeHidden(&$objForm, $Row) {
        //cmd
        $objForm->ae($this->createHiddenAe("cmd"));
        //教科名
        $objForm->ae($this->createHiddenAe("CLASSNAME", $Row["CLASSNAME"]));
    }

    //コンボ作成
    function createCombo(&$objForm, $name, $value, $options, $extra, $size)
    {
        $objForm->ae( array("type"      => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //テキスト作成
    function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
    {
        //単位数
        $objForm->ae( array("type"      => "text",
                            "name"      => $name,
                            "size"      => $size,
                            "maxlength" => $maxlen,
                            "extrahtml" => $extra,
                            "value"     => $value));
        return $objForm->ge($name);
    }

    //ラジオ作成
    function createCheckBox(&$objForm, $name, $value, $extra, $multi) {

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));

        return $objForm->ge($name);
    }

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

    //Hidden作成ae
    function createHiddenAe($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
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
