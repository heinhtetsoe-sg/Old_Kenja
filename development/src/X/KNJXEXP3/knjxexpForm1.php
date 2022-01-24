<?php

require_once('for_php7.php');

class knjxexpForm1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");
        $db     = Query::dbCheckOut();
        //年度と学期
        $arg["CTRL_YEAR"]     = CTRL_YEAR;
        $arg["CTRL_SEMESTER"] = CTRL_SEMESTERNAME;
        //卒業生検索以外
        if ($model->button[$model->programid] != 2){
            //学年コンボボックス
            $opt = array();
            if ($model->cmd == "search"){
                $opt[] = array("label"  => '',
                                "value" => '');
            }
            $result = $db->query(knjxexpQuery::GetHr_Class($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  => $row["HR_NAME"],
                               "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
                if (!isset($model->grade)) $model->grade = $row["GRADE"] ."-" .$row["HR_CLASS"];
                if ($model->grade == $row["HR_NAMEABBV"] && $model->cmd == "list") {
                    $staffname = "担任名：". $row["STAFFNAME_SHOW"] ."( " .$row["STAFFCD"] ." )";
                }
            }
            //担任名
            $arg["STAFFNAME"] = $staffname;
            $objForm->ae( array("type"      => "select",
                                "name"      => "GRADE",
                                "size"      => "1",
                                "extrahtml" => "Onchange=\"btn_submit('chg_grade')\"",
                                "value"     => $model->grade,
                                "options"   => $opt ));

            $arg["GRADE"] = "年組：" .$objForm->ge("GRADE");
        }
        if ($model->check[$model->programid]){
            $objForm->ae(array("type"       => "checkbox",
                                "name"      => "chk_all",
                                "extrahtml" => "onClick=\"return check_all(this);\"" ));

            $arg["CHECK_ALL"] = $objForm->ge("chk_all");
        }
        if ($model->programid == "KNJH160" || $model->programid == "KNJH160A"){ //学籍基礎データ入力（担任）
            $arg["INFO"] = true;
        }
        if ($model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO") {
            $arg["SEARCH_B"] = "1";
        } else {
            $arg["SEARCH_A"] = "1";
        }
        //卒業生検索以外
        if (($model->cmd == "list" && $model->mode == "ungrd") || $model->cmd == "search") {
            //生徒表示
            $result = $db->query(knjxexpQuery::GetStudents($model));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            $schregno = array();
            list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schregno[] = $row["SCHREGNO"];
                $a = array("cmd"     => $cmd,
                          "PRG"      => $model->programid,
                          "AUTH"     => $model->auth,
                          "SCHREGNO" => $row["SCHREGNO"],
                          "mode"     => $model->mode,
                          "GRADE"    => $row["GRADE"],
                          "HR_CLASS" => $row["HR_CLASS"],
                          "ATTENDNO" => $row["ATTENDNO"],
                          "NAME"     => $row["NAME_SHOW"]);
                if ($model->mode == "grd") {  //卒業生
                    $a = array_merge($a,array("GRD_YEAR" => $row["GRD_YEAR"],
                                          "GRD_SEMESTER" => $row["GRD_SEMESTER"],
                                          "GRD_GRADE"    => $row["GRD_GRADE"],
                                          "GRD_HR_CLASS" => $row["GRD_HR_CLASS"],
                                          "GRD_ATTENDNO" => $row["GRD_ATTENDNO"]));
                }
                if ($model->programid == "KNJH160A") {
                    $row["BASE"] = View::alink(REQUESTROOT ."/H/KNJH150A/knjh150aindex.php", "基", "target=right_frame onclick=\"Link(this)\"",$a);
                    $row["HOME"] = View::alink(REQUESTROOT ."/H/KNJH020A/knjh020aindex.php", "親", "target=right_frame onclick=\"Link(this)\"",$a);
                    if ($model->Properties["KNJH010A_DISASTER_".$row["SCHOOL_KIND"]] == "1") {
                        $row["MOVE"] = View::alink(REQUESTROOT ."/H/KNJH010A_DISASTER/knjh010a_disasterindex.php", "環", "target=right_frame onclick=\"Link(this)\"",$a);
                    } else {
                        $row["MOVE"] = View::alink(REQUESTROOT ."/H/KNJH010A/knjh010aindex.php", "環", "target=right_frame onclick=\"Link(this)\"",$a);
                    }
                } else {
                    $row["BASE"] = View::alink(REQUESTROOT ."/H/KNJH150/knjh150index.php", "基", "target=right_frame onclick=\"Link(this)\"",$a);
                    $row["HOME"] = View::alink(REQUESTROOT ."/H/KNJH020/knjh020index.php", "親", "target=right_frame onclick=\"Link(this)\"",$a);
                    $row["MOVE"] = View::alink(REQUESTROOT ."/H/KNJH010/knjh010index.php", "環", "target=right_frame onclick=\"Link(this)\"",$a);
                }

                $row["IMAGE"]    = $image[($row["SEX"]-1)];
                $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
                $arg["data"][]   = $row;
                $i++;
            }
            $arg["CLASS_SUM"] = $i;
            $result->free();
        }
        Query::dbCheckIn($db);

        if ($model->usr_auth == DEF_UPDATABLE || $model->usr_auth == DEF_REFERABLE){
            //在籍または両方
            if ($model->button[$model->programid] == 1 || $model->button[$model->programid] == 3){
                //在ボタンを作成する
                $objForm->ae( array("type"      => "button",
                                    "name"      => "btn_ungrd",
                                    "value"     => " 在 ",
                                    "extrahtml" => "onclick=\"showSearch('ungrd')\""));

                $arg["btn_ungrd"] = $objForm->ge("btn_ungrd");
            }
            //卒業または両方
            if ($model->button[$model->programid] == 2 || $model->button[$model->programid] == 3){
                //卒ボタンを作成する
                $objForm->ae( array("type"      => "button",
                                    "name"      => "btn_grd",
                                    "value"     => " 卒 ",
                                    "extrahtml" => "onclick=\"showSearch('grd')\""));

                $arg["btn_grd"] = $objForm->ge("btn_grd");
            }
        }
        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "GRADE2") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "GRD_YEAR") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "HR_CLASS") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "COURSECODE") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "NAME") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "NAME_SHOW") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "NAME_KANA") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "NAME_ENG") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "SEX") );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "mode",
                            "value" => $model->mode
                            ) );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "path",
                            "value" => REQUESTROOT .$model->path[$model->programid]
                            ) );

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "PROGRAMID",
                            "value" => $model->programid
                            ) );
                            
        if (is_array($schregno)){
            //hidden
            $objForm->ae( array("type"  => "hidden",
                                "name"  => "SCHREGNO",
                                "value" => implode(",", $schregno)
                                ) );
        }
        $arg["finish"]  = $objForm->get_finish();
        $arg["jscript"] = "";

        if($model->cmd=="search" && $model->button[$model->programid] != 2){
            $arg["jscript"] .= "document.forms[0].GRADE.value = '" .$model->search["GRADE"] ."'\n";
        }
        View::toHTML($model, "knjxexpForm1.html", $arg);
    }
}
?>
