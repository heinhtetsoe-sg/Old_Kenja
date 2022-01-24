<?php

require_once('for_php7.php');


class knja133sForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja133sForm1", "POST", "knja133sindex.php", "", "knja133sForm1");

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
            $arg["APP"]["APPHOST"] = '"../../..'.APPLET_ROOT.'/KNJA133S"';
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
        View::toHTML($model, "knja133sForm1.html", $arg); 

    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $db, $model)
{
    //所見処理
    if ($model->syomeiBtn) {
        $query = knja133sQuery::getGdat();
        $result = $db->query($query);
        $gdatArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gdatArray[] = $row["SCHOOL_KIND"];
        }
        $query = knja133sQuery::getSchoolmst();
        $schoolMst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query = knja133sQuery::getNameMstCd2("Z001", $schoolMst["SCHOOLDIV"]);
        $Z001 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $linkSaki = array("P" => array("name" => "小学", "syomei" => "133P"),
                          "J" => array("name" => "中学", "syomei" => "133J"),
                          "H" => array("name" => "高校", "syomei" => $Z001["NAMESPARE1"] == "1" ? "133M" : ($Z001["NAMESPARE1"] == "2" ? "130D" : "130B"))
                         );
        if (get_count($gdatArray) == "1") {
            $arg["gdatOnly"] = "1";
            $linkId = $linkSaki[$gdatArray[0]]["syomei"];
            $linkIdLow = strtolower($linkId);
            $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?INEI=1&EXE_TYPE=PRINCIPAL&setUrl=".$model->setUrl."&RNDM=".$model->randm."'";
            $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "指導要録印刷", $extra);

        } else {
            foreach ($gdatArray as $key => $val) {
                $arg["gdat".$val] = "1";
                $setName = $linkSaki[$val]["name"];
                $linkId = $linkSaki[$val]["syomei"];
                $linkIdLow = strtolower($linkId);
                $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?INEI=1&EXE_TYPE=PRINCIPAL&setUrl=".$model->setUrl."&RNDM=".$model->randm."'";
                $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
                $arg["button"]["btn_print".$val] = knjCreateBtn($objForm, "btn_print".$val, $setName."指導要録印刷", $extra);

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
    knjCreateHidden($objForm, "PRGID", "KNJA133S");
    //2010.02.09
    knjCreateHidden($objForm, "SIGNATURE", $model->signature);
    knjCreateHidden($objForm, "GOSIGN", $model->gosign);
    knjCreateHidden($objForm, "RANDM", $model->randm);
}

?>
