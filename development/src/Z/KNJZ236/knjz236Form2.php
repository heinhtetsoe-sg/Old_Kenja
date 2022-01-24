<?php

require_once('for_php7.php');

class knjz236Form2 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz236index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル設定
        $ChosenData = makeTitle($arg, $db, $model);

        //教科名称取得
        $arg["rightclasscd"] = $db->getOne(knjz236Query::GetClassName($model));

        //科目リストToリスト作成
        $leftCnt = makeSubclassList($objForm, $arg, $db, $model);

        //全部代替/一部代替ラジオボタン 1:全部 2:一部
        $opt = array(1, 2);
        $model->substitution_type_flg = (!$model->substitution_type_flg) ? 1 : $model->substitution_type_flg;
        $extra = array("id=\"SUBSTITUTION_TYPE_FLG1\"", "id=\"SUBSTITUTION_TYPE_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "SUBSTITUTION_TYPE_FLG", $model->substitution_type_flg, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if ($model->record_dat_flg == "1"){
            $arg["show_confirm"] = "Show_Confirm();";
        }

        if (isset($model->message)) { //更新できたら左のリストを再読込
            //更新後、学科設定画面を表示します。
            if (0 < $leftCnt) {
                //代替先科目
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $param_subclasscd = $ChosenData["CLASSCD"]."-".$ChosenData["SCHOOL_KIND"]."-".$ChosenData["CURRICULUM_CD"]."-".$ChosenData["SUBCLASSCD"];
                } else {
                    $param_subclasscd = $ChosenData["SUBCLASSCD"];
                }
                //リンク元のプログラムＩＤ
                $prgid = PROGRAMID;
                //リンク先のURL
                $jump = REQUESTROOT."/Z/KNJZ236_2/knjz236_2index.php";
                //URLパラメータ
                $param  = "?PROGRAMID={$prgid}";
                $param .= "&PARAM_SUBCLASSCD={$param_subclasscd}";
                $param .= "&URL_SCHOOLKIND={$model->urlSchoolKind}";
                $param .= "&URL_SCHOOLCD={$model->urlSchoolCd}";
                $param .= "&MN_ID={$model->mnId}";
                $arg["showKogamen"] = "openKogamen('{$jump}{$param}');";
            } else {
                $arg["reload"] = "window.open('knjz236index.php?cmd=list&init=1', 'left_frame');";
            }
        }

        View::toHTML($model, "knjz236Form2.html", $arg); 
    }
}
/***************************************** 以下関数 *********************************************/
//タイトル設定
function makeTitle(&$arg, $db, $model) {
    $ChosenData = array();
    $ChosenData = $db->getRow(knjz236Query::getChosenData($model, $model->subclasscd),DB_FETCHMODE_ASSOC);
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        $arg["info"]    = array("TOP"        => "代替先科目 : ".$ChosenData["CLASSCD"]."-".$ChosenData["SCHOOL_KIND"]."-".$ChosenData["CURRICULUM_CD"]."-".$ChosenData["SUBCLASSCD"]."&nbsp;&nbsp;".$ChosenData["SUBCLASSNAME"],
                                "LEFT_LIST"  => "代替元科目",
                                "RIGHT_LIST" => "科目一覧" );
    } else {
        $arg["info"]    = array("TOP"        => "代替先科目 : ".$ChosenData["SUBCLASSCD"]."&nbsp;&nbsp;".$ChosenData["SUBCLASSNAME"],
                                "LEFT_LIST"  => "代替元科目",
                                "RIGHT_LIST" => "科目一覧" );
    }
    return $ChosenData;
}

//科目リストToリスト作成
function makeSubclassList(&$objForm, &$arg, $db, &$model) {
    $leftCnt = 0;   //読替登録件数
    $opt_left = $opt_right = array();
    if (isset($model->subclasscd)) {
        $query = knjz236Query::selectQuery($model, $model->subclasscd, $model->rightclasscd, $model->school_kind, $model->curriculum_Cd);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["ATTEND_SUBCLASSCD"]) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $opt_left[]  = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
                } else {
                    $opt_left[]  = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["SUBCLASSCD"]);
                }
                $model->substitution_type_flg = $row["SUBSTITUTION_TYPE_FLG"];
                $leftCnt++;
            } else {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $opt_right[] = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
                } else {
                    $opt_right[] = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"],
                                         "value" => $row["SUBCLASSCD"]);
                }
            }
        }
        $result->free();
    }

    //読替元科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
    $arg["main_part"]["LEFT_PART"]   = knjCreateCombo($objForm, "classyear", "right", $opt_left, $extra, 20);
    //科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
    $arg["main_part"]["RIGHT_PART"]  = knjCreateCombo($objForm, "classmaster", "left", $opt_right, $extra, 20);

    //各種ボタン
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return moves('sel_add_all');\"");
    $arg["main_part"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "＜", "onclick=\"return move('left');\"");
    $arg["main_part"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "＞", "onclick=\"return move('right');\"");
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return moves('sel_del_all');\"");

    return $leftCnt;
}

//ボタン作成
function makeButton(&$objForm, &$arg) {
    //更新ボタン
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更新", "onclick=\"return doSubmit();\"");
    //取消ボタン
    $arg["button"]["btn_clear"]  = knjCreateBtn($objForm, "btn_clear", "取消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"]    = knjCreateBtn($objForm, "btn_end", "終了", "onclick=\"closeWin();\"");

    //学科設定ボタン
    $extra = " onClick=\" wopen('".REQUESTROOT."/Z/KNJZ236_2/knjz236_2index.php?PROGRAMID=PROGRAMID','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_major"] = knjCreateBtn($objForm, "btn_major", "学科設定", $extra);
}

//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "record_dat_flg", 0);
}
?>
