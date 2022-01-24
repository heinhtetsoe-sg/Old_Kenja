<?php

require_once('for_php7.php');

class knje010bSubForm6 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knje010bindex.php", "", "edit");

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        $arg["TITLE"] = "年度 ／ 年次 ／ 指導上参考となる諸事項";

        //学籍資格データよりデータを取得
        $db = Query::dbCheckOut();
        if($model->schregno) {
            $result = $db->query(knje010bQuery::getTrain_ref($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
                $arg["reload"]  = "parent.edit_frame.location.href='knje010bindex.php?cmd=edit'";
        }

        View::toHTML($model, "knje010bSubForm6.html", $arg);
    }
}

?>
