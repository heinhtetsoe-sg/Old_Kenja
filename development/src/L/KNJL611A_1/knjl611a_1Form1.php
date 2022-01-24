<?php

require_once('for_php7.php');

class knjl611a_1Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl611a_1index.php", "", "main");

        //一覧表示
        if ((!isset($model->warning))) {
            //データを取得
            $Row = knjl611a_1Query::get_edit_data($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //受験番号
        $arg["TOP"]["EXAMNO"] = $model->examno;

        //氏名(志願者)
        $arg["TOP"]["NAME"] = $Row["NAME"];
        
        //入試制度
        $applicantdiv_name = $db->getOne(knjl611a_1Query::get_name_cd($model->year, "L003", $model->applicantdiv));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //------------------------------内申-------------------------------------

        //5科合計
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); dataCheck(this, '', 'L067') \" ";
        $arg["data"]["TOTAL5"] = knjCreateTextBox($objForm, $Row["TOTAL5"], "TOTAL5", 2, 2, $extra);
        //欠席日数
        $nmL067 = getNameMst($db, $model, "L067");
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); dataCheck(this, '{$nmL067}', 'L067') \" ";
        $arg["data"]["ABSENCE_DAYS3"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS3"], "ABSENCE_DAYS3", 1, 1, $extra);
        //生徒会/クラス活動
        $nmL068 = getNameMst($db, $model, "L068");
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); dataCheck(this, '{$nmL068}', 'L068') \" ";
        $arg["data"]["SPECIALACTREC"] = knjCreateTextBox($objForm, $Row["SPECIALACTREC"], "SPECIALACTREC", 2, 2, $extra);
        //特別活動
        $nmL069 = getNameMst($db, $model, "L069");
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); dataCheck(this, '{$nmL069}', 'L069') \" ";
        $arg["data"]["TOTALSTUDYTIME"] = knjCreateTextBox($objForm, $Row["TOTALSTUDYTIME"], "TOTALSTUDYTIME", 1, 1, $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL611A/knjl611aindex.php?cmd=reference&SEND_PRGID=KNJL611A_1&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_TESTDIV=".$model->testdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl611a_1Form1.html", $arg);
    }
}
function getNameMst($db, $model, $namecd) {
    $retVal = "";
    $sep = "";
    $query = knjl611a_1Query::getCheckNamecd($model->year, $namecd);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $retVal .= $sep.$row["NAMECD2"];
        $sep = ",";
    }
    $result->free();
    
    return $retVal;
}
?>