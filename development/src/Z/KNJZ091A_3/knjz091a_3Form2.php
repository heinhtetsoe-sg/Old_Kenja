<?php

require_once('for_php7.php');

class knjz091a_3Form2
{
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz091a_3index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->prischoolClassCd)) {
            $Row = knjz091a_3Query::getRow($model, $model->prischoolClassCd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //塾コード
        $query = knjz091a_3Query::getPriname($model);
        $setPriName = $db->getOne($query);
        $arg["data"]["PRISCHOOLCD"] = $setPriName;

        //教室コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PRISCHOOL_CLASS_CD"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_CLASS_CD"], "PRISCHOOL_CLASS_CD", 7, 7, $extra);

        //出身教室名
        $extra = "";
        $arg["data"]["PRISCHOOL_NAME"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_NAME"], "PRISCHOOL_NAME", 50, 75, $extra);

        //出身教室カナ
        $extra = "";
        $arg["data"]["PRISCHOOL_KANA"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_KANA"], "PRISCHOOL_KANA", 50, 75, $extra);

        //出身校舎長氏名
        $extra = "";
        $arg["data"]["PRINCNAME"] = knjCreateTextBox($objForm, $Row["PRINCNAME"], "PRINCNAME", 30, 60, $extra);

        //出身校舎長氏名表示用
        $extra = "";
        $arg["data"]["PRINCNAME_SHOW"] = knjCreateTextBox($objForm, $Row["PRINCNAME_SHOW"], "PRINCNAME_SHOW", 20, 30, $extra);

        //出身校舎長氏名かな
        $extra = "";
        $arg["data"]["PRINCKANA"] = knjCreateTextBox($objForm, $Row["PRINCKANA"], "PRINCKANA", 80, 120, $extra);

        //地区コード
        $opt = array();

        //地区コード
        $result = $db->query(knjz091a_3Query::getDistinct());
        $opt2 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt2[] = array("label" => $row["NAMECD2"]."  ".$row["NAME1"], 
                           "value"  => $row["NAMECD2"]);
        }
        $result->free();

        $extra = "";
        $arg["data"]["DISTRICTCD"] = knjCreateCombo($objForm, "DISTRICTCD", $Row["DISTRICTCD"], $opt2, $extra, 1);

        //出身教室郵便番号
        $arg["data"]["PRISCHOOL_ZIPCD"] = View::popUpZipCode($objForm, "PRISCHOOL_ZIPCD", $Row["PRISCHOOL_ZIPCD"],"PRISCHOOL_ADDR1");
        
        //出身教室住所１
        $extra = "";
        $arg["data"]["PRISCHOOL_ADDR1"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_ADDR1"], "PRISCHOOL_ADDR1", 50, 90, $extra);

        //出身教室住所２
        $extra = "";
        $arg["data"]["PRISCHOOL_ADDR2"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_ADDR2"], "PRISCHOOL_ADDR2", 50, 90, $extra);

        //出身教室電話番号
        $extra = "";
        $arg["data"]["PRISCHOOL_TELNO"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_TELNO"], "PRISCHOOL_TELNO", 14, 14, $extra);

        //出身教室FAX番号
        $extra = "";
        $arg["data"]["PRISCHOOL_FAXNO"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_FAXNO"], "PRISCHOOL_FAXNO", 14, 14, $extra);

        //路線
        for ($i = 1; $i <= 5; $i++) {
            $rosen = "ROSEN_" . $i;
            $extra = "disabled=\"true\" class=\"right_side\" style=\"width:270px;\"";

            $hidden_rosen = "HIDDEN_ROSEN_" . $i;

            $rosen_cd = $Row[$hidden_rosen] ? $Row[$hidden_rosen] : $Row[$rosen];

            $query = knjz091a_3Query::getStationName($rosen_cd);
            $rosen_mei = $db->getOne($query);

            $arg["data"][$rosen] = knjCreateTextBox($objForm, $rosen_mei, $rosen, 50, 75, $extra);
            knjCreateHidden($objForm, $hidden_rosen, $rosen_cd);

            //駅名、かな
            if ($i == "1" || $i == "2") {
                $extra = "";
                $arg["data"]["NEAREST_STATION_NAME".$i] = knjCreateTextBox($objForm, $Row["NEAREST_STATION_NAME".$i], "NEAREST_STATION_NAME".$i, 50, 75, $extra);
                $arg["data"]["NEAREST_STATION_KANA".$i] = knjCreateTextBox($objForm, $Row["NEAREST_STATION_KANA".$i], "NEAREST_STATION_KANA".$i, 50, 75, $extra);
            }
        }

        //DM不可checkbox
        $checkFlg = ($Row["DIRECT_MAIL_FLG"] == "1") ? " checked": "";
        $extra = "id=\"DIRECT_MAIL_FLG\"";
        $arg["data"]["DIRECT_MAIL_FLG"] = knjCreateCheckBox($objForm, "DIRECT_MAIL_FLG", "1", $extra.$checkFlg);

        /**ボタン**/
        //路線選択
        $extra = "onclick=\"Page_jumper2();\"";
        $arg["button"]["btn_suport"] = knjCreateBtn($objForm, 'btn_suport', '路線選択', $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"btn_back();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //校舎登録
        $extra  = " onClick=\" wopen('".REQUESTROOT."/Z/KNJZ091A_3/knjz091a_3index.php?";
        $extra .= "PRISCHOOLCD=".$Row["PRISCHOOLCD"]."&cmd=";
        $extra .= "&AUTH=".$model->auth;
        $extra .= "&CALLID=KNJZ091A_3";
        $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_z091a_3"] = knjCreateBtn($objForm, "btn_z091a_3", "校舎登録", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz091a_3index.php?cmd=list';";
        }
                                    
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz091a_3Form2.html", $arg);
    }
} 
?>
