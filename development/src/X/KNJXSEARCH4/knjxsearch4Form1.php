<?php

require_once('for_php7.php');

class knjxsearch4Form1 {
    function main(&$model) {
        //権限チェック
        $auth = common::SecurityCheck(STAFFCD, $model->programid);
        if ($auth != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        $arg["start"] = $objForm->get_start("knjxsearch4Form1", "POST", "knjxsearch4index.php", "", "knjxsearch4Form1");

        $db     = Query::dbCheckOut();

        //検索ボタン
        $objForm->ae( array("type" 		=> "button",
                            "name"      => "SEARCH_BTN",
                            "value"     => "検索",
                            "extrahtml" => "onclick=\"wopen('knjxsearch4index.php?cmd=search_view','knjxsearch4',0,0,450,250);\""));
        $arg["SEARCH_BTN"] = $objForm->ge("SEARCH_BTN");

        //学校区分抽出
        $schooldiv=="";
        if (isset($model->search_fields["graduate_year"])) {
            $result = $db->query(knjxsearch4Query::GetSchoolDiv($model->search_fields["graduate_year"]));
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 $schooldiv = $row["SCHOOLDIV"];
            }
        }
        if ($schooldiv=="") $schooldiv = $model->control["学校区分"];

        //検索結果表示
        if (isset($model->search_fields)) {
            $result = $db->query(knjxsearch4Query::SearchStudent($model,$model->search_fields,$schooldiv));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            list($path, $cmd) = explode("?cmd=", $model->path);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 array_walk($row, "htmlspecialchars_array");
                 $row["GRADUATE_CLASS"] = $row["HR_NAME"].$row["ATTENDNO"]."番";

                 $row["IMAGE"]    = $image[($row["SEXNUM"]-1)];
                 $row["BIRTHDAY"] = str_replace("-","/",$row["BIRTHDAY"]);

                $objForm->add_element(array("type"      => "checkbox",
                                             "name"     => "chk",
                                             "value"    => $row["SCHREGNO"].",".$row["GRADUATEYEAR"].",".$row["SEMESTER"].",".$row["GRADE"],
                                             "extrahtml"   => "multiple" ));
                $row["CHECK"] = $objForm->ge("chk");

                 $arg["data"][]   = $row;
                 $i++;
            }
            $arg["RESULT"] = "結果　".$i."名";
            $result->free();
            if ($i == 0) {
                $arg["search_result"] = "SearchResult();";
            }
        }

        Query::dbCheckIn($db);

        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "chk_all",
                                    "extrahtml"   => "onClick=\"return check_all();\"" ));

        $arg["CHECK_ALL"] = $objForm->ge("chk_all");

        //hidden(検索条件値を格納する)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRADUATE_YEAR") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRADUATE_CLASS") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "LKANJI") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "LKANA") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxsearch4Form1.html", $arg);
    }
}
?>
