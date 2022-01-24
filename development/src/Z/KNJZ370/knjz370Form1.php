<?php

require_once('for_php7.php');

class knjz370Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz370index.php", "", "list");

        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();

        //前年度コピーボタンを押したときのチェックFLG
        $year_flg  = $db->getOne(knjz370Query::selectYearQuery($model->year, "course_group_dat"));

        //学年コンボ
        $opt_grade = array();
        $result = $db->query(knjz370Query::getGradeQuery());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_grade[] = array("label" => $row["GRADE"]."学年",
                                 "value" => $row["GRADE"]);
        }
        if(!$model->select_grade){
            $model->select_grade = $opt_grade[0]["value"];
        }

        //リスト表示
        $hrclass = array();
        //複数のクラスを取得
        $result = $db->query(knjz370Query::getListQuery($model->select_grade));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if(isset($hrclass[$row["COURSE_SEQ"]])){
                $hrclass[$row["COURSE_SEQ"]] .= "<br>" . $row["HR_NAMEABBV"];
            }else{
                $hrclass[$row["COURSE_SEQ"]] = $row["HR_NAMEABBV"];
            }
        }
        //グループコードを取得
        $result = $db->query(knjz370Query::getListQuery($model->select_grade, "data"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $course_seq=$row["COURSE_SEQ"];
            $row["COURSE_SEQ"] = View::alink("knjz370index.php",
                                             $row["COURSE_SEQ"],
                                             "target=right_frame",
                                             array("cmd"        => "edit",
                                                   "GRADE"      => $row["GRADE"],
                                                   "COURSE_SEQ" => $row["COURSE_SEQ"],
                                                   "GROUP_NAME" => $row["GROUP_NAME"] ));
            $row["HR_CLASS"] = $hrclass[$course_seq];

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);
        
        //対象年度
        $arg["year"] = "&nbsp;対象年度&nbsp;" . $model->year . "&nbsp;";
        //学年コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE",
                            "size"        => "1",
#                            "extrahtml"   => " onChange=\"btn_submit('list')\" ",
                            "extrahtml"   => " onChange=\"btn_submit('chgrade')\" ",
                            "value"       => $model->select_grade,
                            "options"     => $opt_grade ));
        $arg["GRADE"] = "&nbsp;学年：" . $objForm->ge("GRADE");
        //前年度コピーボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "copy_year",
                            "extrahtml"   => " style=\"width:80%;\" onClick=\"btn_submit('copy')\" ",
                            "value"       => "前年度からコピー" ));
        $arg["copy_year"] = "&nbsp;" . $objForm->ge("copy_year");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_flg",
                            "value"     => $year_flg) );

        $arg["finish"]  = $objForm->get_finish();

        //学年コンボ変更時
        if($model->cmd == "chgrade"){
            $arg["reload"] = "parent.right_frame.location.href='knjz370index.php?cmd=edit&GRADE=".$model->select_grade."';";
            unset($model->course_seq);
        }

        View::toHTML($model, "knjz370Form1.html", $arg);
    }
}
?>
