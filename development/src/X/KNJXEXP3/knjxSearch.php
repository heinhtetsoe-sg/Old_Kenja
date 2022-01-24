<?php

require_once('for_php7.php');

class knjxSearch
{
    function main(&$model){
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("search", "POST", "index.php", "right_frame");

        //年度と学期
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["CTRL_SEMESTER"] = CTRL_SEMESTERNAME;

        $db = Query::dbCheckOut();
        //在籍生徒
        if ($model->mode == "ungrd"){
            //学年コンボボックス
            $opt = array();
            $opt[] = array("label"  => '',
                            "value" => '');
                    
            $result = $db->query(knjxexpQuery::GetHr_Class($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  => $row["HR_NAME"],
                               "value" => $row["GRADE"]."-".$row["HR_CLASS"]);

            }

            $objForm->ae( array("type"        => "select",
                                "name"        => "GRADE2",
                                "value"       => $model->grade,
                                "size"        => "1",
                                "options"     => $opt ));

            $arg["GRADE"] = $objForm->ge("GRADE2");
        //卒業生徒
        } else if ($model->mode == "grd") {
            //卒業年度
            $opt = array();
            $opt[] = array("label"  => '',
                            "value" => '');
            $result = $db->query(knjxexpQuery::GetGrdYear());
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  => $row["GRD_YEAR"]."年",
                                "value" => $row["GRD_YEAR"]);

            }
            $objForm->ae( array("type"        => "select",
                                "name"        => "GRD_YEAR",
                                "size"        => "1",
#                                "value"       => $model->search["GRD_YEAR"],
                                "options"     => $opt ));

            $arg["GRD_YEAR"] = $objForm->ge("GRD_YEAR");
            //卒業時組
            $objForm->ae( array("type"        => "text",
                                "name"        => "HR_CLASS",
                                "size"        => 3,
                                "maxlength"   => 3,
                                "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
                                "value"       => ""));
            $arg["HR_CLASS"] = $objForm->ge("HR_CLASS");
        }
        //コースコンボボックス
        $opt = array();
        $opt[] = array("label"  => '',
                        "value" => '');
        $result = $db->query(knjxexpQuery::GetCourseCode());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  => $row["COURSECODE"]."　".$row["COURSECODENAME"],
                            "value" => $row["COURSECODE"]);

        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSECODE",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["COURSECODE"] = $objForm->ge("COURSECODE");

        Query::dbCheckIn($db);

        //漢字姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"  => "",
                            "name"        => "NAME"));

        $arg["NAME"] = $objForm->ge("NAME");

        //漢字姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"  => "",
                            "name"        => "NAME_SHOW"));

        $arg["NAME_SHOW"] = $objForm->ge("NAME_SHOW");

        //かな姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "name"        => "NAME_KANA"));
        
        $arg["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //英字氏名
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"  => "onblur=\"this.value=toAlpha(this.value)\"",
                            "name"        => "NAME_ENG"));

        $arg["NAME_ENG"] = $objForm->ge("NAME_ENG");

        //性別
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "options"     => array(array("label" => "", "value" => ''),
                                                   array("label" => "男性", "value" => '1'),
                                                   array("label" => "女性", "value" => '2'))
                             ));

        $arg["SEX"] = $objForm->ge("SEX");

        //実行ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search",
                            "value"       => "検索",
                            "extrahtml"   => "onclick=\"return search_submit('".$model->mode."');\"" ));
        $arg["btn_search"] = $objForm->ge("btn_search");

        //閉じるボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"return btn_back();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxSearch.html", $arg);
    }
}
?>