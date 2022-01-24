<?php

require_once('for_php7.php');

class knje010bSubForm5 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knje010bindex.php", "", "edit");

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        if ($model->Properties["useQualifiedMst"] == '1') {
            $arg["TITLE"] = "取得日 ／ 区分 ／ 名称 ／ 級・段位 ／ 備考";
        } else {
            $arg["TITLE"] = "取得日 ／ 区分 ／ 内容 ／ 備考";
        }

        //学籍資格データよりデータを取得
        $db = Query::dbCheckOut();
        if($model->schregno) {
            $result = $db->query(knje010bQuery::getAward($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["REGDDATE"]  = str_replace("-","/",$row["REGDDATE"]);

                if ($model->Properties["useQualifiedMst"] == '1') {
                    $arg["data"][] = array("LIST" => "{$row["REGDDATE"]}／{$row["CONDITION_DIV"]}／{$row["QUALIFIED_NAME"]}／{$row["RANK"]}／{$row["REMARK"]}");
                } else {
                    $arg["data"][] = array("LIST" => "{$row["REGDDATE"]}／{$row["CONDITION_DIV"]}／{$row["CONTENTS"]}／{$row["REMARK"]}");
                }
            }
        }
        Query::dbCheckIn($db);

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn(&$objForm, "btn_back", "戻る", $extra);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
                $arg["reload"]  = "parent.edit_frame.location.href='knje010bindex.php?cmd=edit'";
        }

        View::toHTML($model, "knje010bSubForm5.html", $arg);
    }
}

?>
