<?php

require_once('for_php7.php');

class knjm250Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $db         = Query::dbCheckOut();

        //年度
        $opt_year  = array();
        $opt_year[0] = array('label' => CTRL_YEAR,
                             'value' => CTRL_YEAR);
        $opt_year[1] = array('label' => CTRL_YEAR +1 ,
                             'value' => CTRL_YEAR +1 );

        if ($model->ObjYear == "") $model->ObjYear = $opt_year[0]["value"] ;
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["TOP"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->ObjYear, $opt_year, $extra, 1);

        $opt_sub  = array();
        //データセット
        $result = $db->query(knjm250Query::ReadQuery($model));
        $subcnt = 0;
        while ($RowR = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt_sub[$subcnt] = array('label' => $RowR["CHAIRNAME"],
                                      'value' => $RowR["CHAIRCD"].$RowR["SUBCLASSCD"]);

            $get_data[] = array( "SUBCD"        => $subcnt,             //ID設定
                                 "SUBCDNAME"    => $period);

            $subcnt++;
        }
        $result->free();

        if ($model->sub == "") $model->sub = $opt_sub[0]["value"];
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["TOP"]["SELSUB"] = knjCreateCombo($objForm, "SELSUB", $model->sub, $opt_sub, $extra, 1);

        //NAME_MST
        $opt_sem = array();
        $result = $db->query(knjm250Query::GetName($model,"M002"));
        while ($RowN = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sem[] = array('label' => $RowN["NAME1"],
                               'value' => $RowN["NAMECD2"]);
        }
        $result->free();

        $result  = $db->query(knjm250Query::Getperiod($model,$model->sub));
        $peri   = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $period = $peri["REP_SEQ_ALL"];
        $model->repcntall = $period;
        $result->free();

        for ($kai = 0; $kai < $period; $kai++) {

            //ID
            $Row["SUBCD"] = $period - ($period - $kai);

            //回数

            $Row["SUBCDNAME"] = "第".($Row["SUBCD"] + 1)."回";

            $result      = $db->query(knjm250Query::GetRepdata($model,($Row["SUBCD"] + 1)));
            $repstan     = $result->fetchRow(DB_FETCHMODE_ASSOC);

            $result->free();

            //レポート区分

            if (!$repstan["REPORTDIV"]) $repstan["REPORTDIV"] = 1;
            if ($model->cmd != "reset") {
                $model->setdata["REPDIV"][$kai] = $repstan["REPORTDIV"];
            } else if (!$model->setdata["REPDIV"][$kai]) {
                $model->setdata["REPDIV"][$kai] = $repstan["REPORTDIV"];
            }
            $extra = " id=\"".$Row["SUBCD"]."\"";
            $Row["REPDIV"] = knjCreateCombo($objForm, "REPDIV".$kai, $model->setdata["REPDIV"][$kai], $opt_sem, $extra, 1);

            //日付
            if (!$repstan["STANDARD_DATE"]) $repstan["STANDARD_DATE"] = str_replace("-","/",CTRL_DATE);
            if (!$repstan["DEADLINE_DATE"]) $repstan["DEADLINE_DATE"] = '';
            if (!$model->setdata["BASEDAY"][$kai]) $model->setdata["BASEDAY"][$kai] = str_replace("-","/",$repstan["STANDARD_DATE"]);
            if (!$model->setdata["ENDDAY"][$kai] ) $model->setdata["ENDDAY"][$kai]  = str_replace("-","/",$repstan["DEADLINE_DATE"]);
            $Row["BASEDAY"] = View::popUpCalendar($objForm  ,"BASEDAY".$kai ,$model->setdata["BASEDAY"][$kai],this);
            $Row["ENDDAY"]  = View::popUpCalendar($objForm  ,"ENDDAY".$kai  ,$model->setdata["ENDDAY"][$kai],this);

            if (!$model->setdata["REMARK"][$kai]) $model->setdata["REMARK"][$kai] = $repstan["REMARK"];

            //備考
            $extra = "onblur=\"check(this)\" id=\"".$Row["SUBCD"]."\"";
            $Row["REMARK"] = knjCreateTextBox($objForm, $model->setdata["REMARK"][$kai], "REMARK".$kai, 60, 60, $extra);

            $arg["data"][] = $Row;

        }

        //ボタン作成
        $extra = "onClick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onClick=\"btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd", $model->cmd);

        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm250index.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm250Form1.html", $arg);
    }
}
?>
