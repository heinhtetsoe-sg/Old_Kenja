<?php

require_once('for_php7.php');

class knjxsearch5Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");
        //DB接続
        $db     = Query::dbCheckOut();

        //年度と学期
        $arg["CTRL_YEAR"]       = CTRL_YEAR;
        $arg["CTRL_SEMESTER"]   = CTRL_SEMESTERNAME;

        //クラスコンボ
        $query = knjxsearch5Query::GetHr_Class($model);
        makeCombo($objForm, $arg, $db, $query, "LEFT_GRADE", $model->left_grade, "onChange=\"btn_submit('chg_grade')\"", 1);

        //在ボタンを作成する
        $arg["btn_ungrd"] = createBtn($objForm, "btn_ungrd", " 在 ", "onclick=\"showSearch('ungrd')\"");

        //卒ボタンを作成する
        $arg["btn_grd"] = createBtn($objForm, "btn_grd", " 卒 ", "onclick=\"showSearch('grd')\"");

        //生徒リスト表示
        if ($model->cmd == "search" || ($model->cmd == "list" && $model->left_grade)) {
            makeStudentList($objForm, $arg, $db, $model);
            $model->firstFlg = false;
        }

        //hidden作成
        makeHidden($objForm, $model, $schregno);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["jscript"] = "";

        View::toHTML($model, "knjxsearch5Form1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size)
{
    //学年コンボボックス
    $opt = array();
    $opt[] = array("label"  => '',
                   "value" => '');

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);

    }
    $result->free();
    $arg[$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒リスト表示
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    $result = $db->query(knjxsearch5Query::GetStudents($model));
    $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
    $i =0;
    $schregno = array();
    list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);

    while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $schregno[] = $row["SCHREGNO"];
        $a = array("cmd"    => $cmd,
                   "SCHREGNO"    => $row["SCHREGNO"],
                   "mode"        => $model->mode,
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
        $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"",$a);
        $row["IMAGE"] = $image[($row["SEX"]-1)];
        $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
        $arg["data"][] = $row;
        $i++;
    }
    $arg["CLASS_SUM"] = $i;
    $result->free();
}

//hidden作成
function makeHidden(&$objForm, $model, $schregno)
{
    $objForm->ae(createHiddenAe("GRADE"));
    $objForm->ae(createHiddenAe("COURSECODE"));
    $objForm->ae(createHiddenAe("CLUBCD"));
    $objForm->ae(createHiddenAe("CHAIRCD"));
    $objForm->ae(createHiddenAe("HR_CLASS"));
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("NAME"));
    $objForm->ae(createHiddenAe("NAME_SHOW"));
    $objForm->ae(createHiddenAe("NAME_KANA"));
    $objForm->ae(createHiddenAe("NAME_ENG"));
    $objForm->ae(createHiddenAe("KEYWORD"));
    $objForm->ae(createHiddenAe("ACTIONDATE"));
    $objForm->ae(createHiddenAe("mode", $model->mode));
    $objForm->ae(createHiddenAe("path", REQUESTROOT .$model->path[$model->programid]));
    $objForm->ae(createHiddenAe("PROGRAMID", $model->programid));
    if (is_array($schregno)){
        $objForm->ae(createHiddenAe("SCHREGNO", implode(",", $schregno)));
    }
    $objForm->ae(createHiddenAe("GRD_YEAR"));
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

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
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

?>
