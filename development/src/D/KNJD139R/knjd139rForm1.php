<?php

require_once('for_php7.php');

class knjd139rForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd139rindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjd139rQuery::getSemester();
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd139rQuery::getHreportremarkDat($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //画面切替
        $arg["TBLCLMROW1"] = 9;
        $arg["TBLCLMROW2"] = 2;
        if ($model->school_kind == "H") {
            $arg["schoolkind_H"] = 1;
            $arg["TBLCLMROW1"] = 11;
            $arg["TBLCLMROW2"] = 4;
        } else if ($model->school_kind == "J") {
            $arg["schoolkind_J"] = 1;
        }

        //出欠状況備考
        $arg["data"]["ATTENDRECREMARK"] = getTextOrArea($objForm, "ATTENDRECREMARK", $model->getPro["ATTENDRECREMARK"]["moji"], $model->getPro["ATTENDRECREMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        setInputChkHidden($objForm, "ATTENDRECREMARK", $model->getPro["ATTENDRECREMARK"]["moji"], $model->getPro["ATTENDRECREMARK"]["gyou"], $arg);

        //所見
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        setInputChkHidden($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $arg);

        //探究テーマ
        if ($model->cmd == "main" || $model->cmd == "clear") {
            $query = knjd139rQuery::getHreportremarkDetailDat($model, "02", "01");
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["TANKYU_THEME01"] = $Row2["REMARK1"];
            $model->field["TANKYU_THEME02"] = $Row2["REMARK2"];
        }
        $arg["data"]["TANKYU_THEME01"] = getTextOrArea($objForm, "TANKYU_THEME01", $model->getPro["TANKYU_THEME01"]["moji"], $model->getPro["TANKYU_THEME01"]["gyou"], $model->field["TANKYU_THEME01"], $model);
        setInputChkHidden($objForm, "TANKYU_THEME01", $model->getPro["TANKYU_THEME01"]["moji"], $model->getPro["TANKYU_THEME01"]["gyou"], $arg);

        $arg["data"]["TANKYU_THEME02"] = getTextOrArea($objForm, "TANKYU_THEME02", $model->getPro["TANKYU_THEME02"]["moji"], $model->getPro["TANKYU_THEME02"]["gyou"], $model->field["TANKYU_THEME02"], $model);
        setInputChkHidden($objForm, "TANKYU_THEME02", $model->getPro["TANKYU_THEME02"]["moji"], $model->getPro["TANKYU_THEME02"]["gyou"], $arg);
        
        //特別活動の記録
        ////生徒会活動
        if ($model->cmd == "main" || $model->cmd == "clear") {
            $query = knjd139rQuery::getHreportremarkDetailDat($model, "01", "02");
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["COMMITTEE01"] = $Row2["REMARK1"];
            $model->field["COMMITTEE02"] = $Row2["REMARK2"];
            $model->field["COMMITTEE03"] = $Row2["REMARK3"];
        }
        $arg["data"]["COMMITTEE01"] = getTextOrArea($objForm, "COMMITTEE01", $model->getPro["COMMITTEE01"]["moji"], $model->getPro["COMMITTEE01"]["gyou"], $model->field["COMMITTEE01"], $model);
        setInputChkHidden($objForm, "COMMITTEE01", $model->getPro["COMMITTEE01"]["moji"], $model->getPro["COMMITTEE01"]["gyou"], $arg);
        $arg["data"]["COMMITTEE02"] = getTextOrArea($objForm, "COMMITTEE02", $model->getPro["COMMITTEE02"]["moji"], $model->getPro["COMMITTEE02"]["gyou"], $model->field["COMMITTEE02"], $model);
        setInputChkHidden($objForm, "COMMITTEE02", $model->getPro["COMMITTEE02"]["moji"], $model->getPro["COMMITTEE02"]["gyou"], $arg);
        $arg["data"]["COMMITTEE03"] = getTextOrArea($objForm, "COMMITTEE03", $model->getPro["COMMITTEE03"]["moji"], $model->getPro["COMMITTEE03"]["gyou"], $model->field["COMMITTEE03"], $model);
        setInputChkHidden($objForm, "COMMITTEE03", $model->getPro["COMMITTEE03"]["moji"], $model->getPro["COMMITTEE03"]["gyou"], $arg);

        ////学級の活動
        if ($model->cmd == "main" || $model->cmd == "clear") {
            $query = knjd139rQuery::getHreportremarkDetailDat($model, "01", "01");
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["CLASSACT01"] = $Row2["REMARK1"];
            $model->field["CLASSACT02"] = $Row2["REMARK2"];
            $model->field["CLASSACT03"] = $Row2["REMARK3"];
        }
        $arg["data"]["CLASSACT01"] = getTextOrArea($objForm, "CLASSACT01", $model->getPro["CLASSACT01"]["moji"], $model->getPro["CLASSACT01"]["gyou"], $model->field["CLASSACT01"], $model);
        setInputChkHidden($objForm, "CLASSACT01", $model->getPro["CLASSACT01"]["moji"], $model->getPro["CLASSACT01"]["gyou"], $arg);
        $arg["data"]["CLASSACT02"] = getTextOrArea($objForm, "CLASSACT02", $model->getPro["CLASSACT02"]["moji"], $model->getPro["CLASSACT02"]["gyou"], $model->field["CLASSACT02"], $model);
        setInputChkHidden($objForm, "CLASSACT02", $model->getPro["CLASSACT02"]["moji"], $model->getPro["CLASSACT02"]["gyou"], $arg);
        $arg["data"]["CLASSACT03"] = getTextOrArea($objForm, "CLASSACT03", $model->getPro["CLASSACT03"]["moji"], $model->getPro["CLASSACT03"]["gyou"], $model->field["CLASSACT03"], $model);
        setInputChkHidden($objForm, "CLASSACT03", $model->getPro["CLASSACT03"]["moji"], $model->getPro["CLASSACT03"]["gyou"], $arg);

        ////部活動
        if ($model->cmd == "main" || $model->cmd == "clear") {
            $query = knjd139rQuery::getHreportremarkDetailDat($model, "01", "03");
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["CLUBACT01"] = $Row2["REMARK1"];
            $model->field["CLUBACT02"] = $Row2["REMARK2"];
            $model->field["CLUBACT03"] = $Row2["REMARK3"];
        }
        $arg["data"]["CLUBACT01"] = getTextOrArea($objForm, "CLUBACT01", $model->getPro["CLUBACT01"]["moji"], $model->getPro["CLUBACT01"]["gyou"], $model->field["CLUBACT01"], $model);
        setInputChkHidden($objForm, "CLUBACT01", $model->getPro["CLUBACT01"]["moji"], $model->getPro["CLUBACT01"]["gyou"], $arg);
        $arg["data"]["CLUBACT02"] = getTextOrArea($objForm, "CLUBACT02", $model->getPro["CLUBACT02"]["moji"], $model->getPro["CLUBACT02"]["gyou"], $model->field["CLUBACT02"], $model);
        setInputChkHidden($objForm, "CLUBACT02", $model->getPro["CLUBACT02"]["moji"], $model->getPro["CLUBACT02"]["gyou"], $arg);
        $arg["data"]["CLUBACT03"] = getTextOrArea($objForm, "CLUBACT03", $model->getPro["CLUBACT03"]["moji"], $model->getPro["CLUBACT03"]["gyou"], $model->field["CLUBACT03"], $model);
        setInputChkHidden($objForm, "CLUBACT03", $model->getPro["CLUBACT03"]["moji"], $model->getPro["CLUBACT03"]["gyou"], $arg);
        
        ////その他
        if ($model->cmd == "main" || $model->cmd == "clear") {
            $query = knjd139rQuery::getHreportremarkDetailDat($model, "01", "05");
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["OTHER01"] = $Row2["REMARK1"];
            $model->field["OTHER02"] = $Row2["REMARK2"];
            $model->field["OTHER03"] = $Row2["REMARK3"];
        }
        $arg["data"]["OTHER01"] = getTextOrArea($objForm, "OTHER01", $model->getPro["OTHER01"]["moji"], $model->getPro["OTHER01"]["gyou"], $model->field["OTHER01"], $model);
        setInputChkHidden($objForm, "OTHER01", $model->getPro["OTHER01"]["moji"], $model->getPro["OTHER01"]["gyou"], $arg);
        $arg["data"]["OTHER02"] = getTextOrArea($objForm, "OTHER02", $model->getPro["OTHER02"]["moji"], $model->getPro["OTHER02"]["gyou"], $model->field["OTHER02"], $model);
        setInputChkHidden($objForm, "OTHER02", $model->getPro["OTHER02"]["moji"], $model->getPro["OTHER02"]["gyou"], $arg);
        $arg["data"]["OTHER03"] = getTextOrArea($objForm, "OTHER03", $model->getPro["OTHER03"]["moji"], $model->getPro["OTHER03"]["gyou"], $model->field["OTHER03"], $model);
        setInputChkHidden($objForm, "OTHER03", $model->getPro["OTHER03"]["moji"], $model->getPro["OTHER03"]["gyou"], $arg);

        if ($model->school_kind == "H") {
            if ($model->cmd == "main" || $model->cmd == "clear") {
                $query = knjd139rQuery::getHreportremarkDetailDat($model, "01", "07");
                $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $model->field["OTHER04"] = $Row2["REMARK1"];
                $model->field["OTHER05"] = $Row2["REMARK2"];
                $model->field["OTHER06"] = $Row2["REMARK3"];
            }
            $arg["data"]["OTHER04"] = getTextOrArea($objForm, "OTHER04", $model->getPro["OTHER04"]["moji"], $model->getPro["OTHER04"]["gyou"], $model->field["OTHER04"], $model);
            setInputChkHidden($objForm, "OTHER04", $model->getPro["OTHER04"]["moji"], $model->getPro["OTHER04"]["gyou"], $arg);
            $arg["data"]["OTHER05"] = getTextOrArea($objForm, "OTHER05", $model->getPro["OTHER05"]["moji"], $model->getPro["OTHER05"]["gyou"], $model->field["OTHER05"], $model);
            setInputChkHidden($objForm, "OTHER05", $model->getPro["OTHER05"]["moji"], $model->getPro["OTHER05"]["gyou"], $arg);
            $arg["data"]["OTHER06"] = getTextOrArea($objForm, "OTHER06", $model->getPro["OTHER06"]["moji"], $model->getPro["OTHER06"]["gyou"], $model->field["OTHER06"], $model);
            setInputChkHidden($objForm, "OTHER06", $model->getPro["OTHER06"]["moji"], $model->getPro["OTHER06"]["gyou"], $arg);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden作成
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        $arg["finish"] = $objForm->get_finish();

        View::toHTML5($model, "knjd139rForm1.html", $arg);
    }
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {
    //委員会選択1
    $target = "COMMITTEE01";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=1&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee1_1"] = knjCreateBtn($objForm, "btn_committee1_1", "委員会選択", $extra);
    $target = "COMMITTEE02";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=2&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee1_2"] = knjCreateBtn($objForm, "btn_committee1_2", "委員会選択", $extra);
    $target = "COMMITTEE03";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=3&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee1_3"] = knjCreateBtn($objForm, "btn_committee1_3", "委員会選択", $extra);
    
    //委員会選択2
    $target = "CLASSACT01";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=1&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee2_1"] = knjCreateBtn($objForm, "btn_committee2_1", "委員会選択", $extra);
    $target = "CLASSACT02";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=2&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee2_2"] = knjCreateBtn($objForm, "btn_committee2_2", "委員会選択", $extra);
    $target = "CLASSACT03";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=3&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee2_3"] = knjCreateBtn($objForm, "btn_committee2_3", "委員会選択", $extra);

    //部活動選択
    $label = "部活動選択";
    $target = "CLUBACT01";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=1&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"".$setdis;
    $arg["button"]["btn_club1"] = knjCreateBtn($objForm, "btn_club1", $label, $extra);
    $target = "CLUBACT02";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=2&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"".$setdis;
    $arg["button"]["btn_club2"] = knjCreateBtn($objForm, "btn_club2", $label, $extra);
    $target = "CLUBACT03";
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=3&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"".$setdis;
    $arg["button"]["btn_club3"] = knjCreateBtn($objForm, "btn_club3", $label, $extra);
    
    //検定選択1
    $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
    $target = "OTHER01";
    $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"".$setdis;
    $arg["button"]["btn_qualify1"] = knjCreateBtn($objForm, "btn_qualify1", "検定選択", $extra);
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=1&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee3_1"] = knjCreateBtn($objForm, "btn_committee3_1", "委員会選択", $extra);
    $target = "OTHER02";
    $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"".$setdis;
    $arg["button"]["btn_qualify2"] = knjCreateBtn($objForm, "btn_qualify2", "検定選択", $extra);
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=2&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee3_2"] = knjCreateBtn($objForm, "btn_committee3_2", "委員会選択", $extra);
    $target = "OTHER03";
    $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"".$setdis;
    $arg["button"]["btn_qualify3"] = knjCreateBtn($objForm, "btn_qualify3", "検定選択", $extra);
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=3&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee3_3"] = knjCreateBtn($objForm, "btn_committee3_3", "委員会選択", $extra);
    $target = "OTHER04";
    $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"".$setdis;
    $arg["button"]["btn_qualify4"] = knjCreateBtn($objForm, "btn_qualify4", "検定選択", $extra);
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=1&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee3_4"] = knjCreateBtn($objForm, "btn_committee3_4", "委員会選択", $extra);
    $target = "OTHER05";
    $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"".$setdis;
    $arg["button"]["btn_qualify5"] = knjCreateBtn($objForm, "btn_qualify5", "検定選択", $extra);
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=2&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee3_5"] = knjCreateBtn($objForm, "btn_committee3_5", "委員会選択", $extra);
    $target = "OTHER06";
    $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"".$setdis;
    $arg["button"]["btn_qualify6"] = knjCreateBtn($objForm, "btn_qualify6", "検定選択", $extra);
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER=3&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
    $arg["button"]["btn_committee3_6"] = knjCreateBtn($objForm, "btn_committee3_6", "委員会選択", $extra);

    
    //更新ボタン
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }

    //取消ボタン
    $extra = " onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"btnEnd();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
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
        $extra = "style=\"height:".$height."px;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
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
