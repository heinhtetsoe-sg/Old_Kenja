    <?php

require_once('for_php7.php');

class knje390SubForm2_2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2_2", "POST", "knje390index.php", "", "subform2_2");

        //DB接続
        $db = Query::dbCheckOut();

        //カレンダー呼び出し
        $my = new mycalendar();

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];
        // Add by PP for Title 2020-02-03 start
         if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = "A アセスメント表の発達検査画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // for Error 915
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubForm2_2_CurrentCursor915\");</script>";
        } else {
          echo "<script>var error915= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubForm2_2_CurrentCursor915\", error915);
              sessionStorage.removeItem(\"KNJE390SubForm2_2_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform2_check") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //発達検査情報取得
        if ($model->cmd == "subform2_check_set"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getSubQuery2CheckGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field2;
            }
        } else {
            $Row =& $model->field2;
        }
        
        //検査日
        // $Row["CHECK_DATE"] = str_replace("-", "/", $Row["CHECK_DATE"]);
        // $arg["data"]["CHECK_DATE"] = View::popUpCalendar($objForm, "CHECK_DATE", $Row["CHECK_DATE"]);
        $datecutcnt = get_count(preg_split("/-/", $Row["CHECK_DATE"]));
        if ($datecutcnt > 1) {
            $Row["CHECK_DATE"] = substr($Row["CHECK_DATE"], 0, -3);
        }
        $arg["data"]["CHECK_DATE"] = str_replace("\n", "", $my->MyMonthWin2($objForm, "CHECK_DATE", $Row["CHECK_DATE"]));
        
        //検査機関
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"overflow:auto;\" id=\"CHECK_CENTER_TEXT\" aria-label=\"検査機関全角50文字2行まで\"";
        $arg["data"]["CHECK_CENTER_TEXT"] = knjCreateTextArea($objForm, "CHECK_CENTER_TEXT", 2, 101, "soft", $extra, $Row["CHECK_CENTER_TEXT"]);
        $arg["data"]["CHECK_CENTER_TEXT_SIZE"] = '<font size="1" color="red">(全角50文字2行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end
        
        //検査名
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"CHALLENGED_NAMES\" style=\"height:35px; overflow:auto;\" aria-label=\"検査名全角10文字2行まで\"";
        $arg["data"]["CHECK_NAME"] = knjCreateTextArea($objForm, "CHECK_NAME", 2, 21, "soft", $extra, $Row["CHECK_NAME"]);
        $arg["data"]["CHECK_NAME_SIZE"] = '<font size="1" color="red">(全角10文字2行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390SubForm2_2.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje390Query::getSubQuery2CheckRecordList($model);
    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist["RECORD_DIV_NAME"] = '検査機関'.($retCnt+1);
        $centerName = $rowlist["CHECK_CENTER_TEXT"];
        $rowlist["CENTER_NAME"] = $centerName;
        $checkDate = preg_split("/-/", $rowlist["CHECK_DATE"]);
        $rowlist["CHECK_DATE"] = $checkDate[0].'/'.$checkDate[1];
        
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //検査機関マスタ参照
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_check_center_search\" onclick=\"current_cursor('btn_check_center_search'); loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=check_center_search&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 600)\"";
    $arg["button"]["btn_check_center_search"] = knjCreateBtn($objForm, "btn_check_center_search", "検査機関", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //検査名マスタ
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_checkname\" onclick=\"current_cursor('btn_checkname'); loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=checkname_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_checkname"] = knjCreateBtn($objForm, "btn_checkname", "検査名参照", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //追加ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_insert\" onclick=\"current_cursor('btn_insert'); return btn_submit('check2_insert');\" aria-label=\"追加\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('check2_update');\" aria-label=\"更新\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //削除ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_delete\" onclick=\"current_cursor('btn_delete'); return btn_submit('check2_delete');\" aria-label=\"削除\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //戻るボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform2A'); \" aria-label=\"戻る\"");
    // Add by PP for PC-Talker 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

