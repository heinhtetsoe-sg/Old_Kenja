<?php

require_once('for_php7.php');

class knja063aForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //$arg["Read"] = "start();";

        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knja063aindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //割振り元クラスの切替    1:ＨＲクラス 2:複式クラス
        $opt = array(1, 2);
        $model->hr_kirikae = ($model->hr_kirikae == "") ? "1" : $model->hr_kirikae;
        $extra = array("id=\"HR_KIRIKAE1\" onclick=\"return btn_submit('main');\"", "id=\"HR_KIRIKAE2\" onclick=\"return btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "HR_KIRIKAE", $model->hr_kirikae, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        if ($model->hr_kirikae == "2") {
            $arg["ghr_class"] = 1;
            $arg["NOT_REGD_COMMENT"] = "※ [無]は更新対象年度・学期のＨＲクラスが設定されていない{$model->sch_label}です。";
        } else {
            //年度・学期コンボ
            $query = knja063aQuery::getTerm($model);
            $extra = "onchange=\"btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, $model->term, "TERM", $extra, 1);
            $arg["hr_class"] = 1;
        }

        //左クラスコンボ
        $term = ($model->hr_kirikae == "2") ? $model->yearsem["L"]["value"] : $model->term;
        $query = knja063aQuery::getGhrCd($model, $term);
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->ghr_cd, "GHR_CD", $extra, 1);

        //右クラスコンボ
        if ($model->hr_kirikae == "2") {
            $query = knja063aQuery::getGhrCd($model, $model->yearsem["R"]["value"], "right");
        } else {
            $query = knja063aQuery::getHrClass($model);
        }
        $extra = "onchange=\"btn_submit('change_hr_class');\"";
        makeCmb($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1);

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //生徒の登録チェック
        $model->getGhrStudentCnt = $db->getOne(knja063aQuery::getGhrStudents($model, "COUNT"));

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja063aForm1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    //左生徒リスト(溜める式)
    $selectdata      = ($model->selectdata != "")       ? explode(",", $model->selectdata)      : array();
    $selectdataLabel = ($model->selectdataLabel != "")  ? explode(",", $model->selectdataLabel) : array();

    //左生徒リスト
    $cnt = 0;
    $opt_left = array();
    if ($model->cmd == 'change_hr_class' ) {
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[] = array("label" => $selectdataLabel[$i],
                                "value" => $selectdata[$i]);
            $cnt++;
        }
    } else {
        if ($model->hr_kirikae == "2") {
            $query = knja063aQuery::getGhrStudents($model, "left");
        } else {
            $query = knja063aQuery::getGhrStudents($model);
        }
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attendno = (strlen($row["ATTENDNO"]) > 0) ? $row["ATTENDNO"] : "&nbsp;&nbsp;&nbsp;";
            $opt_left[] = array("label" => $row["HOUTEI_HR_NAME"].$row["HOUTEI_ATTENDNO"]."番(".$attendno."番)　　　".$row["SCHREGNO"]."　".$row["NAME"],
                                "value" => $row["SCHREGNO"]);
            $cnt++;
        }
        $result->free();
    }
    $arg["RIGHT_NUM"] = $cnt;

    //右生徒リスト
    $cnt = 0;
    $opt_right = array();
    if ($model->hr_kirikae == "2") {
        $query = knja063aQuery::getGhrStudents($model, "right");
    } else {
        $query = knja063aQuery::getHrStudents($model);
    }
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->hr_kirikae == "2") {
            $not_regd_flg = ($row["NOT_REGD"] == "1") ? " [無] " : "　　　";
            $attendno = (strlen($row["ATTENDNO"]) > 0) ? $row["ATTENDNO"] : "&nbsp;&nbsp;&nbsp;";
            $opt_right[] = array("label" => $row["HOUTEI_HR_NAME"].$row["HOUTEI_ATTENDNO"]."番(".$attendno."番)".$not_regd_flg.$row["SCHREGNO"]."　".$row["NAME"],
                                 "value" => $row["SCHREGNO"]);
        } else {
            $opt_right[]= array("label" => $row["HR_NAME"].$row["ATTENDNO"]."番　".$row["SCHREGNO"]."　".$row["NAME"],
                                "value" => $row["SCHREGNO"]);
        }
        $cnt++;
    }
    $result->free();
    $arg["LEFT_NUM"] = $cnt;

    //生徒一覧リスト(右)
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"moveStudent('left')\"";
    $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_PART", "", $opt_right, $extra, 35);
    if ($model->hr_kirikae == "2") {
        $arg["main_part"]["RIGHT_LABEL"] = $model->yearsem["R"]["label"]."　複式クラス{$model->sch_label}一覧";
    } else {
        $arg["main_part"]["RIGHT_LABEL"] = "ＨＲクラス{$model->sch_label}一覧";
    }

    //対象者一覧リスト(左)
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"moveStudent('right')\"";
    $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_PART", "", $opt_left, $extra, 35);

    if ($model->hr_kirikae == "2") {
        $arg["main_part"]["LEFT_LABEL"] = $model->yearsem["L"]["label"]."　更新対象複式クラス{$model->sch_label}一覧";
    } else {
        $arg["main_part"]["LEFT_LABEL"] = "更新対象複式クラス{$model->sch_label}一覧";
    }

    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('sel_add_all');\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('left');\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('right');\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('sel_del_all');\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //出席番号設定ボタン
    if ($model->getGhrStudentCnt == 0) {
        $extra = "onClick=\"openCheck();\"";
    } else {
        if ($model->hr_kirikae == "2") {
            list ($year, $semester) = preg_split("/-/", $model->yearsem["L"]["value"]);
        } else {
            $year       = $model->year;
            $semester   = $model->semester;
        }
        $subdata  = "wopen('".REQUESTROOT."/A/KNJA063AS1/knja063as1index.php?cmd=&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJA063A&SEND_KIRIKAE={$model->hr_kirikae}&SEND_YEAR={$year}&SEND_SEMESTER={$semester}&SEND_GHR_CD={$model->ghr_cd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        if (!$model->prgid) {
            $extra = "onclick=\"openCheck2()||$subdata;closecheck();\"";
        } else {
            $extra = "onclick=\"openCheck2()||$subdata\"";
        }
    }
    $arg["button"]["btn_attendno"] = knjCreateBtn($objForm, "btn_attendno", "出席番号設定", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataLabel");
    knjCreateHidden($objForm, "SCH_LABEL", $model->sch_label);
}
?>
