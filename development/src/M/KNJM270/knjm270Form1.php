<?php

require_once('for_php7.php');

/********************************************************************/
/* レポート評価登録                                 山城 2005/03/24 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm270Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm270index.php", "", "main");

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");
        //チェック用hidden
        $objForm->ae( array("type"      => "hidden",
                            "value"     => $model->Date,
                            "name"      => "DEFOULTDATE") );

        //添削者
        $opt_staf = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjm270Query::selectStaff($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_staf[] = array("label" => $row["STAFFNAME"],
                                "value" => $row["STAFFCD"]);
        }
        if (!$model->field["STAFF"] && $model->User == 0){
            $model->field["STAFF"] = $opt_staf[0]["value"];
        }else if(!$model->field["STAFF"] && $model->User == 1) {
            $model->field["STAFF"] = STAFFCD;
        }

        if(!$opt_staf[0]) {
            $arg["Closing"] = " closing_window('MSG300');";
        }

        $objForm->ae( array("type"    => "select",
                            "name"    => "STAFF",
                            "size"    => "1",
                            "value"   => $model->field["STAFF"],
                            "extrahtml" => "$disabled onChange=\"btn_submit('');\" ",
                            "options" => $opt_staf));

        $arg["sel"]["STAFF"] = $objForm->ge("STAFF");

        $result->free();
        Query::dbCheckIn($db);

        //レポート番号
        if ($model->cmd == 'addread'){
            $model->field["REPNO"] = '';
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $setSize = "10";
            $extra = "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"keyfocs1(this)\";";
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $setSize = "17";
            $extra = "onkeydown=\"keyfocs1(this)\";";
        } else {
            $setSize = "10";
            $extra = "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"keyfocs1(this)\";";
        }
        $objForm->ae( array("type"        => "text",
                            "name"        => "REPNO",
                            "size"        => $setSize,
                            "maxlength"   => $setSize,
                            "extrahtml"   => $extra,
                            "value"       => $model->field["REPNO"]));

        $arg["sel"]["REPNO"] = $objForm->ge("REPNO");

        //学籍番号
        if ($model->cmd == 'addread'){
            $model->field["SCHREGNO"] = '';
        }
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHREGNO",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"keyfocs2(this)\";",
                            "value"       => $model->field["SCHREGNO"]));

        $arg["sel"]["SCHREGNO"] = $objForm->ge("SCHREGNO");

        //評価
        if ($model->cmd == 'addread'){
            $model->field["HYOUKA"] = '';
        }
        $objForm->ae( array("type"        => "text",
                            "name"        => "HYOUKA",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"checkkey()\";",
                            "value"       => $model->field["HYOUKA"]));

        $arg["sel"]["HYOUKA"] = $objForm->ge("HYOUKA");

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/
        //抽出データ出力
        $schcnt = 0;
        $db = Query::dbCheckOut();
        $result = $db->query(knjm270Query::getSch($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            //チェックボックス
            if($model->field["DELCHK"]["$schcnt"] == "on")
            {
                $check_del = "checked";
            }else {
                $check_del = "";
            }

            $objForm->ae( array("type"      => "checkbox",
                                "name"      => "DELCHK".$schcnt,
                                "value"     => "on",
                                "extrahtml" => $check_del ) );

            $Row["DELCHK"]   = $objForm->ge("DELCHK".$schcnt);
            $Row["DELID"] = "DEL".$schcnt;

            //レポート番号
            $model->setdata["REPORTNO"][$schcnt] = substr(CTRL_YEAR,3,1).$row["SUBCLASSCD"].sprintf("%02d",$row["STANDARD_SEQ"]).$row["REPRESENT_SEQ"];

            if ("1" == $model->Properties["useCurriculumcd"]) { // 画面のレポートNo.は10桁表示
                $Row["REPORTNO_SHOW"] = substr(CTRL_YEAR,3,1).$row["SUBCLASSCD_ONLY"].sprintf("%02d",$row["STANDARD_SEQ"]).$row["REPRESENT_SEQ"];
            } else {
                $Row["REPORTNO_SHOW"] = $model->setdata["REPORTNO"][$schcnt];
            }

            $Row["REPORTNO"] = $model->setdata["REPORTNO"][$schcnt];
            $Row["REPID"] = "REP".$schcnt;

            $objForm->ae( array("type"      => "hidden",
                                "value"     => $model->setdata["REPORTNO"][$schcnt],
                                "name"      => "REPORTNO".$schcnt) );

            //学籍番号
            $model->setdata["SCHREGNO2"][$schcnt] = $row["SCHREGNO"];

            $Row["SCHREGNO2"] = $model->setdata["SCHREGNO2"][$schcnt];
            $Row["SCHID"] = "SCH".$schcnt;

            $objForm->ae( array("type"      => "hidden",
                                "value"     => $model->setdata["SCHREGNO2"][$schcnt],
                                "name"      => "SCHREGNO2".$schcnt) );

            //提出受付
            $model->setdata["RECEIPT_DATE"][$schcnt] = $row["RECEIPT_DATE"];
            //氏名（漢字）
            $model->setdata["NAME"][$schcnt] = $row["NAME_SHOW"];
            
            $Row["NAME"] = $model->setdata["NAME"][$schcnt];
            $Row["NAMEID"] = "NAME".$schcnt;

            //科目名
            $model->setdata["SUBCLASSNAME"][$schcnt] = $row["SUBCLASSNAME"];

            $Row["SUBCLASSNAME"] = $model->setdata["SUBCLASSNAME"][$schcnt];
            $Row["SCLID"] = "SCL".$schcnt;

            //回数
            $model->setdata["STANDARD_SEQ"][$schcnt] = $row["STANDARD_SEQ"];

            $Row["STANDARD_SEQ"] = "第".$model->setdata["STANDARD_SEQ"][$schcnt]."回";
            $Row["STQID"] = "RSQ".$schcnt;

            //評価
            $model->setdata["GRAD_VALUE"][$schcnt] = $row["GRAD_VALUE"];

            //リンク設定
            $subdata = "loadwindow('knjm270index.php?cmd=subform1&GRAD_VALUESUB={$row["GRAD_VALUE"]}&SCHREGNOSUB={$row["SCHREGNO"]}&SUBCLASSSUB={$row["SUBCLASSCD"]}&STSEQSUB={$row["STANDARD_SEQ"]}&RSEQSUB={$row["REPRESENT_SEQ"]}&DATESUB={$model->Date}&CHAIRSUB={$row["CHAIRCD"]}&RECDAYSUB={$row["RECEIPT_DATE"]}',500,200,350,300)";

            $row["GRAD_VALUESUB"] = View::alink("#", htmlspecialchars($row["NAME1"]),"onclick=\"$subdata\"");

            $Row["GRAD_VALUESUB"] = $row["GRAD_VALUESUB"];
            $Row["GRDVID"] = "GRDV".$schcnt;

            //再提出数
            $model->setdata["REPRESENT_SEQ"][$schcnt] = $row["REPRESENT_SEQ"];

            if ($row["REPRESENT_SEQ"] != 0){
                $Row["REPRESENT_SEQ"] = "再".$model->setdata["REPRESENT_SEQ"][$schcnt];
                $Row["RSQID"] = "RSQ".$schcnt;
            }else {
                $Row["REPRESENT_SEQ"] = "";
                $Row["RSQID"] = "RSQ".$schcnt;
            }
            //登録日付
            $model->setdata["T_TIME"][$schcnt] = $row["GRAD_TIME"];

            $Row["T_TIME"] = $model->setdata["T_TIME"][$schcnt];
            $Row["TIMEID"] = "TIME".$schcnt;

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        $model->schcntall = $schcnt;
        $result->free();
        Query::dbCheckIn($db);
        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "登　録",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ));

        //$extra = "onclick=\"closeWin();\"";
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2012/12/20 キーイベントタイムアウト処理復活
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終　了",
                            "extrahtml"   => $extra ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "all_del",
                            "value"       => "全行削除",
                            "extrahtml"   => "onclick=\"return btn_submit('alldel');\"" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "指定行削除",
                            "extrahtml"   => "onclick=\"return btn_submit('chdel');\"" ));

        $arg["button"] = array("BTN_OK"     => $objForm->ge("btn_ok"),
                               "BTN_CLEAR"  => $objForm->ge("btn_cancel"),
                               "ALL_DEL"    => $objForm->ge("all_del"),
                               "BTN_DEL"    => $objForm->ge("btn_del") );
        
        //HIDDEN
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm270Form1.html", $arg); 
    }
}
?>
