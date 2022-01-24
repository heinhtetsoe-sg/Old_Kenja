<?php

require_once('for_php7.php');

class knja126pForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja126pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knja126pQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        if($model->schregno){
            //行動の記録・特別活動の記録
            $behavior = "";
            $result = $db->query(knja126pQuery::getBehavior($model));
            while($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $behavior .= $row1["DIV"].$row1["CODE"].$row1["ANNUAL"];
            }

            //学習記録データ
            $query = knja126pQuery::getStudyRec($model);
            $result = $db->query($query);
            $study = "";
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	                $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	                          $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	            }
            } else {
	            while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	                $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	                          $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	            }
            }

            //出欠記録データ
            $attend = $db->getRow(knja126pQuery::getAttendRec($model), DB_FETCHMODE_ASSOC);

            //HTRAINREMARK_DATのハッシュ値取得
            $hash = ($model->schregno && $row) ? $model->makeHash($row, $behavior, $study, $attend) : "";
            //ATTEST_OPINIONS_DATのハッシュ値取得
            $opinion = $db->getRow(knja126pQuery::getOpinionsDat($model), DB_FETCHMODE_ASSOC);

            //ハッシュ値の比較
            if(($opinion && $row && ($opinion["OPINION"] != $hash)) || (!$hash && $opinion)) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //コミュニケーションへの関心・意欲・態度
        $extra = "style=\"height:62px;\"";
        $arg["data"]["FOREIGNLANGACT1"] = knjCreateTextArea($objForm, "FOREIGNLANGACT1", 4, 21, "soft", $extra, $row["FOREIGNLANGACT1"]);

        //外国語への慣れ親しみ
        $extra = "style=\"height:62px;\"";
        $arg["data"]["FOREIGNLANGACT2"] = knjCreateTextArea($objForm, "FOREIGNLANGACT2", 4, 21, "soft", $extra, $row["FOREIGNLANGACT2"]);

        //言語や文化に関する気付き
        $extra = "style=\"height:62px;\"";
        $arg["data"]["FOREIGNLANGACT3"] = knjCreateTextArea($objForm, "FOREIGNLANGACT3", 4, 21, "soft", $extra, $row["FOREIGNLANGACT3"]);

        //総合所見及び指導上参考となる諸事項
        $extra = "style=\"height:215px;\"";
        $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 15, 45, "soft", $extra, $row["TOTALREMARK"]);

        //学習活動
        $extra = "style=\"height:118px;\"";
        $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 17, "soft", $extra, $row["TOTALSTUDYACT"]);

        //観点
        $arg["data"]["VIEWREMARK"] = knjCreateTextArea($objForm, "VIEWREMARK", 8, 15, "soft", $extra, $row["VIEWREMARK"]);

        //評価
        $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 31, "soft", $extra, $row["TOTALSTUDYVAL"]);

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //署名チェック
        $query = knja126pQuery::getOpinionsWk($model);
        $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opinion = ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) ? false : true;

        //学校種別
        $schoolkind = $db->getOne(knja126pQuery::getSchoolKind($model));

        //ボタン作成
        makeBtn($objForm, $arg, $model, $opinion, $schoolkind);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEMES_CNT", $model->control["学期数"]);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja126pForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $opinion, $schoolkind)
{
    if((AUTHORITY < DEF_UPDATE_RESTRICT) || !$opinion || $schoolkind != 'P'){
        //更新ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
        //前の生徒へボタン
        $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('pre');\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
        //次の生徒へボタン
        $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('next');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
    } else {
        //更新ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = updateNext($model, $objForm, $arg, 'btn_update');
    }
    //取消ボタン
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //行動の記録・特別活動の記録ボタン
    $extra = "onclick=\"return btn_submit('form2');\"";
    $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の記録・特別活動の記録", $extra);

    //通知表所見参照ボタン
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "通知表所見参照", $extra);

    //出欠の記録参照ボタン
    $extra = "onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "出欠の記録参照", $extra);

    //CSV処理
    $fieldSize  = "FOREIGNLANGACT1=120,";
    $fieldSize .= "FOREIGNLANGACT2=120,";
    $fieldSize .= "FOREIGNLANGACT3=120,";
    $fieldSize .= "TOTALSTUDYACT=192,";
    $fieldSize .= "VIEWREMARK=168,";
    $fieldSize .= "TOTALSTUDYVAL=360,";
    $fieldSize .= "TOTALREMARK=990,";
    $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * (int)$model->attendrec_remark_gyou * 3).",";
    $fieldSize .= "SPECIALACTREMARK=510";

    //CSVボタン
    $extra = ($model->schregno && $schoolkind == 'P') ? "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A125P/knjx_a125pindex.php?cmd=sign&FIELDSIZE=".$fieldSize."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
    $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);
}

function updateNext(&$model, &$objForm, &$arg, $btn='btn_update'){

    //更新ボタン
    $extra = "style=\"width:130px\" onclick=\"top.left_frame.updateNext(self, 'pre','".$btn ."');\"";
    $btn_up_pre = KnjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);

    //更新ボタン
    $extra = "style=\"width:130px\" onclick=\"top.left_frame.updateNext(self, 'next','".$btn ."');\"";
    $btn_up_next = KnjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);

    if ($_POST["_ORDER"] == "pre" || $_POST["_ORDER"] == "next" ){
       $order = $_POST["_ORDER"];
       if (!isset($model->warning)){
            $arg["jscript"] = "top.left_frame.nextLink('".$order."')";
            unset($model->message);
       }
    }

    knjCreateHidden($objForm, "_ORDER");
                    
    return $btn_up_pre .$btn_up_next;
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = (int)$gyou % 5;
            $minus = ((int)$gyou / 5) > 1 ? ((int)$gyou / 5) * 6 : 5;
        }
        $height = (int)$gyou * 13.5 + ((int)$gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
