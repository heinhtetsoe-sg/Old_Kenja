<?php

require_once('for_php7.php');

class knjxexp5Form1
{
    function main(&$model){
/*       if ($model->usr_auth != DEF_UPDATABLE && $model->usr_auth != DEF_UPDATE_RESTRICT){
           $arg["jscript"] = "OnAuthError();";
       } */

        //作成経緯：KNJEXPから複製。診療科目のコンボボックスを利用する事と、初期表示で右フレームに検索画面を表示することになり、作成。

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");

        //配列をセット（年度コンボフラグ）
        $combo_flg = knjxexp5Query::getComboFlg();

        $db     = Query::dbCheckOut();
        //年度と学期
        if (!isset($model->exp_year)) $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;
        //年度コンボボックス使用時
        if ($combo_flg[$model->programid] == "1"){
            $opt_year = array();
            $result = $db->query(knjxexp5Query::getYearSemester("off"));
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
        //卒業生検索以外
        if ($model->button[$model->programid] != 2){
            //学年コンボボックス
            $opt = array();
            if ($model->cmd == "search"){
                $opt[] = array("label"  => '',
                                "value" => '');
            }
            if (in_array($model->programid, array("KNJF150", "KNJF150A")) && $model->Properties["useNurseoffRestrict"] == "1" && ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT)) {
                //担当保健室で制限
                $result = $db->query(knjxexp5Query::GetGrade2($model));
            } else {
                $result = $db->query(knjxexp5Query::GetGrade($model));
            }
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  => $row["LABEL"],
                               "value"  => $row["VALUE"]);
                if (!isset($model->grade)) $model->grade = $row["VALUE"];
            }
            $model->grade = $model->cmd == 'search' ? $model->searchGrade : $model->grade;

            $objForm->ae( array("type"        => "select",
                                "name"        => "GRADE",
                                "size"        => "1",
                                "extrahtml"   => "Onchange=\"btn_submit('chg_grade')\"",
                                "value"       => $model->grade,
                                "options"     => $opt ));
            $arg["GRADE"] = "学年：" .$objForm->ge("GRADE");


            //組コンボボックス
            $opt = array();
            $opt[] = array("label"  => '', "value" => '');

