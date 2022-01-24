<?php

require_once('for_php7.php');

class knjxexpForm1
{
    public function main(&$model)
    {
        /*       if ($model->usr_auth != DEF_UPDATABLE && $model->usr_auth != DEF_UPDATE_RESTRICT){
                   $arg["jscript"] = "OnAuthError();";
               } */

        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");

        //年度と学期
        $opt_year = [];
        if ($model->cmd == "search") {
            $opt_year[] = array("label"  => '',
                                "value" => '');
        }
        $db     = Query::dbCheckOut();
        $result = $db->query(knjxexpQuery::getYearSemester("off"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = [
                "label" => $row["YEAR"] ."年度 " .$row["SEMESTERNAME"],
                "value" => $row["YEAR"]."-".$row["SEMESTER"]
            ];
        }
        if (!$model->yearseme && $opt_year[0]["value"]) {
            $model->yearseme = $opt_year[0]["value"];
            $model->year = SUBSTR($model->yearseme, 0, 4);
            $model->seme = SUBSTR($model->yearseme, 5);
        }
        if ($model->cmd == "search") {
            $model->yearseme = "";
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "CTRL_YEAR",
                            "size"        => "1",
                            "extrahtml"   => "Onchange=\"btn_submit('chg_year')\"",
                            "value"       => $model->yearseme,
                            "options"     => $opt_year ));

        $arg["CTRL_YEAR"]       = $objForm->ge("CTRL_YEAR");

        $result->free();
        Query::dbCheckIn($db);

        $db     = Query::dbCheckOut();
        //卒業生検索以外
//        if ($model->button[$model->programid] != 2){
        //学年コンボボックス
        $opt = array();
        if ($model->cmd == "search") {
            $opt[] = array("label"  => '',
                                "value" => '');
        }
//            if ($model->cmd == "chg_year" || $model->cmd == "datachenge"){
        //              $model->grade = "";
        //        }

        $result = $db->query(knjxexpQuery::GetHr_Class($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row["HR_NAME"],
                               "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
            if (!isset($model->grade)) {
                $model->grade = $row["GRADE"] ."-" .$row["HR_CLASS"];
            }
            if ($model->cmd == "chg_year2") {
                $model->grade = $row["GRADE"] ."-" .$row["HR_CLASS"];
                $model->cmd = "datachenge";
            }
            if ($model->grade == $row["HR_NAMEABBV"] && $model->cmd == "list") {
//                    $staffname = "担任名：". $row["STAFFNAME_SHOW"] ."( " .$row["STAFFCD"] ." )";
            }
        }

        //担任名
//            $arg["STAFFNAME"] = $staffname;
        $objForm->ae(array("type"        => "select",
                                "name"        => "GRADE",
                                "size"        => "1",
                                "extrahtml"   => "Onchange=\"btn_submit('chg_grade')\"",
                                "value"       => $model->grade,
                                "options"     => $opt ));

        $arg["GRADE"] = "年組：" .$objForm->ge("GRADE");
//        }
        if ($model->check[$model->programid]) {
            $objForm->ae(array("type"      => "checkbox",
                                "name"      => "chk_all",
                                "extrahtml"   => "onClick=\"return check_all(this);\"" ));

            $arg["CHECK_ALL"] = $objForm->ge("chk_all");
        }
        if ($model->programid == "KNJI120") { //学籍基礎データ入力（担任）
            $arg["INFO"] = true;
        }
        //生徒表示
        if ($model->cmd == 'search') {
            $result = $db->query(knjxexpQuery::GetStudents($model));
        } else {
            $result = $db->query(knjxexpQuery::GetMStudents($model));
        }
        $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
        $i =0;
        $schregno = array();
        list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
        //検索フラグ
        $grdcheck = "";
        if ($model->cmd=="search") {
            $grdcheck = "on";
        } else {
            $grdcheck = "off";
        }

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregno[] = $row["SCHREGNO"];
            $a = array("cmd"    => $cmd,
                      "CTRL_YEAR"   => $model->yearseme,
                      "SCHREGNO"    => $row["SCHREGNO"],
                      "mode"        => $model->mode,
                      "GRADE"       => $row["GRADE"],
                      "HR_CLASS"    => $row["HR_CLASS"],
                      "ATTENDNO"    => $row["ATTENDNO"],
                      "NAME"        => $row["NAME_SHOW"],
                      "GRDCHECK"    => $grdcheck);
            if ($model->mode == "grd") {  //卒業生
                $a = array_merge($a, array("GRD_YEAR"    => $row["GRD_YEAR"],
                                      "GRD_SEMESTER"    => $row["GRD_SEMESTER"],
                                      "GRD_GRADE"       => $row["GRD_GRADE"],
                                      "GRD_HR_CLASS"    => $row["GRD_HR_CLASS"],
                                      "GRD_ATTENDNO"    => $row["GRD_ATTENDNO"]));
            }
            /*
                        if ($model->cmd == 'search'){
                            $a = array_merge($a,array("GRDCHECK"    => "on"));
                        }else {
                            $a = array_merge($a,array("GRDCHECK"    => "off"));
                        }
            */
            $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"", $a);
            $row["IMAGE"] = $image[($row["SEX"]-1)];
            $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            $arg["data"][] = $row;
            $i++;
        }
        $arg["CLASS_SUM"] = $i;
        $result->free();

        Query::dbCheckIn($db);

        if ($model->usr_auth == DEF_UPDATABLE || $model->usr_auth == DEF_REFERABLE) {
            //在籍または両方
            if ($model->button[$model->programid] == 1 || $model->button[$model->programid] == 3) {
                //在ボタンを作成する
                $objForm->ae(array("type"  => "button",
                                    "name"  => "btn_ungrd",
                                    "value" => " 在 ",
                                    "extrahtml" => "onclick=\"showSearch('ungrd')\""));

                $arg["btn_ungrd"] = $objForm->ge("btn_ungrd");
            }
            //卒業または両方
            if ($model->button[$model->programid] == 2 || $model->button[$model->programid] == 3) {
                //卒ボタンを作成する
                $objForm->ae(array("type"  => "button",
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

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GAMENKANYEAR",
                            "value"     => $model->yearseme
                            ));

        if (is_array($schregno)) {
            //hidden
            $objForm->ae(array("type"      => "hidden",
                                "name"      => "SCHREGNO",
                                "value"     => implode(",", $schregno)
                                ));
        }
        $arg["finish"]  = $objForm->get_finish();
        $arg["jscript"] = "";

        if ($model->cmd=="search") {
            $arg["jscript"] .= "document.forms[0].GRADE.value = '" .$model->search["GRADE"] ."'\n";
        }
        if ($model->cmd=="chg_year") {
            $arg["reload"]  = "parent.right_frame.location.href='".REQUESTROOT."/I/KNJI120/knji120index.php?cmd=chg_year&GAMENKANYEAR=".$model->yearseme."&GAMENKANGRADE=".$model->grade."','right_frame'";
//          $arg["reload"]  = "parent.right_frame.location.href='".REQUESTROOT."/I/KNJI120/knji120index.php?cmd=edit&GAMENKANYEAR=".$model->yearseme."&GAMENKANGRADE=".$model->grade."','right_frame'";
        }
        if ($model->cmd=="list") {
            $arg["reload"]  = "parent.right_frame.location.href='".REQUESTROOT."/I/KNJI120/knji120index.php?cmd=edit&GAMENKANYEAR=".$model->yearseme."&GAMENKANGRADE=".$model->grade."','right_frame'";
        }
        View::toHTML($model, "knjxexpForm1.html", $arg);
    }
}
