<?php

require_once('for_php7.php');

class knjh111aform1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh111aindex.php", "", "edit");

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学籍資格データよりデータを取得
        $db = Query::dbCheckOut();
        if ($model->schregno) {
            $query = knjh111aQuery::getAward($model->schregno, $model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["REGDDATE"]  = str_replace("-", "/", $row["REGDDATE"]);
                $row["URL"] = View::alink(
                    "knjh111aindex.php",
                    $row["REGDDATE"],
                    "target=edit_frame",
                    array("cmd"             => "edit",
                                                  "year"            => $row["YEAR"],
                                                  "REGDDATE"        => $row["REGDDATE"],
                                                  "subclasscd"      => $row["SUBCLASSCD"],
                                                  "seq"             => $row["SEQ"],
                                                  "condition"       => $row["CONDITION_DIV"],
                                                  "SHIKAKU_CD"      => $row["SHIKAKU_CD"],
                                                  "TEST_CD"         => $row["TEST_CD"],
                                                  "MANAGEMENT_FLG"  => $row["MANAGEMENT_FLG"],
                                                  "SCHREGNO"        => $model->schregno
                    )
                );
                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list") {
                $arg["reload"]  = "parent.edit_frame.location.href='knjh111aindex.php?cmd=edit&SCHREGNO={$model->schregno}'";
        }

        View::toHTML($model, "knjh111aForm1.html", $arg);
    }
}
