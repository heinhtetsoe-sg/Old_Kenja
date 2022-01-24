<?php

require_once('for_php7.php');

class knjxSearch
{
    function main(&$model){
        $objForm = new form;
        
        $label = "全角で入力する必要がある";
        $label_half = "半角で入力する必要がある";
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("search", "POST", "index.php", "right_frame");

        $db = Query::dbCheckOut();
        //年度と学期
        $Row = $db->getRow(knjxexp_ghrQuery::getYearSemester($model->exp_year), DB_FETCHMODE_ASSOC);
        $arg["EXP_YEAR"] = "年度：" .$Row["YEAR"] ."　学期：" .$Row["SEMESTERNAME"];
         /* Add by PP for PC-Talker 2019-01-20 start */

        $arg["YEAR"] = $Row["YEAR"]."年度";
        $arg["SEMESTERNAME"] = $Row["SEMESTERNAME"]."の";

        /* Add by PP for PC-Talker 2019-01-31 end */

        //在籍生徒
        /* Add by PP for PC-Talker 2019-01-20 start */
        $grade = "";
        /* Add by PP for PC-Talker 2019-01-31 end */
        if ($model->mode == "ungrd"){
            //学年コンボボックス
            $opt = array();
            $opt[] = array("label"  => '',
                            "value" => '');
                    
            $result = $db->query(knjxexp_ghrQuery::GetHr_Class($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  => $row["HR_NAME"],
                               "value" => $row["GRADE"]."-".$row["HR_CLASS"]);

            }

            $objForm->ae( array("type"        => "select",
                                "name"        => "年組",
                                "value"       => $model->grade,
                                /* Edit by PP for PC-Talker 2019-01-20 start */
                                "extrahtml"  => "aria-label=\"年組\"",
                                /* Edit by PP for PC-Talker 2019-01-31 end */
                                "size"        => "1",
                                "options"     => $opt ));
            /* Edit by PP for PC-Talker (focus) 2019-01-20 start */
            $arg["LABEL"] = "在学生の検索画面";
            $grade = "btn_ungrd";
             echo "<script>var TITLE= '".$Row["YEAR"]."年度".$Row["SEMESTERNAME"]."の在学生の検索画面"."';
              </script>";
            /* Edit by PP for PC-Talker(focus) 2019-01-31 end */
            $arg["GRADE"] = $objForm->ge("GRADE2");
        //卒業生徒
        } else if ($model->mode == "grd") {
            //卒業年度
            $opt = array();
            $opt[] = array("label"  => '',
                            "value" => '');
            $result = $db->query(knjxexp_ghrQuery::GetGrdYear());
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  => $row["GRD_YEAR"]."年",
                                "value" => $row["GRD_YEAR"]);

            }
            $objForm->ae( array("type"        => "select",
                                "name"        => "GRD_YEAR",
                                "size"        => "1",
                                /* Edit by PP for PC-Talker 2019-01-20 start */
                                "extrahtml"  => "aria-label=\"卒業年度\"",
                                /* Edit by PP for PC-Talker 2019-01-31 end */
                                "options"     => $opt ));

            $arg["GRD_YEAR"] = $objForm->ge("GRD_YEAR");
            /* Edit by PP for PC-Talker(focus) 2019-01-20 start */
            $arg["LABEL"] = "卒業学生の検索画面";
            $grade = "btn_grd";
            echo "<script>var TITLE= '".$Row["YEAR"]."年度".$Row["SEMESTERNAME"]."の卒業学生の検索画面"."';
              </script>";
            /* Edit by PP for PC-Talker(focus) 2019-01-31 end */
            //卒業時組
            $objForm->ae( array("type"        => "text",
                                "name"        => "HR_CLASS",
                                "size"        => 3,
                                "maxlength"   => 3,
                                "extrahtml"  => "onblur=\"this.value=toAlphaNumber(this.value)\" aria-label=\"$label 卒業組\"",
                                "value"       => ""));
            $arg["HR_CLASS"] = $objForm->ge("HR_CLASS");
        }
        //コースコンボボックス
        $opt = array();
        $opt[] = array("label"  => '',
                        "value" => '');
        $result = $db->query(knjxexp_ghrQuery::GetCourseCode());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  => $row["COURSECODE"]."　".$row["COURSECODENAME"],
                            "value" => $row["COURSECODE"]);

        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSECODE",
                            "size"        => "1",
                            /* Edit by PP for PC-Talker 2019-01-20 start */
                            "extrahtml"  => "aria-label=\"コース\"",
                            /* Edit by PP for PC-Talker 2019-01-31 end */
                            "options"     => $opt ));

        $arg["COURSECODE"] = $objForm->ge("COURSECODE");

        Query::dbCheckIn($db);

        //学籍番号
        $item_name = ($model->Properties["schregno_bubunkensaku"] == "1") ? "学籍番号（部分一致）" : "学籍番号";
        $arg["SRCH_SCHREGNO_ITEM_NAME"] = $item_name;
        $extra = "aria-label=\"$label $item_name\"";
        $arg["SRCH_SCHREGNO"] = knjCreateTextBox($objForm, "", "SRCH_SCHREGNO", 8, 8, $extra);

        //漢字姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            /* Edit by PP for PC-Talker 2019-01-20 start */
                            "extrahtml"  => "aria-label=\"$label 氏名\"",
                            /* Edit by PP for PC-Talker 2019-01-31 end */
                            "name"        => "NAME"));

        $arg["NAME"] = $objForm->ge("NAME");

        //漢字姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            /* Edit by PP for PC-Talker 2019-01-20 start */
                            "extrahtml"  => "aria-label=\"$label 氏名表示用\"",
                            /* Edit by PP for PC-Talker 2019-01-31 end */
                            "name"        => "NAME_SHOW"));

        $arg["NAME_SHOW"] = $objForm->ge("NAME_SHOW");

        //かな姓
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            /* Edit by PP for PC-Talker 2019-01-20 start */
                            "extrahtml"   => "aria-label=\"$label 氏名かな\"",
                            /* Edit by PP for PC-Talker 2019-01-31 end */
                            "name"        => "NAME_KANA"));
        
        $arg["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //英字氏名
        $objForm->ae( array("type"        => "text",
                            "size"        => 40,
                            "maxlength"   => 40,
                            /* Edit by PP for PC-Talker 2019-01-20 start */
                            "extrahtml"  => "onblur=\"this.value=toAlpha(this.value)\" aria-label=\"$label_half 英字氏名\"",
                            /* Edit by PP for PC-Talker 2019-01-31 end */
                            "name"        => "NAME_ENG"));

        $arg["NAME_ENG"] = $objForm->ge("NAME_ENG");

        //性別
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            /* Edit by PP for PC-Talker 2019-01-20 start */
                            "extrahtml"  => "aria-label=\"性別\"",
                            /* Edit by PP for PC-Talker 2019-01-31 end */
                            "options"     => array(array("label" => "", "value" => ''),
                                                   array("label" => "男性", "value" => '1'),
                                                   array("label" => "女性", "value" => '2'))
                             ));

        $arg["SEX"] = $objForm->ge("SEX");

        if($model->programid == "KNJF150"){
            //日付
            $arg["DATE"] = View::popUpCalendar($objForm, "DATE", $model->search["DATE"]);
        }

        //実行ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search",
                            "value"       => "検索",
                            "extrahtml"   => "onclick=\"return search_submit('".$model->mode."');\" id=\"btn_search\"" ));
        $arg["btn_search"] = $objForm->ge("btn_search");

        //閉じるボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"current_cursor('$grade'); return btn_back();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxSearch.html", $arg);
    }
}
?>