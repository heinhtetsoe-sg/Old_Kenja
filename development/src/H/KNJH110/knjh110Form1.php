<?php

require_once('for_php7.php');

class knjh110form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh110index.php", "", "edit");

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        
        //プロパティの値による表示
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        
        //学籍資格データよりデータを取得
        $db = Query::dbCheckOut();
        if($model->schregno)
        {
            $result = $db->query(knjh110Query::getAward($model, $model->schregno));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row["REGDDATE"]  = str_replace("-","/",$row["REGDDATE"]);

                //今年度以外は、リンクなしにする
                if ($row["YEAR"] == CTRL_YEAR) {
			        //教育課程対応
			        if ($model->Properties["useCurriculumcd"] == '1') {
	                    $row["URL"] = View::alink("knjh110index.php", $row["REGDDATE"], "target=edit_frame",
	                                                array("cmd"         => "edit",
	                                                      "REGDDATE"    => $row["REGDDATE"],
	                                                      "subclasscd"  => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"],
	                                                      "seq"         => $row["SEQ"],
	                                                      "condition"   => $row["CONDITION_DIV"],
	                                                      "SCHREGNO"    => $model->schregno
	                                                      ));
                    } else {
	                    $row["URL"] = View::alink("knjh110index.php", $row["REGDDATE"], "target=edit_frame",
	                                                array("cmd"         => "edit",
	                                                      "REGDDATE"    => $row["REGDDATE"],
	                                                      "subclasscd"  => $row["SUBCLASSCD"],
	                                                      "seq"         => $row["SEQ"],
	                                                      "condition"   => $row["CONDITION_DIV"],
	                                                      "SCHREGNO"    => $model->schregno
	                                                      ));
                    }
                } else {
                    $row["URL"] = $row["REGDDATE"];
                }

                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
                $arg["reload"]  = "parent.edit_frame.location.href='knjh110index.php?cmd=edit&SCHREGNO=$model->schregno'";
        }
        
        View::toHTML($model, "knjh110Form1.html", $arg);
    }
}        

?>
