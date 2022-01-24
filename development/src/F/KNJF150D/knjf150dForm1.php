<?php

require_once('for_php7.php');
//タイトル行の行数
# define("DEF_LINE_COUNT",13);
define("DEF_LINE_COUNT",13);
class knjf150dForm1
{
    function main(&$model)
    {

        #time start
        $start = $model->getMicrotime();

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf150dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度(表示/更新時にも利用するため、modelと同じ処理を実装。年月日の変わるタイミングを考慮)
        if ($model->cntl_dt_key) {
            $cutarry = explode("-", $model->cntl_dt_key);
            $model->cntl_dt_year    = $cutarry[1] > 3 ? $cutarry[0] : ($cutarry[0] - 1);
        } else {
            $model->cntl_dt_year = ($model->cntl_dt_year) ? $model->cntl_dt_year : CTRL_YEAR;
        }
        $arg["TOP"]["THIS_YEAR"] = $model->cntl_dt_year;

        if($model->cmd == "year"){
            $date = preg_split("/-/", $model->cntl_dt_key);
            $model->cntl_dt_key = $model->cntl_dt_year."-".$date[1]."-".$date[2];
        }

        $dataFlg = true;
        //年度内の処理のみを行う。
        if(!$model->checkCtrlDay($model->cntl_dt_key)){
            $reset_day = knjf150dQuery::keyMoverQuery($model, $model->cntl_dt_year."-04-01");
            $model->cntl_dt_key = ($reset_day != "")? $reset_day : $model->cntl_dt_year."-04-01" ;
            $dataFlg = false;
        }
        $thisMonth = explode("-",$model->cntl_dt_key);
        /*** ADD 2005/11/04 by ameku ***/
        $wday = array("(日)","(月)","(火)","(水)","(木)","(金)","(土)");
        $w = date("w",strtotime($model->cntl_dt_key));
        $arg["CNTL_DT_KEY"] = str_replace("-","/",$model->cntl_dt_key).$wday[$w];
        /*** ADD 2005/11/04 by ameku ***/

        //カレンダーコントロール
        $arg["control"]["executedate"] = View::popUpCalendar($objForm, "executedate",
                                                             str_replace("-","/",$model->cntl_dt_key),"reload=true");

        //前日へボタンを作成する
        $extra = "style=\"width:110px\"onclick=\"return btn_submit('read_before');\"";
        $arg["btn_before"] = knjCreateBtn($objForm, "btn_before", "<< 前日", $extra);

        //翌日へボタンを作成する
        $extra = "style=\"width:110px\"onclick=\"return btn_submit('read_next');\"";
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", "翌日 >>", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "cntl_dt_key", $model->cntl_dt_key);
        knjCreateHidden($objForm, "dbname", DB_DATABASE);
        knjCreateHidden($objForm, "Security", AUTHORITY.",".$model->staffcd);

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getcol(knjf150dQuery::selectQuery($model)));
        if ($cnt) {
            $result = $db->query(knjf150dQuery::selectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["VISIT_TIME"] = str_replace("-", "/", $row["VISIT_DATE"]).' '.$row["VISIT_HOUR"].':'.$row["VISIT_MINUTE"];
                $row["VISIT_TIME"] = View::alink(REQUESTROOT."/F/KNJF150C/knjf150cindex.php", $row["VISIT_TIME"], "target=_self tabindex=\"-1\"",
                                                array("PROGRAMID"       => "KNJF150C",
                                                      "SEND_PRGID"      => "KNJF150D",
                                                      "SEND_SUBMIT"     => "1",
                                                      "SEND_AUTH"       => AUTHORITY,
                                                      "SCHREGNO"        => $row["SCHREGNO"],
                                                      "GRADE"           => $row["GRADE"],
                                                      "HR_CLASS"        => $row["HR_CLASS"],
                                                      "ATTENDNO"        => $row["ATTENDNO"],
                                                      "NAME"            => $row["NAME"],
                                                      "VISIT_DATE"      => $row["VISIT_DATE"],
                                                      "VISIT_HOUR"      => $row["VISIT_HOUR"],
                                                      "VISIT_MINUTE"    => $row["VISIT_MINUTE"],
                                                      "TYPE"            => $row["TYPE"],
                                                      "cmd"             => "subform{$row["TYPE"]}A")
                                                      );
                $row["OUT_TIME"] = $row["SEQ98_REMARK1"].':'.$row["SEQ98_REMARK2"];
                if ($row["OUT_TIME"] == ":") {
                    $row["OUT_TIME"] = "";
                }
                $row["BODY_TEMPERATURE"] = $row["SEQ06_REMARK3"].'.'.$row["SEQ06_REMARK4"]."℃";
                if (!$row["SEQ06_REMARK3"]) {
                    $row["BODY_TEMPERATURE"] = "";
                }

                $row["TREATMENT1"] = "";
                $sep = "";
                $treatmentCnt = 0;
                $maxCnt = 3;
                if ($row["SEQ09_REMARK1"] == "1") {
                    $row["TREATMENT1"] .= "休養";
                    $sep = ",";
                    $treatmentCnt++;
                }
                if ($row["SEQ09_REMARK3"] == "1") {
                    $row["TREATMENT1"] .= $sep."早退";
                    $sep = ",";
                    $treatmentCnt++;
                }
                if ($row["SEQ09_REMARK6"] == "1") {
                    $row["TREATMENT1"] .= $sep."医療機関";
                    $sep = ",";
                    $treatmentCnt++;
                }
                if ($row["TYPE"] == "1") {
                    $remarkArray = array();
                    $remarkArray["SEQ08_REMARK1"] = "授業";
                    $remarkArray["SEQ08_REMARK2"] = "ホットパック";
                    $remarkArray["SEQ08_REMARK3"] = "アイシング";
                    $remarkArray["SEQ08_REMARK4"] = "水分補給";
                    foreach ($remarkArray as $field => $name) {
                        if ($row[$field] == "1" && $treatmentCnt <= $maxCnt) {
                            $row["TREATMENT1"] .= ($treatmentCnt == $maxCnt) ? $sep."・・・" : $sep.$name;
                            $sep = ",";
                            $treatmentCnt++;
                        }
                    }
                } else if ($row["TYPE"] == "2") {
                    $remarkArray = array();
                    $remarkArray["SEQ08_REMARK1"] = "授業";
                    $remarkArray["SEQ08_REMARK2"] = "洗浄・被覆材";
                    $remarkArray["SEQ08_REMARK3"] = "止血";
                    $remarkArray["SEQ08_REMARK4"] = "固定";
                    $remarkArray["SEQ08_REMARK5"] = "湿布";
                    $remarkArray["SEQ08_REMARK6"] = "ホットパック";
                    $remarkArray["SEQ08_REMARK7"] = "アイシング";
                    $remarkArray["SEQ08_REMARK8"] = "水分補給";
                    foreach ($remarkArray as $field => $name) {
                        if ($row[$field] == "1" && $treatmentCnt <= $maxCnt) {
                            $row["TREATMENT1"] .= ($treatmentCnt == $maxCnt) ? $sep."・・・" : $sep.$name;
                            $sep = ",";
                            $treatmentCnt++;
                        }
                    }
                }
                if ($row["SEQ09_REMARK8"] == "1" && $treatmentCnt <= $maxCnt) {
                    $row["TREATMENT1"] .= ($treatmentCnt == $maxCnt) ? $sep."・・・" : $sep."その他";
                    $sep = ",";
                    $treatmentCnt++;
                }
                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $visit = $setval["SCHREGNO"].":".$setval["VISIT_DATE"].':'.$setval["VISIT_HOUR"].':'.$setval["VISIT_MINUTE"].':'.$setval["TYPE"];
                    $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $visit, "", "1");
                    $arg["data"][] = $setval;
                    $setval = $row;
                }
            }
            $visit = $setval["SCHREGNO"].":".$setval["VISIT_DATE"].':'.$setval["VISIT_HOUR"].':'.$setval["VISIT_MINUTE"].':'.$setval["TYPE"];
            $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED",  $visit, "", "1");

            $arg["data"][] = $setval;
            $result->free();
        } else {
            //データが1件もなかったらメッセージを返す
            if($dataFlg) $model->setWarning("MSG303");
        }
 
        $arg["DHEADER"] = true;

        //新規ボタンを作成する
        $link = REQUESTROOT."/F/KNJF150C/knjf150cindex.php?mode=1&PROGRAMID=KNJF150C&SEND_PRGID=KNJF150D&cmd=&SEND_SUBMIT=1&SEND_AUTH=".AUTHORITY;
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", " 新 規 ", $extra);

        //削除ボタンを作成する
        $extra = " onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", " 削 除 ", $extra);

        //取消ボタンを作成する
        $extra = " onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", " 取 消 ", $extra);

        //終了ボタンを作成する
        $extra = " onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", " 終 了 ", $extra);

        //フォーカス対象無し
        if($first == "true"){
            $first = "off";
        }else{
            $first = "first";
        }

        //hiddenを作成する
        knjCreateHidden($objForm, "SEND_AUTH", AUTHORITY);

        //処理が完了、又は権限が無ければ閉じる。
        if($model->cntl_dt_year == ""){
            $arg["Closing"] = "  closing_window('year'); " ;
        }else if(AUTHORITY == DEF_NOAUTH){
            $arg["Closing"] = "  closing_window('cm'); " ;
        }else if($model->check_staff_dat != "ok" ){
            $arg["Closing"] = "  closing_window('sf'); " ;
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML2($model, "knjf150dForm1.html", $arg);

        #time end
        $end = $model->getMicrotime();
        $time = $end - $start;
        //echo "<BR> This Program took LoadingTime ".$time." sec(s) <BR>";

    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "", 'value' => "");
    } else if ($blank == "ADDALL") {
        $opt[] = array("label" => "--- すべて ---",
                       "value" => -1);
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($multiple != "1") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
