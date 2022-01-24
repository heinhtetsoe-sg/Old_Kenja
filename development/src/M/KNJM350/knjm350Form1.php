<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjm350Form1.php 56590 2017-10-22 13:01:54Z maeshiro $

/********************************************************************/
/* レポート基本情報登録                             山城 2005/03/08 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：変更内容                                 name yyyy/mm/dd */
/********************************************************************/

class knjm350Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $db         = Query::dbCheckOut();

        /*------------------*/
        /*    日付データ    */
        /*------------------*/
        $arg["TOP"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date));

        /*------------------*/
        /*       年度       */
        /*------------------*/
        $opt_year  = array();
        $opt_year[0] = array('label' => CTRL_YEAR,
                             'value' => CTRL_YEAR);
        $opt_year[1] = array('label' => CTRL_YEAR +1 ,
                             'value' => CTRL_YEAR +1 );

        if ($model->ObjYear == "") $model->ObjYear = $opt_year[0]["value"] ;
        $objForm->ae( array("type"       => "select",
                            "name"       => "YEAR",
                            "size"       => "1",
                            "value"      => $model->ObjYear,
                            "extrahtml"  => "onChange=\"return btn_submit('change');\"",
                            "options"    => $opt_year));

        $arg["TOP"]["YEAR"]     = $objForm->ge("YEAR");

        /*------------------*/
        /* 科目データセット */
        /*------------------*/
        $opt_sub  = array();
        $result = $db->query(knjm350Query::GetSubclass($model));

        while($RowR = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $opt_sub[] = array('label' => $RowR["SUBCLASSNAME"],
                               'value' => $RowR["SUBCLASSCD"]);

        }
        $result->free();

        if ($model->sub == "") $model->sub = $opt_sub[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "SELSUB",
                            "size"       => "1",
                            "value"      => $model->sub,
                            "extrahtml"  => "onChange=\"return btn_submit('change');\"",
                            "options"    => $opt_sub));

        $arg["TOP"]["SELSUB"]     = $objForm->ge("SELSUB");


        /*------------------*/
        /*  データ数帳票用  */
        /*------------------*/
        $objForm->ae( array("type"       => "hidden",
                            "name"       => "CNT",
                            "value"      => $model->repcntall));
        $arg["TOP"]["CNT"]     = $objForm->ge("CNT");

        /*------------------*/
        /* チェックボックス */
        /*------------------*/
        if($model->allcheck == "on")
        {
            $check_all = "checked";
        }else {
            $check_all = "";
        }

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "ALLCHECK",
                            "value"     => "on",
                            "extrahtml" => "$check_all onClick =\" return btn_submit('allcheck')\"" ) );

        $arg["TOP"]["ALLCHECK"]     = $objForm->ge("ALLCHECK");

        /*------------------*/
        /* メインデータ設定 */
        /*------------------*/
        $kai = 0;
        $checkary = array();
        $checon   = array();
        
        //前の画面でチェックされていたデータを取得（2006/07/28伊集始まり）
        $setdata_i = array();
        for ($i = 0; $i < get_count($model->kaisu_i["DELCHK"]) ; $i++){
            $setdata_i[$i] = explode(":",$model->kaisu_i["DELCHK"][$i]);
        }
        //（2006/07/28編集終り）

        $result  = $db->query(knjm350Query::GetReportData($model));
        while($maindata = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //ID
            $Row["SUBCD"] = $kai;
            /*------------------*/
            /* チェックボックス */
            /*------------------*/
            $check_del = "";
            if (!isset($model->warning) && $model->cmd == 'read'){
//              $checkary = explode(":",$model->kaisu["DELCHK"][$kai]);     //2006/07/28伊集コメント化（４行）
//              if ( $checkary[0] == "on"){
//                  $check_del = "checked";
//              }
                //前の画面でチェックされていたデータと同じデータをチェックする（2006/07/28伊集始まり）
                for ($j = 0; $j < get_count($setdata_i) ; $j++){
                    if ( $setdata_i[$j][0] == "on" && $setdata_i[$j][1] == $maindata["SCHREGNO"] && $setdata_i[$j][2] == $maindata["STANDARD_SEQ"] && $setdata_i[$j][3] == ($maindata["REPRESENT_SEQ"] + 1) && $setdata_i[$j][4] == $maindata["RECEIPT_DATE"]){
                        $check_del = "checked";
                        break;
                    }
                }
                //（2006/07/28編集終り）
            }
            if($model->allcheck == "on"){
                $check_del = "checked";
            }
            $objForm->ae( array("type"      => "checkbox",
                                "name"      => "DELCHK".$kai,
                                "value"     => "on".":".$maindata["SCHREGNO"].":".$maindata["STANDARD_SEQ"].":".($maindata["REPRESENT_SEQ"] + 1).":".$maindata["RECEIPT_DATE"].":".$maindata["REPORTDIV"].":".$kai,
                                "extrahtml" => $check_del ) );

            $Row["DELCHK"]   = $objForm->ge("DELCHK".$kai);
            $Row["DELID"] = "DEL".$kai;

            /*------------------*/
            /*       印刷       */
            /*------------------*/
            if ($maindata["REPRESENT_PRINT"] == 1){
                $Row["PURINTDIV"] = "済み";
            }else {
                $Row["PURINTDIV"] = "";
            }

            /*------------------*/
            /*     学籍番号     */
            /*------------------*/
            $Row["SCHREGNO"] = $maindata["SCHREGNO"];

            /*------------------*/
            /*     生徒氏名     */
            /*------------------*/
            $Row["SCHREGNAME"] = $maindata["NAME_SHOW"];

            /*------------------*/
            /*       回数       */
            /*------------------*/
            $Row["KAI"] = "第".$maindata["STANDARD_SEQ"]."回";

            /*------------------*/
            /*      再提出      */
            /*------------------*/
            $Row["SAI"] = ($maindata["REPRESENT_SEQ"] + 1)."回目";

            /*------------------*/
            /*       備考       */
            /*------------------*/
            $Row["REMARK"] = $maindata["REMARK"];

            $arg["data"][$kai] = $Row;

            $kai++;
        }
        $result->free();

        $model->repcntall = $kai;

        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        
        $arg["btn_update"] = $objForm->ge("btn_update");
        $arg["btn_end"]    = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE));

        $arg["TOP"]["DBNAME"]     = $objForm->ge("DBNAME");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM350"));

        $arg["TOP"]["useCurriculumcd"] = knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["TOP"]["PRGID"]     = $objForm->ge("PRGID");

        Query::dbCheckIn($db);

        if (!isset($model->warning) && $model->cmd == 'read'){
            $model->cmd = 'main';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm350index.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm350Form1.html", $arg); 
    }
}
?>
