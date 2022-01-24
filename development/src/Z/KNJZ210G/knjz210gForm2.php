<?php

require_once('for_php7.php');

class knjz210gForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz210gindex.php", "", "edit");
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //取得した科目（小学校で言う教科）
        $arg["SUBCLASSCD"]  = $model->subclasscd;
        $arg["SUBCLASSNAME"] = $db->getOne(knjz210gQuery::getSubclassName($model));
        $arg["STUDYREC_VIEWCD"]  = $model->studyrec_viewcd;

        //学期数の取得
        $setNameCd = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setSchoolKind = $db->getOne(knjz210gQuery::getSchoolKind($model->getGrade));
            $setNameCd = "Z".$setSchoolKind."09";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $semester_count = $db->getOne(knjz210gQuery::getSemestercount($setNameCd));
        
        //段階値の最大値取得
        $max_assesslevel = $db->getOne(knjz210gQuery::getAssesslevel('D028'));

        //学期ごとにviewflgの数を確認
        for ($checksemester = 1; $checksemester <= $semester_count; $checksemester++) {
            $viewflg_check += $db->getOne(knjz210gQuery::getViewflg($model, $checksemester));
        }

        //初期化
        $model->data = array();
        $counter = 0;
        //一覧表示
        $result = $db->query(knjz210gQuery::getRow($model, 'D028'));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
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
                //段階値が最大の時の計算は段階値 * フラグチェック数 (評定区分の時は全ての観点コードのフラグチェック数)
                $row["ASSESSHIGH"] = $max_assesslevel * $viewflg_check;
                $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                $row["ASSESSHIGHTEXT"] .= $row["NAMESPARE2"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                $row["ASSESSHIGHTEXT"] .= $max_assesslevel * $viewflg_check;
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
                //hidden
                if ($row["NAMESPARE2"] == '2') {
                    knjCreateHidden($objForm, "Assesshightvalue2", $row["ASSESSHIGH"]);
                } else if ($row["NAMESPARE2"] == '1') {
                    knjCreateHidden($objForm, "Assesshightvalue1", $row["ASSESSHIGH"]);
                }
            }
            
            //上限値を配列で取得
            $model->data["ASSESSHIGH"][] = $row["ASSESSHIGH"];
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
        knjCreateHidden($objForm, "PRGID", "KNJZ210G");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        $arg["finish"]  = $objForm->get_finish();
        
        Query::dbCheckIn($db);
        
        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge_cd'){
            $arg["reload"]  = "parent.left_frame.location.href='knjz210gindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210gForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    //学年教科評定設定ボタンを作成する
    /*$extra = "onclick=\" wopen('".REQUESTROOT."/Z/KNJZ210G/knjz210gindex.php?&SEND_PRGRID=KNJZ210G&SEND_AUTH={$model->auth}&SEND_CLASSCD={$model->field["CLASSCD"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_GRADE={$model->field["GRADE"]}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"closeWin();\"";
    $arg["btn_settei"] = knjCreateBtn($objForm, "btn_settei", "学年教科評定設定", $extra);*/
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
