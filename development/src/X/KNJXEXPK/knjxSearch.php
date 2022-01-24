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
        $arg["REQUESTROOT"] = REQUESTROOT;
        $db = Query::dbCheckOut();

        //学年コンボボックス
        $opt = array();
        $opt[] = array("label"  => '',
                        "value" => '');
                
        $result = $db->query(knjxexpkQuery::GetHr_Class($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
#minei                $opt[] = array("label"  => (int) $row["GRADE"]."年".$row["HR_CLASS"]."組",
            $opt[] = array("label"  => $row["HR_NAME"],
                            "value" => $row["GRADE"]."-".$row["HR_CLASS"]);

        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE2",
                            "value"       => $model->grade,
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["GRADE"] = $objForm->ge("GRADE2");
        //コースコンボボックス
        $opt = array();
        $opt[] = array("label"  => '',
                        "value" => '');
        $result = $db->query(knjxexpkQuery::GetCourseCode());
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
                            "extrahtml"   => "onclick=\"return search_submit();\"" ));

        $arg["btn_search"] = $objForm->ge("btn_search");

        //閉じるボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"return btn_back();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //異動情報検索ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search2",
                            "value"       => "異動情報検索",
                            "extrahtml"   => "onclick=\"return showSearch(2);\" style=\"width:100\"" ));

        $arg["btn_search2"] = $objForm->ge("btn_search2");

        //交付情報検索ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search3",
                            "value"       => "交付情報検索",
                            "extrahtml"   => "onclick=\"return showSearch(3);\" style=\"width:100\"" ));

        $arg["btn_search3"] = $objForm->ge("btn_search3");

        //銀行情報検索ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search4",
                            "value"       => "銀行情報検索",
                            "extrahtml"   => "onclick=\"return showSearch(4);\" style=\"width:102\"" ));

        $arg["btn_search4"] = $objForm->ge("btn_search4");

        //入金情報検索ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search5",
                            "value"       => "入金情報検索（中分類）",
                            "extrahtml"   => "onclick=\"return showSearch(5);\" style=\"width:151\"" ));

        $arg["btn_search5"] = $objForm->ge("btn_search5");

        //入金情報検索ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search6",
                            "value"       => "入金情報検索（小分類）",
                            "extrahtml"   => "onclick=\"return showSearch(6);\" style=\"width:151\"" ));

        $arg["btn_search6"] = $objForm->ge("btn_search6");
        
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "mode" ) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxSearch.html", $arg);
    }
}
?>