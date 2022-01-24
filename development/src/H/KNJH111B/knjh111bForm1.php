<?php

require_once('for_php7.php');

class knjh111bform1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh111bindex.php", "", "edit");

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            if ($model->cntNotPrintResult > 0) $arg["showScore"] = 1;
        } else {
            if ($model->cntNotPrintRank > 0) $arg["showScore"] = 1;
        }

        //学籍資格データよりデータを取得
        $db = Query::dbCheckOut();
        if($model->schregno) {
            $query = knjh111bQuery::getAward($model->schregno, $model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["REGDDATE"]  = str_replace("-","/",$row["REGDDATE"]);
                $row["COLOR"] = "#ffffff";
                if ($row["IS_OYA"] == "1") {
                    $row["COLOR"] = "#D0B0FF";
                } else if ($row["IS_OYA_KO"] == "1") {
                    $row["COLOR"] = "#1e90ff";
                } else if ($row["IS_KO"] == "1") {
                    $row["COLOR"] = "skyblue";
                }

                $row["URL"] = View::alink("knjh111bindex.php", $row["REGDDATE"], "target=edit_frame",
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
                                                  ));
                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
                $arg["reload"]  = "parent.edit_frame.location.href='knjh111bindex.php?cmd=edit&SCHREGNO={$model->schregno}'";
        }

        View::toHTML($model, "knjh111bForm1.html", $arg);
    }
}

?>
