<?php

require_once('for_php7.php');

class knje390SubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje390index.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();
        //カレンダー呼び出し
        $my = new mycalendar();

        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;//年度データはないが、念の為にセット
        }
        if (!$model->record_date) {
            $model->record_date = 'NEW';
        }
        //表示日付をセット
        if ($model->record_date === 'NEW') {
            $setHyoujiDate = '';
        } else {
            $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
        }

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;
        // Add by PP for Title 2020-02-03 start
        if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = $info["NAME_SHOW"]."のB プロフィール画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // textbox 915 error
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubForm1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var error915= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubForm1_CurrentCursor915\", error915);
              sessionStorage.removeItem(\"KNJE390SubForm1_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "subform1A" || $model->cmd == "subform1_clear"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getSubQuery1($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        
        //作成年月日
        $extra = "";
        $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
        $arg["data"]["WRITING_DATE"] = View::popUpCalendar($objForm, "WRITING_DATE", $Row["WRITING_DATE"]);
        
        //通学方法
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getNameMst("E036");
        makeCmb($objForm, $arg, $db, $query, "TSUUGAKU_DIV1", $Row["TSUUGAKU_DIV1"], "aria-label=\"通学方法の登校\"", 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end
        
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getNameMst("E036");
        makeCmb($objForm, $arg, $db, $query, "TSUUGAKU_DIV2", $Row["TSUUGAKU_DIV2"], "aria-label=\"通学方法の下校\"", 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end

        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment(35, 2);
        $comment_TSUUGAKU_DIV1_REMARK = str_replace(array( '(', ')' ), '', $comment);
        $extra = "aria-label=\"通学方法の登校{$comment_TSUUGAKU_DIV1_REMARK}\"";
        $arg["data"]["TSUUGAKU_DIV1_REMARK"] = getTextOrArea($objForm, "TSUUGAKU_DIV1_REMARK", 30, 2, $Row["TSUUGAKU_DIV1_REMARK"], $model, $extra);
        setInputChkHidden($objForm, "TSUUGAKU_DIV1_REMARK", 35, 2, $arg);
        $comment = getTextAreaComment(35, 2);
        $comment_TSUUGAKU_DIV2_REMARK = str_replace(array( '(', ')' ), '', $comment);
        $extra = "aria-label=\"通学方法の下校{$comment_TSUUGAKU_DIV2_REMARK}\"";
        $arg["data"]["TSUUGAKU_DIV2_REMARK"] = getTextOrArea($objForm, "TSUUGAKU_DIV2_REMARK", 30, 2, $Row["TSUUGAKU_DIV2_REMARK"], $model, $extra);
        setInputChkHidden($objForm, "TSUUGAKU_DIV2_REMARK", 35, 2, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //障害名･診断名
        // Add by PP for PC-Talker and current cursor 2020-02-03 start
        $comment = getTextAreaComment(15, 6);
        $comment_CHALLENGED_NAMES = str_replace(array( '(', ')' ), '', $comment);
        $extra = "id=\"CHALLENGED_NAMES\" style=\"height:85px;\" aria-label=\"障害名等{$comment_CHALLENGED_NAMES}\"";
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", 15, 3, $Row["CHALLENGED_NAMES"], $model, $extra);
        setInputChkHidden($objForm, "CHALLENGED_NAMES", 15, 6, $arg);
        // Add by PP for PC-Talker and current cursor 2020-02-20 end

        //障害の実態･特性
        // Add by PP for PC-Talker and current cursor 2020-02-03 start
        $comment = getTextAreaComment(45, 3);
        $comment_CHALLENGED = str_replace(array( '(', ')' ), '', $comment);
        $extra = "style=\"height:45px;\" aria-label=\"障害の実態･特性{$comment_CHALLENGED}\"";
        $arg["data"]["CHALLENGED_STATUS"] = getTextOrArea($objForm, "CHALLENGED_STATUS", 45, 3, $Row["CHALLENGED_STATUS"], $model, $extra);
        setInputChkHidden($objForm, "CHALLENGED_STATUS", 45, 3, $arg);
        // Add by PP for PC-Talker and current cursor 2020-02-20 end

        //療育手帳
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getNameMst("E061");
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_NAME", $Row["CHALLENGED_CARD_NAME"], "aria-label=\"障害者手帳の療育手帳\"", 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end

        //身体障害者手帳
        //身体障害の種別
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getNameMst("E031");
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_CLASS", $Row["CHALLENGED_CARD_CLASS"], "aria-label=\"障害者手帳の身体障害者手帳の種\"", 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end

        //身体障害の等級
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getNameMst("E032");
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_RANK", $Row["CHALLENGED_CARD_RANK"], "aria-label=\"障害者手帳の身体障害者手帳の級\"", 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end

        //手帳の障害名
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getChallengedCardNameMst();
        $label = "障害者手帳の身体障害者手帳";
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_AREA_NAME", $Row["CHALLENGED_CARD_AREA_NAME"], "aria-label=\"{$label}1\"", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_AREA_NAME2", $Row["CHALLENGED_CARD_AREA_NAME2"], "aria-label=\"{$label}2\"", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_AREA_NAME3", $Row["CHALLENGED_CARD_AREA_NAME3"], "aria-label=\"{$label}3\"", 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end

        //精神障害者保健福祉手帳
        //等級
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label=\"障害者手帳の精神障害者保健福祉手帳の級\"";
        $query = knje390Query::getNameMst("E063");
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_REMARK", $Row["CHALLENGED_CARD_REMARK"], $extra, 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end

        //療育手帳の次回判定
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label=\"次回判定\"";
        $arg["data"]["CHALLENGED_CARD_CHECK_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_CHECK_YM", $Row["CHALLENGED_CARD_CHECK_YM"], $extra);
        // Add by PP for PC-Talker 2020-02-20 end

        //身体障害手帳の次回認定
        //チェックボックスを作成
        $extra = $Row["CHALLENGED_CARD_GRANT_FLG"] == "1" ? "checked" : "";
        $extra .= " id=\"CHALLENGED_CARD_GRANT_FLG\" ";
        $arg["data"]["CHALLENGED_CARD_GRANT_FLG"] = knjCreateCheckBox($objForm, "CHALLENGED_CARD_GRANT_FLG", "1", $extra, "");
        //次回認定月を作成
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label=\"次回認定\"";
        $arg["data"]["CHALLENGED_CARD_GRANT_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_GRANT_YM", $Row["CHALLENGED_CARD_GRANT_YM"], $extra);
        // Add by PP for PC-Talker 2020-02-20 end
        //精神障害者保健福祉手帳の有効期限
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label=\"有効期限\"";
        $arg["data"]["CHALLENGED_CARD_BAST_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_BAST_YM", $Row["CHALLENGED_CARD_BAST_YM"], $extra);
        // Add by PP for PC-Talker 2020-02-20 end

        //受給者証の有無
        // Add by PP for PC-Talker 2020-02-03 start
        $extra  = "id=\"WELFARE_MEDICAL_RECEIVE_FLG\" aria-label=\"受給者証の有\"";
        if ($Row["WELFARE_MEDICAL_RECEIVE_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["WELFARE_MEDICAL_RECEIVE_FLG"] = knjCreateCheckBox($objForm, "WELFARE_MEDICAL_RECEIVE_FLG", "1", $extra);
        // Add by PP for PC-Talker 2020-02-20 end

        //種類
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knje390Query::getChallengedCertifNameMst();
        $label = "受給者証の種類";
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV", $Row["WELFARE_MEDICAL_RECEIVE_DIV"], "aria-label=\"{$label}1\"", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV2", $Row["WELFARE_MEDICAL_RECEIVE_DIV2"], "aria-label=\"{$label}2\"", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV3", $Row["WELFARE_MEDICAL_RECEIVE_DIV3"], "aria-label=\"{$label}3\"", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV4", $Row["WELFARE_MEDICAL_RECEIVE_DIV4"], "aria-label=\"{$label}4\"", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV5", $Row["WELFARE_MEDICAL_RECEIVE_DIV5"], "aria-label=\"{$label}5\"", 1, 1);
        // Add by PP for PC-Talker 2020-02-20 end

        //校区
        //小学校
        $query = knje390Query::getSchoolInfo($Row["P_SCHOOL_CD"]);
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["P_SCHOOL_NAME"] = $schoolRow["FINSCHOOL_NAME"];
        //学校検索
        $setKind = $db->getOne(knje390Query::getL019namecd2("4"));
        $schoolCdVal = "document.forms[0]['P_SCHOOL_CD'].value";
        // Add by PP for PC-Talker 2020-02-03 start
        $arg["data"]["P_SCHOOL_CD"] = View::popUpSchoolCd($objForm, "P_SCHOOL_CD", $Row["P_SCHOOL_CD"], $schoolCdVal, "btn_kensaku", "", "P_SCHOOL_CD", "P_SCHOOL_NAME", "", "P_SCHOOL_RITSU", "", "", $setKind, "id=\"P_SCHOOL_CD\" aria-label=\"校区の小学校\"", "aria-label=\"小学校検索\"");
        // Add by PP for PC-Talker 2020-02-20 end
        
        //中学校
        $query = knje390Query::getSchoolInfo($Row["J_SCHOOL_CD"]);
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["J_SCHOOL_NAME"] = $schoolRow["FINSCHOOL_NAME"];
        //学校検索
        $setKind = $db->getOne(knje390Query::getL019namecd2("5"));
        $schoolCdVal = "document.forms[0]['J_SCHOOL_CD'].value";
        // Add by PP for PC-Talker 2020-02-03 start
        $arg["data"]["J_SCHOOL_CD"] = View::popUpSchoolCd($objForm, "J_SCHOOL_CD", $Row["J_SCHOOL_CD"], $schoolCdVal, "btn_kensaku2", "", "J_SCHOOL_CD", "J_SCHOOL_NAME", "", "J_SCHOOL_RITSU", "", "", $setKind, "id=\"J_SCHOOL_CD\" aria-label=\"校区の中学校\"", "aria-label=\"中学校検索\"");
        // Add by PP for PC-Talker 2020-02-20 end

        //避難場所
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label=\"災害時の避難場所\"";
        $arg["data"]["EVACUATION_AREA"] = knjCreateTextBox($objForm, $Row["EVACUATION_AREA"], "EVACUATION_AREA", 40, 40, $extra);
        // Add by PP for PC-Talker 2020-02-20 end
        
        //留意事項
        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment(40, 3);
        $comment_IMPORTANT = str_replace(array( '(', ')' ), '', $comment);
        $extra = "style=\"height:50px;\" aria-label=\"災害時の留意事項{$comment_IMPORTANT}\"";
        $arg["data"]["IMPORTANT_NOTICE"] = getTextOrArea($objForm, "IMPORTANT_NOTICE", 40, 3, $Row["IMPORTANT_NOTICE"], $model, $extra);
        setInputChkHidden($objForm, "IMPORTANT_NOTICE", 40, 3, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //備考
        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment(50, 10);
        $comment_remark = str_replace(array( '(', ')' ), '', $comment);
        $extra = "style=\"height:70px;\" aria-label=\"備考{$comment_remark}\"";
        $arg["data"]["REMARK"] = getTextOrArea($objForm, "REMARK", 50, 10, $Row["REMARK"], $model, $extra);
        setInputChkHidden($objForm, "REMARK", 50, 10, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //履歴用日付
        $model->field["BACKUP_DATE"] = str_replace("-", "/", $model->field["BACKUP_DATE"]);
        $arg["data"]["BACKUP_DATE"] = View::popUpCalendar($objForm, "BACKUP_DATE", $model->field["BACKUP_DATE"]);
        
        //データをカウント
        $mainCountData = knje390Query::getCheckMainDataQuery($db, $model, "1");
        
        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        if(get_count($model->warning)== 0 && $model->cmd !="subform1_clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="subform1_clear"){
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML5($model, "knje390SubForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData)
{
    //データがない場合は、更新、取消、戻る以外は使用不可
    if ($mainCountData == 0) {
        $disabled = "disabled";
    } else {
        $disabled = "";
    }

    //障害名･診断名マスタ
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_statusname\" onclick=\"current_cursor('btn_statusname'); loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名参照", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    
    //学校検索ボタンを作成する
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=&target_number=1',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_schsearch1"] = knjCreateBtn($objForm, "btn_schsearch1", "学校検索", $extra);
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=&target_number=2',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_schsearch2"] = knjCreateBtn($objForm, "btn_schsearch2", "学校検索", $extra);

    //教育歴ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform1_educate&SCHREGNO=".$model->schregno;
    $extra = "id=\"btn_replace\" onclick=\"current_cursor('btn_replace'); window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "教育歴", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //医療ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform1_medical&SCHREGNO=".$model->schregno;
    $extra = "id=\"btn_replace2\" onclick=\"current_cursor('btn_replace2'); window.open('$link','_self');\"";
    $arg["button"]["btn_replace2"] = KnjCreateBtn($objForm, "btn_replace2", "医療", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //健康管理ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform1_healthcare&SCHREGNO=".$model->schregno;
    $extra = "id=\"btn_replace3\" onclick=\"current_cursor('btn_replace3');window.open('$link','_self');\"";
    $arg["button"]["btn_replace3"] = KnjCreateBtn($objForm, "btn_replace3", "健康管理", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //福祉ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform1_welfare&SCHREGNO=".$model->schregno;
    $extra = "id=\"btn_replace4\" onclick=\"current_cursor('btn_replace4');window.open('$link','_self');\"";
    $arg["button"]["btn_replace4"] = KnjCreateBtn($objForm, "btn_replace4", "福祉", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //教育歴ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform1_educate&SCHREGNO=".$model->schregno;
    $extra = "id=\"btn_replace_1\" onclick=\"current_cursor('btn_replace_1'); window.open('$link','_self');\"";
    $arg["button"]["btn_replace_1"] = KnjCreateBtn($objForm, "btn_replace_1", "教育歴", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //医療ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform1_medical&SCHREGNO=".$model->schregno;
    $extra = "id=\"btn_replace2_1\" onclick=\"current_cursor('btn_replace2_1'); window.open('$link','_self');\"";
    $arg["button"]["btn_replace2_1"] = KnjCreateBtn($objForm, "btn_replace2_1", "医療", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //健康管理ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform1_healthcare&SCHREGNO=".$model->schregno;
    $extra = "id=\"btn_replace3_1\" onclick=\"current_cursor('btn_replace3_1');window.open('$link','_self');\"";
    $arg["button"]["btn_replace3_1"] = KnjCreateBtn($objForm, "btn_replace3_1", "健康管理", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //福祉ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform1_welfare&SCHREGNO=".$model->schregno;
    $extra = "id=\"btn_replace4_1\" onclick=\"current_cursor('btn_replace4_1');window.open('$link','_self');\"";
    $arg["button"]["btn_replace4_1"] = KnjCreateBtn($objForm, "btn_replace4_1", "福祉", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //履歴ボタンを作成する
    $extra = "id=\"btn_rireki\" onclick=\"current_cursor('btn_rireki'); return btn_submit('subform1_rireki');\"";
    $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "更新(履歴)", $extra.$disabled);

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform1_updatemain');\" aria-label=\"更新\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update_1\" onclick=\"current_cursor('btn_update_1'); return btn_submit('subform1_updatemain');\" aria-label=\"更新\"";
    $arg["button"]["btn_update_1"] = knjCreateBtn($objForm, "btn_update_1", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end


    //クリアボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('subform1_clear');\" aria-label=\"取消\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset_1\" onclick=\"current_cursor('btn_reset_1'); return btn_submit('subform1_clear');\" aria-label=\"取消\"";
    $arg["button"]["btn_reset_1"] = knjCreateBtn($objForm, "btn_reset_1", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //戻るボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "onclick=\"return btn_submit('edit');\" aria-label=\"戻る\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
    // Add by PP for PC-Talker 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "useFinschoolcdFieldSize", $model->Properties["useFinschoolcdFieldSize"]);
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

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $setExtra = "") {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px; overflow:auto;\" id=\"".$name."\" $setExtra";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\" $setExtra";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}
function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
?>
