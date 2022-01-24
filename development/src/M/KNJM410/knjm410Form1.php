<?php

require_once('for_php7.php');

/********************************************************************/
/* ホームルーム出席入力                             山城 2005/03/23 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：学期の取得方法を修正(年度も参照)         山城 2005/10/18 */
/********************************************************************/

class knjm410Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm410index.php", "", "main");

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");

        //チェック用hidden
        $objForm->ae( array("type"      => "hidden",
                            "value"     => $model->Date,
                            "name"      => "DEFOULTDATE") );

        //講座コンボ
        $opt_chair = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjm410Query::getAuth($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_chair[] = array("label" => $row["CHAIRNAME"],
                                 "value" => $row["CHAIRCD"]);
        }
        if (!$model->field["CHAIR"]){
            $model->field["CHAIR"]        = $opt_chair[0]["value"];
        }
        if(!$opt_chair[0]) {
            $arg["Closing"] = " closing_window('MSG300');";
        }

        $objForm->ae( array("type"    => "select",
                            "name"    => "CHAIR",
                            "size"    => "1",
                            "value"   => $model->field["CHAIR"],
                            "extrahtml" => "onChange=\"btn_submit('');\" ",
                            "options" => $opt_chair));

        $arg["sel"]["CHAIR"] = $objForm->ge("CHAIR");

        $result->free();
        Query::dbCheckIn($db);

        //校時
        $opt_peri = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjm410Query::selectName("B001"));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_peri[] = array("label" => $row["NAME1"],
                                "value" => $row["NAMECD2"]);
        }

        if (!$model->field["PERIOD"]) $model->field["PERIOD"] = $opt_peri[0]["value"];

        $objForm->ae( array("type"      => "select",
                            "name"      => "PERIOD",
                            "size"      => "1",
                            "value"     => $model->field["PERIOD"],
                            "extrahtml" => "onChange=\"btn_submit('');\" ",
                            "options"   => $opt_peri));

        $arg["sel"]["PERIOD"] = $objForm->ge("PERIOD");

        $result->free();
        Query::dbCheckIn($db);

        //学籍番号
        if ($model->cmd == 'addread'){
            $model->field["SCHREGNO"] = '';
        }
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHREGNO",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"checkkey()\";",
                            "value"       => $model->field["SCHREGNO"]));

        $arg["sel"]["SCHREGNO"] = $objForm->ge("SCHREGNO");

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/
        //抽出データ出力
        $schcnt = 0;
        $db = Query::dbCheckOut();
        $result = $db->query(knjm410Query::getSch($model));
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
            //学籍番号
            $model->setdata["SCHREGNO2"][$schcnt] = $row["SCHREGNO"];

            $Row["SCHREGNO2"] = $model->setdata["SCHREGNO2"][$schcnt];
            $Row["SCHID"] = "SCH".$schcnt;

            $objForm->ae( array("type"      => "hidden",
                                "value"     => $model->setdata["SCHREGNO2"][$schcnt],
                                "name"      => "SCHREGNO2".$schcnt) );

            //氏名（漢字）
            $model->setdata["NAME"][$schcnt] = $row["NAME_SHOW"];

            $Row["NAME"] = $model->setdata["NAME"][$schcnt];
            $Row["NAMEID"] = "NAME".$schcnt;

            //登録時刻
            $model->setdata["T_TIME"][$schcnt] = $row["RECEIPT_TIME"];

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
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2013/01/15 キーイベントタイムアウト処理復活
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

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm410Form1.html", $arg); 
    }
}
?>
