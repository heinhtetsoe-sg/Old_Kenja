<?php
class knjh442bForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjh442bindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = knjh442bQuery::getTrainRow($model->schregno, $model->exp_year, $model);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //卒業可能な学年か判定
        $getData = knjh442bQuery::getGraduationGrade($model);
        $model->GradGrade = "";
        $model->GradGrade = $getData["FLG"];
        $model->schoolKind = "";
        $model->schoolKind = $getData["SCHOOL_KIND"];
        $model->AllGrade = "";
        $model->AllGrade = $getData["FLG2"];

        //高校
        $notHDisabled = "";
        if ($model->schregno && $model->schoolKind != "H") {
            $arg["err_alert"] = "alert('更新対象外の生徒です。');";
            $notHDisabled = " disabled ";
        }
        $disabled = ($model->schregno) ? "" : "disabled";

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        
        $readOnly = "";
        $setStyle = "";

        //１年
        $extra = " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["GRADE1_REMARK"] = KnjCreateTextArea($objForm, "GRADE1_REMARK", 5, 100, "soft", $extra, $row["GRADE1_REMARK"]);
        $arg["data"]["GRADE1_REMARK_TYUI"] = '(全角50文字)';

        //２年
        $extra = " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["GRADE2_REMARK"] = KnjCreateTextArea($objForm, "GRADE2_REMARK", 5, 100, "soft", $extra, $row["GRADE2_REMARK"]);
        $arg["data"]["GRADE2_REMARK_TYUI"] = '(全角50文字)';

        //３年
        $extra = " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["GRADE3_REMARK"] = KnjCreateTextArea($objForm, "GRADE3_REMARK", 5, 100, "soft", $extra, $row["GRADE3_REMARK"]);
        $arg["data"]["GRADE3_REMARK_TYUI"] = '(全角50文字)';

        //条件
        $extra = " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["CONDITION"] = KnjCreateTextArea($objForm, "CONDITION", 3, 100, "soft", $extra, $row["CONDITION"]);
        $arg["data"]["CONDITION_TYUI"] = '(全角50文字X3行まで)';

        //受験希望 指定校推薦 大学・学部・学科
        $extra = $readOnly;
        $extra .= " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["HOPE_COLLEGE_NAME1"] = knjCreateTextBox($objForm, $row["HOPE_COLLEGE_NAME1"], "HOPE_COLLEGE_NAME1", 100, 100, $extra);

        //受験希望 指定校推薦 コース
        $extra = $readOnly;
        $extra .= " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["HOPE_COURSE_NAME1"] = knjCreateTextBox($objForm, $row["HOPE_COURSE_NAME1"], "HOPE_COURSE_NAME1", 100, 100, $extra);

        //受験希望 日大付属選抜 大学・学部・学科
        $extra = $readOnly;
        $extra .= " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["HOPE_COLLEGE_NAME2"] = knjCreateTextBox($objForm, $row["HOPE_COLLEGE_NAME2"], "HOPE_COLLEGE_NAME2", 100, 100, $extra);

        //受験希望 日大付属選抜 コース
        $extra = $readOnly;
        $extra .= " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["HOPE_COURSE_NAME2"] = knjCreateTextBox($objForm, $row["HOPE_COURSE_NAME2"], "HOPE_COURSE_NAME2", 100, 100, $extra);

        $arg["IFRAME"] = VIEW::setIframeJs();

        $year1 = "";
        $year2 = "";
        if ($model->schregno) {
            $year1 = knjh442bQuery::getTargetGrade($model, 2);
            $year2 = knjh442bQuery::getTargetGrade($model, 1);
        }

        //部活選択ボタン（１年）
        $arg["button"]["btn_club_grade1"] = makeSelectBtn($objForm, $model, "club", "btn_club_grade1", "部活選択", "GRADE1_REMARK", $year1, $disabled);
        //委員会選択ボタン（１年）
        $arg["button"]["btn_committee_grade1"] = makeSelectBtn($objForm, $model, "committee", "btn_committee_grade1", "委員会選択", "GRADE1_REMARK", $year1, $disabled);
        //記録選択ボタン（１年）
        $arg["button"]["btn_kirokubikou_grade1"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_kirokubikou_grade1", "記録選択", "GRADE1_REMARK", $year1, $disabled);
        //資格選択ボタン（１年）
        $arg["button"]["btn_qualified_grade1"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified_grade1", "資格選択", "GRADE1_REMARK", $year1, $disabled);

        //部活選択ボタン（２年）
        $arg["button"]["btn_club_grade2"] = makeSelectBtn($objForm, $model, "club", "btn_club_grade2", "部活選択", "GRADE2_REMARK", $year2, $disabled);
        //委員会選択ボタン（２年）
        $arg["button"]["btn_committee_grade2"] = makeSelectBtn($objForm, $model, "committee", "btn_committee_grade2", "委員会選択", "GRADE2_REMARK", $year2, $disabled);
        //記録選択ボタン（２年）
        $arg["button"]["btn_kirokubikou_grade2"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_kirokubikou_grade2", "記録選択", "GRADE2_REMARK", $year2, $disabled);
        //資格選択ボタン（２年）
        $arg["button"]["btn_qualified_grade2"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified_grade2", "資格選択", "GRADE2_REMARK", $year2, $disabled);

        //部活選択ボタン（３年）
        $arg["button"]["btn_club_grade3"] = makeSelectBtn($objForm, $model, "club", "btn_club_grade3", "部活選択", "GRADE3_REMARK", $model->exp_year, $disabled);
        //委員会選択ボタン（３年）
        $arg["button"]["btn_committee_grade3"] = makeSelectBtn($objForm, $model, "committee", "btn_committee_grade3", "委員会選択", "GRADE3_REMARK", $model->exp_year, $disabled);
        //記録選択ボタン（３年）
        $arg["button"]["btn_kirokubikou_grade3"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_kirokubikou_grade3", "記録選択", "GRADE3_REMARK", $model->exp_year, $disabled);
        //資格選択ボタン（３年）
        $arg["button"]["btn_qualified_grade3"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified_grade3", "資格選択", "GRADE3_REMARK", $model->exp_year, $disabled);

        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $notHDisabled.$extra);

        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $notHDisabled.$extra, "reset");

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        $fieldSize  = "GRADE1_REMARK=150,";
        $gyouSize   = "GRADE1_REMARK=0,";
        $fieldSize .= "GRADE2_REMARK=150,";
        $gyouSize  .= "GRADE2_REMARK=0,";
        $fieldSize .= "GRADE3_REMARK=150,";
        $gyouSize  .= "GRADE3_REMARK=0,";
        $fieldSize .= "CONDITION=150,";
        $gyouSize  .= "CONDITION=3,";
        $fieldSize .= "HOPE_COLLEGE_NAME1=150,";
        $gyouSize  .= "HOPE_COLLEGE_NAME1=0,";
        $fieldSize .= "HOPE_COURSE_NAME1=150,";
        $gyouSize  .= "HOPE_COURSE_NAME1=0,";
        $fieldSize .= "HOPE_COLLEGE_NAME2=150,";
        $gyouSize  .= "HOPE_COLLEGE_NAME2=0,";
        $fieldSize .= "HOPE_COURSE_NAME2=150,";
        $gyouSize  .= "HOPE_COURSE_NAME2=0,";

        //セキュリティーチェック
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knjh442bQuery::getSecurityHigh());
        Query::dbCheckIn($db);
        if ($model->getPrgId || $securityCnt == 0) {
            $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX442B/knjx442bindex.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJH442B&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = knjCreateBtn($objForm, "btn_check1", "ＣＳＶ処理", $notHDisabled.$extra);
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJH442B");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjh442bForm1.html", $arg);
    }
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $year, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                  //部活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } elseif ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"";
        } elseif ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
