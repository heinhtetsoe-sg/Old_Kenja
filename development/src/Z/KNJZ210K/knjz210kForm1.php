<?php

require_once('for_php7.php');

class knjz210kForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz210kindex.php", "", "list");

        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();

        $year_flg  = $db->getOne(knjz210kQuery::selectYearQuery($model->year, "type_group_mst"));
        $year_flg += $db->getOne(knjz210kQuery::selectYearQuery($model->year, "type_group_hr_dat"));

        //科目取得
        $opt_subclass = array();
        
        //先頭に空リストをセット
        $opt_subclass[] = array("label" => "", "value" => "");

        $subclass_flg = 0;
        $result = $db->query(knjz210kQuery::getSubclassQuery($model->year, $model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
                $opt_subclass[] = array("label" => $row["LABEL"],
                           				"value" => $row["VALUE"]);
            if ($model->subclasscd == $row["VALUE"] || $model->subclasscd == "000000") {
                $subclass_flg = 1;
            }
        }
        $opt_subclass[] = array("label" => "000000" . " " . "全て",
                                "value" => "000000");
        if (!$subclass_flg) {
            $model->subclasscd = $opt_subclass[0]["value"];
        }
        
        ////リスト表示
        //複数のクラスを取得
        $result = $db->query(knjz210kQuery::getListQuery($model->year, $model->subclasscd, "", $model));
        $hrclass = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $whereis = $row["TYPE_GROUP_CD"].$row["GRADE"];
            if (isset($hrclass[$whereis])) {
                $hrclass[$whereis] .= "<br>" . $row["HR_CLASS"];
            } else {
                $hrclass[$whereis] = $row["HR_CLASS"];
            }
        }
        $result = $db->query(knjz210kQuery::getListQuery($model->year, $model->subclasscd, "data", $model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	        {
	            $row["TYPE_GROUP_CDNAME"] = View::alink("knjz210kindex.php",
	                                                    $row["TYPE_GROUP_CD"],
	                                                    "target=right_frame",
	                                                    array("cmd"             => "edit",
	                                                          "TYPE_GROUP_CD"   => $row["TYPE_GROUP_CD"],
	                                                          "SUBCLASSCD"      => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"],
	                                                          "GRADE"           => $row["GRADE"],
	                                                          "TYPE_ASSES_CD"   => $row["TYPE_ASSES_CD"] ))
	                                      . "&nbsp;&nbsp;" . $row["TYPE_GROUP_NAME"];
	            $row["HR_CLASS"] = $hrclass[$row["TYPE_GROUP_CD"].$row["GRADE"]];
	            $arg["data"][] = $row;
	        }
        } else {
	        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	        {
	            $row["TYPE_GROUP_CDNAME"] = View::alink("knjz210kindex.php",
	                                                    $row["TYPE_GROUP_CD"],
	                                                    "target=right_frame",
	                                                    array("cmd"             => "edit",
	                                                          "TYPE_GROUP_CD"   => $row["TYPE_GROUP_CD"],
	                                                          "SUBCLASSCD"      => $row["SUBCLASSCD"],
	                                                          "GRADE"           => $row["GRADE"],
	                                                          "TYPE_ASSES_CD"   => $row["TYPE_ASSES_CD"] ))
	                                      . "&nbsp;&nbsp;" . $row["TYPE_GROUP_NAME"];
	            $row["HR_CLASS"] = $hrclass[$row["TYPE_GROUP_CD"].$row["GRADE"]];
	            $arg["data"][] = $row;
	        }
        }
        
        $result->free();
        Query::dbCheckIn($db);
        
        //対象年度
        $arg["year"] = "&nbsp;対象年度&nbsp;" . $model->year . "&nbsp;";

        //科目コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "extrahtml"   => " onChange=\"btn_submit('list')\" ",
                            "value"       => $model->subclasscd,
                            "options"     => $opt_subclass ));
        $arg["SUBCLASSCD"] = "&nbsp;科目：" . $objForm->ge("SUBCLASSCD");
        //前年度コピーボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "copy_year",
                            "extrahtml"   => " style=\"width:35%;\" onClick=\"btn_submit('copy')\" ",
                            "value"       => "前年度からコピー" ));
        $arg["copy_year"] = "&nbsp;" . $objForm->ge("copy_year");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_flg",
                            "value"     => $year_flg) );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz210kForm1.html", $arg);
    }
}
?>