            $result = $db->query(knjxexp5Query::GetHr_Class($model, $model->grade));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  => $row["HR_NAME"],
                               "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
                if ($model->grade_hrclass == $row["HR_NAMEABBV"] && $model->cmd == "list") {
                    $staffname = "担任名：". $row["STAFFNAME_SHOW"] ."( " .$row["STAFFCD"] ." )";
                }
            }

            $model->grade_hrclass = $model->cmd == 'search' ? $model->searchGradeHr : $model->grade_hrclass;

            //担任名
            $arg["STAFFNAME"] = $staffname;

            $objForm->ae( array("type"        => "select",
                                "name"        => "GRADE_HR_CLASS",
                                "size"        => "1",
                                "extrahtml"   => "Onchange=\"btn_submit('chg_grade_hrclass')\"",
                                "value"       => $model->grade_hrclass,
                                "options"     => $opt ));

            $arg["GRADE_HR_CLASS"] = "組：" .$objForm->ge("GRADE_HR_CLASS");



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
        }

        if ($model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO" || $model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO_NORMAL") {
            $arg["SEARCH_B"] = "1";
        } else {
            $arg["SEARCH_A"] = "1";
        }

        //診療科目
        if (true) {
            $opt = array();
            $arg["DISPDIAGTYPE"] = "1";
            $model->diagtype = $model->diagtype == "" ? "1" : $model->diagtype;
            $opt = array(
                           array("label"=>"内科", "value"=>"1"),
                           array("label"=>"外科", "value"=>"2"),
                           array("label"=>"健康相談", "value"=>"5"),
                           array("label"=>"その他", "value"=>"3")
                           // array("label"=>"保健調査情報", "value"=>"4")  //4は入力画面未作成のため、除外。
                          );
            $extra = "Onchange=\"btn_submit('chg_grade')\"";
            $arg["DIAGTYPE"] = knjCreateCombo($objForm, "DIAGTYPE", $model->diagtype, $opt, $extra, 1, "");
        }


        //卒業生検索以外

        if (($model->cmd == "list" && $model->mode == "ungrd") || $model->cmd == "search") {
            //生徒表示
            $query = knjxexp5Query::GetStudents($model);
            $result = $db->query($query);
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i = 0;
            $linkCnt = 0;
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
                if ($model->diagtype != "") {
                    $a["cmd"] = "subform".$model->diagtype;
                    $a = array_merge($a, array("DIAGTYPE"   => $model->diagtype));
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
                } else {
                    if ($model->programid == "KNJA120A") {
                        $setLink = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "",$a);
                        $setLink = str_replace("href=", "", str_replace(">{$row["NAME_SHOW"]}<", "", str_replace("<a ", "", str_replace("/a>", "", $setLink))));
                        $setLink = str_replace("\"", "", $setLink);
                        //hidden
                        knjCreateHidden($objForm, "linkVal".$row["SCHREGNO"], $setLink);
                        knjCreateHidden($objForm, "linkCnt".$row["SCHREGNO"], $linkCnt);
                        $row["NAME_SHOW"] = "<a href=\"javascript:Link2(this, '{$row["SCHREGNO"]}')\">{$row["NAME_SHOW"]}</a>";
                        $linkCnt++;
                    } else {
                        $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"",$a);
                    }

                }
                $row["IMAGE"] = $image[($row["SEX"]-1)];
                $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
                $arg["data"][] = $row;
                $i++;
            }
            $arg["CLASS_SUM"] = $i;
            $result->free();
        }
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "GRADE2");

        knjCreateHidden($objForm, "GRADE_HR_CLASS2");

        //hidden
        knjCreateHidden($objForm, "GRD_YEAR");

        //hidden
        knjCreateHidden($objForm, "HR_CLASS");

        //hidden
        knjCreateHidden($objForm, "COURSECODE");

        //hidden
        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "SRCH_SCHREGNO");

        //hidden
        knjCreateHidden($objForm, "NAME");

        //hidden
        knjCreateHidden($objForm, "NAME_SHOW");

        //hidden
        knjCreateHidden($objForm, "NAME_KANA");

        //hidden
        knjCreateHidden($objForm, "NAME_ENG");

        //hidden
        knjCreateHidden($objForm, "SEX");

        //hidden
        knjCreateHidden($objForm, "DATE");

        //hidden
        knjCreateHidden($objForm, "mode", $model->mode);

        //hidden
        knjCreateHidden($objForm, "path", REQUESTROOT .$model->path[$model->programid]);

        //hidden
        knjCreateHidden($objForm, "PROGRAMID", $model->programid);
                            
        if (is_array($schregno)){
            //hidden
            knjCreateHidden($objForm, "SCHREGNO", implode(",", $schregno));
        }

        //hidden
        knjCreateHidden($objForm, "changeFlg");
        knjCreateHidden($objForm, "setOrder");
        //上位画面から"LOADFRM"に"true"を指定する事で、初期表示に右フレームに検索画面を表示させる。
        if ($model->loadFrame == "true") {
            $arg["jscript"] = "showSearch('ungrd');";
        } else {
            $arg["jscript"] = "";
            if ($model->usr_auth == DEF_UPDATABLE || $model->usr_auth == DEF_REFERABLE){
                //在籍または両方
                if ($model->button[$model->programid] == 1 || $model->button[$model->programid] == 3){
                    //在ボタンを作成する
                    $objForm->ae( array("type"	=> "button",
                                        "name"	=> "btn_ungrd",
                                        "value"	=> " 在 ",
                                        "extrahtml" => "onclick=\"showSearch('ungrd')\""));

                    $arg["btn_ungrd"] = $objForm->ge("btn_ungrd");
                }
                //卒業または両方
                if ($model->button[$model->programid] == 2 || $model->button[$model->programid] == 3){
                    //卒ボタンを作成する
                    $objForm->ae( array("type"	=> "button",
                                        "name"	=> "btn_grd",
                                        "value"	=> " 卒 ",
                                        "extrahtml" => "onclick=\"showSearch('grd')\""));

                    $arg["btn_grd"] = $objForm->ge("btn_grd");
                }
            }

        }
        knjCreateHidden($objForm, "LOADFRM", $model->loadFrame);

        $arg["finish"]  = $objForm->get_finish();
        if($model->cmd=="search" && $model->button[$model->programid] != 2){
            $arg["jscript"] .= "document.forms[0].GRADE.value = '" .$model->search["GRADE"] ."'\n";
        }
        View::toHTML($model, "knjxexp5Form1.html", $arg);
    }
}
?>
