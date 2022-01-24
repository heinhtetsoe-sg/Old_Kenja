<?php

require_once('for_php7.php');


class knjaopsForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjaopsForm1", "POST", "knjaopsindex.php", "", "knjaopsForm1");

    //DB接続
    $db = Query::dbCheckOut();

    if ($model->existPem == "NONE") {
        $arg["jscript"] = "notPemClose();";
    }

    if (!$model->cmd) {
        $arg["jscript"] = "collHttps('".REQUESTROOT."', 'https')";
    }

    //パスワード
    $arg["data"]["PASSWD"] = knjCreatePassword($objForm, $passwd, "PASSWD", 40, 40, "");

    //ボタン作成
    makeBtn($objForm, $arg, $db, $model);

    //hiddenを作成する
    makeHidden($objForm);

    //DB切断
    Query::dbCheckIn($db);

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjaopsForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $db, $model)
{
    //所見処理
    if ($model->syomeiBtn) {
        $query = knjaopsQuery::getGdat();
        $result = $db->query($query);
        $gdatArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gdatArray[] = $row["SCHOOL_KIND"];
        }
        $query = knjaopsQuery::getSchoolmst();
        $schoolMst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query = knjaopsQuery::getNameMstCd2("Z001", $schoolMst["SCHOOLDIV"]);
        $Z001 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $linkSaki = array("P" => array("name" => "小学", "syomei" => "OPPS"),
                          "J" => array("name" => "中学", "syomei" => "OPJS"),
                          "H" => array("name" => "高校", "syomei" => $Z001["NAMESPARE1"] == "1" ? "OPMS" : "OPHS")
                         );
        if (get_count($gdatArray) == "1") {
            $arg["gdatOnly"] = "1";
            $linkId = $linkSaki[$gdatArray[0]]["syomei"];
            $linkIdLow = strtolower($linkId);
            $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?EXE_TYPE=CHARGE&setUrl=".$model->setUrl."&RNDM=".$model->randm."&SEND_AUTH=".AUTHORITY."'";
            $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_syoken"] = knjCreateBtn($objForm, "btn_syoken", "所見署名", $extra);

        } else {
            foreach ($gdatArray as $key => $val) {
                $arg["gdat".$val] = "1";
                $setName = $linkSaki[$val]["name"];
                $linkId = $linkSaki[$val]["syomei"];
                $linkIdLow = strtolower($linkId);
                $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA{$linkId}/knja{$linkIdLow}index.php?EXE_TYPE=CHARGE&setUrl=".$model->setUrl."&RNDM=".$model->randm."&SEND_AUTH=".AUTHORITY."'";
                $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";
                $arg["button"]["btn_syoken".$val] = knjCreateBtn($objForm, "btn_syoken".$val, $setName."所見署名", $extra);

            }
        }

    }
    //認証
    $extra = "onclick=\"return btn_submit('sslExe');\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "認 証", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJAOPS");
}

?>
