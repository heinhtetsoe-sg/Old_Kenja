<?php

require_once('for_php7.php');

class knjxexpForm1
{
    public function main(&$model)
    {
/*       if ($model->usr_auth != DEF_UPDATABLE && $model->usr_auth != DEF_UPDATE_RESTRICT){
           $arg["jscript"] = "OnAuthError();";
       } */

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");

        //配列をセット（年度コンボフラグ）
        $combo_flg = knjxexpQuery::getComboFlg();

        $db     = Query::dbCheckOut();
        //年度と学期
        if (!isset($model->exp_year)) {
            $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;
        }
        //年度コンボボックス使用時
        if ($combo_flg[$model->programid] == "1") {
            $opt_year = array();
            $result = $db->query(knjxexpQuery::getYearSemester("off"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_year[] = array("label" => $row["YEAR"] ."年度 " .$row["SEMESTERNAME"],
                                    "value" => $row["YEAR"]."-".$row["SEMESTER"]);
            }
            $objForm->ae(array("type"        => "select",
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
        if ($model->button[$model->programid] != 2) {
            //学年コンボボックス
            $opt = array();
            if ($model->cmd == "search") {
                $opt[] = array("label"  => '',
                                "value" => '');
            }
            if (in_array($model->programid, array("KNJF150", "KNJF150A")) && $model->Properties["useNurseoffRestrict"] == "1" && ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT)) {
                //担当保健室で制限
                $result = $db->query(knjxexpQuery::getHrClass2($model));
            } else {
                $result = $db->query(knjxexpQuery::getHrClass($model));
            }
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label"  => $row["HR_NAME"],
                               "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
                if (!isset($model->grade)) {
                    $model->grade = $row["GRADE"] ."-" .$row["HR_CLASS"];
                }
                if ($model->grade == $row["HR_NAMEABBV"] && $model->cmd == "list") {
                    $staffname = "担任名：". $row["STAFFNAME_SHOW"] ."( " .$row["STAFFCD"] ." )";
                }
            }
            //担任名
            $arg["STAFFNAME"] = $staffname;
            $objForm->ae(array("type"        => "select",
                                "name"        => "GRADE",
                                "size"        => "1",
                                "extrahtml"   => "Onchange=\"btn_submit('chg_grade')\"",
                                "value"       => $model->grade,
                                "options"     => $opt ));

            $arg["GRADE"] = "年組：" .$objForm->ge("GRADE");
        }
        if ($model->check[$model->programid]) {
            $objForm->ae(array("type"      => "checkbox",
                                "name"      => "chk_all",
                                "extrahtml"   => "onClick=\"return check_all(this);\"" ));

            $arg["CHECK_ALL"] = $objForm->ge("chk_all");
        }
        if ($model->programid == "KNJA110") { //学籍基礎データ入力（担任）
            $arg["INFO"] = true;
        } elseif ($model->programid == "KNJA110A") { //学籍基礎データ入力（担任）
            $arg["INFO"] = true;
        }

        if ($model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO" || $model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO_NORMAL") {
            $arg["SEARCH_B"] = "1";
        } else {
            $arg["SEARCH_A"] = "1";
        }

        //卒業生検索以外
        if (($model->cmd == "list" && $model->mode == "ungrd") || $model->cmd == "search") {
            //生徒表示
            $result = $db->query(knjxexpQuery::getStudents($model));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i = 0;
            $linkCnt = 0;
            $schregno = array();
            list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
            list($exp_year, $exp_semester) = explode("-", $model->exp_year);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
                if ($model->mode == "grd") {  //卒業生
                    $a = array_merge($a, array("GRD_YEAR"    => $row["GRD_YEAR"],
                                          "GRD_SEMESTER"    => $row["GRD_SEMESTER"],
                                          "GRD_GRADE"       => $row["GRD_GRADE"],
                                          "GRD_HR_CLASS"    => $row["GRD_HR_CLASS"],
                                          "GRD_ATTENDNO"    => $row["GRD_ATTENDNO"]));
                }
                if ($model->check[$model->programid]) {
                    $objForm->ae(array("type"       => "checkbox",
                                       "name"       => "CHECKED",
                                       "multiple"   => true,
                                       "value"      => $row["SCHREGNO"],
                                       "extrahtml"  => "multiple" ));

                    $row["CHECKED"] = $objForm->ge("CHECKED");
                } elseif ($model->programid == "KNJA110") { //学籍基礎データ入力（担任）
                    $row["BASE"] = View::alink(REQUESTROOT ."/A/KNJA110/knja110index.php", "基", "target=right_frame onclick=\"Link(this)\"", $a);
                    unset($a["cmd"]);
                    $row["HOME"] = View::alink(REQUESTROOT ."/A/KNJA110_2/knja110_2index.php", "住", "target=right_frame onclick=\"Link(this)\"", $a);
                    $row["MOVE"] = View::alink(REQUESTROOT ."/A/KNJA110_3/knja110_3index.php", "異", "target=right_frame onclick=\"Link(this)\"", $a);
                } elseif ($model->programid == "KNJA110A") { //学籍基礎データ入力（担任）
                    $row["BASE"] = View::alink(REQUESTROOT ."/A/KNJA110A/knja110aindex.php", "基", "target=right_frame onclick=\"Link(this)\"", $a);
                    unset($a["cmd"]);
                    $row["HOME"] = View::alink(REQUESTROOT ."/A/KNJA110_2A/knja110_2aindex.php", "住", "target=right_frame onclick=\"Link(this)\"", $a);
                    $row["MOVE"] = View::alink(REQUESTROOT ."/A/KNJA110_3A/knja110_3aindex.php", "異", "target=right_frame onclick=\"Link(this)\"", $a);
                } else {
                    if ($model->programid == "KNJA120A") {
                        $setLink = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "", $a);
                        $setLink = str_replace("href=", "", str_replace(">{$row["NAME_SHOW"]}<", "", str_replace("<a ", "", str_replace("/a>", "", $setLink))));
                        $setLink = str_replace("\"", "", $setLink);
                        //hidden
                        knjCreateHidden($objForm, "linkVal".$row["SCHREGNO"], $setLink);
                        knjCreateHidden($objForm, "linkCnt".$row["SCHREGNO"], $linkCnt);
                        $row["NAME_SHOW"] = "<a href=\"javascript:Link2(this, '{$row["SCHREGNO"]}')\">{$row["NAME_SHOW"]}</a>";
                        $linkCnt++;
                    } else {
                        $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"", $a);
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

        if ($model->usr_auth == DEF_UPDATABLE || $model->usr_auth == DEF_REFERABLE) {
            //在籍または両方
            if ($model->button[$model->programid] == 1 || $model->button[$model->programid] == 3) {
                //在ボタンを作成する
                $objForm->ae(array("type"   => "button",
                                    "name"  => "btn_ungrd",
                                    "value" => " 在 ",
                                    "extrahtml" => "onclick=\"showSearch('ungrd')\""));

                $arg["btn_ungrd"] = $objForm->ge("btn_ungrd");
            }
            //卒業または両方
            if ($model->button[$model->programid] == 2 || $model->button[$model->programid] == 3) {
                //卒ボタンを作成する
                $objForm->ae(array("type"   => "button",
                                    "name"  => "btn_grd",
                                    "value" => " 卒 ",
                                    "extrahtml" => "onclick=\"showSearch('grd')\""));

                $arg["btn_grd"] = $objForm->ge("btn_grd");
            }
        }
        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GRADE2"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GRD_YEAR"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "HR_CLASS"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "COURSECODE"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        knjCreateHidden($objForm, "SRCH_SCHREGNO");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "NAME"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "NAME_SHOW"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "NAME_KANA"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "NAME_ENG"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEX"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "DATE"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ENT_DIV"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GRD_DIV"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TRANSFERCD"));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "mode",
                            "value"     => $model->mode
                            ));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "path",
                            "value"     => REQUESTROOT .$model->path[$model->programid]
                            ));

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "PROGRAMID",
                            "value"     => $model->programid
                            ));
                            
        if (is_array($schregno)) {
            //hidden
            $objForm->ae(array("type"      => "hidden",
                                "name"      => "SCHREGNO",
                                "value"     => implode(",", $schregno)
                                ));
        }

        //hidden
        knjCreateHidden($objForm, "changeFlg");
        knjCreateHidden($objForm, "setOrder");

        $arg["finish"]  = $objForm->get_finish();
        $arg["jscript"] = "";

        if ($model->cmd=="search" && $model->button[$model->programid] != 2) {
            $arg["jscript"] .= "document.forms[0].GRADE.value = '" .$model->search["GRADE"] ."'\n";
        }
        View::toHTML($model, "knjxexpForm1.html", $arg);
    }
}
