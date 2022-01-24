<?php

require_once('for_php7.php');

class knjz210fForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz210findex.php", "", "edit");
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //取得した科目（小学校で言う教科）
        $arg["SUBCLASSCD"]  = $model->subclasscd;
        $arg["SUBCLASSNAME"] = $model->subclassname;
        
        //学期数の取得
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $semester_count = $db->getOne(knjz210fQuery::getSemestercount($setNameCd));
        
        //観点の満点登録チェック
        $model->disabled = "";
        if ($model->subclasscd != "" && $model->grade != "") {
            $result = $db->query(knjz210fQuery::getViewlist($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //JVIEWSTAT_LEVEL_MST登録データおよび段階値のMAXの上限値チェック(観点および上限値)
                $getCount = $db->getOne(knjz210fQuery::getCheckViewcd($model, $row["VIEWCD"]));
                if ($getCount == 0) {
                    $model->disabled = "disabled";
                    $arg["viewCheck"] = "viewCheck();";
                }
            }
            $result->free();
        }
        
        //段階値数チェック
        $countAssess = $db->getOne(knjz210fQuery::selectCountQuery($model));
        
        //段階値の上限値の最大値取得
        $model->max_assesshight = $db->getOne(knjz210fQuery::getMaxAssessHight($model));
        
        $getCount = "3";
        if ($countAssess > 0 && $model->cmd == "edit") {
            $model->field["MAX_ASSESSLEVEL"] = $countAssess;
        } else {
            if ($model->cmd == "kakutei") {
                $model->field["MAX_ASSESSLEVEL"] = ($model->field["MAX_ASSESSLEVEL"] != "") ? $model->field["MAX_ASSESSLEVEL"] : $getCount;
            } else {
                if ($model->field["MAX_ASSESSLEVEL"] == "") {
                    $model->field["MAX_ASSESSLEVEL"] = $getCount;
                }
            }
        }
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["MAX_ASSESSLEVEL"] = knjCreateTextBox($objForm, $model->field["MAX_ASSESSLEVEL"], "MAX_ASSESSLEVEL", 1, 1, $extra);

        //初期化
        $model->data = array();
        $counter = 0;
        //一覧表示
        $result = $db->query(knjz210fQuery::getRow($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
            //段階値
            $extra = "";
            if ($countAssess == 0 && $model->fields["ASSESSLEVEL"][$counter] == "") {
                $value = $row["NAMESPARE2"];
            } else {
                $value = (!isset($model->warning)) ? $row["ASSESSLEVEL"] : $model->fields["ASSESSLEVEL"][$counter];
            }
            $row["ASSESSLEVEL"] = knjCreateTextBox($objForm, $value, "ASSESSLEVEL-".$counter, 4, 2, $extra);
            
            //下限値のテキストボックス
            if ($row["NAMESPARE2"] != '1') {
                $extra = "\" onblur=\"isNumb(this, ".($row["NAMESPARE2"] -1).");\"";
                $value = (!isset($model->warning)) ? $row["ASSESSLOW"] : $model->fields["ASSESSLOW"][$counter];
                $row["ASSESSLOW"] = knjCreateTextBox($objForm, $value, "ASSESSLOW-".$counter, 4, 2, $extra);
            } else {
                $row["ASSESSLOW"] = '1';
            }
            
            //上限値の表示
            if ($counter == 0 ) {
                $row["ASSESSHIGH"] = $model->max_assesshight;
                $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                $row["ASSESSHIGHTEXT"] .= $row["NAMESPARE2"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                $row["ASSESSHIGHTEXT"] .= "</span>";
            } else {
                if ($row["ASSESSHIGH"] != "") {
                    $row["ASSESSHIGH"] = $row["ASSESSHIGH"];
                }
                $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                $row["ASSESSHIGHTEXT"] .= $row["NAMESPARE2"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                $row["ASSESSHIGHTEXT"] .= "</span>";
            }
            //段階値の上限値をhiddenで保持
            knjCreateHidden($objForm, "Assesshightvalue".$row["NAMESPARE2"], $row["ASSESSHIGH"]);
            
            //上限値を配列で取得
            $model->data["ASSESSHIGH"][] = $row["ASSESSHIGH"];
            $model->data["NAMESPARE2"][] = $row["NAMESPARE2"];
            $counter++;            
            
            $arg["data"][] = $row;

        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJZ210F");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        $arg["finish"]  = $objForm->get_finish();
        
        Query::dbCheckIn($db);
        
        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge_cd' && $model->cmd != 'reset' && $model->cmd != 'kakutei'){
            $arg["reload"]  = "parent.left_frame.location.href='knjz210findex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210fForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    //学年教科評定設定ボタンを作成する
    /*$extra = "onclick=\" wopen('".REQUESTROOT."/Z/KNJZ210F/knjz210findex.php?&SEND_PRGRID=KNJZ210F&SEND_AUTH={$model->auth}&SEND_CLASSCD={$model->field["CLASSCD"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_GRADE={$model->field["GRADE"]}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"closeWin();\"";
    $arg["btn_settei"] = knjCreateBtn($objForm, "btn_settei", "学年教科評定設定", $extra);*/
    //確定を作成する
    $extra = "onclick=\"return btn_submit('kakutei');\"";
    $arg["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra.$model->disabled);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$model->disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$model->disabled);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
