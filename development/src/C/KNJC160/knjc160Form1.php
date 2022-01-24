<?php

require_once('for_php7.php');

class knjc160Form1
{   
    function main(&$model)
    {
        $objForm        = new form;
        $db = Query::dbCheckOut();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc160index.php", "", "main");
        $arg["jscript"] = "";
        $arg["Closing"] = "";
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //事前処理チェック
        if (!knjc160Query::ChecktoStart($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        //処理年度    
        $opt_year = $opt = array();    
        $result = $db->query(knjc160Query::getYear());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_year[] = array("label" => $row["YEAR"]."年度",
                                "value" => $row["YEAR"]);
        }
        
        //初期値は処理年度
        if ($model->year == "") $model->year = CTRL_YEAR;
        
        $objForm->ae( array("type"      => "select",
                            "name"      => "year",
                            "value"     => $model->year,
                            "options"   => $opt_year,
                            "extrahtml" => "STYLE=\"WIDTH:120\" onChange=\"btn_submit('chg_year');\""));
                                            
        $arg["data"]["YEAR"] = $objForm->ge("year");
    
        //処理月(各学期の期間の月のみをコンボにセット
        $result = $db->query(knjc160Query::getSemesterMonth($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            for ($i = 4; $i < 16; $i++) 
            {   
                $mon = ($i<13) ? $i : ($i-12);
                
                if ($mon < 4) {
                    $year = $model->year + 1;
                } else {
                    $year = $model->year;
                }

                //年と月を合わせて比較する    
                if ((int)($year.sprintf("%02d",$mon)) >= (int)strftime("%Y%m",strtotime($row["SDATE"])) 
                && ((int)$year.sprintf("%02d",$mon)) <= (int)strftime("%Y%m",strtotime($row["EDATE"]))) 
                {
                    //月が学期の開始月または終了月かチェック
                    //開始月の場合は開始日以降その月末日まで集計
                    //開始月の場合は開始日以降翌月の１日まで集計    04/11/01 修正依頼 :alpokinawa n.miyagi
                    if ($mon == (int)strftime("%m",strtotime($row["SDATE"]))) {

                        $flg = "1";
                    
                    //終了月の場合はその月の１日から終了日まで集計
                    //終了月の場合はその月の２日から終了日まで集計  04/11/01 修正依頼 :alpokinawa n.miyagi
                    } else if ($mon == (int)strftime("%m",strtotime($row["EDATE"]))) {
                    
                        $flg = "2";
                    
                    //それ以外はその月の１日から月末日まで集計
                    //それ以外はその月の２日から翌月の１日まで集計  04/11/01 修正依頼 :alpokinawa n.miyagi
                    } else {
                    
                        $flg = "0";
                    }
                    
                    //初期値(学籍処理日の月にする）
                    if ($model->month == "") {
                        if ($mon == strftime("%m", strtotime(CTRL_DATE))) {
                            $model->month = $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg;
                        }
                    }
                        
                    $opt[] = array("label"    =>$mon."月 ( ".$row["SEMESTERNAME"]." )",
                                   "value"    => $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg);              
                }
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "month",
                            "size"        => "1",
                            "extrahtml"   => "STYLE=\"WIDTH:120\"",
                            "value"       => $model->month,
                            "options"     => $opt));
        $arg["data"]["MONTH"] = $objForm->ge("month");
        Query::dbCheckIn($db);

        //ラジオボタンを作成（長欠区分）    add  05/01/24  yamauchi
        $opt_tyouketu[0]=1;
        $opt_tyouketu[1]=2;
        $opt_tyouketu[2]=3;

        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:"2",
                            "multiple"   => $opt_tyouketu));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);

        //ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
                                      
        $arg["button"] = array("btn_print"  => $objForm->ge("btn_print"),
                               "btn_cancel" => $objForm->ge("btn_cancel") );  
        //HIDDEN
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd2") );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //add  05/01/24  yamauchi
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJC160"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjc160Form1.html", $arg); 
    }
}
?>
