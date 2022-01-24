<?php

require_once('for_php7.php');

class knja110aForm2
{
    public function main(&$model)
    {
        $objForm       = new form();
        $arg["reload"] = "";
        $schoolName = $model->school_name;

        if (isset($model->schregno) && !isset($model->warning) && ($model->cmd != "subForm")) {
            $Row = knja110aQuery::getStudentData($model->schregno, $model);
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

        if ($schoolName == "koma") {
            $arg["classify_layout"] = "1";
        } else {
            $arg["testdiv_layout"] = "1";
            if ($schoolName == "meikei") {
                $arg["examtype_layout"] = "1";
            }
        }

        //特別支援学校またはFI複式クラス対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $arg["useSpecial_Support_Hrclass"] = $model->Properties["useSpecial_Support_Hrclass"];
        } elseif ($model->Properties["useFi_Hrclass"] == '1') {
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
            $result = $db->query(knja110aQuery::getGhrCd($model));
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
        } elseif ($model->Properties["useFi_Hrclass"] == '1') {
            //複式年組コンボボックス
            $opt = array();
            $opt[] = array("label" => "", "value" => "");
            $result = $db->query(knja110aQuery::getFiGradeHrclass($model));
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

        //オンラインリンクを張る
        if ($model->Properties["useOnlineLink"] === '1') {
            $arg["useOnlineLink"] = '1';
            //フォルダ名取得 dirnameで取れる値→/usr/local/development/src/K/KNJKOPEN
            $dirFullArray = preg_split("/\//", dirname(__FILE__));
            $foldName = $dirFullArray[get_count($dirFullArray) - 1];

            $arg["data"]["PDF_URL1"] = "../../sousaManual/".$foldName."_1.pdf";
            $arg["data"]["PDF_URL2"] = "../../sousaManual/".$foldName."_2.pdf";
        }
        //出席番号
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ATTENDNO"] = knjCreateTextBox($objForm, $Row["ATTENDNO"], "ATTENDNO", 3, 3, $extra);

        //更新する内容があった場合に日付を入力させるポップアップ
        if ($model->cmd == "subForm") {
            $arg["reload"] = " loadCheck('".REQUESTROOT."')";
        }

        //事前処理チェック
        if (!knja110aQuery::checktoStart($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        //年組コンボボックス
        $result = $db->query(knja110aQuery::getGrdClasQuery($model));
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
        $result = $db->query(knja110aQuery::getCourseSubject());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_coursecd[] = array('label' => str_replace(",", "", $row["COURSEMAJORCD"])."  ".htmlspecialchars($row["COURSE_SUBJECT"]),
                                    'value' => $row["COURSEMAJORCD"]);

            if ($value == $row["COURSEMAJORCD"]) {
                $value_flg = true;
            }
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
        $result = $db->query(knja110aQuery::getCourseCode());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_course3[] = array('label'  => $row["COURSECODE"]."  ".htmlspecialchars($row["COURSECODENAME"]),
                                    'value' => $row["COURSECODE"]);

            if ($value == $row["COURSECODE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $value = ($value != "" && $value_flg) ? $value : $opt_course3[0]["value"];
        $extra = "";
        $arg["data"]["COURSECODE"] = knjCreateCombo($objForm, "COURSECODE", $value, $opt_course3, $extra, 1);

        //学籍番号
        $extra = "onBlur=\"checkSchregno(this);\"";
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, $Row["SCHREGNO"], "SCHREGNO", 10, 8, $extra);

        //学籍番号を自動付番する
        if ($model->Properties["useAutoNumbering"] == '1') {
            //MAX学籍番号取得
            $max_schregno = $db->getOne(knja110aQuery::getMaxSchregno());

            //新規ボタン
            $extra  = "onclick=\"auto_numbering(this, '{$max_schregno}');\"";
            $extra .= ($Row["SCHREGNO"]) ? " disabled" : "";
            $arg["btn_numbering"] = knjCreateBtn($objForm, "btn_numbering", "新 規", $extra);
        }

        //内外区分
        $arg["data"]["INOUTCD"] = $model->createCombo($objForm, $db, "A001", "INOUTCD", $Row["INOUTCD"], 1);

        //氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 50, 80, $extra);
        $model->name = $Row["NAME"];
        $arg["data"]["NAMESLEN"] = $model->nameSLen;

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
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "");

        //性別
        $arg["data"]["SEX"] = $model->createCombo($objForm, $db, "Z002", "SEX", $Row["SEX"], 1);

        //血液型(型)
        $extra = "";
        $arg["data"]["BLOODTYPE"] = knjCreateTextBox($objForm, $Row["BLOODTYPE"], "BLOODTYPE", 3, 2, $extra);

        //血液型(RH型)
        $extra = "";
        $arg["data"]["BLOOD_RH"] = knjCreateTextBox($objForm, $Row["BLOOD_RH"], "BLOOD_RH", 1, 1, $extra);

        //その他
        $arg["data"]["HANDICAP"] = $model->createCombo($objForm, $db, "A025", "HANDICAP", $Row["HANDICAP"], 1);
        //その他 項目名
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $handicapTitle = "教育区分";
        } else {
            $handicapTitle = "その他";
        }
        $arg["data"]["HANDICAP_TITLE"] = $handicapTitle;

        //国籍
        $arg["data"]["NATIONALITY"] = $model->createCombo($objForm, $db, "A024", "NATIONALITY", $Row["NATIONALITY"], 1);

        //国籍２
        $extra = "style=\"width:100px\" onclick=\"loadwindow('knja110aindex.php?cmd=nationality2',0,0,900,280)\"";
        $arg["data"]["button_nationality2"] = knjCreateBtn($objForm, "button_nationality2", "第二国籍", $extra);

        //出身
        if ($model->Properties["Origin_hyouji"] == "1") {
            $arg["data"]["ORIGIN"] = $model->createCombo($objForm, $db, "A053", "ORIGIN", $Row["ORIGIN"], 1);
        }

        //出身中学校
        if ($model->Properties["use_finSchool_teNyuryoku_{$model->schoolKind}"] == "1") {
            $extra = "";
            //特別支援学校対応
            if ($model->Properties["useSpecial_Support_School"] == "1") {
                $extra = "style=\"overflow-y: scroll;\"";
            }
            $arg["data"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = knjCreateTextArea($objForm, "NYUGAKUMAE_SYUSSIN_JOUHOU", "4", "51", "soft", $extra, $Row["NYUGAKUMAE_SYUSSIN_JOUHOU"]);

            $nyugakumaeSyussinJouhouTitle = "出身学校";
            if ($model->schoolKind == "K") {
                //特別支援幼稚部
                if ($model->Properties["useSpecial_Support_School"] == "1") {
                    $nyugakumaeSyussinJouhouTitle = "入学前の状況";
                //幼稚部
                } else {
                    $nyugakumaeSyussinJouhouTitle = "入園前の状況";
                }
            }
            //出身中学校 項目名
            $arg["data"]["NYUGAKUMAE_SYUSSIN_JOUHOU_TITLE"] = $nyugakumaeSyussinJouhouTitle;
        } else {
            $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
            $arg["button"]["btn_searchfs"] = knjCreateBtn($objForm, "btn_searchfs", "検 索", $extra);
            $extra = "";
            $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->finschoolcdKeta, $model->finschoolcdKeta, $extra);
            $finschoolname = $db->getOne(knja110aQuery::getFinschoolName($Row["FINSCHOOLCD"]));
            $arg["data"]["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : $finschoolname;

            //出身中学校 卒業年月日
            $arg["data"]["FINISH_DATE"] = View::popUpCalendar($objForm, "FINISH_DATE", str_replace("-", "/", $Row["FINISH_DATE"]), "");
        }

        //入学
        $query = knja110aQuery::getComeBackT();
        $isComeBack = $db->getOne($query) > 0 ? true : false;
        if ($isComeBack) {
            $query = knja110aQuery::getCBEntDate($model);
            $comeBackEntDate = $db->getOne($query);
            if ($comeBackEntDate) {
                $arg["data"]["CB_ENT_DATE"] = str_replace("-", "/", $comeBackEntDate);
            }
        }

        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE", str_replace("-", "/", $Row["ENT_DATE"]), "");
        //課程入学年度
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["CURRICULUM_YEAR"] = knjCreateTextBox($objForm, $Row["CURRICULUM_YEAR"], "CURRICULUM_YEAR", 4, 4, $extra);
        //入学区分
        $arg["data"]["ENT_DIV"] = $model->createCombo($objForm, $db, "A002", "ENT_DIV", $Row["ENT_DIV"], 1);
        //受験番号
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $examLen = $model->Properties["examnoLen"] ? $model->Properties["examnoLen"] : "5";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO", $examLen, $examLen, $extra);

        if ($schoolName == "koma") {
            //類別
            $arg["data"]["CLASSIFY_NAME"] = $Row["CLASSIFY_NAME"];
        } elseif ($schoolName == "meikei") {
            //入試区分
            $query = knja110aQuery::getNameMst("A056");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "TESTDIV", $Row["TESTDIV"], $extra, 1, "BLANK");

            $query = knja110aQuery::getNameMst("A057");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $Row["EXAM_TYPE"], $extra, 1, "BLANK");
        } else {
            //入試区分
            $arg["data"]["TESTDIV"] = $Row["TESTDIV"];
        }

        //事由
        $extra = "";
        $arg["data"]["ENT_REASON"] = knjCreateTextBox($objForm, $Row["ENT_REASON"], "ENT_REASON", 70, 120, $extra);

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
        } elseif ($model->schoolKind == "H") {
            $arg["hyoujiFieldCourse"] = "1";
            $arg["addrSpan"] = "4";
        } else {
            $arg["addrSpan"] = "3";
        }

        //学校住所2
        $extra = "";
        $arg["data"]["ENT_ADDR2"] = knjCreateTextBox($objForm, $Row["ENT_ADDR2"], "ENT_ADDR2", 45, 90, $extra);

        //卒業
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE", str_replace("-", "/", $Row["GRD_DATE"]), "");
        $arg["data"]["GRD_DIV"] = $model->createCombo($objForm, $db, "A003", "GRD_DIV", $Row["GRD_DIV"], 1);
        if ($model->schoolKind != "H") {
            $arg["data"]["TENGAKU_SAKI_ZENJITU"] = View::popUpCalendar($objForm, "TENGAKU_SAKI_ZENJITU", str_replace("-", "/", $Row["TENGAKU_SAKI_ZENJITU"]), "");
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

        //塾
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["PRISCHOOLCD"] = knjCreateTextBox($objForm, $Row["PRISCHOOLCD"], "PRISCHOOLCD", 7, 7, $extra);

        //教室コード
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["PRISCHOOL_CLASS_CD"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_CLASS_CD"], "PRISCHOOL_CLASS_CD", 7, 7, $extra);

        //かな検索ボタン（塾）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=&pricdname=&priname=&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "検 索", $extra);

        $query = knja110aQuery::getPriSchoolName($Row["PRISCHOOLCD"]);
        $setFin = $db->getOne($query);
        $arg["data"]["PRISCHOOL_NAME"] = $setFin;

        $query = knja110aQuery::getPriSchoolClassName($Row["PRISCHOOLCD"], $Row["PRISCHOOL_CLASS_CD"]);
        $setFin = $db->getOne($query);
        $arg["data"]["PRISCHOOL_CLASS_NAME"] = $setFin;

        //備考1
        $extra = "";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 100, 50, $extra);

        //備考追加ボタン
        if ($model->Properties["useBaseRemarkFlg"] == '1') {
            $arg["useBaseRemarkFlg"] = "1";
            $link = REQUESTROOT."/A/KNJA110A_REMARK/knja110a_remarkindex.php?mode=1&cmd=subform3&SEND_PRGID=KNJD110A&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR=".CTRL_YEAR."&EXP_SEMESTER=".CTRL_SEMESTER."&GRADE={$model->radeClass}&NAME={$model->name}";
            $extra = "onclick=\"Page_jumper('{$link}');\"";
            $arg["button"]["btn_base_remark"] = knjCreateBtn($objForm, "btn_base_remark", "備考追加", $extra);
        }

        //備考2
        $extra = "";
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $Row["REMARK2"], "REMARK2", 100, 50, $extra);

        //備考3
        $extra = "";
        $arg["data"]["REMARK3"] = knjCreateTextBox($objForm, $Row["REMARK3"], "REMARK3", 100, 50, $extra);

        //検索
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/W/SEARCH_SCHOOL/knjwschool_searchindex.php?cmd=&targetname=ENT', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 320)\"";
        $arg["button"]["btn_search_ent"] = knjCreateBtn($objForm, "btn_search_ent", "検 索", $extra);

        //検索
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/W/SEARCH_SCHOOL/knjwschool_searchindex.php?cmd=&targetname=GRD', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 320)\"";
        $arg["button"]["btn_search_grd"] = knjCreateBtn($objForm, "btn_search_grd", "検 索", $extra);

        //一括更新
        $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=replace";
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);

