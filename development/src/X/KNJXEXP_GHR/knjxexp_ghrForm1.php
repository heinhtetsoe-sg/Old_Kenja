<?php

require_once('for_php7.php');

class knjxexp_ghrForm1
{
    function main(&$model){
/*       if ($model->usr_auth != DEF_UPDATABLE && $model->usr_auth != DEF_UPDATE_RESTRICT){
           $arg["jscript"] = "OnAuthError();";
       } */

        $objForm = new form;

        /* Add by Kaung for CurrentCursor 2019-01-03 start */
        $arg["TITLE"] = "左検索画面";
        echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        /* Add by Kaung for CurrentCursor 2019-01-31 end */ 
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");

        //配列をセット（年度コンボフラグ）
        $combo_flg = knjxexp_ghrQuery::getComboFlg();

        $db     = Query::dbCheckOut();
        //年度と学期
        if (!isset($model->exp_year)) $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;
        //年度コンボボックス使用時
        if ($combo_flg[$model->programid] == "1"){
            $opt_year = array();
            $result = $db->query(knjxexp_ghrQuery::getYearSemester("off"));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt_year[] = array("label" => $row["YEAR"] ."年度 " .$row["SEMESTERNAME"],
                                    "value" => $row["YEAR"]."-".$row["SEMESTER"]);
            }
            $objForm->ae( array("type"        => "select",
                                "name"        => "EXP_YEAR",
                                "size"        => "1",
                                "extrahtml"   => "Onchange=\"btn_submit('chg_year')\"",
                                "value"       => $model->exp_year,
                                "options"     => $opt_year ));

            $arg["EXP_YEAR"] = "&nbsp;年度：" .$objForm->ge("EXP_YEAR");
        //上記以外（通常）
        } else {
            $arg["EXP_YEAR"] = "&nbsp;年度：" .CTRL_YEAR ."&nbsp;&nbsp;学期：" .CTRL_SEMESTERNAME;
        }

