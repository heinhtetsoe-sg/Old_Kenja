<?php

require_once('for_php7.php');

class knjj020Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjj020Form1", "POST", "knjj020index.php", "", "knjj020Form1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //校種コンボ
        if (!$model->Properties["useClubMultiSchoolKind"] && $model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjj020Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjj020Form1');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        //部クラブ一覧コンボ
        $query = knjj020Query::selectClub($model, $model->year);
        $extra  = "onchange=\"return btn_submit('knjj020Form1');\" ";
        $extra .= ($model->field["CLUBCD"] !== "ALL") ? "AllClearList();" : "";
        makeCmb($objForm, $arg, $db, $query, "CLUBCD", $model->field["CLUBCD"], $extra, 1, "ALL");

        if ($model->field["CLUBCD"] !== "ALL") {

            $arg["DISPLAY_LIST_TO_LIST"] = true;

            //部クラブ顧問一覧取得
            list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
            $result = $db->query(knjj020Query::selectQuery($model));
            $opt_left_id = $opt_left = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $ume = "" ;
                for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                    $ume .= $fuseji;
                }
                if ($fuseji) {
                    $row["FUSE_STAFFCD"] = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - (int)$simo), (int)$simo);
                } else {
                    $row["FUSE_STAFFCD"] = $row["STAFFCD"];
                }
                $opt_left[] = array("label" => $row["FUSE_STAFFCD"]."  ".$row["STAFFNAME_SHOW"], "value" => $row["STAFFCD"]);
                $opt_left_id[] = $row["STAFFCD"];
            }
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','isGroup','noGroup',1)\"";
            $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "isGroup", "left", $opt_left, $extra, 20);

            $opt_right = array();
            //職員一覧取得
            if (is_array($opt_left_id)){
                $result = $db->query(knjj020Query::selectNoGroupQuery($model, $opt_left_id,$model));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $ume = "" ;
                    for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                        $ume .= $fuseji;
                    }
                    if ($fuseji) {
                        $row["FUSE_STAFFCD"] = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - (int)$simo), (int)$simo);
                    } else {
                        $row["FUSE_STAFFCD"] = $row["STAFFCD"];
                    }
                    $opt_right[] = array("label" => $row["FUSE_STAFFCD"]."  ".$row["STAFFNAME_SHOW"], "value" => $row["STAFFCD"]);
                }
            }
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','isGroup','noGroup',1)\"";
            $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "noGroup", "left", $opt_right, $extra, 20);

            $result->free();
        }

        //年度設定
        $result    = $db->query(knjj020Query::selectYearQuery());   
        $opt       = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"], 
                           "value" => $row["YEAR"]);
            if ($row["YEAR"] == $model->year)
                $no_year = 1;
        }
        if ($no_year==0) $model->year = $opt[0]["value"];
        
        //年度コンボボックスを作成する
        $objForm->ae( array("type"        => "select",
                            "name"        => "YEAR",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt));    

                            
        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                             )); 
                        
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                              
        $arg["year"] = array( "VAL"       => $objForm->ge("YEAR")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

        $arg["info"]    = array("TOP"        => "対象年度：");

        //追加ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','isGroup','noGroup',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //追加ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('left','isGroup','noGroup',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //削除ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('right','isGroup','noGroup',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //削除ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','isGroup','noGroup',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //保存ボタンを作成する
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj020Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "ALL") {
        $opt[] = array('label' => "－全て－",
            'value' => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