        //履歴入力ボタン
        $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=rireki";
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "生徒履歴修正", $extra);

        //入学卒業履歴入力ボタン
        $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=entGrdRireki";
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
        $extra .= "&CALLID=KNJA110A";
        $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $disabled = ($model->schregno == "") ? " disabled": "";
        $arg["button"]["btn_f323"] = knjCreateBtn($objForm, "btn_f323", "生活管理情報", $extra.$disabled);

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

        knjCreateHidden($objForm, "NATIONALITY2_FLG", $model->detail011["NATIONALITY2_FLG"]);
        knjCreateHidden($objForm, "NATIONALITY_NAME_FLG", $model->detail011["NATIONALITY_NAME_FLG"]);
        knjCreateHidden($objForm, "NATIONALITY_NAME_KANA_FLG", $model->detail011["NATIONALITY_NAME_KANA_FLG"]);
        knjCreateHidden($objForm, "NATIONALITY_NAME_ENG_FLG", $model->detail011["NATIONALITY_NAME_ENG_FLG"]);
        knjCreateHidden($objForm, "NATIONALITY_REAL_NAME_FLG", $model->detail011["NATIONALITY_REAL_NAME_FLG"]);
        knjCreateHidden($objForm, "NATIONALITY_REAL_NAME_KANA_FLG", $model->detail011["NATIONALITY_REAL_NAME_KANA_FLG"]);

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

        //第二国籍
        knjCreateHidden($objForm, "NATIONALITY2");
        knjCreateHidden($objForm, "NATIONALITY_NAME");
        knjCreateHidden($objForm, "NATIONALITY_NAME_KANA");
        knjCreateHidden($objForm, "NATIONALITY_NAME_ENG");
        knjCreateHidden($objForm, "NATIONALITY_REAL_NAME");
        knjCreateHidden($objForm, "NATIONALITY_REAL_NAME_KANA");

        knjCreateHidden($objForm, "useAutoNumbering", $model->Properties["useAutoNumbering"]);

        $arg["start"]  = $objForm->get_start("edit", "POST", "knja110aindex.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();
        if ($model->cmd == "subEdit") {
            $arg["reload"]  = "parent.left_frame.location.href=parent.left_frame.location.href;";
        }

        View::toHTML5($model, "knja110aForm2.html", $arg);
    }

    public function updateNext2(&$model, &$objForm, $btn = 'btn_update')
    {
        //更新ボタン
        $objForm->ae(array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"top.main_frame.left_frame.updateNext(self, 'pre','".$btn ."');\""));

        //更新ボタン
        $objForm->ae(array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"top.main_frame.left_frame.updateNext(self, 'next','".$btn ."');\""));
        if ($model->cmd != "subForm") {
            if ($_POST["_ORDER"] == "pre" || $_POST["_ORDER"] == "next") {
                $order = $_POST["_ORDER"];
                if (!isset($model->warning)) {
                    echo <<<EOP
                       <script language="javascript">
                           top.main_frame.left_frame.nextLink('$order');
                       </script>
EOP;
                    unset($model->message);
                    exit;
                }
            }
            knjCreateHidden($objForm, "_ORDER");
        } else {
            knjCreateHidden($objForm, "_ORDER", $_POST["_ORDER"]);
        }
        return $objForm->ge("btn_up_pre") .$objForm->ge("btn_up_next");
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
