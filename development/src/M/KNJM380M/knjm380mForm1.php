<?php

require_once('for_php7.php');

/********************************************************************/
/* スクーリング回数登録                             山城 2005/03/07 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm380mForm1
{
    public function main(&$model)
    {

        //セキュリティーチェック
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm      = new form();
        $db           = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("list", "POST", "knjm380mindex.php", "", "edit");

        //年コンボボックス
        $result = $db->query(knjm380mQuery::getSubClasyearQuery());
        $opt_year = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"],
                                  "value" => $row["YEAR"]);
        }
        if (!isset($model->Year)) {
            $model->Year = CTRL_YEAR;
        }

        $objForm->ae(array("type"    => "select",
                            "name"    => "GrYEAR",
                            "size"    => "1",
                            "value"   => $model->Year,
                            "extrahtml" => "onChange=\"btn_submit('init');\" ",
                            "options" => $opt_year));

        $arg["GrYEAR"] = $objForm->ge("GrYEAR");

        //前年度コピーボタン
        $extra = " onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //講座一覧
        $db = Query::dbCheckOut();
        $result = $db->query(knjm380mQuery::readQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            if ($model->Properties["useCurriculumcd"] == "1") {
                $row["CHAIRCD_SHOW"] = View::alink(
                    "knjm380mindex.php",
                    $row["CHAIRNAME"],
                    "target=\"right_frame\"",
                    array("cmd"      => "edit",
                                                  "CLASSCD" => $row["CLASSCD"],
                                                  "SCHOOL_KIND" => $row["SCHOOL_KIND"],
                                                  "CURRICULUM_CD" => $row["CURRICULUM_CD"],
                                                  "SUBCLASSCD" => $row["SUBCLASSCD"],
                                                  "CHAIRCD" => $row["CHAIRCD"],
                                                  "CHAIRCD_SHOW"     => $row["CHAIRNAME"],
                                                  "GetYear"     => $model->Year,
                                           )
                );
            } else {
                $row["CHAIRCD_SHOW"] = View::alink(
                    "knjm380mindex.php",
                    $row["CHAIRNAME"],
                    "target=\"right_frame\"",
                    array("cmd"         => "edit",
                                                  "SUBCLASSCD" => $row["SUBCLASSCD"],
                                                  "CHAIRCD" => $row["CHAIRCD"],
                                                  "CHAIRCD_SHOW"     => $row["CHAIRNAME"],
                                                  "GetYear"     => $model->Year,
                                           )
                );
            }
            /*
                        $row["CHAIRCD_SHOW"] = View::alink("knjm380mindex.php",
                                                    $row["CHAIRNAME"],
                                                    "target=\"right_frame\"",
                                                    array("cmd"      => "edit",
                                                          "SUBCLASSCD" => $row["SUBCLASSCD"],
                                                          "CHAIRCD" => $row["CHAIRCD"],
                                                          "CHAIRCD_SHOW"     => $row["CHAIRNAME"]
                                                   ));
            */
            $row["SUBCNT"] = $row["SCH_SEQ_ALL"];
            $row["SUBCHECK"] = $row["SCH_SEQ_MIN"];
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "init") {
            $path = REQUESTROOT ."/M/KNJM380M/knjm380mindex.php?cmd=edit&GetYear=".$model->Year;
            $arg["reload"] = "window.open('$path','right_frame');";
        }

        View::toHTML($model, "knjm380mForm1.html", $arg);
    }
}
