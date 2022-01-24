<?php

require_once('for_php7.php');

class knje030Form2
{
    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje030index.php", "", "edit");

        $db = Query::dbCheckOut();
        $temp_cd = "";
        $Row = array();
        $subclasscd="";

        //警告メッセージを表示しない場合
        if(isset($model->knje030cd) && !isset($model->warning)){
            $subclasscd = ($model->subclasscd)?"$model->subclasscd":"$model->knje030cd";
            $Row = $this->makeSelectData($db, $model, $subclasscd);
            $temp_cd = $Row["SUBCLASSCD"];
        }else{
            $Row =& $model->field;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $Row["SUBCLASSCD"]=substr($model->field["CLASSCD"], 0, 2).$model->field["SUBCLASSCD"];
            } else {
                $Row["SUBCLASSCD"]=$model->field["CLASSCD"].$model->field["SUBCLASSCD"];
            }
        }
        //教科作成
        $class_chk = $this->makeClass($objForm, $db, $arg, $model);

        //教育課程
        $query = knje030Query::getCurriculum();
        $extra = "onChange=\"return btn_submit('class');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CURRICULUM_CD"], "CURRICULUM_CD", $extra, 1, "BLANK");

        //科目作成
        $this->makeSubclass($objForm, $db, $arg, $class_chk, $Row, $model);

        //評定・単位作成
        $this->makeSetData($objForm, $db, $arg, $model, $Row);

        if($model->knje030schreg==""){
            $model->knje030schreg = $Row["SCHREGNO"];
        }

        //学籍番号
        if($model->knje030schreg !=""){
            $arg["data"]["SCHREGNO"] = "(学籍番号：".$model->knje030schreg.")";
        }

        //更新ボタンを作成する
        $arg["button"]["btn_update"] = $this->createBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");

        //削除ボタンを作成する
        $arg["button"]["btn_del"] = $this->createBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");

        //クリアボタンを作成する
        $arg["button"]["btn_reset"] = $this->createBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('reset');\"");

        //終了ボタンを作成する
        $arg["button"]["btn_back"] = $this->createBtn($objForm, "btn_back", "終 了", "onclick=\"closeWin();\"");

        if($temp_cd == ""){
            $temp_cd = $model->field["temp_cd"];
        }
        //hidden作成
        $this->makeHidden($objForm, $model, $Row, $temp_cd);

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //listデータ作成
        $this->makeList($db, $arg, $model);

        $cd_change = false;                                                                               

        if ($temp_cd==$Row["SUBCLASSCD"]) {
            $cd_change = true;
        }

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "class") {
            if (VARS::get("cmd") != "edit" && ($cd_change==true || $model->isload != 1)){
                $arg["reload"]  = "window.open('knje030index.php?cmd=list&SCHREGNO=$model->knje030schreg','left_frame');";
            }
        }

        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje030Form2.html", $arg);
    }

    //表示データ作成
    function makeSelectData($db, &$model, $subclasscd) {
        $Row = array();
        $Row["SCHREGNO"]            = "";
        $Row["YEAR"]                = "";
        $Row["SUBCLASSCD"]          = "";
        $Row["SUBCLASSNAME"]        = "";
        $Row["SUBCLASSABBV"]        = "";
        $Row["SUBCLASSNAME_ENG"]    = "";
        $Row["SUBCLASSABBV_ENG"]    = "";
        //転入生成績データ
        $query = knje030Query::getRow($subclasscd, $model->knje030year, $model->knje030schreg);
        $result = $db->query($query);
        $checksubcd = "";
        while($Mid = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($Mid["CHECKSUBCD"]) {
                $checksubcd = "_M";
            }
            $Row["SCHREGNO"]          = $Mid["SCHREGNO"];
            $Row["YEAR"]              = $Mid["YEAR"];
            $Row["SUBCLASSCD"]        = $Mid["SUBCLASSCD"];
            $Row["SUBCLASSNAME"]      = $Mid["SUBCLASSNAME".$checksubcd];
            $Row["SUBCLASSABBV"]      = $Mid["SUBCLASSABBV".$checksubcd];
            $Row["SUBCLASSNAME_ENG"]      = $Mid["SUBCLASSNAME_ENG".$checksubcd];
            $Row["SUBCLASSABBV_ENG"]      = $Mid["SUBCLASSABBV_ENG".$checksubcd];
            $Row[$Mid["ANNUAL"]]["YEAR"]        = $Mid["YEAR"];
            $Row[$Mid["ANNUAL"]]["ANNUAL"]      = $Mid["ANNUAL"];
            $Row[$Mid["ANNUAL"]]["VALUATION"]   = $Mid["VALUATION"];
            $Row[$Mid["ANNUAL"]]["GET_CREDIT"]  = $Mid["GET_CREDIT"];
            $Row[$Mid["ANNUAL"]]["COMP_CREDIT"] = $Mid["COMP_CREDIT"];
            $Row[$Mid["ANNUAL"]]["REMARK"]      = $Mid["REMARK"];
            $Row[$Mid["ANNUAL"]]["UPDATED"] = $Mid["UPDATED"];
            if ($model->cmd == "class") {
                $Row["YEAR"]              = $model->field["YEAR"];
                $Row["ANNUAL"]            = $model->field["ANNUAL"];
                $Row["REMARK"]            = "";
                $Row["SUBCLASSCD"]        = "";
                $Row["SUBCLASSNAME"]      = "";
                $Row["SUBCLASSABBV"]      = "";
                $Row["SUBCLASSNAME_ENG"]  = "";
                $Row["SUBCLASSABBV_ENG"]  = "";
                $Row[$Mid["ANNUAL"]]["YEAR"]        = "";
                $Row[$Mid["ANNUAL"]]["ANNUAL"]      = "";
                $Row[$Mid["ANNUAL"]]["VALUATION"]   = "";
                $Row[$Mid["ANNUAL"]]["GET_CREDIT"]  = "";
                $Row[$Mid["ANNUAL"]]["COMP_CREDIT"] = "";
                $Row[$Mid["ANNUAL"]]["REMARK"]      = "";
                $Row[$Mid["ANNUAL"]]["UPDATED"]     = "";
            }
        }
        return $Row;
    }

    //教科作成
    function makeClass(&$objForm, $db, &$arg, &$model) {
        $show = array();
        //教科取得
        $query = knje030Query::getClass($model);
        $result = $db->query($query);
        $opt_c = array();
        $i = 0;
        while($row_c = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_c[] = array("label" => htmlspecialchars($row_c["LABEL"]), 
                             "value" => $row_c["VALUE"]);

            if ($model->class_select=="" && $i==0){

                $class_chk = $row_c["VALUE"];
                $show["CLASSNAME"]        = $row_c["CLASSNAME"];
                $show["CLASSABBV"]        = $row_c["CLASSABBV"];
                $show["CLASSNAME_ENG"] = $row_c["CLASSNAME_ENG"];
                $show["CLASSABBV_ENG"] = $row_c["CLASSABBV_ENG"];
                $i++;

            }elseif($model->class_select==$row_c["VALUE"]){

                $class_chk = $row_c["VALUE"];
                $show["CLASSNAME"]        = $row_c["CLASSNAME"];
                $show["CLASSABBV"]        = $row_c["CLASSABBV"];
                $show["CLASSNAME_ENG"] = $row_c["CLASSNAME_ENG"];
                $show["CLASSABBV_ENG"] = $row_c["CLASSABBV_ENG"];

            }

        }

        //教科
        $arg["data"]["CLASSCD"] = $this->createCombo($objForm, "CLASSCD", $class_chk, $opt_c, "onchange=\"btn_submit('class');\"", 1);
        $arg["data"]["CLASSABBV"]     = $show["CLASSABBV"];
        $arg["data"]["CLASSNAME_ENG"] = $show["CLASSNAME_ENG"];
        $arg["data"]["CLASSABBV_ENG"] = $show["CLASSABBV_ENG"];

        //教科名
        $objForm->ae($this->createHiddenAe("CLASSNAME", $show["CLASSNAME"]));
        //教科名略称
        $objForm->ae($this->createHiddenAe("CLASSABBV", $show["CLASSABBV"]));
        //教科名英字
        $objForm->ae($this->createHiddenAe("CLASSNAME_ENG", $show["CLASSNAME_ENG"]));
        //教科名略称英字
        $objForm->ae($this->createHiddenAe("CLASSABBV_ENG", $show["CLASSABBV_ENG"]));

        return $class_chk;
    }

    //科目作成
    function makeSubclass(&$objForm, $db, &$arg, $class_chk, $Row, $model) {
        $query = knje030Query::getSublass($class_chk, $model);
        $result = $db->query($query);
        $opt_s = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($model->Properties["useCurriculumcd"] == '1') {
                $label = htmlspecialchars($row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"].":".$row["SUBCLASSNAME"]);
            } else {
                $label = htmlspecialchars($row["SUBCLASSCD"].":".$row["SUBCLASSNAME"]);
            }
            $opt_s[] = array("label" => $label, 
                             "value" => $row["SUBCLASSCD"].":".$row["SUBCLASSABBV"].":".$row["SUBCLASSNAME_ENG"].":".$row["SUBCLASSABBV_ENG"]);
        }
        $result->free();
        
        //科目
        $arg["data"]["SUBCLASS"] = $this->createCombo($objForm, "SUBCLASS", "", $opt_s, "", 1);

        //科目コード
        $arg["data"]["SUBCLASSCD"] = $this->createText($objForm, "SUBCLASSCD", substr($Row["SUBCLASSCD"],2,6), "onblur=\"return check(this)\"", 8, 4);

        //科目名
        $arg["data"]["SUBCLASSNAME"] = $this->createText($objForm, "SUBCLASSNAME", $Row["SUBCLASSNAME"], "", 40, 60);

        //科目略称
        $arg["data"]["SUBCLASSABBV"] = $this->createText($objForm, "SUBCLASSABBV", $Row["SUBCLASSABBV"], "", 5, 9);

        //英字科目名
        $extra = "onblur=\"return moji_hantei(this)\" STYLE=\"ime-mode:disabled\"";
        $arg["data"]["SUBCLASSNAME_ENG"] = $this->createText($objForm, "SUBCLASSNAME_ENG", $Row["SUBCLASSNAME_ENG"], $extra, 40, 40);

        //英字略称
        $arg["data"]["SUBCLASSABBV_ENG"] = $this->createText($objForm, "SUBCLASSABBV_ENG", $Row["SUBCLASSABBV_ENG"], $extra, 20, 20);

        //科目の読込ボタンを作成する
        $arg["button"]["btn_sub"] = $this->createBtn($objForm, "btn_sub", "＜", "onclick=\"return add('');\"");
    }

    //評定・単位作成
    function makeSetData(&$objForm, $db, &$arg, $model, $Row) {
        //学年数にあわせてrowspan値を変化
        $arg["span"]=(int)$model->grade_range + 2;

        $list_d = array();
        for($i = 0; $i <= $model->grade_range; $i++){
            //学年
            $grade = sprintf("%02d", $i);
            $Row_d["ANNUAL"] =  $i;

            if(!in_array($grade,array_keys($Row))){
                $Row[$grade]["YEAR"]        = "";
                $Row[$grade]["VALUATION"]   = "";
                $Row[$grade]["GET_CREDIT"]  = "";
                $Row[$grade]["COMP_CREDIT"] = "";
                $Row[$grade]["REMARK"]      = "";
                $Row[$grade]["UPDATED"]     = "";
            }

            //署名チェック
            $extraCheck = "";
            if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
                $query = knje030Query::getOpinionsWk($Row[$grade]["YEAR"], $Row["SCHREGNO"]);
                $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($check["CHAGE_OPI_SEQ"]) {
                    $extraCheck = " readonly=\"readonly\" STYLE=\"background-color:silver\"";
                }
            }

            //学年
            $extra = "onblur=\"return check(this)\" STYLE=\"text-align: right\"" .$extraCheck;
            $Row_d["YEAR_D"] = $this->createText($objForm, "YEAR".$grade, $Row[$grade]["YEAR"], $extra, 4, 4);

            //評価
            $Row_d["VALUATION"] = $this->createText($objForm, "VALUATION".$grade, $Row[$grade]["VALUATION"], $extra, 2, 2);

            //修得単位
            $Row_d["GET_CREDIT"] = $this->createText($objForm, "GET_CREDIT".$grade, $Row[$grade]["GET_CREDIT"], $extra, 2, 2);

            //履修単位
            $Row_d["COMP_CREDIT"] = $this->createText($objForm, "COMP_CREDIT".$grade, $Row[$grade]["COMP_CREDIT"], $extra, 2, 2);

            //備考
            $Row_d["REMARK"] = $this->createText($objForm, "REMARK".$grade, $Row[$grade]["REMARK"], $extraCheck, 60, 90);

            //hiddenを作成する
            $objForm->ae($this->createHiddenAe("UPDATED".$grade, $Row[$grade]["UPDATED"]));

            $arg["list_d"][] = $Row_d;
        }
    }

    //listデータ作成
    function makeList($db, &$arg, $model) {
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }        
        $query = knje030Query::Record($model,$model->knje030schreg,CTRL_YEAR);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $arg["list"][]=$row;

        }
    }

    //Hidden作成
    function makeHidden(&$objForm, $model, $Row, $temp_cd) {

        //学籍番号
        $objForm->ae($this->createHiddenAe("SCHREGNO", $model->knje030schreg));
        //年度
        $objForm->ae($this->createHiddenAe("YEAR", $Row["YEAR"]));
        //コマンド
        $objForm->ae($this->createHiddenAe("cmd"));
        //temp_cd
        $objForm->ae($this->createHiddenAe("temp_cd", $temp_cd));
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
