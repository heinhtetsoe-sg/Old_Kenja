<?php

require_once('for_php7.php');


class knja122sForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja122sForm1", "POST", "knja122sindex.php", "", "knja122sForm1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->existPem == "NONE") {
            $arg["jscript"] = "notPemClose();";
        }

        if (!$model->cmd) {
            $arg["jscript"] = "collHttps('".REQUESTROOT."', 'https')";
            $model->getPem();
        } else {
            $arg["useApplet"] = "on";
        }

        //パスワード
        $arg["data"]["PASSWD"] = knjCreatePassword($objForm, $model->passwd, "PASSWD", 40, 40, "");
        if ($model->cmd != "sslExe") {
            $arg["APP"]["PASS"]  = $model->cmd == "sslApplet" ? $model->passwd : "";
            $arg["APP"]["RANDM"] = $model->randm;
            $arg["APP"]["STAFF"] = STAFFCD;
            $arg["APP"]["SENDURL"] = $model->setUrl;
            $arg["APP"]["APPHOST"] = '"../../..'.APPLET_ROOT.'/KNJA122S"';
        }

        if ($model->cmd == "sslApplet") {
            $arg["marpColor"] = "red";
        } else {
            $arg["marpColor"] = "white";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja122sForm1.html", $arg); 

    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $db, $model)
{
    //所見処理
    if ($model->syomeiBtn) {
        $query = knja122sQuery::getGdat();
        $result = $db->query($query);
        $gdatArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gdatArray[] = $row["SCHOOL_KIND"];
        }
        $query = knja122sQuery::getSchoolmst();
        $schoolMst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query = knja122sQuery::getNameMstCd2("Z001", $schoolMst["SCHOOLDIV"]);
        $Z001 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $linkSaki = array("P" => array("name" => "小学", "ikkatu" => "124PS", "syomei" => "120PS", "cancel" => "123PS"),
                          "J" => array("name" => "中学", "ikkatu" => "124JS", "syomei" => "120JS", "cancel" => "123JS"),
                          "H" => array("name" => "高校", "ikkatu" => $Z001["NAMESPARE1"] == "1" ? "124MS" : "124S", "syomei" => $Z001["NAMESPARE1"] == "1" ? "120MS" : "120S", "cancel" => $Z001["NAMESPARE1"] == "1" ? "123MS" : "123S")
                         );
        if (get_count($gdatArray) == "1") {
            $arg["gdatOnly"] = "1";
            $linkId = $linkSaki[$gdatArray[0]]["ikkatu"];
            $linkIdLow = strtolower($linkId);
            $paraUrl = str_replace("122S", $linkId, $model->setUrl);
            $paraUrl = str_replace("122s", $linkIdLow, $paraUrl);
            $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?EXE_TYPE=PRINCIPAL&setUrl=".$paraUrl."&RNDM=".$model->randm."&SEND_AUTH=".AUTHORITY."'";
            $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_syoken_ikkatu"] = knjCreateBtn($objForm, "btn_syoken_ikkatu", "所見一括署名", $extra);

            $linkId = $linkSaki[$gdatArray[0]]["syomei"];
            $linkIdLow = strtolower($linkId);
            $paraUrl = str_replace("122S", $linkId, $model->setUrl);
            $paraUrl = str_replace("122s", $linkIdLow, $paraUrl);
            $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?EXE_TYPE=PRINCIPAL&setUrl=".$paraUrl."&RNDM=".$model->randm."&SEND_AUTH=".AUTHORITY."'";
            $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_syoken"] = knjCreateBtn($objForm, "btn_syoken", "所見署名", $extra);

            $linkId = $linkSaki[$gdatArray[0]]["cancel"];
            $linkIdLow = strtolower($linkId);
            $paraUrl = str_replace("122S", $linkId, $model->setUrl);
            $paraUrl = str_replace("122s", $linkIdLow, $paraUrl);
            $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?EXE_TYPE=PRINCIPAL&setUrl=".$paraUrl."&RNDM=".$model->randm."&SEND_AUTH=".AUTHORITY."'";
            $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_syoken_cancel"] = knjCreateBtn($objForm, "btn_syoken_cancel", "署名キャンセル", $extra);
        } else {
            foreach ($gdatArray as $key => $val) {
                $arg["gdat".$val] = "1";
                $setName = $linkSaki[$val]["name"];
                $linkId = $linkSaki[$val]["ikkatu"];
                $linkIdLow = strtolower($linkId);
                $paraUrl = str_replace("122S", $linkId, $model->setUrl);
                $paraUrl = str_replace("122s", $linkIdLow, $paraUrl);
                $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?EXE_TYPE=PRINCIPAL&setUrl=".$paraUrl."&RNDM=".$model->randm."&SEND_AUTH=".AUTHORITY."'";
                $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
                $arg["button"]["btn_syoken_ikkatu".$val] = knjCreateBtn($objForm, "btn_syoken_ikkatu".$val, $setName."所見一括署名", $extra);

                $linkId = $linkSaki[$val]["syomei"];
                $linkIdLow = strtolower($linkId);
                $paraUrl = str_replace("122S", $linkId, $model->setUrl);
                $paraUrl = str_replace("122s", $linkIdLow, $paraUrl);
                $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?EXE_TYPE=PRINCIPAL&setUrl=".$paraUrl."&RNDM=".$model->randm."&SEND_AUTH=".AUTHORITY."'";
                $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
                $arg["button"]["btn_syoken".$val] = knjCreateBtn($objForm, "btn_syoken".$val, $setName."所見署名", $extra);

                $linkId = $linkSaki[$val]["cancel"];
                $linkIdLow = strtolower($linkId);
                $paraUrl = str_replace("122S", $linkId, $model->setUrl);
                $paraUrl = str_replace("122s", $linkIdLow, $paraUrl);
                $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?EXE_TYPE=PRINCIPAL&setUrl=".$paraUrl."&RNDM=".$model->randm."&SEND_AUTH=".AUTHORITY."'";
                $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
                $arg["button"]["btn_syoken_cancel".$val] = knjCreateBtn($objForm, "btn_syoken_cancel".$val, $setName."署名キャンセル", $extra);
            }
        }

    }
    //認証
    $extra = "onclick=\"return btn_signSubmit('sslApplet', '".$model->randm."', '".STAFFCD."');\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "認 証", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJA122S");
	//2010.02.09
    knjCreateHidden($objForm, "SIGNATURE", $model->signature);
    knjCreateHidden($objForm, "GOSIGN", $model->gosign);
    knjCreateHidden($objForm, "RANDM", $model->randm);
}

?>
