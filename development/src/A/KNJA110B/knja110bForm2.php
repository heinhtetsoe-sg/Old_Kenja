<?php

require_once('for_php7.php');

class knja110bForm2
{
    function main(&$model)
    {
        $objForm       = new form;
        $arg["reload"] = "";

        if (isset($model->schregno) && !isset($model->warning) && ($model->cmd != "subForm")) {
            $Row = knja110bQuery::getStudent_data($model->schregno, $model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->schoolKind == "K") {
            $arg["KIND_K"] = "1";
        } else {
            $arg["UNKIND_K"] = "1";
        }

        //特別支援学校またはFI複式クラス対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $arg["useSpecial_Support_Hrclass"] = $model->Properties["useSpecial_Support_Hrclass"];
        } else if ($model->Properties["useFi_Hrclass"] == '1') {
            $arg["useFi_Hrclass"] = $model->Properties["useFi_Hrclass"];
        } else {
            $arg["useSpecial_Support_Hrclass"] = "";
            $arg["useFi_Hrclass"] = "";
        }

        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            //複式年組コンボボックス
            $opt_ghr_cd = array();
            $opt_ghr_cd[] = array("label" => "", "value" => "");
            $result = $db->query(knja110bQuery::getGhrCd($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                 $opt_ghr_cd[] = array("label" => htmlspecialchars($row["GHR_NAME"]),"value" => $row["GHR_CD"]);
            }
            //左リストの年組とあわせる
            if ($model->ghr_cd=="" && $model->chargeclass_flg == 1) {
                $model->ghr_cd = $opt_ghr_cd[0]["value"];
            }
            $ghr_cd = ($Row["GHR_CD"]=="") ? $model->ghr_cd : $Row["GHR_CD"];
            $extra = "";
            $arg["data"]["GHR_CD"] = knjCreateCombo($objForm, "GHR_CD", $ghr_cd, $opt_ghr_cd, $extra, 1);

            //複式出席番号
            $extra = "onblur=\"this.value=toInteger(this.value)\";";
            $arg["data"]["GHR_ATTENDNO"] = knjCreateTextBox($objForm, $Row["GHR_ATTENDNO"], "GHR_ATTENDNO", 3, 3, $extra);

            //訪問生チェックボックス
            $extra  = ($Row["VISITOR"] == "1") ? "checked" : "";
            $extra .= " id=\"VISITOR\"";
            $arg["data"]["VISITOR"] = knjCreateCheckBox($objForm, "VISITOR", "1", $extra, "");
        //FI複式クラスを使うためのプロパティ
        } else if ($model->Properties["useFi_Hrclass"] == '1') {
            //複式年組コンボボックス
            $opt = array();
            $opt[] = array("label" => "", "value" => "");
            $result = $db->query(knja110bQuery::getFiGradeHrclass($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                 $opt[] = array("label" => htmlspecialchars($row["FI_HR_CLASS_NAME"]),"value" => $row["FI_GRADE_HR_CLASS"]);
            }
            //左リストの年組とあわせる
            if ($model->fi_grade_hr_class=="" && $model->chargeclass_flg == 1) {
                $model->fi_grade_hr_class = $opt[0]["value"];
            }
            $fi_grade_hr_class = ($Row["FI_GRADE_HR_CLASS"]=="") ? $model->fi_grade_hr_class : $Row["FI_GRADE_HR_CLASS"];
            $extra = "";
            $arg["data"]["FI_GRADE_HR_CLASS"] = knjCreateCombo($objForm, "FI_GRADE_HR_CLASS", $fi_grade_hr_class, $opt, $extra, 1);

            //複式出席番号
            $extra = "onblur=\"this.value=toInteger(this.value)\";";
            $arg["data"]["FI_ATTENDNO"] = knjCreateTextBox($objForm, $Row["FI_ATTENDNO"], "FI_ATTENDNO", 3, 3, $extra);
        }

        //出席番号
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ATTENDNO"] = knjCreateTextBox($objForm, $Row["ATTENDNO"], "ATTENDNO", 3, 3, $extra);

        //更新する内容があった場合に日付を入力させるポップアップ
        if ($model->cmd == "subForm") {
            $arg["reload"] = " loadCheck('".REQUESTROOT."')";
        }

        //事前処理チェック
        if (!knja110bQuery::ChecktoStart($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        //年組コンボボックス
        $result = $db->query(knja110bQuery::getGrd_ClasQuery($model));
        $opt_grcl = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             $opt_grcl[] = array("label" => htmlspecialchars($row["HR_NAME"]),"value" => $row["GC"]);
        }

        //左リストの年組とあわせる
        if ($model->GradeClass=="" && $model->chargeclass_flg == 1) {
            $model->GradeClass = $opt_grcl[0]["value"];
        }
        $grcl = ($Row["GRCL"]=="") ? $model->GradeClass : $Row["GRCL"];
        $extra = "";
        $arg["data"]["GRADE_CLASS"] = knjCreateCombo($objForm, "GRADE_CLASS", $grcl, $opt_grcl, $extra, 1);

        //年次
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ANNUAL"] = knjCreateTextBox($objForm, $Row["ANNUAL"], "ANNUAL", 2, 2, $extra);

        //課程学科
        $value_flg = false;
        $opt_coursecd = array();
        $opt_coursecd[] = array('label' => "", 'value' => "");
        $value = $Row["COURSEMAJORCD"];
        $result = $db->query(knja110bQuery::getCourse_Subject());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_coursecd[] = array('label' => str_replace(",","",$row["COURSEMAJORCD"])."  ".htmlspecialchars($row["COURSE_SUBJECT"]),
                                    'value' => $row["COURSEMAJORCD"]);

            if ($value == $row["COURSEMAJORCD"]) $value_flg = true;
        }
        $result->free();
        $value = ($value != "" && $value_flg) ? $value : $opt_coursecd[0]["value"];
        $extra = "";
        $arg["data"]["COURSEMAJORCD"] = knjCreateCombo($objForm, "COURSEMAJORCD", $value, $opt_coursecd, $extra, 1);

        //コース
        $value_flg = false;
        $opt_course3 = array();
        $opt_course3[] = array('label' => "", 'value' => "");
        $value = $Row["COURSECODE"];
        $result = $db->query(knja110bQuery::getCourseCode());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_course3[] = array('label'  => $row["COURSECODE"]."  ".htmlspecialchars($row["COURSECODENAME"]),
                                    'value' => $row["COURSECODE"]);

            if ($value == $row["COURSECODE"]) $value_flg = true;
        }
        $result->free();
        $value = ($value != "" && $value_flg) ? $value : $opt_course3[0]["value"];
        $extra = "";
        $arg["data"]["COURSECODE"] = knjCreateCombo($objForm, "COURSECODE", $value, $opt_course3, $extra, 1);

        //学籍番号
        $extra = "onblur=\"this.value=toAlphaNumber(this.value)\";";
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, $Row["SCHREGNO"], "SCHREGNO", 10, 8, $extra);