        //特別支援学校またはFI複式クラス対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $arg["useSpecial_Support_Hrclass".$model->special_div] = "1";
        } else if ($model->Properties["useFi_Hrclass"] == '1') {
            $arg["useFi_Hrclass"] = $model->Properties["useFi_Hrclass"];
        } else {
            $arg["useSpecial_Support_Hrclass"] = "";
            $arg["useFi_Hrclass"] = "";
        }
        //hidden
        knjCreateHidden($objForm, "special_div", $model->special_div);
        knjCreateHidden($objForm, "schoolKind", $model->schoolKind);

        //卒業生検索以外
        if ($model->button[$model->programid] != 2){
            //特別支援学校対応
            //ラジオボタン表示の制御
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1' || $model->Properties["useFi_Hrclass"] == '1') {
                //コンボ切替のラジオボタン 1:年組 2:複式クラス(FI)
                $opt = array(1, 2);
                /* Edit by Kaung for CurrentCursor 2019-01-03 start */
                $extra = array("id=\"HUKUSIKI_RADIO1\" onClick=\"current_cursor('HUKUSIKI_RADIO1');btn_submit('chg_hukusiki_radio');\"", "id=\"HUKUSIKI_RADIO2\" onClick=\"current_cursor('HUKUSIKI_RADIO2');btn_submit('chg_hukusiki_radio');\"");
                $model->hukusiki_radio = $model->hukusiki_radio ? $model->hukusiki_radio : '1';
                $radioArray = knjCreateRadio($objForm, "HUKUSIKI_RADIO", $model->hukusiki_radio, $extra, $opt, get_count($opt));
                foreach($radioArray as $key => $val) $arg[$key] = $val;
                if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                    $arg["RADIO_NAME"] = "実クラス";
                } else if ($model->Properties["useFi_Hrclass"] == '1') {
                    $arg["RADIO_NAME"] = "FI複式";
                }
                //学年混合チェックボックス
                $extra  = ($model->grade_mix == "1") ? "checked" : "";
                $extra .= ($model->hukusiki_radio != "1") ? " disabled" : "";
                $extra .= " onClick=\"current_cursor('GRADE_MIX');return btn_submit('chg_hukusiki_radio');\"";
                /* Edit by Kaung for CurrentCursor 2019-01-31 end */
                $extra .= " id=\"GRADE_MIX\"";
                $arg["GRADE_MIX"] = knjCreateCheckBox($objForm, "GRADE_MIX", "1", $extra, "");
            }

            if ($model->cmd == "chg_hukusiki_radio") {
                $model->ghr_cd = "";
            }
            //特別支援学校対応
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->hukusiki_radio == "2") {
                //複式クラスコンボ
                $opt = array();
                if ($model->cmd == "search"){
                    $opt[] = array("label"  => '',
                                    "value" => '');
                }
                $result = $db->query(knjxexp_ghrQuery::getGhrCd($model));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $opt[] = array("label" => $row["GHR_NAME"],
                                   "value" => $row["GHR_CD"]);
                    if (!$model->ghr_cd) {
                        $model->ghr_cd = $row["GHR_CD"];
                    }
                }
                $objForm->ae( array("type"        => "select",
                                    "name"        => "GHR_CD",
                                    "size"        => "1",
                                    "extrahtml"   => "Onchange=\"current_cursor('GHR_CD');btn_submit('chg_ghr_cd');\" id=\"GHR_CD\" aria-label=\"実クラス\"",/* Edit by Kaung for PC-Talker 2019-01-31 end */
                                    "value"       => $model->ghr_cd,
                                    "options"     => $opt ));

                $arg["GHR_CD"] = "実クラス：" .$objForm->ge("GHR_CD");
            //特別支援学校対応 学年混合
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->hukusiki_radio == "1" && $model->grade_mix == "1") {
                //複式クラスコンボ
                $opt = array();
                if ($model->cmd == "search"){
                    $opt[] = array("label"  => '',
                                    "value" => '');
                }
                $result = $db->query(knjxexp_ghrQuery::getStaffHr($model));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array("label" => $row["LABEL"],
                                   "value" => $row["VALUE"]);
                    if (!$model->ghr_cd) {
                        $model->ghr_cd = $row["VALUE"];
                    }
                }
                $objForm->ae( array("type"        => "select",
                                    "name"        => "GHR_CD",
                                    "size"        => "1",
                                    "extrahtml"   => "Onchange=\"current_cursor('GHR_CD');btn_submit('chg_ghr_cd');\" id=\"GHR_CD\" aria-label=\"学年混合\"",/* Edit by Kaung for PC-Talker 2019-01-20 end */
                                    "value"       => $model->ghr_cd,
                                    "options"     => $opt ));

                $arg["GHR_CD"] = "学年混合：" .$objForm->ge("GHR_CD");
            //FI対応クラス対応
            } else if ($model->Properties["useFi_Hrclass"] == '1' && $model->hukusiki_radio == "2") {
                //複式クラスコンボ
                $opt = array();
                if ($model->cmd == "search"){
                    $opt[] = array("label"  => '',
                                    "value" => '');
                }
                $result = $db->query(knjxexp_ghrQuery::getFiGradeHrclass($model));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $opt[] = array("label" => $row["FI_HR_NAME"],
                                   "value" => $row["FI_GRADE_HR_CLASS"]);
                    if (!isset($model->fi_grade_hr_class)) $model->fi_grade_hr_class = $opt[0]["value"];
                }
                $extra = "Onchange=\"current_cursor('FI_GRADE_HR_CLASS');btn_submit('chg_fi_grade_hr_class')\" id=\"FI_GRADE_HR_CLASS\" aria-label=\"複式クラス\"";/* Edit by Kaung for PC-Talker 2019-01-31 end */
                $arg["FI_GRADE_HR_CLASS"] = knjCreateCombo($objForm, "FI_GRADE_HR_CLASS", $model->fi_grade_hr_class, $opt, $extra, 1);
            } else {
                //年組コンボ
                $opt = array();
                if ($model->cmd == "search"){
                    $opt[] = array("label"  => '',
                                    "value" => '');
                }
                $result = $db->query(knjxexp_ghrQuery::GetHr_Class($model));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $opt[] = array("label" => $row["HR_NAME"],
                                   "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
                    if (!isset($model->grade)) $model->grade = $row["GRADE"] ."-" .$row["HR_CLASS"];
                }
                $objForm->ae( array("type"        => "select",
                                    "name"        => "GRADE",
                                    "size"        => "1",
                                    "extrahtml"   => "Onchange=\"current_cursor('GRADE');btn_submit('chg_grade');\" id=\"GRADE\" aria-label=\"年組\"",/* Edit by Kaung for PC-Talker 2019-01-31 end */
                                    "value"       => $model->grade,
                                    "options"     => $opt ));

                $arg["GRADE"] = "年組：" .$objForm->ge("GRADE");
            }
        }
        if ($model->check[$model->programid]){
            $objForm->ae(array("type"      => "checkbox",
                                "name"      => "chk_all",
                                "extrahtml"   => "onClick=\"return check_all(this);\"" ));

            $arg["CHECK_ALL"] = $objForm->ge("chk_all");
        }
        if ($model->programid == "KNJA110"){ //学籍基礎データ入力（担任）
            $arg["INFO"] = true;
        } else if ($model->programid == "KNJA110A"){ //学籍基礎データ入力（担任）
            $arg["INFO"] = true;
        } else if ($model->programid == "KNJA110B"){ //学籍基礎データ入力（担任）（特別支援学校）
            $arg["INFO"] = true;
        }

        if ($model->Properties["KNJXEXP_GHR_SEARCH"] == "SCHREGNO") {
            $arg["SEARCH_B"] = "1";
        } else {
            $arg["SEARCH_A"] = "1";
        }

        //特別支援学校またはFI複式クラス対応
        //出席番号表示位置の制御
        if (($model->Properties["useSpecial_Support_Hrclass"] == '1' || $model->Properties["useFi_Hrclass"] == '1') && ($model->hukusiki_radio == "2" || ($model->hukusiki_radio == "1" && $model->grade_mix == "1"))) {
            if ($model->hukusiki_radio == "2") {
                $arg["SHOW_HUKUSIKI_ATTENDNO"] = "1";
            } else {
                $arg["SHOW_HUKUSIKI_ATTENDNO".$model->special_div] = "1";
            }
        } else {
            $arg["SHOW_ATTENDNO"] = "1";
        }

        //卒業生検索以外
        if ((($model->cmd == "list" || $model->cmd == "chg_hukusiki_radio") && $model->mode == "ungrd") || $model->cmd == "search") {
            //生徒表示
            if ($model->hukusiki_radio == "1" && $model->grade_mix == "1") {
                $result = $db->query(knjxexp_ghrQuery::getSchInfo($model));
            } else {
                $result = $db->query(knjxexp_ghrQuery::GetStudents($model));
            }
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            $schregno = array();
            list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
            list($exp_year, $exp_semester) = explode("-", $model->exp_year);

            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $schregno[] = $row["SCHREGNO"];
                $a = array("cmd"    => $cmd,
                          "SCHREGNO"    => $row["SCHREGNO"],
                          "mode"        => $model->mode,
                          "EXP_YEAR"    => $exp_year,
                          "EXP_SEMESTER"=> $exp_semester,
                          "GRADE"       => $row["GRADE"],
                          "HR_CLASS"    => $row["HR_CLASS"],
                          "ATTENDNO"    => $row["ATTENDNO"],
                          "NAME"        => $row["NAME_SHOW"]);
                if ($model->mode == "grd"){  //卒業生
                    $a = array_merge($a,array("GRD_YEAR"    => $row["GRD_YEAR"],
                                          "GRD_SEMESTER"    => $row["GRD_SEMESTER"],
                                          "GRD_GRADE"       => $row["GRD_GRADE"],
                                          "GRD_HR_CLASS"    => $row["GRD_HR_CLASS"],
                                          "GRD_ATTENDNO"    => $row["GRD_ATTENDNO"]));
                
                }
                if ($model->check[$model->programid]){
                    $objForm->ae(array("type"       => "checkbox",
                                       "name"       => "CHECKED",
                                       "multiple"   => true,
                                       "value"      => $row["SCHREGNO"],
                                       "extrahtml"  => "multiple" ));

                    $row["CHECKED"] = $objForm->ge("CHECKED");

                } else if ($model->programid == "KNJA110"){ //学籍基礎データ入力（担任）
                    $row["BASE"] = View::alink(REQUESTROOT ."/A/KNJA110/knja110index.php", "基", "target=right_frame onclick=\"Link(this)\"",$a);
                    unset($a["cmd"]);
                    $row["HOME"] = View::alink(REQUESTROOT ."/A/KNJA110_2/knja110_2index.php", "住", "target=right_frame onclick=\"Link(this)\"",$a);
                    $row["MOVE"] = View::alink(REQUESTROOT ."/A/KNJA110_3/knja110_3index.php", "異", "target=right_frame onclick=\"Link(this)\"",$a);
                } else if ($model->programid == "KNJA110A"){ //学籍基礎データ入力（担任）
                    $row["BASE"] = View::alink(REQUESTROOT ."/A/KNJA110A/knja110aindex.php", "基", "target=right_frame onclick=\"Link(this)\"",$a);
                    unset($a["cmd"]);
                    $row["HOME"] = View::alink(REQUESTROOT ."/A/KNJA110_2A/knja110_2aindex.php", "住", "target=right_frame onclick=\"Link(this)\"",$a);
                    $row["MOVE"] = View::alink(REQUESTROOT ."/A/KNJA110_3A/knja110_3aindex.php", "異", "target=right_frame onclick=\"Link(this)\"",$a);
                } else if ($model->programid == "KNJA110B"){ //学籍基礎データ入力（担任）（特別支援学校）
                    $row["BASE"] = View::alink(REQUESTROOT ."/A/KNJA110B/knja110bindex.php", "基", "target=right_frame onclick=\"Link(this)\"",$a);
                    unset($a["cmd"]);
                    $row["HOME"] = View::alink(REQUESTROOT ."/A/KNJA110_2B/knja110_2bindex.php", "住", "target=right_frame onclick=\"Link(this)\"",$a);
                    $row["MOVE"] = View::alink(REQUESTROOT ."/A/KNJA110_3B/knja110_3bindex.php", "異", "target=right_frame onclick=\"Link(this)\"",$a);
                }else{
                    $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"",$a);
                }
                $row["IMAGE"] = $image[($row["SEX"]-1)];
                $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
                //特別支援学校対応
                if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                    $row["GHR_ATTENDNO"] = ($row["GHR_ATTENDNO"] == "") ? "" : $row["GHR_NAMEABBV"]."-".$row["GHR_ATTENDNO"];
                //FI複式クラスを使うためのプロパティ
                } else if ($model->Properties["useFi_Hrclass"] == '1') {
                    $row["FI_ATTENDNO"] = ($row["FI_ATTENDNO"] == "") ? "" : $row["FI_HR_NAMEABBV"]."-".$row["FI_ATTENDNO"];
                }
                $arg["data"][] = $row;
                $i++;
            }
            if($i <= 0){
                $arg['result'] = "左検索画面の検索結果 結果はありません";
            } else {
                $arg['result'] = "左検索画面の検索結果";
            }
            $arg["CLASS_SUM"] = $i;
            $result->free();
        }
        Query::dbCheckIn($db);

        if ($model->usr_auth == DEF_UPDATABLE || $model->usr_auth == DEF_REFERABLE){
            //在籍または両方
            if ($model->button[$model->programid] == 1 || $model->button[$model->programid] == 3){
                //在ボタンを作成する
                $objForm->ae( array("type"	=> "button",
                                    "name"	=> "btn_ungrd",
                                    "value"	=> " 在 ",
                                    "extrahtml" => "onclick=\"showSearch('ungrd')\" id=\"btn_ungrd\""));

                $arg["btn_ungrd"] = $objForm->ge("btn_ungrd");
            }
            //卒業または両方
            if ($model->button[$model->programid] == 2 || $model->button[$model->programid] == 3){
                //卒ボタンを作成する
                $objForm->ae( array("type"	=> "button",
                                    "name"	=> "btn_grd",
                                    "value"	=> " 卒 ",
                                    "extrahtml" => "onclick=\"showSearch('grd')\" id=\"btn_grd\""));

                $arg["btn_grd"] = $objForm->ge("btn_grd");
            }
        }
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRADE2") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRD_YEAR") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HR_CLASS") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COURSECODE") );

        knjCreateHidden($objForm, "SRCH_SCHREGNO");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME_SHOW") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME_KANA") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME_ENG") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEX") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DATE") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "mode",
                            "value"     => $model->mode
                            ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "path",
                            "value"     => REQUESTROOT .$model->path[$model->programid]
                            ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PROGRAMID",
                            "value"     => $model->programid
                            ) );
                            
        if (is_array($schregno)){
            //hidden
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "SCHREGNO",
                                "value"     => implode(",", $schregno)
                                ) );
        }
        $arg["finish"]  = $objForm->get_finish();
        $arg["jscript"] = "";

        if($model->cmd=="search" && $model->button[$model->programid] != 2){
            $arg["jscript"] .= "document.forms[0].GRADE.value = '" .$model->search["GRADE"] ."'\n";
        }
        View::toHTML($model, "knjxexp_ghrForm1.html", $arg);
    }
}
?>
