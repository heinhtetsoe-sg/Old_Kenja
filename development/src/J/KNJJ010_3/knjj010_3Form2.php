<?php

require_once('for_php7.php');

class knjj010_3Form2
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjj010_3index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $Row = $db->getRow(knjj010_3Query::getOneRecord($model, $model->clubcd), DB_FETCHMODE_ASSOC);

        //部クラブコード
        $arg["data"]["CLUBCD"] = $Row["CLUBCD"];

        //部クラブ名
        $arg["data"]["CLUBNAME"] = $Row["CLUBNAME"];

        $query = knjj010_3Query::getSchkind();
        $result = $db->query($query);
        $model->a023 = array();
        $rightSetA023 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->a023[$row["VALUE"]] = $row["LABEL"];
            $rightSetA023[] = $row["VALUE"];
        }

        $query = knjj010_3Query::getClubDetail($model);
        $getClubDetail = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opt_left = $opt_right = array();
        if ($getClubDetail["REMARK1"]) {
            $schoolKindArray = explode(":", $getClubDetail["REMARK1"]);
            foreach ($schoolKindArray as $key => $val) {
                $opt_left[]  = array("label" => $val.":".$model->a023[$val], "value" => $val);
                //A023から設定済みを除いていく
                while (($index = array_search($val, $rightSetA023, true)) !== false) {
                    unset($rightSetA023[$index]);
                }
            }
        }
        foreach ($rightSetA023 as $key => $val) {
            $opt_right[]  = array("label" => $val.":".$model->a023[$val], "value" => $val);
        }

        $extraLeft = "ondblclick=\"move1('right', 'LEFT_KIND', 'RIGHT_KIND', 1);\"";
        $arg["KIND_LIST"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_KIND", "left", $opt_left, $extraLeft." multiple style=\"WIDTH:100%; HEIGHT:170px\"", 10);

        $extraRight = "ondblclick=\"move1('left', 'LEFT_KIND', 'RIGHT_KIND', 1);\"";
        $arg["KIND_LIST"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_KIND", "left", $opt_right, $extraRight." multiple style=\"WIDTH:100%; HEIGHT:170px\"", 10);

        $arg["KIND_LIST"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all2", "≪", "onclick=\"return move1('sel_add_all2', 'LEFT_KIND', 'RIGHT_KIND', 1);\"");
        $arg["KIND_LIST"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add2", "＜", "onclick=\"return move1('left', 'LEFT_KIND', 'RIGHT_KIND', 1);\"");
        $arg["KIND_LIST"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del2", "＞", "onclick=\"return move1('right', 'LEFT_KIND', 'RIGHT_KIND', 1);\"");
        $arg["KIND_LIST"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all2", "≫", "onclick=\"return move1('sel_del_all2', 'LEFT_KIND', 'RIGHT_KIND', 1);\"");

        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return Btn_reset('edit');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $link = REQUESTROOT."/J/KNJJ010/knjj010index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "CHK_CLUBCD", $model->clubcd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if ($model->cmd == "edit2") {
            $arg["reload"] = "parent.left_frame.location.href='knjj010_3index.php?cmd=list';";
        }
                                    
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj010_3Form2.html", $arg);
    }
}