        //内外区分
        $arg["data"]["INOUTCD"] = $model->CreateCombo($objForm,$db,"A001","INOUTCD",$Row["INOUTCD"],1);

        //氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 50, 80, $extra);

        //表示用氏名
        $extra = "";
        $arg["data"]["NAME_SHOW"] = knjCreateTextBox($objForm, $Row["NAME_SHOW"], "NAME_SHOW", 20, 30, $extra);

        //氏名かな
        $extra = "";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 80, 160, $extra);

        //英字氏名
        $extra = "";
        $arg["data"]["NAME_ENG"] = knjCreateTextBox($objForm, $Row["NAME_ENG"], "NAME_ENG", 40, 40, $extra);

        //戸籍氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["REAL_NAME"] = knjCreateTextBox($objForm, $Row["REAL_NAME"], "REAL_NAME", 50, 80, $extra);

        //戸籍氏名かな
        $extra = "";
        $arg["data"]["REAL_NAME_KANA"] = knjCreateTextBox($objForm, $Row["REAL_NAME_KANA"], "REAL_NAME_KANA", 60, 160, $extra);

        //誕生日
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", str_replace("-","/",$Row["BIRTHDAY"]),"");

        //性別
        $arg["data"]["SEX"] = $model->CreateCombo($objForm,$db,"Z002","SEX",$Row["SEX"],1);

        //血液型(型)
        $extra = "";
        $arg["data"]["BLOODTYPE"] = knjCreateTextBox($objForm, $Row["BLOODTYPE"], "BLOODTYPE", 3, 2, $extra);

        //血液型(RH型)
        $extra = "";
        $arg["data"]["BLOOD_RH"] = knjCreateTextBox($objForm, $Row["BLOOD_RH"], "BLOOD_RH", 1, 1, $extra);

        //その他
        $arg["data"]["HANDICAP"] = $model->CreateCombo($objForm,$db,"A025","HANDICAP",$Row["HANDICAP"],1);

        //国籍
        $arg["data"]["NATIONALITY"] = $model->CreateCombo($objForm,$db,"A024","NATIONALITY",$Row["NATIONALITY"],1);

        //出身中学校
        if ($model->schoolKind == "P" || $model->schoolKind == "K") {
            $arg["data"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = knjCreateTextArea($objForm, "NYUGAKUMAE_SYUSSIN_JOUHOU", "4", "51", "soft", $extra, $Row["NYUGAKUMAE_SYUSSIN_JOUHOU"]);
        } else {
            $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
            $arg["button"]["btn_searchfs"] = knjCreateBtn($objForm, "btn_searchfs", "検 索", $extra);
            $extra = "";
            $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->finschoolcdKeta, $model->finschoolcdKeta, $extra);
            $finschoolname = $db->getOne(knja110bQuery::getFinschoolName($Row["FINSCHOOLCD"]));
            $arg["data"]["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : $finschoolname;

            //出身中学校 卒業年月日
            $arg["data"]["FINISH_DATE"] = View::popUpCalendar($objForm, "FINISH_DATE",str_replace("-","/",$Row["FINISH_DATE"]),"");
        }

        //入学
        $query = knja110bQuery::getComeBackT();
        $isComeBack = $db->getOne($query) > 0 ? true : false;
        if ($isComeBack) {
            $query = knja110bQuery::getCB_entDate($model);
            $comeBackEntDate = $db->getOne($query);
            if ($comeBackEntDate) {
                $arg["data"]["CB_ENT_DATE"] = str_replace("-", "/", $comeBackEntDate);
            }
        }

        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE",str_replace("-","/",$Row["ENT_DATE"]),"");
        //課程入学年度
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["CURRICULUM_YEAR"] = knjCreateTextBox($objForm, $Row["CURRICULUM_YEAR"], "CURRICULUM_YEAR", 4, 4, $extra);
        //入学区分
        $arg["data"]["ENT_DIV"] = $model->CreateCombo($objForm,$db,"A002","ENT_DIV",$Row["ENT_DIV"],1);
        //受験番号
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $examLen = $model->Properties["examnoLen"] ? $model->Properties["examnoLen"] : "5";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO", $examLen, $examLen, $extra);

        //事由
        $extra = "";
        $arg["data"]["ENT_REASON"] = knjCreateTextBox($objForm, $Row["ENT_REASON"], "ENT_REASON", 45, 75, $extra);

        //学校名
        $extra = "";
        $arg["data"]["ENT_SCHOOL"] = knjCreateTextBox($objForm, $Row["ENT_SCHOOL"], "ENT_SCHOOL", 45, 75, $extra);

        //学校住所1
        $extra = "";
        $arg["data"]["ENT_ADDR"] = knjCreateTextBox($objForm, $Row["ENT_ADDR"], "ENT_ADDR", 45, 90, $extra);

        //住所２使用(小学校、中学校)
        if ($model->Properties["useAddrField2"] == "1" && $model->schoolKind != "H") {
            $arg["useAddrField2"] = $model->Properties["useAddrField2"];
            $arg["addrSpan"] = "4";
        //高校(住所2と同一フィールド名だが、項目名が異なる)
        } else if ($model->schoolKind == "H") {
            $arg["hyoujiFieldCourse"] = "1";
            $arg["addrSpan"] = "4";
        } else {
            $arg["addrSpan"] = "3";
        }

        //学校住所2
        $extra = "";
        $arg["data"]["ENT_ADDR2"] = knjCreateTextBox($objForm, $Row["ENT_ADDR2"], "ENT_ADDR2", 45, 90, $extra);

        //卒業
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE",str_replace("-","/",$Row["GRD_DATE"]),"");
        $arg["data"]["GRD_DIV"] = $model->CreateCombo($objForm,$db,"A003","GRD_DIV",$Row["GRD_DIV"],1);
        if ($model->schoolKind != "H") {
            $arg["data"]["TENGAKU_SAKI_ZENJITU"] = View::popUpCalendar($objForm, "TENGAKU_SAKI_ZENJITU",str_replace("-","/",$Row["TENGAKU_SAKI_ZENJITU"]),"");
        }
        $arg["data"]["TENGAKU_SAKI_GRADE"] = knjCreateTextBox($objForm, $Row["TENGAKU_SAKI_GRADE"], "TENGAKU_SAKI_GRADE", 10, 15, $extra);

        //事由
        $extra = "";
        $arg["data"]["GRD_REASON"] = knjCreateTextBox($objForm, $Row["GRD_REASON"], "GRD_REASON", 45, 75, $extra);

        //学校名
        $extra = "";
        $arg["data"]["GRD_SCHOOL"] = knjCreateTextBox($objForm, $Row["GRD_SCHOOL"], "GRD_SCHOOL", 45, 75, $extra);

        //学校住所1
        $extra = "";
        $arg["data"]["GRD_ADDR"] = knjCreateTextBox($objForm, $Row["GRD_ADDR"], "GRD_ADDR", 45, 90, $extra);

        //学校住所2
        $extra = "";
        $arg["data"]["GRD_ADDR2"] = knjCreateTextBox($objForm, $Row["GRD_ADDR2"], "GRD_ADDR2", 45, 90, $extra);

        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control_data["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control_data["Extension"];

        if ($model->Properties["useJpgUpload"] == "1") {
            $arg["useJpgUpload"] = "1";
            //ファイルからの取り込み
            $arg["data"]["FILE"] = knjCreateFile($objForm, "FILE", "", 102400);
            //実行ボタン
            $extra = "onclick=\"return btn_submit('execute');\"";
            $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
        }
        if ($model->Properties["useDispUnDispPicture"] === '1') {
            $arg["unDispPicture"] = "1";
        } else {
            $arg["dispPicture"] = "1";
        }

        //出身塾
        $result = $db->query(knja110bQuery::getPrischoolName());
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array( "label"  => $row["PRISCHOOLCD"]."  ".htmlspecialchars($row["PRISCHOOL_NAME"]),
                            "value"  => $row["PRISCHOOLCD"]);
        }
        $extra = "style=width:\"35%\"";
        $arg["data"]["PRISCHOOLCD"] = knjCreateCombo($objForm, "PRISCHOOLCD", $Row["PRISCHOOLCD"], $opt, $extra, 1);

        //備考1
        $extra = "";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 50, 25, $extra);

        //備考2
        $extra = "";
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $Row["REMARK2"], "REMARK2", 50, 25, $extra);

        //備考3
        $extra = "";
        $arg["data"]["REMARK3"] = knjCreateTextBox($objForm, $Row["REMARK3"], "REMARK3", 50, 25, $extra);

        //検索
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/W/SEARCH_SCHOOL/knjwschool_searchindex.php?cmd=&targetname=ENT', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 320)\"";
        $arg["button"]["btn_search_ent"] = knjCreateBtn($objForm, "btn_search_ent", "検 索", $extra);

        //検索
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/W/SEARCH_SCHOOL/knjwschool_searchindex.php?cmd=&targetname=GRD', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 320)\"";
        $arg["button"]["btn_search_grd"] = knjCreateBtn($objForm, "btn_search_grd", "検 索", $extra);

        //一括更新
        $link = REQUESTROOT."/A/KNJA110B/knja110bindex.php?cmd=replace&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);

        //履歴入力ボタン
        $link = REQUESTROOT."/A/KNJA110B/knja110bindex.php?cmd=rireki&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "生徒履歴修正", $extra);

        //入学卒業履歴入力ボタン
        $link = REQUESTROOT."/A/KNJA110B/knja110bindex.php?cmd=entGrdRireki&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_entGrd_rireki"] = knjCreateBtn($objForm, "btn_entGrd_rireki", "入学卒業履歴修正", $extra);

        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = $this->updateNext2($model, $objForm, 'btn_update');

        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //保健
        list($grade, $hrClass) = preg_split("/,/", $grcl);
        $extra  = " onClick=\" wopen('".REQUESTROOT."/F/KNJF323/knjf323index.php?";
        $extra .= "SCHREGNO=".$model->schregno."&cmd=edit";
        $extra .= "&AUTH=".$model->auth;
        $extra .= "&NAME=".$Row["NAME"];
        $extra .= "&GRADE=".$grade;
        $extra .= "&YEAR=".CTRL_YEAR;
        $extra .= "&CALLID=KNJA110B";
        $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"";
        $arg["button"]["btn_f323"] = knjCreateBtn($objForm, "btn_f323", "生活管理情報", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "E_APPDATE");
        knjCreateHidden($objForm, "GRADE_FLG");
        knjCreateHidden($objForm, "HR_CLASS_FLG");
        knjCreateHidden($objForm, "ATTENDNO_FLG");
        knjCreateHidden($objForm, "ANNUAL_FLG");
        knjCreateHidden($objForm, "COURSECD_FLG");
        knjCreateHidden($objForm, "MAJORCD_FLG");
        knjCreateHidden($objForm, "COURSECODE_FLG");
        knjCreateHidden($objForm, "NAME_FLG");
        knjCreateHidden($objForm, "NAME_SHOW_FLG");
        knjCreateHidden($objForm, "NAME_KANA_FLG");
        knjCreateHidden($objForm, "NAME_ENG_FLG");
        knjCreateHidden($objForm, "REAL_NAME_FLG");
        knjCreateHidden($objForm, "REAL_NAME_KANA_FLG");
        knjCreateHidden($objForm, "HANDICAP_FLG");

        knjCreateHidden($objForm, "UPDATED1", $Row["UPDATED1"]);
        knjCreateHidden($objForm, "UPDATED2", $Row["UPDATED2"]);

        if ($model->cmd != "update") {
            $setHiddenVal = $model->subFrm;
            $setHiddenVal["GRADE_CLASS"] = $grcl;
        }
        knjCreateHidden($objForm, "CHECK_COURSEMAJORCD", $setHideenVal["COURSEMAJORCD"]);
        knjCreateHidden($objForm, "CHECK_GRADE_CLASS", $setHideenVal["GRADE_CLASS"]);
        knjCreateHidden($objForm, "CHECK_ATTENDNO", $setHideenVal["ATTENDNO"]);
        knjCreateHidden($objForm, "CHECK_ANNUAL", $setHideenVal["ANNUAL"]);
        knjCreateHidden($objForm, "CHECK_COURSECODE", $setHideenVal["COURSECODE"]);
        knjCreateHidden($objForm, "CHECK_NAME", $setHideenVal["NAME"]);
        knjCreateHidden($objForm, "CHECK_NAME_SHOW", $setHideenVal["NAME_SHOW"]);
        knjCreateHidden($objForm, "CHECK_NAME_KANA", $setHideenVal["NAME_KANA"]);
        knjCreateHidden($objForm, "CHECK_NAME_ENG", $setHideenVal["NAME_ENG"]);
        knjCreateHidden($objForm, "CHECK_REAL_NAME", $setHideenVal["REAL_NAME"]);
        knjCreateHidden($objForm, "CHECK_REAL_NAME_KANA", $setHideenVal["REAL_NAME_KANA"]);
        knjCreateHidden($objForm, "CHECK_HANDICAP", $setHideenVal["HANDICAP"]);

        knjCreateHidden($objForm, "CHK_GRD_SDATE", CTRL_YEAR."/04/01");
        knjCreateHidden($objForm, "CHK_GRD_EDATE", (CTRL_YEAR + 1)."/03/31");

        $arg["start"]  = $objForm->get_start("edit", "POST", "knja110bindex.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();
        if ($model->cmd == "subEdit"){
            $arg["reload"]  = "parent.left_frame.location.href=parent.left_frame.location.href;";
        }

        View::toHTML($model, "knja110bForm2.html", $arg);
    }

    function updateNext2(&$model, &$objForm, $btn='btn_update'){
        //更新ボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"top.main_frame.left_frame.updateNext(self, 'pre','".$btn ."');\""));

        //更新ボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"top.main_frame.left_frame.updateNext(self, 'next','".$btn ."');\""));
        if ($model->cmd != "subForm") {
            if ($_POST["_ORDER"] == "pre" || $_POST["_ORDER"] == "next" ){
               $order = $_POST["_ORDER"];
               if (!isset($model->warning)){
                   echo <<<EOP
                       <script language="javascript">
                           top.main_frame.left_frame.nextLink('$order');
                       </script>
EOP;
                    unset($model->message);
                    exit;
               }
            }
            $objForm->ae( array("type"  => "hidden",
                                "name"  => "_ORDER" ));
        } else {
            $objForm->ae( array("type"  => "hidden",
                                "name"  => "_ORDER",
                                "value" => $_POST["_ORDER"]));
        }
        return $objForm->ge("btn_up_pre") .$objForm->ge("btn_up_next");
    }
}
?>
